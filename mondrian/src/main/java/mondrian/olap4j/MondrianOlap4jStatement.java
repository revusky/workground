/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
* Copyright (c) 2021 Sergei Semenkov
*/

package mondrian.olap4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.List;

import org.eclipse.daanse.olap.api.model.OlapElement;
import org.olap4j.CellSet;
import org.olap4j.CellSetListener;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.mdx.ParseTreeWriter;
import org.olap4j.mdx.SelectNode;

import mondrian.calc.ResultStyle;
import mondrian.olap.DrillThrough;
import mondrian.olap.Explain;
import mondrian.olap.MondrianException;
import mondrian.olap.Query;
import mondrian.olap.QueryCanceledException;
import mondrian.olap.QueryPart;
import mondrian.olap.QueryTimeoutException;
import mondrian.rolap.RolapConnection;
import mondrian.server.Execution;
import mondrian.server.Locus;
import mondrian.server.StatementImpl;
import mondrian.util.Pair;

/**
 * Implementation of {@link org.olap4j.OlapStatement}
 * for the Mondrian OLAP engine.
 *
 * @author jhyde
 * @since May 24, 2007
 */
abstract class MondrianOlap4jStatement
    extends StatementImpl
    implements OlapStatement, mondrian.server.Statement
{
    final MondrianOlap4jConnection olap4jConnection;
    private boolean closed;

    /**
     * Support for {@link #closeOnCompletion()} method.
     */
    protected boolean closeOnCompletion;

    /**
     * Current cell set, or null if the statement is not executing anything.
     * Any method which modifies this member must synchronize
     * on the MondrianOlap4jStatement.
     */
    MondrianOlap4jCellSet openCellSet;

    MondrianOlap4jStatement(
        MondrianOlap4jConnection olap4jConnection)
    {
        assert olap4jConnection != null;
        this.olap4jConnection = olap4jConnection;
        this.closed = false;
    }

    // implement Statement

    @Override
	public ResultSet executeQuery(String mdx) throws SQLException {
        return executeQuery2(mdx, false, null);
    }

    ResultSet executeQuery2(
        String mdx,
        boolean advanced,
        int[] rowCountSlot) throws SQLException
    {
        if (advanced) {
            // REVIEW: I removed 'executeDrillThroughAdvanced' in the cleanup.
            // Do we still need it?
            throw new UnsupportedOperationException();
        }
        QueryPart parseTree;
        try {
            parseTree =
                olap4jConnection.getMondrianConnection().parseStatement(mdx);
        } catch (MondrianException e) {
            throw olap4jConnection.helper.createException(
                "mondrian gave exception while parsing query", e);
        }
        if (parseTree instanceof DrillThrough drillThrough) {
            final Query query = drillThrough.getQuery();
            query.setResultStyle(ResultStyle.LIST);
            setQuery(query);
            CellSet cellSet = executeOlapQueryInternal(query, null);
            final List<Integer> coords = Collections.nCopies(
                cellSet.getAxes().size(), 0);
            final MondrianOlap4jCell cell =
                (MondrianOlap4jCell) cellSet.getCell(coords);

            List<OlapElement> fields = drillThrough.getReturnList();
            if(fields.isEmpty()) {
                MondrianOlap4jCube mondrianOlap4jCube = (MondrianOlap4jCube)cellSet.getMetaData().getCube();
                mondrian.rolap.RolapCube rolapCube = (mondrian.rolap.RolapCube)mondrianOlap4jCube.getOlapElement();

                mondrian.rolap.RolapDrillThroughAction rolapDrillThroughAction =
                        rolapCube.getDefaultDrillThroughAction();
                if(rolapDrillThroughAction != null) {
                    fields = rolapDrillThroughAction.getOlapElements();
                }
            }

            ResultSet resultSet =
                cell.drillThroughInternal(
                    drillThrough.getMaxRowCount(),
                    drillThrough.getFirstRowOrdinal(),
                    fields,
                    true,
                    null,
                    rowCountSlot);
            if (resultSet == null) {
                throw new OlapException(
                    "Cannot do DrillThrough operation on the cell");
            }
            return resultSet;
        } else if (parseTree instanceof Explain explain) {
            String plan = explainInternal(explain.getQuery());
            return olap4jConnection.factory.newFixedResultSet(
                olap4jConnection,
                Collections.singletonList("PLAN"),
                Collections.singletonList(
                    Collections.<Object>singletonList(plan)));
        } else {
            throw olap4jConnection.helper.createException(
                "Query does not have relational result. Use a DRILLTHROUGH query, or execute using the executeOlapQuery method.");
        }
    }

    private String explainInternal(QueryPart query) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        query.explain(pw);
        pw.flush();
        return sw.toString();
    }

    @Override
	public int executeUpdate(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public synchronized void close() {
        if (!closed) {
            closed = true;
            olap4jConnection.mondrianServer.removeStatement(this);
            if (openCellSet != null) {
                MondrianOlap4jCellSet c = openCellSet;
                openCellSet = null;
                c.close();
            }
        }
    }

    @Override
	public int getMaxFieldSize() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setMaxFieldSize(int max) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int getMaxRows() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setMaxRows(int max) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int getQueryTimeout() throws SQLException {
        long timeoutSeconds = getQueryTimeoutMillis() / 1000;
        if (timeoutSeconds > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (timeoutSeconds == 0 && getQueryTimeoutMillis() > 0) {
            // Don't return timeout=0 if e.g. timeoutMillis=500. 0 is special.
            return 1;
        }
        return (int) timeoutSeconds;
    }

    @Override
	public void setQueryTimeout(int seconds) throws SQLException {
        if (seconds < 0) {
            throw olap4jConnection.helper.createException(
                "illegal timeout value " + seconds);
        }
        setQueryTimeoutMillis(seconds * 1000l);
    }

    @Override
	public synchronized void cancel() throws SQLException {
        if (openCellSet != null) {
            openCellSet.cancel();
        }
    }

    @Override
	public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setCursorName(String name) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean execute(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public ResultSet getResultSet() throws SQLException {
        // NOTE: cell set becomes visible in this member while
        // executeOlapQueryInternal is still in progress, and before it has
        // finished executing. Its internal state may not be ready for API
        // calls. JDBC never claims to be thread-safe! (Except for calls to the
        // cancel method.) It is not possible to synchronize, because it would
        // block 'cancel'.
        return openCellSet;
    }

    @Override
	public int getUpdateCount() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean getMoreResults() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int getResultSetConcurrency() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int getResultSetType() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void clearBatch() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public OlapConnection getConnection() {
        return olap4jConnection;
    }

    @Override
	public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public int executeUpdate(
        String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public int executeUpdate(
        String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public int executeUpdate(
        String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean execute(
        String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean execute(
        String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean execute(
        String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public int getResultSetHoldability() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
	public void setPoolable(boolean poolable) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean isPoolable() throws SQLException {
        throw new UnsupportedOperationException();
    }

    // implement Wrapper

    @Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw olap4jConnection.helper.createException(
            new StringBuilder("does not implement '").append(iface).append("'").toString());
    }

    @Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    // implement OlapStatement

    @Override
	public CellSet executeOlapQuery(final String mdx) throws OlapException {
        final Pair<Query, MondrianOlap4jCellSetMetaData> pair = parseQuery(mdx);
        return executeOlapQueryInternal(pair.left, pair.right);
    }

    protected Pair<Query, MondrianOlap4jCellSetMetaData>
    parseQuery(final String mdx)
        throws OlapException
    {
        try {
            final RolapConnection mondrianConnection = getMondrianConnection();
            return Locus.execute(
                mondrianConnection,
                "Parsing query",
                new Locus.Action<Pair<Query, MondrianOlap4jCellSetMetaData>>() {
                    @Override
					public Pair<Query, MondrianOlap4jCellSetMetaData> execute()
                    {
                        final Query query =
                            (Query) mondrianConnection.parseStatement(
                                MondrianOlap4jStatement.this,
                                mdx,
                                null,
                                false);
                        final MondrianOlap4jCellSetMetaData cellSetMetaData =
                            new MondrianOlap4jCellSetMetaData(
                                MondrianOlap4jStatement.this, query);
                        return Pair.of(query, cellSetMetaData);
                    }
                });
        } catch (MondrianException e) {
            throw olap4jConnection.helper.createException(
                "mondrian gave exception while parsing query", e);
        }
    }

    /**
     * Executes a parsed query, closing any previously open cellset.
     *
     *
     * @param query Parsed query
     * @param cellSetMetaData Cell set metadata
     * @return Cell set
     * @throws OlapException if a database error occurs
     */
    protected CellSet executeOlapQueryInternal(
        Query query,
        MondrianOlap4jCellSetMetaData cellSetMetaData) throws OlapException
    {
        // Close the previous open CellSet, if there is one.
        synchronized (this) {
            if (openCellSet != null) {
                final MondrianOlap4jCellSet cs = openCellSet;
                openCellSet = null;
                try {
                    cs.close();
                } catch (Exception e) {
                    throw olap4jConnection.helper.createException(
                        null, "Error while closing previous CellSet", e);
                }
            }

            if (olap4jConnection.preferList) {
                query.setResultStyle(ResultStyle.LIST);
            }
            this.query = query;
            openCellSet = olap4jConnection.factory.newCellSet(this);
        }
        // Release the monitor before executing, to give another thread the
        // opportunity to call cancel.
        try {
            openCellSet.execute();
        } catch (QueryCanceledException e) {
            throw olap4jConnection.helper.createException(
                "Query canceled", e);
        } catch (QueryTimeoutException e) {
            throw olap4jConnection.helper.createException(
                e.getMessage(), e);
        } catch (MondrianException e) {
            throw olap4jConnection.helper.createException(
                "mondrian gave exception while executing query", e);
        }
        return openCellSet;
    }

    @Override
    public synchronized void start(Execution execution) {
        super.start(openCellSet);
    }

    @Override
	public CellSet executeOlapQuery(SelectNode selectNode)
        throws OlapException
    {
        final String mdx = toString(selectNode);
        return executeOlapQuery(mdx);
    }

    @Override
	public void addListener(
        CellSetListener.Granularity granularity,
        CellSetListener cellSetListener) throws OlapException
    {
        // Cell set listener API not supported in this version of mondrian.
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a {@link org.olap4j.mdx.ParseTreeNode} to MDX string.
     *
     * @param node Parse tree node
     * @return MDX text
     */
    private static String toString(ParseTreeNode node) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ParseTreeWriter parseTreeWriter = new ParseTreeWriter(pw);
        node.unparse(parseTreeWriter);
        pw.flush();
        return sw.toString();
    }

    @Override
	public RolapConnection getMondrianConnection() {
        try {
            return olap4jConnection.getMondrianConnection();
        } catch (OlapException e) {
            throw new Olap4jRuntimeException(e);
        }
    }

    /**
     * Called by each child result set (most likely a cell set) when it is
     * closed.
     *
     * @param resultSet Result set or cell set
     */
    void onResultSetClose(ResultSet resultSet) {
        if (closeOnCompletion) {
            close();
        }
    }
}

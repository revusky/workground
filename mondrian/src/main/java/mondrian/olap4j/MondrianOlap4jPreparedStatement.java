/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap4j;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import org.olap4j.CellSet;
import org.olap4j.CellSetMetaData;
import org.olap4j.OlapException;
import org.olap4j.OlapParameterMetaData;
import org.olap4j.PreparedOlapStatement;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.type.BooleanType;
import org.olap4j.type.CubeType;
import org.olap4j.type.DecimalType;
import org.olap4j.type.DimensionType;
import org.olap4j.type.HierarchyType;
import org.olap4j.type.LevelType;
import org.olap4j.type.MemberType;
import org.olap4j.type.NullType;
import org.olap4j.type.NumericType;
import org.olap4j.type.SetType;
import org.olap4j.type.StringType;
import org.olap4j.type.SymbolType;
import org.olap4j.type.TupleType;
import org.olap4j.type.Type;

import mondrian.olap.Parameter;
import mondrian.olap.Query;
import mondrian.util.Pair;

import static org.eigenbase.xom.XOMUtil.discard;

/**
 * Implementation of {@link PreparedOlapStatement}
 * for the Mondrian OLAP engine.
 *
 * <p>This class has sub-classes which implement JDBC 3.0 and JDBC 4.0 APIs;
 * it is instantiated using {@link Factory#newPreparedStatement}.</p>
 *
 * @author jhyde
 * @since Jun 12, 2007
 */
abstract class MondrianOlap4jPreparedStatement
    extends MondrianOlap4jStatement
    implements PreparedOlapStatement, OlapParameterMetaData
{

    MondrianOlap4jCellSetMetaData cellSetMetaData;

    /**
     * Creates a MondrianOlap4jPreparedStatement.
     *
     * @param olap4jConnection Connection
     * @param mdx MDX query string
     *
     * @throws OlapException if database error occurs
     */
    protected MondrianOlap4jPreparedStatement(
        MondrianOlap4jConnection olap4jConnection,
        String mdx)
        throws OlapException
    {
        super(olap4jConnection);
        final Pair<Query, MondrianOlap4jCellSetMetaData> pair = parseQuery(mdx);
        this.query = pair.left;
        this.cellSetMetaData = pair.right;
    }

    // implement PreparedOlapStatement

    @Override
	public CellSet executeQuery() throws OlapException {
        return executeOlapQueryInternal(query, cellSetMetaData);
    }

    @Override
	public OlapParameterMetaData getParameterMetaData() throws OlapException {
        return this;
    }

    @Override
	public Cube getCube() {
        return cellSetMetaData.getCube();
    }

    // implement PreparedStatement

    @Override
	public int executeUpdate() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
        getParameter(parameterIndex).setValue(null);
    }

    @Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setShort(int parameterIndex, short x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setInt(int parameterIndex, int x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setLong(int parameterIndex, long x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setBigDecimal(
        int parameterIndex, BigDecimal x) throws SQLException
    {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setString(int parameterIndex, String x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setTimestamp(
        int parameterIndex, Timestamp x) throws SQLException
    {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setAsciiStream(
        int parameterIndex, InputStream x, int length) throws SQLException
    {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setUnicodeStream(
        int parameterIndex, InputStream x, int length) throws SQLException
    {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setBinaryStream(
        int parameterIndex, InputStream x, int length) throws SQLException
    {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void clearParameters() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setObject(
        int parameterIndex, Object x, int targetSqlType) throws SQLException
    {
        getParameter(parameterIndex).setValue(x);
    }

    @Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
        final Parameter parameter = getParameter(parameterIndex);
        if (x instanceof MondrianOlap4jMember mondrianOlap4jMember) {
            x = mondrianOlap4jMember.member;
        }
        parameter.setValue(x);
    }

    @Override
	public boolean execute() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void addBatch() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setCharacterStream(
        int parameterIndex, Reader reader, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public CellSetMetaData getMetaData() {
        return cellSetMetaData;
    }

    @Override
	public void setDate(
        int parameterIndex, Date x, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setTime(
        int parameterIndex, Time x, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setTimestamp(
        int parameterIndex, Timestamp x, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setNull(
        int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setObject(
        int parameterIndex,
        Object x,
        int targetSqlType,
        int scaleOrLength) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    // implement OlapParameterMetaData

    @Override
	public String getParameterName(int param) throws OlapException {
        Parameter paramDef = getParameter(param);
        return paramDef.getName();
    }

    private Parameter getParameter(int param) throws OlapException {
        final Parameter[] parameters = query.getParameters();
        if (param < 1 || param > parameters.length) {
            //noinspection ThrowableResultOfMethodCallIgnored
            throw this.olap4jConnection.helper.toOlapException(
                this.olap4jConnection.helper.createException(
                    new StringBuilder("parameter ordinal ").append(param).append(" out of range").toString()));
        }
        return parameters[param - 1];
    }

    @Override
	public Type getParameterOlapType(int param) throws OlapException {
        Parameter paramDef = getParameter(param);
        return olap4jConnection.toOlap4j(paramDef.getType());
    }

    @Override
	public int getParameterCount() {
        return query.getParameters().length;
    }

    @Override
	public int isNullable(int param) throws SQLException {
        return ParameterMetaData.parameterNullableUnknown;
    }

    @Override
	public boolean isSigned(int param) throws SQLException {
        final Type type = getParameterOlapType(param);
        return type instanceof NumericType;
    }

    @Override
	public int getPrecision(int param) throws SQLException {
        final Type type = getParameterOlapType(param);
        if (type instanceof NumericType) {
            return 0; // precision not applicable
        }
        if (type instanceof StringType) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }

    @Override
	public int getScale(int param) throws SQLException {
        return 0; // scale not applicable
    }

    @Override
	public int getParameterType(int param) throws SQLException {
        final Type type = getParameterOlapType(param);
        if (type instanceof NumericType) {
            return Types.NUMERIC;
        } else if (type instanceof StringType) {
            return Types.VARCHAR;
        } else if (type instanceof NullType) {
            return Types.NULL;
        } else {
            return Types.OTHER;
        }
    }

    @Override
	public String getParameterTypeName(int param) throws SQLException {
        final Type type = getParameterOlapType(param);
        return type.toString();
    }

    @Override
	public String getParameterClassName(int param) throws SQLException {
        final Type type = getParameterOlapType(param);
        return foo(
            new TypeHelper<Class>() {
                @Override
				public Class booleanType(BooleanType type) {
                    return Boolean.class;
                }

                @Override
				public Class<Cube> cubeType(CubeType cubeType) {
                    return Cube.class;
                }

                @Override
				public Class<Number> decimalType(DecimalType decimalType) {
                    return Number.class;
                }

                @Override
				public Class<Dimension> dimensionType(
                    DimensionType dimensionType)
                {
                    return Dimension.class;
                }

                @Override
				public Class<Hierarchy> hierarchyType(
                    HierarchyType hierarchyType)
                {
                    return Hierarchy.class;
                }

                @Override
				public Class<Level> levelType(LevelType levelType) {
                    return Level.class;
                }

                @Override
				public Class<Member> memberType(MemberType memberType) {
                    return Member.class;
                }

                @Override
				public Class<Void> nullType(NullType nullType) {
                    return Void.class;
                }

                @Override
				public Class<Number> numericType(NumericType numericType) {
                    return Number.class;
                }

                @Override
				public Class<Iterable> setType(SetType setType) {
                    return Iterable.class;
                }

                @Override
				public Class<String> stringType(StringType stringType) {
                    return String.class;
                }

                @Override
				public Class<Member[]> tupleType(TupleType tupleType) {
                    return Member[].class;
                }

                @Override
				public Class symbolType(SymbolType symbolType) {
                    // parameters cannot be of this type
                    throw new UnsupportedOperationException();
                }
            },
            type).getName();
    }

    @Override
	public int getParameterMode(int param) throws SQLException {
        Parameter paramDef = getParameter(param); // forces param range check
        discard(paramDef);
        return ParameterMetaData.parameterModeIn;
    }

    @Override
	public boolean isSet(int parameterIndex) throws SQLException {
        return getParameter(parameterIndex).isSet();
    }

    @Override
	public void unset(int parameterIndex) throws SQLException {
        getParameter(parameterIndex).unsetValue();
    }

    // Helper classes

    private interface TypeHelper<T> {
        T booleanType(BooleanType type);
        T cubeType(CubeType cubeType);
        T decimalType(DecimalType decimalType);
        T dimensionType(DimensionType dimensionType);
        T hierarchyType(HierarchyType hierarchyType);
        T levelType(LevelType levelType);
        T memberType(MemberType memberType);
        T nullType(NullType nullType);
        T numericType(NumericType numericType);
        T setType(SetType setType);
        T stringType(StringType stringType);
        T tupleType(TupleType tupleType);
        T symbolType(SymbolType symbolType);
    }

    <T> T foo(TypeHelper<T> helper, Type type) {
        if (type instanceof BooleanType booleanType) {
            return helper.booleanType(booleanType);
        } else if (type instanceof CubeType cubeType) {
            return helper.cubeType(cubeType);
        } else if (type instanceof DecimalType decimalType) {
            return helper.decimalType(decimalType);
        } else if (type instanceof DimensionType dimensionType) {
            return helper.dimensionType(dimensionType);
        } else if (type instanceof HierarchyType hierarchyType) {
            return helper.hierarchyType(hierarchyType);
        } else if (type instanceof LevelType levelType) {
            return helper.levelType(levelType);
        } else if (type instanceof MemberType memberType) {
            return helper.memberType(memberType);
        } else if (type instanceof NullType nullType) {
            return helper.nullType(nullType);
        } else if (type instanceof NumericType numericType) {
            return helper.numericType(numericType);
        } else if (type instanceof SetType setType) {
            return helper.setType(setType);
        } else if (type instanceof StringType stringType) {
            return helper.stringType(stringType);
        } else if (type instanceof TupleType tupleType) {
            return helper.tupleType(tupleType);
        } else if (type instanceof SymbolType symbolType) {
            return helper.symbolType(symbolType);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}

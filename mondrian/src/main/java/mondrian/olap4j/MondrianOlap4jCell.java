/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2002-2017 Hitachi Vantara
// Copyright (C) 2021 Sergei Semenkov
// All rights reserved.
*/
package mondrian.olap4j;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.model.OlapElement;
import org.olap4j.AllocationPolicy;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.OlapException;
import org.olap4j.Scenario;
import org.olap4j.metadata.Property;
import org.slf4j.Logger;

import mondrian.rolap.RolapCell;
import mondrian.rolap.SqlStatement;

/**
 * Implementation of {@link Cell}
 * for the Mondrian OLAP engine.
 *
 * @author jhyde
 * @since May 24, 2007
 */
public class MondrianOlap4jCell implements Cell {
    private final int[] coordinates;
    private final MondrianOlap4jCellSet olap4jCellSet;
    final RolapCell cell;

    /**
     * Creates a MondrianOlap4jCell.
     *
     * @param coordinates Coordinates
     * @param olap4jCellSet Cell set
     * @param cell Cell in native Mondrian representation
     */
    MondrianOlap4jCell(
        int[] coordinates,
        MondrianOlap4jCellSet olap4jCellSet,
        RolapCell cell)
    {
        assert coordinates != null;
        assert olap4jCellSet != null;
        assert cell != null;
        this.coordinates = coordinates;
        this.olap4jCellSet = olap4jCellSet;
        this.cell = cell;
    }

    @Override
	public CellSet getCellSet() {
        return olap4jCellSet;
    }

    public RolapCell getRolapCell(){
        return this.cell;
    }

    @Override
	public int getOrdinal() {
        return (Integer) cell.getPropertyValue(
            mondrian.olap.Property.CELL_ORDINAL.name);
    }

    @Override
	public List<Integer> getCoordinateList() {
        ArrayList<Integer> list = new ArrayList<>(coordinates.length);
        for (int coordinate : coordinates) {
            list.add(coordinate);
        }
        return list;
    }

    @Override
	public Object getPropertyValue(Property property) {
        // We assume that mondrian properties have the same name as olap4j
        // properties.
        return cell.getPropertyValue(property.getName());
    }

    @Override
	public boolean isEmpty() {
        // FIXME
        return cell.isNull();
    }

    @Override
	public boolean isError() {
        return cell.isError();
    }

    @Override
	public boolean isNull() {
        return cell.isNull();
    }

    @Override
	public double getDoubleValue() throws OlapException {
        Object o = cell.getValue();
        if (o instanceof Number number) {
            return number.doubleValue();
        }
        throw olap4jCellSet.olap4jStatement.olap4jConnection.helper
            .createException(this, "not a number");
    }

    @Override
	public String getErrorText() {
        Object o = cell.getValue();
        if (o instanceof Throwable throwable) {
            return throwable.getMessage();
        } else {
            return null;
        }
    }

    @Override
	public Object getValue() {
        return cell.getValue();
    }

    @Override
	public String getFormattedValue() {
        return cell.getFormattedValue();
    }

    @Override
	public ResultSet drillThrough() throws OlapException {
        return drillThroughInternal(
            -1,
            -1,
            new ArrayList<>(),
            false,
            null,
            null);
    }

    /**
     * Executes drill-through on this cell.
     *
     * <p>Not a part of the public API. Package-protected because this method
     * also implements the DRILLTHROUGH statement.
     *
     * @param maxRowCount Maximum number of rows to retrieve, <= 0 if unlimited
     * @param firstRowOrdinal Ordinal of row to skip to (1-based), or 0 to
     *   start from beginning
     * @param fields            List of fields to return, expressed as MDX
     *                          expressions.
     * @param extendedContext   If true, add non-constraining columns to the
     *                          query for levels below each current member.
     *                          This additional context makes the drill-through
     *                          queries easier for humans to understand.
     * @param logger Logger. If not null and debug is enabled, log SQL here
     * @param rowCountSlot Slot into which the number of fact rows is written
     * @return Result set
     * @throws OlapException on error
     */
    ResultSet drillThroughInternal(
        int maxRowCount,
        int firstRowOrdinal,
        List<OlapElement> fields,
        boolean extendedContext,
        Logger logger,
        int[] rowCountSlot)
        throws OlapException
    {
        if (!cell.canDrillThrough()) {
            return null;
        }
        if (rowCountSlot != null) {
            rowCountSlot[0] = cell.getDrillThroughCount();
        }
        final SqlStatement sqlStmt =
            cell.drillThroughInternal(
                maxRowCount, firstRowOrdinal, fields, extendedContext,
                logger);
        return sqlStmt.getWrappedResultSet();
    }

    @Override
	public void setValue(
        Object newValue,
        AllocationPolicy allocationPolicy,
        Object... allocationArgs)
        throws OlapException
    {
        Scenario scenario =
            olap4jCellSet.olap4jStatement.olap4jConnection.getScenario();
        cell.setValue(scenario, newValue, allocationPolicy, allocationArgs);
    }
}

/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap4j;

import org.eclipse.daanse.olap.api.model.OlapElement;
import org.olap4j.OlapException;
import org.olap4j.impl.Named;
import org.olap4j.impl.NamedListImpl;
import org.olap4j.impl.Olap4jUtil;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.NamedList;

import mondrian.olap.DimensionType;
import mondrian.olap.Util;

/**
 * Implementation of {@link org.olap4j.metadata.Dimension}
 * for the Mondrian OLAP engine.
 *
 * @author jhyde
 * @since May 24, 2007
 */
class MondrianOlap4jDimension
    extends MondrianOlap4jMetadataElement
    implements Dimension, Named
{
    private final MondrianOlap4jSchema olap4jSchema;
    private final org.eclipse.daanse.olap.api.model.Dimension dimension;

    MondrianOlap4jDimension(
        MondrianOlap4jSchema olap4jSchema,
        org.eclipse.daanse.olap.api.model.Dimension dimension)
    {
        this.olap4jSchema = olap4jSchema;
        this.dimension = dimension;
    }

    @Override
	public boolean equals(Object obj) {
        return obj instanceof MondrianOlap4jDimension mo4jd
            && dimension.equals(mo4jd.dimension);
    }

    @Override
	public int hashCode() {
        return dimension.hashCode();
    }

    @Override
	public NamedList<Hierarchy> getHierarchies() {
        final NamedList<MondrianOlap4jHierarchy> list =
            new NamedListImpl<>();
        final MondrianOlap4jConnection olap4jConnection =
            olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection;
        final mondrian.olap.SchemaReader schemaReader =
            olap4jConnection.getMondrianConnection2().getSchemaReader()
            .withLocus();
        for (org.eclipse.daanse.olap.api.model.Hierarchy hierarchy
            : schemaReader.getDimensionHierarchies(dimension))
        {
            list.add(olap4jConnection.toOlap4j(hierarchy));
        }
        return Olap4jUtil.cast(list);
    }

    @Override
	public Hierarchy getDefaultHierarchy() {
        return getHierarchies().get(0);
    }

    @Override
	public Type getDimensionType() throws OlapException {
        final DimensionType dimensionType = dimension.getDimensionType();
        switch (dimensionType) {
        case STANDARD_DIMENSION:
            return Type.OTHER;
        case MEASURES_DIMENSION:
            return Type.MEASURE;
        case TIME_DIMENSION:
            return Type.TIME;
        default:
            throw Util.unexpected(dimensionType);
        }
    }

    @Override
	public String getName() {
        return dimension.getName();
    }

    @Override
	public String getUniqueName() {
        return dimension.getUniqueName();
    }

    @Override
	public String getCaption() {
        return dimension.getLocalized(
            OlapElement.LocalizedProperty.CAPTION,
            olap4jSchema.getLocale());
    }

    @Override
	public String getDescription() {
        return dimension.getLocalized(
            OlapElement.LocalizedProperty.DESCRIPTION,
            olap4jSchema.getLocale());
    }

    @Override
	public boolean isVisible() {
        return dimension.isVisible();
    }

    @Override
	protected OlapElement getOlapElement() {
        return dimension;
    }
}

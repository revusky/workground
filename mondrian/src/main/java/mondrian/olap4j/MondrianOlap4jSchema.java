/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap4j;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.daanse.olap.api.access.Role;
import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.OlapElement;
import org.olap4j.OlapException;
import org.olap4j.impl.Named;
import org.olap4j.impl.NamedListImpl;
import org.olap4j.impl.Olap4jUtil;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Schema;

/**
 * Implementation of {@link org.olap4j.metadata.Schema}
 * for the Mondrian OLAP engine.
 *
 * @author jhyde
 * @since May 24, 2007
 */
class MondrianOlap4jSchema
    extends MondrianOlap4jMetadataElement
    implements Schema, Named
{
    final MondrianOlap4jCatalog olap4jCatalog;
    final String schemaName;
    final org.eclipse.daanse.olap.api.model.Schema schema;

    /**
     * Creates a MondrianOlap4jSchema.
     *
     * <p>The name of the schema is not necessarily the same as
     * schema.getName(). If schema was loaded in a datasources.xml file, the
     * name it was given there (in the &lt;Catalog&gt; element) trumps the name
     * in the catalog.xml file.
     *
     * @param olap4jCatalog Catalog containing schema
     * @param schemaName Name of schema
     * @param schema Mondrian schema
     */
    MondrianOlap4jSchema(
        MondrianOlap4jCatalog olap4jCatalog,
        String schemaName,
        org.eclipse.daanse.olap.api.model.Schema schema)
    {
        this.olap4jCatalog = olap4jCatalog;
        this.schemaName = schemaName;
        this.schema = schema;
    }

    @Override
	public Catalog getCatalog() {
        return olap4jCatalog;
    }

    @Override
	public NamedList<Cube> getCubes() throws OlapException {
        NamedList<MondrianOlap4jCube> list =
            new NamedListImpl<>();
        final MondrianOlap4jConnection olap4jConnection =
            olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection;
        for (org.eclipse.daanse.olap.api.model.Cube cube
            : olap4jConnection.getMondrianConnection()
                .getSchemaReader().getCubes())
        {
            list.add(olap4jConnection.toOlap4j(cube));
        }
        return Olap4jUtil.cast(list);
    }

    @Override
	public NamedList<Dimension> getSharedDimensions() throws OlapException {
        final MondrianOlap4jConnection olap4jConnection =
            olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection;
        final SortedSet<MondrianOlap4jDimension> dimensions =
            new TreeSet<>(
                new Comparator<MondrianOlap4jDimension>() {
                    @Override
					public int compare(
                        MondrianOlap4jDimension o1,
                        MondrianOlap4jDimension o2)
                    {
                        return o1.getName().compareTo(o2.getName());
                    }
                }
            );
        final Role role = olap4jConnection.getMondrianConnection().getRole();
        for (Hierarchy hierarchy : schema.getSharedHierarchies()) {
            if (role.canAccess(hierarchy)) {
                dimensions.add(
                    olap4jConnection.toOlap4j(hierarchy.getDimension()));
            }
        }
        NamedList<MondrianOlap4jDimension> list =
            new NamedListImpl<>();
        list.addAll(dimensions);
        return Olap4jUtil.cast(list);
    }

    @Override
	public Collection<Locale> getSupportedLocales() throws OlapException {
        return Collections.emptyList();
    }

    @Override
	public String getName() {
        return schemaName;
    }

    /**
     * Shorthand for catalog.database.connection.getLocale().
     * Not part of the olap4j api; do not make public.
     *
     * @return Locale of current connection
     */
    final Locale getLocale() {
        return olap4jCatalog.olap4jDatabase.getOlapConnection().getLocale();
    }

    @Override
	protected OlapElement getOlapElement() {
        return null;
    }
}

/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap4j;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.daanse.olap.api.access.HierarchyAccess;
import org.eclipse.daanse.olap.api.model.OlapElement;
import org.olap4j.OlapException;
import org.olap4j.impl.ArrayNamedListImpl;
import org.olap4j.impl.Named;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;

import mondrian.olap.Util;
import mondrian.rolap.RolapConnection;
import mondrian.server.Locus;

/**
 * Implementation of {@link Level}
 * for the Mondrian OLAP engine.
 *
 * @author jhyde
 * @since May 25, 2007
 */
class MondrianOlap4jLevel
    extends MondrianOlap4jMetadataElement
    implements Level, Named
{
    final MondrianOlap4jSchema olap4jSchema;
    final org.eclipse.daanse.olap.api.model.Level level;

    /**
     * Creates a MondrianOlap4jLevel.
     *
     * @param olap4jSchema Schema
     * @param level Mondrian level
     */
    MondrianOlap4jLevel(
        MondrianOlap4jSchema olap4jSchema,
        org.eclipse.daanse.olap.api.model.Level level)
    {
        this.olap4jSchema = olap4jSchema;
        this.level = level;
    }

    @Override
	public boolean equals(Object obj) {
        return obj instanceof MondrianOlap4jLevel mo4jl
            && level.equals(mo4jl.level);
    }

    @Override
	public int hashCode() {
        return level.hashCode();
    }

    @Override
	public int getDepth() {
        return level.getDepth() - getDepthOffset();
    }

    private int getDepthOffset() {
        final HierarchyAccess accessDetails =
            olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection
                .getMondrianConnection2().getRole().getAccessDetails(
                    level.getHierarchy());
        if (accessDetails == null) {
            return 0;
        }
        return accessDetails.getTopLevelDepth();
    }

    @Override
	public Hierarchy getHierarchy() {
        return new MondrianOlap4jHierarchy(olap4jSchema, level.getHierarchy());
    }

    @Override
	public Dimension getDimension() {
        return new MondrianOlap4jDimension(olap4jSchema, level.getDimension());
    }

    @Override
	public boolean isCalculated() {
        return false;
    }

    @Override
	public Type getLevelType() {
        if (level.isAll()) {
            return Type.ALL;
        }
        switch (level.getLevelType()) {
        case REGULAR:
            return Type.REGULAR;
        case TIME_DAYS:
            return Type.TIME_DAYS;
        case TIME_HALF_YEARS:
            return Type.TIME_HALF_YEAR;
        case TIME_HOURS:
            return Type.TIME_HOURS;
        case TIME_MINUTES:
            return Type.TIME_MINUTES;
        case TIME_MONTHS:
            return Type.TIME_MONTHS;
        case TIME_QUARTERS:
            return Type.TIME_QUARTERS;
        case TIME_SECONDS:
            return Type.TIME_SECONDS;
        case TIME_UNDEFINED:
            return Type.TIME_UNDEFINED;
        case TIME_WEEKS:
            return Type.TIME_WEEKS;
        case TIME_YEARS:
            return Type.TIME_YEARS;
        case NULL:
        default:
            throw Util.unexpected(level.getLevelType());
        }
    }

    @Override
	public NamedList<Property> getProperties() {
        return getProperties(true);
    }

    /**
     * Returns a list of this level's properties, optionally including standard
     * properties that are available on every level.
     *
     * <p>NOTE: Not part of the olap4j API.
     *
     * @param includeStandard Whether to include standard properties
     * @return List of properties
     */
    NamedList<Property> getProperties(boolean includeStandard) {
        final NamedList<Property> list = new ArrayNamedListImpl<>() {
            @Override
			public String getName(Object property) {
                return ((Property)property).getName();
            }
        };
        // standard properties first
        if (includeStandard) {
            list.addAll(
                Arrays.asList(Property.StandardMemberProperty.values()));
            list.addAll(MondrianOlap4jProperty.MEMBER_EXTENSIONS.values());
        }
        // then level-specific properties
        for (mondrian.olap.Property property : level.getProperties()) {
            list.add(new MondrianOlap4jProperty(property));
        }
        return list;
    }

    @Override
	public List<Member> getMembers() throws OlapException {
        final MondrianOlap4jConnection olap4jConnection =
            olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection;
        final RolapConnection mondrianConnection =
            olap4jConnection.getMondrianConnection();
        return Locus.execute(
            mondrianConnection,
            "Reading members of level",
            new Locus.Action<List<Member>>() {
                @Override
				public List<Member> execute() {
                    final mondrian.olap.SchemaReader schemaReader =
                        mondrianConnection.getSchemaReader().withLocus();
                    final List<org.eclipse.daanse.olap.api.model.Member> levelMembers =
                        schemaReader.getLevelMembers(level, true);
                    return new AbstractList<>() {
                        @Override
						public Member get(int index) {
                            return olap4jConnection.toOlap4j(
                                levelMembers.get(index));
                        }

                        @Override
						public int size() {
                            return levelMembers.size();
                        }
                    };
                }
            });
    }

    @Override
	public String getName() {
        return level.getName();
    }

    @Override
	public String getUniqueName() {
        return level.getUniqueName();
    }

    @Override
	public String getCaption() {
        return level.getLocalized(
            OlapElement.LocalizedProperty.CAPTION,
            olap4jSchema.getLocale());
    }

    @Override
	public String getDescription() {
        return level.getLocalized(
            OlapElement.LocalizedProperty.DESCRIPTION,
            olap4jSchema.getLocale());
    }

    @Override
	public int getCardinality() {
        return level.getApproxRowCount();
    }

    @Override
	public boolean isVisible() {
        return level.isVisible();
    }

    @Override
	protected OlapElement getOlapElement() {
        return level;
    }
}

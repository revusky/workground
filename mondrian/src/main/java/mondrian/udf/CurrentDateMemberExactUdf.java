/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.udf;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.UserDefinedFunction;

import aQute.bnd.annotation.spi.ServiceProvider;
import mondrian.olap.type.HierarchyType;
import mondrian.olap.type.StringType;
import mondrian.olap.type.Type;
import mondrian.util.Format;

/**
 * User-defined function <code>CurrentDateMember</code>.  Arguments to the
 * function are as follows:
 *
 * <blockquote>
 * <code>
 * CurrentDateMember(&lt;Hierarchy&gt;, &lt;FormatString&gt;)
 * returns &lt;Member&gt;
 * </code>
 * </blockquote>
 *
 * The function returns the member from the specified hierarchy that matches
 * the current date, to the granularity specified by the &lt;FormatString&gt;.
 *
 * The format string conforms to the format string implemented by
 * {@link Format}.
 *
 * @author Zelaine Fong
 */
@ServiceProvider(value = UserDefinedFunction.class)
public class CurrentDateMemberExactUdf extends CurrentDateMemberUdf {
    private final static String CURRENT_DATE_MEMBER_EXACT_UDF_DESCRIPTION =
        "Returns the exact member within the specified dimension "
        + "corresponding to the current date, in the format specified by "
        + "the format parameter. "
        + "If there is no such date, returns the NULL member. "
        + "Format strings are the same as used by the MDX Format function, "
        + "namely the Visual Basic format strings. "
        + "See http://www.apostate.com/programming/vb-format.html.";

    @Override
	public String getDescription() {
        return CURRENT_DATE_MEMBER_EXACT_UDF_DESCRIPTION;
    }

    @Override
	public Type[] getParameterTypes() {
        return new Type[] {
            new HierarchyType(null, null),
            new StringType()
        };
    }

    @Override
	public String[] getReservedWords() {
        return null;
    }
}

/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.type;

/**
 * The type of a empty expression.
 *
 * <p>An example of an empty expression is the third argument to the call
 * <code>DrilldownLevelTop({[Store].[USA]}, 2, , [Measures].[Unit
 * Sales])</code>.
 * </p>
 *
 * @author medstat
 * @since Jan 26, 2009
 */
public class EmptyType extends ScalarType
{
    /**
     * Creates an empty type.
     */
    public EmptyType()
    {
        super("<EMPTY>");
    }

    @Override
	public boolean equals(Object obj) {
        return obj instanceof EmptyType;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}

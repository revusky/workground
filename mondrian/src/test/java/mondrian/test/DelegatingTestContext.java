/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.test;

import java.io.PrintWriter;

import mondrian.olap.Util;

/**
 * Extension of {@link FoodmartTestContextImpl} which delegates all behavior to
 * a parent test context.
 *
 * <p>Derived classes can selectively override methods.
 *
 * @author jhyde
 * @since 7 September, 2005
 */
public class DelegatingTestContext extends FoodmartTestContextImpl {
    protected final TestContext context;

    protected DelegatingTestContext(TestContext context) {
        this.context = context;
    }

    @Override
	public Util.PropertyList getConnectionProperties() {
        return context.getConnectionProperties();
    }

    @Override
	public String getDefaultCubeName() {
        return context.getDefaultCubeName();
    }

    @Override
	public PrintWriter getWriter() {
        return context.getWriter();
    }
}

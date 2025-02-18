/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.spi.impl;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import mondrian.spi.DataSourceResolver;

/**
 * Implementation of {@link mondrian.spi.DataSourceResolver} that looks up
 * a data source using JNDI.
 *
 * @author jhyde
 */
public class JndiDataSourceResolver implements DataSourceResolver {
    /**
     * Public constructor, required for plugin instantiation.
     */
    public JndiDataSourceResolver() {
    }

    @Override
	public DataSource lookup(String dataSourceName) throws NamingException {
        return (DataSource) new InitialContext().lookup(dataSourceName);
    }
}

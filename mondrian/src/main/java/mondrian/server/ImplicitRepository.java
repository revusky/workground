/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.olap4j.OlapConnection;
import org.olap4j.impl.Olap4jUtil;

import mondrian.olap.MondrianServer;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapSchema;

/**
 * Implementation of {@link Repository} for
 * a server that doesn't have a repository: each connection in the server
 * has its own catalog (specified in the connect string) and therefore the
 * catalog and schema metadata will be whatever pertains to that connection.
 * (That's why the methods have a connection parameter.)
 *
 * @author Julian Hyde
 */
public class ImplicitRepository implements Repository {
    public ImplicitRepository() {
        super();
    }

    @Override
	public List<String> getCatalogNames(
        RolapConnection connection,
        String databaseName)
    {
        // In an implicit repository, we assume that there is a single
        // database, a single catalog and a single schema.
        return
            Collections.singletonList(
                connection.getSchema().getName());
    }

    @Override
	public List<String> getDatabaseNames(RolapConnection connection)
    {
        // In an implicit repository, we assume that there is a single
        // database, a single catalog and a single schema.
        return
            Collections.singletonList(
                connection.getSchema().getName());
    }

    @Override
	public Map<String, RolapSchema> getRolapSchemas(
        RolapConnection connection,
        String databaseName,
        String catalogName)
    {
        final RolapSchema schema = connection.getSchema();
        assert schema.getName().equals(catalogName);
        return Collections.singletonMap(schema.getName(), schema);
    }

    @Override
	public OlapConnection getConnection(
        MondrianServer server,
        String databaseName,
        String catalogName,
        String roleName,
        Properties props)
    {
        // This method does not make sense in an ImplicitRepository. The
        // catalog and schema are gleaned from the connection, not vice
        // versa.
        throw new UnsupportedOperationException();
    }

    @Override
	public List<Map<String, Object>> getDatabases(
        RolapConnection connection)
    {
        return Collections.singletonList(
            Olap4jUtil.<String, Object>mapOf(
                "DataSourceName", connection.getSchema().getName(),
                "DataSourceDescription", null,
                "URL", null,
                "DataSourceInfo", connection.getSchema().getName(),
                "ProviderName", "Mondrian",
                "ProviderType", "MDP",
                "AuthenticationMode", "Unauthenticated"));
    }

    @Override
	public void shutdown() {
        // ignore.
    }
}

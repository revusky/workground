/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.rolap;

import javax.sql.DataSource;

import mondrian.olap.Util;
import mondrian.util.ByteString;
import mondrian.util.StringKey;

/**
 * Globally unique identifier for the definition of a JDBC database connection.
 *
 * <p>Two connections should have the same connection key if and only if their
 * databases have the same content.</p>
 *
 * @see RolapConnectionProperties#JdbcConnectionUuid
 *
 * @author jhyde
 */
public class ConnectionKey extends StringKey {
    private ConnectionKey(String s) {
        super(s);
    }

    static ConnectionKey create(
        final String connectionUuidStr,
        final DataSource dataSource,
        final String catalogUrl,
        final String connectionKey,
        final String jdbcUser,
        final String dataSourceStr,
        final String sessionId)
    {
        String s;
        if (connectionUuidStr != null
            && connectionUuidStr.length() != 0)
        {
            s = connectionUuidStr;
        } else {
            final StringBuilder buf = new StringBuilder(100);
            attributeValue(buf, "sessiomId", sessionId);
            if (dataSource != null) {
                attributeValue(buf, "jvm", Util.JVM_INSTANCE_UUID);
                attributeValue(
                    buf, "dataSource", System.identityHashCode(dataSource));
            } else {
                attributeValue(buf, "connectionKey", connectionKey);
                attributeValue(buf, "catalogUrl", catalogUrl);
                attributeValue(buf, "jdbcUser", jdbcUser);
                attributeValue(buf, "dataSourceStr", dataSourceStr);
            }
            s = new ByteString(Util.digestSHA(buf.toString())).toString();
        }
        return new ConnectionKey(s);
    }

    static ConnectionKey create(
            final String connectionUuidStr,
            final DataSource dataSource,
            final String catalogUrl,
            final String connectionKey,
            final String jdbcUser,
            final String dataSourceStr)
    {
        return create(
                connectionUuidStr,
                dataSource,
                catalogUrl,
                connectionKey,
                jdbcUser,
                dataSourceStr,
                null);
    }

    static void attributeValue(
        StringBuilder buf, String attribute, Object value)
    {
        if (value == null) {
            return;
        }
        if (buf.length() > 0) {
            buf.append(';');
        }
        buf.append(attribute)
            .append('=');
        Util.quoteForMdx(buf, value.toString());
    }
}

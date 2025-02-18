/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap4j;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.eclipse.daanse.engine.api.Context;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import aQute.bnd.annotation.spi.ServiceProvider;
import mondrian.rolap.RolapConnectionProperties;

/**
 * Olap4j driver for Mondrian.
 *
 * <p>Since olap4j is a superset of JDBC, you register this driver as you would
 * any JDBC driver:
 *
 * <blockquote>
 * <code>Class.forName("mondrian.olap4j.MondrianOlap4jDriver");</code>
 * </blockquote>
 *
 * <p>Then create a connection using a URL with the prefix "jdbc:mondrian:".
 * For example,
 *
 * <blockquote>
 * <code>import java.sql.Connection;<br/>
 * import java.sql.DriverManager;<br/>
 * import org.olap4j.OlapConnection;<br/>
 * <br/>
 * Connection connection =<br/>
 * &nbsp;&nbsp;DriverManager.getConnection(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;"jdbc:mondrian:Jdbc=jdbc:odbc:MondrianFoodMart;
 * Catalog=file:/mondrian/demo/FoodMart.xml;
 * JdbcDrivers=sun.jdbc.odbc.JdbcOdbcDriver");<br/>
 * OlapConnection olapConnection =<br/>
 * &nbsp;&nbsp;connection.unwrap(OlapConnection.class);</code>
 * </blockquote>
 *
 * <p>Note how we use the {@link Connection#unwrap(Class)} method to down-cast
 * the JDBC connection object to the extension {@link org.olap4j.OlapConnection}
 * object. This method is only available in the JDBC 4.0 (JDK 1.6 onwards).
 *
 * <h3>Connection properties</h3>
 *
 * <p>The driver supports the same set of properties as a traditional mondrian
 * connection. See {@link mondrian.rolap.RolapConnectionProperties}.
 *
 * <h3>Catalogs and schemas</h3>
 *
 * <p>Mondrian has a sole catalog, called "LOCALDB". You will get an error
 * if you attempt to use {@link java.sql.Connection#setCatalog(String)} to set
 * it to anything else.
 *
 * @author jhyde
 * @since May 22, 2007
 */
@ServiceProvider(value = Driver.class)
public class MondrianOlap4jDriver implements Driver {
    protected final Factory factory;
    private Context context = null;
    //static {
    //    try {
    //        register();
    //    } catch (SQLException e) {
    //        e.printStackTrace();
    //    }
    //}

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY)
    public void bindContext(Context context) {
        this.context = context;
    }

    public void unbindContextResolver(Context context) {
        this.context = context;
    }
    /**
     * Creates a MondrianOlap4jDriver.
     */
    public MondrianOlap4jDriver() {
        this.factory = new FactoryJdbc41Impl();
    }

    public MondrianOlap4jDriver(Context context) {
        this.context = context;
        this.factory = new FactoryJdbc41Impl();
    }

    @Override
	public Connection connect(String url, Properties info) throws SQLException {
        if (!MondrianOlap4jConnection.acceptsURL(url)) {
            return null;
        }
        return factory.newConnection(this, url, info, context);
    }

    @Override
	public boolean acceptsURL(String url) throws SQLException {
        return MondrianOlap4jConnection.acceptsURL(url);
    }

    @Override
	public DriverPropertyInfo[] getPropertyInfo(
        String url, Properties info) throws SQLException
    {
        List<DriverPropertyInfo> list = new ArrayList<>();

        // First, add the contents of info
        for (Map.Entry<Object, Object> entry : info.entrySet()) {
            list.add(
                new DriverPropertyInfo(
                    (String) entry.getKey(),
                    (String) entry.getValue()));
        }
        // Next, add property defns not mentioned in info
        for (RolapConnectionProperties p : RolapConnectionProperties.values()) {
            if (info.containsKey(p.name())) {
                continue;
            }
            list.add(
                new DriverPropertyInfo(
                    p.name(),
                    null));
        }
        return list.toArray(new DriverPropertyInfo[list.size()]);
    }

    // JDBC 4.1 support (JDK 1.7 and higher)
    @Override
	public Logger getParentLogger() {
        return Logger.getLogger("");
    }

    /**
     * Returns the driver name. Not in the JDBC API.
     * @return Driver name
     */
    String getName() {
        return MondrianOlap4jDriverVersion.NAME;
    }

    /**
     * Returns the driver version. Not in the JDBC API.
     * @return Driver version
     */
    String getVersion() {
        return MondrianOlap4jDriverVersion.VERSION;
    }

    @Override
	public int getMajorVersion() {
        return MondrianOlap4jDriverVersion.MAJOR_VERSION;
    }

    @Override
	public int getMinorVersion() {
        return MondrianOlap4jDriverVersion.MINOR_VERSION;
    }

    @Override
	public boolean jdbcCompliant() {
        return false;
    }
}

/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.tui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.daanse.olap.api.Connection;
import org.eclipse.daanse.olap.api.model.Cube;
import org.eclipse.daanse.olap.api.model.Dimension;
import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.Member;
import org.eclipse.daanse.olap.api.model.OlapElement;
import org.eclipse.daanse.olap.api.result.Result;
import org.eigenbase.util.property.Property;
import org.eigenbase.xom.XOMException;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;
import org.olap4j.layout.RectangularCellSetFormatter;

import mondrian.olap.Category;
import mondrian.olap.DriverManager;
import mondrian.olap.FunTable;
import mondrian.olap.MondrianProperties;
import mondrian.olap.Parameter;
import mondrian.olap.Query;
import mondrian.olap.Util;
import mondrian.olap.fun.FunInfo;
import mondrian.olap.type.TypeUtil;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.rolap.RolapCube;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathException;

/**
 * Command line utility which reads and executes MDX commands.
 *
 * <p>TODO: describe how to use this class.</p>
 *
 * @author Richard Emberson
 */
public class CmdRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdRunner.class);
    private static final String NL = Util.NL;
    public static final String FOR_PARAMETER_NAMED = "For parameter named \"";
    public static final String THE_VALUE_WAS_TYPE = "the value was type \"";
    public static final String NO_CUBE_FOUND_WITH_NAME = "No cube found with name \"";
    public static final String XML_DATA_IS_VALID = "XML Data is Valid";
    public static final String BAD_COMMAND_USAGE = "Bad command usage: \"";

    private static boolean reloadConnection = true;
    private static final String CATALOG_NAME = "FoodMart";

    private static final Map<Object, String> paraNameValues =
        new HashMap<>();

    private static String[][] commentDelim;
    private static char[] commentStartChars;
    private static boolean allowNestedComments;
    private static final boolean USE_OLAP4J = false;

    private final Options options;
    private long queryTime;
    private long totalQueryTime;
    private String filename;
    private String mdxCmd;
    private String mdxResult;
    private String error;
    private String stack;
    private String connectString;
    private Connection connection;
    private final PrintWriter out;

    static {
        setDefaultCommentState();
    }

    /**
     * Creates a <code>CmdRunner</code>.
     *
     * @param options Option set, or null to use default options
     * @param out Output writer, or null to use {@link System#out}.
     */
    public CmdRunner(Options options, PrintWriter out) {
        if (options == null) {
            options = new Options();
        }
        this.options = options;
        this.filename = null;
        this.mdxResult = null;
        this.error = null;
        this.queryTime = -1;
        if (out == null) {
            out = new PrintWriter(System.out);
        }
        this.out = out;
    }

    public void setTimeQueries(boolean timeQueries) {
        this.options.timeQueries = timeQueries;
    }

    public boolean getTimeQueries() {
        return options.timeQueries;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public long getTotalQueryTime() {
        return totalQueryTime;
    }

    public void noCubeCaching() {
        Cube[] cubes = getCubes();
        for (Cube cube : cubes) {
            RolapCube rcube = (RolapCube) cube;
            rcube.setCacheAggregations(false);
        }
    }

    void setError(String s) {
        this.error = s;
    }

    void setError(Throwable t) {
        this.error = formatError(t);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        this.stack = sw.toString();
    }

    void clearError() {
        this.error = null;
        this.stack = null;
    }

    private String formatError(Throwable mex) {
        String message = mex.getMessage();
        if (message == null) {
            message = mex.toString();
        }
        if (mex.getCause() != null && mex.getCause() != mex) {
            message = message + NL + formatError(mex.getCause());
        }
        return message;
    }

    public static void listPropertyNames(StringBuilder buf) {
        PropertyInfo propertyInfo =
                new PropertyInfo(MondrianProperties.instance());
        for (int i = 0; i < propertyInfo.size(); i++) {
            buf.append(propertyInfo.getProperty(i).getPath());
            buf.append(NL);
        }
    }

    public static void listPropertiesAll(StringBuilder buf) {
        PropertyInfo propertyInfo =
                new PropertyInfo(MondrianProperties.instance());
        for (int i = 0; i < propertyInfo.size(); i++) {
            String propertyName = propertyInfo.getPropertyName(i);
            String propertyValue = propertyInfo.getProperty(i).getString();
            buf.append(propertyName);
            buf.append('=');
            buf.append(propertyValue);
            buf.append(NL);
        }
    }

    /**
     * Returns the value of a property, or null if it is not set.
     */
    private static String getPropertyValue(String propertyName) {
        final Property property = PropertyInfo.lookupProperty(
            MondrianProperties.instance(),
            propertyName);
        return (property != null && property.isSet())
            ? property.getString()
            : null;
    }

    public static void listProperty(String propertyName, StringBuilder buf) {
        buf.append(getPropertyValue(propertyName));
    }

    public static boolean isProperty(String propertyName) {
        final Property property = PropertyInfo.lookupProperty(
            MondrianProperties.instance(),
            propertyName);
        return property != null;
    }

    public static boolean setProperty(String name, String value) {
        final Property property = PropertyInfo.lookupProperty(
            MondrianProperties.instance(),
            name);
        if (property != null && !Objects.equals(property.getString(), value)) {
            property.setString(value);
            return true;
        } else {
            return false;
        }
    }

    public void loadParameters(Query query) {
        Parameter[] params = query.getParameters();
        for (Parameter param : params) {
            loadParameter(query, param);
        }
    }

    /**
     * Looks up the definition of a property with a given name.
     */
    private static class PropertyInfo {
        private final List<Property> propertyList = new ArrayList<>();
        private final List<String> propertyNameList = new ArrayList<>();

        PropertyInfo(MondrianProperties properties) {
            final Class<? extends Object> clazz = properties.getClass();
            final Field[] fields = clazz.getFields();
            for (Field field : fields) {
                if (!Modifier.isPublic(field.getModifiers())
                    || Modifier.isStatic(field.getModifiers())
                    || !Property.class.isAssignableFrom(
                        field.getType()))
                {
                    continue;
                }
                final Property property;
                try {
                    property = (Property) field.get(properties);
                } catch (IllegalAccessException e) {
                    continue;
                }
                propertyList.add(property);
                propertyNameList.add(field.getName());
            }
        }

        public int size() {
            return propertyList.size();
        }

        public Property getProperty(int i) {
            return propertyList.get(i);
        }

        public String getPropertyName(int i) {
            return propertyNameList.get(i);
        }

        /**
         * Looks up the definition of a property with a given name.
         */
        public static Property lookupProperty(
            MondrianProperties properties,
            String propertyName)
        {
            final Class<? extends Object> clazz = properties.getClass();
            final Field field;
            try {
                field = clazz.getField(propertyName);
            } catch (NoSuchFieldException e) {
                return null;
            }
            if (!Modifier.isPublic(field.getModifiers())
                || Modifier.isStatic(field.getModifiers())
                || !Property.class.isAssignableFrom(field.getType()))
            {
                return null;
            }
            try {
                return (Property) field.get(properties);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    private static class Expr {
        enum Type {
            STRING,
            NUMERIC,
            MEMBER
        }

        final Object value;
        final Type type;
        Expr(Object value, Type type) {
            this.value = value;
            this.type = type;
        }
    }

    public void loadParameter(Query query, Parameter param) {
        int category = TypeUtil.typeToCategory(param.getType());
        String name = param.getName();
        String value = CmdRunner.paraNameValues.get(name);
        debug(new StringBuilder("loadParameter: name=").append(name).append(", value=").append(value).toString());
        if (value == null) {
            return;
        }
        Expr expr = parseParameter(value);
        if  (expr == null) {
            return;
        }
        Expr.Type type = expr.type;
        // found the parameter with the given name in the query
        switch (category) {
        case Category.NUMERIC:
            if (type != Expr.Type.NUMERIC) {
                String msg =
                    new StringBuilder(FOR_PARAMETER_NAMED)
                        .append(name)
                        .append("\" of Catetory.Numeric, ")
                        .append(THE_VALUE_WAS_TYPE)
                        .append(type)
                        .append("\"").toString();
                throw new IllegalArgumentException(msg);
            }
            break;
        case Category.STRING:
            if (type != Expr.Type.STRING) {
                String msg =
                    new StringBuilder(FOR_PARAMETER_NAMED)
                        .append(name)
                        .append("\" of Catetory.String, ")
                        .append(THE_VALUE_WAS_TYPE)
                        .append(type)
                        .append("\"").toString();
                throw new IllegalArgumentException(msg);
            }
            break;

        case Category.MEMBER:
            if (type != Expr.Type.MEMBER) {
                String msg = new StringBuilder(FOR_PARAMETER_NAMED)
                    .append(name)
                    .append("\" of Catetory.Member, ")
                    .append(THE_VALUE_WAS_TYPE)
                    .append(type)
                    .append("\"").toString();
                throw new IllegalArgumentException(msg);
            }
            break;

        default:
            throw Util.newInternal("unexpected category " + category);
        }
        query.setParameter(param.getName(), String.valueOf(expr.value));
    }

    static NumberFormat nf = NumberFormat.getInstance();

    // this is taken from JPivot
    public Expr parseParameter(String value) {
        // is it a String (enclose in double or single quotes ?
        String trimmed = value.trim();
        int len = trimmed.length();
        if (trimmed.charAt(0) == '"' && trimmed.charAt(len - 1) == '"') {
            debug("parseParameter. STRING_TYPE: " + trimmed);
            return new Expr(
                trimmed.substring(1, trimmed.length() - 1),
                Expr.Type.STRING);
        }
        if (trimmed.charAt(0) == '\'' && trimmed.charAt(len - 1) == '\'') {
            debug("parseParameter. STRING_TYPE: " + trimmed);
            return new Expr(
                trimmed.substring(1, trimmed.length() - 1),
                Expr.Type.STRING);
        }

        // is it a Number ?
        Number number = null;
        try {
            number = nf.parse(trimmed);
        } catch (ParseException pex) {
            // nothing to do, should be member
        }
        if (number != null) {
            debug("parseParameter. NUMERIC_TYPE: " + number);
            return new Expr(number, Expr.Type.NUMERIC);
        }

        debug("parseParameter. MEMBER_TYPE: " + trimmed);
        Query query = this.connection.parseQuery(this.mdxCmd);

        // assume member, dimension, hierarchy, level
        OlapElement element = Util.lookup(query, Util.parseIdentifier(trimmed));

        debug(
            "parseParameter. exp="
            + ((element == null) ? "null" : element.getClass().getName()));

        if (element instanceof Member member) {
            return new Expr(member, Expr.Type.MEMBER);
        } else if (element instanceof org.eclipse.daanse.olap.api.model.Level level) {
            return new Expr(level, Expr.Type.MEMBER);
        } else if (element instanceof Hierarchy hier) {
            return new Expr(hier, Expr.Type.MEMBER);
        } else if (element instanceof Dimension dim) {
            return new Expr(dim, Expr.Type.MEMBER);
        }
        return null;
    }

    public static void listParameterNameValues(StringBuilder buf) {
        for (Map.Entry<Object, String> e
            : CmdRunner.paraNameValues.entrySet())
        {
            buf.append(e.getKey());
            buf.append('=');
            buf.append(e.getValue());
            buf.append(NL);
        }
    }

    public static void listParam(String name, StringBuilder buf) {
        String v = CmdRunner.paraNameValues.get(name);
        buf.append(v);
    }

    public static boolean isParam(String name) {
        String v = CmdRunner.paraNameValues.get(name);
        return (v != null);
    }

    public static void setParameter(String name, String value) {
        if (name == null) {
            CmdRunner.paraNameValues.clear();
        } else {
            if (value == null) {
                CmdRunner.paraNameValues.remove(name);
            } else {
                CmdRunner.paraNameValues.put(name, value);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // cubes
    //
    public Cube[] getCubes() {
        Connection conn = getConnection();
        return conn.getSchemaReader().withLocus().getCubes();
    }

    public Cube getCube(String name) {
        Cube[] cubes = getCubes();
        for (Cube cube : cubes) {
            if (cube.getName().equals(name)) {
                return cube;
            }
        }
        return null;
    }

    public void listCubeName(StringBuilder buf) {
        Cube[] cubes = getCubes();
        for (Cube cube : cubes) {
            buf.append(cube.getName());
            buf.append(NL);
        }
    }

    public void listCubeAttribues(String name, StringBuilder buf) {
        Cube cube = getCube(name);
        if (cube == null) {
            buf.append(NO_CUBE_FOUND_WITH_NAME);
            buf.append(name);
            buf.append("\"");
        } else {
            RolapCube rcube = (RolapCube) cube;
            buf.append("facttable=");
            buf.append(rcube.getStar().getFactTable().getAlias());
            buf.append(NL);
            buf.append("caching=");
            buf.append(rcube.isCacheAggregations());
            buf.append(NL);
        }
    }

    public void executeCubeCommand(
        String cubename,
        String command,
        StringBuilder buf)
    {
        Cube cube = getCube(cubename);
        if (cube == null) {
            buf.append(NO_CUBE_FOUND_WITH_NAME);
            buf.append(cubename);
            buf.append("\"");
        } else {
            if (command.equals("clearCache")) {
                RolapCube rcube = (RolapCube) cube;
                rcube.clearCachedAggregations();
            } else {
                buf.append("For cube \"");
                buf.append(cubename);
                buf.append("\" there is no command \"");
                buf.append(command);
                buf.append("\"");
            }
        }
    }

    public void setCubeAttribute(
        String cubename,
        String name,
        String value,
        StringBuilder buf)
    {
        Cube cube = getCube(cubename);
        if (cube == null) {
            buf.append(NO_CUBE_FOUND_WITH_NAME);
            buf.append(cubename);
            buf.append("\"");
        } else {
            if (name.equals("caching")) {
                RolapCube rcube = (RolapCube) cube;
                boolean isCache = Boolean.parseBoolean(value);
                rcube.setCacheAggregations(isCache);
            } else {
                buf.append("For cube \"");
                buf.append(cubename);
                buf.append("\" there is no attribute \"");
                buf.append(name);
                buf.append("\"");
            }
        }
    }
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Executes a query and returns the result as a string.
     *
     * @param queryString MDX query text
     * @return result String
     */
    public String execute(String queryString) {
        if (USE_OLAP4J) {
            return runQuery(
                queryString,
                new Function<CellSet,String>() {
                    @Override
					public String apply(CellSet param) {
                        StringWriter stringWriter = new StringWriter();
                        PrintWriter printWriter = new PrintWriter(stringWriter);
                        new RectangularCellSetFormatter(false)
                            .format(param, printWriter);
                        printWriter.flush();
                        return stringWriter.toString();
                    }
                });
        }
        Result result = runQuery(queryString, true);
        if (this.options.highCardResults) {
            return highCardToString(result);
        } else {
            return toString(result);
        }
    }

    /**
     * Executes a query and returns the result.
     *
     * @param queryString MDX query text
     * @return a {@link Result} object
     */
    public Result runQuery(String queryString, boolean loadParams) {
        debug("CmdRunner.runQuery: TOP");
        Result result = null;
        long start = System.currentTimeMillis();
        try {
            this.connection = getConnection();
            debug("CmdRunner.runQuery: AFTER getConnection");
            Query query = this.connection.parseQuery(queryString);
            debug("CmdRunner.runQuery: AFTER parseQuery");
            if (loadParams) {
                loadParameters(query);
            }
            start = System.currentTimeMillis();
            result = this.connection.execute(query);
        } finally {
            queryTime = (System.currentTimeMillis() - start);
            totalQueryTime += queryTime;
            debug("CmdRunner.runQuery: BOTTOM");
        }
        return result;
    }

    /**
     * Executes a query and processes the result using a callback.
     *
     * @param queryString MDX query text
     */
    public <T> T runQuery(String queryString, Function<CellSet, T> f) {
        long start = System.currentTimeMillis();
        OlapConnection connectionInner = null;
        OlapStatement statement = null;
        CellSet cellSet = null;
        try {
            connectionInner = getOlapConnection();
            statement = connectionInner.createStatement();
            debug("CmdRunner.runQuery: AFTER createStatement");
            start = System.currentTimeMillis();
            cellSet = statement.executeOlapQuery(queryString);
            return f.apply(cellSet);
        } catch (SQLException e) {
            throw new TuiRuntimeException(e);
        } finally {
            queryTime = (System.currentTimeMillis() - start);
            totalQueryTime += queryTime;
            debug("CmdRunner.runQuery: BOTTOM");
            Util.close(cellSet, statement, connectionInner);
        }
    }

    /**
     * Converts a {@link Result} object to a string
     *
     * @return String version of mondrian Result object.
     */
    public String toString(Result result) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        result.print(pw);
        pw.flush();
        return sw.toString();
    }
    /**
     * Converts a {@link Result} object to a string printing to standard
     * output directly, without buffering.
     *
     * @return null String since output is dump directly to stdout.
     */
    public String highCardToString(Result result) {
        result.print(new PrintWriter(System.out, true));
        return null;
    }


    public void makeConnectString() {
        String connectStringInner = CmdRunner.getConnectStringProperty();
        debug("CmdRunner.makeConnectString: connectString=" + connectStringInner);

        Util.PropertyList connectProperties;
        if (connectStringInner == null || connectStringInner.equals("")) {
            // create new and add provider
            connectProperties = new Util.PropertyList();
            connectProperties.put(
                RolapConnectionProperties.Provider.name(),
                "mondrian");
        } else {
            // load with existing connect string
            connectProperties = Util.parseConnectString(connectStringInner);
        }

        // override jdbc url
        String jdbcURL = CmdRunner.getJdbcURLProperty();

        debug("CmdRunner.makeConnectString: jdbcURL=" + jdbcURL);

        if (jdbcURL != null) {
            // add jdbc url to connect string
            connectProperties.put(
                RolapConnectionProperties.Jdbc.name(),
                jdbcURL);
        }

        // override jdbc drivers
        String jdbcDrivers = CmdRunner.getJdbcDriversProperty();

        debug("CmdRunner.makeConnectString: jdbcDrivers=" + jdbcDrivers);
        if (jdbcDrivers != null) {
            // add jdbc drivers to connect string
            connectProperties.put(
                RolapConnectionProperties.JdbcDrivers.name(),
                jdbcDrivers);
        }

        // override catalog url
        String catalogURL = CmdRunner.getCatalogURLProperty();

        debug("CmdRunner.makeConnectString: catalogURL=" + catalogURL);

        if (catalogURL != null) {
            // add catalog url to connect string
            connectProperties.put(
                RolapConnectionProperties.Catalog.name(),
                catalogURL);
        }

        // override JDBC user
        String jdbcUser = CmdRunner.getJdbcUserProperty();

        debug("CmdRunner.makeConnectString: jdbcUser=" + jdbcUser);

        if (jdbcUser != null) {
            // add user to connect string
            connectProperties.put(
                RolapConnectionProperties.JdbcUser.name(),
                jdbcUser);
        }

        // override JDBC password
        String jdbcPassword = CmdRunner.getJdbcPasswordProperty();

        debug("CmdRunner.makeConnectString: jdbcPassword=" + jdbcPassword);

        if (jdbcPassword != null) {
            // add password to connect string
            connectProperties.put(
                RolapConnectionProperties.JdbcPassword.name(),
                jdbcPassword);
        }

        if (options.roleName != null) {
            connectProperties.put(
                RolapConnectionProperties.Role.name(),
                options.roleName);
        }

        debug(
            "CmdRunner.makeConnectString: connectProperties="
            + connectProperties);

        this.connectString = connectProperties.toString();
    }

    /**
     * Gets a connection to Mondrian.
     *
     * @return Mondrian {@link Connection}
     */
    public Connection getConnection() {
        return getConnection(CmdRunner.reloadConnection);
    }

    /**
     * Gets a Mondrian connection, creating a new one if fresh is true.
     *
     * @return mondrian Connection.
     */
    public synchronized Connection getConnection(boolean fresh) {
        // FIXME: fresh is currently ignored.
        if (this.connectString == null) {
            makeConnectString();
        }
        if (this.connection == null) {
            this.connection =
                DriverManager.getConnection(this.connectString, null);
        }
        return this.connection;
    }

    /**
     * Gets an olap4j connection, creating a new one if fresh is true.
     *
     * @return mondrian Connection.
     */
    private synchronized OlapConnection getOlapConnection() throws SQLException {
        if (this.connectString == null) {
            makeConnectString();
        }
        final String olapConnectString = "jdbc:mondrian:" + connectString;
        final java.sql.Connection jdbcConnection =
            java.sql.DriverManager.getConnection(olapConnectString);
        // Cast to OlapWrapper lets code work on JDK1.5, before java.sql.Wrapper
        //noinspection RedundantCast
        return ((OlapWrapper) jdbcConnection).unwrap(OlapConnection.class);
    }

    public String getConnectString() {
        return getConnectString(CmdRunner.reloadConnection);
    }

    public synchronized String getConnectString(boolean fresh) {
        if (this.connectString == null) {
            makeConnectString();
        }
        return this.connectString;
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    //
    // static methods
    //
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    protected void debug(String msg) {
        if (options.debug) {
            out.println(msg);
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // properties
    /////////////////////////////////////////////////////////////////////////
    protected static String getConnectStringProperty() {
        return MondrianProperties.instance().TestConnectString.get();
    }
    protected static String getJdbcURLProperty() {
        return MondrianProperties.instance().FoodmartJdbcURL.get();
    }

    protected static String getJdbcUserProperty() {
        return MondrianProperties.instance().TestJdbcUser.get();
    }

    protected static String getJdbcPasswordProperty() {
        return MondrianProperties.instance().TestJdbcPassword.get();
    }
    protected static String getCatalogURLProperty() {
        return MondrianProperties.instance().CatalogURL.get();
    }
    protected static String getJdbcDriversProperty() {
        return MondrianProperties.instance().JdbcDrivers.get();
    }

    /////////////////////////////////////////////////////////////////////////
    // command loop
    /////////////////////////////////////////////////////////////////////////

    protected void commandLoop(boolean interactive) {
        commandLoop(
            new BufferedReader(
                new InputStreamReader(System.in)),
                interactive);
    }

    protected void commandLoop(File file) throws IOException {
        try(FileReader in = new FileReader(file)) {
            commandLoop(new BufferedReader(in), false);
        }
    }

    protected void commandLoop(
        String mdxCmd,
        boolean interactive)
    {
        StringReader is = new StringReader(mdxCmd);
        commandLoop(is, interactive);
    }

    private static final String COMMAND_PROMPT_START = "> ";
    private static final String COMMAND_PROMPT_MID = "? ";

    /**
     * The Command Loop where lines are read from the InputStream and
     * interpreted. If interactive then prompts are printed.
     *
     * @param in Input reader (preferably buffered)
     * @param interactive Whether the session is interactive
     */
    protected void commandLoop(Reader in, boolean interactive) {
        StringBuilder buf = new StringBuilder(2048);
        boolean inMdxCmd = false;
        String resultString = null;

        for (;;) {
            if (resultString != null) {
                printResults(resultString);
                printQueryTime();
                resultString = null;
                buf.setLength(0);
            } else if (interactive && (error != null)) {
                printResults(error);
                printQueryTime();
            }
            if (interactive) {
                if (inMdxCmd) {
                    out.print(COMMAND_PROMPT_MID);
                } else {
                    out.print(COMMAND_PROMPT_START);
                }
                out.flush();
            }
            if (!inMdxCmd) {
                buf.setLength(0);
            }
            String line;
            try {
                line = readLine(in, inMdxCmd);
            } catch (IOException e) {
                throw new TuiRuntimeException(
                    "Exception while reading command line", e);
            }
            if (line != null) {
                line = line.trim();
            }
            debug("line=" + line);


            // If not in the middle of reading an mdx query and
            // we reach end of file on the stream, then we are over.
            if (! inMdxCmd && line == null) {
                    return;
            }

            // If not reading an mdx query, then check if the line is a
            // user command.
            if (! inMdxCmd) {
                String cmd = line;
                if (cmd.startsWith("help")) {
                    resultString = executeHelp(cmd);
                } else if (cmd.startsWith("set")) {
                    resultString = executeSet(cmd);
                } else if (cmd.startsWith("file")) {
                    resultString = executeFile(cmd);
                } else if (cmd.startsWith("list")) {
                    resultString = executeList(cmd);
                } else if (cmd.startsWith("func")) {
                    resultString = executeFunc(cmd);
                } else if (cmd.startsWith("param")) {
                    resultString = executeParam(cmd);
                } else if (cmd.startsWith("cube")) {
                    resultString = executeCube(cmd);
                } else if (cmd.startsWith("error")) {
                    resultString = executeError(cmd);
                } else if (cmd.startsWith("echo")) {
                    resultString = executeEcho(cmd);
                } else if (cmd.startsWith("expr")) {
                    resultString = executeExpr(cmd);
                } else if (cmd.equals("=")) {
                    resultString = reExecuteMdxCmd();
                } else if (cmd.startsWith("exit")) {
                    break;
                }
                if (resultString != null) {
                    inMdxCmd = false;
                    continue;
                }
            }

            // Are we ready to execute an mdx query.
            if ((line == null)
                || ((line.length() == 1)
                    && ((line.charAt(0) == EXECUTE_CHAR)
                        || (line.charAt(0) == CANCEL_CHAR))))
            {
                // If EXECUTE_CHAR, then execute, otherwise its the
                // CANCEL_CHAR and simply empty buffer.
                if ((line == null) || (line.charAt(0) == EXECUTE_CHAR)) {
                    String mdxCmdInner = buf.toString().trim();
                    debug(new StringBuilder("mdxCmd=\"").append(mdxCmdInner).append("\"").toString());
                    resultString = executeMdxCmd(mdxCmdInner);
                }

                inMdxCmd = false;

            } else if (line.length() > 0) {
                // OK, just add the line to the mdx query we are building.
                inMdxCmd = true;

                if (line.endsWith(SEMI_COLON_STRING)) {
                    // Remove the ';' character.
                    buf.append(line.substring(0, line.length() - 1));
                    String mdxCmdInner = buf.toString().trim();
                    debug(new StringBuilder("mdxCmd=\"").append(mdxCmdInner).append("\"").toString());
                    resultString = executeMdxCmd(mdxCmdInner);
                    inMdxCmd = false;
                } else {
                    buf.append(line);
                    // add carriage return so that query keeps formatting
                    buf.append(NL);
                }
            }
        }
    }

    protected void printResults(String resultString) {
        if (resultString != null) {
            resultString = resultString.trim();
            if (resultString.length() > 0) {
                out.println(resultString);
                out.flush();
            }
        }
    }
    protected void printQueryTime() {
        if (options.timeQueries && (queryTime != -1)) {
            out.println("time[" + queryTime +  "ms]");
            out.flush();
            queryTime = -1;
        }
    }

    /**
     * Gather up a line ending in '\n' or EOF.
     * Returns null if at EOF.
     * Strip out comments. If a comment character appears within a
     * string then its not a comment. Strings are defined with "\"" or
     * "'" characters. Also, a string can span more than one line (a
     * nice little complication). So, if we read a string, then we consume
     * the whole string as part of the "line" returned,
     * including EOL characters.
     * If an escape character is seen '\\', then it and the next character
     * is added to the line regardless of what the next character is.
     */
    protected static String readLine(
        Reader reader,
        boolean inMdxCmd)
        throws IOException
    {
        StringBuilder buf = new StringBuilder(128);
        StringBuilder line = new StringBuilder(128);
        int offset;
        int i = getLine(reader, line);
        boolean inName = false;

        for (offset = 0; offset < line.length(); offset++) {
            char c = line.charAt(offset);

            if (c == ESCAPE_CHAR) {
                buf.append(ESCAPE_CHAR);
                buf.append(line.charAt(++offset));
            } else if (!inName
                       && ((c == STRING_CHAR_1) || (c == STRING_CHAR_2)))
            {
                i = readString(reader, line, offset, buf, i);
                offset = 0;
            } else {
                int commentType=-1;

                if (c == BRACKET_START) {
                    inName = true;
                } else if (c == BRACKET_END) {
                    inName = false;
                } else if (! inName) {
                    // check if we have the start of a comment block
                    // check if we have the start of a comment block
                    for (int x = 0; x < commentDelim.length; x++) {
                        if (c != commentStartChars[x]) {
                            continue;
                        }
                        String startComment = commentDelim[x][0];
                        boolean foundCommentStart = true;
                        for (int j = 1;
                            j + offset < line.length()
                            && j < startComment.length();
                            j++)
                        {
                            if (line.charAt(j + offset)
                                != startComment.charAt(j))
                            {
                                foundCommentStart = false;
                            }
                        }

                        if (foundCommentStart) {
                            if (x == 0) {
                                // A '#' must be the first character on a line
                                if (offset == 0) {
                                    commentType = x;
                                    break;
                                }
                            } else {
                                commentType = x;
                                break;
                            }
                        }
                    }
                }

                // -1 means no comment
                if (commentType == -1) {
                    buf.append(c);
                } else {
                    // check for comment to end of line comment
                    if (commentDelim[commentType][1] == null) {
                        break;
                    } else {
                        // handle delimited comment block
                        i = readBlock(
                            reader, line, offset,
                            commentDelim[commentType][0],
                            commentDelim[commentType][1],
                            false, false, buf, i);
                        offset = 0;
                    }
                }
            }
        }

        if (i == -1 && buf.length() == 0) {
            return null;
        } else {
            return buf.toString();
        }
    }

   /**
     * Read the next line of input.  Return the terminating character,
     * -1 for end of file, or \n or \r.  Add \n and \r to the end of the
     * buffer to be included in strings and comment blocks.
     */
    protected static int getLine(
        Reader reader,
        StringBuilder line)
        throws IOException
    {
        line.setLength(0);
        for (;;) {
            int i = reader.read();

            if (i == -1) {
                return i;
            }

            line.append((char)i);

            if (i == '\n' || i == '\r') {
                return i;
            }
        }
    }

    /**
     * Start of a string, read all of it even if it spans
     * more than one line adding each line's <cr> to the
     * buffer.
     */
    protected static int readString(
        Reader reader,
        StringBuilder line,
        int offset,
        StringBuilder buf,
        int i)
        throws IOException
    {
        String delim = line.substring(offset, offset + 1);
        return readBlock(
            reader, line, offset, delim, delim, true, true, buf, i);
    }

    /**
     * Start of a delimted block, read all of it even if it spans
     * more than one line adding each line's <cr> to the
     * buffer.
     *
     * A delimited block is a delimited comment (/\* ... *\/), or a string.
     */
    protected static int readBlock(
        Reader reader,
        StringBuilder line,
        int offset,
        final String startDelim,
        final String endDelim,
        final boolean allowEscape,
        final boolean addToBuf,
        StringBuilder buf,
        int i)
        throws IOException
    {
        int depth = 1;
        if (addToBuf) {
            buf.append(startDelim);
        }
        offset += startDelim.length();

        for (;;) {
            // see if we are at the end of the block
            if (line.substring(offset).startsWith(endDelim)) {
                if (addToBuf) {
                    buf.append(endDelim);
                }
                offset += endDelim.length();
                if (--depth == 0) {
                     break;
                }
            // check for nested block
            } else if (allowNestedComments
                       && line.substring(offset).startsWith(startDelim))
            {
                if (addToBuf) {
                    buf.append(startDelim);
                }
                offset += startDelim.length();
                depth++;
            } else if (offset < line.length()) {
                // not at the end of line, so eat the next char
                char c = line.charAt(offset++);
                if (allowEscape && c == ESCAPE_CHAR) {
                    if (addToBuf) {
                        buf.append(ESCAPE_CHAR);
                    }

                    if (offset < line.length()) {
                        if (addToBuf) {
                            buf.append(line.charAt(offset));
                        }
                        offset++;
                    }
                } else if (addToBuf) {
                    buf.append(c);
                }
            } else {
                // finished a line; read in the next one and continue
                if (i == -1) {
                    break;
                }
                i = getLine(reader, line);

                // line will always contain EOL marker at least, unless at EOF
                offset = 0;
                if (line.length() == 0) {
                    break;
                }
            }
        }

        // remove to the end of the string, so caller starts at offset 0
        if (offset > 0) {
            line.delete(0, offset - 1);
        }

        return i;
    }

    /////////////////////////////////////////////////////////////////////////
    // xmla file
    /////////////////////////////////////////////////////////////////////////

    /**
     * This is called to process a file containing XMLA as the contents
     * of SOAP xml.
     *
     */
    protected void processSoapXmla(
        File file,
        int validateXmlaResponse) throws ServletException, IOException, SAXException,
        ParserConfigurationException, TransformerException, XPathException {
        String catalogURL = CmdRunner.getCatalogURLProperty();
        Map<String, String> catalogNameUrls = new HashMap<>();
        catalogNameUrls.put(CATALOG_NAME, catalogURL);

        long start = System.currentTimeMillis();

        byte[] bytes = null;
        try {
            bytes = XmlaSupport.processSoapXmla(
                file,
                getConnectString(),
                catalogNameUrls,
                null);
        } finally {
            queryTime = (System.currentTimeMillis() - start);
            totalQueryTime += queryTime;
        }

        String response = new String(bytes);
        out.println(response);

        switch (validateXmlaResponse) {
        case VALIDATE_NONE:
            break;
        case VALIDATE_TRANSFORM:
            XmlaSupport.validateSchemaSoapXmla(bytes);
            out.println(XML_DATA_IS_VALID);
            break;
        case VALIDATE_XPATH:
            XmlaSupport.validateSoapXmlaUsingXpath(bytes);
            out.println(XML_DATA_IS_VALID);
            break;
        default:
            break;
        }
    }

    /**
     * This is called to process a file containing XMLA xml.
     *
     */
    protected void processXmla(
        File file,
        int validateXmlaResponce) throws XOMException, IOException, SAXException, ParserConfigurationException,
        TransformerException, XPathException {
        String catalogURL = CmdRunner.getCatalogURLProperty();
        Map<String, String> catalogNameUrls = new HashMap<>();
        catalogNameUrls.put(CATALOG_NAME, catalogURL);

        long start = System.currentTimeMillis();

        byte[] bytes = null;
        try {
            bytes = XmlaSupport.processXmla(
                file,
                getConnectString(),
                catalogNameUrls);
        } finally {
            queryTime = (System.currentTimeMillis() - start);
            totalQueryTime += queryTime;
        }

        String response = new String(bytes);
        out.println(response);

        switch (validateXmlaResponce) {
        case VALIDATE_NONE:
            break;
        case VALIDATE_TRANSFORM:
            XmlaSupport.validateSchemaXmla(bytes);
            out.println(XML_DATA_IS_VALID);
            break;
        case VALIDATE_XPATH:
            XmlaSupport.validateXmlaUsingXpath(bytes);
            out.println(XML_DATA_IS_VALID);
            break;
        default:
            break;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // user commands and help messages
    /////////////////////////////////////////////////////////////////////////
    private static final String INDENT = "  ";

    private static final int UNKNOWN_CMD        = 0x0000;
    private static final int HELP_CMD           = 0x0001;
    private static final int SET_CMD            = 0x0002;
    private static final int LOG_CMD            = 0x0004;
    private static final int FILE_CMD           = 0x0008;
    private static final int LIST_CMD           = 0x0010;
    private static final int MDX_CMD            = 0x0020;
    private static final int FUNC_CMD           = 0x0040;
    private static final int PARAM_CMD          = 0x0080;
    private static final int CUBE_CMD           = 0x0100;
    private static final int ERROR_CMD          = 0x0200;
    private static final int ECHO_CMD           = 0x0400;
    private static final int EXPR_CMD           = 0x0800;
    private static final int EXIT_CMD           = 0x1000;

    private static final int ALL_CMD  = HELP_CMD  |
                                        SET_CMD   |
                                        LOG_CMD   |
                                        FILE_CMD  |
                                        LIST_CMD  |
                                        MDX_CMD   |
                                        FUNC_CMD  |
                                        PARAM_CMD |
                                        CUBE_CMD  |
                                        ERROR_CMD |
                                        ECHO_CMD  |
                                        EXPR_CMD  |
                                        EXIT_CMD;

    private static final char ESCAPE_CHAR         = '\\';
    private static final char EXECUTE_CHAR        = '=';
    private static final char CANCEL_CHAR         = '~';
    private static final char STRING_CHAR_1       = '"';
    private static final char STRING_CHAR_2       = '\'';
    private static final char BRACKET_START       = '[';
    private static final char BRACKET_END         = ']';

    private static final String SEMI_COLON_STRING = ";";

    //////////////////////////////////////////////////////////////////////////
    // help
    //////////////////////////////////////////////////////////////////////////
    protected static String executeHelp(String mdxCmd) {
        StringBuilder buf = new StringBuilder(200);

        String[] tokens = mdxCmd.split("\\s+");

        int cmd = UNKNOWN_CMD;

        if (tokens.length == 1) {
            buf.append("Commands:");
            cmd = ALL_CMD;

        } else if (tokens.length == 2) {
            String cmdName = tokens[1];

            if (cmdName.equals("help")) {
                cmd = HELP_CMD;
            } else if (cmdName.equals("set")) {
                cmd = SET_CMD;
            } else if (cmdName.equals("log")) {
                cmd = LOG_CMD;
            } else if (cmdName.equals("file")) {
                cmd = FILE_CMD;
            } else if (cmdName.equals("list")) {
                cmd = LIST_CMD;
            } else if (cmdName.equals("func")) {
                cmd = FUNC_CMD;
            } else if (cmdName.equals("param")) {
                cmd = PARAM_CMD;
            } else if (cmdName.equals("cube")) {
                cmd = CUBE_CMD;
            } else if (cmdName.equals("error")) {
                cmd = ERROR_CMD;
            } else if (cmdName.equals("echo")) {
                cmd = ECHO_CMD;
            } else if (cmdName.equals("exit")) {
                cmd = EXIT_CMD;
            } else {
                cmd = UNKNOWN_CMD;
            }
        }

        if (cmd == UNKNOWN_CMD) {
            buf.append("Unknown help command: ");
            buf.append(mdxCmd);
            buf.append(NL);
            buf.append("Type \"help\" for list of commands");
        }

        if ((cmd & HELP_CMD) != 0) {
            // help
            buf.append(NL);
            appendIndent(buf, 1);
            buf.append("help");
            buf.append(NL);
            appendIndent(buf, 2);
            buf.append("Prints this text");
        }

        if ((cmd & SET_CMD) != 0) {
            // set
            buf.append(NL);
            appendSet(buf);
        }

        if ((cmd & LOG_CMD) != 0) {
            // set
            buf.append(NL);
            appendLog(buf);
        }

        if ((cmd & FILE_CMD) != 0) {
            // file
            buf.append(NL);
            appendFile(buf);
        }
        if ((cmd & LIST_CMD) != 0) {
            // list
            buf.append(NL);
            appendList(buf);
        }

        if ((cmd & MDX_CMD) != 0) {
            buf.append(NL);
            appendIndent(buf, 1);
            buf.append("<mdx query> <cr> ( '");
            buf.append(EXECUTE_CHAR);
            buf.append("' | '");
            buf.append(CANCEL_CHAR);
            buf.append("' ) <cr>");
            buf.append(NL);
            appendIndent(buf, 2);
            buf.append("Execute or cancel mdx query.");
            buf.append(NL);
            appendIndent(buf, 2);
            buf.append("An mdx query may span one or more lines.");
            buf.append(NL);
            appendIndent(buf, 2);
            buf.append("After the last line of the query has been entered,");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("on the next line a single execute character, '");
            buf.append(EXECUTE_CHAR);
            buf.append("', may be entered");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("followed by a carriage return.");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("The lone '");
            buf.append(EXECUTE_CHAR);
            buf.append("' informs the interpreter that the query has");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("has been entered and is ready to execute.");
            buf.append(NL);
            appendIndent(buf, 2);
            buf.append("At anytime during the entry of a query the cancel");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("character, '");
            buf.append(CANCEL_CHAR);
            buf.append("', may be entered alone on a line.");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("This removes all of the query text from the");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("the command interpreter.");
            buf.append(NL);
            appendIndent(buf, 2);
            buf.append("Queries can also be ended by using a semicolon ';'");
            buf.append(NL);
            appendIndent(buf, 3);
            buf.append("at the end of a line.");
        }
        if ((cmd & FUNC_CMD) != 0) {
            buf.append(NL);
            appendFunc(buf);
        }

        if ((cmd & PARAM_CMD) != 0) {
            buf.append(NL);
            appendParam(buf);
        }

        if ((cmd & CUBE_CMD) != 0) {
            buf.append(NL);
            appendCube(buf);
        }

        if ((cmd & ERROR_CMD) != 0) {
            buf.append(NL);
            appendError(buf);
        }

        if ((cmd & ECHO_CMD) != 0) {
            buf.append(NL);
            appendEcho(buf);
        }

        if ((cmd & EXPR_CMD) != 0) {
            buf.append(NL);
            appendExpr(buf);
        }

        if (cmd == ALL_CMD) {
            // reexecute
            buf.append(NL);
            appendIndent(buf, 1);
            buf.append("= <cr>");
            buf.append(NL);
            appendIndent(buf, 2);
            buf.append("Re-Execute mdx query.");
        }

        if ((cmd & EXIT_CMD) != 0) {
            // exit
            buf.append(NL);
            appendExit(buf);
        }


        return buf.toString();
    }

    protected static void appendIndent(StringBuilder buf, int i) {
        while (i-- > 0) {
            buf.append(CmdRunner.INDENT);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // set
    //////////////////////////////////////////////////////////////////////////
    protected static void appendSet(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("set [ property[=value ] ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With no args, prints all mondrian properties and values.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"property\" prints property's value.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"property=value\" set property to that value.");
    }

    protected String executeSet(String mdxCmd) {
        StringBuilder buf = new StringBuilder(400);

        String[] tokens = mdxCmd.split("\\s+");

        if (tokens.length == 1) {
            // list all properties
            listPropertiesAll(buf);

        } else if (tokens.length == 2) {
            String arg = tokens[1];
            int index = arg.indexOf('=');
            if (index == -1) {
                listProperty(arg, buf);
            } else {
                String[] nv = arg.split("=");
                String name = nv[0];
                String value = nv[1];
                if (isProperty(name)) {
                    try {
                        if (setProperty(name, value)) {
                            this.connectString = null;
                        }
                    } catch (Exception ex) {
                        setError(ex);
                    }
                } else {
                    buf.append("Bad property name:");
                    buf.append(name);
                    buf.append(NL);
                }
            }

        } else {
            buf.append(BAD_COMMAND_USAGE);
            buf.append(mdxCmd);
            buf.append('"');
            buf.append(NL);
            appendSet(buf);
        }

        return buf.toString();
    }

    //////////////////////////////////////////////////////////////////////////
    // log
    //////////////////////////////////////////////////////////////////////////
    protected static void appendLog(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("log [ classname[=level ] ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With no args, prints the current log level of all classes.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"classname\" prints the current log level of the class.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"classname=level\" set log level to new value.");
    }



    //////////////////////////////////////////////////////////////////////////
    // file
    //////////////////////////////////////////////////////////////////////////
    protected static void appendFile(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("file [ filename | '=' ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With no args, prints the last filename executed.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"filename\", read and execute filename .");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"=\" character, re-read and re-execute previous filename .");
    }

    protected String executeFile(String mdxCmd) {
        StringBuilder buf = new StringBuilder(512);
        String[] tokens = mdxCmd.split("\\s+");

        if (tokens.length == 1) {
            if (this.filename != null) {
                buf.append(this.filename);
            }

        } else if (tokens.length == 2) {
            String token = tokens[1];
            String nameOfFile = null;
            if ((token.length() == 1) && (token.charAt(0) == EXECUTE_CHAR)) {
                // file '='
                if (this.filename == null) {
                    buf.append(BAD_COMMAND_USAGE);
                    buf.append(mdxCmd);
                    buf.append("\", no file to re-execute");
                    buf.append(NL);
                    appendFile(buf);
                } else {
                    nameOfFile = this.filename;
                }
            } else {
                // file filename
                nameOfFile = token;
            }

            if (nameOfFile != null) {
                this.filename = nameOfFile;

                try {
                    commandLoop(new File(this.filename));
                } catch (IOException ex) {
                    setError(ex);
                    buf.append("Error: ").append(ex);
                }
            }

        } else {
            buf.append(BAD_COMMAND_USAGE);
            buf.append(mdxCmd);
            buf.append('"');
            buf.append(NL);
            appendFile(buf);
        }
        return buf.toString();
    }

    //////////////////////////////////////////////////////////////////////////
    // list
    //////////////////////////////////////////////////////////////////////////
    protected static void appendList(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("list [ cmd | result ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With no arguments, list previous cmd and result");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"cmd\" argument, list the last mdx query cmd.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"result\" argument, list the last mdx query result.");
    }

    protected String executeList(String mdxCmd) {
        StringBuilder buf = new StringBuilder(200);

        String[] tokens = mdxCmd.split("\\s+");

        if (tokens.length == 1) {
            if (this.mdxCmd != null) {
                buf.append(this.mdxCmd);
                if (mdxResult != null) {
                    buf.append(NL);
                    buf.append(mdxResult);
                }
            } else if (mdxResult != null) {
                buf.append(mdxResult);
            }

        } else if (tokens.length == 2) {
            String arg = tokens[1];
            if (arg.equals("cmd")) {
                if (this.mdxCmd != null) {
                    buf.append(this.mdxCmd);
                }
            } else if (arg.equals("result")) {
                if (mdxResult != null) {
                    buf.append(mdxResult);
                }
            } else {
                buf.append("Bad sub command usage:");
                buf.append(mdxCmd);
                buf.append(NL);
                appendList(buf);
            }
        } else {
            buf.append(BAD_COMMAND_USAGE);
            buf.append(mdxCmd);
            buf.append('"');
            buf.append(NL);
            appendList(buf);
        }

        return buf.toString();
    }

    //////////////////////////////////////////////////////////////////////////
    // func
    //////////////////////////////////////////////////////////////////////////
    protected static void appendFunc(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("func [ name ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With no arguments, list all defined function names");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"name\" argument, display the functions:");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append("name, description, and syntax");
    }
    protected String executeFunc(String mdxCmd) {
        StringBuilder buf = new StringBuilder(200);

        String[] tokens = mdxCmd.split("\\s+");

        final FunTable funTable = getConnection().getSchema().getFunTable();
        if (tokens.length == 1) {
            // prints names only once
            List<FunInfo> funInfoList = funTable.getFunInfoList();
            Iterator<FunInfo> it = funInfoList.iterator();
            String prevName = null;
            while (it.hasNext()) {
                FunInfo fi = it.next();
                String name = fi.getName();
                if (prevName == null || ! prevName.equals(name)) {
                    buf.append(name);
                    buf.append(NL);
                    prevName = name;
                }
            }

        } else if (tokens.length == 2) {
            String funcname = tokens[1];
            List<FunInfo> funInfoList = funTable.getFunInfoList();
            List<FunInfo> matches = new ArrayList<>();

            for (FunInfo fi : funInfoList) {
                if (fi.getName().equalsIgnoreCase(funcname)) {
                    matches.add(fi);
                }
            }

            if (matches.isEmpty()) {
                buf.append("Bad function name \"");
                buf.append(funcname);
                buf.append("\", usage:");
                buf.append(NL);
                appendList(buf);
            } else {
                Iterator<FunInfo> it = matches.iterator();
                boolean doname = true;
                while (it.hasNext()) {
                    FunInfo fi = it.next();
                    if (doname) {
                        buf.append(fi.getName());
                        buf.append(NL);
                        doname = false;
                    }

                    appendIndent(buf, 1);
                    buf.append(fi.getDescription());
                    buf.append(NL);

                    String[] sigs = fi.getSignatures();
                    if (sigs == null) {
                        appendIndent(buf, 2);
                        buf.append("Signature: ");
                        buf.append("NONE");
                        buf.append(NL);
                    } else {
                        for (String sig : sigs) {
                            appendIndent(buf, 2);
                            buf.append(sig);
                            buf.append(NL);
                        }
                    }
/*
                    appendIndent(buf, 1);
                    buf.append("Return Type: ");
                    int returnType = fi.getReturnTypes();
                    if (returnType >= 0) {
                        buf.append(cat.getName(returnType));
                    } else {
                        buf.append("NONE");
                    }
                    buf.append(nl);
                    int[][] paramsArray = fi.getParameterTypes();
                    if (paramsArray == null) {
                        appendIndent(buf, 1);
                        buf.append("Paramter Types: ");
                        buf.append("NONE");
                        buf.append(nl);

                    } else {
                        for (int j = 0; j < paramsArray.length; j++) {
                            int[] params = paramsArray[j];
                            appendIndent(buf, 1);
                            buf.append("Paramter Types: ");
                            for (int k = 0; k < params.length; k++) {
                                int param = params[k];
                                buf.append(cat.getName(param));
                                buf.append(' ');
                            }
                            buf.append(nl);
                        }
                    }
*/
                }
            }
        } else {
            buf.append(BAD_COMMAND_USAGE);
            buf.append(mdxCmd);
            buf.append('"');
            buf.append(NL);
            appendList(buf);
        }

        return buf.toString();
    }
    //////////////////////////////////////////////////////////////////////////
    // param
    //////////////////////////////////////////////////////////////////////////
    protected static void appendParam(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("param [ name[=value ] ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With no argumnts, all param name/value pairs are printed.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"name\" argument, the value of the param is printed.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"name=value\" sets the parameter with name to value.");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append(" If name is null, then unsets all parameters");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append(
            " If value is null, then unsets the parameter associated with value");
    }
    protected String executeParam(String mdxCmd) {
        StringBuilder buf = new StringBuilder(200);

        String[] tokens = mdxCmd.split("\\s+");

        if (tokens.length == 1) {
            // list all properties
            listParameterNameValues(buf);

        } else if (tokens.length == 2) {
            String arg = tokens[1];
            int index = arg.indexOf('=');
            if (index == -1) {
                if (isParam(arg)) {
                    listParam(arg, buf);
                } else {
                    buf.append("Bad parameter name:");
                    buf.append(arg);
                    buf.append(NL);
                }
            } else {
                String[] nv = arg.split("=");
                String name = (nv.length == 0) ? null : nv[0];
                String value = (nv.length == 2) ? nv[1] : null;
                setParameter(name, value);
            }

        } else {
            buf.append(BAD_COMMAND_USAGE);
            buf.append(mdxCmd);
            buf.append('"');
            buf.append(NL);
            appendSet(buf);
        }

        return buf.toString();
    }
    //////////////////////////////////////////////////////////////////////////
    // cube
    //////////////////////////////////////////////////////////////////////////
    protected static void appendCube(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("cube [ cubename [ name [=value | command] ] ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With no argumnts, all cubes are listed by name.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"cubename\" argument, cube attribute name/values for:");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append("fact table (readonly)");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append("aggregate caching (readwrite)");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("are printed");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"cubename name=value\" sets the readwrite attribute with name to value.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"cubename command\" executes the commands:");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append("clearCache");
    }

    protected String executeCube(String mdxCmd) {
        StringBuilder buf = new StringBuilder(200);

        String[] tokens = mdxCmd.split("\\s+");

        if (tokens.length == 1) {
            // list all properties
            listCubeName(buf);
        } else if (tokens.length == 2) {
            String cubename = tokens[1];
            listCubeAttribues(cubename, buf);

        } else if (tokens.length == 3) {
            String cubename = tokens[1];
            String arg = tokens[2];
            int index = arg.indexOf('=');
            if (index == -1) {
                // its a commnd
                executeCubeCommand(cubename, arg, buf);
            } else {
                String[] nv = arg.split("=");
                String name = (nv.length == 0) ? null : nv[0];
                String value = (nv.length == 2) ? nv[1] : null;
                setCubeAttribute(cubename, name, value, buf);
            }

        } else {
            buf.append(BAD_COMMAND_USAGE);
            buf.append(mdxCmd);
            buf.append('"');
            buf.append(NL);
            appendSet(buf);
        }

        return buf.toString();
    }
    //////////////////////////////////////////////////////////////////////////
    // error
    //////////////////////////////////////////////////////////////////////////
    protected static void appendError(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("error [ msg | stack ] <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With no argumnts, both message and stack are printed.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("With \"msg\" argument, the Error message is printed.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append(
            "With \"stack\" argument, the Error stack trace is printed.");
    }

    protected String executeError(String mdxCmd) {
        StringBuilder buf = new StringBuilder(200);

        String[] tokens = mdxCmd.split("\\s+");

        if (tokens.length == 1) {
            if (error != null) {
                buf.append(error);
                if (stack != null) {
                    buf.append(NL);
                    buf.append(stack);
                }
            } else if (stack != null) {
                buf.append(stack);
            }

        } else if (tokens.length == 2) {
            String arg = tokens[1];
            if (arg.equals("msg")) {
                if (error != null) {
                    buf.append(error);
                }
            } else if (arg.equals("stack")) {
                if (stack != null) {
                    buf.append(stack);
                }
            } else {
                buf.append("Bad sub command usage:");
                buf.append(mdxCmd);
                buf.append(NL);
                appendList(buf);
            }
        } else {
            buf.append(BAD_COMMAND_USAGE);
            buf.append(mdxCmd);
            buf.append('"');
            buf.append(NL);
            appendList(buf);
        }

        return buf.toString();
    }

    //////////////////////////////////////////////////////////////////////////
    // echo
    //////////////////////////////////////////////////////////////////////////
    protected static void appendEcho(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("echo text <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("echo text to standard out.");
    }

    protected String executeEcho(String mdxCmd) {
        try {
            return (mdxCmd.length() == 4)
                ? "" : mdxCmd.substring(4);
        } catch (Exception ex) {
            setError(ex);
            return null;
        }
    }
    //////////////////////////////////////////////////////////////////////////
    // expr
    //////////////////////////////////////////////////////////////////////////
    protected static void appendExpr(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("expr cubename expression<cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("evaluate an expression against a cube.");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("where: ");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append("cubename is single word or string using [], '' or \"\"");
        buf.append(NL);
        appendIndent(buf, 3);
        buf.append("expression is string using \"\"");
    }
    protected String executeExpr(String mdxCmd) {
        StringBuilder buf = new StringBuilder(256);

        mdxCmd = (mdxCmd.length() == 5)
                ? "" : mdxCmd.substring(5);

        String regex = "(\"[^\"]+\"|'[^\']+'|\\[[^\\]]+\\]|[^\\s]+)\\s+.*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(mdxCmd);
        boolean b = m.matches();

        if (! b) {
            buf.append("Could not parse into \"cubename expression\" command:");
            buf.append(NL);
            buf.append(mdxCmd);
            String msg = buf.toString();
            setError(msg);
            return msg;
        } else {
            String cubeName = m.group(1);
            String expression = mdxCmd.substring(cubeName.length() + 1);

            if (cubeName.charAt(0) == '"' || cubeName.charAt(0) == '\'' || cubeName.charAt(0) == '[') {
                cubeName = cubeName.substring(1, cubeName.length() - 1);
            }

            int len = expression.length();
            if (expression.charAt(0) == '"') {
                if (expression.charAt(len - 1) != '"') {
                    buf.append("Missing end '\"' in expression:");
                    buf.append(NL);
                    buf.append(expression);
                    String msg = buf.toString();
                    setError(msg);
                    return msg;
                }
                expression = expression.substring(1, len - 1);

            } else if (expression.charAt(0) == '\'') {
                if (expression.charAt(len - 1) != '\'') {
                    buf.append("Missing end \"'\" in expression:");
                    buf.append(NL);
                    buf.append(expression);
                    String msg = buf.toString();
                    setError(msg);
                    return msg;
                }
                expression = expression.substring(1, len - 1);
            }

            Cube cube = getCube(cubeName);
            if (cube == null) {
                buf.append(NO_CUBE_FOUND_WITH_NAME);
                buf.append(cubeName);
                buf.append("\"");
                String msg = buf.toString();
                setError(msg);
                return msg;

            } else {
                try {
                    if (cubeName.indexOf(' ') >= 0 && cubeName.charAt(0) != '[') {
                        cubeName = Util.quoteMdxIdentifier(cubeName);
                    }
                    final char c = '\'';
                    if (expression.indexOf('\'') != -1) {
                        // make sure all "'" are escaped
                        int start = 0;
                        int index = expression.indexOf('\'', start);
                        if (index == 0) {
                            // error: starts with "'"
                            buf.append("Double \"''\" starting expression:");
                            buf.append(NL);
                            buf.append(expression);
                            String msg = buf.toString();
                            setError(msg);
                            return msg;
                        }
                        while (index != -1) {
                            if (expression.charAt(index - 1) != '\\') {
                                // error
                                buf.append("Non-escaped \"'\" in expression:");
                                buf.append(NL);
                                buf.append(expression);
                                String msg = buf.toString();
                                setError(msg);
                                return msg;
                            }
                            start = index + 1;
                            index = expression.indexOf('\'', start);
                        }
                    }

                    // taken from FoodMartTest code
                    StringBuilder queryStringBuf = new StringBuilder(64);
                    queryStringBuf.append("with member [Measures].[Foo] as ");
                    queryStringBuf.append(c);
                    queryStringBuf.append(expression);
                    queryStringBuf.append(c);
                    queryStringBuf.append(
                        " select {[Measures].[Foo]} on columns from ");
                    queryStringBuf.append(cubeName);

                    String queryString = queryStringBuf.toString();

                    Result result = runQuery(queryString, true);
                    String resultString =
                        result.getCell(new int[]{0}).getFormattedValue();
                    mdxResult = resultString;
                    clearError();

                    buf.append(resultString);
                } catch (Exception ex) {
                    setError(ex);
                    buf.append("Error: ").append(ex);
                }
            }
        }
        return buf.toString();
    }
    //////////////////////////////////////////////////////////////////////////
    // exit
    //////////////////////////////////////////////////////////////////////////
    protected static void appendExit(StringBuilder buf) {
        appendIndent(buf, 1);
        buf.append("exit <cr>");
        buf.append(NL);
        appendIndent(buf, 2);
        buf.append("Exit mdx command interpreter.");
    }


    protected String reExecuteMdxCmd() {
        if (this.mdxCmd == null) {
            return "No command to execute";
        } else {
            return executeMdxCmd(this.mdxCmd);
        }
    }

    protected String executeMdxCmd(String mdxCmd) {
        this.mdxCmd = mdxCmd;
        try {
            String resultString = execute(mdxCmd);
            mdxResult = resultString;
            clearError();
            return resultString;
        } catch (Exception ex) {
            setError(ex);
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // helpers
    /////////////////////////////////////////////////////////////////////////
    protected static void loadPropertiesFromFile(
        String propFile)
        throws IOException
    {
        MondrianProperties.instance().load(new FileInputStream(propFile));
    }

    /////////////////////////////////////////////////////////////////////////
    // main
    /////////////////////////////////////////////////////////////////////////

    /**
     * Prints a usage message.
     *
     * @param msg Prefix to the message
     * @param out Output stream
     */
    protected static void usage(String msg, PrintStream out) {
        StringBuilder buf = new StringBuilder(256);
        if (msg != null) {
            buf.append(msg);
            buf.append(NL);
        }
        buf.append("Usage: mondrian.tui.CmdRunner args")
            .append(NL)
            .append("  args:")
            .append(NL)
            .append("  -h               : print this usage text")
            .append(NL)
            .append("  -H               : ready to print out high cardinality")
            .append(NL)
            .append("                     dimensions")
            .append(NL)
            .append("  -d               : enable local debugging")
            .append(NL)
            .append("  -t               : time each mdx query")
            .append(NL)
            .append("  -nocache         : turn off in-memory aggregate caching")
            .append(NL)
            .append("                     for all cubes regardless of setting")
            .append(NL)
            .append("                     in schema")
            .append(NL)
            .append("  -rc              : do NOT reload connections each query")
            .append(NL)
            .append("                     (default is to reload connections)")
            .append(NL)
            .append("  -p propertyfile  : load mondrian properties")
            .append(NL)
            .append("  -r role_name     : set the connections role name")
            .append(NL)
            .append("  -f mdx_filename+ : execute mdx in one or more files")
            .append(NL)
            .append("  -x xmla_filename+: execute XMLA in one or more files")
            .append("                     the XMLA request has no SOAP wrapper")
            .append(NL)
            .append("  -xs soap_xmla_filename+ ")
            .append("                   : execute Soap XMLA in one or more files")
            .append("                     the XMLA request has a SOAP wrapper")
            .append(NL)
            .append("  -vt              : validate xmla response using transforms")
            .append("                     only used with -x or -xs flags")
            .append(NL)
            .append("  -vx              : validate xmla response using xpaths")
            .append("                     only used with -x or -xs flags")
            .append(NL)
            .append("  mdx_cmd          : execute mdx_cmd")
            .append(NL).toString();

        out.println(buf.toString());
    }

    /**
     * Set the default comment delimiters for CmdRunner.  These defaults are
     * # to end of line
     * plus all the comment delimiters in Scanner.
     */
    private static void setDefaultCommentState() {
        allowNestedComments = mondrian.olap.Scanner.getNestedCommentsState();
        String[][] scannerCommentsDelimiters =
            mondrian.olap.Scanner.getCommentDelimiters();
        commentDelim = new String[scannerCommentsDelimiters.length + 1][2];
        commentStartChars = new char[scannerCommentsDelimiters.length + 1];


        // CmdRunner has extra delimiter; # to end of line
        commentDelim[0][0] = "#";
        commentDelim[0][1] = null;
        commentStartChars[0] = commentDelim[0][0].charAt(0);


        // copy all the rest of the delimiters
        for (int x = 0; x < scannerCommentsDelimiters.length; x++) {
            commentDelim[x + 1][0] = scannerCommentsDelimiters[x][0];
            commentDelim[x + 1][1] = scannerCommentsDelimiters[x][1];
            commentStartChars[x + 1] = commentDelim[x + 1][0].charAt(0);
        }
    }

    private static final int DO_MDX             = 1;
    private static final int DO_XMLA            = 2;
    private static final int DO_SOAP_XMLA       = 3;

    private static final int VALIDATE_NONE      = 1;
    private static final int VALIDATE_TRANSFORM = 2;
    private static final int VALIDATE_XPATH     = 3;

    protected static class Options {
        private boolean debug = false;
        private boolean timeQueries;
        private boolean noCache = false;
        private String roleName;
        private int validateXmlaResponse = VALIDATE_NONE;
        private final List<String> filenames = new ArrayList<>();
        private int doingWhat = DO_MDX;
        private String singleMdxCmd;
        private boolean highCardResults;
    }

    public static void main(String[] args) throws Exception {
        Options options;
        try {
            options = parseOptions(args);
        } catch (BadOption badOption) {
            usage(badOption.getMessage(), System.out);
            Throwable t = badOption.getCause();
            if (t != null) {
                LOGGER.error("CmdRunner error", t);
            }
            return;
        }

        CmdRunner cmdRunner =
                new CmdRunner(options, new PrintWriter(System.out));
        if (options.noCache) {
            cmdRunner.noCubeCaching();
        }

        if (!options.filenames.isEmpty()) {
            for (String filename : options.filenames) {
                cmdRunner.filename = filename;
                switch (options.doingWhat) {
                case DO_MDX:
                    // its a file containing mdx
                    cmdRunner.commandLoop(new File(filename));
                    break;
                case DO_XMLA:
                    // its a file containing XMLA
                    cmdRunner.processXmla(
                        new File(filename),
                        options.validateXmlaResponse);
                    break;
                default:
                    // its a file containing SOAP XMLA
                    cmdRunner.processSoapXmla(
                        new File(filename),
                        options.validateXmlaResponse);
                    break;
                }
                if (cmdRunner.error != null) {
                    System.err.println(filename);
                    System.err.println(cmdRunner.error);
                    if (cmdRunner.stack != null) {
                        System.err.println(cmdRunner.stack);
                    }
                    cmdRunner.printQueryTime();
                    cmdRunner.clearError();
                }
            }
        } else if (options.singleMdxCmd != null) {
            cmdRunner.commandLoop(options.singleMdxCmd, false);
            if (cmdRunner.error != null) {
                System.err.println(cmdRunner.error);
                if (cmdRunner.stack != null) {
                    System.err.println(cmdRunner.stack);
                }
            }
        } else {
            cmdRunner.commandLoop(true);
        }
        cmdRunner.printTotalQueryTime();
    }

    private void printTotalQueryTime() {
        if (options.timeQueries && totalQueryTime != queryTime) {
            // only print if different
            out.println(new StringBuilder("total[").append(totalQueryTime).append("ms]").toString());
        }
        out.flush();
    }

    private static Options parseOptions(String[] args)
        throws BadOption, IOException
    {
        final Options options = new Options();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-h")) {
                throw new BadOption(null);
            } else if (arg.equals("-H")) {
                options.highCardResults = true;

            } else if (arg.equals("-d")) {
                options.debug = true;

            } else if (arg.equals("-t")) {
                options.timeQueries = true;

            } else if (arg.equals("-nocache")) {
                options.noCache = true;

            } else if (arg.equals("-rc")) {
                CmdRunner.reloadConnection = false;

            } else if (arg.equals("-vt")) {
                options.validateXmlaResponse = VALIDATE_TRANSFORM;

            } else if (arg.equals("-vx")) {
                options.validateXmlaResponse = VALIDATE_XPATH;

            } else if (arg.equals("-f")) {
                i++;
                if (i == args.length) {
                    throw new BadOption("no mdx filename given");
                }
                options.filenames.add(args[i]);

            } else if (arg.equals("-x")) {
                i++;
                if (i == args.length) {
                    throw new BadOption("no XMLA filename given");
                }
                options.doingWhat = DO_XMLA;
                options.filenames.add(args[i]);

            } else if (arg.equals("-xs")) {
                i++;
                if (i == args.length) {
                    throw new BadOption("no XMLA filename given");
                }
                options.doingWhat = DO_SOAP_XMLA;
                options.filenames.add(args[i]);

            } else if (arg.equals("-p")) {
                i++;
                if (i == args.length) {
                    throw new BadOption("no mondrian properties file given");
                }
                String propFile = args[i];
                loadPropertiesFromFile(propFile);

            } else if (arg.equals("-r")) {
                i++;
                if (i == args.length) {
                    throw new BadOption("no role name given");
                }
                options.roleName = args[i];
            } else if (!options.filenames.isEmpty()) {
                options.filenames.add(arg);
            } else {
                options.singleMdxCmd = arg;
            }
        }
        return options;
    }

    private static class BadOption extends Exception {
        BadOption(String msg) {
            super(msg);
        }
        BadOption(String msg, Exception ex) {
            super(msg, ex);
        }
    }
}

/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2001-2005 Julian Hyde
// Copyright (C) 2005-2019 Hitachi Vantara and others
// All Rights Reserved.
*/
package mondrian.rolap;

import static mondrian.rolap.util.NamedSetUtil.getFormula;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.daanse.db.dialect.api.Dialect;
import org.eclipse.daanse.engine.api.Context;
import org.eclipse.daanse.olap.api.access.Access;
import org.eclipse.daanse.olap.api.access.Role;
import org.eclipse.daanse.olap.api.access.RollupPolicy;
import org.eclipse.daanse.olap.api.model.Cube;
import org.eclipse.daanse.olap.api.model.Dimension;
import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.Level;
import org.eclipse.daanse.olap.api.model.Member;
import org.eclipse.daanse.olap.api.model.NamedSet;
import org.eclipse.daanse.olap.api.model.OlapElement;
import org.eclipse.daanse.olap.api.model.Schema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.PrivateDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Relation;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Script;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.ParameterTypeEnum;
import org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb.DimensionUsageImpl;
import org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb.PrivateDimensionImpl;
import org.eigenbase.xom.DOMWrapper;
import org.eigenbase.xom.Parser;
import org.eigenbase.xom.XOMException;
import org.eigenbase.xom.XOMUtil;
import org.olap4j.impl.Olap4jUtil;
import org.olap4j.mdx.IdentifierSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import mondrian.olap.CacheControl;
import mondrian.olap.Category;
import mondrian.olap.Exp;
import mondrian.olap.Formula;
import mondrian.olap.FunTable;
import mondrian.olap.Id;
import mondrian.olap.MondrianProperties;
import mondrian.olap.MondrianServer;
import mondrian.olap.Parameter;
import mondrian.olap.RoleImpl;
import mondrian.olap.SchemaReader;
import mondrian.olap.Syntax;
import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.olap.fun.FunTableImpl;
import mondrian.olap.fun.GlobalFunTable;
import mondrian.olap.fun.Resolver;
import mondrian.olap.fun.UdfResolver;
import mondrian.olap.type.MemberType;
import mondrian.olap.type.NumericType;
import mondrian.olap.type.StringType;
import mondrian.olap.type.Type;
import mondrian.resource.MondrianResource;
import mondrian.rolap.aggmatcher.AggTableManager;
import mondrian.spi.DataSourceChangeListener;
import mondrian.spi.UserDefinedFunction;
import mondrian.spi.impl.Scripts;
import mondrian.util.ByteString;
import mondrian.util.ClassResolver;

/**
 * A <code>RolapSchema</code> is a collection of {@link RolapCube}s and
 * shared {@link RolapDimension}s. It is shared betweeen {@link
 * RolapConnection}s. It caches {@link MemberReader}s, etc.
 *
 * @see RolapConnection
 * @author jhyde
 * @since 26 July, 2001
 */
public class RolapSchema implements Schema {
    static final Logger LOGGER = LoggerFactory.getLogger(RolapSchema.class);

    private static final Set<Access> schemaAllowed =
        Olap4jUtil.enumSetOf(
            Access.NONE,
            Access.ALL,
            Access.ALL_DIMENSIONS,
            Access.CUSTOM);

    private static final Set<Access> cubeAllowed =
        Olap4jUtil.enumSetOf(Access.NONE, Access.ALL, Access.CUSTOM);

    private static final Set<Access> dimensionAllowed =
        Olap4jUtil.enumSetOf(Access.NONE, Access.ALL, Access.CUSTOM);

    private static final Set<Access> hierarchyAllowed =
        Olap4jUtil.enumSetOf(Access.NONE, Access.ALL, Access.CUSTOM);

    private static final Set<Access> memberAllowed =
        Olap4jUtil.enumSetOf(Access.NONE, Access.ALL);
    public static final String WHILE_PARSING_CATALOG = "while parsing catalog ";

    private String name;

    /**
     * Internal use only.
     */
    private RolapConnection internalConnection;

    /**
     * Holds cubes in this schema.
     */
    private final Map<String, RolapCube> mapNameToCube =
        new HashMap<>();

    /**
     * Maps {@link String shared hierarchy name} to {@link MemberReader}.
     * Shared between all statements which use this connection.
     */
    private final Map<String, MemberReader> mapSharedHierarchyToReader =
        new HashMap<>();

    /**
     * Maps {@link String names of shared hierarchies} to {@link
     * RolapHierarchy the canonical instance of those hierarchies}.
     */
    private final Map<String, RolapHierarchy> mapSharedHierarchyNameToHierarchy
        =
        new HashMap<>();

    /**
     * The default role for connections to this schema.
     */
    private Role defaultRole;

    private ByteString sha512Bytes;

    /**
     * A schema's aggregation information
     */
    private AggTableManager aggTableManager;

    /**
     * This is basically a unique identifier for this RolapSchema instance
     * used it its equals and hashCode methods.
     */
    final SchemaKey key;

    /**
     * Maps {@link String names of roles} to {@link Role roles with those names}.
     */
    private final Map<String, Role> mapNameToRole = new HashMap<>();

    /**
     * Maps {@link String names of sets} to {@link NamedSet named sets}.
     */
    private final Map<String, NamedSet> mapNameToSet =
        new HashMap<>();

    /**
     * Table containing all standard MDX functions, plus user-defined functions
     * for this schema.
     */
    private FunTable funTable;

    private org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema xmlSchema;

    final List<RolapSchemaParameter > parameterList =
        new ArrayList< >();

    private Date schemaLoadDate;

    private DataSourceChangeListener dataSourceChangeListener;

    /**
     * List of warnings. Populated when a schema is created by a connection
     * that has
     * {@link mondrian.rolap.RolapConnectionProperties#Ignore Ignore}=true.
     */
    private final List<Exception> warningList = new ArrayList<>();
    private Map<String, Object> metadata;

    /**
     * Unique schema instance id that will be used
     * to inform clients when the schema has changed.
     *
     * <p>Expect a different ID for each Mondrian instance node.
     */
    private final String id;

    private Context context;

    /**
     * This is ONLY called by other constructors (and MUST be called
     * by them) and NEVER by the Pool.
     *
     * @param key Key
     * @param connectInfo Connect properties
     * @param context Context
     * @param sha512Bytes MD5 hash
     * @param useContentChecksum Whether to use content checksum
     */
    private RolapSchema(
        final SchemaKey key,
        final Util.PropertyList connectInfo,
        final Context context,
        final ByteString sha512Bytes,
        boolean useContentChecksum)
    {
        this.id = Util.generateUuidString();
        this.key = key;
        this.sha512Bytes = sha512Bytes;
        if (useContentChecksum && sha512Bytes == null) {
            throw new AssertionError();
        }

        this.context=context;
        DriverManager.drivers().forEach(System.out::println);
        // the order of the next two lines is important
        this.defaultRole = Util.createRootRole(this);
        final MondrianServer internalServer = MondrianServer.forId(null);
        this.internalConnection =
            new RolapConnection(internalServer, connectInfo, this, context);
        internalServer.removeConnection(internalConnection);
        internalServer.removeStatement(
            internalConnection.getInternalStatement());

        this.aggTableManager = new AggTableManager(this);
        this.dataSourceChangeListener =
            createDataSourceChangeListener(connectInfo);
    }

    /**
     * Create RolapSchema given the MD5 hash, catalog name and string (content)
     * and the connectInfo object.
     *
     * @param md5Bytes may be null
     * @param catalogUrl URL of catalog
     * @param catalogStr may be null
     * @param connectInfo Connection properties
     */
    public RolapSchema(
        SchemaKey key,
        ByteString md5Bytes,
        String catalogUrl,
        String catalogStr,
        Util.PropertyList connectInfo,
        Context context)
    {
        this(key, connectInfo, context, md5Bytes, md5Bytes != null);
        load(catalogUrl, catalogStr, connectInfo);
        assert this.sha512Bytes != null;
    }

    /**
     * @deprecated for tests only!
     */
    @Deprecated
    RolapSchema(
        SchemaKey key,
        ByteString md5Bytes,
        RolapConnection internalConnection)
    {
        this.id = Util.generateUuidString();
        this.key = key;
        this.sha512Bytes = md5Bytes;
        this.defaultRole = Util.createRootRole(this);
        this.internalConnection = internalConnection;
    }

    protected void flushSegments() {
        final RolapConnection internalConnection = getInternalConnection();
        if (internalConnection != null) {
            final CacheControl cc = internalConnection.getCacheControl(null);
            for (RolapCube cube : getCubeList()) {
                cc.flush(cc.createMeasuresRegion(cube));
            }
        }
    }

    /**
     * Clears the cache of JDBC tables for the aggs.
     */
    protected void flushJdbcSchema() {
        // Cleanup the agg table manager's caches.
        if (aggTableManager != null) {
            aggTableManager.finalCleanUp();
            aggTableManager = null;
        }
    }

    /**
     * Performs a sweep of the JDBC tables caches and the segment data.
     * Only called internally when a schema and it's data must be refreshed.
     */
    protected void finalCleanUp() {
        // Cleanup the segment data.
        flushSegments();

        // Cleanup the agg JDBC cache
        flushJdbcSchema();
    }

    @Override
	protected void finalize() throws Throwable {
        try {
            super.finalize();
            // Only clear the JDBC cache to prevent leaks.
            flushJdbcSchema();
        } catch (Throwable t) {
            LOGGER.info(
                MondrianResource.instance()
                    .FinalizerErrorRolapSchema.baseMessage,
                t);
        }
    }

    @Override
	public boolean equals(Object o) {
        if (!(o instanceof RolapSchema other)) {
            return false;
        }
        return other.key.equals(key);
    }

    @Override
	public int hashCode() {
        return key.hashCode();
    }

    protected Logger getLogger() {
        return LOGGER;
    }

  /**
   * @deprecated API changed to also pass Mondrian connection properties
   * @param catalogUrl URL of catalog
   * @param catalogStr Text of catalog, or null
   */
  @Deprecated
    protected void load(String catalogUrl, String catalogStr) {
      load(catalogUrl, catalogStr, new PropertyList());
    }

    /**
     * Method called by all constructors to load the catalog into DOM and build
     * application mdx and sql objects.
     *
     * @param catalogUrl URL of catalog
     * @param catalogStr Text of catalog, or null
     * @param connectInfo Mondrian connection properties
     */
    protected void load(
        String catalogUrl,
        String catalogStr,
        PropertyList connectInfo)
    {
        try {
            final Parser xmlParser = XOMUtil.createDefaultParser();

            final DOMWrapper def;
            if (catalogStr == null) {
                InputStream in = null;
                try {
                    in = Util.readVirtualFile(catalogUrl);
                    def = xmlParser.parse(in);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }

                // Compute catalog string, if needed for debug or for computing
                // Md5 hash.
                if (getLogger().isDebugEnabled() || sha512Bytes == null) {
                    try {
                        catalogStr = Util.readVirtualFileAsString(catalogUrl);
                    } catch (java.io.IOException ex) {
                        getLogger().debug("RolapSchema.load: ex=" + ex);
                        catalogStr = "?";
                    }
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "RolapSchema.load: content: \n" + catalogStr);
                }
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "RolapSchema.load: catalogStr: \n" + catalogStr);
                }

                def = xmlParser.parse(catalogStr);
            }

            if (sha512Bytes == null) {
                // If a null catalogStr was passed in, we should have
                // computed it above by re-reading the catalog URL.
                assert catalogStr != null;
                sha512Bytes = new ByteString(Util.digestSHA(catalogStr));
            }

            // throw error if we have an incompatible schema
            checkSchemaVersion(def);
            //TODO remove def
            JAXBContext jaxbContext =
                JAXBContext.newInstance(org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb.SchemaImpl.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            xmlSchema =
                (org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema) jaxbUnmarshaller.unmarshal(new StringReader(catalogStr));

            if (getLogger().isDebugEnabled()) {
                StringWriter sw = new StringWriter(4096);
                PrintWriter pw = new PrintWriter(sw);
                pw.println("RolapSchema.load: dump xmlschema");
                xmlSchema.display(pw, 2);
                pw.flush();
                getLogger().debug(sw.toString());
            }

            load(xmlSchema);
        } catch (FileSystemException e) {
            throw Util.newError(e, WHILE_PARSING_CATALOG + catalogUrl);
        } catch (XOMException | IOException | JAXBException e) {
            throw Util.newError(e, WHILE_PARSING_CATALOG + catalogUrl);
        }


        aggTableManager.initialize(connectInfo);
        setSchemaLoadDate();
    }

    private void checkSchemaVersion(final DOMWrapper schemaDom) {
        String schemaVersion = schemaDom.getAttribute("metamodelVersion");
        if (schemaVersion == null) {
            if (hasMondrian4Elements(schemaDom)) {
                schemaVersion = "4.x";
            } else {
                schemaVersion = "3.x";
            }
        }

        String[] versionParts = schemaVersion.split("\\.");
        final String schemaMajor =
            versionParts.length > 0 ? versionParts[0] : "";

        String serverSchemaVersion =
            Integer.toString(MondrianServer.forId(null).getSchemaVersion());

        if (serverSchemaVersion.compareTo(schemaMajor) < 0) {
            String errorMsg =
                new StringBuilder("Schema version '").append(schemaVersion)
                    .append("' is later than schema version ")
                    .append("'3.x' supported by this version of Mondrian").toString();
            throw Util.newError(errorMsg);
        }
    }

    private boolean hasMondrian4Elements(final DOMWrapper schemaDom) {
        // check for Mondrian 4 schema elements:
        for (DOMWrapper child : schemaDom.getChildren()) {
            if ("PhysicalSchema".equals(child.getTagName())) {
                // Schema/PhysicalSchema
                return true;
            } else if ("Cube".equals(child.getTagName())) {
                for (DOMWrapper grandchild : child.getChildren()) {
                    if ("MeasureGroups".equals(grandchild.getTagName())) {
                        // Schema/Cube/MeasureGroups
                        return true;
                    }
                }
            }
        }
        // otherwise assume version 3.x
        return false;
    }

    private void setSchemaLoadDate() {
        schemaLoadDate = new Date();
    }

    @Override
	public Date getSchemaLoadDate() {
        return schemaLoadDate;
    }

    @Override
	public List<Exception> getWarnings() {
        return Collections.unmodifiableList(warningList);
    }

    public Role getDefaultRole() {
        return defaultRole;
    }

    public org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema getXMLSchema() {
        return xmlSchema;
    }

    @Override
	public String getName() {
        Util.assertPostcondition(name != null, "return != null");
        Util.assertPostcondition(name.length() > 0, "return.length() > 0");
        return name;
    }

    /**
     * Returns this schema instance unique ID.
     * @return A string representing the schema ID.
     */
    @Override
	public String getId() {
        return this.id;
    }

    /**
     * Returns this schema instance unique key.
     * @return a {@link SchemaKey}.
     */
    public SchemaKey getKey() {
        return key;
    }

    @Override
	public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Returns this schema's SQL dialect.
     *
     * <p>NOTE: This method is not cheap. The implementation gets a connection
     * from the connection pool.
     *
     * @return dialect
     */
    public Dialect getDialect() {
        return context.getDialect();
    }

    private void load(org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema xmlSchema) {
        this.name = xmlSchema.name();
        if (name == null || name.equals("")) {
            throw Util.newError("<Schema> name must be set");
        }

        this.metadata =
            RolapHierarchy.createMetadataMap(xmlSchema.annotations());
        // Validate user-defined functions. Must be done before we validate
        // calculated members, because calculated members will need to use the
        // function table.
        final Map<String, UdfResolver.UdfFactory> mapNameToUdf =
            new HashMap<>();
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.UserDefinedFunction udf
            : xmlSchema.userDefinedFunctions())
        {
            final Scripts.ScriptDefinition scriptDef = toScriptDef(udf.script());
            defineFunction(mapNameToUdf, udf.name(), udf.className(), scriptDef);
        }
        final RolapSchemaFunctionTable funTable =
            new RolapSchemaFunctionTable(mapNameToUdf.values());
        funTable.init();
        this.funTable = funTable;

        // Validate public dimensions.
        for (PrivateDimension xmlDimension : xmlSchema.dimensions()) {
            if (xmlDimension.foreignKey() != null) {
                throw MondrianResource.instance()
                    .PublicDimensionMustNotHaveForeignKey.ex(
                        xmlDimension.name());
            }
        }

        // Create parameters.
        Set<String> parameterNames = new HashSet<>();
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.Parameter xmlParameter : xmlSchema.parameters()) {
            String name = xmlParameter.name();
            if (!parameterNames.add(name)) {
                throw MondrianResource.instance().DuplicateSchemaParameter.ex(
                    name);
            }
            Type type;
            if (ParameterTypeEnum.STRING.equals(xmlParameter.type())) {
                type = new StringType();
            } else if (ParameterTypeEnum.NUMERIC.equals(xmlParameter.type())) {
                type = new NumericType();
            } else {
                type = new MemberType(null, null, null, null);
            }
            final String description = xmlParameter.description();
            final boolean modifiable = xmlParameter.modifiable();
            String defaultValue = xmlParameter.defaultValue();
            RolapSchemaParameter param =
                new RolapSchemaParameter(
                    this, name, defaultValue, description, type, modifiable);
            Util.discard(param);
        }

        // Create cubes.
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.Cube xmlCube : xmlSchema.cubes()) {
            if (xmlCube.enabled()) {
                RolapCube cube = new RolapCube(this, xmlSchema, xmlCube, context);
                Util.discard(cube);
            }
        }

        // Create virtual cubes.
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.VirtualCube xmlVirtualCube : xmlSchema.virtualCubes()) {
            if (xmlVirtualCube.enabled()) {
                RolapCube cube =
                    new RolapCube(this, xmlSchema, xmlVirtualCube, context);
                Util.discard(cube);
            }
        }

        // Create named sets.
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.NamedSet xmlNamedSet : xmlSchema.namedSets()) {
            mapNameToSet.put(xmlNamedSet.name(), createNamedSet(xmlNamedSet));
        }

        // Create roles.
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.Role xmlRole : xmlSchema.roles()) {
            Role role = createRole(xmlRole);
            mapNameToRole.put(xmlRole.name(), role);
        }

        // Set default role.
        if (xmlSchema.defaultRole() != null) {
            Role role = lookupRole(xmlSchema.defaultRole());
            if (role == null) {
                error(
                    new StringBuilder("Role '").append(xmlSchema.defaultRole()).append("' not found").toString(),
                    locate(xmlSchema, "defaultRole"));
            } else {
                // At this stage, the only roles in mapNameToRole are
                // RoleImpl roles so it is safe to case.
                defaultRole = role;
            }
        }
    }

    static Scripts.ScriptDefinition toScriptDef(Script script) {
        if (script == null) {
            return null;
        }
        final Scripts.ScriptLanguage language =
            Scripts.ScriptLanguage.lookup(script.language());
        if (language == null) {
            throw Util.newError(
                new StringBuilder("Invalid script language '").append(script.language()).append("'").toString());
        }
        return new Scripts.ScriptDefinition(script.cdata(), language);
    }

    /**
     * Returns the location of an element or attribute in an XML document.
     *
     * <p>TODO: modify eigenbase-xom parser to return position info
     *
     * @param node Node
     * @param attributeName Attribute name, or null
     * @return Location of node or attribute in an XML document
     */
    XmlLocation locate(org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema node, String attributeName) {
        return null;
    }

    /**
     * Reports an error. If we are tolerant of errors
     * (see {@link mondrian.rolap.RolapConnectionProperties#Ignore}), adds
     * it to the stack, overwise throws. A thrown exception will typically
     * abort the attempt to create the exception.
     *
     * @param message Message
     * @param xmlLocation Location of XML element or attribute that caused
     * the error, or null
     */
    void error(
        String message,
        XmlLocation xmlLocation)
    {
        final RuntimeException ex = new RuntimeException(message);
        if (internalConnection != null
            && "true".equals(
                internalConnection.getProperty(
                    RolapConnectionProperties.Ignore.name())))
        {
            warningList.add(ex);
        } else {
            throw ex;
        }
    }

    private NamedSet createNamedSet(org.eclipse.daanse.olap.rolap.dbmapper.model.api.NamedSet xmlNamedSet) {
        final String formulaString = getFormula(xmlNamedSet);
        final Exp exp;
        try {
            exp = getInternalConnection().parseExpression(formulaString);
        } catch (Exception e) {
            throw MondrianResource.instance().NamedSetHasBadFormula.ex(
                xmlNamedSet.name(), e);
        }
        final Formula formula =
            new Formula(
                new Id(
                    new Id.NameSegment(
                        xmlNamedSet.name(),
                        Id.Quoting.UNQUOTED)),
                exp);
        return formula.getNamedSet();
    }

    private Role createRole(org.eclipse.daanse.olap.rolap.dbmapper.model.api.Role xmlRole) {
        if (xmlRole.union() != null) {
            return createUnionRole(xmlRole);
        }

        RoleImpl role = new RoleImpl();
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.SchemaGrant schemaGrant : xmlRole.schemaGrants()) {
            handleSchemaGrant(role, schemaGrant);
        }
        role.makeImmutable();
        return role;
    }

    // package-local visibility for testing purposes
    Role createUnionRole(org.eclipse.daanse.olap.rolap.dbmapper.model.api.Role xmlRole) {
        if (xmlRole.schemaGrants() != null && !xmlRole.schemaGrants().isEmpty()) {
            throw MondrianResource.instance().RoleUnionGrants.ex();
        }

        List<? extends org.eclipse.daanse.olap.rolap.dbmapper.model.api.RoleUsage> usages = xmlRole.union().roleUsages();
        List<Role> roleList = new ArrayList<>(usages.size());
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.RoleUsage roleUsage : usages) {
            Role role = mapNameToRole.get(roleUsage.roleName());
            if (role == null) {
                throw MondrianResource.instance().UnknownRole.ex(
                    roleUsage.roleName());
            }
            roleList.add(role);
        }
        return RoleImpl.union(roleList);
    }

    // package-local visibility for testing purposes
    void handleSchemaGrant(RoleImpl role, org.eclipse.daanse.olap.rolap.dbmapper.model.api.SchemaGrant schemaGrant) {
        role.grant(this, getAccess(schemaGrant.access().name(), schemaAllowed));
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.CubeGrant cubeGrant : schemaGrant.cubeGrants()) {
            handleCubeGrant(role, cubeGrant);
        }
    }

    // package-local visibility for testing purposes
    public void handleCubeGrant(RoleImpl role, org.eclipse.daanse.olap.rolap.dbmapper.model.api.CubeGrant cubeGrant) {
        RolapCube cube = lookupCube(cubeGrant.cube());
        if (cube == null) {
            throw Util.newError(new StringBuilder("Unknown cube '").append(cubeGrant.cube()).append("'").toString());
        }
        role.grant(cube, getAccess(cubeGrant.access(), cubeAllowed));

        SchemaReader reader = cube.getSchemaReader(null);
        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.DimensionGrant grant
            : cubeGrant.dimensionGrants())
        {
            Dimension dimension =
                lookup(cube, reader, Category.DIMENSION, grant.dimension());
            role.grant(
                dimension,
                getAccess(grant.access().name(), dimensionAllowed));
        }

        for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.HierarchyGrant hierarchyGrant
            : cubeGrant.hierarchyGrants())
        {
            handleHierarchyGrant(role, cube, reader, hierarchyGrant);
        }
    }

    // package-local visibility for testing purposes
    public void handleHierarchyGrant(
        RoleImpl role,
        RolapCube cube,
        SchemaReader reader,
        org.eclipse.daanse.olap.rolap.dbmapper.model.api.HierarchyGrant grant)
    {
        Hierarchy hierarchy =
            lookup(cube, reader, Category.HIERARCHY, grant.hierarchy());
        final Access hierarchyAccess =
            getAccess(grant.access().getValue(), hierarchyAllowed);
        Level topLevel = findLevelForHierarchyGrant(
            cube, reader, hierarchyAccess, grant.topLevel(), "topLevel");
        Level bottomLevel = findLevelForHierarchyGrant(
            cube, reader, hierarchyAccess, grant.bottomLevel(), "bottomLevel");

        RollupPolicy rollupPolicy;
        if (grant.rollupPolicy() != null) {
            try {
                rollupPolicy =
                    RollupPolicy.valueOf(
                        grant.rollupPolicy().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw Util.newError(
                    new StringBuilder("Illegal rollupPolicy value '")
                        .append(grant.rollupPolicy())
                        .append("'").toString());
            }
        } else {
            rollupPolicy = RollupPolicy.FULL;
        }
        role.grant(
            hierarchy, hierarchyAccess, topLevel, bottomLevel, rollupPolicy);

        final boolean ignoreInvalidMembers =
            MondrianProperties.instance().IgnoreInvalidMembers.get();

        int membersRejected = 0;
        if (!grant.memberGrants().isEmpty()) {
            if (hierarchyAccess != Access.CUSTOM) {
                throw Util.newError(
                    "You may only specify <MemberGrant> if <Hierarchy> has access='custom'");
            }

            for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.MemberGrant memberGrant
                : grant.memberGrants())
            {
                Member member = reader.withLocus()
                    .getMemberByUniqueName(
                        Util.parseIdentifier(memberGrant.member()),
                        !ignoreInvalidMembers);
                if (member == null) {
                    // They asked to ignore members that don't exist
                    // (e.g. [Store].[USA].[Foo]), so ignore this grant
                    // too.
                    assert ignoreInvalidMembers;
                    membersRejected++;
                    continue;
                }
                if (member.getHierarchy() != hierarchy) {
                    throw Util.newError(
                        new StringBuilder("Member '").append(member)
                        .append("' is not in hierarchy '").append(hierarchy).append("'").toString());
                }
                role.grant(
                    member,
                    getAccess(memberGrant.access().getValue(), memberAllowed));
            }
        }

        if (membersRejected > 0
            && grant.memberGrants().size() == membersRejected)
        {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                    "Rolling back grants of Hierarchy '{}' to NONE, because it contains no valid restricted members",
                    hierarchy.getUniqueName());
            }
            role.grant(hierarchy, Access.NONE, null, null, rollupPolicy);
        }
    }

    private <T extends OlapElement> T lookup(
        RolapCube cube,
        SchemaReader reader,
        int category,
        String id)
    {
        List<Id.Segment> segments = Util.parseIdentifier(id);
        //noinspection unchecked
        return (T) reader.lookupCompound(cube, segments, true, category);
    }

    private Level findLevelForHierarchyGrant(
        RolapCube cube,
        SchemaReader schemaReader,
        Access hierarchyAccess,
        String name, String desc)
    {
        if (name == null) {
            return null;
        }

        if (hierarchyAccess != Access.CUSTOM) {
            throw Util.newError(
                new StringBuilder("You may only specify '").append(desc).append("' if access='custom'").toString());
        }
        return lookup(cube, schemaReader, Category.LEVEL, name);
    }

    private Access getAccess(String accessString, Set<Access> allowed) {
        final Access access = Access.valueOf(accessString.toUpperCase());
        if (allowed.contains(access)) {
            return access; // value is ok
        }
        throw Util.newError(new StringBuilder("Bad value access='").append(accessString).append("'").toString());
    }

    @Override
	public Dimension createDimension(Cube cube, String xml) {
        org.eclipse.daanse.olap.rolap.dbmapper.model.api.CubeDimension xmlDimension;
        try {
            final Parser xmlParser = XOMUtil.createDefaultParser();
            final DOMWrapper def = xmlParser.parse(xml);
            final String tagName = def.getTagName();
            if (tagName.equals("Dimension")) {
                JAXBContext jaxbContext = JAXBContext.newInstance(PrivateDimensionImpl.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                xmlDimension = (org.eclipse.daanse.olap.rolap.dbmapper.model.api.PrivateDimension) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            } else if (tagName.equals("DimensionUsage")) {
                JAXBContext jaxbContext = JAXBContext.newInstance(DimensionUsageImpl.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                xmlDimension = (org.eclipse.daanse.olap.rolap.dbmapper.model.api.DimensionUsage) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            } else {
                throw new XOMException(
                    new StringBuilder("Got <").append(tagName)
                    .append("> when expecting <Dimension> or <DimensionUsage>").toString());
            }
        } catch (XOMException | JAXBException e) {
            throw Util.newError(
                e,
                new StringBuilder("Error while adding dimension to cube '").append(cube)
                .append("' from XML [").append(xml).append("]").toString());
        }
        return ((RolapCube) cube).createDimension(xmlDimension, xmlSchema);
    }

    @Override
	public Cube createCube(String xml) {
        RolapCube cube;
        try {
            final Parser xmlParser = XOMUtil.createDefaultParser();
            final DOMWrapper def = xmlParser.parse(xml);
            final String tagName = def.getTagName();
            if (tagName.equals("Cube")) {
                // Create empty XML schema, to keep the method happy. This is
                // okay, because there are no forward-references to resolve.
                final org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema xmlSchema = new org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb.SchemaImpl();
                JAXBContext jaxbContext = JAXBContext.newInstance(org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb.CubeImpl.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                org.eclipse.daanse.olap.rolap.dbmapper.model.api.Cube xmlDimension = (org.eclipse.daanse.olap.rolap.dbmapper.model.api.Cube) jaxbUnmarshaller.unmarshal(new StringReader(xml));
                cube = new RolapCube(this, xmlSchema, xmlDimension, context);
            } else if (tagName.equals("VirtualCube")) {
                // Need the real schema here.
                org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema xmlSchema = getXMLSchema();
                JAXBContext jaxbContext = JAXBContext.newInstance(org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb.VirtualCubeImpl.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                org.eclipse.daanse.olap.rolap.dbmapper.model.api.VirtualCube xmlDimension = (org.eclipse.daanse.olap.rolap.dbmapper.model.api.VirtualCube) jaxbUnmarshaller.unmarshal(new StringReader(xml));
                cube = new RolapCube(this, xmlSchema, xmlDimension, context);
            } else {
                throw new XOMException(
                    new StringBuilder("Got <").append(tagName).append("> when expecting <Cube>").toString());
            }
        } catch (XOMException | JAXBException e) {
            throw Util.newError(
                e,
                new StringBuilder("Error while creating cube from XML [").append(xml).append("]").toString());
        }
        return cube;
    }

    public static List<RolapSchema> getRolapSchemas() {
        return RolapSchemaPool.instance().getRolapSchemas();
    }

    public static boolean cacheContains(RolapSchema rolapSchema) {
        return RolapSchemaPool.instance().contains(rolapSchema);
    }

    @Override
	public Cube lookupCube(final String cube, final boolean failIfNotFound) {
        RolapCube mdxCube = lookupCube(cube);
        if (mdxCube == null && failIfNotFound) {
            throw MondrianResource.instance().MdxCubeNotFound.ex(cube);
        }
        return mdxCube;
    }

    /**
     * Finds a cube called 'cube' in the current catalog, or return null if no
     * cube exists.
     */
    protected RolapCube lookupCube(final String cubeName) {
        return mapNameToCube.get(Util.normalizeName(cubeName));
    }

    /**
     * Returns an xmlCalculatedMember called 'calcMemberName' in the
     * cube called 'cubeName' or return null if no calculatedMember or
     * xmlCube by those name exists.
     */
    protected org.eclipse.daanse.olap.rolap.dbmapper.model.api.CalculatedMember lookupXmlCalculatedMember(
        final String calcMemberName,
        final String cubeName)
    {
        for (final org.eclipse.daanse.olap.rolap.dbmapper.model.api.Cube cube : xmlSchema.cubes()) {
            if (!Util.equalName(cube.name(), cubeName)) {
                continue;
            }
            for (org.eclipse.daanse.olap.rolap.dbmapper.model.api.CalculatedMember xmlCalcMember
                : cube.calculatedMembers())
            {
                // FIXME: Since fully-qualified names are not unique, we
                // should compare unique names. Also, the logic assumes that
                // CalculatedMember.dimension is not quoted (e.g. "Time")
                // and CalculatedMember.hierarchy is quoted
                // (e.g. "[Time].[Weekly]").
                if (Util.equalName(
                        calcMemberFqName(xmlCalcMember),
                        calcMemberName))
                {
                    return xmlCalcMember;
                }
            }
        }
        return null;
    }

    public static String calcMemberFqName(org.eclipse.daanse.olap.rolap.dbmapper.model.api.CalculatedMember xmlCalcMember)
    {
        if (xmlCalcMember.dimension() != null) {
            return Util.makeFqName(
                Util.quoteMdxIdentifier(xmlCalcMember.dimension()),
                xmlCalcMember.name());
        } else {
            return Util.makeFqName(
                xmlCalcMember.hierarchy(), xmlCalcMember.name());
        }
    }

    public List<RolapCube> getCubesWithStar(RolapStar star) {
        List<RolapCube> list = new ArrayList<>();
        for (RolapCube cube : mapNameToCube.values()) {
            if (star == cube.getStar()) {
                list.add(cube);
            }
        }
        return list;
    }

    /**
     * Adds a cube to the cube name map.
     * @see #lookupCube(String)
     */
    protected void addCube(final RolapCube cube) {
        mapNameToCube.put(
            Util.normalizeName(cube.getName()),
            cube);
    }

    @Override
	public boolean removeCube(final String cubeName) {
        final RolapCube cube =
            mapNameToCube.remove(Util.normalizeName(cubeName));
        return cube != null;
    }

    @Override
	public Cube[] getCubes() {
        Collection<RolapCube> cubes = mapNameToCube.values();
        return cubes.toArray(new RolapCube[cubes.size()]);
    }

    public List<RolapCube> getCubeList() {
        return new ArrayList<>(mapNameToCube.values());
    }

    @Override
	public Hierarchy[] getSharedHierarchies() {
        Collection<RolapHierarchy> hierarchies =
            mapSharedHierarchyNameToHierarchy.values();
        return hierarchies.toArray(new RolapHierarchy[hierarchies.size()]);
    }

    RolapHierarchy getSharedHierarchy(final String name) {
        return mapSharedHierarchyNameToHierarchy.get(name);
    }

    public NamedSet getNamedSet(String name) {
        return mapNameToSet.get(name);
    }

    public NamedSet getNamedSet(IdentifierSegment segment) {
        // FIXME: write a map that efficiently maps segment->value, taking
        // into account case-sensitivity etc.
        for (Map.Entry<String, NamedSet> entry : mapNameToSet.entrySet()) {
            if (Util.matches(segment, entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
	public Role lookupRole(final String role) {
        return mapNameToRole.get(role);
    }

    public Set<String> roleNames() {
        return mapNameToRole.keySet();
    }

    @Override
	public FunTable getFunTable() {
        return funTable;
    }

    @Override
	public Parameter[] getParameters() {
        return parameterList.toArray(
            new Parameter[parameterList.size()]);
    }

    /**
     * Defines a user-defined function in this table.
     *
     * <p>If the function is not valid, throws an error.
     *
     * @param name Name of the function.
     * @param className Name of the class which implements the function.
     *   The class must implement {@link mondrian.spi.UserDefinedFunction}
     *   (otherwise it is a user-error).
     */
    private void defineFunction(
        Map<String, UdfResolver.UdfFactory> mapNameToUdf,
        final String name,
        String className,
        final Scripts.ScriptDefinition script)
    {
        if (className == null && script == null) {
            throw Util.newError(
                "Must specify either className attribute or Script element");
        }
        if (className != null && script != null) {
            throw Util.newError(
                "Must not specify both className attribute and Script element");
        }
        final UdfResolver.UdfFactory udfFactory;
        if (className != null) {
            // Lookup class.
            try {
                final Class<UserDefinedFunction> klass =
                    ClassResolver.INSTANCE.forName(className, true);

                // Instantiate UDF by calling correct constructor.
                udfFactory = new UdfResolver.ClassUdfFactory(klass, name);
            } catch (ClassNotFoundException e) {
                throw MondrianResource.instance().UdfClassNotFound.ex(
                    name,
                    className);
            }
        } else {
            udfFactory =
                new UdfResolver.UdfFactory() {
                    @Override
					public UserDefinedFunction create() {
                        return Scripts.userDefinedFunction(script, name);
                    }
                };
        }
        // Validate function.
        validateFunction(udfFactory);
        // Check for duplicate.
        UdfResolver.UdfFactory existingUdf = mapNameToUdf.get(name);
        if (existingUdf != null) {
            throw MondrianResource.instance().UdfDuplicateName.ex(name);
        }
        mapNameToUdf.put(name, udfFactory);
    }

    /**
     * Throws an error if a user-defined function does not adhere to the
     * API.
     */
    private void validateFunction(UdfResolver.UdfFactory udfFactory) {
        final UserDefinedFunction udf = udfFactory.create();

        // Check that the name is not null or empty.
        final String udfName = udf.getName();
        if (udfName == null || udfName.equals("")) {
            throw Util.newInternal(
                new StringBuilder("User-defined function defined by class '")
                .append(udf.getClass()).append("' has empty name").toString());
        }
        // It's OK for the description to be null.
        final String description = udf.getDescription();
        Util.discard(description);
        final Type[] parameterTypes = udf.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Type parameterType = parameterTypes[i];
            if (parameterType == null) {
                throw Util.newInternal(
                    new StringBuilder("Invalid user-defined function '")
                    .append(udfName).append("': parameter type #").append(i).append(" is null").toString());
            }
        }
        // It's OK for the reserved words to be null or empty.
        final String[] reservedWords = udf.getReservedWords();
        Util.discard(reservedWords);
        // Test that the function returns a sensible type when given the FORMAL
        // types. It may still fail when we give it the ACTUAL types, but it's
        // impossible to check that now.
        final Type returnType = udf.getReturnType(parameterTypes);
        if (returnType == null) {
            throw Util.newInternal(
                new StringBuilder("Invalid user-defined function '")
                .append(udfName).append("': return type is null").toString());
        }
        final Syntax syntax = udf.getSyntax();
        if (syntax == null) {
            throw Util.newInternal(
                new StringBuilder("Invalid user-defined function '")
                .append(udfName).append("': syntax is null").toString());
        }
    }

    /**
     * Gets a {@link MemberReader} with which to read a hierarchy. If the
     * hierarchy is shared (<code>sharedName</code> is not null), looks up
     * a reader from a cache, or creates one if necessary.
     *
     * <p>Synchronization: thread safe
     */
    synchronized MemberReader createMemberReader(
        final String sharedName,
        final RolapHierarchy hierarchy,
        final String memberReaderClass)
    {
        MemberReader reader;
        if (sharedName != null) {
            reader = mapSharedHierarchyToReader.get(sharedName);
            if (reader == null) {
                reader = createMemberReader(hierarchy, memberReaderClass);
                // share, for other uses of the same shared hierarchy
                if (false) {
                    mapSharedHierarchyToReader.put(sharedName, reader);
                }
/*
System.out.println("RolapSchema.createMemberReader: "+
"add to sharedHierName->Hier map"+
" sharedName=" + sharedName +
", hierarchy=" + hierarchy.getName() +
", hierarchy.dim=" + hierarchy.getDimension().getName()
);
if (mapSharedHierarchyNameToHierarchy.containsKey(sharedName)) {
System.out.println("RolapSchema.createMemberReader: CONTAINS NAME");
} else {
                mapSharedHierarchyNameToHierarchy.put(sharedName, hierarchy);
}
*/
                mapSharedHierarchyNameToHierarchy.computeIfAbsent(sharedName, k -> hierarchy);

                //mapSharedHierarchyNameToHierarchy.put(sharedName, hierarchy);
            } else {
//                final RolapHierarchy sharedHierarchy = (RolapHierarchy)
//                        mapSharedHierarchyNameToHierarchy.get(sharedName);
//                final RolapDimension sharedDimension = (RolapDimension)
//                        sharedHierarchy.getDimension();
//                final RolapDimension dimension =
//                    (RolapDimension) hierarchy.getDimension();
//                Util.assertTrue(
//                        dimension.getGlobalOrdinal() ==
//                        sharedDimension.getGlobalOrdinal());
            }
        } else {
            reader = createMemberReader(hierarchy, memberReaderClass);
        }
        return reader;
    }

    /**
     * Creates a {@link MemberReader} with which to Read a hierarchy.
     */
    private MemberReader createMemberReader(
        final RolapHierarchy hierarchy,
        final String memberReaderClass)
    {
        if (memberReaderClass != null) {
            Exception e2;
            try {
                Properties properties = null;
                Class<?> clazz = ClassResolver.INSTANCE.forName(
                    memberReaderClass,
                    true);
                Constructor<?> constructor = clazz.getConstructor(
                    RolapHierarchy.class,
                    Properties.class);
                Object o = constructor.newInstance(hierarchy, properties);
                if (o instanceof MemberReader) {
                    return (MemberReader) o;
                } else if (o instanceof MemberSource) {
                    return new CacheMemberReader((MemberSource) o);
                } else {
                    throw Util.newInternal(
                        new StringBuilder("member reader class ").append(clazz)
                        .append(" does not implement ").append(MemberSource.class).toString());
                }
            } catch (ClassNotFoundException e) {
                e2 = e;
            } catch (NoSuchMethodException e) {
                e2 = e;
            } catch (InstantiationException e) {
                e2 = e;
            } catch (IllegalAccessException e) {
                e2 = e;
            } catch (InvocationTargetException e) {
                e2 = e;
            }
            throw Util.newInternal(
                e2,
                "while instantiating member reader '" + memberReaderClass);
        } else {
            SqlMemberSource source = new SqlMemberSource(hierarchy);
            Dimension dimension = hierarchy.getDimension();
            if (dimension.isHighCardinality()) {
                String msg = MondrianResource.instance()
                    .HighCardinalityInDimension.str(
                        dimension.getUniqueName());
                LOGGER.warn(msg);
                LOGGER.debug(
                    "High cardinality for {}", dimension);
                return new NoCacheMemberReader(source);
            } else {
                LOGGER.debug(
                    "Normal cardinality for {}", hierarchy.getDimension());
                if (MondrianProperties.instance().DisableCaching.get()) {
                    // If the cell cache is disabled, we can't cache
                    // the members or else we get undefined results,
                    // depending on the functions used and all.
                    return new NoCacheMemberReader(source);
                } else {
                    return new SmartMemberReader(source);
                }
            }
        }
    }

    @Override
	public SchemaReader getSchemaReader() {
        return new RolapSchemaReader(context,defaultRole, this).withLocus();
    }

    /**
     * Creates a {@link DataSourceChangeListener} with which to detect changes
     * to datasources.
     */
    private DataSourceChangeListener createDataSourceChangeListener(
        Util.PropertyList connectInfo)
    {
        DataSourceChangeListener changeListener = null;

        // If CatalogContent is specified in the connect string, ignore
        // everything else. In particular, ignore the dynamic schema
        // processor.
        String dataSourceChangeListenerStr = connectInfo.get(
            RolapConnectionProperties.DataSourceChangeListener.name());

        if (!Util.isEmpty(dataSourceChangeListenerStr)) {
            try {
                changeListener =
                    ClassResolver.INSTANCE.instantiateSafe(
                        dataSourceChangeListenerStr);
            } catch (Exception e) {
                throw Util.newError(
                    e,
                    "loading DataSourceChangeListener "
                    + dataSourceChangeListenerStr);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                    "RolapSchema.createDataSourceChangeListener: create datasource change listener {}\"",
                    dataSourceChangeListenerStr);
            }
        }
        return changeListener;
    }

    /**
     * Returns the checksum of this schema. Returns
     * <code>null</code> if {@link RolapConnectionProperties#UseContentChecksum}
     * is set to false.
     *
     * @return MD5 checksum of this schema
     */
    public ByteString getChecksum() {
        return sha512Bytes;
    }

    /**
     * Connection for purposes of parsing and validation. Careful! It won't
     * have the correct locale or access-control profile.
     */
    public RolapConnection getInternalConnection() {
        return internalConnection;
    }

 // package-local visibility for testing purposes
    public RolapStar makeRolapStar(final Relation fact) {
        return new RolapStar(this, context, fact);
    }

    /**
     * <code>RolapStarRegistry</code> is a registry for {@link RolapStar}s.
     */
    public class RolapStarRegistry {
        private final Map<List<String>, RolapStar> stars =
            new HashMap<>();

        RolapStarRegistry() {
        }

        /**
         * Looks up a {@link RolapStar}, creating it if it does not exist.
         *
         * <p> {@link RolapStar.Table#addJoin} works in a similar way.
         */
        synchronized RolapStar getOrCreateStar(
            final Relation fact)
        {
            final List<String> rolapStarKey = RolapUtil.makeRolapStarKey(fact);
            RolapStar star = stars.get(rolapStarKey);
            if (star == null) {
                star = makeRolapStar(fact);
                stars.put(rolapStarKey, star);
                // let cache manager load pending segments
                // from external cache if needed
                MondrianServer.forConnection(
                    internalConnection).getAggregationManager().getCacheMgr(internalConnection)
                    .loadCacheForStar(star);
            }
            return star;
        }

        synchronized RolapStar getStar(List<String> starKey) {
          return stars.get(starKey);
      }

        synchronized Collection<RolapStar> getStars() {
            return stars.values();
        }
    }

    private RolapStarRegistry rolapStarRegistry = new RolapStarRegistry();

    public RolapStarRegistry getRolapStarRegistry() {
        return rolapStarRegistry;
    }

    /**
     * Function table which contains all of the user-defined functions in this
     * schema, plus all of the standard functions.
     */
    static class RolapSchemaFunctionTable extends FunTableImpl {
        private final List<UdfResolver.UdfFactory> udfFactoryList;

        RolapSchemaFunctionTable(Collection<UdfResolver.UdfFactory> udfs) {
            udfFactoryList = new ArrayList<>(udfs);
        }

        @Override
		public void defineFunctions(Builder builder) {
            final FunTable globalFunTable = GlobalFunTable.instance();
            for (String reservedWord : globalFunTable.getReservedWords()) {
                builder.defineReserved(reservedWord);
            }
            for (Resolver resolver : globalFunTable.getResolvers()) {
                builder.define(resolver);
            }
            for (UdfResolver.UdfFactory udfFactory : udfFactoryList) {
                builder.define(new UdfResolver(udfFactory));
            }
        }
    }

    public RolapStar getStar(final String factTableName) {
        return getStar(RolapUtil.makeRolapStarKey(factTableName));
    }

    public RolapStar getStar(final List<String> starKey) {
      return getRolapStarRegistry().getStar(starKey);
  }

    public Collection<RolapStar> getStars() {
        return getRolapStarRegistry().getStars();
    }

    final RolapNativeRegistry nativeRegistry = new RolapNativeRegistry();

    RolapNativeRegistry getNativeRegistry() {
        return nativeRegistry;
    }

    /**
     * @return Returns the dataSourceChangeListener.
     */
    public DataSourceChangeListener getDataSourceChangeListener() {
        return dataSourceChangeListener;
    }

    /**
     * @param dataSourceChangeListener The dataSourceChangeListener to set.
     */
    public void setDataSourceChangeListener(
        DataSourceChangeListener dataSourceChangeListener)
    {
        this.dataSourceChangeListener = dataSourceChangeListener;
    }

    /**
     * Location of a node in an XML document.
     */
    private interface XmlLocation {
    }
}

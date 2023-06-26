/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.xmla.client.soapmessage;

public class Constants {

    private Constants() {
        // constructor
    }

    public static final String URN_SCHEMAS_MICROSOFT_COM_XML_ANALYSIS = "urn:schemas-microsoft-com:xml-analysis";
    public static final String SOAP_ACTION_DISCOVER = URN_SCHEMAS_MICROSOFT_COM_XML_ANALYSIS + ":Discover";
    public static final String SOAP_ACTION_EXECUTE = URN_SCHEMAS_MICROSOFT_COM_XML_ANALYSIS + ":Execute";
    public static final String PROPERTIES = "Properties";
    public static final String PROPERTY_LIST = "PropertyList";
    public static final String RESTRICTION_LIST = "RestrictionList";
    public static final String RESTRICTIONS = "Restrictions";
    public static final String DISCOVER = "Discover";
    public static final String REQUEST_TYPE = "RequestType";
    public static final String LOCALE_IDENTIFIER = "LocaleIdentifier";
    public static final String DATA_SOURCE_INFO = "DataSourceInfo";
    public static final String CONTENT = "Content";
    public static final String FORMAT = "Format";
    public static final String CATALOG = "Catalog";
    public static final String AXIS_FORMAT = "AxisFormat";

    public static final String RESTRICTIONS_CATALOG_NAME = "CATALOG_NAME";
    public static final String RESTRICTIONS_SCHEMA_NAME = "SCHEMA_NAME";
    public static final String RESTRICTIONS_CUBE_NAME = "CUBE_NAME";
    public static final String RESTRICTIONS_DIMENSION_UNIQUE_NAME = "DIMENSION_UNIQUE_NAME";
    public static final String RESTRICTIONS_HIERARCHY_NAME = "HIERARCHY_NAME";
    public static final String RESTRICTIONS_HIERARCHY_UNIQUE_NAME = "HIERARCHY_UNIQUE_NAME";
    public static final String RESTRICTIONS_HIERARCHY_ORIGIN = "HIERARCHY_ORIGIN";
    public static final String RESTRICTIONS_CUBE_SOURCE = "CUBE_SOURCE";
    public static final String RESTRICTIONS_HIERARCHY_VISIBILITY = "HIERARCHY_VISIBILITY";

    public static final String RESTRICTIONS_ORIGIN = "ORIGIN";
    public static final String RESTRICTIONS_INTERFACE_NAME = "INTERFACE_NAME";
    public static final String RESTRICTIONS_LIBRARY_NAME = "LIBRARY_NAME";

    public static final String RESTRICTIONS_DIMENSION_NAME = "DIMENSION_NAME";
    public static final String RESTRICTIONS_DIMENSION_VISIBILITY = "DIMENSION_VISIBILITY";

    public static final String RESTRICTIONS_BASE_CUBE_NAME = "BASE_CUBE_NAME";

    public static final String RESTRICTIONS_ACTION_NAME = "ACTION_NAME";
    public static final String RESTRICTIONS_ACTION_TYPE = "ACTION_TYPE";
    public static final String RESTRICTIONS_COORDINATE = "COORDINATE";
    public static final String RESTRICTIONS_COORDINATE_TYPE = "COORDINATE_TYPE";
    public static final String RESTRICTIONS_INVOCATION = "INVOCATION";

    public static final String RESTRICTIONS_TABLE_CATALOG = "TABLE_CATALOG";
    public static final String RESTRICTIONS_TABLE_SCHEMA = "RESTRICTIONS_TABLE_SCHEMA";
    public static final String RESTRICTIONS_TABLE_NAME = "TABLE_NAME";
    public static final String RESTRICTIONS_TABLE_TYPE = "TABLE_TYPE";

    public static final String RESTRICTIONS_ENUM_NAME = "EnumName";

    public static final String RESTRICTIONS_KEYWORD = "Keyword";

    public static final String RESTRICTIONS_LITERAL_NAME = "LiteralName";

    public static final String RESTRICTIONS_PROPERTY_NAME = "PropertyName";

    public static final String RESTRICTIONS_SCHEMA_OWNER = "SCHEMA_OWNER";

    public static final String RESTRICTIONS_MEASUREGROUP_NAME = "MEASUREGROUP_NAME";

    public static final String RESTRICTIONS_DATABASE_ID = "DatabaseID";
    public static final String RESTRICTIONS_DIMENSION_ID = "DimensionID";
    public static final String RESTRICTIONS_CUBE_ID = "CubeID";
    public static final String RESTRICTIONS_MEASURE_GROUP_ID = "MeasureGroupID";
    public static final String RESTRICTIONS_PARTITION_ID = "PartitionID";
    public static final String RESTRICTIONS_PERSPECTIVE_ID = "PerspectiveID";
    public static final String RESTRICTIONS_PERMISSION_ID = "DimensionPermissionID";
    public static final String RESTRICTIONS_ROLE_ID = "RoleID";
    public static final String RESTRICTIONS_DATABASE_PERMISSION_ID = "DatabasePermissionID";
    public static final String RESTRICTIONS_MINING_MODEL_ID = "MiningModelID";
    public static final String RESTRICTIONS_MINING_MODEL_PERMISSION_ID = "MiningModelPermissionID";
    public static final String RESTRICTIONS_DATA_SOURCE_ID = "DataSourceID";
    public static final String RESTRICTIONS_MINING_STRUCTURE_ID = "MiningStructureID";
    public static final String RESTRICTIONS_AGGREGATION_DESIGN_ID = "AggregationDesignID";
    public static final String RESTRICTIONS_TRACE_ID = "TraceID";
    public static final String RESTRICTIONS_MINING_STRUCTURE_PERMISSION_ID = "MiningStructurePermissionID";
    public static final String RESTRICTIONS_CUBE_PERMISSION_ID = "CubePermissionID";
    public static final String RESTRICTIONS_ASSEMBLY_ID = "AssemblyID";
    public static final String RESTRICTIONS_MDX_SCRIPT_ID = "MdxScriptID";
    public static final String RESTRICTIONS_DATA_SOURCE_VIEW_ID = "DataSourceViewID";
    public static final String RESTRICTIONS_DATA_SOURCE_PERMISSION_ID = "DataSourcePermissionID";
    public static final String RESTRICTIONS_OBJECT_EXPANSION = "ObjectExpansion";

    public static final String RESTRICTIONS_COLUMN_NAME = "COLUMN_NAME";
    public static final String RESTRICTIONS_COLUMN_OLAP_TYPE = "COLUMN_OLAP_TYPE";

    public static final String RESTRICTIONS_DATA_TYPE = "DATA_TYPE";
    public static final String RESTRICTIONS_BEST_MATCH = "BEST_MATCH";

    public static final String RESTRICTIONS_LEVEL_NAME = "LEVEL_NAME";
    public static final String RESTRICTIONS_LEVEL_UNIQUE_NAME = "LEVEL_UNIQUE_NAME";
    public static final String RESTRICTIONS_LEVEL_VISIBILITY = "LEVEL_VISIBILITY";

    public static final String RESTRICTIONS_MEASURE_NAME = "MEASURE_NAME";
    public static final String RESTRICTIONS_MEASURE_UNIQUE_NAME = "MEASURE_UNIQUE_NAME";
    public static final String RESTRICTIONS_MEASURE_VISIBILITY = "MEASURE_VISIBILITY";

    public static final String RESTRICTIONS_LEVEL_NUMBER = "LEVEL_NUMBER";
    public static final String RESTRICTIONS_MEMBER_NAME = "MEMBER_NAME";
    public static final String RESTRICTIONS_MEMBER_UNIQUE_NAME = "MEMBER_UNIQUE_NAME";
    public static final String RESTRICTIONS_MEMBER_TYPE = "MEMBER_TYPE";
    public static final String RESTRICTIONS_MEMBER_CAPTION = "MEMBER_CAPTION";
    public static final String RESTRICTIONS_TREE_OP = "TREE_OP";

    public static final String RESTRICTIONS_PROPERTY_TYPE = "PROPERTY_TYPE";
    public static final String RESTRICTIONS_PROPERTY_ORIGIN = "PROPERTY_ORIGIN";
    public static final String RESTRICTIONS_PROPERTY_VISIBILITY = "PROPERTY_VISIBILITY";

    public static final String RESTRICTIONS_SET_NAME = "SET_NAME";
    public static final String RESTRICTIONS_SCOPE = "SCOPE";

    public static final String RESTRICTIONS_KPI_NAME = "KPI_NAME";

    public static final String MDSCHEMA_FUNCTIONS = "MDSCHEMA_FUNCTIONS";
    public static final String MDSCHEMA_DIMENSIONS = "MDSCHEMA_DIMENSIONS";
    public static final String MDSCHEMA_CUBES = "MDSCHEMA_CUBES";
    public static final String MDSCHEMA_ACTIONS = "MDSCHEMA_ACTIONS";
    public static final String DBSCHEMA_TABLES = "DBSCHEMA_TABLES";
    public static final String DISCOVER_LITERALS = "DISCOVER_LITERALS";
    public static final String DISCOVER_KEYWORDS = "DISCOVER_KEYWORDS";
    public static final String DISCOVER_ENUMERATORS = "DISCOVER_ENUMERATORS";
    public static final String DISCOVER_SCHEMA_ROWSETS = "DISCOVER_SCHEMA_ROWSETS";
    public static final String DISCOVER_PROPERTIES = "DISCOVER_PROPERTIES";
    public static final String DBSCHEMA_CATALOGS = "DBSCHEMA_CATALOGS";
    public static final String DISCOVER_DATASOURCES = "DISCOVER_DATASOURCES";
    public static final String DISCOVER_XML_METADATA = "DISCOVER_XML_METADATA";
    public static final String DBSCHEMA_COLUMNS = "DBSCHEMA_COLUMNS";
    public static final String DBSCHEMA_PROVIDER_TYPES = "DBSCHEMA_PROVIDER_TYPES";
    public static final String DBSCHEMA_SCHEMATA = "DBSCHEMA_SCHEMATA";
    public static final String DBSCHEMA_SOURCE_TABLES = "DBSCHEMA_SOURCE_TABLES";
    public static final String DBSCHEMA_TABLES_INFO = "DBSCHEMA_TABLES_INFO";
    public static final String MDSCHEMA_HIERARCHIES = "MDSCHEMA_HIERARCHIES";
    public static final String MDSCHEMA_LEVELS = "MDSCHEMA_LEVELS";
    public static final String MDSCHEMA_MEASUREGROUP_DIMENSIONS = "MDSCHEMA_MEASUREGROUP_DIMENSIONS";
    public static final String MDSCHEMA_MEASURES = "MDSCHEMA_MEASURES";
    public static final String MDSCHEMA_MEMBERS = "MDSCHEMA_MEMBERS";
    public static final String MDSCHEMA_PROPERTIES = "MDSCHEMA_PROPERTIES";
    public static final String MDSCHEMA_SETS = "MDSCHEMA_SETS";
    public static final String MDSCHEMA_KPIS = "MDSCHEMA_KPIS";
    public static final String MDSCHEMA_MEASUREGROUPS = "MDSCHEMA_MEASUREGROUPS";

    public static final String VALUENS = "valuens";
    public static final String WRITE = "Write";
    public static final String VISUALIZATION_PROPERTIES = "VisualizationProperties";
    public static final String VISIBLE = "Visible";
    public static final String VALUE2 = "Value";
    public static final String TRANSLATION = "Translation";
    public static final String STORAGE_MODE = "StorageMode";
    public static final String STORAGE_LOCATION = "StorageLocation";
    public static final String STATE = "State";
    public static final String SERVER_ID = "ServerID";
    public static final String ROLE_ID = "RoleID";
    public static final String REFRESH_POLICY = "RefreshPolicy";
    public static final String REFRESH_INTERVAL = "RefreshInterval";
    public static final String READ_DEFINITION = "ReadDefinition";
    public static final String PROCESSING_STATE = "ProcessingState";
    public static final String PROCESSING_PRIORITY = "ProcessingPriority";
    public static final String PROCESSING_MODE = "ProcessingMode";
    public static final String PROCESS = "Process";
    public static final String PERSISTENCE = "Persistence";
    public static final String ORDINAL = "Ordinal";
    public static final String MINING_MODEL_ID = "MiningModelID";
    public static final String MEASURES = "Measures";
    public static final String MEASURE_ID = "MeasureID";
    public static final String MEASURE = "Measure";
    public static final String LAST_SCHEMA_UPDATE = "LastSchemaUpdate";
    public static final String LAST_PROCESSED = "LastProcessed";
    public static final String LANGUAGE = "Language";
    public static final String KEY_COLUMNS = "KeyColumns";
    public static final String KEY_COLUMN = "KeyColumn";
    public static final String HIERARCHY = "Hierarchy";
    public static final String HIERARCHIES = "Hierarchies";
    public static final String FILTER = "Filter";
    public static final String EXPRESSION = "Expression";
    public static final String ESTIMATED_SIZE = "EstimatedSize";
    public static final String ESTIMATED_ROWS = "EstimatedRows";
    public static final String DISPLAY_FOLDER = "DisplayFolder";
    public static final String DIMENSIONS = "Dimensions";
    public static final String DIMENSION_ID = "DimensionID";
    public static final String DEFAULT_MEMBER = "DefaultMember";
    public static final String DATA_SOURCE_VIEW_ID = "DataSourceViewID";
    public static final String DATA_SOURCE_ID = "DataSourceID";
    public static final String CUBE_ID = "CubeID";
    public static final String CUBE_DIMENSION_ID = "CubeDimensionID";
    public static final String CREATED_TIMESTAMP = "CreatedTimestamp";
    public static final String COMMAND = "Command";
    public static final String COLUMNS = "Columns";
    public static final String COLUMN_ID = "ColumnID";
    public static final String COLLATION = "Collation";
    public static final String CAPTION = "Caption";
    public static final String ATTRIBUTES = "Attributes";
    public static final String ATTRIBUTE_ID = "AttributeID";
    public static final String ATTRIBUTE_HIERARCHY_VISIBLE = "AttributeHierarchyVisible";
    public static final String ATTRIBUTE = "Attribute";
    public static final String ALLOW_DRILL_THROUGH = "AllowDrillThrough";
    public static final String AGGREGATION_PREFIX = "AggregationPrefix";
    public static final String AGGREGATION_DESIGN_ID = "AggregationDesignID";
    public static final String ACCOUNT = "Account";
    public static final String SOURCE = "Source";
    public static final String TRANSLATIONS = "Translations";
    public static final String MEASURE_GROUP_ID = "MeasureGroupID";
    public static final String VALUE = "value";
    public static final String DESCRIPTION = "Description";
    public static final String TABLE_ID = "TableID";
    public static final String DIMENSION = "Dimension";
    public static final String EXECUTE = "Execute";



    public static final String MESSAGES = "Messages";
    public static final String EXCEPTION = "Exception";
    public static final String ROW = "row";
    public static final String CATALOG_NAME = "CATALOG_NAME";
    public static final String SCHEMA_NAME = "SCHEMA_NAME";
    public static final String CUBE_NAME = "CUBE_NAME";
    public static final String DIMENSION_UNIQUE_NAME = "DIMENSION_UNIQUE_NAME";
    public static final String HIERARCHY_NAME = "HIERARCHY_NAME";
    public static final String HIERARCHY_UNIQUE_NAME = "HIERARCHY_UNIQUE_NAME";
    public static final String HIERARCHY_GUID = "HIERARCHY_GUID";
    public static final String HIERARCHY_CAPTION = "HIERARCHY_CAPTION";
    public static final String DIMENSION_TYPE = "DIMENSION_TYPE";
    public static final String HIERARCHY_CARDINALITY = "HIERARCHY_CARDINALITY";
    public static final String DEFAULT_MEMBER_UC = "DEFAULT_MEMBER";
    public static final String ALL_MEMBER = "ALL_MEMBER";
    public static final String DESCRIPTION_UC = "DESCRIPTION";
    public static final String STRUCTURE = "STRUCTURE";
    public static final String IS_VIRTUAL = "IS_VIRTUAL";
    public static final String IS_READWRITE = "IS_READWRITE";
    public static final String DIMENSION_UNIQUE_SETTINGS = "DIMENSION_UNIQUE_SETTINGS";
    public static final String DIMENSION_MASTER_UNIQUE_NAME = "DIMENSION_MASTER_UNIQUE_NAME";
    public static final String DIMENSION_IS_VISIBLE = "DIMENSION_IS_VISIBLE";
    public static final String HIERARCHY_ORDINAL = "HIERARCHY_ORDINAL";
    public static final String DIMENSION_IS_SHARED = "DIMENSION_IS_SHARED";
    public static final String IERARCHY_IS_VISIBLE = "IERARCHY_IS_VISIBLE";
    public static final String HIERARCHY_ORIGIN = "HIERARCHY_ORIGIN";
    public static final String HIERARCHY_DISPLAY_FOLDER = "HIERARCHY_DISPLAY_FOLDER";
    public static final String INSTANCE_SELECTION = "INSTANCE_SELECTION";
    public static final String GROUPING_BEHAVIOR = "GROUPING_BEHAVIOR";
    public static final String STRUCTURE_TYPE = "STRUCTURE_TYPE";
    public static final String FUNCTION_NAME = "FUNCTION_NAME";
    public static final String PARAMETER_LIST = "PARAMETER_LIST";
    public static final String RETURN_TYPE = "RETURN_TYPE";
    public static final String ORIGIN = "ORIGIN";
    public static final String INTERFACE_NAME = "INTERFACE_NAME";
    public static final String LIBRARY_NAME = "LIBRARY_NAME";
    public static final String DLL_NAME = "DLL_NAME";
    public static final String HELP_FILE = "HELP_FILE";
    public static final String HELP_CONTEXT = "HELP_CONTEXT";
    public static final String OBJECT = "OBJECT";
    public static final String CAPTION_UC = "CAPTION";
    public static final String DIRECTQUERY_PUSHABLE = "DIRECTQUERY_PUSHABLE";
    public static final String PARAMETERINFO = "PARAMETERINFO";
    public static final String NAME = "NAME";
    public static final String OPTIONAL = "OPTIONAL";
    public static final String REPEATABLE = "REPEATABLE";
    public static final String REPEATGROUP = "REPEATGROUP";
    public static final String DIMENSION_NAME = "DIMENSION_NAME";
    public static final String DIMENSION_GUID = "DIMENSION_GUID";
    public static final String DIMENSION_CAPTION = "DIMENSION_CAPTION";
    public static final String DIMENSION_ORDINAL = "DIMENSION_ORDINAL";
    public static final String DIMENSION_CARDINALITY = "DIMENSION_CARDINALITY";
    public static final String DEFAULT_HIERARCHY = "DEFAULT_HIERARCHY";
    public static final String DIMENSION_MASTER_NAME = "DIMENSION_MASTER_NAME";
    public static final String ACTION_NAME = "ACTION_NAME";
    public static final String ACTION_TYPE = "ACTION_TYPE";
    public static final String COORDINATE = "COORDINATE";
    public static final String COORDINATE_TYPE = "COORDINATE_TYPE";
    public static final String ACTION_CAPTION = "ACTION_CAPTION";
    public static final String CONTENT_UC = "CONTENT";
    public static final String APPLICATION = "APPLICATION";
    public static final String INVOCATION = "INVOCATION";
    public static final String CUBE_TYPE = "CUBE_TYPE";
    public static final String CUBE_GUID = "CUBE_GUID";
    public static final String CREATED_ON = "CREATED_ON";
    public static final String LAST_SCHEMA_UPDATE_UC = "LAST_SCHEMA_UPDATE";
    public static final String SCHEMA_UPDATED_BY = "SCHEMA_UPDATED_BY";
    public static final String LAST_DATA_UPDATE = "LAST_DATA_UPDATE";
    public static final String DATA_UPDATED_BY = "DATA_UPDATED_BY";
    public static final String IS_DRILLTHROUGH_ENABLED = "IS_DRILLTHROUGH_ENABLED";
    public static final String IS_LINKABLE = "IS_LINKABLE";
    public static final String IS_WRITE_ENABLED = "IS_WRITE_ENABLED";
    public static final String IS_SQL_ENABLED = "IS_SQL_ENABLED";
    public static final String CUBE_CAPTION = "CUBE_CAPTION";
    public static final String BASE_CUBE_NAME = "BASE_CUBE_NAME";
    public static final String CUBE_SOURCE = "CUBE_SOURCE";
    public static final String PREFERRED_QUERY_PATTERNS = "PREFERRED_QUERY_PATTERNS";
    public static final String ROLES = "ROLES";
    public static final String DATE_MODIFIED = "DATE_MODIFIED";
    public static final String COMPATIBILITY_LEVEL = "COMPATIBILITY_LEVEL";
    public static final String TYPE = "TYPE";
    public static final String VERSION = "VERSION";
    public static final String DATABASE_ID = "DATABASE_ID";
    public static final String DATE_QUERIED = "DATE_QUERIED";
    public static final String CURRENTLY_USED = "CURRENTLY_USED";
    public static final String POPULARITY = "POPULARITY";
    public static final String WEIGHTEDPOPULARITY = "WEIGHTEDPOPULARITY";
    public static final String CLIENTCACHEREFRESHPOLICY = "CLIENTCACHEREFRESHPOLICY";
    public static final String TABLE_CATALOG = "TABLE_CATALOG";
    public static final String TABLE_SCHEMA = "TABLE_SCHEMA";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String TABLE_TYPE = "TABLE_TYPE";
    public static final String TABLE_GUID = "TABLE_GUID";
    public static final String TABLE_PROP_ID = "TABLE_PROP_ID";
    public static final String DATE_CREATED = "DATE_CREATED";
    public static final String KEYWORD = "Keyword";
    public static final String SCHEMA_OWNER = "SCHEMA_OWNER";
    public static final String SET_NAME = "SET_NAME";
    public static final String SCOPE = "SCOPE";
    public static final String BOOKMARKS = "BOOKMARKS";
    public static final String BOOKMARK_TYPE = "BOOKMARK_TYPE";
    public static final String BOOKMARK_DATA_TYPE = "BOOKMARK_DATA_TYPE";
    public static final String BOOKMARK_MAXIMUM_LENGTH = "BOOKMARK_MAXIMUM_LENGTH";
    public static final String BOOKMARK_INFORMATION = "BOOKMARK_INFORMATION";
    public static final String TABLE_VERSION = "TABLE_VERSION";
    public static final String CARDINALITY = "CARDINALITY";
    public static final String MEASUREGROUP_NAME = "MEASUREGROUP_NAME";
    public static final String KPI_NAME = "KPI_NAME";
    public static final String KPI_CAPTION = "KPI_CAPTION";
    public static final String KPI_DESCRIPTION = "KPI_DESCRIPTION";
    public static final String KPI_DISPLAY_FOLDER = "KPI_DISPLAY_FOLDER";
    public static final String KPI_VALUE = "KPI_VALUE";
    public static final String KPI_GOAL = "KPI_GOAL";
    public static final String KPI_STATUS = "KPI_STATUS";
    public static final String KPI_TREND = "KPI_TREND";
    public static final String KPI_STATUS_GRAPHIC = "KPI_STATUS_GRAPHIC";
    public static final String KPI_TREND_GRAPHIC = "KPI_TREND_GRAPHIC";
    public static final String KPI_WEIGHT = "KPI_WEIGHT";
    public static final String KPI_CURRENT_TIME_MEMBER = "KPI_CURRENT_TIME_MEMBER";
    public static final String KPI_PARENT_KPI_NAME = "KPI_PARENT_KPI_NAME";
    public static final String ANNOTATIONS = "ANNOTATIONS";
    public static final String MEASUREGROUP_CAPTION = "MEASUREGROUP_CAPTION";
    public static final String EXPRESSION_UC = "EXPRESSION";
    public static final String DIMENSIONS_UC = "DIMENSIONS";
    public static final String SET_CAPTION = "SET_CAPTION";
    public static final String SET_DISPLAY_FOLDER = "SET_DISPLAY_FOLDER";
    public static final String SET_EVALUATION_CONTEXT = "SET_EVALUATION_CONTEXT";
    public static final String LEVEL_UNIQUE_NAME = "LEVEL_UNIQUE_NAME";
    public static final String MEMBER_UNIQUE_NAME = "MEMBER_UNIQUE_NAME";
    public static final String PROPERTY_TYPE = "PROPERTY_TYPE";
    public static final String PROPERTY_NAME = "PROPERTY_NAME";
    public static final String PROPERTY_CAPTION = "PROPERTY_CAPTION";
    public static final String DATA_TYPE = "DATA_TYPE";
    public static final String CHARACTER_MAXIMUM_LENGTH = "CHARACTER_MAXIMUM_LENGTH";
    public static final String CHARACTER_OCTET_LENGTH = "CHARACTER_OCTET_LENGTH";
    public static final String NUMERIC_PRECISION = "NUMERIC_PRECISION";
    public static final String NUMERIC_SCALE = "NUMERIC_SCALE";
    public static final String PROPERTY_CONTENT_TYPE = "PROPERTY_CONTENT_TYPE";
    public static final String SQL_COLUMN_NAME = "SQL_COLUMN_NAME";
    public static final String LANGUAGE_UC = "LANGUAGE";
    public static final String ROPERTY_ORIGIN = "ROPERTY_ORIGIN";
    public static final String PROPERTY_ATTRIBUTE_HIERARCHY_NAME = "PROPERTY_ATTRIBUTE_HIERARCHY_NAME";
    public static final String PROPERTY_CARDINALITY = "PROPERTY_CARDINALITY";
    public static final String MIME_TYPE = "MIME_TYPE";
    public static final String PROPERTY_IS_VISIBLE = "PROPERTY_IS_VISIBLE";
    public static final String LEVEL_NUMBER = "LEVEL_NUMBER";
    public static final String MEMBER_ORDINAL = "MEMBER_ORDINAL";
    public static final String MEMBER_NAME = "MEMBER_NAME";
    public static final String MEMBER_TYPE = "MEMBER_TYPE";
    public static final String MEMBER_GUID = "MEMBER_GUID";
    public static final String MEMBER_CAPTION = "MEMBER_CAPTION";
    public static final String CHILDREN_CARDINALITY = "CHILDREN_CARDINALITY";
    public static final String PARENT_LEVEL = "PARENT_LEVEL";
    public static final String PARENT_UNIQUE_NAME = "PARENT_UNIQUE_NAME";
    public static final String PARENT_COUN = "PARENT_COUN";
    public static final String MEMBER_KEY = "MEMBER_KEY";
    public static final String IS_PLACEHOLDERMEMBER = "IS_PLACEHOLDERMEMBER";
    public static final String IS_DATAMEMBER = "IS_DATAMEMBER";
    public static final String MEASURE_NAME = "MEASURE_NAME";
    public static final String MEASURE_UNIQUE_NAME = "MEASURE_UNIQUE_NAME";
    public static final String MEASURE_CAPTION = "MEASURE_CAPTION";
    public static final String MEASURE_GUID = "MEASURE_GUID";
    public static final String MEASURE_AGGREGATOR = "MEASURE_AGGREGATOR";
    public static final String MEASURE_UNITS = "MEASURE_UNITS";
    public static final String MEASURE_IS_VISIBLE = "MEASURE_IS_VISIBLE";
    public static final String LEVELS_LIST = "LEVELS_LIST";
    public static final String MEASURE_NAME_SQL_COLUMN_NAME = "MEASURE_NAME_SQL_COLUMN_NAME";
    public static final String MEASURE_UNQUALIFIED_CAPTION = "MEASURE_UNQUALIFIED_CAPTION";
    public static final String DEFAULT_FORMAT_STRING = "DEFAULT_FORMAT_STRING";
    public static final String MEASUREGROUP_CARDINALITY = "MEASUREGROUP_CARDINALITY";
    public static final String DIMENSION_IS_FACT_DIMENSION = "DIMENSION_IS_FACT_DIMENSION";
    public static final String DIMENSION_GRANULARITY = "DIMENSION_GRANULARITY";
    public static final String MEASURE_GROUP_DIMENSION = "MeasureGroupDimension";
    public static final String LEVEL_NAME = "LEVEL_NAME";
    public static final String LEVEL_GUID = "LEVEL_GUID";
    public static final String LEVEL_CAPTION = "LEVEL_CAPTION";
    public static final String LEVEL_CARDINALITY = "LEVEL_CARDINALITY";
    public static final String LEVEL_TYPE = "LEVEL_TYPE";
    public static final String CUSTOM_ROLLUP_SETTINGS = "CUSTOM_ROLLUP_SETTINGS";
    public static final String LEVEL_UNIQUE_SETTINGS = "LEVEL_UNIQUE_SETTINGS";
    public static final String LEVEL_IS_VISIBLE = "LEVEL_IS_VISIBLE";
    public static final String LEVEL_ORDERING_PROPERTY = "LEVEL_ORDERING_PROPERTY";
    public static final String LEVEL_DBTYPE = "LEVEL_DBTYPE";
    public static final String LEVEL_MASTER_UNIQUE_NAME = "LEVEL_MASTER_UNIQUE_NAME";
    public static final String LEVEL_NAME_SQL_COLUMN_NAME = "LEVEL_NAME_SQL_COLUMN_NAME";
    public static final String LEVEL_KEY_SQL_COLUMN_NAME = "LEVEL_KEY_SQL_COLUMN_NAME";
    public static final String LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME = "LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME";
    public static final String LEVEL_ATTRIBUTE_HIERARCHY_NAME = "LEVEL_ATTRIBUTE_HIERARCHY_NAME";
    public static final String LEVEL_KEY_CARDINALITY = "LEVEL_KEY_CARDINALITY";
    public static final String LEVEL_ORIGIN = "LEVEL_ORIGIN";
    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String COLUMN_SIZE = "COLUMN_SIZE";
    public static final String LITERAL_PREFIX = "LITERAL_PREFIX";
    public static final String LITERAL_SUFFIX = "LITERAL_SUFFIX";
    public static final String CREATE_PARAMS = "CREATE_PARAMS";
    public static final String IS_NULLABLE = "IS_NULLABLE";
    public static final String CASE_SENSITIVE = "CASE_SENSITIVE";
    public static final String SEARCHABLE = "SEARCHABLE";
    public static final String UNSIGNED_ATTRIBUTE = "UNSIGNED_ATTRIBUTE";
    public static final String FIXED_PREC_SCALE = "FIXED_PREC_SCALE";
    public static final String AUTO_UNIQUE_VALUE = "AUTO_UNIQUE_VALUE";
    public static final String LOCAL_TYPE_NAME = "LOCAL_TYPE_NAME";
    public static final String MINIMUM_SCALE = "MINIMUM_SCALE";
    public static final String MAXIMUM_SCALE = "MAXIMUM_SCALE";
    public static final String GUID = "GUID";
    public static final String TYPE_LIB = "TYPE_LIB";
    public static final String IS_LONG = "IS_LONG";
    public static final String BEST_MATCH = "BEST_MATCH";
    public static final String IS_FIXEDLENGTH = "IS_FIXEDLENGTH";
    public static final String DIMENSION_PATH = "DIMENSION_PATH";
    public static final String TYPE_GUID = "TYPE_GUID";
    public static final String COLUMN_NAME = "COLUMN_NAME";
    public static final String COLUMN_GUID = "COLUMN_GUID";
    public static final String COLUMN_PROPID = "COLUMN_PROPID";
    public static final String ORDINAL_POSITION = "ORDINAL_POSITION";
    public static final String COLUMN_HAS_DEFAULT = "COLUMN_HAS_DEFAULT";
    public static final String COLUMN_DEFAULT = "COLUMN_DEFAULT";
    public static final String COLUMN_FLAG = "COLUMN_FLAG";
    public static final String DATETIME_PRECISION = "DATETIME_PRECISION";
    public static final String CHARACTER_SET_CATALOG = "CHARACTER_SET_CATALOG";
    public static final String CHARACTER_SET_SCHEMA = "CHARACTER_SET_SCHEMA";
    public static final String CHARACTER_SET_NAME = "CHARACTER_SET_NAME";
    public static final String COLLATION_CATALOG = "COLLATION_CATALOG";
    public static final String COLLATION_SCHEMA = "COLLATION_SCHEMA";
    public static final String COLLATION_NAME = "COLLATION_NAME";
    public static final String DOMAIN_CATALOG = "DOMAIN_CATALOG";
    public static final String DOMAIN_SCHEMA = "DOMAIN_SCHEMA";
    public static final String DOMAIN_NAME = "DOMAIN_NAME";
    public static final String COLUMN_OLAP_TYPE = "COLUMN_OLAP_TYPE";
    public static final String ENUM_NAME = "EnumName";
    public static final String ENUM_DESCRIPTION = "EnumDescription";
    public static final String ENUM_TYPE = "EnumType";
    public static final String ELEMENT_NAME = "ElementName";
    public static final String ELEMENT_DESCRIPTION = "ElementDescription";
    public static final String ELEMENT_VALUE = "ElementValue";
    public static final String PROPERTY_NAME1 = "PropertyName";
    public static final String SCHEMA_NAME1 = "SchemaName";
    public static final String SCHEMA_GUID = "SchemaGuid";
    public static final String DESCRIPTION1 = "Description";
    public static final String RESTRICTIONS_MASK = "RestrictionsMask";
    public static final String LITERAL_NAME = "LiteralName";
    public static final String LITERAL_VALUE = "LiteralValue";
    public static final String LITERAL_INVALID_CHARS = "LiteralInvalidChars";
    public static final String LITERAL_INVALID_STARTING_CHARS = "LiteralInvalidStartingChars";
    public static final String LITERAL_MAX_LENGTH = "LiteralMaxLength";
    public static final String LITERAL_NAME_VALUE = "LiteralNameValue";

}

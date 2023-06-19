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
}

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

import jakarta.xml.soap.SOAPBody;
import org.eclipse.daanse.xmla.api.common.enums.ActionTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.AuthenticationModeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ClientCacheRefreshPolicyEnum;
import org.eclipse.daanse.xmla.api.common.enums.ColumnFlagsEnum;
import org.eclipse.daanse.xmla.api.common.enums.ColumnOlapTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.CoordinateTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.CubeSourceEnum;
import org.eclipse.daanse.xmla.api.common.enums.CubeTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.CustomRollupSettingEnum;
import org.eclipse.daanse.xmla.api.common.enums.DimensionCardinalityEnum;
import org.eclipse.daanse.xmla.api.common.enums.DimensionTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.DimensionUniqueSettingEnum;
import org.eclipse.daanse.xmla.api.common.enums.DirectQueryPushableEnum;
import org.eclipse.daanse.xmla.api.common.enums.GroupingBehaviorEnum;
import org.eclipse.daanse.xmla.api.common.enums.HierarchyOriginEnum;
import org.eclipse.daanse.xmla.api.common.enums.InstanceSelectionEnum;
import org.eclipse.daanse.xmla.api.common.enums.InterfaceNameEnum;
import org.eclipse.daanse.xmla.api.common.enums.InvocationEnum;
import org.eclipse.daanse.xmla.api.common.enums.LevelDbTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.LevelOriginEnum;
import org.eclipse.daanse.xmla.api.common.enums.LevelTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.LevelUniqueSettingsEnum;
import org.eclipse.daanse.xmla.api.common.enums.LiteralNameEnumValueEnum;
import org.eclipse.daanse.xmla.api.common.enums.MeasureAggregatorEnum;
import org.eclipse.daanse.xmla.api.common.enums.MemberTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.OriginEnum;
import org.eclipse.daanse.xmla.api.common.enums.PreferredQueryPatternsEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyCardinalityEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyContentTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyOriginEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ProviderTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ScopeEnum;
import org.eclipse.daanse.xmla.api.common.enums.SearchableEnum;
import org.eclipse.daanse.xmla.api.common.enums.SetEvaluationContextEnum;
import org.eclipse.daanse.xmla.api.common.enums.StructureEnum;
import org.eclipse.daanse.xmla.api.common.enums.StructureTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TableTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TypeEnum;
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.enumerators.DiscoverEnumeratorsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.keywords.DiscoverKeywordsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.literals.DiscoverLiteralsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.properties.DiscoverPropertiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.xmlmetadata.DiscoverXmlMetaDataResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.actions.MdSchemaActionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.cubes.MdSchemaCubesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.demensions.MdSchemaDimensionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.ParameterInfo;
import org.eclipse.daanse.xmla.api.discover.mdschema.hierarchies.MdSchemaHierarchiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.levels.MdSchemaLevelsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MeasureGroupDimension;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroups.MdSchemaMeasureGroupsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measures.MdSchemaMeasuresResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.members.MdSchemaMembersResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.properties.MdSchemaPropertiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.sets.MdSchemaSetsResponseRow;
import org.eclipse.daanse.xmla.model.record.discover.MeasureGroupDimensionR;
import org.eclipse.daanse.xmla.model.record.discover.ParameterInfoR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.catalogs.DbSchemaCatalogsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.columns.DbSchemaColumnsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.providertypes.DbSchemaProviderTypesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.schemata.DbSchemaSchemataResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.sourcetables.DbSchemaSourceTablesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.tables.DbSchemaTablesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.tablesinfo.DbSchemaTablesInfoResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.discover.datasources.DiscoverDataSourcesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.discover.enumerators.DiscoverEnumeratorsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.discover.keywords.DiscoverKeywordsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.discover.literals.DiscoverLiteralsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.discover.properties.DiscoverPropertiesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.discover.schemarowsets.DiscoverSchemaRowsetsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.discover.xmlmetadata.DiscoverXmlMetaDataResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.actions.MdSchemaActionsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.cubes.MdSchemaCubesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.demensions.MdSchemaDimensionsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.functions.MdSchemaFunctionsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.hierarchies.MdSchemaHierarchiesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.kpis.MdSchemaKpisResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.levels.MdSchemaLevelsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measuregroups.MdSchemaMeasureGroupsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measures.MdSchemaMeasuresResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.members.MdSchemaMembersResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.properties.MdSchemaPropertiesResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.sets.MdSchemaSetsResponseRowR;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class Convertor {

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
    public static final String DEFAULT_MEMBER = "DEFAULT_MEMBER";
    public static final String ALL_MEMBER = "ALL_MEMBER";
    public static final String DESCRIPTION = "DESCRIPTION";
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
    public static final String CAPTION = "CAPTION";
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
    public static final String CONTENT = "CONTENT";
    public static final String APPLICATION = "APPLICATION";
    public static final String INVOCATION = "INVOCATION";
    public static final String CUBE_TYPE = "CUBE_TYPE";
    public static final String CUBE_GUID = "CUBE_GUID";
    public static final String CREATED_ON = "CREATED_ON";
    public static final String LAST_SCHEMA_UPDATE = "LAST_SCHEMA_UPDATE";
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

    private Convertor() {
        //constructor
    }

    public static List<DbSchemaTablesInfoResponseRow> convertToDbSchemaTablesInfoResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaTablesInfoResponseRowR(
                Optional.ofNullable(m.get(TABLE_CATALOG)),
                Optional.ofNullable(m.get(TABLE_SCHEMA)),
                m.get(TABLE_NAME),
                m.get(TABLE_TYPE),
                Optional.ofNullable(getInt(m.get(TABLE_GUID))),
                Optional.ofNullable(getBoolean(m.get("BOOKMARKS"))),
                Optional.ofNullable(getInt(m.get("BOOKMARK_TYPE"))),
                Optional.ofNullable(getInt(m.get("BOOKMARK_DATA_TYPE"))),
                Optional.ofNullable(getInt(m.get("BOOKMARK_MAXIMUM_LENGTH"))),
                Optional.ofNullable(getInt(m.get("BOOKMARK_INFORMATION"))),
                Optional.ofNullable(getLong(m.get("TABLE_VERSION"))),
                Optional.ofNullable(getLong(m.get("CARDINALITY"))),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(getInt(m.get(TABLE_PROP_ID)))
            )
        ).collect(toList());
    }

    public static List<DbSchemaSourceTablesResponseRow> convertToDbSchemaSourceTablesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaSourceTablesResponseRowR(
                Optional.ofNullable(m.get("TABLE_CATALOG")),
                Optional.ofNullable(m.get("TABLE_SCHEMA")),
                m.get("TABLE_NAME"),
                TableTypeEnum.fromValue(m.get("TABLE_TYPE"))
            )
        ).collect(toList());
    }

    public static List<MdSchemaKpisResponseRow> convertToMdSchemaKpisResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaKpisResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get("MEASUREGROUP_NAME")),
                Optional.ofNullable(m.get("KPI_NAME")),
                Optional.ofNullable(m.get("KPI_CAPTION")),
                Optional.ofNullable(m.get("KPI_DESCRIPTION")),
                Optional.ofNullable(m.get("KPI_DISPLAY_FOLDER")),
                Optional.ofNullable(m.get("KPI_VALUE")),
                Optional.ofNullable(m.get("KPI_GOAL")),
                Optional.ofNullable(m.get("KPI_STATUS")),
                Optional.ofNullable(m.get("KPI_TREND")),
                Optional.ofNullable(m.get("KPI_STATUS_GRAPHIC")),
                Optional.ofNullable(m.get("KPI_TREND_GRAPHIC")),
                Optional.ofNullable(m.get("KPI_WEIGHT")),
                Optional.ofNullable(m.get("KPI_CURRENT_TIME_MEMBER")),
                Optional.ofNullable(m.get("KPI_PARENT_KPI_NAME")),
                Optional.ofNullable(m.get("ANNOTATIONS")),
                Optional.ofNullable(ScopeEnum.fromValue(m.get("SCOPE")))
            )
        ).collect(toList());
    }

    public static List<MdSchemaMeasureGroupsResponseRow> convertToMdSchemaMeasureGroupsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaMeasureGroupsResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get("MEASUREGROUP_NAME")),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(getBoolean(m.get("isWriteEnabled"))),
                Optional.ofNullable(m.get("MEASUREGROUP_CAPTION"))
            )
        ).collect(toList());
    }

    public static List<MdSchemaSetsResponseRow> convertToMdSchemaSetsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaSetsResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get(SET_NAME)),
                Optional.ofNullable(ScopeEnum.fromValue(m.get(SCOPE))),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(m.get("EXPRESSION")),
                Optional.ofNullable(m.get("DIMENSIONS")),
                Optional.ofNullable(m.get("SET_CAPTION")),
                Optional.ofNullable(m.get("SET_DISPLAY_FOLDER")),
                Optional.ofNullable(SetEvaluationContextEnum.fromValue(m.get("SET_EVALUATION_CONTEXT")))
            )
        ).collect(toList());
    }

    public static List<MdSchemaPropertiesResponseRow> convertToMdSchemaPropertiesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaPropertiesResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get("DIMENSION_UNIQUE_NAME")),
                Optional.ofNullable(m.get("HIERARCHY_UNIQUE_NAME")),
                Optional.ofNullable(m.get("LEVEL_UNIQUE_NAME")),
                Optional.ofNullable(m.get("MEMBER_UNIQUE_NAME")),
                Optional.ofNullable(PropertyTypeEnum.fromValue(m.get("PROPERTY_TYPE"))),
                Optional.ofNullable(m.get("PROPERTY_NAME")),
                Optional.ofNullable(m.get("PROPERTY_CAPTION")),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get("DATA_TYPE"))),
                Optional.ofNullable(getInt(m.get("CHARACTER_MAXIMUM_LENGTH"))),
                Optional.ofNullable(getInt(m.get("CHARACTER_OCTET_LENGTH"))),
                Optional.ofNullable(getInt(m.get("NUMERIC_PRECISION"))),
                Optional.ofNullable(getInt(m.get("NUMERIC_SCALE"))),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(PropertyContentTypeEnum.fromValue(m.get("PROPERTY_CONTENT_TYPE"))),
                Optional.ofNullable(m.get("SQL_COLUMN_NAME")),
                Optional.ofNullable(getInt(m.get("LANGUAGE"))),
                Optional.ofNullable(PropertyOriginEnum.fromValue(m.get("ROPERTY_ORIGIN"))),
                Optional.ofNullable(m.get("PROPERTY_ATTRIBUTE_HIERARCHY_NAME")),
                Optional.ofNullable(PropertyCardinalityEnum.fromValue(m.get("PROPERTY_CARDINALITY"))),
                Optional.ofNullable(m.get("MIME_TYPE")),
                Optional.ofNullable(getBoolean(m.get("PROPERTY_IS_VISIBLE")))
            )
        ).collect(toList());
    }

    public static List<MdSchemaMembersResponseRow> convertToMdSchemaMembersResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaMembersResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get("DIMENSION_UNIQUE_NAME")),
                Optional.ofNullable(m.get("HIERARCHY_UNIQUE_NAME")),
                Optional.ofNullable(m.get("LEVEL_UNIQUE_NAME")),
                Optional.ofNullable(getInt(m.get("LEVEL_NUMBER"))),
                Optional.ofNullable(getInt(m.get("MEMBER_ORDINAL"))),
                Optional.ofNullable(m.get("MEMBER_NAME")),
                Optional.ofNullable(m.get("MEMBER_UNIQUE_NAME")),
                Optional.ofNullable(MemberTypeEnum.fromValue(m.get("MEMBER_TYPE"))),
                Optional.ofNullable(getInt(m.get("MEMBER_GUID"))),
                Optional.ofNullable(m.get("MEMBER_CAPTION")),
                Optional.ofNullable(getInt(m.get("CHILDREN_CARDINALITY"))),
                Optional.ofNullable(getInt(m.get("PARENT_LEVEL"))),
                Optional.ofNullable(m.get("PARENT_UNIQUE_NAME")),
                Optional.ofNullable(getInt(m.get("PARENT_COUN"))),
                Optional.ofNullable(m.get("DESCRIPTION")),
                Optional.ofNullable(m.get("EXPRESSION")),
                Optional.ofNullable(m.get("MEMBER_KEY")),
                Optional.ofNullable(getBoolean(m.get("IS_PLACEHOLDERMEMBER"))),
                Optional.ofNullable(getBoolean(m.get("IS_DATAMEMBER"))),
                Optional.ofNullable(ScopeEnum.fromValue(m.get("DESCRIPTION")))
            )
        ).collect(toList());
    }

    public static List<MdSchemaMeasuresResponseRow> convertToMdSchemaMeasuresResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaMeasuresResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get("MEASURE_NAME")),
                Optional.ofNullable(m.get("MEASURE_UNIQUE_NAME")),
                Optional.ofNullable(m.get("MEASURE_CAPTION")),
                Optional.ofNullable(getInt(m.get("MEASURE_GUID"))),
                Optional.ofNullable(MeasureAggregatorEnum.fromValue(m.get("MEASURE_AGGREGATOR"))),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get("DATA_TYPE"))),
                Optional.ofNullable(getInt(m.get("NUMERIC_PRECISION"))),
                Optional.ofNullable(getInt(m.get("NUMERIC_SCALE"))),
                Optional.ofNullable(m.get("MEASURE_UNITS")),
                Optional.ofNullable(m.get("DESCRIPTION")),
                Optional.ofNullable(m.get("EXPRESSION")),
                Optional.ofNullable(getBoolean(m.get("MEASURE_IS_VISIBLE"))),
                Optional.ofNullable(m.get("LEVELS_LIST")),
                Optional.ofNullable(m.get("MEASURE_NAME_SQL_COLUMN_NAME")),
                Optional.ofNullable(m.get("MEASURE_UNQUALIFIED_CAPTION")),
                Optional.ofNullable(m.get("MEASUREGROUP_NAME")),
                Optional.ofNullable(m.get("DEFAULT_FORMAT_STRING"))
            )
        ).collect(toList());
    }

    public static List<MdSchemaMeasureGroupDimensionsResponseRow> convertToMdSchemaMeasureGroupDimensionsResponseRow(
        SOAPBody b
    ) {
        List<MdSchemaMeasureGroupDimensionsResponseRow> result = new ArrayList<>();
        NodeList nodeList = b.getElementsByTagName(ROW);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList nl = nodeList.item(i).getChildNodes();
                Map<String, String> m = getMapValues(nl);
                result.add(new MdSchemaMeasureGroupDimensionsResponseRowR(
                    Optional.ofNullable(m.get(CATALOG_NAME)),
                    Optional.ofNullable(m.get(SCHEMA_NAME)),
                    Optional.ofNullable(m.get(CUBE_NAME)),
                    Optional.ofNullable(m.get("MEASUREGROUP_NAME")),
                    Optional.ofNullable(m.get("MEASUREGROUP_CARDINALITY")),
                    Optional.ofNullable(m.get("DIMENSION_UNIQUE_NAME")),
                    Optional.ofNullable(DimensionCardinalityEnum.fromValue(m.get("DIMENSION_CARDINALITY"))),
                    Optional.ofNullable(getBoolean(m.get("DIMENSION_IS_VISIBLE"))),
                    Optional.ofNullable(getBoolean(m.get("DIMENSION_IS_FACT_DIMENSION"))),
                    Optional.ofNullable(convertToMeasureGroupDimensionList(nodeList.item(i))),
                    Optional.ofNullable(m.get("DIMENSION_GRANULARITY"))
                ));
            }
        }
        return List.of();
    }

    public static List<MdSchemaLevelsResponseRow> convertToMdSchemaLevelsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaLevelsResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get("DIMENSION_UNIQUE_NAME")),
                Optional.ofNullable(m.get("HIERARCHY_UNIQUE_NAME")),
                Optional.ofNullable(m.get("LEVEL_NAME")),
                Optional.ofNullable(m.get("LEVEL_UNIQUE_NAME")),
                Optional.ofNullable(getInt(m.get("LEVEL_GUID"))),
                Optional.ofNullable(m.get("LEVEL_CAPTION")),
                Optional.ofNullable(getInt(m.get("LEVEL_NUMBER"))),
                Optional.ofNullable(getInt(m.get("LEVEL_CARDINALITY"))),
                Optional.ofNullable(LevelTypeEnum.fromValue(m.get("LEVEL_TYPE"))),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(CustomRollupSettingEnum.fromValue(m.get("CUSTOM_ROLLUP_SETTINGS"))),
                Optional.ofNullable(LevelUniqueSettingsEnum.fromValue(m.get("LEVEL_UNIQUE_SETTINGS"))),
                Optional.ofNullable(getBoolean(m.get("LEVEL_IS_VISIBLE"))),
                Optional.ofNullable(m.get("LEVEL_ORDERING_PROPERTY")),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get("LEVEL_DBTYPE"))),
                Optional.ofNullable(m.get("LEVEL_MASTER_UNIQUE_NAME")),
                Optional.ofNullable(m.get("LEVEL_NAME_SQL_COLUMN_NAME")),
                Optional.ofNullable(m.get("LEVEL_KEY_SQL_COLUMN_NAME")),
                Optional.ofNullable(m.get("LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME")),
                Optional.ofNullable(m.get("LEVEL_ATTRIBUTE_HIERARCHY_NAME")),
                Optional.ofNullable(getInt(m.get("LEVEL_KEY_CARDINALITY"))),
                Optional.ofNullable(LevelOriginEnum.fromValue(m.get("LEVEL_ORIGIN")))
            )
        ).collect(toList());
    }

    public static List<DbSchemaSchemataResponseRow> convertToDbSchemaSchemataResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaSchemataResponseRowR(
                m.get(CATALOG_NAME),
                m.get(SCHEMA_NAME),
                m.get(SCHEMA_OWNER)
            )
        ).collect(toList());
    }

    public static List<DbSchemaProviderTypesResponseRow> convertToDbSchemaProviderTypesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaProviderTypesResponseRowR(
                Optional.ofNullable(m.get("TYPE_NAME")),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get("DATA_TYPE"))),
                Optional.ofNullable(getInt(m.get("COLUMN_SIZE"))),
                Optional.ofNullable(m.get("LITERAL_PREFIX")),
                Optional.ofNullable(m.get("LITERAL_SUFFIX")),
                Optional.ofNullable(m.get("CREATE_PARAMS")),
                Optional.ofNullable(getBoolean(m.get("IS_NULLABLE"))),
                Optional.ofNullable(getBoolean(m.get("CASE_SENSITIVE"))),
                Optional.ofNullable(SearchableEnum.fromValue(m.get("SEARCHABLE"))),
                Optional.ofNullable(getBoolean(m.get("UNSIGNED_ATTRIBUTE"))),
                Optional.ofNullable(getBoolean(m.get("FIXED_PREC_SCALE"))),
                Optional.ofNullable(getBoolean(m.get("AUTO_UNIQUE_VALUE"))),
                Optional.ofNullable(m.get("LOCAL_TYPE_NAME")),
                Optional.ofNullable(getInt(m.get("MINIMUM_SCALE"))),
                Optional.ofNullable(getInt(m.get("MAXIMUM_SCALE"))),
                Optional.ofNullable(getInt(m.get("GUID"))),
                Optional.ofNullable(m.get("TYPE_LIB")),
                Optional.ofNullable(m.get("VERSION")),
                Optional.ofNullable(getBoolean(m.get("IS_LONG"))),
                Optional.ofNullable(getBoolean(m.get("BEST_MATCH"))),
                Optional.ofNullable(getBoolean(m.get("IS_FIXEDLENGTH")))
            )
        ).collect(toList());
    }

    public static List<DbSchemaColumnsResponseRow> convertToDbSchemaColumnsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaColumnsResponseRowR(
                Optional.ofNullable(m.get(TABLE_CATALOG)),
                Optional.ofNullable(m.get(TABLE_SCHEMA)),
                Optional.ofNullable(m.get(TABLE_NAME)),
                Optional.ofNullable(m.get("COLUMN_NAME")),
                Optional.ofNullable(getInt(m.get("COLUMN_GUID"))),
                Optional.ofNullable(getInt(m.get("COLUMN_PROPID"))),
                Optional.ofNullable(getInt(m.get("ORDINAL_POSITION"))),
                Optional.ofNullable(getBoolean(m.get("COLUMN_HAS_DEFAULT"))),
                Optional.ofNullable(m.get("COLUMN_DEFAULT")),
                Optional.ofNullable(ColumnFlagsEnum.fromValue(m.get("COLUMN_FLAG"))),
                Optional.ofNullable(getBoolean(m.get("IS_NULLABLE"))),
                Optional.ofNullable(getInt(m.get("DATA_TYPE"))),
                Optional.ofNullable(getInt(m.get("TYPE_GUID"))),
                Optional.ofNullable(getInt(m.get("CHARACTER_MAXIMUM_LENGTH"))),
                Optional.ofNullable(getInt(m.get("CHARACTER_OCTET_LENGTH"))),
                Optional.ofNullable(getInt(m.get("NUMERIC_PRECISION"))),
                Optional.ofNullable(getInt(m.get("NUMERIC_SCALE"))),
                Optional.ofNullable(getInt(m.get("DATETIME_PRECISION"))),
                Optional.ofNullable(m.get("CHARACTER_SET_CATALOG")),
                Optional.ofNullable(m.get("CHARACTER_SET_SCHEMA")),
                Optional.ofNullable(m.get("CHARACTER_SET_NAME")),
                Optional.ofNullable(m.get("COLLATION_CATALOG")),
                Optional.ofNullable(m.get("COLLATION_SCHEMA")),
                Optional.ofNullable(m.get("COLLATION_NAME")),
                Optional.ofNullable(m.get("DOMAIN_CATALOG")),
                Optional.ofNullable(m.get("DOMAIN_SCHEMA")),
                Optional.ofNullable(m.get("DOMAIN_NAME")),
                Optional.ofNullable(m.get("DESCRIPTION")),
                Optional.ofNullable(ColumnOlapTypeEnum.fromValue(m.get("COLUMN_OLAP_TYPE")))
            )
        ).collect(toList());
    }

    public static List<DiscoverXmlMetaDataResponseRow> convertToDiscoverXmlMetaDataResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverXmlMetaDataResponseRowR(
                m.get("MetaData")
            )
        ).collect(toList());
    }

    public static List<DiscoverDataSourcesResponseRow> convertToDiscoverDataSourcesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverDataSourcesResponseRowR(
                m.get("DataSourceName"),
                Optional.ofNullable(m.get("DataSourceDescription")),
                Optional.ofNullable(m.get("URL")),
                Optional.ofNullable(m.get("DataSourceInfo")),
                m.get("ProviderName"),
                Optional.ofNullable(ProviderTypeEnum.fromValue(m.get("ProviderType"))),
                Optional.ofNullable(AuthenticationModeEnum.fromValue(m.get("AuthenticationMode")))
            )
        ).collect(toList());
    }

    public static List<DbSchemaTablesResponseRow> convertToDbSchemaTablesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaTablesResponseRowR(
                Optional.ofNullable(m.get(TABLE_CATALOG)),
                Optional.ofNullable(m.get(TABLE_SCHEMA)),
                Optional.ofNullable(m.get(TABLE_NAME)),
                Optional.ofNullable(m.get(TABLE_TYPE)),
                Optional.ofNullable(m.get(TABLE_GUID)),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(getInt(m.get(TABLE_PROP_ID))),
                Optional.ofNullable(getLocalDateTime(m.get(DATE_CREATED))),
                Optional.ofNullable(getLocalDateTime(m.get(DATE_MODIFIED)))
            )
        ).collect(toList());
    }

    public static List<DiscoverEnumeratorsResponseRow> convertToDiscoverEnumeratorsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverEnumeratorsResponseRowR(
                m.get("EnumName"),
                Optional.ofNullable(m.get("EnumDescription")),
                m.get("EnumType"),
                m.get("ElementName"),
                Optional.ofNullable(m.get("ElementDescription")),
                Optional.ofNullable(m.get("ElementValue"))
            )
        ).collect(toList());
    }

    public static List<DiscoverPropertiesResponseRow> convertToDiscoverPropertiesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverPropertiesResponseRowR(
                m.get("PropertyName"),
                Optional.ofNullable(m.get("PropertyDescription")),
                Optional.ofNullable(m.get("PropertyType")),
                m.get("PropertyAccessType"),
                Optional.ofNullable(getBoolean(m.get("IsRequired"))),
                Optional.ofNullable(m.get("Value"))
            )
        ).collect(toList());
    }

    public static List<DiscoverSchemaRowsetsResponseRow> convertToDiscoverSchemaRowsetsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverSchemaRowsetsResponseRowR(
                m.get("SchemaName"),
                Optional.ofNullable(m.get("SchemaGuid")),
                Optional.ofNullable(m.get("Restrictions")),
                Optional.ofNullable(m.get("Description")),
                Optional.ofNullable(getLong(m.get("RestrictionsMask")))
            )
        ).collect(toList());
    }

    public static List<DiscoverLiteralsResponseRow> convertToDiscoverLiteralsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverLiteralsResponseRowR(
                m.get("LiteralName"),
                m.get("LiteralValue"),
                m.get("LiteralInvalidChars"),
                m.get("LiteralInvalidStartingChars"),
                getInt(m.get("LiteralMaxLength")),
                LiteralNameEnumValueEnum.fromValue(getInt(m.get("LiteralNameValue")))
            )
        ).collect(toList());
    }

    public static List<DiscoverKeywordsResponseRow> convertToDiscoverKeywordsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverKeywordsResponseRowR(
                m.get(KEYWORD)
            )
        ).collect(toList());
    }

    public static List<MdSchemaHierarchiesResponseRow> convertToMdSchemaHierarchiesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaHierarchiesResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get(DIMENSION_UNIQUE_NAME)),
                Optional.ofNullable(m.get(HIERARCHY_NAME)),
                Optional.ofNullable(m.get(HIERARCHY_UNIQUE_NAME)),
                Optional.ofNullable(getInt(m.get(HIERARCHY_GUID))),
                Optional.ofNullable(m.get(HIERARCHY_CAPTION)),
                Optional.ofNullable(DimensionTypeEnum.fromValue(getInt(m.get(DIMENSION_TYPE)))),
                Optional.ofNullable(getInt(m.get(HIERARCHY_CARDINALITY))),
                Optional.ofNullable(m.get(DEFAULT_MEMBER)),
                Optional.ofNullable(m.get(ALL_MEMBER)),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(StructureEnum.fromValue(m.get(STRUCTURE))),
                Optional.ofNullable(getBoolean(m.get(IS_VIRTUAL))),
                Optional.ofNullable(getBoolean(m.get(IS_READWRITE))),
                Optional.ofNullable(DimensionUniqueSettingEnum.fromValue(m.get(DIMENSION_UNIQUE_SETTINGS))),
                Optional.ofNullable(m.get(DIMENSION_MASTER_UNIQUE_NAME)),
                Optional.ofNullable(getBoolean(m.get(DIMENSION_IS_VISIBLE))),
                Optional.ofNullable(getInt(m.get(HIERARCHY_ORDINAL))),
                Optional.ofNullable(getBoolean(m.get(DIMENSION_IS_SHARED))),
                Optional.ofNullable(getBoolean(m.get(IERARCHY_IS_VISIBLE))),
                Optional.ofNullable(HierarchyOriginEnum.fromValue(m.get(HIERARCHY_ORIGIN))),
                Optional.ofNullable(m.get(HIERARCHY_DISPLAY_FOLDER)),
                Optional.ofNullable(InstanceSelectionEnum.fromValue(m.get(INSTANCE_SELECTION))),
                Optional.ofNullable(GroupingBehaviorEnum.fromValue(m.get(GROUPING_BEHAVIOR))),
                Optional.ofNullable(StructureTypeEnum.fromValue(m.get(STRUCTURE_TYPE)))
            )
        ).collect(toList());
    }

    public static List<MdSchemaFunctionsResponseRow> convertToMdSchemaFunctionsResponseRow(SOAPBody b) {
        List<MdSchemaFunctionsResponseRow> result = new ArrayList<>();
        NodeList nodeList = b.getElementsByTagName(ROW);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList nl = nodeList.item(i).getChildNodes();
                Map<String, String> m = getMapValues(nl);
                new MdSchemaFunctionsResponseRowR(
                    Optional.ofNullable(m.get(FUNCTION_NAME)),
                    Optional.ofNullable(m.get(DESCRIPTION)),
                    m.get(PARAMETER_LIST),
                    Optional.ofNullable(getInt(m.get(RETURN_TYPE))),
                    Optional.ofNullable(OriginEnum.fromValue(m.get(ORIGIN))),
                    Optional.ofNullable(InterfaceNameEnum.fromValue(m.get(INTERFACE_NAME))),
                    Optional.ofNullable(m.get(LIBRARY_NAME)),
                    Optional.ofNullable(m.get(DLL_NAME)),
                    Optional.ofNullable(m.get(HELP_FILE)),
                    Optional.ofNullable(m.get(HELP_CONTEXT)),
                    Optional.ofNullable(m.get(OBJECT)),
                    Optional.ofNullable(m.get(CAPTION)),
                    Optional.ofNullable(convertToParameterInfoList(nodeList.item(i))),
                    Optional.ofNullable(DirectQueryPushableEnum.fromValue(m.get(DIRECTQUERY_PUSHABLE))));
            }
        }
        return result;
    }

    public static List<MdSchemaDimensionsResponseRow> convertToMdSchemaDimensionsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaDimensionsResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(m.get(DIMENSION_NAME)),
                Optional.ofNullable(m.get(DIMENSION_UNIQUE_NAME)),
                Optional.ofNullable(getInt(m.get(DIMENSION_GUID))),
                Optional.ofNullable(m.get(DIMENSION_CAPTION)),
                Optional.ofNullable(getInt(m.get(DIMENSION_ORDINAL))),
                Optional.ofNullable(DimensionTypeEnum.fromValue(getInt(m.get(DIMENSION_TYPE)))),
                Optional.ofNullable(getInt(m.get(DIMENSION_CARDINALITY))),
                Optional.ofNullable(m.get(DEFAULT_HIERARCHY)),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(getBoolean(m.get(IS_VIRTUAL))),
                Optional.ofNullable(getBoolean(m.get(IS_READWRITE))),
                Optional.ofNullable(DimensionUniqueSettingEnum.fromValue(m.get(DIMENSION_UNIQUE_SETTINGS))),
                Optional.ofNullable(m.get(DIMENSION_MASTER_NAME)),
                Optional.ofNullable(getBoolean(m.get(DIMENSION_IS_VISIBLE)))
            )
        ).collect(toList());
    }

    public static List<MdSchemaActionsResponseRow> convertToMdSchemaActionsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaActionsResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                m.get(CUBE_NAME),
                Optional.ofNullable(m.get(ACTION_NAME)),
                Optional.ofNullable(ActionTypeEnum.fromValue(m.get(ACTION_TYPE))),
                m.get(COORDINATE),
                CoordinateTypeEnum.fromValue(m.get(COORDINATE_TYPE)),
                Optional.ofNullable(m.get(ACTION_CAPTION)),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(m.get(CONTENT)),
                Optional.ofNullable(m.get(APPLICATION)),
                Optional.ofNullable(InvocationEnum.fromValue(m.get(INVOCATION)))
            )
        ).collect(toList());

    }

    public static List<MdSchemaCubesResponseRow> convertToMdSchemaCubesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new MdSchemaCubesResponseRowR(
                m.get(CATALOG_NAME),
                Optional.ofNullable(m.get(SCHEMA_NAME)),
                Optional.ofNullable(m.get(CUBE_NAME)),
                Optional.ofNullable(CubeTypeEnum.fromValue(m.get(CUBE_TYPE))),
                Optional.ofNullable(getInt(m.get(CUBE_GUID))),
                Optional.ofNullable(getLocalDateTime(m.get(CREATED_ON))),
                Optional.ofNullable(getLocalDateTime(m.get(LAST_SCHEMA_UPDATE))),
                Optional.ofNullable(m.get(SCHEMA_UPDATED_BY)),
                Optional.ofNullable(getLocalDateTime(m.get(LAST_DATA_UPDATE))),
                Optional.ofNullable(m.get(DATA_UPDATED_BY)),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(getBoolean(m.get(IS_DRILLTHROUGH_ENABLED))),
                Optional.ofNullable(getBoolean(m.get(IS_LINKABLE))),
                Optional.ofNullable(getBoolean(m.get(IS_WRITE_ENABLED))),
                Optional.ofNullable(getBoolean(m.get(IS_SQL_ENABLED))),
                Optional.ofNullable(m.get(CUBE_CAPTION)),
                Optional.ofNullable(m.get(BASE_CUBE_NAME)),
                Optional.ofNullable(CubeSourceEnum.fromValue(m.get(CUBE_SOURCE))),
                Optional.ofNullable(PreferredQueryPatternsEnum.fromValue(getInt(m.get(PREFERRED_QUERY_PATTERNS))))
            )
        ).collect(toList());
    }

    public static List<DbSchemaCatalogsResponseRow> convertToDbSchemaCatalogsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaCatalogsResponseRowR(
                Optional.ofNullable(m.get(CATALOG_NAME)),
                Optional.ofNullable(m.get(DESCRIPTION)),
                Optional.ofNullable(m.get(ROLES)),
                Optional.ofNullable(getLocalDateTime(m.get(DATE_MODIFIED))),
                Optional.ofNullable(getInt(m.get(COMPATIBILITY_LEVEL))),
                Optional.ofNullable(TypeEnum.fromValue(m.get(TYPE))),
                Optional.ofNullable(getInt(m.get(VERSION))),
                Optional.ofNullable(m.get(DATABASE_ID)),
                Optional.ofNullable(getLocalDateTime(m.get(DATE_QUERIED))),
                Optional.ofNullable(getBoolean(m.get(CURRENTLY_USED))),
                Optional.ofNullable(getDouble(m.get(POPULARITY))),
                Optional.ofNullable(getDouble(m.get(WEIGHTEDPOPULARITY))),
                Optional.ofNullable(ClientCacheRefreshPolicyEnum.fromValue(m.get(CLIENTCACHEREFRESHPOLICY)))
            )
        ).collect(toList());
    }

    private static List<MeasureGroupDimension> convertToMeasureGroupDimensionList(Node node) {
        List<MeasureGroupDimension> result = new ArrayList<>();
        NodeList nl = node.getChildNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if ("DIMENSION_PATH".equals(n.getNodeName())) {
                    result.addAll(convertToMeasureGroupDimensionList(n.getChildNodes()));
                }
            }
        }
        return List.of();
    }

    private static List<MeasureGroupDimension> convertToMeasureGroupDimensionList(NodeList nl) {
        List<MeasureGroupDimension> result = new ArrayList<>();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if ("MeasureGroupDimension".equals(n.getNodeName())) {
                    result.add(
                        new MeasureGroupDimensionR(n.getNodeValue())
                    );
                }
            }
        }
        return List.of();
    }

    private static List<ParameterInfo> convertToParameterInfoList(Node node) {
        List<ParameterInfo> result = new ArrayList<>();
        NodeList nl = node.getChildNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (PARAMETERINFO.equals(n.getNodeName())) {
                    Map<String, String> m = getMapValues(n.getChildNodes());
                    result.add(
                        new ParameterInfoR(m.get(NAME),
                            m.get(DESCRIPTION),
                            getBoolean(m.get(OPTIONAL)),
                            getBoolean(m.get(REPEATABLE)),
                            getInt(m.get(REPEATGROUP)))
                    );
                }
            }
        }
        return List.of();
    }

    private static Map<String, String> getMapValues(NodeList nl) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            result.put(n.getNodeName(), n.getTextContent());
        }
        return result;
    }

    private static Boolean getBoolean(String value) {
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    private static Integer getInt(String value) {
        if (value != null) {
            return Integer.parseInt(value);
        }
        return null;
    }

    private static LocalDateTime getLocalDateTime(String value) {
        if (value != null) {
            return LocalDateTime.parse(value);
        }
        return null;
    }

    private static Double getDouble(String value) {
        if (value != null) {
            return Double.parseDouble(value);
        }
        return null;
    }

    private static Long getLong(String value) {
        if (value != null) {
            return Long.parseLong(value);
        }
        return null;
    }

    private static List<Map<String, String>> getMapValuesList(SOAPBody b) {
        List<Map<String, String>> result = new ArrayList<>();
        NodeList nodeList = b.getElementsByTagName(ROW);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList nl = nodeList.item(i).getChildNodes();
                Map<String, String> map = getMapValues(nl);
                result.add(map);
            }
        }
        return result;
    }

}

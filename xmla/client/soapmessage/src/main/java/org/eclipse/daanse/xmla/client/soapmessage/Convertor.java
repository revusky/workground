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
import org.eclipse.daanse.xmla.api.execute.alter.AlterResponse;
import org.eclipse.daanse.xmla.api.execute.cancel.CancelResponse;
import org.eclipse.daanse.xmla.api.execute.clearcache.ClearCacheResponse;
import org.eclipse.daanse.xmla.api.execute.statement.StatementResponse;
import org.eclipse.daanse.xmla.api.mddataset.Axis;
import org.eclipse.daanse.xmla.api.mddataset.AxisInfo;
import org.eclipse.daanse.xmla.api.mddataset.CellInfoItem;
import org.eclipse.daanse.xmla.api.mddataset.CellType;
import org.eclipse.daanse.xmla.api.mddataset.CellTypeError;
import org.eclipse.daanse.xmla.api.mddataset.HierarchyInfo;
import org.eclipse.daanse.xmla.api.mddataset.MemberType;
import org.eclipse.daanse.xmla.api.mddataset.OlapInfoCube;
import org.eclipse.daanse.xmla.api.mddataset.Type;
import org.eclipse.daanse.xmla.api.xmla_empty.Emptyresult;
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
import org.eclipse.daanse.xmla.model.record.engine200.WarningColumnR;
import org.eclipse.daanse.xmla.model.record.engine200.WarningLocationObjectR;
import org.eclipse.daanse.xmla.model.record.engine200.WarningMeasureR;
import org.eclipse.daanse.xmla.model.record.exception.ErrorTypeR;
import org.eclipse.daanse.xmla.model.record.exception.ExceptionR;
import org.eclipse.daanse.xmla.model.record.exception.MessageLocationR;
import org.eclipse.daanse.xmla.model.record.exception.MessagesR;
import org.eclipse.daanse.xmla.model.record.exception.StartEndR;
import org.eclipse.daanse.xmla.model.record.exception.WarningTypeR;
import org.eclipse.daanse.xmla.model.record.execute.alter.AlterResponseR;
import org.eclipse.daanse.xmla.model.record.execute.cancel.CancelResponseR;
import org.eclipse.daanse.xmla.model.record.execute.clearcache.ClearCacheResponseR;
import org.eclipse.daanse.xmla.model.record.execute.statement.StatementResponseR;
import org.eclipse.daanse.xmla.model.record.mddataset.AxesInfoR;
import org.eclipse.daanse.xmla.model.record.mddataset.AxesR;
import org.eclipse.daanse.xmla.model.record.mddataset.AxisInfoR;
import org.eclipse.daanse.xmla.model.record.mddataset.AxisR;
import org.eclipse.daanse.xmla.model.record.mddataset.CellDataR;
import org.eclipse.daanse.xmla.model.record.mddataset.CellInfoItemR;
import org.eclipse.daanse.xmla.model.record.mddataset.CellInfoR;
import org.eclipse.daanse.xmla.model.record.mddataset.CellSetTypeR;
import org.eclipse.daanse.xmla.model.record.mddataset.CellTypeErrorR;
import org.eclipse.daanse.xmla.model.record.mddataset.CellTypeR;
import org.eclipse.daanse.xmla.model.record.mddataset.CubeInfoR;
import org.eclipse.daanse.xmla.model.record.mddataset.HierarchyInfoR;
import org.eclipse.daanse.xmla.model.record.mddataset.MddatasetR;
import org.eclipse.daanse.xmla.model.record.mddataset.MemberTypeR;
import org.eclipse.daanse.xmla.model.record.mddataset.MembersTypeR;
import org.eclipse.daanse.xmla.model.record.mddataset.OlapInfoCubeR;
import org.eclipse.daanse.xmla.model.record.mddataset.OlapInfoR;
import org.eclipse.daanse.xmla.model.record.mddataset.ValueR;
import org.eclipse.daanse.xmla.model.record.xmla_empty.EmptyresultR;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ACTION_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ACTION_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ACTION_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ALL_MEMBER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ANNOTATIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.APPLICATION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.AUTO_UNIQUE_VALUE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.BASE_CUBE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.BEST_MATCH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.BOOKMARKS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.BOOKMARK_DATA_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.BOOKMARK_INFORMATION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.BOOKMARK_MAXIMUM_LENGTH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.BOOKMARK_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CAPTION_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CASE_SENSITIVE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CATALOG_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CHARACTER_MAXIMUM_LENGTH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CHARACTER_OCTET_LENGTH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CHARACTER_SET_CATALOG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CHARACTER_SET_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CHARACTER_SET_SCHEMA;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CHILDREN_CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CLIENTCACHEREFRESHPOLICY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLLATION_CATALOG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLLATION_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLLATION_SCHEMA;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_DEFAULT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_FLAG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_HAS_DEFAULT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_OLAP_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_PROPID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_SIZE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COMPATIBILITY_LEVEL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CONTENT_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COORDINATE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COORDINATE_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CREATED_ON;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CREATE_PARAMS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUBE_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUBE_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUBE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUBE_SOURCE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUBE_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CURRENTLY_USED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUSTOM_ROLLUP_SETTINGS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATABASE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATA_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATA_UPDATED_BY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATETIME_PRECISION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATE_CREATED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATE_MODIFIED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATE_QUERIED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DEFAULT_FORMAT_STRING;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DEFAULT_HIERARCHY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DEFAULT_MEMBER_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DESCRIPTION1;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DESCRIPTION_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSIONS_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_GRANULARITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_IS_FACT_DIMENSION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_IS_SHARED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_IS_VISIBLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_MASTER_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_MASTER_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_ORDINAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_PATH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_UNIQUE_SETTINGS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIRECTQUERY_PUSHABLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DLL_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DOMAIN_CATALOG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DOMAIN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DOMAIN_SCHEMA;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ELEMENT_DESCRIPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ELEMENT_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ELEMENT_VALUE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ENUM_DESCRIPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ENUM_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ENUM_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.EXCEPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.EXPRESSION_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.FIXED_PREC_SCALE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.FUNCTION_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.GROUPING_BEHAVIOR;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HELP_CONTEXT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HELP_FILE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_DISPLAY_FOLDER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_ORDINAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_ORIGIN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IERARCHY_IS_VISIBLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.INSTANCE_SELECTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.INTERFACE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.INVOCATION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_DATAMEMBER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_DRILLTHROUGH_ENABLED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_FIXEDLENGTH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_LINKABLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_LONG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_NULLABLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_PLACEHOLDERMEMBER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_READWRITE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_SQL_ENABLED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_VIRTUAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.IS_WRITE_ENABLED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KEYWORD;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_CURRENT_TIME_MEMBER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_DESCRIPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_DISPLAY_FOLDER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_GOAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_PARENT_KPI_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_STATUS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_STATUS_GRAPHIC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_TREND;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_TREND_GRAPHIC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_VALUE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KPI_WEIGHT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LANGUAGE_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LAST_DATA_UPDATE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LAST_SCHEMA_UPDATE_UC;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVELS_LIST;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_ATTRIBUTE_HIERARCHY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_DBTYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_IS_VISIBLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_KEY_CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_KEY_SQL_COLUMN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_MASTER_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_NAME_SQL_COLUMN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_NUMBER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_ORDERING_PROPERTY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_ORIGIN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LEVEL_UNIQUE_SETTINGS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LIBRARY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_INVALID_CHARS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_INVALID_STARTING_CHARS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_MAX_LENGTH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_NAME_VALUE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_PREFIX;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_SUFFIX;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LITERAL_VALUE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LOCAL_TYPE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MAXIMUM_SCALE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASUREGROUP_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASUREGROUP_CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASUREGROUP_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_AGGREGATOR;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_GROUP_DIMENSION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_IS_VISIBLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_NAME_SQL_COLUMN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_UNITS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_UNQUALIFIED_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEMBER_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEMBER_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEMBER_KEY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEMBER_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEMBER_ORDINAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEMBER_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEMBER_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MESSAGES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MIME_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MINIMUM_SCALE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.NUMERIC_PRECISION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.NUMERIC_SCALE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.OBJECT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.OPTIONAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ORDINAL_POSITION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ORIGIN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PARAMETERINFO;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PARAMETER_LIST;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PARENT_COUN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PARENT_LEVEL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PARENT_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.POPULARITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PREFERRED_QUERY_PATTERNS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_ATTRIBUTE_HIERARCHY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_CARDINALITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_CONTENT_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_IS_VISIBLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_NAME1;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.REPEATABLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.REPEATGROUP;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MASK;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RETURN_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ROLES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ROPERTY_ORIGIN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ROW;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SCHEMA_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SCHEMA_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SCHEMA_NAME1;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SCHEMA_OWNER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SCHEMA_UPDATED_BY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SCOPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SEARCHABLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SET_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SET_DISPLAY_FOLDER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SET_EVALUATION_CONTEXT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SET_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SQL_COLUMN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.STRUCTURE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.STRUCTURE_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_CATALOG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_PROP_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_SCHEMA;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_VERSION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TYPE_GUID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TYPE_LIB;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TYPE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.UNSIGNED_ATTRIBUTE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.VERSION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.WEIGHTEDPOPULARITY;

public class Convertor {

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
                Optional.ofNullable(getBoolean(m.get(BOOKMARKS))),
                Optional.ofNullable(getInt(m.get(BOOKMARK_TYPE))),
                Optional.ofNullable(getInt(m.get(BOOKMARK_DATA_TYPE))),
                Optional.ofNullable(getInt(m.get(BOOKMARK_MAXIMUM_LENGTH))),
                Optional.ofNullable(getInt(m.get(BOOKMARK_INFORMATION))),
                Optional.ofNullable(getLong(m.get(TABLE_VERSION))),

                Optional.ofNullable(getLong(m.get(CARDINALITY))),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(getInt(m.get(TABLE_PROP_ID)))
            )
        ).collect(toList());
    }

    public static List<DbSchemaSourceTablesResponseRow> convertToDbSchemaSourceTablesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DbSchemaSourceTablesResponseRowR(
                Optional.ofNullable(m.get(TABLE_CATALOG)),
                Optional.ofNullable(m.get(TABLE_SCHEMA)),
                m.get(TABLE_NAME),
                TableTypeEnum.fromValue(m.get(TABLE_TYPE))
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
                Optional.ofNullable(m.get(MEASUREGROUP_NAME)),
                Optional.ofNullable(m.get(KPI_NAME)),
                Optional.ofNullable(m.get(KPI_CAPTION)),
                Optional.ofNullable(m.get(KPI_DESCRIPTION)),
                Optional.ofNullable(m.get(KPI_DISPLAY_FOLDER)),
                Optional.ofNullable(m.get(KPI_VALUE)),
                Optional.ofNullable(m.get(KPI_GOAL)),
                Optional.ofNullable(m.get(KPI_STATUS)),
                Optional.ofNullable(m.get(KPI_TREND)),
                Optional.ofNullable(m.get(KPI_STATUS_GRAPHIC)),
                Optional.ofNullable(m.get(KPI_TREND_GRAPHIC)),
                Optional.ofNullable(m.get(KPI_WEIGHT)),
                Optional.ofNullable(m.get(KPI_CURRENT_TIME_MEMBER)),
                Optional.ofNullable(m.get(KPI_PARENT_KPI_NAME)),
                Optional.ofNullable(m.get(ANNOTATIONS)),
                Optional.ofNullable(ScopeEnum.fromValue(m.get(SCOPE)))
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
                Optional.ofNullable(m.get(MEASUREGROUP_NAME)),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(getBoolean(m.get(IS_WRITE_ENABLED))),
                Optional.ofNullable(m.get(MEASUREGROUP_CAPTION))
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
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(m.get(EXPRESSION_UC)),
                Optional.ofNullable(m.get(DIMENSIONS_UC)),
                Optional.ofNullable(m.get(SET_CAPTION)),
                Optional.ofNullable(m.get(SET_DISPLAY_FOLDER)),
                Optional.ofNullable(SetEvaluationContextEnum.fromValue(m.get(SET_EVALUATION_CONTEXT)))
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
                Optional.ofNullable(m.get(DIMENSION_UNIQUE_NAME)),
                Optional.ofNullable(m.get(HIERARCHY_UNIQUE_NAME)),
                Optional.ofNullable(m.get(LEVEL_UNIQUE_NAME)),
                Optional.ofNullable(m.get(MEMBER_UNIQUE_NAME)),
                Optional.ofNullable(PropertyTypeEnum.fromValue(m.get(PROPERTY_TYPE))),
                Optional.ofNullable(m.get(PROPERTY_NAME)),
                Optional.ofNullable(m.get(PROPERTY_CAPTION)),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get(DATA_TYPE))),
                Optional.ofNullable(getInt(m.get(CHARACTER_MAXIMUM_LENGTH))),
                Optional.ofNullable(getInt(m.get(CHARACTER_OCTET_LENGTH))),
                Optional.ofNullable(getInt(m.get(NUMERIC_PRECISION))),
                Optional.ofNullable(getInt(m.get(NUMERIC_SCALE))),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(PropertyContentTypeEnum.fromValue(m.get(PROPERTY_CONTENT_TYPE))),
                Optional.ofNullable(m.get(SQL_COLUMN_NAME)),
                Optional.ofNullable(getInt(m.get(LANGUAGE_UC))),
                Optional.ofNullable(PropertyOriginEnum.fromValue(m.get(ROPERTY_ORIGIN))),
                Optional.ofNullable(m.get(PROPERTY_ATTRIBUTE_HIERARCHY_NAME)),
                Optional.ofNullable(PropertyCardinalityEnum.fromValue(m.get(PROPERTY_CARDINALITY))),
                Optional.ofNullable(m.get(MIME_TYPE)),
                Optional.ofNullable(getBoolean(m.get(PROPERTY_IS_VISIBLE)))
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
                Optional.ofNullable(m.get(DIMENSION_UNIQUE_NAME)),
                Optional.ofNullable(m.get(HIERARCHY_UNIQUE_NAME)),
                Optional.ofNullable(m.get(LEVEL_UNIQUE_NAME)),
                Optional.ofNullable(getInt(m.get(LEVEL_NUMBER))),
                Optional.ofNullable(getInt(m.get(MEMBER_ORDINAL))),
                Optional.ofNullable(m.get(MEMBER_NAME)),
                Optional.ofNullable(m.get(MEMBER_UNIQUE_NAME)),
                Optional.ofNullable(MemberTypeEnum.fromValue(m.get(MEMBER_TYPE))),
                Optional.ofNullable(getInt(m.get(MEMBER_GUID))),
                Optional.ofNullable(m.get(MEMBER_CAPTION)),
                Optional.ofNullable(getInt(m.get(CHILDREN_CARDINALITY))),
                Optional.ofNullable(getInt(m.get(PARENT_LEVEL))),
                Optional.ofNullable(m.get(PARENT_UNIQUE_NAME)),
                Optional.ofNullable(getInt(m.get(PARENT_COUN))),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(m.get(EXPRESSION_UC)),
                Optional.ofNullable(m.get(MEMBER_KEY)),
                Optional.ofNullable(getBoolean(m.get(IS_PLACEHOLDERMEMBER))),
                Optional.ofNullable(getBoolean(m.get(IS_DATAMEMBER))),
                Optional.ofNullable(ScopeEnum.fromValue(m.get(DESCRIPTION_UC)))
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
                Optional.ofNullable(m.get(MEASURE_NAME)),
                Optional.ofNullable(m.get(MEASURE_UNIQUE_NAME)),
                Optional.ofNullable(m.get(MEASURE_CAPTION)),
                Optional.ofNullable(getInt(m.get(MEASURE_GUID))),
                Optional.ofNullable(MeasureAggregatorEnum.fromValue(m.get(MEASURE_AGGREGATOR))),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get(DATA_TYPE))),
                Optional.ofNullable(getInt(m.get(NUMERIC_PRECISION))),
                Optional.ofNullable(getInt(m.get(NUMERIC_SCALE))),
                Optional.ofNullable(m.get(MEASURE_UNITS)),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(m.get(EXPRESSION_UC)),
                Optional.ofNullable(getBoolean(m.get(MEASURE_IS_VISIBLE))),
                Optional.ofNullable(m.get(LEVELS_LIST)),
                Optional.ofNullable(m.get(MEASURE_NAME_SQL_COLUMN_NAME)),
                Optional.ofNullable(m.get(MEASURE_UNQUALIFIED_CAPTION)),
                Optional.ofNullable(m.get(MEASUREGROUP_NAME)),
                Optional.ofNullable(m.get(DEFAULT_FORMAT_STRING))
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
                    Optional.ofNullable(m.get(MEASUREGROUP_NAME)),
                    Optional.ofNullable(m.get(MEASUREGROUP_CARDINALITY)),
                    Optional.ofNullable(m.get(DIMENSION_UNIQUE_NAME)),
                    Optional.ofNullable(DimensionCardinalityEnum.fromValue(m.get(DIMENSION_CARDINALITY))),
                    Optional.ofNullable(getBoolean(m.get(DIMENSION_IS_VISIBLE))),
                    Optional.ofNullable(getBoolean(m.get(DIMENSION_IS_FACT_DIMENSION))),
                    Optional.ofNullable(convertToMeasureGroupDimensionList(nodeList.item(i))),
                    Optional.ofNullable(m.get(DIMENSION_GRANULARITY))
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
                Optional.ofNullable(m.get(DIMENSION_UNIQUE_NAME)),
                Optional.ofNullable(m.get(HIERARCHY_UNIQUE_NAME)),
                Optional.ofNullable(m.get(LEVEL_NAME)),
                Optional.ofNullable(m.get(LEVEL_UNIQUE_NAME)),
                Optional.ofNullable(getInt(m.get(LEVEL_GUID))),
                Optional.ofNullable(m.get(LEVEL_CAPTION)),
                Optional.ofNullable(getInt(m.get(LEVEL_NUMBER))),
                Optional.ofNullable(getInt(m.get(LEVEL_CARDINALITY))),
                Optional.ofNullable(LevelTypeEnum.fromValue(m.get(LEVEL_TYPE))),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(CustomRollupSettingEnum.fromValue(m.get(CUSTOM_ROLLUP_SETTINGS))),
                Optional.ofNullable(LevelUniqueSettingsEnum.fromValue(m.get(LEVEL_UNIQUE_SETTINGS))),
                Optional.ofNullable(getBoolean(m.get(LEVEL_IS_VISIBLE))),
                Optional.ofNullable(m.get(LEVEL_ORDERING_PROPERTY)),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get(LEVEL_DBTYPE))),
                Optional.ofNullable(m.get(LEVEL_MASTER_UNIQUE_NAME)),
                Optional.ofNullable(m.get(LEVEL_NAME_SQL_COLUMN_NAME)),
                Optional.ofNullable(m.get(LEVEL_KEY_SQL_COLUMN_NAME)),
                Optional.ofNullable(m.get(LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME)),
                Optional.ofNullable(m.get(LEVEL_ATTRIBUTE_HIERARCHY_NAME)),
                Optional.ofNullable(getInt(m.get(LEVEL_KEY_CARDINALITY))),
                Optional.ofNullable(LevelOriginEnum.fromValue(m.get(LEVEL_ORIGIN)))
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
                Optional.ofNullable(m.get(TYPE_NAME)),
                Optional.ofNullable(LevelDbTypeEnum.fromValue(m.get(DATA_TYPE))),
                Optional.ofNullable(getInt(m.get(COLUMN_SIZE))),
                Optional.ofNullable(m.get(LITERAL_PREFIX)),
                Optional.ofNullable(m.get(LITERAL_SUFFIX)),
                Optional.ofNullable(m.get(CREATE_PARAMS)),
                Optional.ofNullable(getBoolean(m.get(IS_NULLABLE))),
                Optional.ofNullable(getBoolean(m.get(CASE_SENSITIVE))),
                Optional.ofNullable(SearchableEnum.fromValue(m.get(SEARCHABLE))),
                Optional.ofNullable(getBoolean(m.get(UNSIGNED_ATTRIBUTE))),
                Optional.ofNullable(getBoolean(m.get(FIXED_PREC_SCALE))),
                Optional.ofNullable(getBoolean(m.get(AUTO_UNIQUE_VALUE))),
                Optional.ofNullable(m.get(LOCAL_TYPE_NAME)),
                Optional.ofNullable(getInt(m.get(MINIMUM_SCALE))),
                Optional.ofNullable(getInt(m.get(MAXIMUM_SCALE))),
                Optional.ofNullable(getInt(m.get(GUID))),
                Optional.ofNullable(m.get(TYPE_LIB)),
                Optional.ofNullable(m.get(VERSION)),
                Optional.ofNullable(getBoolean(m.get(IS_LONG))),
                Optional.ofNullable(getBoolean(m.get(BEST_MATCH))),
                Optional.ofNullable(getBoolean(m.get(IS_FIXEDLENGTH)))
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
                Optional.ofNullable(m.get(COLUMN_NAME)),
                Optional.ofNullable(getInt(m.get(COLUMN_GUID))),
                Optional.ofNullable(getInt(m.get(COLUMN_PROPID))),
                Optional.ofNullable(getInt(m.get(ORDINAL_POSITION))),
                Optional.ofNullable(getBoolean(m.get(COLUMN_HAS_DEFAULT))),
                Optional.ofNullable(m.get(COLUMN_DEFAULT)),
                Optional.ofNullable(ColumnFlagsEnum.fromValue(m.get(COLUMN_FLAG))),
                Optional.ofNullable(getBoolean(m.get(IS_NULLABLE))),
                Optional.ofNullable(getInt(m.get(DATA_TYPE))),
                Optional.ofNullable(getInt(m.get(TYPE_GUID))),
                Optional.ofNullable(getInt(m.get(CHARACTER_MAXIMUM_LENGTH))),
                Optional.ofNullable(getInt(m.get(CHARACTER_OCTET_LENGTH))),
                Optional.ofNullable(getInt(m.get(NUMERIC_PRECISION))),
                Optional.ofNullable(getInt(m.get(NUMERIC_SCALE))),
                Optional.ofNullable(getInt(m.get(DATETIME_PRECISION))),
                Optional.ofNullable(m.get(CHARACTER_SET_CATALOG)),
                Optional.ofNullable(m.get(CHARACTER_SET_SCHEMA)),
                Optional.ofNullable(m.get(CHARACTER_SET_NAME)),
                Optional.ofNullable(m.get(COLLATION_CATALOG)),
                Optional.ofNullable(m.get(COLLATION_SCHEMA)),
                Optional.ofNullable(m.get(COLLATION_NAME)),
                Optional.ofNullable(m.get(DOMAIN_CATALOG)),
                Optional.ofNullable(m.get(DOMAIN_SCHEMA)),
                Optional.ofNullable(m.get(DOMAIN_NAME)),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(ColumnOlapTypeEnum.fromValue(m.get(COLUMN_OLAP_TYPE)))
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
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
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
                m.get(ENUM_NAME),
                Optional.ofNullable(m.get(ENUM_DESCRIPTION)),
                m.get(ENUM_TYPE),
                m.get(ELEMENT_NAME),
                Optional.ofNullable(m.get(ELEMENT_DESCRIPTION)),
                Optional.ofNullable(m.get(ELEMENT_VALUE))
            )
        ).collect(toList());
    }

    public static List<DiscoverPropertiesResponseRow> convertToDiscoverPropertiesResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverPropertiesResponseRowR(
                m.get(PROPERTY_NAME1),
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
                m.get(SCHEMA_NAME1),
                Optional.ofNullable(m.get(SCHEMA_GUID)),
                Optional.ofNullable(m.get(RESTRICTIONS)),
                Optional.ofNullable(m.get(DESCRIPTION1)),
                Optional.ofNullable(getLong(m.get(RESTRICTIONS_MASK)))
            )
        ).collect(toList());
    }

    public static List<DiscoverLiteralsResponseRow> convertToDiscoverLiteralsResponseRow(SOAPBody b) {
        List<Map<String, String>> l = getMapValuesList(b);
        return l.stream().map(m ->
            new DiscoverLiteralsResponseRowR(
                m.get(LITERAL_NAME),
                m.get(LITERAL_VALUE),
                m.get(LITERAL_INVALID_CHARS),
                m.get(LITERAL_INVALID_STARTING_CHARS),
                getInt(m.get(LITERAL_MAX_LENGTH)),
                LiteralNameEnumValueEnum.fromValue(getInt(m.get(LITERAL_NAME_VALUE)))
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
                Optional.ofNullable(m.get(DEFAULT_MEMBER_UC)),
                Optional.ofNullable(m.get(ALL_MEMBER)),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
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
                    Optional.ofNullable(m.get(DESCRIPTION_UC)),
                    m.get(PARAMETER_LIST),
                    Optional.ofNullable(getInt(m.get(RETURN_TYPE))),
                    Optional.ofNullable(OriginEnum.fromValue(m.get(ORIGIN))),
                    Optional.ofNullable(InterfaceNameEnum.fromValue(m.get(INTERFACE_NAME))),
                    Optional.ofNullable(m.get(LIBRARY_NAME)),
                    Optional.ofNullable(m.get(DLL_NAME)),
                    Optional.ofNullable(m.get(HELP_FILE)),
                    Optional.ofNullable(m.get(HELP_CONTEXT)),
                    Optional.ofNullable(m.get(OBJECT)),
                    Optional.ofNullable(m.get(CAPTION_UC)),
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
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
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
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
                Optional.ofNullable(m.get(CONTENT_UC)),
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
                Optional.ofNullable(getLocalDateTime(m.get(LAST_SCHEMA_UPDATE_UC))),
                Optional.ofNullable(m.get(SCHEMA_UPDATED_BY)),
                Optional.ofNullable(getLocalDateTime(m.get(LAST_DATA_UPDATE))),
                Optional.ofNullable(m.get(DATA_UPDATED_BY)),
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
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
                Optional.ofNullable(m.get(DESCRIPTION_UC)),
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

    public static StatementResponse convertToStatementResponse(SOAPBody soapBody) {

        NodeList olapInfoNl = soapBody.getElementsByTagName("OlapInfo");
        NodeList axesNl = soapBody.getElementsByTagName("Axes");
        NodeList cellDataNl = soapBody.getElementsByTagName("CellData");
        NodeList exceptionNl = soapBody.getElementsByTagName(EXCEPTION);
        NodeList messagesNl = soapBody.getElementsByTagName(MESSAGES);

        OlapInfoR olapInfo = getOlapInfo(olapInfoNl);
        AxesR axes = getAxes(axesNl);
        CellDataR cellData = getCellData(cellDataNl);
        ExceptionR exception = getException(exceptionNl);
        MessagesR messages = getMessages(messagesNl);

        MddatasetR mdDataSet = new MddatasetR(
            olapInfo,
            axes,
            cellData,
            exception,
            messages
        );
        return new StatementResponseR(mdDataSet);
    }

    public static AlterResponse convertToAlterResponse(SOAPBody soapBody) {
        NodeList exceptionNl = soapBody.getElementsByTagName(EXCEPTION);
        NodeList messagesNl = soapBody.getElementsByTagName(MESSAGES);
        ExceptionR exception = getException(exceptionNl);
        MessagesR messages = getMessages(messagesNl);
        Emptyresult emptyresult = new EmptyresultR(exception, messages);
        return new AlterResponseR(emptyresult);
    }

    public static ClearCacheResponse convertToClearCacheResponse(SOAPBody soapBody) {
        NodeList exceptionNl = soapBody.getElementsByTagName(EXCEPTION);
        NodeList messagesNl = soapBody.getElementsByTagName(MESSAGES);
        ExceptionR exception = getException(exceptionNl);
        MessagesR messages = getMessages(messagesNl);
        Emptyresult emptyresult = new EmptyresultR(exception, messages);
        return new ClearCacheResponseR(emptyresult);
    }

    public static CancelResponse convertToCancelResponse(SOAPBody soapBody) {
        NodeList exceptionNl = soapBody.getElementsByTagName(EXCEPTION);
        NodeList messagesNl = soapBody.getElementsByTagName(MESSAGES);
        ExceptionR exception = getException(exceptionNl);
        MessagesR messages = getMessages(messagesNl);
        Emptyresult emptyresult = new EmptyresultR(exception, messages);
        return new CancelResponseR(emptyresult);
    }

    private static MessagesR getMessages(NodeList nl) {
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i).getChildNodes().item(i);
                if (node != null && MESSAGES.equals(node.getNodeName())) {
                    return new MessagesR(getTypeList(node.getChildNodes()));
                }
            }
        }
        return null;
    }

    private static List<org.eclipse.daanse.xmla.api.exception.Type> getTypeList(NodeList nl) {
        if (nl != null) {
            List<org.eclipse.daanse.xmla.api.exception.Type> list = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && "Error".equals(node.getNodeName())) {
                    list.add(getErrorType(node));
                }
                if (node != null && "Warning".equals(node.getNodeName())) {
                    list.add(getWarningType(node));
                }
            }
            return list;
        }
        return null;
    }

    private static org.eclipse.daanse.xmla.api.exception.Type getWarningType(Node node) {
        if (node != null) {
            MessageLocationR location = getMessageLocation(node.getChildNodes());
            NamedNodeMap nm = node.getAttributes();

            return new WarningTypeR(location,
                getInt(getAttribute(nm, "WarningCode")),
                getAttribute(nm, DESCRIPTION1),
                getAttribute(nm, "Source"),
                getAttribute(nm, "HelpFile"));
        }
        return null;
    }

    private static org.eclipse.daanse.xmla.api.exception.Type getErrorType(Node node) {
        if (node != null) {
            MessageLocationR location = getMessageLocation(node.getChildNodes());
            String callstack = getCallstack(node.getChildNodes());
            NamedNodeMap nm = node.getAttributes();
            return new ErrorTypeR(location,
                callstack,
                getLong(getAttribute(nm, "ErrorCode")),
                getAttribute(nm, DESCRIPTION1),
                getAttribute(nm, "Source"),
                getAttribute(nm, "HelpFile"));
        }
        return null;
    }

    private static MessageLocationR getMessageLocation(NodeList nl) {
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && "Location".equals(node.getNodeName())) {
                    StartEndR start = null;
                    StartEndR end = null;
                    Integer lineOffset = null;
                    Integer textLength = null;
                    WarningLocationObjectR sourceObject = null;
                    WarningLocationObjectR dependsOnObject = null;
                    Integer rowNumber = null;
                    NodeList nodeList = node.getChildNodes();
                    if (nodeList != null) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            Node n = nodeList.item(j);
                            if (n != null) {
                                if ("Start".equals(n.getNodeName())) {
                                    start = getStartEnd(n.getChildNodes());
                                }
                                if ("End".equals(n.getNodeName())) {
                                    end = getStartEnd(n.getChildNodes());
                                }
                                if ("LineOffset".equals(n.getNodeName())) {
                                    lineOffset = getInt(n.getTextContent());
                                }
                                if ("TextLength".equals(n.getNodeName())) {
                                    textLength = getInt(n.getTextContent());
                                }
                                if ("SourceObject".equals(n.getNodeName())) {
                                    sourceObject = getWarningLocationObject(n.getChildNodes());
                                }
                                if ("DependsOnObject".equals(n.getNodeName())) {
                                    dependsOnObject = getWarningLocationObject(n.getChildNodes());
                                }
                                if ("RowNumber".equals(n.getNodeName())) {
                                    rowNumber = getInt(n.getTextContent());
                                }
                            }
                        }
                    }
                    return new MessageLocationR(
                        start,
                        end,
                        lineOffset,
                        textLength,
                        sourceObject,
                        dependsOnObject,
                        rowNumber);
                }
            }

        }
        return null;
    }

    private static WarningLocationObjectR getWarningLocationObject(NodeList nl) {
        if (nl != null) {
            WarningColumnR warningColumn = null;
            WarningMeasureR warningMeasure = null;
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null) {
                    if ("WarningColumn".equals(node.getNodeName())) {
                        warningColumn = getWarningColumn(node.getChildNodes());
                    }
                    if ("WarningMeasure".equals(node.getNodeName()) && node.getNodeValue() != null) {
                        warningMeasure = getWarningMeasure(node.getChildNodes());
                    }
                }
            }
            return new WarningLocationObjectR(warningColumn, warningMeasure);
        }
        return null;
    }

    private static WarningMeasureR getWarningMeasure(NodeList nl) {
        Map<String, String> map = getMapValues(nl);
        return new WarningMeasureR(
            map.get("Cube"),
            map.get("MeasureGroup"),
            map.get("MeasureName")
        );
    }

    private static WarningColumnR getWarningColumn(NodeList nl) {
        Map<String, String> map = getMapValues(nl);
        return new WarningColumnR(
            map.get("Dimension"),
            map.get("Attribute")
        );
    }

    private static StartEndR getStartEnd(NodeList nl) {
        if (nl != null) {
            int line = 0;
            int column = 0;
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null) {
                    if ("Line".equals(node.getNodeName()) && node.getNodeValue() != null) {
                        line = getInt(node.getNodeValue());
                    }
                    if ("Column".equals(node.getNodeName()) && node.getNodeValue() != null) {
                        column = getInt(node.getNodeValue());
                    }
                }
            }
            return new StartEndR(line, column);
        }

        return null;
    }

    private static String getCallstack(NodeList nl) {
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && "Callstack".equals(node.getNodeName())) {
                    return node.getNodeValue();
                }
            }

        }
        return null;
    }

    private static ExceptionR getException(NodeList nl) {
        if (nl != null) {
            return new ExceptionR();
        }
        return null;
    }

    private static CellDataR getCellData(NodeList nl) {
        if (nl != null) {
            List<CellType> list = new ArrayList<>();
            CellSetTypeR cellSetType = null;
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null) {
                    if ("Cell".equals(node.getNodeName())) {
                        NodeList nodeList = node.getChildNodes();
                        list.add(new CellTypeR(getValue(nodeList), getCellInfoItem(nodeList),
                            getLong(getAttribute(node.getAttributes(), "CellOrdinal"))));
                    }
                    if ("CellSetType".equals(node.getNodeName())) {
                        cellSetType = new CellSetTypeR(getDataList(node.getChildNodes()));
                    }
                }
            }
            return new CellDataR(list, cellSetType);
        }
        return null;
    }

    private static List<byte[]> getDataList(NodeList nl) {
        if (nl != null) {
            List<byte[]> result = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i).getChildNodes().item(i);
                if (node != null && "Data".equals(node.getNodeName()) && node.getNodeValue() != null) {
                    result.add(node.getNodeValue().getBytes(StandardCharsets.UTF_8));
                }
            }
            return result;
        }
        return null;
    }

    private static ValueR getValue(NodeList nl) {
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i).getChildNodes().item(i);
                if (node != null && "Value".equals(node.getNodeName())) {
                    return new ValueR(getCellTypeErrorList(node.getChildNodes()));
                }
            }
        }
        return null;
    }

    private static List<CellTypeError> getCellTypeErrorList(NodeList nl) {
        if (nl != null) {
            List<CellTypeError> list = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && "Error".equals(node.getNodeName())) {
                    NamedNodeMap nm = node.getAttributes();
                    list.add(new CellTypeErrorR(getLong(getAttribute(nm, "ErrorCode")), getAttribute(nm,
                        DESCRIPTION1)));
                }
            }
            return list;
        }
        return null;
    }

    private static AxesR getAxes(NodeList nl) {
        if (nl != null) {
            List<Axis> list = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && "Axis".equals(node.getNodeName())) {
                    list.add(new AxisR(getMembersList(node.getChildNodes()), getAttribute(node.getAttributes(), "name"
                    )));
                }
            }
            return new AxesR(list);
        }
        return null;
    }

    private static List<Type> getMembersList(NodeList nl) {

        if (nl != null) {
            List<Type> list = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && "Members".equals(node.getNodeName())) {
                    list.add(new MembersTypeR(getMemberList(node.getChildNodes()), getAttribute(node.getAttributes(),
                        "Hierarchy")));
                }
            }
            return list;
        }
        return null;
    }

    private static List<MemberType> getMemberList(NodeList nl) {
        if (nl != null) {
            List<MemberType> list = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && "Member".equals(node.getNodeName())) {
                    list.add(new MemberTypeR(getCellInfoItem(node.getChildNodes()), getAttribute(node.getAttributes()
                        , "Hierarchy")));
                }
            }
            return list;
        }
        return null;
    }

    private static OlapInfoR getOlapInfo(NodeList olapInfoNl) {
        if (olapInfoNl != null && olapInfoNl.getLength() > 0) {
            return getOlapInfo(olapInfoNl.item(0));
        }
        return null;
    }

    private static OlapInfoR getOlapInfo(Node item) {
        NodeList cn = item.getChildNodes();
        CubeInfoR cubeInfo = null;
        AxesInfoR axesInfo = null;
        CellInfoR cellInfo = null;

        if (cn != null) {
            for (int i = 0; i < cn.getLength(); i++) {
                Node n = cn.item(i);
                if (n != null) {
                    if ("CubeInfo".equals(n.getNodeName())) {
                        cubeInfo = getCubeInfo(n);
                    }
                    if ("AxesInfo".equals(n.getNodeName())) {
                        axesInfo = getAxesInfo(n);
                    }
                    if ("CellInfo".equals(n.getNodeName())) {
                        cellInfo = getCellInfo(n);
                    }
                }
            }
        }
        return new OlapInfoR(
            cubeInfo,
            axesInfo,
            cellInfo
        );
    }

    private static CellInfoR getCellInfo(Node n) {
        return new CellInfoR(getCellInfoItem(n.getChildNodes()));
    }

    private static AxesInfoR getAxesInfo(Node n) {
        if (n != null) {
            NodeList nl = n.getChildNodes();
            if (nl != null && n.getChildNodes().getLength() > 0) {
                List<AxisInfo> list = new ArrayList<>();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = n.getChildNodes().item(i);

                    if ("AxisInfo".equals(node.getNodeName())) {
                        String name = getAttribute(node.getAttributes(), "name");
                        List<HierarchyInfo> hierarchyInfoList = getHierarchyInfo(node.getChildNodes());
                        list.add(
                            new AxisInfoR(
                                hierarchyInfoList,
                                name
                            )
                        );
                    }
                }
                return new AxesInfoR(list);
            }
        }
        return null;
    }

    private static List<HierarchyInfo> getHierarchyInfo(NodeList nl) {
        if (nl != null) {
            List<HierarchyInfo> list = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i).getChildNodes().item(i);
                if (node != null && "HierarchyInfo".equals(node.getNodeName())) {
                    NamedNodeMap namedNodeMap = node.getAttributes();
                    String name = getAttribute(namedNodeMap, "name");
                    list.add(new HierarchyInfoR(getCellInfoItem(node.getChildNodes()), name));
                }
            }
        }
        return null;
    }

    private static List<CellInfoItem> getCellInfoItem(NodeList nl) {
        if (nl != null) {
            List<CellInfoItem> list = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i).getChildNodes().item(i);
                if (node != null) {
                    NamedNodeMap namedNodeMap = node.getAttributes();
                    String name = getAttribute(namedNodeMap, "name");
                    String type = getAttribute(namedNodeMap, "type");
                    list.add(new CellInfoItemR(
                        node.getNodeName(),
                        name,
                        Optional.ofNullable(type)
                    ));
                }
            }
        }
        return null;
    }

    private static String getAttribute(NamedNodeMap namedNodeMap, String name) {
        if (namedNodeMap != null) {
            Node nameNode = namedNodeMap.getNamedItem(name);
            if (nameNode != null) {
                return nameNode.getNodeValue();
            }
        }
        return null;
    }

    private static CubeInfoR getCubeInfo(Node n) {
        if (n != null) {
            NodeList nl = n.getChildNodes();
            if (nl != null && n.getChildNodes().getLength() > 0) {
                List<OlapInfoCube> list = new ArrayList<>();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = n.getChildNodes().item(i);
                    if ("Cube".equals(node.getNodeName())) {
                        Map<String, String> m = getMapValues(node.getChildNodes());
                        list.add(
                            new OlapInfoCubeR(
                                m.get("CubeName"),
                                getInstant(m.get("LastDataUpdate")),
                                getInstant(m.get("LastSchemaUpdate"))
                            )
                        );
                    }
                }
                return new CubeInfoR(list);
            }
        }
        return null;
    }

    private static List<MeasureGroupDimension> convertToMeasureGroupDimensionList(Node node) {
        List<MeasureGroupDimension> result = new ArrayList<>();
        NodeList nl = node.getChildNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (DIMENSION_PATH.equals(n.getNodeName())) {
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
                if (MEASURE_GROUP_DIMENSION.equals(n.getNodeName())) {
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
                            m.get(DESCRIPTION_UC),
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

    private static Instant getInstant(String value) {
        if (value != null) {
            return Instant.parse(value);
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

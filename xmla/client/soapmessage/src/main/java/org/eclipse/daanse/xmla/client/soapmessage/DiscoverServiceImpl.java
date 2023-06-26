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

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.eclipse.daanse.xmla.api.discover.DiscoverService;
import org.eclipse.daanse.xmla.api.discover.Properties;
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsRestrictions;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsRestrictions;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesRestrictions;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataRestrictions;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesRestrictions;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesRestrictions;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoRestrictions;
import org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRequest;
import org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions;
import org.eclipse.daanse.xmla.api.discover.discover.enumerators.DiscoverEnumeratorsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.enumerators.DiscoverEnumeratorsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.enumerators.DiscoverEnumeratorsRestrictions;
import org.eclipse.daanse.xmla.api.discover.discover.keywords.DiscoverKeywordsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.keywords.DiscoverKeywordsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.keywords.DiscoverKeywordsRestrictions;
import org.eclipse.daanse.xmla.api.discover.discover.literals.DiscoverLiteralsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.literals.DiscoverLiteralsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.literals.DiscoverLiteralsRestrictions;
import org.eclipse.daanse.xmla.api.discover.discover.properties.DiscoverPropertiesRequest;
import org.eclipse.daanse.xmla.api.discover.discover.properties.DiscoverPropertiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.properties.DiscoverPropertiesRestrictions;
import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsRestrictions;
import org.eclipse.daanse.xmla.api.discover.discover.xmlmetadata.DiscoverXmlMetaDataRequest;
import org.eclipse.daanse.xmla.api.discover.discover.xmlmetadata.DiscoverXmlMetaDataResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.xmlmetadata.DiscoverXmlMetaDataRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.actions.MdSchemaActionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.actions.MdSchemaActionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.actions.MdSchemaActionsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.cubes.MdSchemaCubesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.cubes.MdSchemaCubesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.cubes.MdSchemaCubesRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.demensions.MdSchemaDimensionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.demensions.MdSchemaDimensionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.demensions.MdSchemaDimensionsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.hierarchies.MdSchemaHierarchiesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.hierarchies.MdSchemaHierarchiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.hierarchies.MdSchemaHierarchiesRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.levels.MdSchemaLevelsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.levels.MdSchemaLevelsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.levels.MdSchemaLevelsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroups.MdSchemaMeasureGroupsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroups.MdSchemaMeasureGroupsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroups.MdSchemaMeasureGroupsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.measures.MdSchemaMeasuresRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measures.MdSchemaMeasuresResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measures.MdSchemaMeasuresRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.members.MdSchemaMembersRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.members.MdSchemaMembersResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.members.MdSchemaMembersRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.properties.MdSchemaPropertiesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.properties.MdSchemaPropertiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.properties.MdSchemaPropertiesRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.sets.MdSchemaSetsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.sets.MdSchemaSetsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.sets.MdSchemaSetsRestrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions.RESTRICTIONS_AUTHENTICATION_MODE;
import static org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions.RESTRICTIONS_DATA_SOURCE_DESCRIPTION;
import static org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions.RESTRICTIONS_DATA_SOURCE_INFO;
import static org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions.RESTRICTIONS_DATA_SOURCE_NAME;
import static org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions.RESTRICTIONS_PROVIDER_NAME;
import static org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions.RESTRICTIONS_PROVIDER_TYPE;
import static org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRestrictions.RESTRICTIONS_URL;
import static org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisRestrictions.RESTRICTIONS_KPI_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DBSCHEMA_CATALOGS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DBSCHEMA_COLUMNS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DBSCHEMA_PROVIDER_TYPES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DBSCHEMA_SCHEMATA;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DBSCHEMA_SOURCE_TABLES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DBSCHEMA_TABLES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DBSCHEMA_TABLES_INFO;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER_DATASOURCES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER_ENUMERATORS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER_KEYWORDS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER_LITERALS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER_PROPERTIES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER_SCHEMA_ROWSETS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISCOVER_XML_METADATA;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_ACTIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_CUBES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_DIMENSIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_FUNCTIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_HIERARCHIES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_KPIS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_LEVELS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_MEASUREGROUPS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_MEASUREGROUP_DIMENSIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_MEASURES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_MEMBERS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MDSCHEMA_SETS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTIES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_LIST;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.REQUEST_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_ACTION_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_ACTION_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_AGGREGATION_DESIGN_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_ASSEMBLY_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_BASE_CUBE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_BEST_MATCH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_CATALOG_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_COLUMN_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_COLUMN_OLAP_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_COORDINATE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_COORDINATE_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_CUBE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_CUBE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_CUBE_PERMISSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_CUBE_SOURCE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DATABASE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DATABASE_PERMISSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DATA_SOURCE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DATA_SOURCE_PERMISSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DATA_SOURCE_VIEW_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DATA_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DIMENSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DIMENSION_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DIMENSION_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_DIMENSION_VISIBILITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_ENUM_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_HIERARCHY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_HIERARCHY_ORIGIN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_HIERARCHY_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_HIERARCHY_VISIBILITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_INTERFACE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_INVOCATION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_KEYWORD;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_LEVEL_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_LEVEL_NUMBER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_LEVEL_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_LEVEL_VISIBILITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_LIBRARY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_LITERAL_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MDX_SCRIPT_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEASUREGROUP_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEASURE_GROUP_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEASURE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEASURE_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEASURE_VISIBILITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEMBER_CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEMBER_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEMBER_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MEMBER_UNIQUE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MINING_MODEL_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MINING_MODEL_PERMISSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MINING_STRUCTURE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_MINING_STRUCTURE_PERMISSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_OBJECT_EXPANSION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_ORIGIN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_PARTITION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_PERMISSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_PERSPECTIVE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_PROPERTY_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_PROPERTY_ORIGIN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_PROPERTY_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_PROPERTY_VISIBILITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_ROLE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_SCHEMA_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_SCHEMA_OWNER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_SCOPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_SET_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_TABLE_CATALOG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_TABLE_NAME;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_TABLE_SCHEMA;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_TABLE_TYPE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_TRACE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTIONS_TREE_OP;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.RESTRICTION_LIST;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SOAP_ACTION_DISCOVER;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDbSchemaCatalogsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDbSchemaColumnsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDbSchemaProviderTypesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDbSchemaSchemataResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDbSchemaSourceTablesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDbSchemaTablesInfoResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDbSchemaTablesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDiscoverDataSourcesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDiscoverEnumeratorsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDiscoverKeywordsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDiscoverLiteralsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDiscoverPropertiesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDiscoverSchemaRowsetsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToDiscoverXmlMetaDataResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaActionsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaCubesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaDimensionsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaFunctionsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaHierarchiesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaKpisResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaLevelsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaMeasureGroupDimensionsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaMeasureGroupsResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaMeasuresResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaMembersResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaPropertiesResponseRow;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToMdSchemaSetsResponseRow;

public class DiscoverServiceImpl extends AbstractService implements DiscoverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoverServiceImpl.class);
    private SoapClient soapClient;

    public DiscoverServiceImpl(SoapClient soapClient) {
        this.soapClient = soapClient;
    }

    @Override
    public List<DbSchemaCatalogsResponseRow> dbSchemaCatalogs(DbSchemaCatalogsRequest dbSchemaCatalogsRequest) {

        try {
            Consumer<SOAPMessage> msg = getConsumer(dbSchemaCatalogsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDbSchemaCatalogsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dbSchemaCatalogs error", e);
        }
        return List.of();
    }

    @Override
    public List<DbSchemaTablesResponseRow> dbSchemaTables(DbSchemaTablesRequest dbSchemaTablesRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(dbSchemaTablesRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDbSchemaTablesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dbSchemaTables error", e);
        }
        return List.of();
    }

    @Override
    public List<DiscoverEnumeratorsResponseRow> discoverEnumerators(
        DiscoverEnumeratorsRequest discoverEnumeratorsRequest
    ) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(discoverEnumeratorsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDiscoverEnumeratorsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService discoverEnumerators error", e);
        }
        return List.of();
    }

    @Override
    public List<DiscoverKeywordsResponseRow> discoverKeywords(DiscoverKeywordsRequest discoverKeywordsRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(discoverKeywordsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDiscoverKeywordsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService discoverKeywords error", e);
        }
        return List.of();
    }

    @Override
    public List<DiscoverLiteralsResponseRow> discoverLiterals(DiscoverLiteralsRequest discoverLiteralsRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(discoverLiteralsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDiscoverLiteralsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService discoverLiterals error", e);
        }
        return List.of();
    }

    @Override
    public List<DiscoverPropertiesResponseRow> discoverProperties(DiscoverPropertiesRequest discoverPropertiesRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(discoverPropertiesRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDiscoverPropertiesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService discoverProperties error", e);
        }
        return List.of();
    }

    @Override
    public List<DiscoverSchemaRowsetsResponseRow> discoverSchemaRowsets(
        DiscoverSchemaRowsetsRequest discoverSchemaRowsetsRequest
    ) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(discoverSchemaRowsetsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDiscoverSchemaRowsetsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService discoverProperties error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaActionsResponseRow> mdSchemaActions(MdSchemaActionsRequest mdSchemaActionsRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(mdSchemaActionsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaActionsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaCubes error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaCubesResponseRow> mdSchemaCubes(MdSchemaCubesRequest mdSchemaCubesRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(mdSchemaCubesRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaCubesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaCubes error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaDimensionsResponseRow> mdSchemaDimensions(MdSchemaDimensionsRequest mdSchemaDimensionsRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(mdSchemaDimensionsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaDimensionsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaFunctions error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaFunctionsResponseRow> mdSchemaFunctions(MdSchemaFunctionsRequest mdSchemaFunctionsRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(mdSchemaFunctionsRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaFunctionsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaFunctions error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaHierarchiesResponseRow> mdSchemaHierarchies(MdSchemaHierarchiesRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaHierarchiesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dataSources error", e);
        }
        return List.of();
    }

    @Override
    public List<DiscoverDataSourcesResponseRow> dataSources(DiscoverDataSourcesRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDiscoverDataSourcesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dataSources error", e);
        }
        return List.of();
    }

    @Override
    public List<DiscoverXmlMetaDataResponseRow> xmlMetaData(DiscoverXmlMetaDataRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDiscoverXmlMetaDataResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService xmlMetaData error", e);
        }
        return List.of();
    }

    @Override
    public List<DbSchemaColumnsResponseRow> dbSchemaColumns(DbSchemaColumnsRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDbSchemaColumnsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService xmlMetaData error", e);
        }
        return List.of();
    }

    @Override
    public List<DbSchemaProviderTypesResponseRow> dbSchemaProviderTypes(DbSchemaProviderTypesRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDbSchemaProviderTypesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dbSchemaProviderTypes error", e);
        }
        return List.of();
    }

    @Override
    public List<DbSchemaSchemataResponseRow> dbSchemaSchemata(DbSchemaSchemataRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDbSchemaSchemataResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dbSchemaSchemata error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaLevelsResponseRow> mdSchemaLevels(MdSchemaLevelsRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaLevelsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaLevels error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaMeasureGroupDimensionsResponseRow> mdSchemaMeasureGroupDimensions(
        MdSchemaMeasureGroupDimensionsRequest requestApi
    ) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaMeasureGroupDimensionsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaMeasureGroupDimensions error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaMeasuresResponseRow> mdSchemaMeasures(MdSchemaMeasuresRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaMeasuresResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaMeasureGroupDimensions error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaMembersResponseRow> mdSchemaMembers(MdSchemaMembersRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaMembersResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaMembers error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaPropertiesResponseRow> mdSchemaProperties(MdSchemaPropertiesRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaPropertiesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaProperties error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaSetsResponseRow> mdSchemaSets(MdSchemaSetsRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaSetsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaProperties error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaKpisResponseRow> mdSchemaKpis(MdSchemaKpisRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaKpisResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaKpis error", e);
        }
        return List.of();
    }

    @Override
    public List<MdSchemaMeasureGroupsResponseRow> mdSchemaMeasureGroups(MdSchemaMeasureGroupsRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToMdSchemaMeasureGroupsResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService mdSchemaMeasureGroups error", e);
        }
        return List.of();
    }

    @Override
    public List<DbSchemaSourceTablesResponseRow> dbSchemaSourceTables(DbSchemaSourceTablesRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDbSchemaSourceTablesResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dbSchemaSourceTables error", e);
        }
        return List.of();

    }

    @Override
    public List<DbSchemaTablesInfoResponseRow> dbSchemaTablesInfo(DbSchemaTablesInfoRequest requestApi) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(requestApi);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_DISCOVER), msg);
            return convertToDbSchemaTablesInfoResponseRow(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("DiscoverService dbSchemaTablesInfo error", e);
        }
        return List.of();
    }

    private Consumer<SOAPMessage> getConsumer(DbSchemaTablesInfoRequest requestApi) {
        return message -> {
            try {
                DbSchemaTablesInfoRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_TABLES_INFO);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                addChildElement(restrictionList, RESTRICTIONS_TABLE_NAME, dr.tableName());
                addChildElement(restrictionList, RESTRICTIONS_TABLE_TYPE, dr.tableType().getValue());

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DbSchemaTablesInfoRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DbSchemaSourceTablesRequest requestApi) {
        return message -> {
            try {
                DbSchemaSourceTablesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_SOURCE_TABLES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_CATALOG, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                addChildElement(restrictionList, RESTRICTIONS_TABLE_NAME, dr.tableName());
                addChildElement(restrictionList, RESTRICTIONS_TABLE_TYPE, dr.tableType().getValue());

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DbSchemaSourceTablesRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaMeasureGroupsRequest requestApi) {
        return message -> {
            try {
                MdSchemaMeasureGroupsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_MEASUREGROUPS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_CATALOG, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.measureGroupName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEASUREGROUP_NAME,
                    v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaMeasureGroupsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaKpisRequest requestApi) {
        return message -> {
            try {
                MdSchemaKpisRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_KPIS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.kpiName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_KPI_NAME, v));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaMeasureGroupsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaSetsRequest requestApi) {
        return message -> {
            try {
                MdSchemaSetsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_SETS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.setName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SET_NAME, v));
                dr.scope().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCOPE,
                    String.valueOf(v.getValue())));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));
                dr.hierarchyUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_HIERARCHY_UNIQUE_NAME, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaPropertiesRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaPropertiesRequest requestApi) {
        return message -> {
            try {
                MdSchemaPropertiesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_TABLES_INFO);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.dimensionUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_UNIQUE_NAME, v));
                dr.hierarchyUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_HIERARCHY_UNIQUE_NAME, v));
                dr.levelUniqueName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LEVEL_UNIQUE_NAME,
                    v));
                dr.memberUniqueName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEMBER_UNIQUE_NAME
                    , v));
                dr.propertyType().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PROPERTY_TYPE,
                    String.valueOf(v.getValue())));
                dr.propertyName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PROPERTY_NAME, v));
                dr.propertyOrigin().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PROPERTY_ORIGIN,
                    String.valueOf(v.getValue())));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));
                dr.propertyVisibility().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_PROPERTY_VISIBILITY, String.valueOf(v)));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaPropertiesRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaMembersRequest requestApi) {
        return message -> {
            try {
                MdSchemaMembersRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_MEMBERS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.dimensionUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_UNIQUE_NAME, v));
                dr.hierarchyUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_HIERARCHY_UNIQUE_NAME, v));
                dr.levelUniqueName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LEVEL_UNIQUE_NAME,
                    v));
                dr.levelNumber().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LEVEL_NUMBER,
                    String.valueOf(v)));
                dr.memberName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEMBER_NAME, v));
                dr.memberUniqueName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEMBER_UNIQUE_NAME
                    , v));
                dr.memberType().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEMBER_TYPE,
                    String.valueOf(v.getValue())));
                dr.memberCaption().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEMBER_CAPTION, v));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));
                dr.treeOp().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TREE_OP,
                    String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaMeasuresRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaMeasuresRequest requestApi) {
        return message -> {
            try {
                MdSchemaMeasuresRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_MEASURES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.measureName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEASURE_NAME, v));
                dr.measureUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_MEASURE_UNIQUE_NAME, v));
                dr.measureGroupName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEASUREGROUP_NAME,
                    v));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEASURE_VISIBILITY,
                    String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaMeasuresRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaMeasureGroupDimensionsRequest requestApi) {
        return message -> {
            try {
                MdSchemaMeasureGroupDimensionsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_MEASUREGROUP_DIMENSIONS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.measureGroupName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEASUREGROUP_NAME,
                    v));
                dr.dimensionUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_UNIQUE_NAME, v));
                dr.dimensionVisibility().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_VISIBILITY, String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaLevelsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaLevelsRequest requestApi) {
        return message -> {
            try {
                MdSchemaLevelsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_LEVELS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.dimensionUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_UNIQUE_NAME, v));
                dr.hierarchyUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_HIERARCHY_UNIQUE_NAME, v));
                dr.levelName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LEVEL_NAME, v));
                dr.levelUniqueName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LEVEL_UNIQUE_NAME,
                    v));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));
                dr.levelVisibility().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LEVEL_VISIBILITY,
                    String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaLevelsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DbSchemaSchemataRequest requestApi) {
        return message -> {
            try {
                DbSchemaSchemataRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_SCHEMATA);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, dr.catalogName());
                addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, dr.schemaName());
                addChildElement(restrictionList, RESTRICTIONS_SCHEMA_OWNER, dr.schemaOwner());

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DbSchemaSchemataRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DbSchemaProviderTypesRequest requestApi) {
        return message -> {
            try {
                DbSchemaProviderTypesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_PROVIDER_TYPES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.dataType().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DATA_TYPE,
                    String.valueOf(v.getValue())));
                dr.bestMatch().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_BEST_MATCH,
                    String.valueOf(v)));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DbSchemaColumnsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DbSchemaColumnsRequest requestApi) {
        return message -> {
            try {
                DbSchemaColumnsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_COLUMNS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.tableCatalog().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_CATALOG, v));
                dr.tableSchema().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_SCHEMA, v));
                dr.tableName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_NAME, v));
                dr.tableName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_COLUMN_NAME, v));
                dr.tableName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_COLUMN_OLAP_TYPE, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DbSchemaColumnsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DiscoverXmlMetaDataRequest requestApi) {
        return message -> {
            try {
                DiscoverXmlMetaDataRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DISCOVER_XML_METADATA);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.databaseId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DATABASE_ID, v));
                dr.dimensionId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DIMENSION_ID, v));
                dr.cubeId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_ID, v));
                dr.measureGroupId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MEASURE_GROUP_ID, v));
                dr.partitionId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PARTITION_ID, v));
                dr.perspectiveId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PERSPECTIVE_ID, v));
                dr.dimensionPermissionId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PERMISSION_ID
                    , v));
                dr.roleId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_ROLE_ID, v));
                dr.dataSourcePermissionId().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DATABASE_PERMISSION_ID, v));
                dr.miningModelId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MINING_MODEL_ID, v));
                dr.miningModelPermissionId().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_MINING_MODEL_PERMISSION_ID, v));
                dr.dataSourceId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DATA_SOURCE_ID, v));
                dr.miningStructureId().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_MINING_STRUCTURE_ID, v));
                dr.aggregationDesignId().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_AGGREGATION_DESIGN_ID, v));
                dr.traceId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TRACE_ID, v));
                dr.miningStructurePermissionId().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_MINING_STRUCTURE_PERMISSION_ID, v));
                dr.cubePermissionId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_PERMISSION_ID
                    , v));
                dr.assemblyId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_ASSEMBLY_ID, v));
                dr.mdxScriptId().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_MDX_SCRIPT_ID, v));
                dr.dataSourceViewId().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DATA_SOURCE_VIEW_ID, v));
                dr.dataSourcePermissionId().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DATA_SOURCE_PERMISSION_ID, v));
                dr.objectExpansion().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_OBJECT_EXPANSION,
                    v.getValue()));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DiscoverSchemaRowsetsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DiscoverSchemaRowsetsRequest requestApi) {
        return message -> {
            try {
                DiscoverSchemaRowsetsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DISCOVER_SCHEMA_ROWSETS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DiscoverSchemaRowsetsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DiscoverLiteralsRequest requestApi) {
        return message -> {
            try {
                DiscoverLiteralsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DISCOVER_LITERALS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.literalName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LITERAL_NAME, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DiscoverLiteralsRequest accept error", e);
            }

        };
    }

    private Consumer<SOAPMessage> getConsumer(DiscoverPropertiesRequest requestApi) {
        return message -> {
            try {
                DiscoverPropertiesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DISCOVER_PROPERTIES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.propertyName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PROPERTY_NAME, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DiscoverKeywordsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DiscoverKeywordsRequest requestApi) {
        return message -> {
            try {
                DiscoverKeywordsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DISCOVER_KEYWORDS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.keyword().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_KEYWORD, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DiscoverKeywordsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DiscoverEnumeratorsRequest requestApi) {
        return message -> {
            try {
                DiscoverEnumeratorsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DISCOVER_ENUMERATORS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.enumName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_ENUM_NAME, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaActionsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DbSchemaTablesRequest requestApi) {
        return message -> {
            try {
                DbSchemaTablesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_TABLES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.tableCatalog().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_CATALOG, v));
                dr.tableSchema().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_SCHEMA, v));
                dr.tableName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_NAME, v));
                dr.tableType().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_TABLE_TYPE, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DbSchemaTablesRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DbSchemaCatalogsRequest requestApi) {
        return message -> {
            try {
                DbSchemaCatalogsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DBSCHEMA_CATALOGS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DbSchemaCatalogsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaActionsRequest requestApi) {
        return message -> {
            try {
                MdSchemaActionsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_ACTIONS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                if (dr.cubeName() != null) {
                    addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, dr.cubeName());
                }
                dr.actionName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_ACTION_NAME, v));
                dr.actionType().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_ACTION_TYPE,
                    String.valueOf(v.getValue())));
                dr.coordinate().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_COORDINATE, v));
                if (dr.coordinateType() != null) {
                    addChildElement(restrictionList, RESTRICTIONS_COORDINATE_TYPE,
                        String.valueOf(dr.coordinateType().getValue()));
                }
                if (dr.invocation() != null) {
                    addChildElement(restrictionList, RESTRICTIONS_INVOCATION,
                        String.valueOf(dr.invocation().getValue()));
                }
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaActionsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaCubesRequest requestApi) {
        return message -> {
            try {
                MdSchemaCubesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_CUBES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                if (dr.catalogName() != null) {
                    addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, dr.catalogName());
                }
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.baseCubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_BASE_CUBE_NAME, v));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaCubesRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaDimensionsRequest requestApi) {
        return message -> {
            try {
                MdSchemaDimensionsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_DIMENSIONS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);
                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.dimensionName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DIMENSION_NAME, v));
                dr.dimensionUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_UNIQUE_NAME, v));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));
                dr.dimensionVisibility().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_VISIBILITY
                    , String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaDimensionsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaFunctionsRequest requestApi) {
        return message -> {
            try {
                MdSchemaFunctionsRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_FUNCTIONS);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.origin().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_ORIGIN,
                    String.valueOf(v.getValue())));
                dr.interfaceName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_INTERFACE_NAME,
                    v.name()));
                dr.libraryName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_LIBRARY_NAME, v));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaFunctionsRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(MdSchemaHierarchiesRequest requestApi) {
        return message -> {
            try {
                MdSchemaHierarchiesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(MDSCHEMA_HIERARCHIES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);

                dr.catalogName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CATALOG_NAME, v));
                dr.schemaName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_SCHEMA_NAME, v));
                dr.cubeName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_NAME, v));
                dr.dimensionUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_DIMENSION_UNIQUE_NAME, v));
                dr.hierarchyName().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_HIERARCHY_NAME, v));
                dr.hierarchyUniqueName().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_HIERARCHY_UNIQUE_NAME, v));
                dr.hierarchyOrigin().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_HIERARCHY_ORIGIN, String.valueOf(v)));
                dr.cubeSource().ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_CUBE_SOURCE,
                    String.valueOf(v.getValue())));
                dr.hierarchyVisibility().ifPresent(v -> addChildElement(restrictionList,
                    RESTRICTIONS_HIERARCHY_VISIBILITY, String.valueOf(v.getValue())));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService MdSchemaHierarchiesRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(DiscoverDataSourcesRequest requestApi) {
        return message -> {
            try {
                DiscoverDataSourcesRestrictions dr = requestApi.restrictions();
                Properties properties = requestApi.properties();
                SOAPElement discover = message.getSOAPBody()
                    .addChildElement(DISCOVER);
                discover.addChildElement(REQUEST_TYPE).setTextContent(DISCOVER_DATASOURCES);
                SOAPElement restrictionList = discover.addChildElement(RESTRICTIONS)
                    .addChildElement(RESTRICTION_LIST);
                addChildElement(restrictionList, RESTRICTIONS_DATA_SOURCE_NAME, dr.dataSourceName());
                dr.dataSourceDescription()
                    .ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DATA_SOURCE_DESCRIPTION, v));
                dr.dataSourceDescription()
                    .ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DATA_SOURCE_DESCRIPTION, v));
                dr.url()
                    .ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_URL, v));
                dr.dataSourceInfo()
                    .ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_DATA_SOURCE_INFO, v));
                if (dr.providerName() != null) {
                    addChildElement(restrictionList, RESTRICTIONS_PROVIDER_NAME, dr.providerName());
                }
                dr.providerType()
                    .ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_PROVIDER_TYPE, v.name()));
                dr.authenticationMode()
                    .ifPresent(v -> addChildElement(restrictionList, RESTRICTIONS_AUTHENTICATION_MODE, v.name()));

                SOAPElement propertyList = discover.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("DiscoverService DiscoverDataSourcesRequest accept error", e);
            }
        };
    }

}

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
package org.eclipse.daanse.xmla.ws.jakarta.basic;

import static org.eclipse.daanse.xmla.ws.jakarta.basic.CommandConvertor.convertCancel;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CommandConvertor.convertClearCache;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.ConvertorUtil.convertException;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.ConvertorUtil.convertMessages;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.xmla.api.common.enums.ActionTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.AuthenticationModeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ColumnOlapTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.CoordinateTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.CubeSourceEnum;
import org.eclipse.daanse.xmla.api.common.enums.InterfaceNameEnum;
import org.eclipse.daanse.xmla.api.common.enums.InvocationEnum;
import org.eclipse.daanse.xmla.api.common.enums.LevelDbTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.MemberTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ObjectExpansionEnum;
import org.eclipse.daanse.xmla.api.common.enums.OriginEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyOriginEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ProviderTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ScopeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TableTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TreeOpEnum;
import org.eclipse.daanse.xmla.api.common.enums.VisibilityEnum;
import org.eclipse.daanse.xmla.api.common.properties.AxisFormat;
import org.eclipse.daanse.xmla.api.common.properties.Content;
import org.eclipse.daanse.xmla.api.common.properties.Format;
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
import org.eclipse.daanse.xmla.api.execute.ExecuteParameter;
import org.eclipse.daanse.xmla.api.execute.alter.AlterRequest;
import org.eclipse.daanse.xmla.api.execute.alter.AlterResponse;
import org.eclipse.daanse.xmla.api.execute.cancel.CancelRequest;
import org.eclipse.daanse.xmla.api.execute.cancel.CancelResponse;
import org.eclipse.daanse.xmla.api.execute.clearcache.ClearCacheRequest;
import org.eclipse.daanse.xmla.api.execute.clearcache.ClearCacheResponse;
import org.eclipse.daanse.xmla.api.execute.statement.StatementRequest;
import org.eclipse.daanse.xmla.api.execute.statement.StatementResponse;
import org.eclipse.daanse.xmla.api.mddataset.Mddataset;
import org.eclipse.daanse.xmla.api.xmla.Cancel;
import org.eclipse.daanse.xmla.api.xmla.ClearCache;
import org.eclipse.daanse.xmla.api.xmla_empty.Emptyresult;
import org.eclipse.daanse.xmla.model.record.discover.PropertiesR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.catalogs.DbSchemaCatalogsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.catalogs.DbSchemaCatalogsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.columns.DbSchemaColumnsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.columns.DbSchemaColumnsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.providertypes.DbSchemaProviderTypesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.providertypes.DbSchemaProviderTypesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.schemata.DbSchemaSchemataRequestR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.schemata.DbSchemaSchemataRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.sourcetables.DbSchemaSourceTablesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.sourcetables.DbSchemaSourceTablesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.tables.DbSchemaTablesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.tables.DbSchemaTablesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.tablesinfo.DbSchemaTablesInfoRequestR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.tablesinfo.DbSchemaTablesInfoRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discover.datasources.DiscoverDataSourcesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discover.datasources.DiscoverDataSourcesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discover.enumerators.DiscoverEnumeratorsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discover.enumerators.DiscoverEnumeratorsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discover.keywords.DiscoverKeywordsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discover.keywords.DiscoverKeywordsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discover.literals.DiscoverLiteralsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discover.literals.DiscoverLiteralsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discover.properties.DiscoverPropertiesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discover.properties.DiscoverPropertiesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discover.schemarowsets.DiscoverSchemaRowsetsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discover.schemarowsets.DiscoverSchemaRowsetsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discover.xmlmetadata.DiscoverXmlMetaDataRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discover.xmlmetadata.DiscoverXmlMetaDataRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.actions.MdSchemaActionsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.actions.MdSchemaActionsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.cubes.MdSchemaCubesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.cubes.MdSchemaCubesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.demensions.MdSchemaDimensionsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.demensions.MdSchemaDimensionsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.functions.MdSchemaFunctionsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.functions.MdSchemaFunctionsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.hierarchies.MdSchemaHierarchiesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.hierarchies.MdSchemaHierarchiesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.kpis.MdSchemaKpisRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.kpis.MdSchemaKpisRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.levels.MdSchemaLevelsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.levels.MdSchemaLevelsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measuregroups.MdSchemaMeasureGroupsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measuregroups.MdSchemaMeasureGroupsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measures.MdSchemaMeasuresRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.measures.MdSchemaMeasuresRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.members.MdSchemaMembersRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.members.MdSchemaMembersRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.properties.MdSchemaPropertiesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.properties.MdSchemaPropertiesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.sets.MdSchemaSetsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.sets.MdSchemaSetsRestrictionsR;
import org.eclipse.daanse.xmla.model.record.execute.ExecuteParameterR;
import org.eclipse.daanse.xmla.model.record.execute.alter.AlterRequestR;
import org.eclipse.daanse.xmla.model.record.execute.cancel.CancelRequestR;
import org.eclipse.daanse.xmla.model.record.execute.clearcache.ClearCacheRequestR;
import org.eclipse.daanse.xmla.model.record.execute.statement.StatementRequestR;
import org.eclipse.daanse.xmla.model.record.xmla.AlterR;
import org.eclipse.daanse.xmla.model.record.xmla.StatementR;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Discover;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DiscoverResponse;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Execute;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.ExecuteResponse;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.PropertyList;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Return;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.MeasureGroupDimensionXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.ParameterInfoXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.Row;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.Rowset;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.dbschema.DbSchemaCatalogsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.dbschema.DbSchemaColumnsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.dbschema.DbSchemaProviderTypesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.dbschema.DbSchemaSchemataResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.dbschema.DbSchemaSourceTablesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.dbschema.DbSchemaTablesInfoResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.dbschema.DbSchemaTablesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverDataSourcesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverEnumeratorsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverKeywordsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverLiteralsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverPropertiesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverSchemaRowsetsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverXmlMetaDataResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaActionsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaCubesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaDimensionsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaFunctionsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaHierarchiesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaKpisResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaLevelsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaMeasureGroupDimensionsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaMeasureGroupsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaMeasuresResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaMembersResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaPropertiesResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema.MdSchemaSetsResponseRowXml;
import org.eclipse.daanse.xmla.ws.jakarta.model.xsd.Schema;

import jakarta.xml.bind.JAXBException;

public class Convert {

	private Convert() {
	}
    private static Optional<Integer> localeIdentifier(PropertyList propertyList) {
        return Optional.ofNullable(propertyList.getLocaleIdentifier());
    }

    private static Optional<Content> content(PropertyList propertyList) {
        String content = propertyList.getContent();
        if (content != null) {
            return Optional.ofNullable(Content.fromValue(content));
        }
        return Optional.empty();
    }

    private static Optional<String> catalog(PropertyList propertyList) {
        return Optional.ofNullable(propertyList.getCatalog());
    }

    private static Optional<String> dataSourceInfo(PropertyList propertyList) {
        return Optional.ofNullable(propertyList.getDataSourceInfo());
    }

    private static Optional<Format> format(PropertyList propertyList) {
        String format = propertyList.getFormat();
        if (format != null) {
            return Optional.ofNullable(Format.fromValue(format));
        }
        return Optional.empty();
    }

    private static Optional<AxisFormat> axisFormat(PropertyList propertyList) {
        String format = propertyList.getAxisFormat();
        if (format != null) {
            return Optional.ofNullable(AxisFormat.fromValue(format));
        }
        return Optional.empty();
    }

    private static PropertyList propertyList(Discover requestWs) {
        return requestWs.getProperties()
                .getPropertyList();
    }

    private static PropertyList propertyList(Execute requestWs) {
        return requestWs.getProperties()
            .getPropertyList();
    }

    private static Map<String, String> restrictionsMap(Discover requestWs) {
        return requestWs.getRestrictions()
                .getRestrictionMap();
    }

    private static PropertiesR discoverProperties(PropertyList propertyList) {
        PropertiesR properties = new PropertiesR();
        properties.setLocaleIdentifier(localeIdentifier(propertyList));
        properties.setContent(content(propertyList));
        properties.setFormat(format(propertyList));
        properties.setAxisFormat(axisFormat(propertyList));
        properties.setDataSourceInfo(dataSourceInfo(propertyList));
        properties.setCatalog(catalog(propertyList));

        return properties;
    }

    private static DiscoverPropertiesRestrictionsR discoverPropertiesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String propertyName = map.get(DiscoverPropertiesRestrictions.RESTRICTIONS_PROPERTY_NAME);

        return new DiscoverPropertiesRestrictionsR(Optional.ofNullable(propertyName));
    }

    public static DiscoverPropertiesRequest fromDiscoverProperties(Discover requestWs) {

        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DiscoverPropertiesRestrictionsR restrictions = discoverPropertiesRestrictions(requestWs);

        return new DiscoverPropertiesRequestR(properties, restrictions);

    }

    public static DiscoverResponse toDiscoverProperties(List<DiscoverPropertiesResponseRow> responseApi)
            throws JAXBException, IOException {

        List<Row> rows = responseApi.stream()
                .map(Convert::convertDiscoverPropertiesResponseRow)
                .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DiscoverPropertiesResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverPropertiesResponseRow(DiscoverPropertiesResponseRow apiRow) {
        DiscoverPropertiesResponseRowXml row = new DiscoverPropertiesResponseRowXml();

        // Mandatory
        row.setPropertyName(apiRow.propertyName());
        row.setPropertyAccessType(apiRow.propertyAccessType());

        // Optional
        apiRow.propertyDescription()
                .ifPresent(row::setPropertyDescription);
        apiRow.propertyType()
                .ifPresent(row::setPropertyType);
        apiRow.required()
                .ifPresent(row::setRequired);
        apiRow.value()
                .ifPresent(row::setValue);

        return row;
    }

    public static DbSchemaCatalogsRequest fromDiscoverDbSchemaCatalogs(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DbSchemaCatalogsRestrictionsR restrictions = discoverDbSchemaCatalogsRestrictions(requestWs);

        return new DbSchemaCatalogsRequestR(properties, restrictions);
    }

    private static DbSchemaCatalogsRestrictionsR discoverDbSchemaCatalogsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(DbSchemaCatalogsRestrictions.RESTRICTIONS_CATALOG_NAME);
        return new DbSchemaCatalogsRestrictionsR(Optional.ofNullable(catalogName));
    }



    public static DiscoverResponse toDiscoverDbSchemaCatalogs(List<DbSchemaCatalogsResponseRow> responseApi)
        throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDbSchemaCatalogsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DbSchemaCatalogsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDbSchemaCatalogsResponseRow(DbSchemaCatalogsResponseRow apiRow) {
        DbSchemaCatalogsResponseRowXml row = new DbSchemaCatalogsResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.roles()
            .ifPresent(row::setRoles);
        apiRow.dateModified()
            .ifPresent(row::setDateModified);
        apiRow.compatibilityLevel()
            .ifPresent(row::setCompatibilityLevel);
        apiRow.type()
            .ifPresent(i -> row.setType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.TypeEnum.fromValue(i.getValue())));
        apiRow.version()
            .ifPresent(row::setVersion);
        apiRow.databaseId()
            .ifPresent(row::setDatabaseId);
        apiRow.dateQueried()
            .ifPresent(row::setDateQueried);
        apiRow.currentlyUsed()
            .ifPresent(row::setCurrentlyUsed);
        apiRow.popularity()
            .ifPresent(row::setPopularity);
        apiRow.weightedPopularity()
            .ifPresent(row::setWeightedPopularity);
        apiRow.clientCacheRefreshPolicy()
            .ifPresent(i -> row.setClientCacheRefreshPolicy(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ClientCacheRefreshPolicyEnum.fromValue(i.getValue())));

        return row;
    }

    public static DiscoverResponse toDiscoverSchemaRowsets(List<DiscoverSchemaRowsetsResponseRow> responseApi) throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverSchemaRowsetsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DiscoverSchemaRowsetsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverSchemaRowsetsResponseRow(DiscoverSchemaRowsetsResponseRow apiRow) {
        DiscoverSchemaRowsetsResponseRowXml row = new DiscoverSchemaRowsetsResponseRowXml();

        // Mandatory
        row.setSchemaName(apiRow.schemaName());

        // Optional
        apiRow.schemaGuid()
            .ifPresent(row::setSchemaGuid);
        apiRow.restrictions()
            .ifPresent(row::setRestrictions);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.restrictionsMask()
            .ifPresent(row::setRestrictionsMask);

        return row;
    }

    private static DiscoverSchemaRowsetsRestrictionsR discoverSchemaRowsetsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String schemaName = map.get(DiscoverSchemaRowsetsRestrictions.RESTRICTIONS_SCHEMA_NAME);

        return new DiscoverSchemaRowsetsRestrictionsR(Optional.ofNullable(schemaName));
    }

    public static DiscoverSchemaRowsetsRequest fromDiscoverSchemaRowsets(Discover requestWs) {

        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DiscoverSchemaRowsetsRestrictionsR restrictions = discoverSchemaRowsetsRestrictions(requestWs);

        return new DiscoverSchemaRowsetsRequestR(properties, restrictions);

    }

    public static DiscoverEnumeratorsRequest fromDiscoverEnumerators(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DiscoverEnumeratorsRestrictionsR restrictions = discoverEnumeratorsRestrictions(requestWs);
        return new DiscoverEnumeratorsRequestR(properties, restrictions);

    }

    private static DiscoverEnumeratorsRestrictionsR discoverEnumeratorsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String enumName = map.get(DiscoverEnumeratorsRestrictions.RESTRICTIONS_ENUM_NAME);

        return new DiscoverEnumeratorsRestrictionsR(Optional.ofNullable(enumName));
    }

    public static DiscoverResponse toDiscoverEnumerators(List<DiscoverEnumeratorsResponseRow> responseApi)
        throws JAXBException, IOException {

        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverEnumeratorsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DiscoverEnumeratorsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverEnumeratorsResponseRow(DiscoverEnumeratorsResponseRow apiRow) {
        DiscoverEnumeratorsResponseRowXml row = new DiscoverEnumeratorsResponseRowXml();

        // Mandatory
        row.setEnumName(apiRow.enumName());
        row.setEnumType(apiRow.enumType());
        row.setElementName(apiRow.elementName());

        // Optional
        apiRow.enumDescription()
            .ifPresent(row::setEnumDescription);
        apiRow.elementDescription()
            .ifPresent(row::setElementDescription);
        apiRow.elementValue()
            .ifPresent(row::setElementValue);

        return row;
    }

    public static DiscoverKeywordsRequest fromDiscoverKeywords(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DiscoverKeywordsRestrictionsR restrictions = discoverKeywordsRestrictions(requestWs);
        return new DiscoverKeywordsRequestR(properties, restrictions);
    }

    private static DiscoverKeywordsRestrictionsR discoverKeywordsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String keyword = map.get(DiscoverKeywordsRestrictions.RESTRICTIONS_KEYWORD);

        return new DiscoverKeywordsRestrictionsR(Optional.ofNullable(keyword));
    }

    public static DiscoverResponse toDiscoverKeywords(List<DiscoverKeywordsResponseRow> responseApi)
        throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverKeywordsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DiscoverKeywordsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverKeywordsResponseRow(DiscoverKeywordsResponseRow apiRow) {
        DiscoverKeywordsResponseRowXml row = new DiscoverKeywordsResponseRowXml();

        // Mandatory
        row.setKeyword(apiRow.keyword());

        return row;
    }

    public static DiscoverLiteralsRequest fromDiscoverLiterals(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DiscoverLiteralsRestrictionsR restrictions = discoverLiteralsRestrictions(requestWs);
        return new DiscoverLiteralsRequestR(properties, restrictions);
    }

    private static DiscoverLiteralsRestrictionsR discoverLiteralsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String keyword = map.get(DiscoverLiteralsRestrictions.RESTRICTIONS_LITERAL_NAME);

        return new DiscoverLiteralsRestrictionsR(Optional.ofNullable(keyword));
    }

    public static DiscoverResponse toDiscoverLiterals(List<DiscoverLiteralsResponseRow> responseApi)
        throws JAXBException, IOException {

        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverLiteralsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DiscoverLiteralsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverLiteralsResponseRow(DiscoverLiteralsResponseRow apiRow) {
        DiscoverLiteralsResponseRowXml row = new DiscoverLiteralsResponseRowXml();

        // Mandatory
        row.setLiteralName(apiRow.literalName());
        row.setLiteralValue(apiRow.literalValue());
        row.setLiteralInvalidChars(apiRow.literalInvalidChars());
        row.setLiteralInvalidStartingChars(apiRow.literalInvalidStartingChars());
        row.setLiteralMaxLength(apiRow.literalMaxLength());
        row.setLiteralNameValue(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LiteralNameEnumValueEnum.fromValue(apiRow.literalNameEnumValue().getValue()));

        return row;
    }

    public static DbSchemaTablesRequest fromDiscoverDbSchemaTables(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DbSchemaTablesRestrictionsR restrictions = discoverDbSchemaTablesRestrictions(requestWs);
        return new DbSchemaTablesRequestR(properties, restrictions);
    }

    private static DbSchemaTablesRestrictionsR discoverDbSchemaTablesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String tableCatalog = map.get(DbSchemaTablesRestrictions.RESTRICTIONS_TABLE_CATALOG);
        String tableSchema = map.get(DbSchemaTablesRestrictions.RESTRICTIONS_TABLE_SCHEMA);
        String tableName = map.get(DbSchemaTablesRestrictions.RESTRICTIONS_TABLE_NAME);
        String tableType = map.get(DbSchemaTablesRestrictions.RESTRICTIONS_TABLE_TYPE);

        return new DbSchemaTablesRestrictionsR(Optional.ofNullable(tableCatalog), Optional.ofNullable(tableSchema),
                Optional.ofNullable(tableName), Optional.ofNullable(tableType));
    }

    public static DiscoverResponse toDiscoverDbSchemaTables(List<DbSchemaTablesResponseRow> responseApi)
        throws JAXBException, IOException {

        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverDbSchemaTablesResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DbSchemaTablesResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverDbSchemaTablesResponseRow(DbSchemaTablesResponseRow apiRow) {
        DbSchemaTablesResponseRowXml row = new DbSchemaTablesResponseRowXml();

        // Optional
        apiRow.tableCatalog()
            .ifPresent(row::setTableCatalog);
        apiRow.tableSchema()
            .ifPresent(row::setTableSchema);
        apiRow.tableName()
            .ifPresent(row::setTableName);
        apiRow.tableType()
            .ifPresent(row::setTableType);
        apiRow.tableGuid()
            .ifPresent(row::setTableGuid);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.tablePropId()
            .ifPresent(row::setTablePropId);
        apiRow.dateCreated()
            .ifPresent(row::setDateCreated);
        apiRow.dateModified()
            .ifPresent(row::setDateModified);

        return row;
    }

    public static MdSchemaActionsRequest fromDiscoverMdSchemaActions(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaActionsRestrictionsR restrictions = discoverMdSchemaActionsRestrictions(requestWs);
        return new MdSchemaActionsRequestR(properties, restrictions);
    }

    private static MdSchemaActionsRestrictionsR discoverMdSchemaActionsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_CUBE_NAME);
        String actionName = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_ACTION_NAME);
        String actionType = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_ACTION_TYPE);
        String coordinate = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_COORDINATE);
        String coordinateType = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_COORDINATE_TYPE);
        String invocation = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_INVOCATION);
        String cubeSource = map.get(MdSchemaActionsRestrictions.RESTRICTIONS_CUBE_SOURCE);

        return new MdSchemaActionsRestrictionsR(Optional.ofNullable(catalogName), Optional.ofNullable(schemaName),
            cubeName, Optional.ofNullable(actionName), Optional.ofNullable(ActionTypeEnum.fromValue(actionType)),
            Optional.ofNullable(coordinate), CoordinateTypeEnum.fromValue(coordinateType),
            InvocationEnum.fromValue(invocation), Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)));
    }

    public static DiscoverResponse toDiscoverMdSchemaActions(List<MdSchemaActionsResponseRow> responseApi)
        throws JAXBException, IOException {

        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverMdSchemaActionsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, MdSchemaActionsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverMdSchemaActionsResponseRow(MdSchemaActionsResponseRow apiRow) {
        MdSchemaActionsResponseRowXml row = new MdSchemaActionsResponseRowXml();

        // Mandatory
        row.setCubeName(apiRow.cubeName());
        row.setCoordinate(apiRow.coordinate());
        row.setCoordinateType(
            org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.CoordinateTypeEnum
                .fromValue(apiRow.coordinateType().getValue()));

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.actionName()
            .ifPresent(row::setActionName);
        apiRow.actionType()
            .ifPresent(i -> row.setActionType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ActionTypeEnum.fromValue(i.getValue())));
        apiRow.actionCaption()
            .ifPresent(row::setActionCaption);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.content()
            .ifPresent(row::setContent);
        apiRow.application()
            .ifPresent(row::setApplication);
        apiRow.invocation()
            .ifPresent(i -> row.setInvocation(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.InvocationEnum.fromValue(i.getValue())));
        return row;
    }

    public static MdSchemaCubesRequest fromDiscoverMdSchemaCubes(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaCubesRestrictionsR restrictions = discoverMdSchemaCubesRestrictions(requestWs);
        return new MdSchemaCubesRequestR(properties, restrictions);
    }

    private static MdSchemaCubesRestrictionsR discoverMdSchemaCubesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaCubesRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaCubesRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaCubesRestrictions.RESTRICTIONS_CUBE_NAME);
        String baseCubeName = map.get(MdSchemaCubesRestrictions.RESTRICTIONS_BASE_CUBE_NAME);
        String cubeSource = map.get(MdSchemaCubesRestrictions.RESTRICTIONS_CUBE_SOURCE);

        return new MdSchemaCubesRestrictionsR(catalogName, Optional.ofNullable(schemaName), Optional.ofNullable(cubeName),
                Optional.ofNullable(baseCubeName), Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)));
    }

    public static DiscoverResponse toDiscoverMdSchemaCubes(List<MdSchemaCubesResponseRow> responseApi)
        throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverMdSchemaCubesResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, MdSchemaCubesResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverMdSchemaCubesResponseRow(MdSchemaCubesResponseRow apiRow) {
        MdSchemaCubesResponseRowXml row = new MdSchemaCubesResponseRowXml();

        // Mandatory
        row.setCatalogName(apiRow.catalogName());

        // Optional
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.cubeType()
            .ifPresent(i -> row.setCubeType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.CubeTypeEnum.fromValue(i.name())));
        apiRow.cubeGuid()
            .ifPresent(row::setCubeGuid);
        apiRow.createdOn()
            .ifPresent(row::setCreatedOn);
        apiRow.lastSchemaUpdate()
            .ifPresent(row::setLastSchemaUpdate);
        apiRow.schemaUpdatedBy()
            .ifPresent(row::setSchemaUpdatedBy);
        apiRow.lastDataUpdate()
            .ifPresent(row::setLastDataUpdate);
        apiRow.dataUpdateDBy()
            .ifPresent(row::setDataUpdateDBy);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.isDrillThroughEnabled()
            .ifPresent(row::setDrillThroughEnabled);
        apiRow.isLinkable()
            .ifPresent(row::setLinkable);
        apiRow.isWriteEnabled()
            .ifPresent(row::setWriteEnabled);
        apiRow.isSqlEnabled()
            .ifPresent(row::setSqlEnabled);
        apiRow.cubeCaption()
            .ifPresent(row::setCubeCaption);
        apiRow.baseCubeName()
            .ifPresent(row::setBaseCubeName);
        apiRow.cubeSource()
            .ifPresent(i -> row.setCubeSource(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.CubeSourceEnum.fromValue(i.getValue())));
        apiRow.preferredQueryPatterns()
            .ifPresent(i -> row.setPreferredQueryPatterns(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.PreferredQueryPatternsEnum.fromValue(i.getValue())));

        return row;
    }

    public static MdSchemaDimensionsRequest fromDiscoverMdSchemaDimensions(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaDimensionsRestrictionsR restrictions = discoverMdSchemaDimensionsRestrictions(requestWs);
        return new MdSchemaDimensionsRequestR(properties, restrictions);
    }

    private static MdSchemaDimensionsRestrictionsR discoverMdSchemaDimensionsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaDimensionsRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaDimensionsRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaDimensionsRestrictions.RESTRICTIONS_CUBE_NAME);
        String dimensionName = map.get(MdSchemaDimensionsRestrictions.RESTRICTIONS_DIMENSION_NAME);
        String dimensionUniqueName = map.get(MdSchemaDimensionsRestrictions.RESTRICTIONS_DIMENSION_UNIQUE_NAME);
        String cubeSource = map.get(MdSchemaDimensionsRestrictions.RESTRICTIONS_CUBE_SOURCE);
        String dimensionVisibility = map.get(MdSchemaDimensionsRestrictions.RESTRICTIONS_DIMENSION_VISIBILITY);

        return new MdSchemaDimensionsRestrictionsR(Optional.ofNullable(catalogName), Optional.ofNullable(schemaName),
                Optional.ofNullable(cubeName), Optional.ofNullable(dimensionName),
                Optional.ofNullable(dimensionUniqueName),
                Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)),
                Optional.ofNullable(VisibilityEnum.fromValue(dimensionVisibility)));
    }

    public static DiscoverResponse toDiscoverMdSchemaDimensions(List<MdSchemaDimensionsResponseRow> responseApi)
        throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverMdSchemaDimensionsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, MdSchemaDimensionsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverMdSchemaDimensionsResponseRow(MdSchemaDimensionsResponseRow apiRow) {
        MdSchemaDimensionsResponseRowXml row = new MdSchemaDimensionsResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.dimensionName()
            .ifPresent(row::setDimensionName);
        apiRow.dimensionUniqueName()
            .ifPresent(row::setDimensionUniqueName);
        apiRow.dimensionGuid()
            .ifPresent(row::setDimensionGuid);
        apiRow.dimensionCaption()
            .ifPresent(row::setDimensionCaption);
        apiRow.dimensionOptional()
            .ifPresent(row::setDimensionOptional);
        apiRow.dimensionType()
            .ifPresent(i -> row.setDimensionType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.DimensionTypeEnum.fromValue(i.getValue())));
        apiRow.dimensionCardinality()
            .ifPresent(row::setDimensionCardinality);
        apiRow.defaultHierarchy()
            .ifPresent(row::setDefaultHierarchy);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.isVirtual()
            .ifPresent(row::setVirtual);
        apiRow.isReadWrite()
            .ifPresent(row::setReadWrite);
        apiRow.dimensionUniqueSetting()
            .ifPresent(i -> row.setDimensionUniqueSetting(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.DimensionUniqueSettingEnum.fromValue(i.getValue())));
        apiRow.dimensionMasterName()
            .ifPresent(row::setDimensionMasterName);
        apiRow.dimensionIsVisible()
            .ifPresent(row::setDimensionIsVisible);


        return row;
    }


    public static MdSchemaFunctionsRequest fromDiscoverMdSchemaFunctions(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaFunctionsRestrictionsR restrictions = discoverMdSchemaFunctionsRestrictions(requestWs);
        return new MdSchemaFunctionsRequestR(properties, restrictions);
    }

    private static MdSchemaFunctionsRestrictionsR discoverMdSchemaFunctionsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String origin = map.get(MdSchemaFunctionsRestrictions.RESTRICTIONS_ORIGIN);
        String interfaceName = map.get(MdSchemaFunctionsRestrictions.RESTRICTIONS_INTERFACE_NAME);
        String libraryName = map.get(MdSchemaFunctionsRestrictions.RESTRICTIONS_LIBRARY_NAME);

        return new MdSchemaFunctionsRestrictionsR(Optional.ofNullable(OriginEnum.fromValue(origin)),
                Optional.ofNullable(InterfaceNameEnum.fromValue(interfaceName)), Optional.ofNullable(libraryName));
    }

    public static DiscoverResponse toDiscoverMdSchemaFunctions(List<MdSchemaFunctionsResponseRow> responseApi) throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverMdSchemaFunctionsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, MdSchemaFunctionsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverMdSchemaFunctionsResponseRow(MdSchemaFunctionsResponseRow apiRow) {
        MdSchemaFunctionsResponseRowXml row = new MdSchemaFunctionsResponseRowXml();

        // Mandatory
        row.setParameterList(apiRow.parameterList());

        // Optional
        apiRow.functionalName()
            .ifPresent(row::setFunctionalName);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.returnType()
            .ifPresent(row::setReturnType);
        apiRow.origin()
            .ifPresent(i -> row.setOrigin(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.OriginEnum.fromValue(i.getValue())));
        apiRow.interfaceName()
            .ifPresent(i -> row.setInterfaceName(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.InterfaceNameEnum.fromValue(i.name())));
        apiRow.libraryName()
            .ifPresent(row::setLibraryName);
        apiRow.dllName()
            .ifPresent(row::setDllName);
        apiRow.helpFile()
            .ifPresent(row::setHelpFile);
        apiRow.helpContext()
            .ifPresent(row::setHelpContext);
        apiRow.object()
            .ifPresent(row::setObject);
        apiRow.caption()
            .ifPresent(row::setCaption);

		apiRow.parameterInfo().ifPresent(parameterInfos -> row.setParameterInfo(parameterInfos.stream().map(i -> {
			ParameterInfoXml result = new ParameterInfoXml();
			result.setName(i.name());
			result.setDescription(i.description());
			result.setOptional(i.optional());
			result.setRepeatable(i.repeatable());
			result.setRepeatGroup(i.repeatGroup());
			return result;
		}).toList()));

        apiRow.directQueryPushable()
            .ifPresent(i -> row.setDirectQueryPushable(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.DirectQueryPushableEnum.fromValue(i.getValue())));

        return row;
    }

    public static MdSchemaHierarchiesRequest fromDiscoverMdSchemaHierarchies(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaHierarchiesRestrictionsR restrictions = discoverMdSchemaHierarchiesRestrictions(requestWs);
        return new MdSchemaHierarchiesRequestR(properties, restrictions);
    }

    private static MdSchemaHierarchiesRestrictionsR discoverMdSchemaHierarchiesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_CUBE_NAME);
        String dimensionUniqueName = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_DIMENSION_UNIQUE_NAME);
        String hierarchyName = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_HIERARCHY_NAME);
        String hierarchyUniqueName = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_HIERARCHY_UNIQUE_NAME);
        String hierarchyOrigin = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_HIERARCHY_ORIGIN);
        String cubeSource = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_CUBE_SOURCE);
        String hierarchyVisibility = map.get(MdSchemaHierarchiesRestrictions.RESTRICTIONS_HIERARCHY_VISIBILITY);

        return new MdSchemaHierarchiesRestrictionsR(Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            Optional.ofNullable(cubeName),
            Optional.ofNullable(dimensionUniqueName),
            Optional.ofNullable(hierarchyName),
            Optional.ofNullable(hierarchyUniqueName),
            Optional.ofNullable(hierarchyOrigin == null ? null : Integer.valueOf(hierarchyOrigin)),
            Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)),
            Optional.ofNullable(VisibilityEnum.fromValue(hierarchyVisibility)));
    }

    public static DiscoverResponse toDiscoverMdSchemaHierarchies(List<MdSchemaHierarchiesResponseRow> responseApi) throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverMdSchemaHierarchiesResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, MdSchemaHierarchiesResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverMdSchemaHierarchiesResponseRow(MdSchemaHierarchiesResponseRow apiRow) {
        MdSchemaHierarchiesResponseRowXml row = new MdSchemaHierarchiesResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.dimensionUniqueName()
            .ifPresent(row::setDimensionUniqueName);
        apiRow.hierarchyName()
            .ifPresent(row::setHierarchyName);
        apiRow.hierarchyUniqueName()
            .ifPresent(row::setHierarchyUniqueName);
        apiRow.hierarchyGuid()
            .ifPresent(row::setHierarchyGuid);
        apiRow.hierarchyCaption()
            .ifPresent(row::setHierarchyCaption);
        apiRow.dimensionType()
            .ifPresent(i -> row.setDimensionType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.DimensionTypeEnum.fromValue(i.getValue())));
        apiRow.hierarchyCardinality()
            .ifPresent(row::setHierarchyCardinality);
        apiRow.defaultMember()
            .ifPresent(row::setDefaultMember);
        apiRow.allMember()
            .ifPresent(row::setAllMember);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.structure()
            .ifPresent(i -> row.setStructure(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.StructureEnum.fromValue(i.getValue())));
        apiRow.isVirtual()
            .ifPresent(row::setVirtual);
        apiRow.isReadWrite()
            .ifPresent(row::setReadWrite);
        apiRow.dimensionUniqueSettings()
            .ifPresent(i -> row.setDimensionUniqueSettings(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.DimensionUniqueSettingEnum.fromValue(i.getValue())));
        apiRow.dimensionMasterUniqueName()
            .ifPresent(row::setDimensionMasterUniqueName);
        apiRow.dimensionIsVisible()
            .ifPresent(row::setDimensionIsVisible);
        apiRow.dimensionIsVisible()
            .ifPresent(row::setDimensionIsVisible);
        apiRow.hierarchyOrdinal()
            .ifPresent(row::setHierarchyOrdinal);
        apiRow.dimensionIsShared()
            .ifPresent(row::setDimensionIsShared);
        apiRow.hierarchyIsVisible()
            .ifPresent(row::setHierarchyIsVisible);
        apiRow.hierarchyOrigin()
            .ifPresent(i -> row.setHierarchyOrigin(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.HierarchyOriginEnum.fromValue(i.getValue())));
        apiRow.hierarchyDisplayFolder()
            .ifPresent(row::setHierarchyDisplayFolder);
        apiRow.instanceSelection()
            .ifPresent(i -> row.setInstanceSelection(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.InstanceSelectionEnum.fromValue(i.getValue())));
        apiRow.groupingBehavior()
            .ifPresent(i -> row.setGroupingBehavior(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.GroupingBehaviorEnum.fromValue(i.getValue())));
        apiRow.structureType()
            .ifPresent(i -> row.setStructureType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.StructureTypeEnum.fromValue(i.getValue())));

        return row;
    }

    public static DiscoverDataSourcesRequest fromDiscoverDataSources(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DiscoverDataSourcesRestrictionsR restrictions = discoverDataSourcesRestrictions(requestWs);
        return new DiscoverDataSourcesRequestR(properties, restrictions);
    }

    private static DiscoverDataSourcesRestrictionsR discoverDataSourcesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);
        String dataSourcesName = map.get(DiscoverDataSourcesRestrictions.RESTRICTIONS_DATA_SOURCE_NAME);
        String dataSourcesDescription = map.get(DiscoverDataSourcesRestrictions.RESTRICTIONS_DATA_SOURCE_DESCRIPTION);
        String url = map.get(DiscoverDataSourcesRestrictions.RESTRICTIONS_URL);
        String dataSourcesInfo = map.get(DiscoverDataSourcesRestrictions.RESTRICTIONS_DATA_SOURCE_INFO);
        String providerName = map.get(DiscoverDataSourcesRestrictions.RESTRICTIONS_PROVIDER_NAME);
        String providerType = map.get(DiscoverDataSourcesRestrictions.RESTRICTIONS_PROVIDER_TYPE);
        String authenticationMode = map.get(DiscoverDataSourcesRestrictions.RESTRICTIONS_AUTHENTICATION_MODE);


        return new DiscoverDataSourcesRestrictionsR(
            dataSourcesName,
            Optional.ofNullable(dataSourcesDescription),
            Optional.ofNullable(url),
            Optional.ofNullable(dataSourcesInfo),
            providerName,
            Optional.ofNullable(ProviderTypeEnum.fromValue(providerType)),
            Optional.ofNullable(AuthenticationModeEnum.fromValue(authenticationMode)));
    }

    public static DiscoverResponse toDiscoverDataSources(List<DiscoverDataSourcesResponseRow> responseApi)
        throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverDataSourcesResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DiscoverDataSourcesResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverDataSourcesResponseRow(DiscoverDataSourcesResponseRow apiRow) {
        DiscoverDataSourcesResponseRowXml row = new DiscoverDataSourcesResponseRowXml();

        // Mandatory
        row.setDataSourceName(apiRow.dataSourceName());
        row.setProviderName(apiRow.providerName());

        // Optional
        apiRow.dataSourceDescription()
            .ifPresent(row::setDataSourceDescription);
        apiRow.url()
            .ifPresent(row::setUrl);
        apiRow.dataSourceInfo()
            .ifPresent(row::setDataSourceInfo);
        apiRow.providerType()
            .ifPresent(i -> row.setProviderType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ProviderTypeEnum.fromValue(i.name())));
        apiRow.authenticationMode()
            .ifPresent(i -> row.setAuthenticationMode(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.AuthenticationModeEnum.fromValue(i.getValue())));

        return row;
    }

    public static DiscoverResponse toDiscoverXmlMetaData(List<DiscoverXmlMetaDataResponseRow> responseApi) throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDiscoverXmlMetaDataResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DiscoverXmlMetaDataResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDiscoverXmlMetaDataResponseRow(DiscoverXmlMetaDataResponseRow apiRow) {
        DiscoverXmlMetaDataResponseRowXml row = new DiscoverXmlMetaDataResponseRowXml();

        // Mandatory
        row.setMetaData(apiRow.metaData());

        return row;
    }

    public static DiscoverXmlMetaDataRequest fromDiscoverXmlMetaData(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DiscoverXmlMetaDataRestrictionsR restrictions = discoverXmlMetaDataRestrictions(requestWs);
        return new DiscoverXmlMetaDataRequestR(properties, restrictions);
    }

    private static DiscoverXmlMetaDataRestrictionsR discoverXmlMetaDataRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String databaseId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_DATABASE_ID);
        String dimensionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_DIMENSION_ID);
        String cubeId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_CUBE_ID);
        String measureGroupId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_MEASURE_GROUP_ID);
        String partitionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_PARTITION_ID);
        String perspectiveId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_PERSPECTIVE_ID);
        String dimensionPermissionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_PERMISSION_ID);
        String roleId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_ROLE_ID);
        String databasePermissionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_DATABASE_PERMISSION_ID);
        String miningModelId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_MINING_MODEL_ID);
        String miningModelPermissionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_MINING_MODEL_PERMISSION_ID);
        String dataSourceId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_DATA_SOURCE_ID);
        String miningStructureId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_MINING_STRUCTURE_ID);
        String aggregationDesignId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_AGGREGATION_DESIGN_ID);
        String traceId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_TRACE_ID);
        String miningStructurePermissionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_MINING_STRUCTURE_PERMISSION_ID);
        String cubePermissionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_CUBE_PERMISSION_ID);
        String assemblyId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_ASSEMBLY_ID);
        String mdxScriptId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_MDX_SCRIPT_ID);
        String dataSourceViewId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_DATA_SOURCE_VIEW_ID);
        String dataSourcePermissionId = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_DATA_SOURCE_PERMISSION_ID);
        String objectExpansion = map.get(DiscoverXmlMetaDataRestrictions.RESTRICTIONS_OBJECT_EXPANSION);


        return new DiscoverXmlMetaDataRestrictionsR(
            Optional.ofNullable(databaseId),
            Optional.ofNullable(dimensionId),
            Optional.ofNullable(cubeId),
            Optional.ofNullable(measureGroupId),
            Optional.ofNullable(partitionId),
            Optional.ofNullable(perspectiveId),
            Optional.ofNullable(dimensionPermissionId),
            Optional.ofNullable(roleId),
            Optional.ofNullable(databasePermissionId),
            Optional.ofNullable(miningModelId),
            Optional.ofNullable(miningModelPermissionId),
            Optional.ofNullable(dataSourceId),
            Optional.ofNullable(miningStructureId),
            Optional.ofNullable(aggregationDesignId),
            Optional.ofNullable(traceId),
            Optional.ofNullable(miningStructurePermissionId),
            Optional.ofNullable(cubePermissionId),
            Optional.ofNullable(assemblyId),
            Optional.ofNullable(mdxScriptId),
            Optional.ofNullable(dataSourceViewId),
            Optional.ofNullable(dataSourcePermissionId),
            Optional.ofNullable(ObjectExpansionEnum.fromValue(objectExpansion)));
    }

    public static DbSchemaColumnsRequest fromDiscoverDbSchemaColumns(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DbSchemaColumnsRestrictionsR restrictions = discoverDbSchemaColumnsRestrictions(requestWs);
        return new DbSchemaColumnsRequestR(properties, restrictions);
    }

    private static DbSchemaColumnsRestrictionsR discoverDbSchemaColumnsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String tableCatalog = map.get(DbSchemaColumnsRestrictions.RESTRICTIONS_TABLE_CATALOG);
        String tableSchema = map.get(DbSchemaColumnsRestrictions.RESTRICTIONS_TABLE_SCHEMA);
        String tableName = map.get(DbSchemaColumnsRestrictions.RESTRICTIONS_TABLE_NAME);
        String columnName = map.get(DbSchemaColumnsRestrictions.RESTRICTIONS_COLUMN_NAME);
        String columnOlapType = map.get(DbSchemaColumnsRestrictions.RESTRICTIONS_COLUMN_OLAP_TYPE);

        return new DbSchemaColumnsRestrictionsR(
            Optional.ofNullable(tableCatalog),
            Optional.ofNullable(tableSchema),
            Optional.ofNullable(tableName),
            Optional.ofNullable(columnName),
            Optional.ofNullable(ColumnOlapTypeEnum.fromValue(columnOlapType)));
    }

    public static DiscoverResponse toDiscoverDbSchemaColumns(List<DbSchemaColumnsResponseRow> responseApi)
        throws JAXBException, IOException {
        List<Row> rows = responseApi.stream()
            .map(Convert::convertDbSchemaColumnsResponseRow)
            .toList();

        DiscoverResponse responseWs = new DiscoverResponse();
        responseWs.setReturn(getReturn(rows, DbSchemaColumnsResponseRowXml.class));

        return responseWs;
    }

    private static Row convertDbSchemaColumnsResponseRow(DbSchemaColumnsResponseRow apiRow) {
        DbSchemaColumnsResponseRowXml row = new DbSchemaColumnsResponseRowXml();

        // Optional
        apiRow.tableCatalog()
            .ifPresent(row::setTableCatalog);
        apiRow.tableSchema()
            .ifPresent(row::setTableSchema);
        apiRow.tableName()
            .ifPresent(row::setTableName);
        apiRow.columnName()
            .ifPresent(row::setColumnName);
        apiRow.columnGuid()
            .ifPresent(row::setColumnGuid);
        apiRow.columnPropId()
            .ifPresent(row::setColumnPropId);
        apiRow.ordinalPosition()
            .ifPresent(row::setOrdinalPosition);
        apiRow.columnHasDefault()
            .ifPresent(row::setColumnHasDefault);
        apiRow.columnDefault()
            .ifPresent(row::setColumnDefault);
        apiRow.columnFlags()
            .ifPresent(i -> row.setColumnFlags(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ColumnFlagsEnum.fromValue(i.getValue())));
        apiRow.isNullable()
            .ifPresent(row::setNullable);
        apiRow.dataType()
            .ifPresent(row::setDataType);
        apiRow.typeGuid()
            .ifPresent(row::setTypeGuid);
        apiRow.characterMaximum()
            .ifPresent(row::setCharacterMaximum);
        apiRow.characterOctetLength()
            .ifPresent(row::setCharacterOctetLength);
        apiRow.numericPrecision()
            .ifPresent(row::setNumericPrecision);
        apiRow.numericScale()
            .ifPresent(row::setNumericScale);
        apiRow.dateTimePrecision()
            .ifPresent(row::setDateTimePrecision);
        apiRow.characterSetCatalog()
            .ifPresent(row::setCharacterSetCatalog);
        apiRow.characterSetSchema()
            .ifPresent(row::setCharacterSetSchema);
        apiRow.characterSetName()
            .ifPresent(row::setCharacterSetName);
        apiRow.collationCatalog()
            .ifPresent(row::setCollationCatalog);
        apiRow.collationSchema()
            .ifPresent(row::setCollationSchema);
        apiRow.collationName()
            .ifPresent(row::setCollationName);
        apiRow.domainCatalog()
            .ifPresent(row::setDomainCatalog);
        apiRow.domainSchema()
            .ifPresent(row::setDomainSchema);
        apiRow.domainName()
            .ifPresent(row::setDomainName);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.columnOlapType()
            .ifPresent(i -> row.setColumnOlapType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ColumnOlapTypeEnum.fromValue(i.name())));

        return row;
    }

    public static DbSchemaProviderTypesRequest fromDiscoverDbSchemaProviderTypes(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DbSchemaProviderTypesRestrictionsR restrictions = discoverDbSchemaProviderTypesRestrictions(requestWs);
        return new DbSchemaProviderTypesRequestR(properties, restrictions);
    }

    private static DbSchemaProviderTypesRestrictionsR discoverDbSchemaProviderTypesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String dataType = map.get(DbSchemaProviderTypesRestrictions.RESTRICTIONS_DATA_TYPE);
        String bestMatch = map.get(DbSchemaProviderTypesRestrictions.RESTRICTIONS_BEST_MATCH);

        return new DbSchemaProviderTypesRestrictionsR(
            Optional.ofNullable(LevelDbTypeEnum.fromValue(dataType)),
            Optional.ofNullable(bestMatch == null ? null : Boolean.valueOf(bestMatch)));
    }

    public static DiscoverResponse toDiscoverDbSchemaProviderTypes(List<DbSchemaProviderTypesResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertDbSchemaProviderTypesResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, DbSchemaProviderTypesResponseRowXml.class));

            return responseWs;
    }

    private static Row convertDbSchemaProviderTypesResponseRow(DbSchemaProviderTypesResponseRow apiRow) {
        DbSchemaProviderTypesResponseRowXml row = new DbSchemaProviderTypesResponseRowXml();

        // Optional
        apiRow.typeName()
            .ifPresent(row::setTypeName);
        apiRow.dataType()
            .ifPresent(i -> row.setDataType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LevelDbTypeEnum.fromValue(i.getValue())));
        apiRow.columnSize()
            .ifPresent(row::setColumnSize);
        apiRow.literalPrefix()
            .ifPresent(row::setLiteralPrefix);
        apiRow.literalSuffix()
        .ifPresent(row::setLiteralSuffix);
        apiRow.createParams()
            .ifPresent(row::setCreateParams);
        apiRow.isNullable()
            .ifPresent(row::setNullable);
        apiRow.caseSensitive()
            .ifPresent(row::setCaseSensitive);
        apiRow.searchable()
            .ifPresent(i -> row.setSearchable(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.SearchableEnum.fromValue(i.getValue())));
        apiRow.unsignedAttribute()
            .ifPresent(row::setUnsignedAttribute);
        apiRow.fixedPrecScale()
            .ifPresent(row::setFixedPrecScale);
        apiRow.autoUniqueValue()
            .ifPresent(row::setAutoUniqueValue);
        apiRow.localTypeName()
            .ifPresent(row::setLocalTypeName);
        apiRow.minimumScale()
            .ifPresent(row::setMinimumScale);
        apiRow.maximumScale()
            .ifPresent(row::setMaximumScale);
        apiRow.guid()
            .ifPresent(row::setGuid);
        apiRow.typeLib()
            .ifPresent(row::setTypeLib);
        apiRow.version()
            .ifPresent(row::setVersion);
        apiRow.isLong()
            .ifPresent(row::setLong);
        apiRow.bestMatch()
            .ifPresent(row::setBestMatch);
        apiRow.isFixedLength()
            .ifPresent(row::setFixedLength);

        return row;
    }

    public static DbSchemaSchemataRequest fromDiscoverDbSchemaSchemata(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DbSchemaSchemataRestrictionsR restrictions = discoverDbSchemaSchemataRestrictions(requestWs);
        return new DbSchemaSchemataRequestR(properties, restrictions);
    }

    private static DbSchemaSchemataRestrictionsR discoverDbSchemaSchemataRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(DbSchemaSchemataRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(DbSchemaSchemataRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String schemaOwner = map.get(DbSchemaSchemataRestrictions.RESTRICTIONS_SCHEMA_OWNER);

        return new DbSchemaSchemataRestrictionsR(
            catalogName,
            schemaName,
            schemaOwner);
    }

    public static DiscoverResponse toDiscoverDbSchemaSchemata(List<DbSchemaSchemataResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertDbSchemaSchemataResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, DbSchemaSchemataResponseRowXml.class));

            return responseWs;
    }

    private static Row convertDbSchemaSchemataResponseRow(DbSchemaSchemataResponseRow apiRow) {
        DbSchemaSchemataResponseRowXml row = new DbSchemaSchemataResponseRowXml();

        // Mandatory
        row.setCatalogName(apiRow.catalogName());
        row.setSchemaName(apiRow.schemaName());
        row.setSchemaOwner(apiRow.schemaOwner());

        return row;
    }

    public static MdSchemaLevelsRequest fromDiscoverMdSchemaLevels(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaLevelsRestrictionsR restrictions = discoverMdSchemaLevelsRestrictions(requestWs);
        return new MdSchemaLevelsRequestR(properties, restrictions);
    }

    private static MdSchemaLevelsRestrictionsR discoverMdSchemaLevelsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_CUBE_NAME);
        String dimensionUniqueName = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_DIMENSION_UNIQUE_NAME);
        String hierarchyUniqueName = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_HIERARCHY_UNIQUE_NAME);
        String levelName = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_LEVEL_NAME);
        String levelUniqueName = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_LEVEL_UNIQUE_NAME);
        String cubeSource = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_CUBE_SOURCE);
        String levelVisibility = map.get(MdSchemaLevelsRestrictions.RESTRICTIONS_LEVEL_VISIBILITY);

        return new MdSchemaLevelsRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            Optional.ofNullable(cubeName),
            Optional.ofNullable(dimensionUniqueName),
            Optional.ofNullable(hierarchyUniqueName),
            Optional.ofNullable(levelName),
            Optional.ofNullable(levelUniqueName),
            Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)),
            Optional.ofNullable(VisibilityEnum.fromValue(levelVisibility)));
    }

    public static DiscoverResponse toDiscoverMdSchemaLevels(List<MdSchemaLevelsResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaLevelsResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaLevelsResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaLevelsResponseRow(MdSchemaLevelsResponseRow apiRow) {
        MdSchemaLevelsResponseRowXml row = new MdSchemaLevelsResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.dimensionUniqueName()
            .ifPresent(row::setDimensionUniqueName);
        apiRow.hierarchyUniqueName()
            .ifPresent(row::setHierarchyUniqueName);
        apiRow.levelName()
            .ifPresent(row::setLevelName);
        apiRow.levelUniqueName()
            .ifPresent(row::setLevelUniqueName);
        apiRow.levelGuid()
            .ifPresent(row::setLevelGuid);
        apiRow.levelCaption()
            .ifPresent(row::setLevelCaption);
        apiRow.levelNumber()
            .ifPresent(row::setLevelNumber);
        apiRow.levelCardinality()
            .ifPresent(row::setLevelCardinality);
        apiRow.levelType()
            .ifPresent(i -> row.setLevelType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LevelTypeEnum.fromValue(i.getValue())));
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.customRollupSetting()
            .ifPresent(i -> row.setCustomRollupSettings(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.CustomRollupSettingEnum.fromValue(i.getValue())));
        apiRow.levelUniqueSettings()
            .ifPresent(i -> row.setLevelUniqueSettings(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LevelUniqueSettingsEnum.fromValue(i.getValue())));
        apiRow.levelIsVisible()
            .ifPresent(row::setLevelIsVisible);
        apiRow.levelOrderingProperty()
            .ifPresent(row::setLevelOrderingProperty);
        apiRow.levelDbType()
            .ifPresent(i -> row.setLevelDbType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LevelDbTypeEnum.fromValue(i.getValue())));
        apiRow.levelMasterUniqueName()
            .ifPresent(row::setLevelMasterUniqueName);
        apiRow.levelNameSqlColumnName()
            .ifPresent(row::setLevelNameSqlColumnName);
        apiRow.levelKeySqlColumnName()
            .ifPresent(row::setLevelKeySqlColumnName);
        apiRow.levelUniqueNameSqlColumnName()
            .ifPresent(row::setLevelUniqueNameSqlColumnName);
        apiRow.levelAttributeHierarchyName()
            .ifPresent(row::setLevelAttributeHierarchyName);
        apiRow.levelKeyCardinality()
            .ifPresent(row::setLevelKeyCardinality);
        apiRow.levelOrigin()
            .ifPresent(i -> row.setLevelOrigin(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LevelOriginEnum.fromValue(i.getValue())));

        return row;
    }

    public static MdSchemaMeasureGroupDimensionsRequest fromDiscoverMdSchemaMeasureGroupDimensions(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaMeasureGroupDimensionsRestrictionsR restrictions = discoverMdSchemaMeasureGroupDimensionsRestrictions(requestWs);
        return new MdSchemaMeasureGroupDimensionsRequestR(properties, restrictions);
    }

    private static MdSchemaMeasureGroupDimensionsRestrictionsR discoverMdSchemaMeasureGroupDimensionsRestrictions(
        Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaMeasureGroupDimensionsRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaMeasureGroupDimensionsRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaMeasureGroupDimensionsRestrictions.RESTRICTIONS_CUBE_NAME);
        String measureGroupName = map.get(MdSchemaMeasureGroupDimensionsRestrictions.RESTRICTIONS_MEASUREGROUP_NAME);
        String dimensionUniqueName = map.get(MdSchemaMeasureGroupDimensionsRestrictions.RESTRICTIONS_DIMENSION_UNIQUE_NAME);
        String dimensionVisibility = map.get(MdSchemaMeasureGroupDimensionsRestrictions.RESTRICTIONS_DIMENSION_VISIBILITY);

        return new MdSchemaMeasureGroupDimensionsRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            Optional.ofNullable(cubeName),
            Optional.ofNullable(dimensionUniqueName),
            Optional.ofNullable(measureGroupName),
            Optional.ofNullable(VisibilityEnum.fromValue(dimensionVisibility)));
    }

    public static DiscoverResponse toDiscoverMdSchemaMeasureGroupDimensions(List<MdSchemaMeasureGroupDimensionsResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaMeasureGroupDimensionsResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaMeasureGroupDimensionsResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaMeasureGroupDimensionsResponseRow(MdSchemaMeasureGroupDimensionsResponseRow apiRow) {
        MdSchemaMeasureGroupDimensionsResponseRowXml row = new MdSchemaMeasureGroupDimensionsResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.measureGroupName()
            .ifPresent(row::setMeasureGroupName);
        apiRow.measureGroupCardinality()
            .ifPresent(row::setMeasureGroupCardinality);
        apiRow.dimensionUniqueName()
            .ifPresent(row::setDimensionUniqueName);
        apiRow.dimensionCardinality()
            .ifPresent(i -> row.setDimensionCardinality(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.DimensionCardinalityEnum.fromValue(i.name())));
        apiRow.dimensionIsVisible()
            .ifPresent(row::setDimensionIsVisible);
        apiRow.dimensionIsFactDimension()
            .ifPresent(row::setDimensionIsFactDimension);
        apiRow.dimensionGranularity()
            .ifPresent(row::setDimensionGranularity);

		apiRow.dimensionPath().ifPresent(mgDimensions -> row.setDimensionPath(mgDimensions.stream().map(i -> {
			MeasureGroupDimensionXml result = new MeasureGroupDimensionXml();
			result.setMeasureGroupDimension(i.measureGroupDimension());
			return result;
		}).toList())
		);

        return row;
    }

    public static MdSchemaMeasuresRequest fromDiscoverMdSchemaMeasures(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaMeasuresRestrictionsR restrictions = discoverMdSchemaMeasuresRestrictions(requestWs);
        return new MdSchemaMeasuresRequestR(properties, restrictions);
    }

    private static MdSchemaMeasuresRestrictionsR discoverMdSchemaMeasuresRestrictions(Discover requestWs) {
            Map<String, String> map = restrictionsMap(requestWs);

            String catalogName = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_CATALOG_NAME);
            String schemaName = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_SCHEMA_NAME);
            String cubeName = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_CUBE_NAME);
            String measureName = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_MEASURE_NAME);
            String measureUniqueName = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_MEASURE_UNIQUE_NAME);
            String measureGroupName = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_MEASUREGROUP_NAME);
            String cubeSource = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_CUBE_SOURCE);
            String measureVisibility = map.get(MdSchemaMeasuresRestrictions.RESTRICTIONS_MEASURE_VISIBILITY);

        return new MdSchemaMeasuresRestrictionsR(
                Optional.ofNullable(catalogName),
                Optional.ofNullable(schemaName),
                Optional.ofNullable(cubeName),
                Optional.ofNullable(measureName),
                Optional.ofNullable(measureUniqueName),
                Optional.ofNullable(measureGroupName),
                Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)),
                Optional.ofNullable(VisibilityEnum.fromValue(measureVisibility)));
    }

    public static DiscoverResponse toDiscoverMdSchemaMeasures(List<MdSchemaMeasuresResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaMeasuresResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaMembersResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaMeasuresResponseRow(MdSchemaMeasuresResponseRow apiRow) {
        MdSchemaMeasuresResponseRowXml row = new MdSchemaMeasuresResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.measureName()
            .ifPresent(row::setMeasureName);
        apiRow.measureUniqueName()
            .ifPresent(row::setMeasureUniqueName);
        apiRow.measureCaption()
            .ifPresent(row::setMeasureCaption);
        apiRow.measureGuid()
            .ifPresent(row::setMeasureGuid);
        apiRow.measureAggregator()
            .ifPresent(i -> row.setMeasureAggregator(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.MeasureAggregatorEnum.fromValue(i.getValue())));
        apiRow.dataType()
            .ifPresent(i -> row.setDataType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LevelDbTypeEnum.fromValue(i.getValue())));
        apiRow.numericPrecision()
            .ifPresent(row::setNumericPrecision);
        apiRow.numericScale()
            .ifPresent(row::setNumericScale);
        apiRow.measureUnits()
            .ifPresent(row::setMeasureUnits);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.expression()
            .ifPresent(row::setExpression);
        apiRow.measureIsVisible()
            .ifPresent(row::setMeasureIsVisible);
        apiRow.levelsList()
            .ifPresent(row::setLevelsList);
        apiRow.measureNameSqlColumnName()
            .ifPresent(row::setMeasureNameSqlColumnName);
        apiRow.measureUnqualifiedCaption()
            .ifPresent(row::setMeasureUnqualifiedCaption);
        apiRow.measureGroupName()
            .ifPresent(row::setMeasureGroupName);
        apiRow.defaultFormatString()
            .ifPresent(row::setDefaultFormatString);
        return row;
    }

    public static MdSchemaMembersRequest fromDiscoverMdSchemaMembers(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaMembersRestrictionsR restrictions = discoverMdSchemaMembersRestrictions(requestWs);
        return new MdSchemaMembersRequestR(properties, restrictions);
    }

    private static MdSchemaMembersRestrictionsR discoverMdSchemaMembersRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_CUBE_NAME);
        String dimensionUniqueName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_DIMENSION_UNIQUE_NAME);
        String hierarchyUniqueName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_HIERARCHY_UNIQUE_NAME);
        String levelUniqueName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_LEVEL_UNIQUE_NAME);
        String levelNumber = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_LEVEL_NUMBER);
        String memberName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_MEMBER_NAME);
        String memberUniqueName = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_MEMBER_UNIQUE_NAME);
        String memberType = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_MEMBER_TYPE);
        String memberCaption = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_MEMBER_CAPTION);
        String cubeSource = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_CUBE_SOURCE);
        String treeOp = map.get(MdSchemaMembersRestrictions.RESTRICTIONS_TREE_OP);


        return new MdSchemaMembersRestrictionsR(
                Optional.ofNullable(catalogName),
                Optional.ofNullable(schemaName),
                Optional.ofNullable(cubeName),
                Optional.ofNullable(dimensionUniqueName),
                Optional.ofNullable(hierarchyUniqueName),
                Optional.ofNullable(levelUniqueName),
                Optional.ofNullable(levelNumber == null ? null: Integer.valueOf(levelNumber)),
                Optional.ofNullable(memberName),
                Optional.ofNullable(memberUniqueName),
                Optional.ofNullable(MemberTypeEnum.fromValue(memberType)),
                Optional.ofNullable(memberCaption),
                Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)),
                Optional.ofNullable(TreeOpEnum.fromValue(treeOp)));
    }

    public static DiscoverResponse toDiscoverMdSchemaMembers(List<MdSchemaMembersResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaMembersResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaMembersResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaMembersResponseRow(MdSchemaMembersResponseRow apiRow) {
        MdSchemaMembersResponseRowXml row = new MdSchemaMembersResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.dimensionUniqueName()
            .ifPresent(row::setDimensionUniqueName);
        apiRow.hierarchyUniqueName()
            .ifPresent(row::setHierarchyUniqueName);
        apiRow.levelUniqueName()
            .ifPresent(row::setLevelUniqueName);
        apiRow.levelNumber()
            .ifPresent(row::setLevelNumber);
        apiRow.memberOrdinal()
            .ifPresent(row::setMemberOrdinal);
        apiRow.memberName()
            .ifPresent(row::setMemberName);
        apiRow.memberUniqueName()
            .ifPresent(row::setMemberUniqueName);
        apiRow.memberType()
            .ifPresent(i -> row.setMemberType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.MemberTypeEnum.fromValue(i.getValue())));
        apiRow.memberGuid()
            .ifPresent(row::setMemberGuid);
        apiRow.measureCaption()
            .ifPresent(row::setMeasureCaption);
        apiRow.childrenCardinality()
            .ifPresent(row::setChildrenCardinality);
        apiRow.parentLevel()
            .ifPresent(row::setParentLevel);
        apiRow.parentUniqueName()
            .ifPresent(row::setParentUniqueName);
        apiRow.parentCount()
            .ifPresent(row::setParentCount);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.expression()
            .ifPresent(row::setExpression);
        apiRow.memberKey()
            .ifPresent(row::setMemberKey);
        apiRow.isPlaceHolderMember()
            .ifPresent(row::setPlaceHolderMember);
        apiRow.isDataMember()
            .ifPresent(row::setDataMember);
        apiRow.scope()
            .ifPresent(i -> row.setScope(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ScopeEnum.fromValue(i.getValue())));

        return row;
    }

    public static MdSchemaPropertiesRequest fromDiscoverMdSchemaProperties(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaPropertiesRestrictionsR restrictions = discoverMdSchemaPropertiesRestrictions(requestWs);
        return new MdSchemaPropertiesRequestR(properties, restrictions);
    }

    private static MdSchemaPropertiesRestrictionsR discoverMdSchemaPropertiesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_CUBE_NAME);
        String dimensionUniqueName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_DIMENSION_UNIQUE_NAME);
        String hierarchyUniqueName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_HIERARCHY_UNIQUE_NAME);
        String levelUniqueName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_LEVEL_UNIQUE_NAME);
        String memberUniqueName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_MEMBER_UNIQUE_NAME);
        String propertyType = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_PROPERTY_TYPE);
        String propertyName = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_PROPERTY_NAME);
        String propertyOrigin = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_PROPERTY_ORIGIN);
        String cubeSource = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_CUBE_SOURCE);
        String propertyVisibility = map.get(MdSchemaPropertiesRestrictions.RESTRICTIONS_PROPERTY_VISIBILITY);

        return new MdSchemaPropertiesRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            Optional.ofNullable(cubeName),
            Optional.ofNullable(dimensionUniqueName),
            Optional.ofNullable(hierarchyUniqueName),
            Optional.ofNullable(levelUniqueName),
            Optional.ofNullable(memberUniqueName),
            Optional.ofNullable(PropertyTypeEnum.fromValue(propertyType)),
            Optional.ofNullable(propertyName),
            Optional.ofNullable(PropertyOriginEnum.fromValue(propertyOrigin)),
            Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)),
            Optional.ofNullable(VisibilityEnum.fromValue(propertyVisibility)));
    }

    public static DiscoverResponse toDiscoverMdSchemaProperties(List<MdSchemaPropertiesResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaPropertiesResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaPropertiesResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaPropertiesResponseRow(MdSchemaPropertiesResponseRow apiRow) {
        MdSchemaPropertiesResponseRowXml row = new MdSchemaPropertiesResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.dimensionUniqueName()
            .ifPresent(row::setDimensionUniqueName);
        apiRow.hierarchyUniqueName()
            .ifPresent(row::setHierarchyUniqueName);
        apiRow.levelUniqueName()
            .ifPresent(row::setLevelUniqueName);
        apiRow.memberUniqueName()
            .ifPresent(row::setMemberUniqueName);
        apiRow.propertyType()
            .ifPresent(i -> row.setPropertyType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.PropertyTypeEnum.fromValue(i.getValue())));
        apiRow.propertyName()
            .ifPresent(row::setPropertyName);
        apiRow.propertyCaption()
            .ifPresent(row::setPropertyCaption);
        apiRow.dataType()
            .ifPresent(i -> row.setDataType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.LevelDbTypeEnum.fromValue(i.getValue())));
        apiRow.characterMaximumLength()
            .ifPresent(row::setCharacterMaximumLength);
        apiRow.characterOctetLength()
        .ifPresent(row::setCharacterOctetLength);
        apiRow.numericPrecision()
            .ifPresent(row::setNumericPrecision);
        apiRow.numericScale()
            .ifPresent(row::setNumericScale);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.propertyContentType()
            .ifPresent(i -> row.setPropertyContentType(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.PropertyContentTypeEnum.fromValue(i.getValue())));
        apiRow.sqlColumnName()
            .ifPresent(row::setSqlColumnName);
        apiRow.language()
            .ifPresent(row::setLanguage);
        apiRow.propertyOrigin()
            .ifPresent(i -> row.setPropertyOrigin(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.PropertyOriginEnum.fromValue(i.getValue())));
        apiRow.propertyAttributeHierarchyName()
            .ifPresent(row::setPropertyAttributeHierarchyName);
        apiRow.propertyCardinality()
            .ifPresent(i -> row.setPropertyCardinality(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.PropertyCardinalityEnum.fromValue(i.name())));
        apiRow.mimeType()
            .ifPresent(row::setMimeType);
        apiRow.propertyIsVisible()
            .ifPresent(row::setPropertyIsVisible);

        return row;
    }

    public static MdSchemaSetsRequest fromDiscoverMdSchemaSets(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaSetsRestrictionsR restrictions = discoverMdSchemaSetsRestrictions(requestWs);
        return new MdSchemaSetsRequestR(properties, restrictions);
    }

    private static MdSchemaSetsRestrictionsR discoverMdSchemaSetsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaSetsRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaSetsRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaSetsRestrictions.RESTRICTIONS_CUBE_NAME);
        String setName = map.get(MdSchemaSetsRestrictions.RESTRICTIONS_SET_NAME);
        String scope = map.get(MdSchemaSetsRestrictions.RESTRICTIONS_SCOPE);
        String cubeSource = map.get(MdSchemaSetsRestrictions.RESTRICTIONS_CUBE_SOURCE);
        String hierarchyUniqueName = map.get(MdSchemaSetsRestrictions.RESTRICTIONS_HIERARCHY_UNIQUE_NAME);

        return new MdSchemaSetsRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            Optional.ofNullable(cubeName),
            Optional.ofNullable(setName),
            Optional.ofNullable(ScopeEnum.fromValue(scope)),
            Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)),
            Optional.ofNullable(hierarchyUniqueName));
    }

    public static DiscoverResponse toDiscoverMdSchemaSets(List<MdSchemaSetsResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaSetsResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaSetsResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaSetsResponseRow(MdSchemaSetsResponseRow apiRow) {
        MdSchemaSetsResponseRowXml row = new MdSchemaSetsResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.setName()
            .ifPresent(row::setSetName);
        apiRow.scope()
            .ifPresent(i -> row.setScope(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ScopeEnum.fromValue(i.getValue())));
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.expression()
            .ifPresent(row::setExpression);
        apiRow.dimension()
            .ifPresent(row::setDimension);
        apiRow.setCaption()
            .ifPresent(row::setSetCaption);
        apiRow.setDisplayFolder()
            .ifPresent(row::setSetDisplayFolder);
        apiRow.setEvaluationContext()
            .ifPresent(i -> row.setSetEvaluationContext(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.SetEvaluationContextEnum.fromValue(i.getValue())));

        return row;
    }

    public static MdSchemaKpisRequest fromDiscoverMdSchemaKpis(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaKpisRestrictionsR restrictions = discoverMdSchemaKpisRestrictions(requestWs);
        return new MdSchemaKpisRequestR(properties, restrictions);
    }

    private static MdSchemaKpisRestrictionsR discoverMdSchemaKpisRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaKpisRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaKpisRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaKpisRestrictions.RESTRICTIONS_CUBE_NAME);
        String kpiName = map.get(MdSchemaKpisRestrictions.RESTRICTIONS_KPI_NAME);
        String cubeSource = map.get(MdSchemaKpisRestrictions.RESTRICTIONS_CUBE_SOURCE);

        return new MdSchemaKpisRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            Optional.ofNullable(cubeName),
            Optional.ofNullable(kpiName),
            Optional.ofNullable(CubeSourceEnum.fromValue(cubeSource)));
    }

    public static DiscoverResponse toDiscoverMdSchemaKpis(List<MdSchemaKpisResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaKpisResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaKpisResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaKpisResponseRow(MdSchemaKpisResponseRow apiRow) {
        MdSchemaKpisResponseRowXml row = new MdSchemaKpisResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.measureGroupName()
            .ifPresent(row::setMeasureGroupName);
        apiRow.kpiName()
            .ifPresent(row::setKpiName);
        apiRow.kpiCaption()
            .ifPresent(row::setKpiCaption);
        apiRow.kpiDescription()
            .ifPresent(row::setKpiDescription);
        apiRow.kpiDisplayFolder()
            .ifPresent(row::setKpiDisplayFolder);
        apiRow.kpiValue()
            .ifPresent(row::setKpiValue);
        apiRow.kpiGoal()
            .ifPresent(row::setKpiGoal);
        apiRow.kpiStatus()
            .ifPresent(row::setKpiStatus);
        apiRow.kpiTrend()
            .ifPresent(row::setKpiTrend);
        apiRow.kpiStatusGraphic()
            .ifPresent(row::setKpiStatusGraphic);
        apiRow.kpiTrendGraphic()
            .ifPresent(row::setKpiTrendGraphic);
        apiRow.kpiWight()
            .ifPresent(row::setKpiWight);
        apiRow.kpiCurrentTimeMember()
            .ifPresent(row::setKpiCurrentTimeMember);
        apiRow.kpiParentKpiName()
            .ifPresent(row::setKpiParentKpiName);
        apiRow.annotation()
            .ifPresent(row::setAnnotation);
        apiRow.scope()
            .ifPresent(i -> row.setScope(
                org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ScopeEnum.fromValue(i.getValue())));

        return row;
    }

    public static MdSchemaMeasureGroupsRequest fromDiscoverMdSchemaMeasureGroups(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        MdSchemaMeasureGroupsRestrictionsR restrictions = discoverMdSchemaMeasureGroupsRestrictions(requestWs);
        return new MdSchemaMeasureGroupsRequestR(properties, restrictions);
    }

    private static MdSchemaMeasureGroupsRestrictionsR discoverMdSchemaMeasureGroupsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(MdSchemaMeasureGroupsRestrictions.RESTRICTIONS_CATALOG_NAME);
        String schemaName = map.get(MdSchemaMeasureGroupsRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String cubeName = map.get(MdSchemaMeasureGroupsRestrictions.RESTRICTIONS_CUBE_NAME);
        String measureGroupName = map.get(MdSchemaMeasureGroupsRestrictions.RESTRICTIONS_MEASUREGROUP_NAME);

        return new MdSchemaMeasureGroupsRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            Optional.ofNullable(cubeName),
            Optional.ofNullable(measureGroupName));
    }

    public static DiscoverResponse toDiscoverMdSchemaMeasureGroups(List<MdSchemaMeasureGroupsResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertMdSchemaMeasureGroupsResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, MdSchemaMeasureGroupsResponseRowXml.class));

            return responseWs;
    }

    private static Row convertMdSchemaMeasureGroupsResponseRow(MdSchemaMeasureGroupsResponseRow apiRow) {
        MdSchemaMeasureGroupsResponseRowXml row = new MdSchemaMeasureGroupsResponseRowXml();

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.cubeName()
            .ifPresent(row::setCubeName);
        apiRow.measureGroupName()
            .ifPresent(row::setMeasureGroupName);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.isWriteEnabled()
            .ifPresent(row::setWriteEnabled);
        apiRow.measureGroupCaption()
            .ifPresent(row::setMeasureGroupCaption);

        return row;
    }

    public static DiscoverResponse toDiscoverDbSchemaSourceTables(List<DbSchemaSourceTablesResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertDbSchemaSourceTablesResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, DbSchemaSourceTablesResponseRowXml.class));

            return responseWs;
    }

    private static Row convertDbSchemaSourceTablesResponseRow(DbSchemaSourceTablesResponseRow apiRow) {
        DbSchemaSourceTablesResponseRowXml row = new DbSchemaSourceTablesResponseRowXml();

        // Mandatory
        row.setTableName(apiRow.tableName());
        row.setTableType(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.TableTypeEnum.fromValue(apiRow.tableType().getValue()));

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);

        return row;
    }

    public static DbSchemaSourceTablesRequest fromDiscoverDbSchemaSourceTables(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DbSchemaSourceTablesRestrictionsR restrictions = discoverDdSchemaSourceTablesRestrictions(requestWs);
        return new DbSchemaSourceTablesRequestR(properties, restrictions);
    }

    private static DbSchemaSourceTablesRestrictionsR discoverDdSchemaSourceTablesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(DbSchemaSourceTablesRestrictions.RESTRICTIONS_TABLE_CATALOG);
        String schemaName = map.get(DbSchemaSourceTablesRestrictions.RESTRICTIONS_SCHEMA_NAME);
        String tableName = map.get(DbSchemaSourceTablesRestrictions.RESTRICTIONS_TABLE_NAME);
        String tableType = map.get(DbSchemaSourceTablesRestrictions.RESTRICTIONS_TABLE_TYPE);

        return new DbSchemaSourceTablesRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            tableName, TableTypeEnum.fromValue(tableType));
    }

    public static DbSchemaTablesInfoRequest fromDiscoverDbSchemaTablesInfo(Discover requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        DbSchemaTablesInfoRestrictionsR restrictions = discoverDdSchemaTablesInfoRestrictions(requestWs);
        return new DbSchemaTablesInfoRequestR(properties, restrictions);
    }

    private static DbSchemaTablesInfoRestrictionsR discoverDdSchemaTablesInfoRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String catalogName = map.get(DbSchemaTablesInfoRestrictions.RESTRICTIONS_TABLE_CATALOG);
        String schemaName = map.get(DbSchemaTablesInfoRestrictions.RESTRICTIONS_TABLE_SCHEMA);
        String tableName = map.get(DbSchemaTablesInfoRestrictions.RESTRICTIONS_TABLE_NAME);
        String tableType = map.get(DbSchemaTablesInfoRestrictions.RESTRICTIONS_TABLE_TYPE);

        return new DbSchemaTablesInfoRestrictionsR(
            Optional.ofNullable(catalogName),
            Optional.ofNullable(schemaName),
            tableName,
            TableTypeEnum.fromValue(tableType));
    }

    public static DiscoverResponse toDiscoverDbSchemaTablesInfo(List<DbSchemaTablesInfoResponseRow> responseApi)
        throws JAXBException, IOException {
            List<Row> rows = responseApi.stream()
                .map(Convert::convertDbSchemaTablesInfoResponseRow)
                .toList();

            DiscoverResponse responseWs = new DiscoverResponse();
            responseWs.setReturn(getReturn(rows, DbSchemaTablesInfoResponseRowXml.class));

            return responseWs;
    }

    private static Row convertDbSchemaTablesInfoResponseRow(DbSchemaTablesInfoResponseRow apiRow) {
        DbSchemaTablesInfoResponseRowXml row = new DbSchemaTablesInfoResponseRowXml();

        // Mandatory
        row.setTableName(apiRow.tableName());
        row.setTableType(apiRow.tableType());

        // Optional
        apiRow.catalogName()
            .ifPresent(row::setCatalogName);
        apiRow.schemaName()
            .ifPresent(row::setSchemaName);
        apiRow.tableGuid()
            .ifPresent(row::setTableGuid);
        apiRow.bookmarks()
            .ifPresent(row::setBookmarks);
        apiRow.bookmarkType()
            .ifPresent(row::setBookmarkType);
        apiRow.bookmarkDataType()
            .ifPresent(row::setBookmarkDataType);
        apiRow.bookmarkMaximumLength()
            .ifPresent(row::setBookmarkMaximumLength);
        apiRow.bookmarkInformation()
            .ifPresent(row::setBookmarkInformation);
        apiRow.tableVersion()
            .ifPresent(row::setTableVersion);
        apiRow.cardinality()
            .ifPresent(row::setCardinality);
        apiRow.description()
            .ifPresent(row::setDescription);
        apiRow.tablePropId()
            .ifPresent(row::setTablePropId);

        return row;
    }

    public static StatementRequest fromStatement(Execute requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        StatementR command = new StatementR(requestWs.getCommand().getStatement());
        List<ExecuteParameter> parameters = parameters(requestWs);
        return new StatementRequestR(properties, parameters, command);
    }

    public static ExecuteResponse toStatement(StatementResponse responseApi) {
        Return ret = convertStatementResponse(responseApi.mdDataSet());
        ExecuteResponse responseWs = new ExecuteResponse();
        responseWs.setReturnValue(ret);

        return responseWs;
    }

    public static AlterRequest fromAlter(Execute requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        AlterR command = AlterCommandConvertor.convertAlterCommand(requestWs.getCommand().getAlter());
        List<ExecuteParameter> parameters = parameters(requestWs);
        return new AlterRequestR(properties, parameters, command);
    }


    public static ExecuteResponse toAlter(AlterResponse responseApi) {
        Return ret = converReturnEmptyresult(responseApi.emptyresult());
        ExecuteResponse responseWs = new ExecuteResponse();
        responseWs.setReturnValue(ret);

        return responseWs;
    }

    public static ClearCacheRequest fromClearCache(Execute requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        ClearCache command = convertClearCache(requestWs.getCommand().getClearCache());
        List<ExecuteParameter> parameters = parameters(requestWs);
        return new ClearCacheRequestR(properties, parameters, command);
    }

    public static ExecuteResponse toClearCache(ClearCacheResponse responseApi) {
        Return ret = converReturnEmptyresult(responseApi.emptyresult());
        ExecuteResponse responseWs = new ExecuteResponse();
        responseWs.setReturnValue(ret);

        return responseWs;
    }

    public static CancelRequest fromCancel(Execute requestWs) {
        PropertiesR properties = discoverProperties(propertyList(requestWs));
        Cancel command = convertCancel(requestWs.getCommand().getCancel());
        List<ExecuteParameter> parameters = parameters(requestWs);
        return new CancelRequestR(properties, parameters, command);
    }

    public static ExecuteResponse toCancel(CancelResponse responseApi) {
        Return ret = converReturnEmptyresult(responseApi.emptyresult());
        ExecuteResponse responseWs = new ExecuteResponse();
        responseWs.setReturnValue(ret);

        return responseWs;
    }

    private static Return converReturnEmptyresult(Emptyresult emptyresult) {
        Return ret = new Return();
        ret.setValue(convertEmptyresult(emptyresult));
        return ret;
    }

    private static org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_empty.Emptyresult convertEmptyresult(Emptyresult emptyresult) {
        org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_empty.Emptyresult result = new org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_empty.Emptyresult();
        result.setException(convertException(emptyresult.exception()));
        result.setMessages(convertMessages(emptyresult.messages()));
        return result;
    }

    private static Return convertStatementResponse(Mddataset mdDataSet) {
        Return ret = new Return();
        ret.setValue(MdDataSetConvertor.convertMdDataSet(mdDataSet));
        return ret;
    }

    private static Return getReturn(List<Row> rows, Class<?> cl) throws JAXBException, IOException {
        Schema schema = SchemaUtil.generateSchema("urn:schemas-microsoft-com:xml-analysis:rowset",
            cl);
        Return r = new Return();
        Rowset rs = new Rowset();
        rs.setSchema(schema);
        rs.setRow(rows);
        r.setValue(rs);
        return r;
    }

	private static List<ExecuteParameter> parameters(Execute requestWs) {
		if (requestWs.getParameters() != null) {
			return requestWs.getParameters().stream()
					.map(i -> new ExecuteParameterR(i.getName(), i.getValue())).map(ExecuteParameter.class::cast)
					.toList();
		}
		return List.of();
	}

}

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
package org.eclipse.daanse.xmla.ws.tck;

import static org.eclipse.daanse.xmla.api.common.enums.AuthenticationModeEnum.UNAUTHENTICATED;
import static org.eclipse.daanse.xmla.api.common.enums.ProviderTypeEnum.MDP;
import static org.eclipse.daanse.xmla.ws.tck.SOAPUtil.string;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.xml.soap.SOAPException;
import org.eclipse.daanse.xmla.api.XmlaService;
import org.eclipse.daanse.xmla.api.common.enums.ActionTypeEnum;
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
import org.eclipse.daanse.xmla.api.common.enums.ScopeEnum;
import org.eclipse.daanse.xmla.api.common.enums.SearchableEnum;
import org.eclipse.daanse.xmla.api.common.enums.SetEvaluationContextEnum;
import org.eclipse.daanse.xmla.api.common.enums.StructureEnum;
import org.eclipse.daanse.xmla.api.common.enums.StructureTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TableTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TypeEnum;
import org.eclipse.daanse.xmla.api.discover.DiscoverService;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.ParameterInfo;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MeasureGroupDimension;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.assertj3.XmlAssert;

import jakarta.xml.soap.SOAPMessage;

import javax.xml.transform.TransformerException;

@ExtendWith(ConfigurationExtension.class)
@WithFactoryConfiguration(factoryPid = Constants.PID_MS_SOAP, name = "test-ms-config", location = "?", properties = {
    @Property(key = "xmlaService.target", value = "(" + Constants.XMLASERVICE_FILTER_KEY + "="
        + Constants.XMLASERVICE_FILTER_VALUE + ")"),
    @Property(key = "osgi.soap.endpoint.contextpath", value = Constants.WS_PATH)})
@WithFactoryConfiguration(factoryPid = "org.eclipse.daanse.ws.handler.SOAPLoggingHandler", name = "test-ms-config",
    location = "?", properties = {
    @Property(key = "osgi.soap.endpoint.selector", value = "(service.pid=*)")})
@RequireServiceComponentRuntime
class DiscoverResponseTest {

    private static final String ONE_0X00000001 = "0x00000001";

	private static final String ONE_0X0001 = "0x0001";

	private static final String TABLE_TYPE_LOW = "tableType";

	private static final String TABLE_SCHEMA_LOW = "tableSchema";

	private static final String TABLE_NAME_LOW = "tableName";

	private static final String TABLE_CATALOG_LOW = "tableCatalog";

	private static final String SCHEMA_NAME_LOW = "schemaName";

	private static final String MEMBER_UNIQUE_NAME = "memberUniqueName";

	private static final String MEASURE_GROUP_NAME = "measureGroupName";

	private static final String MEASURE_CAPTION = "measureCaption";

	private static final String LEVEL_UNIQUE_NAME_LOW = "levelUniqueName";

	private static final String HIERARCHY_UNIQUE_NAME_LOW = "hierarchyUniqueName";

	private static final String FALSE = "false";

	private static final String EXPRESSION_LOW = "expression";

	private static final String DIMENSION_UNIQUE_NAME_LOW = "dimensionUniqueName";

	private static final String DESCRIPTION_LOW = "description";

	private static final String CUBE_NAME_LOW = "cubeName";

	private static final String CATALOG_NAME_LOW = "catalogName";

	private static final String TABLE_TYPE = "TABLE_TYPE";

	private static final String TABLE_SCHEMA = "TABLE_SCHEMA";

	private static final String TABLE_NAME = "TABLE_NAME";

	private static final String TABLE_CATALOG = "TABLE_CATALOG";

	private static final String SCOPE = "SCOPE";

	private static final String SCHEMA_NAME = "SCHEMA_NAME";

	private static final String NUMERIC_SCALE = "NUMERIC_SCALE";

	private static final String NUMERIC_PRECISION = "NUMERIC_PRECISION";

	private static final String MEASUREGROUP_NAME = "MEASUREGROUP_NAME";

	private static final String LEVEL_UNIQUE_NAME = "LEVEL_UNIQUE_NAME";

	private static final String HIERARCHY_UNIQUE_NAME = "HIERARCHY_UNIQUE_NAME";

	private static final String EXPRESSION = "EXPRESSION";

	private static final String DIMENSION_UNIQUE_NAME = "DIMENSION_UNIQUE_NAME";

	private static final String DIMENSION_IS_VISIBLE = "DIMENSION_IS_VISIBLE";

	private static final String DESCRIPTION = "DESCRIPTION";

	private static final String DATA_TYPE = "DATA_TYPE";

	private static final String CUBE_NAME = "CUBE_NAME";

	private static final String CATALOG_NAME = "CATALOG_NAME";

	private static final String DATE = "2023-02-16T10:10";

	private static final String SOAP_ENVELOPE_SOAP_BODY_MSXMLA_DISCOVER_RESPONSE_RETURN_ROWSET_ROOT_ROWSET_ROW_ROWSET = "/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root/rowset:row/rowset";

	private Logger logger = LoggerFactory.getLogger(DiscoverResponseTest.class);

    @InjectBundleContext
    BundleContext bc;

    @BeforeEach
    void beforaEach() {
        XmlaService xmlaService = mock(XmlaService.class);
        DiscoverService discoverService = mock(DiscoverService.class);

        when(xmlaService.discover()).thenReturn(discoverService);

        bc.registerService(XmlaService.class, xmlaService, FrameworkUtil
            .asDictionary(Map.of(Constants.XMLASERVICE_FILTER_KEY, Constants.XMLASERVICE_FILTER_VALUE)));
    }

    public static final String REQUEST = """
        <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
            <RequestType>%s</RequestType>
            <Restrictions>
                <RestrictionList>
                    <TABLE_TYPE>ALIAS</TABLE_TYPE>
                    <COORDINATE_TYPE>1</COORDINATE_TYPE>
                    <INVOCATION>1</INVOCATION>
                </RestrictionList>
            </Restrictions>
            <Properties>
                <PropertyList>
                </PropertyList>
            </Properties>
        </Discover>
        """;

    @Test
    void testDiscoverDataSources(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {
        DiscoverDataSourcesResponseRowR row = new DiscoverDataSourcesResponseRowR("dataSourceName",
            Optional.of("dataSourceDescription"), Optional.of("url"),
            Optional.of("dataSourceInfo"), "providerName",
            Optional.of(MDP), Optional.of(UNAUTHENTICATED));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dataSources(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "DISCOVER_DATASOURCES")));

        logger.debug("DiscoverDataSources response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.of(
            "DataSourceName", "dataSourceName",
            "DataSourceDescription", "dataSourceDescription",
            "URL", "url",
            "DataSourceInfo", "dataSourceInfo",
            "ProviderName", "providerName",
            "ProviderType", "MDP",
            "AuthenticationMode", "Unauthenticated"
        ));
    }

    @Test
    void testDiscoverEnumerators(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DiscoverEnumeratorsResponseRowR row = new DiscoverEnumeratorsResponseRowR("enumName",
            Optional.of("enumDescription"), "enumType", "elementName",
            Optional.of("elementDescription"), Optional.of("elementValue"));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.discoverEnumerators(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "DISCOVER_ENUMERATORS")));

        logger.debug("DiscoverEnumerators response :");
        String responseStr =  string(response);
        logger.debug(responseStr);


        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.of(
            "EnumName", "enumName",
            "EnumDescription", "enumDescription",
            "EnumType", "enumType",
            "ElementName", "elementName",
            "ElementDescription", "elementDescription",
            "ElementValue", "elementValue"
        ));
    }

    @Test
    void testDiscoverKeywords(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DiscoverKeywordsResponseRowR row = new DiscoverKeywordsResponseRowR("keyword");

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.discoverKeywords(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "DISCOVER_KEYWORDS")));

        logger.debug("DiscoverKeywords response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.of(
            "Keyword", "keyword"
        ));
    }

    @Test
    void testDiscoverLiterals(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DiscoverLiteralsResponseRowR row = new DiscoverLiteralsResponseRowR("literalName",
            "literalValue", "literalInvalidChars", "literalInvalidStartingChars",
            10, LiteralNameEnumValueEnum.DBLITERAL_BINARY_LITERAL);

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.discoverLiterals(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "DISCOVER_LITERALS")));

        logger.debug("DiscoverLiterals response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.of(
            "LiteralName", "literalName",
            "LiteralValue", "literalValue",
            "LiteralInvalidChars", "literalInvalidChars",
            "LiteralInvalidStartingChars", "literalInvalidStartingChars",
            "LiteralMaxLength", "10",
            "LiteralNameValue", "1"
        ));
    }

    @Test
    void testDiscoverProperties(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DiscoverPropertiesResponseRowR row = new DiscoverPropertiesResponseRowR("DbpropMsmdSubqueries",
            Optional.of("An enumeration value that determines the behavior of subqueries."), Optional.of("Integer"),
            "ReadWrite", Optional.of(false), Optional.of("1"));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.discoverProperties(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "DISCOVER_PROPERTIES"
            )));

        logger.debug("DiscoverProperties response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        xmlAssert.hasXPath("/SOAP:Envelope");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root/rowset:row")
            .exist();
        xmlAssert.valueByXPath("count(/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root/rowset:row)"
        ).isEqualTo(1);

        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_DISCOVER_RESPONSE_RETURN_ROWSET_ROOT_ROWSET_ROW_ROWSET +
            ":PropertyName")
            .isEqualTo("DbpropMsmdSubqueries");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_DISCOVER_RESPONSE_RETURN_ROWSET_ROOT_ROWSET_ROW_ROWSET +
            ":PropertyDescription")
            .isEqualTo("An enumeration value that determines the behavior of subqueries.");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_DISCOVER_RESPONSE_RETURN_ROWSET_ROOT_ROWSET_ROW_ROWSET +
            ":PropertyAccessType")
            .isEqualTo("ReadWrite");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_DISCOVER_RESPONSE_RETURN_ROWSET_ROOT_ROWSET_ROW_ROWSET +
            ":IsRequired")
            .isEqualTo(FALSE);
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_DISCOVER_RESPONSE_RETURN_ROWSET_ROOT_ROWSET_ROW_ROWSET +
            ":Value")
            .isEqualTo("1");
    }

    @Test
    void testDiscoverSchemaRowSets(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DiscoverSchemaRowsetsResponseRowR row = new DiscoverSchemaRowsetsResponseRowR(SCHEMA_NAME_LOW,
            Optional.of("schemaGuid"), Optional.of("restrictions"), Optional.of(DESCRIPTION_LOW), Optional.of(10l));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.discoverSchemaRowsets(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "DISCOVER_SCHEMA_ROWSETS")));

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.of(
            "SchemaName", SCHEMA_NAME_LOW,
            "SchemaGuid", "schemaGuid",
            "Restrictions", "restrictions",
            "Description", DESCRIPTION_LOW,
            "RestrictionsMask", "10"
        ));
    }

    @Test
    void testDiscoverXmlMetadata(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DiscoverXmlMetaDataResponseRowR row = new DiscoverXmlMetaDataResponseRowR("metaData");

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.xmlMetaData(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "DISCOVER_XML_METADATA")));

        logger.debug("DiscoverXmlMetadata response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.of(
            "MetaData", "metaData"
        ));
    }

    @Test
    void testDbschemaCatalogs(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DbSchemaCatalogsResponseRowR row = new DbSchemaCatalogsResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(DESCRIPTION_LOW),
            Optional.of("roles"),
            Optional.of(LocalDateTime.of(2023, 2, 16, 10, 10)),
            Optional.of(1), Optional.of(TypeEnum.MULTIDIMENSIONAL), Optional.of(2), Optional.of("databaseId"),
            Optional.of(LocalDateTime.of(2023, 2, 16, 10, 10)),
            Optional.of(true), Optional.of(1.1d), Optional.of(1.2d),
            Optional.of(ClientCacheRefreshPolicyEnum.REFRESH_NEWER_DATA));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dbSchemaCatalogs(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "DBSCHEMA_CATALOGS")));

        logger.debug("DbschemaCatalogs response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("ROLES", "roles"),
            Map.entry("DATE_MODIFIED", DATE),
            Map.entry("TYPE", "0x00"),
            Map.entry("VERSION", "2"),
            Map.entry("DATABASE_ID", "databaseId"),
            Map.entry("DATE_QUERIED", DATE),
            Map.entry("CURRENTLY_USED", "true"),
            Map.entry("POPULARITY", "1.1"),
            Map.entry("WEIGHTEDPOPULARITY", "1.2"),
            Map.entry("CLIENTCACHEREFRESHPOLICY", "0")));
    }

    @Test
    void testDbschemaColumns(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DbSchemaColumnsResponseRowR row = new DbSchemaColumnsResponseRowR(
            Optional.of(TABLE_CATALOG_LOW),
            Optional.of(TABLE_SCHEMA_LOW),
            Optional.of(TABLE_NAME_LOW),
            Optional.of("columnName"),
            Optional.of(1),
            Optional.of(2),
            Optional.of(3),
            Optional.of(true),
            Optional.of("columnDefault"),
            Optional.of(ColumnFlagsEnum.DBCOLUMNFLAGS_ISBOOKMARK),
            Optional.of(false),
            Optional.of(4),
            Optional.of(5),
            Optional.of(6),
            Optional.of(7),
            Optional.of(8),
            Optional.of(9),
            Optional.of(10),
            Optional.of("characterSetCatalog"),
            Optional.of("characterSetSchema"),
            Optional.of("characterSetName"),
            Optional.of("collationCatalog"),
            Optional.of("collationSchema"),
            Optional.of("collationName"),
            Optional.of("domainCatalog"),
            Optional.of("domainSchema"),
            Optional.of("domainName"),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(ColumnOlapTypeEnum.ATTRIBUTE)
        );

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dbSchemaColumns(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "DBSCHEMA_COLUMNS")));

        logger.debug("DbschemaColumns response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(TABLE_CATALOG, TABLE_CATALOG_LOW),
            Map.entry(TABLE_SCHEMA, TABLE_SCHEMA_LOW),
            Map.entry(TABLE_NAME, TABLE_NAME_LOW),
            Map.entry("COLUMN_NAME", "columnName"),
            Map.entry("COLUMN_GUID", "1"),
            Map.entry("COLUMN_PROPID", "2"),
            Map.entry("ORDINAL_POSITION", "3"),
            Map.entry("COLUMN_HAS_DEFAULT", "true"),
            Map.entry("COLUMN_DEFAULT", "columnDefault"),
            Map.entry("COLUMN_FLAG", "0x1"),
            Map.entry("IS_NULLABLE", FALSE),
            Map.entry(DATA_TYPE, "4"),
            Map.entry("TYPE_GUID", "5"),
            Map.entry("CHARACTER_MAXIMUM_LENGTH", "6"),
            Map.entry("CHARACTER_OCTET_LENGTH", "7"),
            Map.entry(NUMERIC_PRECISION, "8"),
            Map.entry(NUMERIC_SCALE, "9"),
            Map.entry("DATETIME_PRECISION", "10"),
            Map.entry("CHARACTER_SET_CATALOG", "characterSetCatalog"),
            Map.entry("CHARACTER_SET_SCHEMA", "characterSetSchema"),
            Map.entry("CHARACTER_SET_NAME", "characterSetName"),
            Map.entry("COLLATION_CATALOG", "collationCatalog"),
            Map.entry("COLLATION_SCHEMA", "collationSchema"),
            Map.entry("COLLATION_NAME", "collationName"),
            Map.entry("DOMAIN_CATALOG", "domainCatalog"),
            Map.entry("DOMAIN_SCHEMA", "domainSchema"),
            Map.entry("DOMAIN_NAME", "domainName"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("COLUMN_OLAP_TYPE", "ATTRIBUTE")));
    }

    @Test
    void testDbschemaProviderTypes(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DbSchemaProviderTypesResponseRowR row = new DbSchemaProviderTypesResponseRowR(
            Optional.of("typeName"),
            Optional.of(LevelDbTypeEnum.DBTYPE_EMPTY),
            Optional.of(1),
            Optional.of("literalPrefix"),
            Optional.of("literalSuffix"),
            Optional.of("createParams"),
            Optional.of(true),
            Optional.of(false),
            Optional.of(SearchableEnum.DB_UNSEARCHABLE),
            Optional.of(true),
            Optional.of(false),
            Optional.of(true),
            Optional.of("localTypeName"),
            Optional.of(2),
            Optional.of(3),
            Optional.of(4),
            Optional.of("typeLib"),
            Optional.of("version"),
            Optional.of(false),
            Optional.of(true),
            Optional.of(false));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dbSchemaProviderTypes(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "DBSCHEMA_PROVIDER_TYPES")));

        logger.debug("DbschemaProviderTypes response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry("TYPE_NAME", "typeName"),
            Map.entry(DATA_TYPE, "0"),
            Map.entry("COLUMN_SIZE", "1"),
            Map.entry("LITERAL_PREFIX", "literalPrefix"),
            Map.entry("LITERAL_SUFFIX", "literalSuffix"),
            Map.entry("CREATE_PARAMS", "createParams"),
            Map.entry("IS_NULLABLE", "true"),
            Map.entry("CASE_SENSITIVE", FALSE),
            Map.entry("SEARCHABLE", "0x01"),
            Map.entry("UNSIGNED_ATTRIBUTE", "true"),
            Map.entry("FIXED_PREC_SCALE", FALSE),
            Map.entry("AUTO_UNIQUE_VALUE", "true"),
            Map.entry("LOCAL_TYPE_NAME", "localTypeName"),
            Map.entry("MINIMUM_SCALE", "2"),
            Map.entry("MAXIMUM_SCALE", "3"),
            Map.entry("GUID", "4"),
            Map.entry("TYPE_LIB", "typeLib"),
            Map.entry("VERSION", "version"),
            Map.entry("IS_LONG", FALSE),
            Map.entry("BEST_MATCH", "true"),
            Map.entry("IS_FIXEDLENGTH", FALSE)));
    }

    @Test
    void testDbschemaSchemata(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DbSchemaSchemataResponseRowR row = new DbSchemaSchemataResponseRowR(
            CATALOG_NAME_LOW,
            SCHEMA_NAME_LOW,
            "schemaOwner");

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dbSchemaSchemata(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "DBSCHEMA_SCHEMATA")));

        logger.debug("DbschemaSchemata response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry("SCHEMA_OWNER", "schemaOwner")));
    }

    @Test
    void testDbschemaSourceTables(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DbSchemaSourceTablesResponseRowR row = new DbSchemaSourceTablesResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            TABLE_NAME_LOW,
            TableTypeEnum.ALIAS);

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dbSchemaSourceTables(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "DBSCHEMA_SOURCE_TABLES")));

        logger.debug("DbschemaSourceTables response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(TABLE_CATALOG, CATALOG_NAME_LOW),
            Map.entry(TABLE_SCHEMA, SCHEMA_NAME_LOW),
            Map.entry(TABLE_NAME, TABLE_NAME_LOW),
            Map.entry(TABLE_TYPE, "ALIAS")));
    }

    @Test
    void testDbSchemaTables(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DbSchemaTablesResponseRowR row = new DbSchemaTablesResponseRowR(
            Optional.of(TABLE_CATALOG_LOW),
            Optional.of(TABLE_SCHEMA_LOW),
            Optional.of(TABLE_NAME_LOW),
            Optional.of(TABLE_TYPE_LOW),
            Optional.of("tableGuid"),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(1),
            Optional.of(LocalDateTime.of(2023, 2, 16, 10, 10)),
            Optional.of(LocalDateTime.of(2023, 2, 16, 10, 10)));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dbSchemaTables(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "DBSCHEMA_TABLES")));

        logger.debug("DbSchemaTables response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(TABLE_CATALOG, TABLE_CATALOG_LOW),
            Map.entry(TABLE_SCHEMA, TABLE_SCHEMA_LOW),
            Map.entry(TABLE_NAME, TABLE_NAME_LOW),
            Map.entry(TABLE_TYPE, TABLE_TYPE_LOW),
            Map.entry("TABLE_GUID", "tableGuid"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("TABLE_PROP_ID", "1"),
            Map.entry("DATE_CREATED", DATE),
            Map.entry("DATE_MODIFIED", DATE)));
    }

    @Test
    void testDbSchemaTablesInfo(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        DbSchemaTablesInfoResponseRowR row = new DbSchemaTablesInfoResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            TABLE_NAME_LOW,
            TABLE_TYPE_LOW,
            Optional.of(1),
            Optional.of(true),
            Optional.of(2),
            Optional.of(3),
            Optional.of(4),
            Optional.of(5),
            Optional.of(6l),
            Optional.of(7l),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(8));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.dbSchemaTablesInfo(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "DBSCHEMA_TABLES_INFO")));

        logger.debug("DbSchemaTablesInfo response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(TABLE_CATALOG, CATALOG_NAME_LOW),
            Map.entry(TABLE_SCHEMA, SCHEMA_NAME_LOW),
            Map.entry(TABLE_NAME, TABLE_NAME_LOW),
            Map.entry(TABLE_TYPE, TABLE_TYPE_LOW),
            Map.entry("TABLE_GUID", "1"),
            Map.entry("BOOKMARKS", "true"),
            Map.entry("BOOKMARK_TYPE", "2"),
            Map.entry("BOOKMARK_DATA_TYPE", "3"),
            Map.entry("BOOKMARK_MAXIMUM_LENGTH", "4"),
            Map.entry("BOOKMARK_INFORMATION", "5"),
            Map.entry("TABLE_VERSION", "6"),
            Map.entry("CARDINALITY", "7"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("TABLE_PROP_ID", "8")));
    }

    @Test
    void testMdSchemaActions(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaActionsResponseRowR row = new MdSchemaActionsResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            CUBE_NAME_LOW,
            Optional.of("actionName"),
            Optional.of(ActionTypeEnum.URL),
            "coordinate",
            CoordinateTypeEnum.CUBE,
            Optional.of("actionCaption"),
            Optional.of(DESCRIPTION_LOW),
            Optional.of("content"),
            Optional.of("application"),
            Optional.of(InvocationEnum.NORMAL_OPERATION));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaActions(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "MDSCHEMA_ACTIONS")));

        logger.debug("MdSchemaActions response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry("ACTION_NAME", "actionName"),
            Map.entry("ACTION_TYPE", "0x01"),
            Map.entry("COORDINATE", "coordinate"),
            Map.entry("COORDINATE_TYPE", "1"),
            Map.entry("ACTION_CAPTION", "actionCaption"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("CONTENT", "content"),
            Map.entry("APPLICATION", "application"),
            Map.entry("INVOCATION", "1")));
    }

    @Test
    void testMdSchemaCubes(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaCubesResponseRowR row = new MdSchemaCubesResponseRowR(
            CATALOG_NAME_LOW,
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(CubeTypeEnum.CUBE),
            Optional.of(1),
            Optional.of(LocalDateTime.of(2023, 2, 16, 10, 10)),
            Optional.of(LocalDateTime.of(2023, 2, 16, 10, 10)),
            Optional.of("schemaUpdatedBy"),
            Optional.of(LocalDateTime.of(2023, 2, 16, 10, 10)),
            Optional.of("dataUpdateDBy"),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(true),
            Optional.of(false),
            Optional.of(true),
            Optional.of(false),
            Optional.of("cubeCaption"),
            Optional.of("baseCubeName"),
            Optional.of(CubeSourceEnum.CUBE),
            Optional.of(PreferredQueryPatternsEnum.CROSS_JOIN));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaCubes(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "MDSCHEMA_CUBES")));

        logger.debug("MdSchemaCubes response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry("CUBE_TYPE", "CUBE"),
            Map.entry("CUBE_GUID", "1"),
            Map.entry("CREATED_ON", DATE),
            Map.entry("LAST_SCHEMA_UPDATE", DATE),
            Map.entry("SCHEMA_UPDATED_BY", "schemaUpdatedBy"),
            Map.entry("LAST_DATA_UPDATE", DATE),
            Map.entry("DATA_UPDATED_BY", "dataUpdateDBy"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("IS_DRILLTHROUGH_ENABLED", "true"),
            Map.entry("IS_LINKABLE", FALSE),
            Map.entry("IS_WRITE_ENABLED", "true"),
            Map.entry("IS_SQL_ENABLED", FALSE),
            Map.entry("CUBE_CAPTION", "cubeCaption"),
            Map.entry("BASE_CUBE_NAME", "baseCubeName"),
            Map.entry("CUBE_SOURCE", "0x01"),
            Map.entry("PREFERRED_QUERY_PATTERNS", "0x00")
        ));
    }

    @Test
    void testMdSchemaDimensions(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaDimensionsResponseRowR row = new MdSchemaDimensionsResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of("dimensionName"),
            Optional.of(DIMENSION_UNIQUE_NAME_LOW),
            Optional.of(1),
            Optional.of("dimensionCaption"),
            Optional.of(2),
            Optional.of(DimensionTypeEnum.UNKNOWN),
            Optional.of(3),
            Optional.of("defaultHierarchy"),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(true),
            Optional.of(false),
            Optional.of(DimensionUniqueSettingEnum.MEMBER_KEY),
            Optional.of("dimensionMasterName"),
            Optional.of(true));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaDimensions(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST, "MDSCHEMA_DIMENSIONS"
            )));

        logger.debug("MdSchemaDimensions response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry("DIMENSION_NAME", "dimensionName"),
            Map.entry(DIMENSION_UNIQUE_NAME, DIMENSION_UNIQUE_NAME_LOW),
            Map.entry("DIMENSION_GUID", "1"),
            Map.entry("DIMENSION_CAPTION", "dimensionCaption"),
            Map.entry("DIMENSION_ORDINAL", "2"),
            Map.entry("DIMENSION_TYPE", "0"),
            Map.entry("DIMENSION_CARDINALITY", "3"),
            Map.entry("DEFAULT_HIERARCHY", "defaultHierarchy"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("IS_VIRTUAL", "true"),
            Map.entry("IS_READWRITE", FALSE),
            Map.entry("DIMENSION_UNIQUE_SETTINGS", ONE_0X00000001),
            Map.entry("DIMENSION_MASTER_NAME", "dimensionMasterName"),
            Map.entry(DIMENSION_IS_VISIBLE, "true")
        ));
    }

    @Test
    void testMdSchemaFunctions(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        List<ParameterInfo> parameterInfoList = List.of(new ParameterInfoR(
            "name",
            DESCRIPTION_LOW,
            true,
            false,
            1));
        MdSchemaFunctionsResponseRowR row = new MdSchemaFunctionsResponseRowR(
            Optional.of("functionalName"),
            Optional.of(DESCRIPTION_LOW),
            "parameterList",
            Optional.of(1),
            Optional.of(OriginEnum.MSOLAP),
            Optional.of(InterfaceNameEnum.FILTER),
            Optional.of("libraryName"),
            Optional.of("dllName"),
            Optional.of("helpFile"),
            Optional.of("helpContext"),
            Optional.of("object"),
            Optional.of("caption"),
            Optional.of(parameterInfoList),
            Optional.of(DirectQueryPushableEnum.MEASURE));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaFunctions(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_FUNCTIONS")));

        logger.debug("MdSchemaDimensions response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry("FUNCTION_NAME", "functionalName"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("PARAMETER_LIST", "parameterList"),
            Map.entry("RETURN_TYPE", "1"),
            Map.entry("ORIGIN", "0x1"),
            Map.entry("INTERFACE_NAME", "FILTER"),
            Map.entry("LIBRARY_NAME", "libraryName"),
            Map.entry("DLL_NAME", "dllName"),
            Map.entry("HELP_FILE", "helpFile"),
            Map.entry("HELP_CONTEXT", "helpContext"),
            Map.entry("OBJECT", "object"),
            Map.entry("CAPTION", "caption"),
            Map.entry("PARAMETERINFO", "namedescriptiontruefalse1"),
            Map.entry("DIRECTQUERY_PUSHABLE", "0x1")
        ));
    }

    @Test
    void testMdSchemaHierarchies(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaHierarchiesResponseRowR row = new MdSchemaHierarchiesResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(DIMENSION_UNIQUE_NAME_LOW),
            Optional.of("hierarchyName"),
            Optional.of(HIERARCHY_UNIQUE_NAME_LOW),
            Optional.of(1),
            Optional.of("hierarchyCaption"),
            Optional.of(DimensionTypeEnum.UNKNOWN),
            Optional.of(2),
            Optional.of("defaultMember"),
            Optional.of("allMember"),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(StructureEnum.HIERARCHY_FULLY_BALANCED),
            Optional.of(true),
            Optional.of(false),
            Optional.of(DimensionUniqueSettingEnum.MEMBER_KEY),
            Optional.of("dimensionMasterUniqueName"),
            Optional.of(true),
            Optional.of(3),
            Optional.of(false),
            Optional.of(true),
            Optional.of(HierarchyOriginEnum.USER_DEFINED),
            Optional.of("hierarchyDisplayFolder"),
            Optional.of(InstanceSelectionEnum.DROPDOWN),
            Optional.of(GroupingBehaviorEnum.ENCOURAGED),
            Optional.of(StructureTypeEnum.NATURAL));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaHierarchies(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_HIERARCHIES")));

        logger.debug("MdSchemaHierarchies response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry(DIMENSION_UNIQUE_NAME, DIMENSION_UNIQUE_NAME_LOW),
            Map.entry("HIERARCHY_NAME", "hierarchyName"),
            Map.entry(HIERARCHY_UNIQUE_NAME, HIERARCHY_UNIQUE_NAME_LOW),
            Map.entry("HIERARCHY_GUID", "1"),
            Map.entry("HIERARCHY_CAPTION", "hierarchyCaption"),
            Map.entry("DIMENSION_TYPE", "0"),
            Map.entry("HIERARCHY_CARDINALITY", "2"),
            Map.entry("DEFAULT_MEMBER", "defaultMember"),
            Map.entry("ALL_MEMBER", "allMember"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("STRUCTURE", "0"),
            Map.entry("IS_VIRTUAL", "true"),
            Map.entry("IS_READWRITE", FALSE),
            Map.entry("DIMENSION_UNIQUE_SETTINGS", ONE_0X00000001),
            Map.entry("DIMENSION_MASTER_UNIQUE_NAME", "dimensionMasterUniqueName"),
            Map.entry(DIMENSION_IS_VISIBLE, "true"),
            Map.entry("HIERARCHY_ORDINAL", "3"),
            Map.entry("DIMENSION_IS_SHARED", FALSE),
            Map.entry("HIERARCHY_IS_VISIBLE", "true"),
            Map.entry("HIERARCHY_ORIGIN", ONE_0X0001),
            Map.entry("HIERARCHY_DISPLAY_FOLDER", "hierarchyDisplayFolder"),
            Map.entry("INSTANCE_SELECTION", "1"),
            Map.entry("GROUPING_BEHAVIOR", "1"),
            Map.entry("STRUCTURE_TYPE", "Natural")
        ));


    }

    @Test
    void testMdSchemaKpis(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaKpisResponseRowR row = new MdSchemaKpisResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(MEASURE_GROUP_NAME),
            Optional.of("kpiName"),
            Optional.of("kpiCaption"),
            Optional.of("kpiDescription"),
            Optional.of("kpiDisplayFolder"),
            Optional.of("kpiValue"),
            Optional.of("kpiGoal"),
            Optional.of("kpiStatus"),
            Optional.of("kpiTrend"),
            Optional.of("kpiStatusGraphic"),
            Optional.of("kpiTrendGraphic"),
            Optional.of("kpiWight"),
            Optional.of("kpiCurrentTimeMember"),
            Optional.of("kpiParentKpiName"),
            Optional.of("annotation"),
            Optional.of(ScopeEnum.GLOBAL));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaKpis(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_KPIS")));

        logger.debug("MdSchemaKpis response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry(MEASUREGROUP_NAME, MEASURE_GROUP_NAME),
            Map.entry("KPI_NAME", "kpiName"),
            Map.entry("KPI_CAPTION", "kpiCaption"),
            Map.entry("KPI_DESCRIPTION", "kpiDescription"),
            Map.entry("KPI_DISPLAY_FOLDER", "kpiDisplayFolder"),
            Map.entry("KPI_VALUE", "kpiValue"),
            Map.entry("KPI_GOAL", "kpiGoal"),
            Map.entry("KPI_STATUS", "kpiStatus"),
            Map.entry("KPI_TREND", "kpiTrend"),
            Map.entry("KPI_STATUS_GRAPHIC", "kpiStatusGraphic"),
            Map.entry("KPI_TREND_GRAPHIC", "kpiTrendGraphic"),
            Map.entry("KPI_WEIGHT", "kpiWight"),
            Map.entry("KPI_CURRENT_TIME_MEMBER", "kpiCurrentTimeMember"),
            Map.entry("KPI_PARENT_KPI_NAME", "kpiParentKpiName"),
            Map.entry("ANNOTATIONS", "annotation"),
            Map.entry(SCOPE, "1")
        ));
    }

    @Test
    void testMdSchemaLevels(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaLevelsResponseRowR row = new MdSchemaLevelsResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(DIMENSION_UNIQUE_NAME_LOW),
            Optional.of(HIERARCHY_UNIQUE_NAME_LOW),
            Optional.of("levelName"),
            Optional.of(LEVEL_UNIQUE_NAME_LOW),
            Optional.of(1),
            Optional.of("levelCaption"),
            Optional.of(2),
            Optional.of(3),
            Optional.of(LevelTypeEnum.ALL),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(CustomRollupSettingEnum.CUSTOM_ROLLUP_EXPRESSION_EXIST),
            Optional.of(LevelUniqueSettingsEnum.KEY_COLUMNS),
            Optional.of(true),
            Optional.of("levelOrderingProperty"),
            Optional.of(LevelDbTypeEnum.DBTYPE_EMPTY),
            Optional.of("levelMasterUniqueName"),
            Optional.of("levelNameSqlColumnName"),
            Optional.of("levelKeySqlColumnName"),
            Optional.of("levelUniqueNameSqlColumnName"),
            Optional.of("levelAttributeHierarchyName"),
            Optional.of(4),
            Optional.of(LevelOriginEnum.USER_DEFINED));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaLevels(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_LEVELS")));

        logger.debug("testMdSchemaLevels response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry(DIMENSION_UNIQUE_NAME, DIMENSION_UNIQUE_NAME_LOW),
            Map.entry(HIERARCHY_UNIQUE_NAME, HIERARCHY_UNIQUE_NAME_LOW),
            Map.entry("LEVEL_NAME", "levelName"),
            Map.entry(LEVEL_UNIQUE_NAME, LEVEL_UNIQUE_NAME_LOW),
            Map.entry("LEVEL_GUID", "1"),
            Map.entry("LEVEL_CAPTION", "levelCaption"),
            Map.entry("LEVEL_NUMBER", "2"),
            Map.entry("LEVEL_CARDINALITY", "3"),
            Map.entry("LEVEL_TYPE", ONE_0X0001),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("CUSTOM_ROLLUP_SETTINGS", "0x01"),
            Map.entry("LEVEL_UNIQUE_SETTINGS", ONE_0X00000001),
            Map.entry("LEVEL_IS_VISIBLE", "true"),
            Map.entry("LEVEL_ORDERING_PROPERTY", "levelOrderingProperty"),
            Map.entry("LEVEL_DBTYPE", "0"),
            Map.entry("LEVEL_MASTER_UNIQUE_NAME", "levelMasterUniqueName"),
            Map.entry("LEVEL_NAME_SQL_COLUMN_NAME", "levelNameSqlColumnName"),
            Map.entry("LEVEL_KEY_SQL_COLUMN_NAME", "levelKeySqlColumnName"),
            Map.entry("LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME", "levelUniqueNameSqlColumnName"),
            Map.entry("LEVEL_ATTRIBUTE_HIERARCHY_NAME", "levelAttributeHierarchyName"),
            Map.entry("LEVEL_KEY_CARDINALITY", "4"),
            Map.entry("LEVEL_ORIGIN", ONE_0X0001)
        ));
    }

    @Test
    void testMdSchemaMeasuregroupDimensions(@InjectService XmlaService xmlaService) throws SOAPException, IOException, TransformerException {

        List<MeasureGroupDimension> list = List.of(new MeasureGroupDimensionR("measureGroupDimension"));
        MdSchemaMeasureGroupDimensionsResponseRowR row = new MdSchemaMeasureGroupDimensionsResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(MEASURE_GROUP_NAME),
            Optional.of("measureGroupCardinality"),
            Optional.of(DIMENSION_UNIQUE_NAME_LOW),
            Optional.of(DimensionCardinalityEnum.ONE),
            Optional.of(true),
            Optional.of(false),
            Optional.of(list),
            Optional.of("dimensionGranularity"));

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaMeasureGroupDimensions(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_MEASUREGROUP_DIMENSIONS")));

        logger.debug("MdSchemaMeasuregroupDimensions response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry(MEASUREGROUP_NAME, MEASURE_GROUP_NAME),
            Map.entry("MEASUREGROUP_CARDINALITY", "measureGroupCardinality"),
            Map.entry(DIMENSION_UNIQUE_NAME, DIMENSION_UNIQUE_NAME_LOW),
            Map.entry("DIMENSION_CARDINALITY", "ONE"),
            Map.entry(DIMENSION_IS_VISIBLE, "true"),
            Map.entry("DIMENSION_IS_FACT_DIMENSION", FALSE),
            Map.entry("DIMENSION_PATH", "measureGroupDimension"),
            Map.entry("DIMENSION_GRANULARITY", "dimensionGranularity")
        ));
    }

    @Test
    void testMdSchemaMeasuregroups(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaMeasureGroupsResponseRowR row = new MdSchemaMeasureGroupsResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(MEASURE_GROUP_NAME),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(true),
            Optional.of("measureGroupCaption")
        );

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaMeasureGroups(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_MEASUREGROUPS")));

        logger.debug("MdSchemaMeasuregroups response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry(MEASUREGROUP_NAME, MEASURE_GROUP_NAME),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("IS_WRITE_ENABLED", "true"),
            Map.entry("MEASUREGROUP_CAPTION", "measureGroupCaption")
        ));
    }

    @Test
    void testMdSchemaMeasures(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaMeasuresResponseRowR row = new MdSchemaMeasuresResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of("measureName"),
            Optional.of("measureUniqueName"),
            Optional.of(MEASURE_CAPTION),
            Optional.of(1),
            Optional.of(MeasureAggregatorEnum.MDMEASURE_AGGR_UNKNOWN),
            Optional.of(LevelDbTypeEnum.DBTYPE_EMPTY),
            Optional.of(2),
            Optional.of(3),
            Optional.of("measureUnits"),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(EXPRESSION_LOW),
            Optional.of(true),
            Optional.of("levelsList"),
            Optional.of("measureNameSqlColumnName"),
            Optional.of("measureUnqualifiedCaption"),
            Optional.of(MEASURE_GROUP_NAME),
            Optional.of("defaultFormatString")
        );

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaMeasures(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_MEASURES")));

        logger.debug("MdSchemaMeasures response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry("MEASURE_NAME", "measureName"),
            Map.entry("MEASURE_UNIQUE_NAME", "measureUniqueName"),
            Map.entry("MEASURE_CAPTION", MEASURE_CAPTION),
            Map.entry("MEASURE_GUID","1"),
            Map.entry("MEASURE_AGGREGATOR", "0"),
            Map.entry(DATA_TYPE, "0"),
            Map.entry(NUMERIC_PRECISION, "2"),
            Map.entry(NUMERIC_SCALE, "3"),
            Map.entry("MEASURE_UNITS", "measureUnits"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry(EXPRESSION, EXPRESSION_LOW),
            Map.entry("MEASURE_IS_VISIBLE", "true"),
            Map.entry("LEVELS_LIST", "levelsList"),
            Map.entry("MEASURE_NAME_SQL_COLUMN_NAME", "measureNameSqlColumnName"),
            Map.entry("MEASURE_UNQUALIFIED_CAPTION", "measureUnqualifiedCaption"),
            Map.entry(MEASUREGROUP_NAME, MEASURE_GROUP_NAME),
            Map.entry("DEFAULT_FORMAT_STRING", "defaultFormatString")
        ));
    }

    @Test
    void testMdSchemaMembers(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaMembersResponseRowR row = new MdSchemaMembersResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(DIMENSION_UNIQUE_NAME_LOW),
            Optional.of(HIERARCHY_UNIQUE_NAME_LOW),
            Optional.of(LEVEL_UNIQUE_NAME_LOW),
            Optional.of(1),
            Optional.of(2),
            Optional.of("memberName"),
            Optional.of(MEMBER_UNIQUE_NAME),
            Optional.of(MemberTypeEnum.UNKNOWN),
            Optional.of(3),
            Optional.of(MEASURE_CAPTION),
            Optional.of(4),
            Optional.of(5),
            Optional.of("parentUniqueName"),
            Optional.of(6),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(EXPRESSION_LOW),
            Optional.of("memberKey"),
            Optional.of(true),
            Optional.of(false),
            Optional.of(ScopeEnum.GLOBAL)
        );

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaMembers(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_MEMBERS")));

        logger.debug("MdSchemaMembers response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry(DIMENSION_UNIQUE_NAME, DIMENSION_UNIQUE_NAME_LOW),
            Map.entry(HIERARCHY_UNIQUE_NAME, HIERARCHY_UNIQUE_NAME_LOW),
            Map.entry(LEVEL_UNIQUE_NAME, LEVEL_UNIQUE_NAME_LOW),
            Map.entry("LEVEL_NUMBER", "1"),
            Map.entry("MEMBER_ORDINAL", "2"),
            Map.entry("MEMBER_NAME", "memberName"),
            Map.entry("MEMBER_UNIQUE_NAME", MEMBER_UNIQUE_NAME),
            Map.entry("MEMBER_TYPE", "0"),
            Map.entry("MEMBER_GUID", "3"),
            Map.entry("MEMBER_CAPTION", MEASURE_CAPTION),
            Map.entry("CHILDREN_CARDINALITY", "4"),
            Map.entry("PARENT_LEVEL", "5"),
            Map.entry("PARENT_UNIQUE_NAME", "parentUniqueName"),
            Map.entry("PARENT_COUNT", "6"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry(EXPRESSION, EXPRESSION_LOW),
            Map.entry("MEMBER_KEY", "memberKey"),
            Map.entry("IS_PLACEHOLDERMEMBER", "true"),
            Map.entry("IS_DATAMEMBER", FALSE),
            Map.entry(SCOPE, "1")
        ));
    }

    @Test
    void testMdSchemaProperties(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaPropertiesResponseRowR row = new MdSchemaPropertiesResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of(DIMENSION_UNIQUE_NAME_LOW),
            Optional.of(HIERARCHY_UNIQUE_NAME_LOW),
            Optional.of(LEVEL_UNIQUE_NAME_LOW),
            Optional.of(MEMBER_UNIQUE_NAME),
            Optional.of(PropertyTypeEnum.PROPERTY_MEMBER),
            Optional.of("propertyName"),
            Optional.of("propertyCaption"),
            Optional.of(LevelDbTypeEnum.DBTYPE_EMPTY),
            Optional.of(1),
            Optional.of(2),
            Optional.of(3),
            Optional.of(4),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(PropertyContentTypeEnum.REGULAR),
            Optional.of("sqlColumnName"),
            Optional.of(5),
            Optional.of(PropertyOriginEnum.USER_DEFINED),
            Optional.of("propertyAttributeHierarchyName"),
            Optional.of(PropertyCardinalityEnum.ONE),
            Optional.of("mimeType"),
            Optional.of(true)
        );

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaProperties(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_PROPERTIES")));

        logger.debug("MdSchemaProperties response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry(DIMENSION_UNIQUE_NAME, DIMENSION_UNIQUE_NAME_LOW),
            Map.entry(HIERARCHY_UNIQUE_NAME, HIERARCHY_UNIQUE_NAME_LOW),
            Map.entry(LEVEL_UNIQUE_NAME, LEVEL_UNIQUE_NAME_LOW),
            Map.entry("MEMBER_UNIQUE_NAME", MEMBER_UNIQUE_NAME),
            Map.entry("PROPERTY_TYPE", "1"),
            Map.entry("PROPERTY_NAME", "propertyName"),
            Map.entry("PROPERTY_CAPTION", "propertyCaption"),
            Map.entry(DATA_TYPE, "0"),
            Map.entry("CHARACTER_MAXIMUM_LENGTH", "1"),
            Map.entry("CHARACTER_OCTET_LENGTH", "2"),
            Map.entry(NUMERIC_PRECISION, "3"),
            Map.entry(NUMERIC_SCALE, "4"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry("PROPERTY_CONTENT_TYPE", "0x00"),
            Map.entry("SQL_COLUMN_NAME", "sqlColumnName"),
            Map.entry("LANGUAGE", "5"),
            Map.entry("PROPERTY_ORIGIN", "1"),
            Map.entry("PROPERTY_ATTRIBUTE_HIERARCHY_NAME", "propertyAttributeHierarchyName"),
            Map.entry("PROPERTY_CARDINALITY", "ONE"),
            Map.entry("MIME_TYPE", "mimeType"),
            Map.entry("PROPERTY_IS_VISIBLE", "true")
        ));
    }

    @Test
    void testMdSchemaSets(@InjectService XmlaService xmlaService) throws SOAPException, IOException,
        TransformerException {

        MdSchemaSetsResponseRowR row = new MdSchemaSetsResponseRowR(
            Optional.of(CATALOG_NAME_LOW),
            Optional.of(SCHEMA_NAME_LOW),
            Optional.of(CUBE_NAME_LOW),
            Optional.of("setName"),
            Optional.of(ScopeEnum.GLOBAL),
            Optional.of(DESCRIPTION_LOW),
            Optional.of(EXPRESSION_LOW),
            Optional.of("dimension"),
            Optional.of("setCaption"),
            Optional.of("setDisplayFolder"),
            Optional.of(SetEvaluationContextEnum.STATIC)
        );

        DiscoverService discoverService = xmlaService.discover();
        when(discoverService.mdSchemaSets(any())).thenReturn(List.of(row));

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(String.format(REQUEST,
                "MDSCHEMA_SETS")));

        logger.debug("MdSchemaSets response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);

        checkRowValues(xmlAssert, Map.ofEntries(
            Map.entry(CATALOG_NAME, CATALOG_NAME_LOW),
            Map.entry(SCHEMA_NAME, SCHEMA_NAME_LOW),
            Map.entry(CUBE_NAME, CUBE_NAME_LOW),
            Map.entry("SET_NAME", "setName"),
            Map.entry(SCOPE,  "1"),
            Map.entry(DESCRIPTION, DESCRIPTION_LOW),
            Map.entry(EXPRESSION, EXPRESSION_LOW),
            Map.entry("DIMENSIONS", "dimension"),
            Map.entry("SET_CAPTION", "setCaption"),
            Map.entry("SET_DISPLAY_FOLDER", "setDisplayFolder"),
            Map.entry("SET_EVALUATION_CONTEXT", "1")
        ));
    }

    private void checkRow(XmlAssert xmlAssert) {
        xmlAssert.hasXPath("/SOAP:Envelope");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root/rowset:row")
            .exist();
        xmlAssert.valueByXPath("count(/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root/rowset:row)"
        ).isEqualTo(1);
    }

    private void checkRowValues(XmlAssert xmlAssert, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            xmlAssert.valueByXPath(new StringBuilder("/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset" +
                ":root/rowset:row/rowset:").append(entry.getKey()).toString())
                .isEqualTo(entry.getValue());
        }
    }
}

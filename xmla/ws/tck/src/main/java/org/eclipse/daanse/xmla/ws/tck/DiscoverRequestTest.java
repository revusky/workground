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

import org.eclipse.daanse.xmla.api.XmlaService;
import org.eclipse.daanse.xmla.api.common.enums.ActionTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.AuthenticationModeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ColumnOlapTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.CubeSourceEnum;
import org.eclipse.daanse.xmla.api.common.enums.LevelDbTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.MemberTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ObjectExpansionEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyOriginEnum;
import org.eclipse.daanse.xmla.api.common.enums.PropertyTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ProviderTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ScopeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TableTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TreeOpEnum;
import org.eclipse.daanse.xmla.api.common.enums.VisibilityEnum;
import org.eclipse.daanse.xmla.api.discover.DiscoverService;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoRequest;
import org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRequest;
import org.eclipse.daanse.xmla.api.discover.discover.enumerators.DiscoverEnumeratorsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.keywords.DiscoverKeywordsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.literals.DiscoverLiteralsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.properties.DiscoverPropertiesRequest;
import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.xmlmetadata.DiscoverXmlMetaDataRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.actions.MdSchemaActionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.cubes.MdSchemaCubesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.demensions.MdSchemaDimensionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.hierarchies.MdSchemaHierarchiesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.levels.MdSchemaLevelsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroups.MdSchemaMeasureGroupsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measures.MdSchemaMeasuresRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.members.MdSchemaMembersRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.properties.MdSchemaPropertiesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.sets.MdSchemaSetsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.xmla.api.common.enums.CoordinateTypeEnum.CUBE;
import static org.eclipse.daanse.xmla.api.common.enums.InvocationEnum.NORMAL_OPERATION;
import static org.eclipse.daanse.xmla.api.common.properties.Content.SCHEMA_DATA;
import static org.eclipse.daanse.xmla.api.common.properties.Format.TABULAR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ConfigurationExtension.class)
@WithFactoryConfiguration(factoryPid = Constants.PID_MS_SOAP, name = "test-ms-config", location = "?", properties = {
        @Property(key = "xmlaService.target", value = "(" + Constants.XMLASERVICE_FILTER_KEY + "="
                + Constants.XMLASERVICE_FILTER_VALUE + ")"),
        @Property(key = "osgi.soap.endpoint.contextpath", value = Constants.WS_PATH) })
@WithFactoryConfiguration(factoryPid = "org.eclipse.daanse.ws.handler.SOAPLoggingHandler", name = "test-ms-config", location = "?", properties = {
        @Property(key = "osgi.soap.endpoint.selector", value = "(service.pid=*)") })
@RequireServiceComponentRuntime
class DiscoverRequestTest {

    private static final String TABLE_NAME = "tableName";
	private static final String SCHEMA_NAME = "schemaName";
	private static final String MEASURE_GROUP_NAME = "measureGroupName";
	private static final String LEVEL_UNIQUE_NAME = "levelUniqueName";
	private static final String HIERARCHY_UNIQUE_NAME = "hierarchyUniqueName";
	private static final String DIMENSION_UNIQUE_NAME = "dimensionUniqueName";
	private static final String CUBE_NAME = "cubeName";
	private static final String CATALOG_NAME = "catalogName";
	private static final String FOOD_MART = "FoodMart";
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

    @Test
    void testDiscoverPropertiesLocaleIdentifier(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<DiscoverPropertiesRequest> captor = ArgumentCaptor.forClass(DiscoverPropertiesRequest.class);

        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                  <RequestType>DISCOVER_PROPERTIES</RequestType>
                  <Restrictions>
                    <RestrictionList>
                    </RestrictionList>
                  </Restrictions>
                  <Properties>
                    <PropertyList>
                      <LocaleIdentifier>1033</LocaleIdentifier>
                    </PropertyList>
                  </Properties>
                </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).discoverProperties(captor.capture());

        DiscoverPropertiesRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isPresent()
                                        .contains(1033);
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.content()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.format()).isNotNull()
                                        .isNotPresent();
                            });
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r ->
                                assertThat(r.propertyName()).isNotNull()
                                        .isNotPresent()
                            );
                });
    }

    @Test
    void testDiscoverPropertiesRestricted(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<DiscoverPropertiesRequest> captor = ArgumentCaptor.forClass(DiscoverPropertiesRequest.class);

        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                  <RequestType>DISCOVER_PROPERTIES</RequestType>
                  <Restrictions>
                    <RestrictionList>
                      <PropertyName>ProviderVersion</PropertyName>
                    </RestrictionList>
                  </Restrictions>
                  <Properties>
                    <PropertyList>
                      <LocaleIdentifier>1033</LocaleIdentifier>
                      <DataSourceInfo>FoodMart</DataSourceInfo>
                      <Content>SchemaData</Content>
                      <Format>Tabular</Format>
                    </PropertyList>
                  </Properties>
                </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).discoverProperties(captor.capture());

        DiscoverPropertiesRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isPresent()
                                        .contains(1033);
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .isPresent()
                                        .contains(FOOD_MART);
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                                assertThat(p.format()).isNotNull()
                                        .isPresent()
                                        .contains(TABULAR);
                            });
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r ->
                                assertThat(r.propertyName()).isNotNull()
                                        .isPresent()
                                        .contains("ProviderVersion")
                            );
                });
    }

    @Test
    void testDiscoverSchemaRowSets(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<DiscoverSchemaRowsetsRequest> captor = ArgumentCaptor
                .forClass(DiscoverSchemaRowsetsRequest.class);
        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                  <RequestType>DISCOVER_SCHEMA_ROWSETS</RequestType>
                  <Restrictions>
                    <RestrictionList>
                      <SchemaName>FoodMart</SchemaName>
                    </RestrictionList>
                  </Restrictions>
                  <Properties>
                    <PropertyList>
                      <LocaleIdentifier>1033</LocaleIdentifier>
                      <DataSourceInfo>FoodMart</DataSourceInfo>
                      <Catalog>FoodMart</Catalog>
                      <Format>Tabular</Format>
                    </PropertyList>
                  </Properties>
                </Discover>
                """;
        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).discoverSchemaRowsets(captor.capture());

        DiscoverSchemaRowsetsRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r ->
                                assertThat(r.schemaName()).isNotNull()
                                        .isPresent()
                                        .contains(FOOD_MART)
                            );
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isPresent()
                                        .contains(1033);
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .isPresent()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .isPresent()
                                        .contains(TABULAR);
                                assertThat(p.catalog()).isNotNull()
                                        .isPresent()
                                        .contains(FOOD_MART);
                            });

                });
    }

    @Test
    void testDiscoverEnumerators(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<DiscoverEnumeratorsRequest> captor = ArgumentCaptor.forClass(DiscoverEnumeratorsRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>DISCOVER_ENUMERATORS</RequestType>
                    <Restrictions>
                      <RestrictionList>
                        <EnumName>FoodMart</EnumName>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                    <DataSourceInfo>FoodMart</DataSourceInfo>
                    <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).discoverEnumerators(captor.capture());

        DiscoverEnumeratorsRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r ->
                                assertThat(r.enumName()).isNotNull()
                                        .contains(FOOD_MART)
                            );
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .isPresent()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testDiscoverKeywords(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<DiscoverKeywordsRequest> captor = ArgumentCaptor.forClass(DiscoverKeywordsRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>DISCOVER_KEYWORDS</RequestType>
                    <Restrictions>
                      <RestrictionList>
                    <Keyword>Keyword1</Keyword>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                        <DataSourceInfo>FoodMart</DataSourceInfo>
                        <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).discoverKeywords(captor.capture());

        DiscoverKeywordsRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r ->
                                assertThat(r.keyword()).isNotNull()
                                        .contains("Keyword1")
                            );
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testDiscoverLiterals(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<DiscoverLiteralsRequest> captor = ArgumentCaptor.forClass(DiscoverLiteralsRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>DISCOVER_LITERALS</RequestType>
                    <Restrictions>
                      <RestrictionList>
                        <LiteralName>literalName</LiteralName>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                        <DataSourceInfo>FoodMart</DataSourceInfo>
                        <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).discoverLiterals(captor.capture());

        DiscoverLiteralsRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r ->
                                assertThat(r.literalName()).isNotNull()
                                        .isPresent()
                                        .contains("literalName")
                            );
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testDbSchemaTables(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<DbSchemaTablesRequest> captor = ArgumentCaptor
                .forClass(DbSchemaTablesRequest.class);
        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                <RequestType>DBSCHEMA_TABLES</RequestType>
                <Restrictions>
                  <RestrictionList>
                    <TABLE_CATALOG>tableCatalog</TABLE_CATALOG>
                    <RESTRICTIONS_TABLE_SCHEMA>tableSchema</RESTRICTIONS_TABLE_SCHEMA>
                    <TABLE_NAME>tableName</TABLE_NAME>
                    <TABLE_TYPE>tableType</TABLE_TYPE>
                  </RestrictionList>
                </Restrictions>
                <Properties>
                  <PropertyList>
                    <DataSourceInfo>FoodMart</DataSourceInfo>
                    <Content>SchemaData</Content>
                  </PropertyList>
                </Properties>
                </Discover>""";

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).dbSchemaTables(captor.capture());

        DbSchemaTablesRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r -> {
                                assertThat(r.tableCatalog()).isNotNull()
                                        .isPresent()
                                        .contains("tableCatalog");
                                assertThat(r.tableName()).isNotNull()
                                        .isPresent()
                                        .contains(TABLE_NAME);
                                assertThat(r.tableType()).isNotNull()
                                        .isPresent()
                                        .contains("tableType");
                                assertThat(r.tableSchema()).isNotNull()
                                        .isPresent()
                                        .contains("tableSchema");
                            });
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testMdSchemaActions(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaActionsRequest> captor = ArgumentCaptor
                .forClass(MdSchemaActionsRequest.class);
        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>MDSCHEMA_ACTIONS</RequestType>
                    <Restrictions>
                      <RestrictionList>
                        <CATALOG_NAME>catalogName</CATALOG_NAME>
                        <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                        <CUBE_NAME>cubeName</CUBE_NAME>
                        <ACTION_NAME>actionName</ACTION_NAME>
                        <ACTION_TYPE>0x01</ACTION_TYPE>
                        <COORDINATE>coordinate</COORDINATE>
                        <COORDINATE_TYPE>1</COORDINATE_TYPE>
                        <INVOCATION>1</INVOCATION>
                        <CUBE_SOURCE>0x01</CUBE_SOURCE>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                        <DataSourceInfo>FoodMart</DataSourceInfo>
                        <Catalog>FoodMart</Catalog>
                        <Format>Tabular</Format>
                        <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                </Discover>""";

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaActions(captor.capture());

        MdSchemaActionsRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r -> {
                                assertThat(r.catalogName()).isNotNull()
                                        .isPresent()
                                        .contains(CATALOG_NAME);
                                assertThat(r.schemaName()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_NAME);
                                assertThat(r.cubeName()).isEqualTo(CUBE_NAME);
                                assertThat(r.actionName()).isNotNull()
                                        .isPresent()
                                        .contains("actionName");
                                assertThat(r.actionType()).isNotNull()
                                        .isPresent()
                                        .contains(ActionTypeEnum.URL);
                                assertThat(r.coordinate()).isNotNull()
                                        .isPresent()
                                        .contains("coordinate");
                                assertThat(r.coordinateType()).isNotNull()
                                        .isEqualTo(CUBE);
                                assertThat(r.invocation()).isNotNull()
                                        .isEqualTo(NORMAL_OPERATION);
                                assertThat(r.cubeSource()).isNotNull()
                                	.isPresent()
                                	.contains(CubeSourceEnum.CUBE);
                            });
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                        .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.catalog()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .contains(TABULAR);
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testMdSchemaCubes(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaCubesRequest> captor = ArgumentCaptor
                .forClass(MdSchemaCubesRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_CUBES</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <BASE_CUBE_NAME>baseCubeName</BASE_CUBE_NAME>
                            <CUBE_SOURCE>0x01</CUBE_SOURCE>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaCubes(captor.capture());

        MdSchemaCubesRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r -> {
                                assertThat(r.schemaName()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_NAME);
                                assertThat(r.cubeName()).isNotNull()
                                        .isPresent()
                                        .contains(CUBE_NAME);
                                assertThat(r.baseCubeName()).isNotNull()
                                        .isPresent()
                                        .contains("baseCubeName");
                                assertThat(r.cubeSource()).isNotNull()
                                        .isPresent()
                                        .contains(CubeSourceEnum.CUBE);
                            });
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.catalog()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .contains(TABULAR);
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testMdSchemaDimensions(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaDimensionsRequest> captor = ArgumentCaptor
                .forClass(MdSchemaDimensionsRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>MDSCHEMA_DIMENSIONS</RequestType>
                    <Restrictions>
                      <RestrictionList>
                        <CATALOG_NAME>catalogName</CATALOG_NAME>
                        <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                        <CUBE_NAME>cubeName</CUBE_NAME>
                        <DIMENSION_NAME>dimensionName</DIMENSION_NAME>
                        <DIMENSION_UNIQUE_NAME>dimensionUniqueName</DIMENSION_UNIQUE_NAME>
                        <CUBE_SOURCE>0x01</CUBE_SOURCE>
                        <DIMENSION_VISIBILITY>0x01</DIMENSION_VISIBILITY>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                        <DataSourceInfo>FoodMart</DataSourceInfo>
                        <Catalog>FoodMart</Catalog>
                        <Format>Tabular</Format>
                        <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaDimensions(captor.capture());

        MdSchemaDimensionsRequest request = captor.getValue();
        assertThat(request).isNotNull()
                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r -> {
                                assertThat(r.catalogName()).isNotNull()
                                        .isPresent()
                                        .contains(CATALOG_NAME);
                                assertThat(r.schemaName()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_NAME);
                                assertThat(r.cubeName()).isNotNull()
                                        .isPresent()
                                        .contains(CUBE_NAME);
                                assertThat(r.dimensionName()).isNotNull()
                                        .isPresent()
                                        .contains("dimensionName");
                                assertThat(r.dimensionUniqueName()).isNotNull()
                                        .isPresent()
                                        .contains(DIMENSION_UNIQUE_NAME);
                                assertThat(r.cubeSource()).isNotNull()
                                    .isPresent()
                                    .contains(CubeSourceEnum.CUBE);
                                assertThat(r.dimensionVisibility()).isNotNull()
                                    .isPresent()
                                    .contains(VisibilityEnum.VISIBLE);
                            });
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.catalog()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.format()).isNotNull()
                                        .contains(TABULAR);
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testMdSchemaFunctions(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaFunctionsRequest> captor = ArgumentCaptor
                .forClass(MdSchemaFunctionsRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>MDSCHEMA_FUNCTIONS</RequestType>
                    <Restrictions>
                      <RestrictionList>
                        <FUNCTION_NAME>Item</FUNCTION_NAME>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                        <DataSourceInfo>FoodMart</DataSourceInfo>
                        <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaFunctions(captor.capture());

        MdSchemaFunctionsRequest request = captor.getValue();
        assertThat(request).isNotNull()

                .satisfies(d -> {
                    // getRestrictions
                    assertThat(d.restrictions()).isNotNull()
                            .satisfies(r ->
                                assertThat(r.origin()).isNotNull()
                                        .isNotPresent()
                            );
                    // getProperties
                    assertThat(d.properties()).isNotNull()
                            .satisfies(p -> {
                                assertThat(p.localeIdentifier()).isNotNull()
                                .isNotPresent();
                                assertThat(p.dataSourceInfo()).isNotNull()
                                        .contains(FOOD_MART);
                                assertThat(p.catalog()).isNotNull()
                                .isNotPresent();
                                assertThat(p.format()).isNotNull()
                                .isNotPresent();
                                assertThat(p.content()).isNotNull()
                                        .isPresent()
                                        .contains(SCHEMA_DATA);
                            });
                });
    }

    @Test
    void testMdSchemaHierarchies(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaHierarchiesRequest> captor = ArgumentCaptor
            .forClass(MdSchemaHierarchiesRequest.class);
        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                <RequestType>MDSCHEMA_HIERARCHIES</RequestType>
                <Restrictions>
                  <RestrictionList>
                    <CATALOG_NAME>catalogName</CATALOG_NAME>
                    <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                    <CUBE_NAME>cubeName</CUBE_NAME>
                    <DIMENSION_UNIQUE_NAME>dimensionUniqueName</DIMENSION_UNIQUE_NAME>
                    <HIERARCHY_NAME>hierarchyName</HIERARCHY_NAME>
                    <HIERARCHY_UNIQUE_NAME>hierarchyUniqueName</HIERARCHY_UNIQUE_NAME>
                    <HIERARCHY_ORIGIN>1</HIERARCHY_ORIGIN>
                    <CUBE_SOURCE>0x01</CUBE_SOURCE>
                    <HIERARCHY_VISIBILITY>0x01</HIERARCHY_VISIBILITY>
                  </RestrictionList>
                </Restrictions>
                <Properties>
                  <PropertyList>
                    <DataSourceInfo>FoodMart</DataSourceInfo>
                    <Content>SchemaData</Content>
                  </PropertyList>
                </Properties>
                </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaHierarchies(captor.capture());

        MdSchemaHierarchiesRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.dimensionUniqueName()).isNotNull()
                            .isPresent().contains(DIMENSION_UNIQUE_NAME);
                        assertThat(r.hierarchyName()).isNotNull()
                            .isPresent().contains("hierarchyName");
                        assertThat(r.hierarchyUniqueName()).isNotNull()
                            .isPresent().contains(HIERARCHY_UNIQUE_NAME);
                        assertThat(r.hierarchyOrigin()).isNotNull()
                            .isPresent().contains(1);
                        assertThat(r.cubeSource()).isNotNull()
                            .isPresent().contains(CubeSourceEnum.CUBE);
                        assertThat(r.hierarchyVisibility()).isNotNull()
                            .isPresent().contains(VisibilityEnum.VISIBLE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isNotPresent();
                        assertThat(p.format()).isNotNull()
                            .isNotPresent();
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testDiscoverDataSources(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<DiscoverDataSourcesRequest> captor = ArgumentCaptor
            .forClass(DiscoverDataSourcesRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>DISCOVER_DATASOURCES</RequestType>
                    <Restrictions>
                      <RestrictionList>
                        <DataSourceName>dataSourceName</DataSourceName>
                        <DataSourceDescription>dataSourceDescription</DataSourceDescription>
                        <URL>url</URL>
                        <DataSourceInfo>dataSourceInfo</DataSourceInfo>
                        <ProviderName>providerName</ProviderName>
                        <ProviderType>DMP</ProviderType>
                        <AuthenticationMode>Authenticated</AuthenticationMode>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                        <DataSourceInfo>FoodMart</DataSourceInfo>
                        <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).dataSources(captor.capture());

        DiscoverDataSourcesRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {

                        assertThat(r.dataSourceName()).isNotNull()
                            .isEqualTo("dataSourceName");
                        assertThat(r.dataSourceDescription()).isNotNull()
                            .isPresent().contains("dataSourceDescription");
                        assertThat(r.url()).isNotNull()
                            .isPresent().contains("url");
                        assertThat(r.dataSourceInfo()).isNotNull()
                            .isPresent().contains("dataSourceInfo");
                        assertThat(r.providerName()).isNotNull()
                            .isEqualTo("providerName");
                        assertThat(r.providerType()).isNotNull()
                            .isPresent().contains(ProviderTypeEnum.DMP);
                        assertThat(r.authenticationMode()).isNotNull()
                            .isPresent().contains(AuthenticationModeEnum.AUTHENTICATED);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isNotPresent();
                        assertThat(p.format()).isNotNull()
                            .isNotPresent();
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testDiscoverXmlMetadata(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<DiscoverXmlMetaDataRequest> captor = ArgumentCaptor
            .forClass(DiscoverXmlMetaDataRequest.class);
        final String sRequest = """
                    <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                    <RequestType>DISCOVER_XML_METADATA</RequestType>
                    <Restrictions>
                      <RestrictionList>
                    <DatabaseID>databaseId</DatabaseID>
                        <DimensionID>dimensionId</DimensionID>
                        <CubeID>cubeId</CubeID>
                        <MeasureGroupID>measureGroupId</MeasureGroupID>
                        <PartitionID>partitionId</PartitionID>
                        <PerspectiveID>perspectiveId</PerspectiveID>
                        <DimensionPermissionID>dimensionPermissionId</DimensionPermissionID>
                        <RoleID>roleId</RoleID>
                        <DatabasePermissionID>databasePermissionId</DatabasePermissionID>
                        <MiningModelID>miningModelId</MiningModelID>
                        <MiningModelPermissionID>miningModelPermissionId</MiningModelPermissionID>
                        <DataSourceID>dataSourceId</DataSourceID>
                        <MiningStructureID>miningStructureId</MiningStructureID>
                        <AggregationDesignID>aggregationDesignId</AggregationDesignID>
                        <TraceID>traceId</TraceID>
                        <MiningStructurePermissionID>miningStructurePermissionId</MiningStructurePermissionID>
                        <CubePermissionID>cubePermissionId</CubePermissionID>
                        <AssemblyID>assemblyId</AssemblyID>
                        <MdxScriptID>mdxScriptId</MdxScriptID>
                        <DataSourceViewID>dataSourceViewId</DataSourceViewID>
                        <DataSourcePermissionID>dataSourcePermissionId</DataSourcePermissionID>
                        <ObjectExpansion>ReferenceOnly</ObjectExpansion>
                      </RestrictionList>
                    </Restrictions>
                    <Properties>
                      <PropertyList>
                        <DataSourceInfo>FoodMart</DataSourceInfo>
                        <Content>SchemaData</Content>
                      </PropertyList>
                    </Properties>
                    </Discover>
                """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).xmlMetaData(captor.capture());

        DiscoverXmlMetaDataRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.databaseId()).isNotNull()
                            .isPresent().contains("databaseId");
                        assertThat(r.dimensionId()).isNotNull()
                            .isPresent().contains("dimensionId");
                        assertThat(r.cubeId()).isNotNull()
                            .isPresent().contains("cubeId");
                        assertThat(r.measureGroupId()).isNotNull()
                            .isPresent().contains("measureGroupId");
                        assertThat(r.partitionId()).isNotNull()
                            .isPresent().contains("partitionId");
                        assertThat(r.perspectiveId()).isNotNull()
                            .isPresent().contains("perspectiveId");
                        assertThat(r.dimensionPermissionId()).isNotNull()
                            .isPresent().contains("dimensionPermissionId");
                        assertThat(r.roleId()).isNotNull()
                            .isPresent().contains("roleId");
                        assertThat(r.dataSourcePermissionId()).isNotNull()
                            .isPresent().contains("dataSourcePermissionId");
                        assertThat(r.miningModelId()).isNotNull()
                            .isPresent().contains("miningModelId");
                        assertThat(r.miningModelPermissionId()).isNotNull()
                            .isPresent().contains("miningModelPermissionId");
                        assertThat(r.dataSourceId()).isNotNull()
                            .isPresent().contains("dataSourceId");
                        assertThat(r.miningStructureId()).isNotNull()
                            .isPresent().contains("miningStructureId");
                        assertThat(r.aggregationDesignId()).isNotNull()
                            .isPresent().contains("aggregationDesignId");
                        assertThat(r.traceId()).isNotNull()
                            .isPresent().contains("traceId");
                        assertThat(r.miningStructurePermissionId()).isNotNull()
                            .isPresent().contains("miningStructurePermissionId");
                        assertThat(r.cubePermissionId()).isNotNull()
                            .isPresent().contains("cubePermissionId");
                        assertThat(r.assemblyId()).isNotNull()
                            .isPresent().contains("assemblyId");
                        assertThat(r.mdxScriptId()).isNotNull()
                            .isPresent().contains("mdxScriptId");
                        assertThat(r.dataSourceViewId()).isNotNull()
                            .isPresent().contains("dataSourceViewId");
                        assertThat(r.dataSourcePermissionId()).isNotNull()
                            .isPresent().contains("dataSourcePermissionId");
                        assertThat(r.objectExpansion()).isNotNull()
                            .isPresent().contains(ObjectExpansionEnum.REFERENCE_ONLY);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isNotPresent();
                        assertThat(p.format()).isNotNull()
                            .isNotPresent();
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testDbSchemaColumns(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<DbSchemaColumnsRequest> captor = ArgumentCaptor
            .forClass(DbSchemaColumnsRequest.class);
        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                <RequestType>DBSCHEMA_COLUMNS</RequestType>
                <Restrictions>
                  <RestrictionList>
                    <TABLE_CATALOG>tableCatalog</TABLE_CATALOG>
                    <TABLE_SCHEMA>tableSchema</TABLE_SCHEMA>
                    <TABLE_NAME>tableName</TABLE_NAME>
                    <COLUMN_NAME>columnName</COLUMN_NAME>
                    <COLUMN_OLAP_TYPE>ATTRIBUTE</COLUMN_OLAP_TYPE>
                  </RestrictionList>
                </Restrictions>
                <Properties>
                  <PropertyList>
                    <DataSourceInfo>FoodMart</DataSourceInfo>
                    <Content>SchemaData</Content>
                  </PropertyList>
                </Properties>
                </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).dbSchemaColumns(captor.capture());

        DbSchemaColumnsRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.tableCatalog()).isNotNull()
                            .isPresent().contains("tableCatalog");
                        assertThat(r.tableSchema()).isNotNull()
                            .isPresent().contains("tableSchema");
                        assertThat(r.tableName()).isNotNull()
                            .isPresent().contains(TABLE_NAME);
                        assertThat(r.columnName()).isNotNull()
                            .isPresent().contains("columnName");
                        assertThat(r.columnOlapType()).isNotNull()
                            .isPresent().contains(ColumnOlapTypeEnum.ATTRIBUTE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isNotPresent();
                        assertThat(p.format()).isNotNull()
                            .isNotPresent();
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testDbSchemaProviderTypes(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<DbSchemaProviderTypesRequest> captor = ArgumentCaptor
            .forClass(DbSchemaProviderTypesRequest.class);
        final String sRequest = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                <RequestType>DBSCHEMA_PROVIDER_TYPES</RequestType>
                <Restrictions>
                  <RestrictionList>
                    <DATA_TYPE>133</DATA_TYPE>
                    <BEST_MATCH>true</BEST_MATCH>
                  </RestrictionList>
                </Restrictions>
                <Properties>
                  <PropertyList>
                    <DataSourceInfo>FoodMart</DataSourceInfo>
                    <Content>SchemaData</Content>
                  </PropertyList>
                </Properties>
                </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).dbSchemaProviderTypes(captor.capture());

        DbSchemaProviderTypesRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.dataType()).isNotNull()
                            .isPresent().contains(LevelDbTypeEnum.DBTYPE_DBDATE);
                        assertThat(r.bestMatch()).isNotNull()
                            .isPresent().contains(true);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isNotPresent();
                        assertThat(p.format()).isNotNull()
                            .isNotPresent();
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaLevels(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaLevelsRequest> captor = ArgumentCaptor.forClass(MdSchemaLevelsRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_LEVELS</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <DIMENSION_UNIQUE_NAME>dimensionUniqueName</DIMENSION_UNIQUE_NAME>
                            <HIERARCHY_UNIQUE_NAME>hierarchyUniqueName</HIERARCHY_UNIQUE_NAME>
                            <LEVEL_NAME>levelName</LEVEL_NAME>
                            <LEVEL_UNIQUE_NAME>levelUniqueName</LEVEL_UNIQUE_NAME>
                            <CUBE_SOURCE>0x01</CUBE_SOURCE>
                            <LEVEL_VISIBILITY>0x01</LEVEL_VISIBILITY>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaLevels(captor.capture());

        MdSchemaLevelsRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.dimensionUniqueName()).isNotNull()
                            .isPresent().contains(DIMENSION_UNIQUE_NAME);
                        assertThat(r.hierarchyUniqueName()).isNotNull()
                            .isPresent().contains(HIERARCHY_UNIQUE_NAME);
                        assertThat(r.levelName()).isNotNull()
                            .isPresent().contains("levelName");
                        assertThat(r.levelUniqueName()).isNotNull()
                            .isPresent().contains(LEVEL_UNIQUE_NAME);
                        assertThat(r.cubeSource()).isNotNull()
                            .isPresent().contains(CubeSourceEnum.CUBE);
                        assertThat(r.levelVisibility()).isNotNull()
                            .isPresent().contains(VisibilityEnum.VISIBLE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaMeasureGroupDimensions(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaMeasureGroupDimensionsRequest> captor = ArgumentCaptor.forClass(MdSchemaMeasureGroupDimensionsRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_MEASUREGROUP_DIMENSIONS</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <MEASUREGROUP_NAME>measureGroupName</MEASUREGROUP_NAME>
                            <DIMENSION_UNIQUE_NAME>dimensionUniqueName</DIMENSION_UNIQUE_NAME>
                            <DIMENSION_VISIBILITY>0x01</DIMENSION_VISIBILITY>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaMeasureGroupDimensions(captor.capture());

        MdSchemaMeasureGroupDimensionsRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.measureGroupName()).isNotNull()
                            .isPresent().contains(MEASURE_GROUP_NAME);
                        assertThat(r.dimensionUniqueName()).isNotNull()
                            .isPresent().contains(DIMENSION_UNIQUE_NAME);
                        assertThat(r.dimensionVisibility()).isNotNull()
                            .isPresent().contains(VisibilityEnum.VISIBLE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaMeasures(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaMeasuresRequest> captor = ArgumentCaptor.forClass(MdSchemaMeasuresRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_MEASURES</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <MEASURE_NAME>measureName</MEASURE_NAME>
                            <MEASURE_UNIQUE_NAME>measureUniqueName</MEASURE_UNIQUE_NAME>
                            <MEASUREGROUP_NAME>measureGroupName</MEASUREGROUP_NAME>
                            <CUBE_SOURCE>0x01</CUBE_SOURCE>
                            <MEASURE_VISIBILITY>0x01</MEASURE_VISIBILITY>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaMeasures(captor.capture());

        MdSchemaMeasuresRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.measureName()).isNotNull()
                            .isPresent().contains("measureName");
                        assertThat(r.measureUniqueName()).isNotNull()
                            .isPresent().contains("measureUniqueName");
                        assertThat(r.measureGroupName()).isNotNull()
                            .isPresent().contains(MEASURE_GROUP_NAME);
                        assertThat(r.cubeSource()).isNotNull()
                            .isPresent().contains(CubeSourceEnum.CUBE);
                        assertThat(r.measureVisibility()).isNotNull()
                            .isPresent().contains(VisibilityEnum.VISIBLE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaMembers(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaMembersRequest> captor = ArgumentCaptor.forClass(MdSchemaMembersRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_MEMBERS</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <DIMENSION_UNIQUE_NAME>dimensionUniqueName</DIMENSION_UNIQUE_NAME>
                            <HIERARCHY_UNIQUE_NAME>hierarchyUniqueName</HIERARCHY_UNIQUE_NAME>
                            <LEVEL_UNIQUE_NAME>levelUniqueName</LEVEL_UNIQUE_NAME>
                            <LEVEL_NUMBER>1</LEVEL_NUMBER>
                            <MEMBER_NAME>memberName</MEMBER_NAME>
                            <MEMBER_UNIQUE_NAME>memberUniqueName</MEMBER_UNIQUE_NAME>
                            <MEMBER_TYPE>1</MEMBER_TYPE>
                            <MEMBER_CAPTION>memberCaption</MEMBER_CAPTION>
                            <CUBE_SOURCE>0x01</CUBE_SOURCE>
                            <TREE_OP>0x20</TREE_OP>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaMembers(captor.capture());

        MdSchemaMembersRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.dimensionUniqueName()).isNotNull()
                            .isPresent().contains(DIMENSION_UNIQUE_NAME);
                        assertThat(r.hierarchyUniqueName()).isNotNull()
                            .isPresent().contains(HIERARCHY_UNIQUE_NAME);
                        assertThat(r.levelUniqueName()).isNotNull()
                            .isPresent().contains(LEVEL_UNIQUE_NAME);
                        assertThat(r.levelNumber()).isNotNull()
                            .isPresent().contains(1);
                        assertThat(r.memberName()).isNotNull()
                            .isPresent().contains("memberName");
                        assertThat(r.memberUniqueName()).isNotNull()
                            .isPresent().contains("memberUniqueName");
                        assertThat(r.memberType()).isNotNull()
                            .isPresent().contains(MemberTypeEnum.REGULAR_MEMBER);
                        assertThat(r.memberCaption()).isNotNull()
                            .isPresent().contains("memberCaption");
                        assertThat(r.cubeSource()).isNotNull()
                            .isPresent().contains(CubeSourceEnum.CUBE);
                        assertThat(r.treeOp()).isNotNull()
                            .isPresent().contains(TreeOpEnum.ALL);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaProperties(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaPropertiesRequest> captor = ArgumentCaptor.forClass(MdSchemaPropertiesRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_PROPERTIES</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <DIMENSION_UNIQUE_NAME>dimensionUniqueName</DIMENSION_UNIQUE_NAME>
                            <HIERARCHY_UNIQUE_NAME>hierarchyUniqueName</HIERARCHY_UNIQUE_NAME>
                            <LEVEL_UNIQUE_NAME>levelUniqueName</LEVEL_UNIQUE_NAME>
                            <MEMBER_UNIQUE_NAME>memberUniqueName</MEMBER_UNIQUE_NAME>
                            <PROPERTY_TYPE>1</PROPERTY_TYPE>
                            <PROPERTY_NAME>propertyName</PROPERTY_NAME>
                            <PROPERTY_ORIGIN>1</PROPERTY_ORIGIN>
                            <CUBE_SOURCE>0x01</CUBE_SOURCE>
                            <PROPERTY_VISIBILITY>0x01</PROPERTY_VISIBILITY>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaProperties(captor.capture());

        MdSchemaPropertiesRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.dimensionUniqueName()).isNotNull()
                            .isPresent().contains(DIMENSION_UNIQUE_NAME);
                        assertThat(r.hierarchyUniqueName()).isNotNull()
                            .isPresent().contains(HIERARCHY_UNIQUE_NAME);
                        assertThat(r.levelUniqueName()).isNotNull()
                            .isPresent().contains(LEVEL_UNIQUE_NAME);
                        assertThat(r.memberUniqueName()).isNotNull()
                            .isPresent().contains("memberUniqueName");
                        assertThat(r.propertyType()).isNotNull()
                            .isPresent().contains(PropertyTypeEnum.PROPERTY_MEMBER);
                        assertThat(r.propertyName()).isNotNull()
                            .isPresent().contains("propertyName");
                        assertThat(r.propertyOrigin()).isNotNull()
                            .isPresent().contains(PropertyOriginEnum.USER_DEFINED);
                        assertThat(r.cubeSource()).isNotNull()
                            .isPresent().contains(CubeSourceEnum.CUBE);
                        assertThat(r.propertyVisibility()).isNotNull()
                            .isPresent().contains(VisibilityEnum.VISIBLE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaSets(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaSetsRequest> captor = ArgumentCaptor.forClass(MdSchemaSetsRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_SETS</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <SET_NAME>setName</SET_NAME>
                            <SCOPE>1</SCOPE>
                            <CUBE_SOURCE>0x01</CUBE_SOURCE>
                            <HIERARCHY_UNIQUE_NAME>hierarchyUniqueName</HIERARCHY_UNIQUE_NAME>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaSets(captor.capture());

        MdSchemaSetsRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.setName()).isNotNull()
                            .isPresent().contains("setName");
                        assertThat(r.scope()).isNotNull()
                            .isPresent().contains(ScopeEnum.GLOBAL);
                        assertThat(r.cubeSource()).isNotNull()
                            .isPresent().contains(CubeSourceEnum.CUBE);
                        assertThat(r.hierarchyUniqueName()).isNotNull()
                            .isPresent().contains(HIERARCHY_UNIQUE_NAME);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaKpis(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaKpisRequest> captor = ArgumentCaptor.forClass(MdSchemaKpisRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_KPIS</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <KPI_NAME>kpiName</KPI_NAME>
                            <CUBE_SOURCE>0x01</CUBE_SOURCE>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaKpis(captor.capture());

        MdSchemaKpisRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.kpiName()).isNotNull()
                            .isPresent().contains("kpiName");
                        assertThat(r.cubeSource()).isNotNull()
                            .isPresent().contains(CubeSourceEnum.CUBE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testMdSchemaMeasureGroups(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<MdSchemaMeasureGroupsRequest> captor = ArgumentCaptor.forClass(MdSchemaMeasureGroupsRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>MDSCHEMA_MEASUREGROUPS</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <CUBE_NAME>cubeName</CUBE_NAME>
                            <MEASUREGROUP_NAME>measureGroupName</MEASUREGROUP_NAME>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).mdSchemaMeasureGroups(captor.capture());

        MdSchemaMeasureGroupsRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.cubeName()).isNotNull()
                            .isPresent().contains(CUBE_NAME);
                        assertThat(r.measureGroupName()).isNotNull()
                            .isPresent().contains(MEASURE_GROUP_NAME);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testDbSchemaTablesInfo(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<DbSchemaTablesInfoRequest> captor = ArgumentCaptor.forClass(DbSchemaTablesInfoRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>DBSCHEMA_TABLES_INFO</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <TABLE_CATALOG>catalogName</TABLE_CATALOG>
                            <TABLE_SCHEMA>schemaName</TABLE_SCHEMA>
                            <TABLE_NAME>tableName</TABLE_NAME>
                            <TABLE_TYPE>TABLE</TABLE_TYPE>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).dbSchemaTablesInfo(captor.capture());

        DbSchemaTablesInfoRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.tableName()).isNotNull().isEqualTo(TABLE_NAME);
                        assertThat(r.tableType()).isNotNull().isEqualTo(TableTypeEnum.TABLE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testDbschemaSourceTables(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<DbSchemaSourceTablesRequest> captor = ArgumentCaptor.forClass(DbSchemaSourceTablesRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>DBSCHEMA_SOURCE_TABLES</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <TABLE_CATALOG>catalogName</TABLE_CATALOG>
                            <TABLE_SCHEMA>schemaName</TABLE_SCHEMA>
                            <TABLE_NAME>tableName</TABLE_NAME>
                            <TABLE_TYPE>TABLE</TABLE_TYPE>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).dbSchemaSourceTables(captor.capture());

        DbSchemaSourceTablesRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull()
                            .isPresent().contains(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull()
                            .isPresent().contains(SCHEMA_NAME);
                        assertThat(r.tableName()).isNotNull().isEqualTo(TABLE_NAME);
                        assertThat(r.tableType()).isNotNull().isEqualTo(TableTypeEnum.TABLE);
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                        	.isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }

    @Test
    void testDbschemaSchemata(@InjectService XmlaService xmlaService) {

        ArgumentCaptor<DbSchemaSchemataRequest> captor = ArgumentCaptor.forClass(DbSchemaSchemataRequest.class);
        final String sRequest = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                        <RequestType>DBSCHEMA_SCHEMATA</RequestType>
                        <Restrictions>
                          <RestrictionList>
                            <CATALOG_NAME>catalogName</CATALOG_NAME>
                            <SCHEMA_NAME>schemaName</SCHEMA_NAME>
                            <SCHEMA_OWNER>schemaOwner</SCHEMA_OWNER>
                          </RestrictionList>
                        </Restrictions>
                        <Properties>
                          <PropertyList>
                            <DataSourceInfo>FoodMart</DataSourceInfo>
                            <Catalog>FoodMart</Catalog>
                            <Format>Tabular</Format>
                            <Content>SchemaData</Content>
                          </PropertyList>
                        </Properties>
            </Discover>
            """;

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
            SOAPUtil.envelop(sRequest));

        DiscoverService discoverService = xmlaService.discover();
        verify(discoverService, (times(1))).dbSchemaSchemata(captor.capture());

        DbSchemaSchemataRequest request = captor.getValue();
        assertThat(request).isNotNull()

            .satisfies(d -> {
                // getRestrictions
                assertThat(d.restrictions()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.catalogName()).isNotNull().isEqualTo(CATALOG_NAME);
                        assertThat(r.schemaName()).isNotNull().isEqualTo(SCHEMA_NAME);
                        assertThat(r.schemaOwner()).isNotNull().isEqualTo("schemaOwner");
                    });
                // getProperties
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.localeIdentifier()).isNotNull()
                            .isNotPresent();
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                        	.contains(TABULAR);
                        assertThat(p.content()).isNotNull()
                            .isPresent()
                            .contains(SCHEMA_DATA);
                    });
            });
    }
}

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
import org.eclipse.daanse.xmla.api.execute.ExecuteService;
import org.eclipse.daanse.xmla.api.execute.alter.AlterRequest;
import org.eclipse.daanse.xmla.api.execute.cancel.CancelRequest;
import org.eclipse.daanse.xmla.api.execute.clearcache.ClearCacheRequest;
import org.eclipse.daanse.xmla.api.execute.statement.StatementRequest;
import org.eclipse.daanse.xmla.api.xmla.ColumnBinding;
import org.eclipse.daanse.xmla.api.xmla.DataSourceViewBinding;
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

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.xmla.api.common.properties.AxisFormat.TUPLE_FORMAT;
import static org.eclipse.daanse.xmla.api.common.properties.Format.TABULAR;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.ALTER_REQUEST;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.CANCEL_REQUEST;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.CLEAR_CACHE_REQUEST;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.STATEMENT_REQUEST;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ConfigurationExtension.class)
@WithFactoryConfiguration(factoryPid = Constants.PID_MS_SOAP, name = "test-ms-config", location = "?", properties = {
    @Property(key = "xmlaService.target", value = "(" + Constants.XMLASERVICE_FILTER_KEY + "="
        + Constants.XMLASERVICE_FILTER_VALUE + ")"),
    @Property(key = "osgi.soap.endpoint.contextpath", value = Constants.WS_PATH)})
@WithFactoryConfiguration(factoryPid = "org.eclipse.daanse.ws.handler.SOAPLoggingHandler", name = "test-ms-config",
    location = "?", properties = {
    @Property(key = "osgi.soap.endpoint.selector", value = "(service.pid=*)")})
@RequireServiceComponentRuntime
class ExecuteRequestTest {

    private static final String REPORT_AND_STOP = "ReportAndStop";

	private static final String FOOD_MART = "FoodMart";


    @InjectBundleContext
    BundleContext bc;

    @BeforeEach
    void beforaEach() {
        XmlaService xmlaService = mock(XmlaService.class);
        ExecuteService executeService = mock(ExecuteService.class);

        when(xmlaService.execute()).thenReturn(executeService);

        bc.registerService(XmlaService.class, xmlaService, FrameworkUtil
            .asDictionary(Map.of(Constants.XMLASERVICE_FILTER_KEY, Constants.XMLASERVICE_FILTER_VALUE)));
    }

    @Test
    void testStatement(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<StatementRequest> captor = ArgumentCaptor.forClass(StatementRequest.class);

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_EXECUTE),
            SOAPUtil.envelop(STATEMENT_REQUEST));

        ExecuteService executeService = xmlaService.execute();
        verify(executeService, (times(1))).statement(captor.capture());

        StatementRequest request = captor.getValue();
        assertThat(request).isNotNull()
            .satisfies(d -> {
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                            .isPresent().contains(TABULAR);
                        assertThat(p.axisFormat()).isNotNull()
                            .isPresent().contains(TUPLE_FORMAT);
                    });
                assertThat(d.command()).isNotNull()
                    .satisfies(r ->
                        assertThat(r.statement().trim()).isNotNull()
                            .isEqualTo("select [Measures].[Sales Count] on 0, non empty [Store].[Store State].members" +
                                " on 1 from [Sales]")
                    );
            });
    }

    @Test
    void testClearcache(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<ClearCacheRequest> captor = ArgumentCaptor.forClass(ClearCacheRequest.class);

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_EXECUTE),
            SOAPUtil.envelop(CLEAR_CACHE_REQUEST));

        ExecuteService executeService = xmlaService.execute();
        verify(executeService, (times(1))).clearCache(captor.capture());

        ClearCacheRequest request = captor.getValue();
        assertThat(request).isNotNull()
            .satisfies(d -> {
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                            .isPresent().contains(TABULAR);
                        assertThat(p.axisFormat()).isNotNull()
                            .isPresent().contains(TUPLE_FORMAT);
                    });
                assertThat(d.command()).isNotNull()
                    .satisfies(r ->
                        assertThat(r.object()).isNotNull().satisfies(o -> {
                            assertThat(o.serverID()).isNotNull().isEqualTo("serverID");
                            assertThat(o.databaseID()).isNotNull().isEqualTo("databaseID");
                            assertThat(o.roleID()).isNotNull().isEqualTo("roleID");
                            assertThat(o.traceID()).isNotNull().isEqualTo("traceID");
                            assertThat(o.assemblyID()).isNotNull().isEqualTo("assemblyID");
                            assertThat(o.dimensionID()).isNotNull().isEqualTo("dimensionID");
                            assertThat(o.dimensionPermissionID()).isNotNull().isEqualTo("dimensionPermissionID");
                            assertThat(o.dataSourceID()).isNotNull().isEqualTo("dataSourceID");
                            assertThat(o.dataSourcePermissionID()).isNotNull().isEqualTo("dataSourcePermissionID");
                            assertThat(o.databasePermissionID()).isNotNull().isEqualTo("databasePermissionID");
                            assertThat(o.dataSourceViewID()).isNotNull().isEqualTo("dataSourceViewID");
                            assertThat(o.cubeID()).isNotNull().isEqualTo("cubeID");
                            assertThat(o.miningStructureID()).isNotNull().isEqualTo("miningStructureID");
                            assertThat(o.measureGroupID()).isNotNull().isEqualTo("measureGroupID");
                            assertThat(o.perspectiveID()).isNotNull().isEqualTo("perspectiveID");
                            assertThat(o.cubePermissionID()).isNotNull().isEqualTo("cubePermissionID");
                            assertThat(o.mdxScriptID()).isNotNull().isEqualTo("mdxScriptID");
                            assertThat(o.partitionID()).isNotNull().isEqualTo("partitionID");
                            assertThat(o.aggregationDesignID()).isNotNull().isEqualTo("aggregationDesignID");
                            assertThat(o.miningModelID()).isNotNull().isEqualTo("miningModelID");
                            assertThat(o.miningModelPermissionID()).isNotNull().isEqualTo("miningModelPermissionID");
                            assertThat(o.miningStructurePermissionID()).isNotNull().isEqualTo("miningStructurePermissionID");
                        })
                    );
            });
    }

    @Test
    void testCancel(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<CancelRequest> captor = ArgumentCaptor.forClass(CancelRequest.class);

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_EXECUTE),
            SOAPUtil.envelop(CANCEL_REQUEST));

        ExecuteService executeService = xmlaService.execute();
        verify(executeService, (times(1))).cancel(captor.capture());

        CancelRequest request = captor.getValue();
        assertThat(request).isNotNull()
            .satisfies(d -> {
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                            .isPresent().contains(TABULAR);
                        assertThat(p.axisFormat()).isNotNull()
                            .isPresent().contains(TUPLE_FORMAT);
                    });
                assertThat(d.command()).isNotNull()
                    .satisfies(c -> {
                            assertThat(c.connectionID()).isNotNull().isEqualTo(1);
                            assertThat(c.sessionID()).isNotNull().isEqualTo("sessionID");
                            assertThat(c.spid()).isNotNull().isEqualTo(2);
                            assertThat(c.cancelAssociated()).isNotNull().isEqualTo(true);
                    });
            });
    }

    @Test
    void testAlter(@InjectService XmlaService xmlaService) {
        ArgumentCaptor<AlterRequest> captor = ArgumentCaptor.forClass(AlterRequest.class);
        Duration duration = Duration.parse("-PT1S");

        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_EXECUTE),
            SOAPUtil.envelop(ALTER_REQUEST));

        ExecuteService executeService = xmlaService.execute();
        verify(executeService, (times(1))).alter(captor.capture());

        AlterRequest request = captor.getValue();
        assertThat(request).isNotNull()
            .satisfies(d -> {
                assertThat(d.properties()).isNotNull()
                    .satisfies(p -> {
                        assertThat(p.dataSourceInfo()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.catalog()).isNotNull()
                            .isPresent().contains(FOOD_MART);
                        assertThat(p.format()).isNotNull()
                            .isPresent().contains(TABULAR);
                        assertThat(p.axisFormat()).isNotNull()
                            .isPresent().contains(TUPLE_FORMAT);
                    });
                assertThat(d.command()).isNotNull()
                    .satisfies(r -> {
                        assertThat(r.object()).isNotNull();
                        assertThat(r.object().databaseID())
                        .isNotNull().isEqualTo("AdventureWorks_SSAS_Alter");
                        assertThat(r.object().dimensionID())
                        .isNotNull().isEqualTo("Dim Customer");
                        assertThat(r.objectDefinition()).isNotNull().satisfies(od ->
                        	assertThat(od.dimension()).isNotNull().satisfies(dimension -> {
                        		assertThat(dimension.id()).isNotNull().isEqualTo("Dim Customer");
                        		assertThat(dimension.name()).isNotNull().isEqualTo("Customer");
                                assertThat(dimension.language()).isNotNull().isEqualTo(1033);
                                assertThat(dimension.collation()).isNotNull().isEqualTo("Latin1_General_CI_AS");
                                assertThat(dimension.unknownMemberName()).isNotNull().isEqualTo("Unknown");
                                assertThat(dimension.source()).isNotNull().satisfies(s ->
                                    assertThat(((DataSourceViewBinding)s).dataSourceViewID()).isNotNull().isEqualTo("dsvAdventureWorksDW2008")
                                );
                                assertThat(dimension.errorConfiguration()).isNotNull().satisfies(ec -> {
                                    assertThat(ec.keyNotFound()).isNotNull().isPresent().contains(REPORT_AND_STOP);
                                    assertThat(ec.keyDuplicate()).isNotNull().isPresent().contains(REPORT_AND_STOP);
                                    assertThat(ec.nullKeyNotAllowed()).isNotNull().isPresent().contains(REPORT_AND_STOP);
                                });
                                assertThat(dimension.attributes()).isNotNull().satisfies(ats ->
                                    assertThat(ats.get(0)).isNotNull().satisfies(at -> {
                                        assertThat(at.id()).isNotNull().isEqualTo("Customer Key");
                                        assertThat(at.name()).isNotNull().isEqualTo("Customer Key");
                                        assertThat(at.usage()).isNotNull().isEqualTo("Key");
                                        assertThat(at.estimatedCount()).isNotNull().isEqualTo(18484);
                                        assertThat(at.orderBy()).isNotNull().isEqualTo("Key");
                                        assertThat(at.keyColumns()).isNotNull().satisfies(kcs ->
                                            assertThat(kcs.get(0)).isNotNull().satisfies(kc -> {
                                                assertThat(kc.dataType()).isNotNull().isEqualTo("Integer");
                                                assertThat(kc.source()).isNotNull().isPresent().satisfies(o -> {
                                                    assertThat(((ColumnBinding)o.get()).tableID()).isNotNull().isEqualTo("dbo_DimCustomer");
                                                    assertThat(((ColumnBinding)o.get()).columnID()).isNotNull().isEqualTo("CustomerKey");
                                                });
                                            })
                                        );
                                    })
                                );
                                assertThat(dimension.proactiveCaching()).isNotNull().satisfies(pc -> {
                                    assertThat(pc.silenceInterval()).isNotNull().isPresent().contains(duration);
                                    assertThat(pc.latency()).isNotNull().isPresent().contains(duration);
                                    assertThat(pc.silenceOverrideInterval()).isNotNull().isPresent().contains(duration);
                                    assertThat(pc.forceRebuildInterval()).isNotNull().isPresent().contains(duration);
                                });
                        	})
                        );
                    });
            });
    }

}

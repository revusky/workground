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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Discover;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DiscoverResponse;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Return;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.Row;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.Rowset;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverPropertiesResponseRowXml;
import org.eclipse.daanse.xmla.ws.tck.Constants;
import org.eclipse.daanse.xmla.ws.tck.SOAPUtil;
import org.eclipse.daanse.xmla.ws.tck.XMLUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.dictionary.Dictionaries;
import org.xmlunit.assertj3.XmlAssert;

import jakarta.xml.soap.SOAPMessage;

class MsXmlAnalysisSoapTest {

    @InjectBundleContext
    BundleContext bc;
    private MsXmlAnalysisSoap xmlaWs;
    private ArgumentCaptor<Discover> dicoverCaptor;

    @BeforeEach
    void beforeEach() {
        xmlaWs =spy( new MsXmlAnalysisSoap());


        bc.registerService(MsXmlAnalysisSoap.class, xmlaWs, Dictionaries.dictionaryOf("osgi.soap.endpoint.contextpath",
                Constants.WS_PATH, "osgi.soap.endpoint.implementor", true));
        dicoverCaptor = ArgumentCaptor.forClass(Discover.class);

    }

    @Test
    void testDiscoverRequest() throws Exception {

        final String request = """
                <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
                  <RequestType>TEST_VALUE</RequestType>
                  <Restrictions>
                    <RestrictionList>
                      <CATALOG_NAME>catalogName</CATALOG_NAME>
                      <CUBE_NAME>cubeName</CUBE_NAME>
                      <DIMENSION_UNIQUE_NAME>dimensionUniqueName</DIMENSION_UNIQUE_NAME>
                    </RestrictionList>
                  </Restrictions>
                  <Properties>
                    <PropertyList>
                      <DataSourceInfo>dataSourceInfo</DataSourceInfo>
                      <Catalog>catalog</Catalog>
                      <Format>Tabular</Format>
                      <LocaleIdentifier>1033</LocaleIdentifier>
                    </PropertyList>
                  </Properties>
                </Discover>
                """;
        SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL, Optional.of(Constants.SOAP_ACTION_DISCOVER),
                SOAPUtil.envelop(request));

        verify(xmlaWs, (times(1))).discover(dicoverCaptor.capture(), Mockito.any(), Mockito.any(), Mockito.any());

        Discover discover = dicoverCaptor.getValue();
        assertThat(discover).isNotNull()
                .satisfies(d -> {
                    // getRequestType
                    assertThat(d.getRequestType()).isNotNull()
                            .isEqualTo("TEST_VALUE");
                    // getRestrictions
                    assertThat(d.getRestrictions()).isNotNull()
                            .satisfies(r -> {
                                assertThat(r.getRestrictionMap()).isNotNull()
                                        .hasSize(3)
                                        .containsEntry("CATALOG_NAME", "catalogName")
                                        .containsEntry("CUBE_NAME", "cubeName")
                                        .containsEntry("DIMENSION_UNIQUE_NAME", "dimensionUniqueName");
                            });
                    // getProperties
                    assertThat(d.getProperties()).isNotNull()
                            .satisfies(r -> {
                                assertThat(r.getPropertyList()).isNotNull()
                                        .satisfies(pl -> {
                                            assertThat(pl.getDataSourceInfo()).isEqualTo("dataSourceInfo");
                                            assertThat(pl.getCatalog()).isEqualTo("catalog");
                                            assertThat(pl.getFormat()).isEqualTo("Tabular");
                                            assertThat(pl.getLocaleIdentifier()).isEqualTo(1033);
                                        });

                            });

                });
    }

    public static final String REQUEST_DISCOVER_DUMMY = """
            <Discover xmlns="urn:schemas-microsoft-com:xml-analysis">
              <RequestType>DUMMY</RequestType>
              <Restrictions>
                <RestrictionList>
                </RestrictionList>
              </Restrictions>
              <Properties>
                <PropertyList>
                </PropertyList>
              </Properties>
            </Discover>
            """;

    @Test
    void testResponse() throws Exception {

        DiscoverResponse discoverResponse = new DiscoverResponse();

        Return r = new Return();
        Rowset rs = new Rowset();
        Row row = new DiscoverPropertiesResponseRowXml();
        rs.setRow(List.of(row));

        r.setValue(rs);

        discoverResponse.setReturn(r);

        doReturn(discoverResponse).when(xmlaWs)
                .discover(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
                Optional.of(Constants.SOAP_ACTION_DISCOVER), SOAPUtil.envelop(REQUEST_DISCOVER_DUMMY));

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        xmlAssert.hasXPath("/SOAP:Envelope");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:DiscoverResponse/return/rowset:root")
                .exist();

    }
}

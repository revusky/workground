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

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.eclipse.daanse.xmla.api.XmlaService;
import org.eclipse.daanse.xmla.api.exception.Messages;
import org.eclipse.daanse.xmla.api.exception.Type;
import org.eclipse.daanse.xmla.api.execute.ExecuteService;
import org.eclipse.daanse.xmla.api.execute.cancel.CancelResponse;
import org.eclipse.daanse.xmla.api.execute.clearcache.ClearCacheResponse;
import org.eclipse.daanse.xmla.api.mddataset.Axis;
import org.eclipse.daanse.xmla.api.mddataset.AxisInfo;
import org.eclipse.daanse.xmla.api.mddataset.CellInfoItem;
import org.eclipse.daanse.xmla.api.mddataset.CellType;
import org.eclipse.daanse.xmla.api.mddataset.CellTypeError;
import org.eclipse.daanse.xmla.api.mddataset.HierarchyInfo;
import org.eclipse.daanse.xmla.api.mddataset.MemberType;
import org.eclipse.daanse.xmla.api.mddataset.OlapInfoCube;
import org.eclipse.daanse.xmla.model.record.engine200.WarningColumnR;
import org.eclipse.daanse.xmla.model.record.engine200.WarningLocationObjectR;
import org.eclipse.daanse.xmla.model.record.engine200.WarningMeasureR;
import org.eclipse.daanse.xmla.model.record.exception.ErrorTypeR;
import org.eclipse.daanse.xmla.model.record.exception.ExceptionR;
import org.eclipse.daanse.xmla.model.record.exception.MessageLocationR;
import org.eclipse.daanse.xmla.model.record.exception.MessagesR;
import org.eclipse.daanse.xmla.model.record.exception.StartEndR;
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

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.daanse.xmla.ws.tck.SOAPUtil.string;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.ALTER_REQUEST;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.CANCEL_REQUEST;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.CLEAR_CACHE_REQUEST;
import static org.eclipse.daanse.xmla.ws.tck.TestRequests.STATEMENT_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
class ExecuteResponseTest {

    private static final String VALUE2 = "value";

	private static final String MEASURE_NAME = "measureName";

	private static final String MEASURE_GROUP = "measureGroup";

	private static final String HIERARCHY = "hierarchy";

	private static final String DIMENSION = "dimension";

	private static final String DESCRIPTION = "description";

	private static final String ATTRIBUTE = "attribute";

	private static final String SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN = "/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/";

	private Logger logger = LoggerFactory.getLogger(ExecuteResponseTest.class);

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
    void testStatement(@InjectService XmlaService xmlaService) throws SOAPException, IOException, TransformerException {
        CellInfoItem any = new CellInfoItemR("tagName",
            "name", Optional.of(VALUE2));

        OlapInfoCube cube = new OlapInfoCubeR("cubeName",
            Instant.ofEpochMilli(2000l),
            Instant.ofEpochMilli(2000l));

        CubeInfoR cubeInfo = new CubeInfoR(List.of(cube));

        HierarchyInfo hierarchyInfo = new HierarchyInfoR(List.of(any), "name");

        AxisInfo axisInfo = new AxisInfoR(List.of(hierarchyInfo), "name");

        AxesInfoR axesInfo = new AxesInfoR(List.of(axisInfo));

        CellInfoR cellInfo = new CellInfoR(List.of(any));

        OlapInfoR olapInfo = new OlapInfoR(cubeInfo,
            axesInfo,
            cellInfo);

        MemberType member = new MemberTypeR(List.of(any), HIERARCHY);

        org.eclipse.daanse.xmla.api.mddataset.Type setType = new MembersTypeR(List.of(member), HIERARCHY);

        Axis axis = new AxisR(List.of(setType), "name");

        AxesR axes = new AxesR(List.of(axis));

        CellTypeError error = new CellTypeErrorR(1l,
            DESCRIPTION);

        ValueR value = new ValueR(List.of(error));

        CellType cell = new CellTypeR(value,
            List.of(any),
        1);

        CellSetTypeR cellSet = new CellSetTypeR(List.of(new byte[]{1, 2}));

        CellDataR cellData = new CellDataR(List.of(cell), cellSet);

        ExceptionR exception = new ExceptionR();

        MessagesR messages = new MessagesR(List.of(getErrorType()));

        MddatasetR mdDataSet = new MddatasetR(olapInfo,
            axes,
            cellData,
            exception,
            messages);
        StatementResponseR row = new StatementResponseR(mdDataSet);

        ExecuteService executeService = xmlaService.execute();
        when(executeService.statement(any())).thenReturn(row);

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_EXECUTE), SOAPUtil.envelop(STATEMENT_REQUEST));

        logger.debug("Statement response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkRow(xmlAssert);
    }

    @Test
    void testAlter(@InjectService XmlaService xmlaService) throws SOAPException, IOException, TransformerException {

        ExceptionR exception = new ExceptionR();
        Messages messages = new MessagesR(List.of(getErrorType()));
        EmptyresultR emptyresult = new EmptyresultR(exception, messages);
        AlterResponseR alterResponse = new AlterResponseR(emptyresult);
        ExecuteService executeService = xmlaService.execute();
        when(executeService.alter(any())).thenReturn(alterResponse);

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_EXECUTE), SOAPUtil.envelop(ALTER_REQUEST));

        logger.debug("Alter response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkAlert(xmlAssert);
    }

    @Test
    void testClearCache(@InjectService XmlaService xmlaService) throws SOAPException, IOException, TransformerException {

        ExceptionR exception = new ExceptionR();
        Messages messages = new MessagesR(List.of(getErrorType()));
        EmptyresultR emptyresult = new EmptyresultR(exception, messages);
        ClearCacheResponse alterResponse = new ClearCacheResponseR(emptyresult);
        ExecuteService executeService = xmlaService.execute();
        when(executeService.clearCache(any())).thenReturn(alterResponse);

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_EXECUTE), SOAPUtil.envelop(CLEAR_CACHE_REQUEST));

        logger.debug("response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkAlert(xmlAssert);
    }

    @Test
    void testCancel(@InjectService XmlaService xmlaService) throws SOAPException, IOException, TransformerException {

        ExceptionR exception = new ExceptionR();
        Messages messages = new MessagesR(List.of(getErrorType()));
        EmptyresultR emptyresult = new EmptyresultR(exception, messages);
        CancelResponse cancelResponse = new CancelResponseR(emptyresult);
        ExecuteService executeService = xmlaService.execute();
        when(executeService.cancel(any())).thenReturn(cancelResponse);

        SOAPMessage response = SOAPUtil.callSoapWebService(Constants.SOAP_ENDPOINT_URL,
            Optional.of(Constants.SOAP_ACTION_EXECUTE), SOAPUtil.envelop(CANCEL_REQUEST));

        logger.debug("response :");
        String responseStr =  string(response);
        logger.debug(responseStr);

        XmlAssert xmlAssert = XMLUtil.createAssert(response);
        checkAlert(xmlAssert);
    }

    private void checkAlert(XmlAssert xmlAssert) {
        xmlAssert.hasXPath("/SOAP:Envelope");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse").exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/empty:root")
            .exist();
        checkException(xmlAssert, "empty");

        checkMessages(xmlAssert,"empty");
    }

    private void checkMessages(XmlAssert xmlAssert, String ns) {
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error")
            .exist().haveAttribute("Description", DESCRIPTION)
            .haveAttribute("HelpFile", "helpFile")
            .haveAttribute("Source", "source");
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location")
            .exist();
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/Start")
            .exist();
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/Start")
            .exist();
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/Start/Line")
            .exist();
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/Start/Line")
            .isEqualTo("1");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/Start/Column")
            .isEqualTo("2");

        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/End")
            .exist();
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/End/Line")
            .exist();
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/End/Line")
            .isEqualTo("3");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/End/Column")
            .isEqualTo("4");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/LineOffset")
            .isEqualTo("1");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/TextLength")
            .isEqualTo("2");
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject")
            .exist();
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject/engine200:WarningColumn")
            .exist();
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject/engine200:WarningColumn/Dimension")
            .isEqualTo(DIMENSION);
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject/engine200:WarningColumn/Attribute")
            .isEqualTo(ATTRIBUTE);

        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject/engine200:WarningMeasure")
            .exist();
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject/engine200:WarningMeasure/Cube")
            .isEqualTo("cube");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject/engine200:WarningMeasure/MeasureGroup")
            .isEqualTo(MEASURE_GROUP);
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/SourceObject/engine200:WarningMeasure/MeasureName")
            .isEqualTo(MEASURE_NAME);


        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject")
            .exist();
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject/engine200:WarningColumn")
            .exist();
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject/engine200:WarningColumn/Dimension")
            .isEqualTo(DIMENSION);
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject/engine200:WarningColumn/Attribute")
            .isEqualTo(ATTRIBUTE);

        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject/engine200:WarningMeasure")
            .exist();
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject/engine200:WarningMeasure/Cube")
            .isEqualTo("cube");
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject/engine200:WarningMeasure/MeasureGroup")
            .isEqualTo(MEASURE_GROUP);
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/DependsOnObject/engine200:WarningMeasure/MeasureName")
            .isEqualTo(MEASURE_NAME);

        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/RowNumber")
            .exist();
        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Location/RowNumber")
            .isEqualTo("3");

        xmlAssert.valueByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Messages/Error/Callstack")
            .isEqualTo("callstack");

    }

    private void checkException(XmlAssert xmlAssert, String ns) {
        xmlAssert.nodesByXPath(SOAP_ENVELOPE_SOAP_BODY_MSXMLA_EXECUTE_RESPONSE_MSXMLA_RETURN + ns + ":root/Exception")
            .exist();
    }

    private Type getErrorType() {
        StartEndR start = new StartEndR(1, 2);

        StartEndR end = new StartEndR(3, 4);

        WarningColumnR warningColumn = new WarningColumnR(DIMENSION, ATTRIBUTE);

        WarningMeasureR warningMeasure = new WarningMeasureR("cube",
            MEASURE_GROUP,
            MEASURE_NAME);

        WarningLocationObjectR sourceObject = new WarningLocationObjectR(warningColumn,
            warningMeasure);

        WarningLocationObjectR dependsOnObject = new WarningLocationObjectR(warningColumn,
            warningMeasure);

        MessageLocationR location = new MessageLocationR(start,
            end,
            1,
            2,
            sourceObject,
            dependsOnObject,
            3);

        return new ErrorTypeR(location,
            "callstack",
            1l,
            DESCRIPTION,
            "source",
            "helpFile");
    }

    private void checkRow(XmlAssert xmlAssert) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .withZone(ZoneId.systemDefault());
        xmlAssert.hasXPath("/SOAP:Envelope");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse").exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root")
            .exist();

        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CubeInfo")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CubeInfo/Cube")
            .exist();
        xmlAssert.valueByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CubeInfo/Cube/CubeName")
            .isEqualTo("cubeName");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CubeInfo/Cube/engine:LastDataUpdate")
            .exist();
        xmlAssert.valueByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CubeInfo/Cube/engine:LastDataUpdate")
            .isEqualTo(formatter.format(Instant.ofEpochMilli(2000l)));
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CubeInfo/Cube/engine:LastSchemaUpdate")
            .exist();
        xmlAssert.valueByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CubeInfo/Cube/engine:LastSchemaUpdate")
            .isEqualTo(formatter.format(Instant.ofEpochMilli(2000l)));


        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/AxesInfo")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/AxesInfo/AxisInfo")
            .exist().haveAttribute("name", "name");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/AxesInfo/AxisInfo/HierarchyInfo")
            .exist().haveAttribute("name", "name");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/AxesInfo/AxisInfo/HierarchyInfo/tagName")
            .exist().haveAttribute("name", "name").haveAttribute("type", VALUE2);


        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CellInfo")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/OlapInfo/CellInfo/tagName")
            .exist().haveAttribute("name", "name").haveAttribute("type", VALUE2);


        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/Axes")
        .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/Axes/Axis")
            .exist().haveAttribute("name", "name");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/Axes/Axis/Members")
            .exist().haveAttribute("Hierarchy", HIERARCHY);
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/Axes/Axis/Members/Member")
            .exist().haveAttribute("Hierarchy", HIERARCHY);
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/Axes/Axis/Members/Member/tagName")
            .exist().haveAttribute("name", "name").haveAttribute("type", VALUE2);


        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/CellData")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/CellData/Cell")
            .exist().haveAttribute("CellOrdinal", "1");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/CellData/Cell/Value")
            .exist();
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/CellData/Cell/Value/Error")
            .exist().haveAttribute("Description", DESCRIPTION).haveAttribute("ErrorCode", "1");
        xmlAssert.nodesByXPath("/SOAP:Envelope/SOAP:Body/msxmla:ExecuteResponse/msxmla:return/mddataset:root/CellData/Cell/tagName")
            .exist().haveAttribute("name", "name").haveAttribute("type", VALUE2);

        checkException(xmlAssert, "mddataset");

        checkMessages(xmlAssert, "mddataset");
    }

}

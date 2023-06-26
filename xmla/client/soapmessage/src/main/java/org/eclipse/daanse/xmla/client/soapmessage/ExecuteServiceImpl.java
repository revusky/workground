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
import org.eclipse.daanse.xmla.api.discover.Properties;
import org.eclipse.daanse.xmla.api.engine.ImpersonationInfo;
import org.eclipse.daanse.xmla.api.engine200_200.ExpressionBinding;
import org.eclipse.daanse.xmla.api.engine200_200.RowNumberBinding;
import org.eclipse.daanse.xmla.api.engine300.CalculationPropertiesVisualizationProperties;
import org.eclipse.daanse.xmla.api.engine300.HierarchyVisualizationProperties;
import org.eclipse.daanse.xmla.api.engine300.RelationshipEndVisualizationProperties;
import org.eclipse.daanse.xmla.api.engine300_300.Relationship;
import org.eclipse.daanse.xmla.api.engine300_300.RelationshipEnd;
import org.eclipse.daanse.xmla.api.engine300_300.RelationshipEndTranslation;
import org.eclipse.daanse.xmla.api.engine300_300.Relationships;
import org.eclipse.daanse.xmla.api.engine300_300.XEvent;
import org.eclipse.daanse.xmla.api.execute.ExecuteParameter;
import org.eclipse.daanse.xmla.api.execute.ExecuteService;
import org.eclipse.daanse.xmla.api.execute.alter.AlterRequest;
import org.eclipse.daanse.xmla.api.execute.alter.AlterResponse;
import org.eclipse.daanse.xmla.api.execute.cancel.CancelRequest;
import org.eclipse.daanse.xmla.api.execute.cancel.CancelResponse;
import org.eclipse.daanse.xmla.api.execute.clearcache.ClearCacheRequest;
import org.eclipse.daanse.xmla.api.execute.clearcache.ClearCacheResponse;
import org.eclipse.daanse.xmla.api.execute.statement.StatementRequest;
import org.eclipse.daanse.xmla.api.execute.statement.StatementResponse;
import org.eclipse.daanse.xmla.api.xmla.Account;
import org.eclipse.daanse.xmla.api.xmla.Action;
import org.eclipse.daanse.xmla.api.xmla.Aggregation;
import org.eclipse.daanse.xmla.api.xmla.AggregationAttribute;
import org.eclipse.daanse.xmla.api.xmla.AggregationDesign;
import org.eclipse.daanse.xmla.api.xmla.AggregationDesignAttribute;
import org.eclipse.daanse.xmla.api.xmla.AggregationDesignDimension;
import org.eclipse.daanse.xmla.api.xmla.AggregationDimension;
import org.eclipse.daanse.xmla.api.xmla.AggregationInstance;
import org.eclipse.daanse.xmla.api.xmla.AggregationInstanceAttribute;
import org.eclipse.daanse.xmla.api.xmla.AggregationInstanceDimension;
import org.eclipse.daanse.xmla.api.xmla.AggregationInstanceMeasure;
import org.eclipse.daanse.xmla.api.xmla.AlgorithmParameter;
import org.eclipse.daanse.xmla.api.xmla.Alter;
import org.eclipse.daanse.xmla.api.xmla.AndOrType;
import org.eclipse.daanse.xmla.api.xmla.AndOrTypeEnum;
import org.eclipse.daanse.xmla.api.xmla.Annotation;
import org.eclipse.daanse.xmla.api.xmla.Assembly;
import org.eclipse.daanse.xmla.api.xmla.Attach;
import org.eclipse.daanse.xmla.api.xmla.AttributeBinding;
import org.eclipse.daanse.xmla.api.xmla.AttributePermission;
import org.eclipse.daanse.xmla.api.xmla.AttributeRelationship;
import org.eclipse.daanse.xmla.api.xmla.AttributeTranslation;
import org.eclipse.daanse.xmla.api.xmla.Backup;
import org.eclipse.daanse.xmla.api.xmla.Batch;
import org.eclipse.daanse.xmla.api.xmla.BeginTransaction;
import org.eclipse.daanse.xmla.api.xmla.Binding;
import org.eclipse.daanse.xmla.api.xmla.BoolBinop;
import org.eclipse.daanse.xmla.api.xmla.CalculatedMeasureBinding;
import org.eclipse.daanse.xmla.api.xmla.CalculationProperty;
import org.eclipse.daanse.xmla.api.xmla.Cancel;
import org.eclipse.daanse.xmla.api.xmla.CellPermission;
import org.eclipse.daanse.xmla.api.xmla.ClearCache;
import org.eclipse.daanse.xmla.api.xmla.CloneDatabase;
import org.eclipse.daanse.xmla.api.xmla.ColumnBinding;
import org.eclipse.daanse.xmla.api.xmla.Command;
import org.eclipse.daanse.xmla.api.xmla.CommitTransaction;
import org.eclipse.daanse.xmla.api.xmla.Create;
import org.eclipse.daanse.xmla.api.xmla.Cube;
import org.eclipse.daanse.xmla.api.xmla.CubeAttribute;
import org.eclipse.daanse.xmla.api.xmla.CubeAttributeBinding;
import org.eclipse.daanse.xmla.api.xmla.CubeDimension;
import org.eclipse.daanse.xmla.api.xmla.CubeDimensionBinding;
import org.eclipse.daanse.xmla.api.xmla.CubeDimensionPermission;
import org.eclipse.daanse.xmla.api.xmla.CubeHierarchy;
import org.eclipse.daanse.xmla.api.xmla.CubePermission;
import org.eclipse.daanse.xmla.api.xmla.DBCC;
import org.eclipse.daanse.xmla.api.xmla.DSVTableBinding;
import org.eclipse.daanse.xmla.api.xmla.DataItem;
import org.eclipse.daanse.xmla.api.xmla.DataMiningMeasureGroupDimension;
import org.eclipse.daanse.xmla.api.xmla.DataSource;
import org.eclipse.daanse.xmla.api.xmla.DataSourcePermission;
import org.eclipse.daanse.xmla.api.xmla.DataSourceView;
import org.eclipse.daanse.xmla.api.xmla.DataSourceViewBinding;
import org.eclipse.daanse.xmla.api.xmla.Database;
import org.eclipse.daanse.xmla.api.xmla.DatabasePermission;
import org.eclipse.daanse.xmla.api.xmla.DegenerateMeasureGroupDimension;
import org.eclipse.daanse.xmla.api.xmla.Delete;
import org.eclipse.daanse.xmla.api.xmla.DesignAggregations;
import org.eclipse.daanse.xmla.api.xmla.Detach;
import org.eclipse.daanse.xmla.api.xmla.Dimension;
import org.eclipse.daanse.xmla.api.xmla.DimensionAttribute;
import org.eclipse.daanse.xmla.api.xmla.DimensionBinding;
import org.eclipse.daanse.xmla.api.xmla.DimensionPermission;
import org.eclipse.daanse.xmla.api.xmla.DrillThroughAction;
import org.eclipse.daanse.xmla.api.xmla.Drop;
import org.eclipse.daanse.xmla.api.xmla.ErrorConfiguration;
import org.eclipse.daanse.xmla.api.xmla.Event;
import org.eclipse.daanse.xmla.api.xmla.EventColumnID;
import org.eclipse.daanse.xmla.api.xmla.EventSession;
import org.eclipse.daanse.xmla.api.xmla.EventType;
import org.eclipse.daanse.xmla.api.xmla.FoldingParameters;
import org.eclipse.daanse.xmla.api.xmla.Group;
import org.eclipse.daanse.xmla.api.xmla.Hierarchy;
import org.eclipse.daanse.xmla.api.xmla.ImageLoad;
import org.eclipse.daanse.xmla.api.xmla.ImageSave;
import org.eclipse.daanse.xmla.api.xmla.IncrementalProcessingNotification;
import org.eclipse.daanse.xmla.api.xmla.InheritedBinding;
import org.eclipse.daanse.xmla.api.xmla.Insert;
import org.eclipse.daanse.xmla.api.xmla.Kpi;
import org.eclipse.daanse.xmla.api.xmla.Level;
import org.eclipse.daanse.xmla.api.xmla.Lock;
import org.eclipse.daanse.xmla.api.xmla.MajorObject;
import org.eclipse.daanse.xmla.api.xmla.ManyToManyMeasureGroupDimension;
import org.eclipse.daanse.xmla.api.xmla.MdxScript;
import org.eclipse.daanse.xmla.api.xmla.Measure;
import org.eclipse.daanse.xmla.api.xmla.MeasureBinding;
import org.eclipse.daanse.xmla.api.xmla.MeasureGroup;
import org.eclipse.daanse.xmla.api.xmla.MeasureGroupAttribute;
import org.eclipse.daanse.xmla.api.xmla.MeasureGroupBinding;
import org.eclipse.daanse.xmla.api.xmla.MeasureGroupDimension;
import org.eclipse.daanse.xmla.api.xmla.MeasureGroupDimensionBinding;
import org.eclipse.daanse.xmla.api.xmla.Member;
import org.eclipse.daanse.xmla.api.xmla.MergePartitions;
import org.eclipse.daanse.xmla.api.xmla.MiningModel;
import org.eclipse.daanse.xmla.api.xmla.MiningModelColumn;
import org.eclipse.daanse.xmla.api.xmla.MiningModelPermission;
import org.eclipse.daanse.xmla.api.xmla.MiningModelingFlag;
import org.eclipse.daanse.xmla.api.xmla.MiningStructure;
import org.eclipse.daanse.xmla.api.xmla.MiningStructureColumn;
import org.eclipse.daanse.xmla.api.xmla.MiningStructurePermission;
import org.eclipse.daanse.xmla.api.xmla.NotType;
import org.eclipse.daanse.xmla.api.xmla.NotifyTableChange;
import org.eclipse.daanse.xmla.api.xmla.ObjectExpansion;
import org.eclipse.daanse.xmla.api.xmla.ObjectReference;
import org.eclipse.daanse.xmla.api.xmla.Partition;
import org.eclipse.daanse.xmla.api.xmla.Permission;
import org.eclipse.daanse.xmla.api.xmla.Perspective;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveAction;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveAttribute;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveCalculation;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveDimension;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveHierarchy;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveKpi;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveMeasure;
import org.eclipse.daanse.xmla.api.xmla.PerspectiveMeasureGroup;
import org.eclipse.daanse.xmla.api.xmla.ProactiveCaching;
import org.eclipse.daanse.xmla.api.xmla.ProactiveCachingBinding;
import org.eclipse.daanse.xmla.api.xmla.ProactiveCachingIncrementalProcessingBinding;
import org.eclipse.daanse.xmla.api.xmla.ProactiveCachingQueryBinding;
import org.eclipse.daanse.xmla.api.xmla.QueryBinding;
import org.eclipse.daanse.xmla.api.xmla.QueryNotification;
import org.eclipse.daanse.xmla.api.xmla.ReferenceMeasureGroupDimension;
import org.eclipse.daanse.xmla.api.xmla.RegularMeasureGroupDimension;
import org.eclipse.daanse.xmla.api.xmla.ReportAction;
import org.eclipse.daanse.xmla.api.xmla.ReportFormatParameter;
import org.eclipse.daanse.xmla.api.xmla.ReportParameter;
import org.eclipse.daanse.xmla.api.xmla.Restore;
import org.eclipse.daanse.xmla.api.xmla.Role;
import org.eclipse.daanse.xmla.api.xmla.RollbackTransaction;
import org.eclipse.daanse.xmla.api.xmla.RowBinding;
import org.eclipse.daanse.xmla.api.xmla.ScalarMiningStructureColumn;
import org.eclipse.daanse.xmla.api.xmla.Scope;
import org.eclipse.daanse.xmla.api.xmla.Server;
import org.eclipse.daanse.xmla.api.xmla.ServerProperty;
import org.eclipse.daanse.xmla.api.xmla.SetAuthContext;
import org.eclipse.daanse.xmla.api.xmla.StandardAction;
import org.eclipse.daanse.xmla.api.xmla.Statement;
import org.eclipse.daanse.xmla.api.xmla.Subscribe;
import org.eclipse.daanse.xmla.api.xmla.Synchronize;
import org.eclipse.daanse.xmla.api.xmla.TableBinding;
import org.eclipse.daanse.xmla.api.xmla.TableMiningStructureColumn;
import org.eclipse.daanse.xmla.api.xmla.TabularBinding;
import org.eclipse.daanse.xmla.api.xmla.TimeAttributeBinding;
import org.eclipse.daanse.xmla.api.xmla.TimeBinding;
import org.eclipse.daanse.xmla.api.xmla.Trace;
import org.eclipse.daanse.xmla.api.xmla.TraceFilter;
import org.eclipse.daanse.xmla.api.xmla.Translation;
import org.eclipse.daanse.xmla.api.xmla.Unlock;
import org.eclipse.daanse.xmla.api.xmla.Unsubscribe;
import org.eclipse.daanse.xmla.api.xmla.Update;
import org.eclipse.daanse.xmla.api.xmla.UpdateCells;
import org.eclipse.daanse.xmla.api.xmla.UserDefinedGroupBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ACCOUNT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.AGGREGATION_DESIGN_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.AGGREGATION_PREFIX;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ALLOW_DRILL_THROUGH;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ATTRIBUTE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ATTRIBUTES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ATTRIBUTE_HIERARCHY_VISIBLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ATTRIBUTE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CAPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLLATION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMNS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COLUMN_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.COMMAND;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CREATED_TIMESTAMP;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUBE_DIMENSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CUBE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATA_SOURCE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATA_SOURCE_VIEW_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DEFAULT_MEMBER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DESCRIPTION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DIMENSION_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DISPLAY_FOLDER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ESTIMATED_ROWS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ESTIMATED_SIZE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.EXECUTE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.EXPRESSION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.FILTER;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHIES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.HIERARCHY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KEY_COLUMN;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.KEY_COLUMNS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LANGUAGE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LAST_PROCESSED;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LAST_SCHEMA_UPDATE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_GROUP_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MEASURE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.MINING_MODEL_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ORDINAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PERSISTENCE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROCESS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROCESSING_MODE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROCESSING_PRIORITY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROCESSING_STATE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTIES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.PROPERTY_LIST;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.READ_DEFINITION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.REFRESH_INTERVAL;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.REFRESH_POLICY;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.ROLE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SERVER_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SOAP_ACTION_EXECUTE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.SOURCE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.STATE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.STORAGE_LOCATION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.STORAGE_MODE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TABLE_ID;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TRANSLATION;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.TRANSLATIONS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.VALUE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.VALUE2;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.VALUENS;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.VISIBLE;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.VISUALIZATION_PROPERTIES;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.WRITE;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToAlterResponse;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToCancelResponse;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToClearCacheResponse;
import static org.eclipse.daanse.xmla.client.soapmessage.Convertor.convertToStatementResponse;

public class ExecuteServiceImpl extends AbstractService implements ExecuteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteServiceImpl.class);
    private SoapClient soapClient;

    public ExecuteServiceImpl(SoapClient soapClient) {
        this.soapClient = soapClient;
    }

    @Override
    public StatementResponse statement(StatementRequest statementRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(statementRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_EXECUTE), msg);
            return convertToStatementResponse(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("ExecuteService statement error", e);
        }
        return null;
    }

    @Override
    public AlterResponse alter(AlterRequest alterRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(alterRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_EXECUTE), msg);
            return convertToAlterResponse(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("ExecuteService alter error", e);
        }
        return null;
    }

    @Override
    public ClearCacheResponse clearCache(ClearCacheRequest clearCacheRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(clearCacheRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_EXECUTE), msg);
            return convertToClearCacheResponse(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("ExecuteService clearCache error", e);
        }
        return null;
    }


    @Override
    public CancelResponse cancel(CancelRequest cancelRequest) {
        try {
            Consumer<SOAPMessage> msg = getConsumer(cancelRequest);
            SOAPMessage message = soapClient.callSoapWebService(Optional.of(SOAP_ACTION_EXECUTE), msg);
            return convertToCancelResponse(message.getSOAPBody());
        } catch (SOAPException e) {
            LOGGER.error("ExecuteService cancel error", e);
        }
        return null;
    }


    private Consumer<SOAPMessage> getConsumer(CancelRequest requestApi) {
        return message -> {
            try {
                Cancel cancel = requestApi.command();
                Properties properties = requestApi.properties();
                List<ExecuteParameter> executeParameterList = requestApi.parameters();

                SOAPElement execute = message.getSOAPBody()
                    .addChildElement(EXECUTE);
                SOAPElement commandElement = execute.addChildElement(COMMAND);

                setCancel(commandElement, cancel);

                SOAPElement propertyList = execute.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);
                setParameterList(execute, executeParameterList);

            } catch (SOAPException e) {
                LOGGER.error("ExecuteService ClearCacheRequest accept error", e);
            }
        };
    }

    private void setCancel(SOAPElement element, Cancel cancel) {
        SOAPElement cancelElement = addChildElement(element, "Cancel");
        addChildElement(cancelElement, "ConnectionID", String.valueOf(cancel.connectionID()));
        addChildElement(cancelElement, "SessionID", cancel.sessionID());
        addChildElement(cancelElement, "SPID", String.valueOf(cancel.spid()));
        addChildElement(cancelElement, "CancelAssociated", String.valueOf(cancel.cancelAssociated()));

    }

    private Consumer<SOAPMessage> getConsumer(ClearCacheRequest requestApi) {
        return message -> {
            try {
                ClearCache clearCache = requestApi.command();
                Properties properties = requestApi.properties();
                List<ExecuteParameter> executeParameterList = requestApi.parameters();

                SOAPElement execute = message.getSOAPBody()
                    .addChildElement(EXECUTE);
                SOAPElement commandElement = execute.addChildElement(COMMAND);
                setClearCache(commandElement, clearCache);

                SOAPElement propertyList = execute.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);
                setParameterList(execute, executeParameterList);

            } catch (SOAPException e) {
                LOGGER.error("ExecuteService ClearCacheRequest accept error", e);
            }
        };
    }

    private Consumer<SOAPMessage> getConsumer(AlterRequest requestApi) {
        return message -> {
            try {
                Alter alter = requestApi.command();
                Properties properties = requestApi.properties();
                List<ExecuteParameter> executeParameterList = requestApi.parameters();

                SOAPElement execute = message.getSOAPBody()
                    .addChildElement(EXECUTE);
                SOAPElement commandElement = execute.addChildElement(COMMAND);
                setAlter(commandElement, alter);

                SOAPElement propertyList = execute.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);
                setParameterList(execute, executeParameterList);

            } catch (SOAPException e) {
                LOGGER.error("ExecuteService AlterRequest accept error", e);
            }
        };
    }

    private void setParameterList(SOAPElement element, List<ExecuteParameter> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Parameters");
            list.forEach(it -> setExecuteParameter(chElement, it));
        }
    }

    private void setExecuteParameter(SOAPElement element, ExecuteParameter it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Parameter");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, VALUE2, it.value() == null ? null : it.value().toString());
        }
    }

    private void setTrace(SOAPElement element, Trace it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Trace");
            addChildElement(chElement, "LogFileName", it.logFileName());
            addChildElement(chElement, "LogFileAppend", String.valueOf(it.logFileAppend()));
            addChildElement(chElement, "LogFileSize", String.valueOf(it.logFileSize()));
            addChildElement(chElement, "Audit", String.valueOf(it.audit()));
            addChildElement(chElement, "LogFileRollover", String.valueOf(it.logFileRollover()));
            addChildElement(chElement, "AutoRestart", String.valueOf(it.autoRestart()));
            addChildElement(chElement, "StopTime", convertInstant(it.stopTime()));
            setTraceFilter(chElement, it.filter());
            setEventType(chElement, it.eventType());
        }
    }

    private void setEventType(SOAPElement element, EventType it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "EventType");
            setEventList(chElement, it.events());
            setXEvent(chElement, it.xEvent());
        }
    }

    private void setXEvent(SOAPElement element, XEvent it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "XEvent");
            setEventSession(chElement, it.eventSession());
        }
    }

    private void setEventSession(SOAPElement element, EventSession it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "event_session");
            addChildElement(chElement, "name", it.name());
            addChildElement(chElement, "maxMemory", String.valueOf(it.maxMemory()));
            addChildElement(chElement, "eventRetentionMode", it.eventRetentionMode() == null ? null :
                it.eventRetentionMode().value());
            addChildElement(chElement, "dispatchLatency", String.valueOf(it.dispatchLatency()));
            addChildElement(chElement, "maxEventSize", String.valueOf(it.maxEventSize()));
            addChildElement(chElement, "memoryPartitionMode", it.eventRetentionMode() == null ? null :
                it.eventRetentionMode().value());
            addChildElement(chElement, "trackCausality", String.valueOf(it.trackCausality()));
        }
    }

    private void setEventList(SOAPElement element, List<Event> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Events");
            list.forEach(it -> setEvent(chElement, it));
        }
    }

    private void setEvent(SOAPElement element, Event it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Event");
            addChildElement(chElement, "EventID", it.eventID());
            setEventColumnID(chElement, it.columns());
        }
    }

    private void setEventColumnID(SOAPElement element, EventColumnID it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, COLUMNS);
            setColumnID(chElement, it.columnID());
        }

    }

    private void setColumnID(SOAPElement element, List<String> list) {
        if (list != null) {
            list.forEach(it -> addChildElement(element, COLUMN_ID).setTextContent(it));
        }
    }

    private void setTraceFilter(SOAPElement element, TraceFilter it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, FILTER);
            setNotType(chElement, "Not", it.not());
            setAndOrType(chElement, "Or", it.or());
            setAndOrType(chElement, "And", it.and());
            setBoolBinop(chElement, "Equal", it.isEqual());
            setBoolBinop(chElement, "NotEqual", it.notEqual());
            setBoolBinop(chElement, "Less", it.less());
            setBoolBinop(chElement, "LessOrEqual", it.lessOrEqual());
            setBoolBinop(chElement, "Greater", it.greater());
            setBoolBinop(chElement, "GreaterOrEqual", it.greaterOrEqual());
            setBoolBinop(chElement, "Like", it.like());
            setBoolBinop(chElement, "NotLike", it.notLike());
        }
    }

    private void setBoolBinop(SOAPElement element, String nodeName, BoolBinop it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, nodeName);
            addChildElement(chElement, COLUMN_ID, it.columnID());
            addChildElement(chElement, VALUE2, it.value());
        }
    }

    private void setAndOrType(SOAPElement element, String nodeName, AndOrType it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, nodeName);
            setAndOrTypeEnumList(chElement, it.notOrOrOrAnd());
        }
    }

    private void setAndOrTypeEnumList(SOAPElement element, List<AndOrTypeEnum> list) {
        if (list != null) {
            list.forEach(it -> addChildElement(element, it.name()));
        }
    }

    private void setNotType(SOAPElement element, String nodeName, NotType it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, nodeName);
            setNotType(chElement, "Not", it.not());
            setAndOrType(chElement, "Or", it.or());
            setAndOrType(chElement, "And", it.and());
            setBoolBinop(chElement, "Equal", it.isEqual());
            setBoolBinop(chElement, "NotEqual", it.notEqual());
            setBoolBinop(chElement, "Less", it.less());
            setBoolBinop(chElement, "LessOrEqual", it.lessOrEqual());
            setBoolBinop(chElement, "Greater", it.greater());
            setBoolBinop(chElement, "GreaterOrEqual", it.greaterOrEqual());
            setBoolBinop(chElement, "Like", it.like());
            setBoolBinop(chElement, "NotLike", it.notLike());
        }
    }

    private void setServer(SOAPElement element, Server it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Server");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, CREATED_TIMESTAMP, String.valueOf(it.createdTimestamp()));
            addChildElement(chElement, LAST_SCHEMA_UPDATE, String.valueOf(it.lastSchemaUpdate()));
            addChildElement(chElement, DESCRIPTION, it.description());
            setAnnotationList(chElement, it.annotations());
            addChildElement(chElement, "ProductName", it.productName());
            addChildElement(chElement, "Edition", it.edition());
            addChildElement(chElement, "EditionID", String.valueOf(it.editionID()));
            addChildElement(chElement, "Version", it.version());
            addChildElement(chElement, "ServerMode", it.serverMode());
            addChildElement(chElement, "ProductLevel", it.productLevel());
            addChildElement(chElement, "DefaultCompatibilityLevel", String.valueOf(it.defaultCompatibilityLevel()));
            addChildElement(chElement, "SupportedCompatibilityLevels", it.supportedCompatibilityLevels());
            setDatabaseList(chElement, it.databases());
            setAssemblyList(chElement, it.assemblies());
            setTraceList(chElement, it.traces());
            setRoleList(chElement, it.roles());
            setServerPropertyList(chElement, it.serverProperties());
        }
    }

    private void setServerPropertyList(SOAPElement element, List<ServerProperty> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "ServerProperties");
            list.forEach(it -> setServerProperty(chElement, it));
        }
    }

    private void setServerProperty(SOAPElement element, ServerProperty it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "ServerProperty");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, VALUE2, it.value());
            addChildElement(chElement, "RequiresRestart", String.valueOf(it.requiresRestart()));
            addChildElement(chElement, "PendingValue", it.pendingValue().toString());
            addChildElement(chElement, "DefaultValue", it.defaultValue().toString());
            addChildElement(chElement, "DisplayFlag", String.valueOf(it.displayFlag()));
            addChildElement(chElement, "Type", it.type());
        }
    }

    private void setRoleList(SOAPElement element, List<Role> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Roles");
            list.forEach(it -> setRole(chElement, it));
        }
    }

    private void setTraceList(SOAPElement element, List<Trace> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Traces");
            list.forEach(it -> setTrace(chElement, it));
        }
    }

    private void setAssemblyList(SOAPElement element, List<Assembly> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Assemblies");
            list.forEach(it -> setAssembly(chElement, it));
        }
    }

    private void setDatabaseList(SOAPElement element, List<Database> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Databases");
            list.forEach(it -> setDatabase(chElement, it));
        }
    }

    private void setRole(SOAPElement element, Role it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Role");
            addChildElement(chElement, "Name", it.name());
            it.id().ifPresent(v -> addChildElement(chElement, "ID", v));
            it.createdTimestamp().ifPresent(v -> addChildElement(chElement, CREATED_TIMESTAMP, convertInstant(v)));
            it.lastSchemaUpdate().ifPresent(v -> addChildElement(chElement, LAST_SCHEMA_UPDATE, convertInstant(v)));
            it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
            it.members().ifPresent(v -> setMemberList(chElement, v));
        }
    }

    private void setMemberList(SOAPElement element, List<Member> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Members");
            list.forEach(it -> setMember(chElement, it));
        }
    }

    private void setMember(SOAPElement element, Member it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Member");
            it.name().ifPresent(v -> addChildElement(chElement, "Name", v));
            it.sid().ifPresent(v -> addChildElement(chElement, "Sid", v));
        }
    }

    private void setPerspective(SOAPElement element, Perspective it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Perspective");
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            addChildElement(chElement, "DefaultMeasure", it.defaultMeasure());
            setPerspectiveDimensionList(chElement, it.dimensions());
            setPerspectiveMeasureGroupList(chElement, it.measureGroups());
            setPerspectiveCalculationList(chElement, it.calculations());
            setPerspectiveKpiList(chElement, it.kpis());
            setPerspectiveActionList(chElement, it.actions());
        }
    }

    private void setPerspectiveActionList(SOAPElement element, List<PerspectiveAction> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Actions");
            list.forEach(it -> setPerspectiveAction(chElement, it));
        }
    }

    private void setPerspectiveAction(SOAPElement element, PerspectiveAction it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Action");
            addChildElement(chElement, "ActionID", it.actionID());
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPerspectiveKpiList(SOAPElement element, List<PerspectiveKpi> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Kpis");
            list.forEach(it -> setPerspectiveKpi(chElement, it));
        }
    }

    private void setPerspectiveKpi(SOAPElement element, PerspectiveKpi it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Kpi");
            addChildElement(chElement, "KpiID", it.kpiID());
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPerspectiveCalculationList(SOAPElement element, List<PerspectiveCalculation> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Calculations");
            list.forEach(it -> setPerspectiveCalculation(chElement, it));
        }
    }

    private void setPerspectiveCalculation(SOAPElement element, PerspectiveCalculation it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Calculation");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "Type", it.type());
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPerspectiveMeasureGroupList(SOAPElement element, List<PerspectiveMeasureGroup> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "MeasureGroups");
            list.forEach(it -> setPerspectiveMeasureGroup(chElement, it));
        }

    }

    private void setPerspectiveMeasureGroup(SOAPElement element, PerspectiveMeasureGroup it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "MeasureGroup");
            addChildElement(chElement, MEASURE_GROUP_ID, it.measureGroupID());
            it.measures().ifPresent(v -> setPerspectiveMeasureList(chElement, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPerspectiveMeasureList(SOAPElement element, List<PerspectiveMeasure> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, MEASURES);
            list.forEach(it -> setPerspectiveMeasure(chElement, it));
        }

    }

    private void setPerspectiveMeasure(SOAPElement element, PerspectiveMeasure it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, MEASURE);
            addChildElement(chElement, MEASURE_ID, it.measureID());
            setAnnotationList(chElement, it.annotations());
        }
    }

    private void setPerspectiveDimensionList(SOAPElement element, List<PerspectiveDimension> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, DIMENSIONS);
            list.forEach(it -> setPerspectiveDimension(chElement, it));
        }
    }

    private void setPerspectiveDimension(SOAPElement element, PerspectiveDimension it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, DIMENSION);
            addChildElement(chElement, CUBE_DIMENSION_ID, it.cubeDimensionID());
            it.attributes().ifPresent(v -> setPerspectiveAttributeList(chElement, v));
            it.hierarchies().ifPresent(v -> setPerspectiveHierarchyList(chElement, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPerspectiveHierarchyList(SOAPElement element, List<PerspectiveHierarchy> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, HIERARCHIES);
            list.forEach(it -> setPerspectiveHierarchy(chElement, it));
        }
    }

    private void setPerspectiveHierarchy(SOAPElement element, PerspectiveHierarchy it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, HIERARCHY);
            addChildElement(chElement, "HierarchyID", it.hierarchyID());
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPerspectiveAttributeList(SOAPElement element, List<PerspectiveAttribute> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTES);
            list.forEach(it -> setPerspectiveAttribute(chElement, it));
        }
    }

    private void setPerspectiveAttribute(SOAPElement element, PerspectiveAttribute it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTE);
            addChildElement(chElement, ATTRIBUTE_ID, it.attributeID());
            it.attributeHierarchyVisible().ifPresent(v -> addChildElement(chElement, ATTRIBUTE_HIERARCHY_VISIBLE,
             String.valueOf(v)));
            it.defaultMember().ifPresent(v -> addChildElement(chElement, DEFAULT_MEMBER, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPermission(SOAPElement element, String tagName, Permission it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, tagName);
            addChildElement(chElement, ROLE_ID, it.roleID());
            it.process().ifPresent(v -> addChildElement(chElement, PROCESS, String.valueOf(v)));
            it.readDefinition().ifPresent(v -> addChildElement(chElement, READ_DEFINITION, v.getValue()));
            it.read().ifPresent(v -> addChildElement(chElement, "Read", v.getValue()));
            it.write().ifPresent(v -> addChildElement(chElement, WRITE, v.getValue()));
            if (it instanceof DatabasePermission dp) {
                dp.administer().ifPresent(v -> addChildElement(chElement, "Administer", String.valueOf(v)));
            }
            if (it instanceof DataSourcePermission) {

            }
            if (it instanceof DimensionPermission dp) {
                dp.attributePermissions().ifPresent(v -> setAttributePermissionList(chElement, v));
                dp.allowedRowsExpression().ifPresent(v -> addChildElement(chElement, "AllowedRowsExpression", v));
            }
            if (it instanceof MiningStructurePermission mp) {
                mp.allowDrillThrough().ifPresent(v -> addChildElement(chElement, ALLOW_DRILL_THROUGH, String.valueOf(v)));
            }
            if (it instanceof MiningModelPermission mmp) {
                mmp.allowDrillThrough().ifPresent(v -> addChildElement(chElement, ALLOW_DRILL_THROUGH, String.valueOf(v)));
                mmp.allowBrowsing().ifPresent(v -> addChildElement(chElement, "AllowBrowsing", String.valueOf(v)));

            }
            if (it instanceof CubePermission cp) {
                cp.readSourceData().ifPresent(v -> addChildElement(chElement, "ReadSourceData", v));
                cp.dimensionPermissions().ifPresent(v -> setCubeDimensionPermissionList(chElement, v));
                cp.cellPermissions().ifPresent(v -> setCellPermissionList(chElement, v));
            }
        }
    }

    private void setPartition(SOAPElement element, Partition it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Partition");
            setTabularBinding(chElement, it.source());
            addChildElement(chElement, PROCESSING_PRIORITY, String.valueOf(it.processingPriority()));
            addChildElement(chElement, AGGREGATION_PREFIX, it.aggregationPrefix());
            setPartitionStorageMode(chElement, it.storageMode());
            addChildElement(chElement, PROCESSING_MODE, it.processingMode());
            setErrorConfiguration(chElement, it.errorConfiguration());
            addChildElement(chElement, STORAGE_LOCATION, it.storageLocation());
            addChildElement(chElement, "RemoteDatasourceID", it.remoteDatasourceID());
            addChildElement(chElement, "Slice", it.slice());
            setProactiveCaching(chElement, it.proactiveCaching());
            addChildElement(chElement, "Type", it.type());
            addChildElement(chElement, ESTIMATED_SIZE, String.valueOf(it.estimatedSize()));
            addChildElement(chElement, ESTIMATED_ROWS, String.valueOf(it.estimatedRows()));
            setPartitionCurrentStorageMode(chElement, it.currentStorageMode());
            addChildElement(chElement, AGGREGATION_DESIGN_ID, it.aggregationDesignID());
            setAggregationInstanceList(chElement, it.aggregationInstances());
            setDataSourceViewBinding(chElement, it.aggregationInstanceSource());
            addChildElement(chElement, LAST_PROCESSED, convertInstant(it.lastProcessed()));
            addChildElement(chElement, STATE, it.state());
            addChildElement(chElement, "StringStoresCompatibilityLevel",
                String.valueOf(it.stringStoresCompatibilityLevel()));
            addChildElement(chElement, "CurrentStringStoresCompatibilityLevel",
             String.valueOf(it.currentStringStoresCompatibilityLevel()));
            addChildElement(chElement, "DirectQueryUsage", it.directQueryUsage());
        }
    }

    private void setAggregationInstanceList(SOAPElement element, List<AggregationInstance> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "AggregationInstances");
            list.forEach(it -> setAggregationInstance(chElement, it));
        }
    }

    private void setAggregationInstance(SOAPElement element, AggregationInstance it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "AggregationInstance");
            addChildElement(chElement, "AggregationType", it.aggregationType());
            setTabularBinding(chElement, it.source());
            setAggregationInstanceDimensionList(chElement, it.dimensions());
            setAggregationInstanceMeasureList(chElement, it.measures());
        }
    }

    private void setAggregationInstanceMeasureList(SOAPElement element, List<AggregationInstanceMeasure> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, MEASURES);
            list.forEach(it -> setAggregationInstanceMeasure(chElement, it));
        }
    }

    private void setAggregationInstanceMeasure(SOAPElement element, AggregationInstanceMeasure it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, MEASURE);
            addChildElement(chElement, MEASURE_ID, it.measureID());
            setColumnBinding(chElement, it.source());
        }
    }

    private void setColumnBinding(SOAPElement element, ColumnBinding it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, SOURCE);
            addChildElement(chElement, TABLE_ID, it.tableID());
            addChildElement(chElement, COLUMN_ID, it.columnID());
        }
    }

    private void setAggregationInstanceDimensionList(SOAPElement element, List<AggregationInstanceDimension> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, DIMENSIONS);
            list.forEach(it -> setAggregationInstanceDimension(chElement, it));
        }
    }

    private void setAggregationInstanceDimension(SOAPElement element, AggregationInstanceDimension it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, DIMENSION);
            addChildElement(chElement, CUBE_DIMENSION_ID, it.cubeDimensionID());
            it.attributes().ifPresent(v -> setAggregationInstanceAttributeList(chElement, v));
        }
    }

    private void setAggregationInstanceAttributeList(SOAPElement element, List<AggregationInstanceAttribute> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTES);
            list.forEach(it -> setAggregationInstanceAttribute(chElement, it));
        }
    }

    private void setAggregationInstanceAttribute(SOAPElement element, AggregationInstanceAttribute it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTE);
            addChildElement(chElement, ATTRIBUTE_ID, it.attributeID());
            it.keyColumns().ifPresent(v -> setDataItemList(chElement, KEY_COLUMNS, KEY_COLUMN, v));
        }
    }

    private void setDataItemList(SOAPElement element, String wrapperName, String tagName, List<DataItem> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, wrapperName);
            list.forEach(it -> setDataItem(chElement, tagName, it));
        }
    }

    private void setDataItem(SOAPElement element, String tagName, DataItem it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, tagName);
            addChildElement(chElement, "DataType", it.dataType());
            it.dataSize().ifPresent(v -> addChildElement(chElement, "DataSize", String.valueOf(v)));
            it.mimeType().ifPresent(v -> addChildElement(chElement, "MimeType", v));
            it.nullProcessing().ifPresent(v -> addChildElement(chElement, "NullProcessing", v.getValue()));
            it.trimming().ifPresent(v -> addChildElement(chElement, "Trimming", v));
            it.invalidXmlCharacters().ifPresent(v -> addChildElement(chElement, "InvalidXmlCharacters", v.getValue()));
            it.collation().ifPresent(v -> addChildElement(chElement, COLLATION, v));
            it.format().ifPresent(v -> addChildElement(chElement, "Format", v.getValue()));
            it.source().ifPresent(v -> setBinding(chElement, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setPartitionCurrentStorageMode(SOAPElement element, Partition.CurrentStorageMode it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "CurrentStorageMode");
            chElement.setTextContent(it.value().value());
            chElement.setAttribute(VALUENS, it.valuens());
        }
    }

    private void setPartitionStorageMode(SOAPElement element, Partition.StorageMode it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, STORAGE_MODE);
            chElement.setTextContent(it.value().value());
            chElement.setAttribute(VALUENS, it.valuens());
        }
    }

    private void setTabularBinding(SOAPElement element, TabularBinding it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, SOURCE);
            if (it instanceof TableBinding tb) {
                tb.dataSourceID().ifPresent(v -> addChildElement(chElement, DATA_SOURCE_ID, v));
                addChildElement(chElement, "DbTableName", tb.dbTableName());
                tb.dbSchemaName().ifPresent(v -> addChildElement(chElement, "DbSchemaName", v));
            }
            if (it instanceof QueryBinding qb) {
                qb.dataSourceID().ifPresent(v -> addChildElement(chElement, DATA_SOURCE_ID, v));
                addChildElement(chElement, "QueryDefinition", qb.queryDefinition());
            }
            if (it instanceof DSVTableBinding dtb) {
                dtb.dataSourceViewID().ifPresent(v -> addChildElement(chElement, DATA_SOURCE_VIEW_ID, v));
                addChildElement(chElement, TABLE_ID, dtb.tableID());
                dtb.dataEmbeddingStyle().ifPresent(v -> addChildElement(chElement, "DataEmbeddingStyle", v));
            }
        }
    }

    private void setMiningStructure(SOAPElement element, MiningStructure it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "MiningStructure");
            it.source().ifPresent(v -> setBinding(chElement, v));
            it.lastProcessed().ifPresent(v -> addChildElement(chElement, LAST_PROCESSED, convertInstant(v)));
            it.translations().ifPresent(v -> setTranslationList(chElement, TRANSLATIONS, TRANSLATION, v));
            it.language().ifPresent(v -> addChildElement(chElement, LANGUAGE, String.valueOf(v)));
            it.collation().ifPresent(v -> addChildElement(chElement, COLLATION, v));
            it.errorConfiguration().ifPresent(v -> setErrorConfiguration(chElement, v));
            it.cacheMode().ifPresent(v -> addChildElement(chElement, "CacheMode", v));
            it.holdoutMaxPercent().ifPresent(v -> addChildElement(chElement, "HoldoutMaxPercent", String.valueOf(v)));
            it.holdoutMaxCases().ifPresent(v -> addChildElement(chElement, "HoldoutMaxCases", String.valueOf(v)));
            it.holdoutMaxCases().ifPresent(v -> addChildElement(chElement, "HoldoutSeed", String.valueOf(v)));
            it.holdoutMaxCases().ifPresent(v -> addChildElement(chElement, "HoldoutActualSize", String.valueOf(v)));
            setMiningStructureColumnList(chElement, it.columns());
            it.state().ifPresent(v -> addChildElement(chElement, STATE, v));
            it.miningStructurePermissions().ifPresent(v -> setMiningStructurePermissionList(chElement, v));
            it.miningModels().ifPresent(v -> setMiningModelList(chElement, v));
        }
    }

    private void setMiningModelList(SOAPElement element, List<MiningModel> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "MiningModels");
            list.forEach(it -> setMiningModel(chElement, it));
        }
    }

    private void setMiningStructurePermissionList(SOAPElement element, List<MiningStructurePermission> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "MiningStructurePermissions");
            list.forEach(it -> setMiningStructurePermission(chElement, it));

        }

    }

    private void setMiningStructurePermission(SOAPElement element, MiningStructurePermission it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "MiningStructurePermission");
            addChildElement(chElement, ROLE_ID, it.roleID());
            it.process().ifPresent(v -> addChildElement(chElement, PROCESS, String.valueOf(v)));
            it.readDefinition().ifPresent(v -> addChildElement(chElement, READ_DEFINITION, v.getValue()));
            it.read().ifPresent(v -> addChildElement(chElement, "Read", v.getValue()));
            it.write().ifPresent(v -> addChildElement(chElement, WRITE, v.getValue()));
        }
    }

    private void setMiningStructureColumnList(SOAPElement element, List<MiningStructureColumn> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, COLUMNS);
            list.forEach(it -> setMiningStructureColumn(chElement, it));
        }
    }

    private void setMiningStructureColumn(SOAPElement element, MiningStructureColumn it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Column");
            if (it instanceof ScalarMiningStructureColumn smsc) {
                addChildElement(chElement, "Name", smsc.name());
                smsc.id().ifPresent(v -> addChildElement(chElement, "ID", v));
                smsc.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
                smsc.type().ifPresent(v -> addChildElement(chElement, "Type", v));
                smsc.annotations().ifPresent(v -> setAnnotationList(chElement, v));
                smsc.isKey().ifPresent(v -> addChildElement(chElement, "IsKey", String.valueOf(v)));
                smsc.source().ifPresent(v -> setBinding(chElement, v));
                smsc.description().ifPresent(v -> addChildElement(chElement, "Distribution", v));
                smsc.modelingFlags().ifPresent(v -> setMiningModelingFlagList(chElement, v));
                addChildElement(chElement, "Content", smsc.content());
                smsc.classifiedColumns().ifPresent(v -> setClassifiedColumnList(chElement, v));
                smsc.discretizationMethod().ifPresent(v -> addChildElement(chElement, "DiscretizationMethod", v));
                smsc.discretizationBucketCount().ifPresent(v -> addChildElement(chElement, "DiscretizationBucketCount"
                , String.valueOf(v)));
                smsc.keyColumns().ifPresent(v -> setDataItemList(chElement, KEY_COLUMNS, KEY_COLUMN, v));
                smsc.nameColumn().ifPresent(v -> setDataItem(chElement, "NameColumn", v));
                smsc.translations().ifPresent(v -> setTranslationList(chElement, TRANSLATIONS, TRANSLATION, v));
            }
            if (it instanceof TableMiningStructureColumn tmsc) {
                tmsc.foreignKeyColumns().ifPresent(v -> setDataItemList(chElement, "ForeignKeyColumns",
"ForeignKeyColumn", v));
                tmsc.sourceMeasureGroup().ifPresent(v -> setMeasureGroupBinding(chElement, v));
                tmsc.columns().ifPresent(v -> setMiningStructureColumnList(chElement, v));
                tmsc.translations().ifPresent(v -> setTranslationList(chElement, TRANSLATIONS, TRANSLATION, v));
            }
        }
    }

    private void setMeasureGroupBinding(SOAPElement element, MeasureGroupBinding it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "SourceMeasureGroup");
            addChildElement(chElement, DATA_SOURCE_ID, it.dataSourceID());
            addChildElement(chElement, CUBE_ID, it.cubeID());
            addChildElement(chElement, MEASURE_GROUP_ID, it.measureGroupID());
            it.persistence().ifPresent(v -> addChildElement(chElement, PERSISTENCE, v.getValue()));
            it.refreshPolicy().ifPresent(v -> addChildElement(chElement, REFRESH_POLICY, v.getValue()));
            it.refreshInterval().ifPresent(v -> addChildElement(chElement, REFRESH_INTERVAL, convertDuration(v)));
            it.filter().ifPresent(v -> addChildElement(chElement, FILTER, v));
        }
    }

    private void setClassifiedColumnList(SOAPElement element, List<String> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "ClassifiedColumns");
            list.forEach(it -> addChildElement(chElement, "ClassifiedColumn").setTextContent(it));
        }
    }

    private void setMiningModelingFlagList(SOAPElement element, List<MiningModelingFlag> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "ModelingFlags");
            list.forEach(it -> setMiningModelingFlag(chElement, it));
        }
    }

    private void setMiningModelingFlag(SOAPElement element, MiningModelingFlag it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "ModelingFlags");
            it.modelingFlag().ifPresent(v -> addChildElement(chElement, "ModelingFlag", v));
        }
    }

    private void setMiningModel(SOAPElement element, MiningModel it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "MiningModel");
            addChildElement(chElement, "Algorithm", it.algorithm());
            it.lastProcessed().ifPresent(v -> addChildElement(chElement, LAST_PROCESSED, convertInstant(v)));
            it.algorithmParameters().ifPresent(v -> setAlgorithmParameterList(chElement, v));
            it.allowDrillThrough().ifPresent(v -> addChildElement(chElement, ALLOW_DRILL_THROUGH, String.valueOf(v)));
            it.translations().ifPresent(v -> setAttributeTranslationList(chElement, v));
            it.columns().ifPresent(v -> setMiningModelColumnList(chElement, v));
            it.state().ifPresent(v -> addChildElement(chElement, STATE, v));
            it.foldingParameters().ifPresent(v -> setFoldingParameters(chElement, v));
            it.filter().ifPresent(v -> addChildElement(chElement, FILTER, v));
            it.miningModelPermissions().ifPresent(v -> setMiningModelPermissionList(chElement, v));
            it.language().ifPresent(v -> addChildElement(chElement, LANGUAGE, v));
            it.collation().ifPresent(v -> addChildElement(chElement, COLLATION, v));
        }
    }

    private void setMiningModelPermissionList(SOAPElement element, List<MiningModelPermission> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "MiningModelPermissions");
            list.forEach(it -> setMiningModelPermission(chElement, it));
        }
    }

    private void setMiningModelPermission(SOAPElement element, MiningModelPermission it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "MiningModelPermission");
            addChildElement(chElement, ROLE_ID, it.roleID());
            it.process().ifPresent(v -> addChildElement(chElement, PROCESS, String.valueOf(v)));
            it.readDefinition().ifPresent(v -> addChildElement(chElement, READ_DEFINITION, v.getValue()));
            it.read().ifPresent(v -> addChildElement(chElement, "Read", v.getValue()));
            it.write().ifPresent(v -> addChildElement(chElement, WRITE, v.getValue()));
            it.allowDrillThrough().ifPresent(v -> addChildElement(chElement, ALLOW_DRILL_THROUGH, String.valueOf(v)));
            it.allowBrowsing().ifPresent(v -> addChildElement(chElement, "AllowBrowsing", String.valueOf(v)));
        }
    }

    private void setFoldingParameters(SOAPElement element, FoldingParameters it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "FoldingParameters");
            addChildElement(chElement, "FoldIndex", String.valueOf(it.foldIndex()));
            addChildElement(chElement, "FoldCount", String.valueOf(it.foldCount()));
            it.foldMaxCases().ifPresent(v -> addChildElement(chElement, "FoldMaxCases", String.valueOf(v)));
            it.foldTargetAttribute().ifPresent(v -> addChildElement(chElement, "FoldTargetAttribute", v));
        }
    }

    private void setMiningModelColumnList(SOAPElement element, List<MiningModelColumn> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, COLUMNS);
            list.forEach(it -> setMiningModelColumn(chElement, it));
        }
    }

    private void setMiningModelColumn(SOAPElement element, MiningModelColumn it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Column");
            addChildElement(chElement, "Name", it.name());
            it.id().ifPresent(v -> addChildElement(chElement, "ID", v));
            it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
            it.sourceColumnID().ifPresent(v -> addChildElement(chElement, "SourceColumnID", v));
            it.usage().ifPresent(v -> addChildElement(chElement, "Usage", v));
            it.filter().ifPresent(v -> addChildElement(chElement, FILTER, v));
            it.translations().ifPresent(v -> setTranslationList(chElement, TRANSLATIONS, TRANSLATION, v));
            it.columns().ifPresent(v -> setMiningModelColumnList(chElement, v));
            it.modelingFlags().ifPresent(v -> setMiningModelingFlagList(chElement, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setAttributeTranslationList(SOAPElement element, List<AttributeTranslation> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, TRANSLATIONS);
            list.forEach(it -> setAttributeTranslation(chElement, it));
        }
    }

    private void setAttributeTranslation(SOAPElement element, AttributeTranslation it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, TRANSLATION);
            addChildElement(chElement, LANGUAGE, String.valueOf(it.language()));
            it.caption().ifPresent(v -> addChildElement(chElement, CAPTION, v));
            it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
            it.displayFolder().ifPresent(v -> addChildElement(chElement, DISPLAY_FOLDER, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
            it.captionColumn().ifPresent(v -> setDataItem(chElement, "CaptionColumn", v));
            it.membersWithDataCaption().ifPresent(v -> addChildElement(chElement, "MembersWithDataCaption", v));
        }
    }

    private void setAlgorithmParameterList(SOAPElement element, List<AlgorithmParameter> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "AlgorithmParameters");
            list.forEach(it -> setAlgorithmParameter(chElement, it));
        }
    }

    private void setAlgorithmParameter(SOAPElement element, AlgorithmParameter it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "AlgorithmParameter");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, VALUE2, it.value().toString());
        }
    }

    private void setMeasureGroup(SOAPElement element, MeasureGroup it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "MeasureGroup");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, CREATED_TIMESTAMP, String.valueOf(it.createdTimestamp()));
            addChildElement(chElement, LAST_SCHEMA_UPDATE, String.valueOf(it.lastSchemaUpdate()));
            addChildElement(chElement, DESCRIPTION, it.description());
            setAnnotationList(chElement, it.annotations());
            addChildElement(chElement, LAST_PROCESSED, String.valueOf(it.lastProcessed()));
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            addChildElement(chElement, "Type", it.type());
            addChildElement(chElement, STATE, it.state());
            setMeasureList(chElement, it.measures());
            addChildElement(chElement, "DataAggregation", it.dataAggregation());
            setMeasureGroupBinding(chElement, it.source());
            setMeasureGroupStorageMode(chElement, it.storageMode());
            addChildElement(chElement, STORAGE_LOCATION, it.storageLocation());
            addChildElement(chElement, "IgnoreUnrelatedDimensions", String.valueOf(it.ignoreUnrelatedDimensions()));
            setProactiveCaching(chElement, it.proactiveCaching());
            addChildElement(chElement, ESTIMATED_ROWS, String.valueOf(it.estimatedRows()));
            setErrorConfiguration(chElement, it.errorConfiguration());
            addChildElement(chElement, ESTIMATED_SIZE, String.valueOf(it.estimatedSize()));
            addChildElement(chElement, PROCESSING_MODE, it.processingMode());
            setMeasureGroupDimensionList(chElement, it.dimensions());
            setPartitionList(chElement, it.partitions());
            addChildElement(chElement, AGGREGATION_PREFIX, it.aggregationPrefix());
            addChildElement(chElement, PROCESSING_PRIORITY, String.valueOf(it.processingPriority()));
            setAggregationDesignList(chElement, it.aggregationDesigns());
        }
    }

    private void setAggregationDesignList(SOAPElement element, List<AggregationDesign> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "AggregationDesigns");
            list.forEach(it -> setAggregationDesign(chElement, it));
        }
    }

    private void setPartitionList(SOAPElement element, List<Partition> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Partitions");
            list.forEach(it -> setPartition(chElement, it));
        }
    }

    private void setMeasureGroupDimensionList(SOAPElement element, List<MeasureGroupDimension> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, DIMENSIONS);
            list.forEach(it -> setMeasureGroupDimension(chElement, it));
        }
    }

    private void setMeasureGroupDimension(SOAPElement element, MeasureGroupDimension it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, DIMENSION);
            if (it instanceof ManyToManyMeasureGroupDimension d) {
                addChildElement(chElement, MEASURE_GROUP_ID, String.valueOf(d.measureGroupID()));
                addChildElement(chElement, "DirectSlice", String.valueOf(d.directSlice()));
                addChildElement(chElement, CUBE_DIMENSION_ID, String.valueOf(d.cubeDimensionID()));
                setAnnotationList(chElement, d.annotations());
                setMeasureGroupDimensionBinding(chElement, d.source());
            }
            if (it instanceof RegularMeasureGroupDimension d) {
                addChildElement(chElement, "Cardinality", d.cardinality());
                setMeasureGroupAttributeList(chElement, d.attributes());
                addChildElement(chElement, CUBE_DIMENSION_ID, String.valueOf(d.cubeDimensionID()));
                setAnnotationList(chElement, d.annotations());
                setMeasureGroupDimensionBinding(chElement, d.source());

            }
            if (it instanceof ReferenceMeasureGroupDimension d) {
                addChildElement(chElement, "IntermediateCubeDimensionID", d.intermediateCubeDimensionID());
                addChildElement(chElement, "IntermediateGranularityAttributeID",
                    d.intermediateGranularityAttributeID());
                addChildElement(chElement, "Materialization", d.materialization());
                addChildElement(chElement, PROCESSING_STATE, d.processingState());
                addChildElement(chElement, CUBE_DIMENSION_ID, String.valueOf(d.cubeDimensionID()));
                setAnnotationList(chElement, d.annotations());
                setMeasureGroupDimensionBinding(chElement, d.source());

            }
            if (it instanceof DegenerateMeasureGroupDimension d) {
                addChildElement(chElement, "ShareDimensionStorage", d.shareDimensionStorage());
                addChildElement(chElement, CUBE_DIMENSION_ID, String.valueOf(d.cubeDimensionID()));
                setAnnotationList(chElement, d.annotations());
                setMeasureGroupDimensionBinding(chElement, d.source());
            }
            if (it instanceof DataMiningMeasureGroupDimension d) {
                addChildElement(chElement, "CaseCubeDimensionID", d.caseCubeDimensionID());
                addChildElement(chElement, CUBE_DIMENSION_ID, String.valueOf(d.cubeDimensionID()));
                setAnnotationList(chElement, d.annotations());
                setMeasureGroupDimensionBinding(chElement, d.source());
            }
        }
    }

    private void setMeasureGroupAttributeList(SOAPElement element, List<MeasureGroupAttribute> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTES);
            list.forEach(it -> setMeasureGroupAttribute(chElement, it));
        }
    }

    private void setMeasureGroupAttribute(SOAPElement element, MeasureGroupAttribute it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTE);
            addChildElement(chElement, ATTRIBUTE_ID, it.attributeID());
            setDataItemList(chElement, KEY_COLUMNS, KEY_COLUMN, it.keyColumns());
            addChildElement(chElement, "Type", it.type());
            setAnnotationList(chElement, it.annotations());
        }
    }

    private void setMeasureGroupDimensionBinding(SOAPElement element, MeasureGroupDimensionBinding it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, SOURCE);
            addChildElement(chElement, CUBE_DIMENSION_ID, it.cubeDimensionID());
        }
    }

    private void setMeasureGroupStorageMode(SOAPElement element, MeasureGroup.StorageMode it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, STORAGE_MODE);
            addChildElement(chElement, VALUENS, it.valuens());
            addChildElement(chElement, VALUE, it.value() == null ? null : it.value().value());
        }

    }

    private void setMeasureList(SOAPElement element, List<Measure> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, MEASURES);
            list.forEach(it -> setMeasure(chElement, it));
        }

    }

    private void setMeasure(SOAPElement element, Measure it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, MEASURE);

            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, DESCRIPTION, it.description());
            addChildElement(chElement, "AggregateFunction", it.aggregateFunction());
            addChildElement(chElement, "DataType", it.dataType());
            setDataItem(chElement, SOURCE, it.source());
            addChildElement(chElement, VISIBLE, String.valueOf(it.visible()));
            addChildElement(chElement, "MeasureExpression", it.measureExpression());
            addChildElement(chElement, DISPLAY_FOLDER, it.displayFolder());
            addChildElement(chElement, "FormatString", it.formatString());
            addChildElement(chElement, "BackColor", it.backColor());
            addChildElement(chElement, "ForeColor", it.foreColor());
            addChildElement(chElement, "FontName", it.fontName());
            addChildElement(chElement, "FontSize", it.fontSize());
            addChildElement(chElement, "FontFlags", it.fontFlags());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            setAnnotationList(chElement, it.annotations());
        }
    }

    private void setMdxScript(SOAPElement element, MdxScript it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "MdxScript");
            setCommandList(chElement, it.commands());
            addChildElement(chElement, "DefaultScript", String.valueOf(it.defaultScript()));
            setCalculationPropertyList(chElement, it.calculationProperties());
        }
    }

    private void setCalculationPropertyList(SOAPElement element, List<CalculationProperty> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "CalculationProperties");
            list.forEach(it -> setCalculationProperty(chElement, it));
        }
    }

    private void setCalculationProperty(SOAPElement element, CalculationProperty it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "CalculationProperty");
            addChildElement(chElement, "CalculationReference", it.calculationReference());
            addChildElement(chElement, "CalculationType", it.calculationType());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            addChildElement(chElement, DESCRIPTION, it.description());
            addChildElement(chElement, VISIBLE, String.valueOf(it.visible()));
            addChildElement(chElement, "SolveOrder", String.valueOf(it.solveOrder()));
            addChildElement(chElement, "FormatString", it.formatString());
            addChildElement(chElement, "ForeColor", it.foreColor());
            addChildElement(chElement, "BackColor", it.backColor());
            addChildElement(chElement, "FontName", it.fontName());
            addChildElement(chElement, "FontSize", it.fontSize());
            addChildElement(chElement, "FontFlags", it.fontFlags());
            addChildElement(chElement, "NonEmptyBehavior", it.nonEmptyBehavior());
            addChildElement(chElement, "AssociatedMeasureGroupID", it.associatedMeasureGroupID());
            addChildElement(chElement, DISPLAY_FOLDER, it.displayFolder());
            addChildElement(chElement, LANGUAGE, String.valueOf(it.language()));
            setCalculationPropertiesVisualizationProperties(chElement, it.visualizationProperties());
        }
    }

    private void setCalculationPropertiesVisualizationProperties(
        SOAPElement element,
        CalculationPropertiesVisualizationProperties it
    ) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, VISUALIZATION_PROPERTIES);
            addChildElement(chElement, "IsDefaultMeasure", String.valueOf(it.isDefaultMeasure()));
            addChildElement(chElement, "IsSimpleMeasure", String.valueOf(it.isSimpleMeasure()));
        }
    }

    private void setCommandList(SOAPElement element, List<Command> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Commands");
            list.forEach(it -> setCommand(chElement, it));
        }
    }

    private void setCommand(SOAPElement element, Command it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, COMMAND);
            if (it instanceof Alter a) {
                setAlter(chElement, a);
            }
            if (it instanceof Attach) {
                throw new UnsupportedOperationException("Attach operation doesn't supported");
            }
            if (it instanceof Backup) {
                throw new UnsupportedOperationException("Backup operation doesn't supported");
            }
            if (it instanceof Batch) {
                throw new UnsupportedOperationException("Batch operation doesn't supported");
            }
            if (it instanceof BeginTransaction) {
                throw new UnsupportedOperationException("BeginTransaction operation doesn't supported");
            }
            if (it instanceof Cancel c) {
                setCancel(chElement, c);
            }
            if (it instanceof ClearCache c) {
                setClearCache(chElement, c);
            }
            if (it instanceof CloneDatabase) {
                throw new UnsupportedOperationException("CloneDatabase operation doesn't supported");
            }
            if (it instanceof CommitTransaction) {
                throw new UnsupportedOperationException("CommitTransaction operation doesn't supported");
            }
            if (it instanceof Create) {
                throw new UnsupportedOperationException("Create operation doesn't supported");
            }
            if (it instanceof DBCC) {
                throw new UnsupportedOperationException("DBCC operation doesn't supported");
            }
            if (it instanceof Delete) {
                throw new UnsupportedOperationException("Delete operation doesn't supported");
            }
            if (it instanceof DesignAggregations) {
                throw new UnsupportedOperationException("DesignAggregations operation doesn't supported");
            }
            if (it instanceof Detach) {
                throw new UnsupportedOperationException("Detach operation doesn't supported");
            }
            if (it instanceof Drop) {
                throw new UnsupportedOperationException("Drop operation doesn't supported");
            }
            if (it instanceof ImageLoad) {
                throw new UnsupportedOperationException("ImageLoad operation doesn't supported");
            }
            if (it instanceof ImageSave) {
                throw new UnsupportedOperationException("ImageSave operation doesn't supported");
            }
            if (it instanceof Insert) {
                throw new UnsupportedOperationException("Insert operation doesn't supported");
            }
            if (it instanceof Lock) {
                throw new UnsupportedOperationException("Lock operation doesn't supported");
            }
            if (it instanceof MergePartitions) {
                throw new UnsupportedOperationException("MergePartitions operation doesn't supported");
            }
            if (it instanceof NotifyTableChange) {
                throw new UnsupportedOperationException("NotifyTableChange operation doesn't supported");
            }
            if (it instanceof Process) {
                throw new UnsupportedOperationException("Process operation doesn't supported");
            }
            if (it instanceof Restore) {
                throw new UnsupportedOperationException("Restore operation doesn't supported");
            }
            if (it instanceof RollbackTransaction) {
                throw new UnsupportedOperationException("RollbackTransaction operation doesn't supported");
            }
            if (it instanceof SetAuthContext) {
                throw new UnsupportedOperationException("SetAuthContext operation doesn't supported");
            }
            if (it instanceof Statement) {
                throw new UnsupportedOperationException("Statement operation doesn't supported");
            }
            if (it instanceof Subscribe) {
                throw new UnsupportedOperationException("Subscribe operation doesn't supported");
            }
            if (it instanceof Synchronize) {
                throw new UnsupportedOperationException("Synchronize operation doesn't supported");
            }
            if (it instanceof Unlock) {
                throw new UnsupportedOperationException("Unlock operation doesn't supported");
            }
            if (it instanceof Unsubscribe) {
                throw new UnsupportedOperationException("Unsubscribe operation doesn't supported");
            }
            if (it instanceof Update) {
                throw new UnsupportedOperationException("Update operation doesn't supported");
            }
            if (it instanceof UpdateCells) {
                throw new UnsupportedOperationException("UpdateCells operation doesn't supported");
            }
        }
    }

    private void setAlter(SOAPElement element, Alter a) {
        if (a != null) {
            SOAPElement chElement = addChildElement(element, "Alter");
            setObjectReference(chElement, a.object());
            setMajorObject(chElement, a.objectDefinition());
            setScope(chElement, a.scope());
            addChildElement(chElement, "AllowCreate", String.valueOf(a.allowCreate()));
            setObjectExpansion(chElement, a.objectExpansion());
        }
    }

    private void setClearCache(SOAPElement element, ClearCache it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "ClearCache");
            setObject(chElement, it.object());
        }
    }

    private void setObjectExpansion(SOAPElement element, ObjectExpansion it) {
        if (it != null) {
            addChildElement(element, "ObjectExpansion", it.value());
        }
    }

    private void setScope(SOAPElement element, Scope it) {
        if (it != null) {
            addChildElement(element, "Scope", it.value());
        }
    }

    private void setMajorObject(SOAPElement element, MajorObject it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "ObjectDefinition");
            setAggregationDesign(chElement, it.aggregationDesign());
            setAssembly(chElement, it.assembly());
            setCube(chElement, it.cube());
            setDatabase(chElement, it.database());
            setDataSource(chElement, it.dataSource());
            setDataSourceView(chElement, it.dataSourceView());
            setDimension(chElement, DIMENSION, it.dimension());
            setMdxScript(chElement, it.mdxScript());
            setMeasureGroup(chElement, it.measureGroup());
            setMiningModel(chElement, it.miningModel());
            setMiningStructure(chElement, it.miningStructure());
            setPartition(chElement, it.partition());
            setPermission(chElement, "Permission", it.permission());
            setPerspective(chElement, it.perspective());
            setRole(chElement, it.role());
            setServer(chElement, it.server());
            setTrace(chElement, it.trace());
        }
    }

    private void setObjectReference(SOAPElement element, ObjectReference it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Object");
            addChildElement(chElement, SERVER_ID, it.serverID());
            addChildElement(chElement, "DatabaseID", it.databaseID());
            addChildElement(chElement, ROLE_ID, it.roleID());
            addChildElement(chElement, "TraceID", it.traceID());
            addChildElement(chElement, "AssemblyID", it.assemblyID());
            addChildElement(chElement, DIMENSION_ID, it.dimensionID());
            addChildElement(chElement, "DimensionPermissionID", it.dimensionPermissionID());
            addChildElement(chElement, DATA_SOURCE_ID, it.dataSourceID());
            addChildElement(chElement, "DataSourcePermissionID", it.dataSourcePermissionID());
            addChildElement(chElement, "DatabasePermissionID", it.databasePermissionID());
            addChildElement(chElement, DATA_SOURCE_VIEW_ID, it.dataSourceViewID());
            addChildElement(chElement, CUBE_ID, it.cubeID());
            addChildElement(chElement, "MiningStructureID", it.miningStructureID());
            addChildElement(chElement, MEASURE_GROUP_ID, it.measureGroupID());
            addChildElement(chElement, "PerspectiveID", it.perspectiveID());
            addChildElement(chElement, "CubePermissionID", it.cubePermissionID());
            addChildElement(chElement, "MdxScriptID", it.mdxScriptID());
            addChildElement(chElement, "PartitionID", it.partitionID());
            addChildElement(chElement, AGGREGATION_DESIGN_ID, it.aggregationDesignID());
            addChildElement(chElement, MINING_MODEL_ID, it.miningModelID());
            addChildElement(chElement, "MiningModelPermissionID", it.miningModelPermissionID());
            addChildElement(chElement, "MiningStructurePermissionID", it.miningStructurePermissionID());
        }
    }

    private void setDimension(SOAPElement element, String tagName,  Dimension it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, tagName);
            setBinding(chElement, it.source());
            addChildElement(chElement, MINING_MODEL_ID, it.miningModelID());
            addChildElement(chElement, "Type", it.type());
            setDimensionUnknownMember(chElement, it.unknownMember());
            addChildElement(chElement, "MdxMissingMemberMode", it.mdxMissingMemberMode());
            setErrorConfiguration(chElement, it.errorConfiguration());
            addChildElement(chElement, STORAGE_MODE, it.storageMode());
            addChildElement(chElement, "WriteEnabled", String.valueOf(it.writeEnabled()));
            addChildElement(chElement, PROCESSING_PRIORITY, String.valueOf(it.processingPriority()));
            addChildElement(chElement, LAST_PROCESSED, convertInstant(it.lastProcessed()));
            setDimensionPermissionList(chElement, it.dimensionPermissions());
            addChildElement(chElement, "DependsOnDimensionID", it.dependsOnDimensionID());
            addChildElement(chElement, LANGUAGE, String.valueOf(it.language()));
            addChildElement(chElement, COLLATION, it.collation());
            addChildElement(chElement, "UnknownMemberName", it.unknownMemberName());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            addChildElement(chElement, STATE, it.state());
            setProactiveCaching(chElement, it.proactiveCaching());
            addChildElement(chElement, PROCESSING_MODE, it.processingMode());
            addChildElement(chElement, "ProcessingGroup", it.processingGroup());
            setDimensionCurrentStorageMode(chElement, it.currentStorageMode());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            setDimensionAttributeList(chElement, it.attributes());
            addChildElement(chElement, "AttributeAllMemberName", it.attributeAllMemberName());
            setTranslationList(chElement, "AttributeAllMemberTranslations", "AttributeAllMemberTranslation",
                it.translations());
            setHierarchyList(chElement, it.hierarchies());
            addChildElement(chElement, "ProcessingRecommendation", it.processingRecommendation());
            setRelationships(chElement, it.relationships());
            addChildElement(chElement, "StringStoresCompatibilityLevel",
                String.valueOf(it.stringStoresCompatibilityLevel()));
            addChildElement(chElement, "CurrentStringStoresCompatibilityLevel",
                String.valueOf(it.currentStringStoresCompatibilityLevel()));
        }
    }

    private void setRelationships(SOAPElement element, Relationships it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Relationships");
            setRelationshipList(chElement, it.relationship());
        }
    }

    private void setRelationshipList(SOAPElement element, List<Relationship> list) {
        if (list != null) {
            list.forEach(it -> setRelationship(element, it));
        }
    }

    private void setRelationship(SOAPElement element, Relationship it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Relationship");
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, VISIBLE, String.valueOf(it.visible()));
            setRelationshipEnd(chElement, "FromRelationshipEnd", it.fromRelationshipEnd());
            setRelationshipEnd(chElement, "ToRelationshipEnd", it.toRelationshipEnd());
        }
    }

    private void setRelationshipEnd(SOAPElement element, String nodeName, RelationshipEnd it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, nodeName);
            addChildElement(chElement, "Role", it.role());
            addChildElement(chElement, "Multiplicity", it.multiplicity());
            addChildElement(chElement, DIMENSION_ID, it.dimensionID());
            setRelationshipEndAttributes(chElement, it.attributes());
            setRelationshipEndTranslations(chElement, it.translations());
            setRelationshipEndVisualizationProperties(chElement, it.visualizationProperties());
        }
    }

    private void setRelationshipEndVisualizationProperties(
        SOAPElement element,
        RelationshipEndVisualizationProperties it
    ) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, VISUALIZATION_PROPERTIES);
            addChildElement(chElement, "FolderPosition", String.valueOf(it.folderPosition()));
            addChildElement(chElement, "ContextualNameRule", it.contextualNameRule());
            addChildElement(chElement, "DefaultDetailsPosition", String.valueOf(it.defaultDetailsPosition()));
            addChildElement(chElement, "DisplayKeyPosition", String.valueOf(it.displayKeyPosition()));
            addChildElement(chElement, "CommonIdentifierPosition", String.valueOf(it.commonIdentifierPosition()));
            addChildElement(chElement, "IsDefaultMeasure", String.valueOf(it.isDefaultMeasure()));
            addChildElement(chElement, "IsDefaultImage", String.valueOf(it.isDefaultImage()));
            addChildElement(chElement, "SortPropertiesPosition", String.valueOf(it.sortPropertiesPosition()));
        }
    }

    private void setRelationshipEndTranslations(SOAPElement element, List<RelationshipEndTranslation> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, TRANSLATIONS);
            list.forEach(it -> setRelationshipEndTranslation(chElement, it));
        }
    }

    private void setRelationshipEndTranslation(SOAPElement element, RelationshipEndTranslation it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, TRANSLATION);
            addChildElement(chElement, LANGUAGE, String.valueOf(it.language()));
            addChildElement(chElement, CAPTION, it.caption());
            addChildElement(chElement, DESCRIPTION, it.description());
            addChildElement(chElement, DISPLAY_FOLDER, it.displayFolder());
            setAnnotationList(chElement, it.annotations());
            addChildElement(chElement, "CollectionCaption", it.collectionCaption());
        }
    }

    private void setRelationshipEndAttributes(SOAPElement element, List<String> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTES);
            list.forEach(it -> setRelationshipEndAttribute(chElement, it));
        }
    }

    private void setRelationshipEndAttribute(SOAPElement element, String it) {
        if (it != null) {
            addChildElement(addChildElement(element, ATTRIBUTE), ATTRIBUTE_ID, it);
        }
    }

    private void setHierarchyList(SOAPElement element, List<Hierarchy> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, HIERARCHIES);
            list.forEach(it -> setHierarchy(chElement, it));
        }
    }

    private void setHierarchy(SOAPElement element, Hierarchy it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, HIERARCHY);
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, DESCRIPTION, it.description());
            addChildElement(chElement, PROCESSING_STATE, it.processingState());
            addChildElement(chElement, "StructureType", it.structureType());
            addChildElement(chElement, DISPLAY_FOLDER, it.displayFolder());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            addChildElement(chElement, "AllMemberName", it.allMemberName());
            setAnnotationList(chElement, it.annotations());
            setTranslationList(chElement, "AllMemberTranslations", "AllMemberTranslations", it.allMemberTranslations());
            addChildElement(chElement, "MemberNamesUnique", String.valueOf(it.memberNamesUnique()));
            addChildElement(chElement, "MemberKeysUnique", String.valueOf(it.memberKeysUnique()));
            addChildElement(chElement, "AllowDuplicateNames", String.valueOf(it.allowDuplicateNames()));
            setLevelList(chElement, it.levels());
            setAnnotationList(chElement, it.annotations());
            setHierarchyVisualizationProperties(chElement, it.visualizationProperties());
        }
    }

    private void setHierarchyVisualizationProperties(SOAPElement element, HierarchyVisualizationProperties it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, VISUALIZATION_PROPERTIES);
            addChildElement(chElement, "ContextualNameRule", it.contextualNameRule());
            addChildElement(chElement, "FolderPosition", String.valueOf(it.folderPosition()));
        }
    }

    private void setLevelList(SOAPElement element, List<Level> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Levels");
            list.forEach(it -> setLevel(chElement, it));
        }
    }

    private void setLevel(SOAPElement element, Level it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Level");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, DESCRIPTION, it.description());
            addChildElement(chElement, "SourceAttributeID", it.sourceAttributeID());
            addChildElement(chElement, "HideMemberIf", it.hideMemberIf());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            setAnnotationList(chElement, it.annotations());
        }
    }

    private void setDimensionAttributeList(SOAPElement element, List<DimensionAttribute> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTES);
            list.forEach(it -> setDimensionAttribute(chElement, it));
        }
    }

    private void setDimensionAttribute(SOAPElement element, DimensionAttribute it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTE);
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, DESCRIPTION, it.description());
            setDimensionAttributeType(chElement, it.type());
            addChildElement(chElement, "Usage", it.usage());
            setBinding(chElement, it.source());
            addChildElement(chElement, "EstimatedCount", String.valueOf(it.estimatedCount()));
            setDataItemList(chElement, KEY_COLUMNS, KEY_COLUMN, it.keyColumns());
            setDataItem(chElement, "NameColumn", it.nameColumn());
            setDataItem(chElement, "ValueColumn", it.valueColumn());
            setAttributeTranslationList(chElement, it.translations());
            setAttributeRelationshipList(chElement, it.attributeRelationships());
            addChildElement(chElement, "DiscretizationMethod", it.discretizationMethod());
            addChildElement(chElement, "DiscretizationBucketCount", String.valueOf(it.discretizationBucketCount()));
            addChildElement(chElement, "RootMemberIf", it.rootMemberIf());
            addChildElement(chElement, "OrderBy", it.orderBy());
            addChildElement(chElement, DEFAULT_MEMBER, it.defaultMember());
            addChildElement(chElement, "OrderByAttributeID", it.orderByAttributeID());
            setDataItem(chElement, "SkippedLevelsColumn", it.skippedLevelsColumn());
            addChildElement(chElement, "NamingTemplate", it.namingTemplate());
            addChildElement(chElement, "MembersWithData", it.membersWithData());
            addChildElement(chElement, "MembersWithDataCaption", it.membersWithDataCaption());
            setTranslationList(chElement, "NamingTemplateTranslations", "NamingTemplateTranslation",
                it.namingTemplateTranslations());
            setDataItem(chElement, "CustomRollupColumn", it.customRollupColumn());
            setDataItem(chElement, "CustomRollupPropertiesColumn", it.customRollupPropertiesColumn());
            setDataItem(chElement, "UnaryOperatorColumn", it.unaryOperatorColumn());
            addChildElement(chElement, "AttributeHierarchyOrdered", String.valueOf(it.attributeHierarchyOrdered()));
            addChildElement(chElement, "MemberNamesUnique", String.valueOf(it.memberNamesUnique()));
            addChildElement(chElement, "IsAggregatable", String.valueOf(it.isAggregatable()));
            addChildElement(chElement, "AttributeHierarchyEnabled", String.valueOf(it.attributeHierarchyEnabled()));
            addChildElement(chElement, "AttributeHierarchyOptimizedState", it.attributeHierarchyOptimizedState());
            addChildElement(chElement, ATTRIBUTE_HIERARCHY_VISIBLE, String.valueOf(it.attributeHierarchyVisible()));
            addChildElement(chElement, "AttributeHierarchyDisplayFolder", it.attributeHierarchyDisplayFolder());
            addChildElement(chElement, "KeyUniquenessGuarantee", String.valueOf(it.keyUniquenessGuarantee()));
            addChildElement(chElement, "GroupingBehavior", it.groupingBehavior());
            addChildElement(chElement, "InstanceSelection", it.instanceSelection());
            setAnnotationList(chElement, it.annotations());
            addChildElement(chElement, PROCESSING_STATE, it.processingState());
            addChildElement(chElement, "AttributeHierarchyProcessingState",
             it.attributeHierarchyProcessingState() == null ? null : it.attributeHierarchyProcessingState().value());
            addChildElement(chElement, "ExtendedType", it.extendedType());
        }
    }

    private void setAttributeRelationshipList(SOAPElement element, List<AttributeRelationship> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "AttributeRelationships");
            list.forEach(it -> setAttributeRelationship(chElement, it));
        }

    }

    private void setAttributeRelationship(SOAPElement element, AttributeRelationship it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "AttributeRelationship");
            addChildElement(chElement, ATTRIBUTE_ID, it.attributeID());
            addChildElement(chElement, "RelationshipType", it.relationshipType());
            addChildElement(chElement, "Cardinality", it.cardinality());
            addChildElement(chElement, "Optionality", it.optionality());
            addChildElement(chElement, "OverrideBehavior", it.overrideBehavior());
            setAnnotationList(chElement, it.annotations());
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, VISIBLE, String.valueOf(it.visible()));
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
        }
    }

    private void setDimensionAttributeType(SOAPElement element, DimensionAttribute.Type it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Type");
            addChildElement(chElement, VALUENS, it.valuens());
            addChildElement(chElement, VALUE, it.value() == null ? null : it.value().value());
        }
    }

    private void setDimensionCurrentStorageMode(SOAPElement element, Dimension.CurrentStorageMode it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "CurrentStorageMode");
            addChildElement(chElement, VALUENS, it.valuens());
            addChildElement(chElement, VALUE, it.value() == null ? null : it.value().value());
        }
    }

    private void setDimensionPermissionList(SOAPElement element, List<DimensionPermission> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "DimensionPermissions");
            list.forEach(it -> setDimensionPermission(chElement, it));
        }

    }

    private void setDimensionPermission(SOAPElement element, DimensionPermission it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "DimensionPermission");
            addChildElement(chElement, ROLE_ID, it.roleID());
            it.process().ifPresent(v -> addChildElement(chElement, PROCESS, String.valueOf(v)));
            it.readDefinition().ifPresent(v -> addChildElement(chElement, READ_DEFINITION, v.getValue()));
            it.read().ifPresent(v -> addChildElement(chElement, "Read", v.getValue()));
            it.write().ifPresent(v -> addChildElement(chElement, WRITE, v.getValue()));

            addChildElement(chElement, "Name", it.name());
            it.id().ifPresent(v -> addChildElement(chElement, "ID", v));
            it.createdTimestamp().ifPresent(v -> addChildElement(chElement, CREATED_TIMESTAMP, convertInstant(v)));
            it.lastSchemaUpdate().ifPresent(v -> addChildElement(chElement, LAST_SCHEMA_UPDATE, convertInstant(v)));
            it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
            it.attributePermissions().ifPresent(v -> setAttributePermissionList(chElement, v));

            it.allowedRowsExpression().ifPresent(v -> addChildElement(chElement, "AllowedRowsExpression", v));
        }
    }

    private void setDimensionUnknownMember(SOAPElement element, Dimension.UnknownMember it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "UnknownMember");
            addChildElement(chElement, VALUENS, it.valuens());
            addChildElement(chElement, VALUE, it.value() == null ? null : it.value().value());
        }
    }

    private void setDataSourceView(SOAPElement element, DataSourceView it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "DataSourceView");
            addChildElement(chElement, DATA_SOURCE_ID, it.dataSourceID());

            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, CREATED_TIMESTAMP, convertInstant(it.createdTimestamp()));
            addChildElement(chElement, LAST_SCHEMA_UPDATE, convertInstant(it.lastSchemaUpdate()));
            addChildElement(chElement, DESCRIPTION, it.description());
            setAnnotationList(chElement, it.annotations());
        }
    }


    private void setDataSource(SOAPElement element, DataSource it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "DataSource");
            addChildElement(chElement, "ManagedProvider", it.managedProvider());
            addChildElement(chElement, "ConnectionString", it.connectionString());
            addChildElement(chElement, "ConnectionStringSecurity", it.connectionStringSecurity());
            setImpersonationInfo(chElement, "ImpersonationInfo", it.impersonationInfo());
            addChildElement(chElement, "Isolation", it.isolation());
            addChildElement(chElement, "MaxActiveConnections", String.valueOf(it.maxActiveConnections()));
            addChildElement(chElement, "Timeout", convertDuration(it.timeout()));
            setDataSourcePermissionList(chElement, it.dataSourcePermissions());
            setImpersonationInfo(chElement, "QueryImpersonationInfo", it.queryImpersonationInfo());
            addChildElement(chElement, "QueryHints", it.queryHints());
        }
    }

    private void setDataSourcePermissionList(SOAPElement element, List<DataSourcePermission> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "DataSourcePermissions");
            list.forEach(it -> setDataSourcePermission(chElement, it));
        }
    }

    private void setDataSourcePermission(SOAPElement element, DataSourcePermission it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "DataSourcePermission");
            addChildElement(chElement, ROLE_ID, it.roleID());
            it.process().ifPresent(v -> addChildElement(chElement, PROCESS, String.valueOf(v)));
            it.readDefinition().ifPresent(v -> addChildElement(chElement, READ_DEFINITION, v.getValue()));
            it.read().ifPresent(v -> addChildElement(chElement, "Read", v.getValue()));
            it.write().ifPresent(v -> addChildElement(chElement, WRITE, v.getValue()));
        }
    }

    private void setImpersonationInfo(SOAPElement element, String tagName, ImpersonationInfo it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, tagName);
            addChildElement(chElement, "ImpersonationMode", it.impersonationMode());
            it.account().ifPresent(v -> addChildElement(chElement, ACCOUNT, v));
            addChildElement(chElement, "ImpersonationMode", it.impersonationMode());
            it.account().ifPresent(v -> addChildElement(chElement, ACCOUNT, v));
            it.password().ifPresent(v -> addChildElement(chElement, "Password", v));
            it.impersonationInfoSecurity().ifPresent(v -> addChildElement(chElement, "ImpersonationInfoSecurity", v));
        }
    }

    private void setDatabase(SOAPElement element, Database it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Database");
            addChildElement(chElement, "LastUpdate", convertInstant(it.lastUpdate()));
            addChildElement(chElement, STATE, it.state());
            addChildElement(chElement, "ReadWriteMode", it.readWriteMode());
            addChildElement(chElement, "DbStorageLocation", it.dbStorageLocation());
            addChildElement(chElement, AGGREGATION_PREFIX, it.aggregationPrefix());
            addChildElement(chElement, PROCESSING_PRIORITY, String.valueOf(it.processingPriority()));
            addChildElement(chElement, ESTIMATED_SIZE, String.valueOf(it.estimatedSize()));
            addChildElement(chElement, LAST_PROCESSED, convertInstant(it.lastProcessed()));
            addChildElement(chElement, LANGUAGE, String.valueOf(it.language()));
            addChildElement(chElement, COLLATION, String.valueOf(it.collation()));
            addChildElement(chElement, VISIBLE, String.valueOf(it.visible()));
            addChildElement(chElement, "MasterDataSourceID", it.masterDataSourceID());
            setImpersonationInfo(chElement, "DataSourceImpersonationInfo", it.dataSourceImpersonationInfo());
            setAccountList(chElement, it.accounts());
            setDataSourceList(chElement, it.dataSources());
            setDataSourceViewList(chElement, it.dataSourceViews());
            setDimensionList(chElement, it.dimensions());
            setCubeList(chElement, it.cubes());
            setMiningStructureList(chElement, it.dimensions());
            setRoleList(chElement, it.roles());
            setAssemblyList(chElement, it.assemblies());
            setDatabasePermissionList(chElement, it.databasePermissions());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            addChildElement(chElement, "StorageEngineUsed", it.storageEngineUsed());
            addChildElement(chElement, "ImagePath", it.imagePath());
            addChildElement(chElement, "ImageUrl", it.imageUrl());
            addChildElement(chElement, "ImageUniqueID", it.imageUniqueID());
            addChildElement(chElement, "ImageVersion", it.imageVersion());
            addChildElement(chElement, "Token", it.token());
            addChildElement(chElement, "CompatibilityLevel", String.valueOf(it.compatibilityLevel()));
            addChildElement(chElement, "DirectQueryMode", it.directQueryMode());
        }
    }

    private void setDatabasePermissionList(SOAPElement element, List<DatabasePermission> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "DatabasePermissions");
            list.forEach(it -> setPermission(chElement, "DatabasePermission", it));
        }
    }

    private void setMiningStructureList(SOAPElement element, List<Dimension> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "MiningStructures");
            list.forEach(it -> setDimension(chElement, "MiningStructure", it));
        }
    }

    private void setCubeList(SOAPElement element, List<Cube> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Cubes");
            list.forEach(it -> setCube(chElement, it));
        }
    }

    private void setDimensionList(SOAPElement element, List<Dimension> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, DIMENSIONS);
            list.forEach(it -> setDimension(chElement, DIMENSION, it));
        }
    }

    private void setDataSourceViewList(SOAPElement element, List<DataSourceView> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "DataSourceViews");
            list.forEach(it -> setDataSourceView(chElement, it));
        }
    }

    private void setDataSourceList(SOAPElement element, List<DataSource> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "DataSources");
            list.forEach(it -> setDataSource(chElement, it));
        }
    }

    private void setAccountList(SOAPElement element, List<Account> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Accounts");
            list.forEach(it -> setAccount(chElement, it));
        }
    }

    private void setAccount(SOAPElement element, Account it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, ACCOUNT);
            addChildElement(chElement, "AccountType", it.accountType());
            addChildElement(chElement, "AggregationFunction", it.aggregationFunction());
            setAliasList(chElement, it.aliases());
            setAnnotationList(chElement, it.annotations());
        }
    }

    private void setAliasList(SOAPElement element, List<String> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Aliases");
            list.forEach(it -> addChildElement(chElement, "Alias", it));
        }
    }

    private void setCube(SOAPElement element, Cube cube) {
        if (cube != null) {
            SOAPElement chElement = addChildElement(element, "Cube");
            addChildElement(chElement, LANGUAGE, String.valueOf(cube.language()));
            addChildElement(chElement, COLLATION, String.valueOf(cube.collation()));
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, cube.translations());
            setCubeDimensionsList(chElement, cube.dimensions());
            setCubePermissionList(chElement, cube.cubePermissions());
            setMdxScriptList(chElement, cube.mdxScripts());
            setPerspectiveList(chElement, cube.perspectives());
            addChildElement(chElement, STATE, cube.state());
            addChildElement(chElement, "DefaultMeasure", cube.defaultMeasure());
            addChildElement(chElement, VISIBLE, String.valueOf(cube.visible()));
            setMeasureGroupList(chElement, cube.measureGroups());
            setDataSourceViewBinding(chElement, cube.source());
            addChildElement(chElement, AGGREGATION_PREFIX, cube.aggregationPrefix());
            addChildElement(chElement, PROCESSING_PRIORITY, String.valueOf(cube.processingPriority()));
            setCubeStorageMode(chElement, cube.storageMode());
            addChildElement(chElement, PROCESSING_MODE, cube.processingMode());
            addChildElement(chElement, "ScriptCacheProcessingMode", cube.scriptCacheProcessingMode());
            addChildElement(chElement, "ScriptErrorHandlingMode", cube.scriptErrorHandlingMode());
            addChildElement(chElement, "DaxOptimizationMode", cube.daxOptimizationMode());
            setProactiveCaching(chElement, cube.proactiveCaching());
            setKpiList(chElement, cube.kpis());
            setErrorConfiguration(chElement, cube.errorConfiguration());
            setActionList(chElement, cube.actions());
            addChildElement(chElement, STORAGE_LOCATION, cube.storageLocation());
            addChildElement(chElement, ESTIMATED_ROWS, String.valueOf(cube.estimatedRows()));
            addChildElement(chElement, LAST_PROCESSED, String.valueOf(cube.lastProcessed()));
        }
    }

    private void setActionList(SOAPElement element, List<Action> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Actions");
            list.forEach(it -> setAction(chElement, it));
        }
    }

    private void setAction(SOAPElement element, Action it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Action");
            addChildElement(chElement, "Name", it.name());
            it.id().ifPresent(v -> addChildElement(chElement, "ID", v));
            it.caption().ifPresent(v -> addChildElement(chElement, CAPTION, v));
            it.captionIsMdx().ifPresent(v -> addChildElement(chElement, "CaptionIsMdx", String.valueOf(v)));
            it.translations().ifPresent(v -> setTranslationList(chElement, TRANSLATIONS, TRANSLATION, v));
            addChildElement(chElement, "TargetType", it.targetType().getValue());
            it.target().ifPresent(v -> addChildElement(chElement, "Target", v));
            it.condition().ifPresent(v -> addChildElement(chElement, "Condition", v));
            addChildElement(chElement, "Type", it.type().getValue());
            it.invocation().ifPresent(v -> addChildElement(chElement, "Invocation", v));
            it.application().ifPresent(v -> addChildElement(chElement, "Application", v));
            it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));

            if (it instanceof DrillThroughAction dta) {
                dta.defaultAction().ifPresent(v -> addChildElement(chElement, "Default", String.valueOf(v)));
                dta.columns().ifPresent(v -> setBindingList(chElement, v));
                dta.maximumRows().ifPresent(v -> addChildElement(chElement, "MaximumRows", String.valueOf(v)));
            }
            if (it instanceof ReportAction ra) {
                addChildElement(chElement, "ReportServer", ra.reportServer());
                ra.path().ifPresent(v -> addChildElement(chElement, "Path", v));
                ra.reportParameters().ifPresent(v -> setReportParameterList(chElement, v));
                ra.reportFormatParameters().ifPresent(v -> setReportFormatParameterList(chElement, v));
            }
            if (it instanceof StandardAction sa) {
                addChildElement(chElement, EXPRESSION, sa.expression());
            }
        }
    }

    private void setReportFormatParameterList(SOAPElement element, List<ReportFormatParameter> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "ReportFormatParameters");
            list.forEach(it -> setReportFormatParameter(chElement, it));
        }
    }

    private void setReportFormatParameter(SOAPElement element, ReportFormatParameter it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "ReportFormatParameter");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, VALUE2, it.value());
        }
    }

    private void setReportParameterList(SOAPElement element, List<ReportParameter> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "ReportParameters");
            list.forEach(it -> setReportParameter(chElement, it));
        }
    }

    private void setReportParameter(SOAPElement element, ReportParameter it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "ReportParameter");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, VALUE2, it.value());
        }
    }

    private void setBindingList(SOAPElement element, List<Binding> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, COLUMNS);
            list.forEach(it -> setBinding(chElement, it));
        }
    }

    private void setBinding(SOAPElement element, Binding it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, SOURCE);
            if (it instanceof ColumnBinding cb) {
                addChildElement(chElement, TABLE_ID, cb.tableID());
                addChildElement(chElement, COLUMN_ID, cb.columnID());
            }
            if (it instanceof RowBinding rb) {
                addChildElement(chElement, TABLE_ID, rb.tableID());
            }
            if (it instanceof DataSourceViewBinding dsvb) {
                addChildElement(chElement, DATA_SOURCE_VIEW_ID, dsvb.dataSourceViewID());
            }
            if (it instanceof AttributeBinding ab) {
                addChildElement(chElement, ATTRIBUTE_ID, ab.attributeID());
                addChildElement(chElement, "Type", ab.type() == null ? null : ab.type().getValue());
                addChildElement(chElement, ORDINAL, String.valueOf(ab.ordinal()));
            }
            if (it instanceof UserDefinedGroupBinding udgb) {
                addChildElement(chElement, ATTRIBUTE_ID, udgb.attributeID());
                udgb.groups().ifPresent(v -> setGroupList(chElement, v));
            }
            if (it instanceof MeasureBinding mb) {
                addChildElement(chElement, MEASURE_ID, mb.measureID());
            }
            if (it instanceof CubeAttributeBinding cab) {
                addChildElement(chElement, CUBE_ID, cab.cubeID());
                addChildElement(chElement, CUBE_DIMENSION_ID, cab.cubeDimensionID());
                addChildElement(chElement, ATTRIBUTE_ID, cab.attributeID());
                addChildElement(chElement, "Type", cab.type() == null ? null : cab.type().getValue());
                cab.ordinal().ifPresent(v -> setCubeAttributeBindingOrdinalList(chElement, v));
            }
            if (it instanceof DimensionBinding db) {
                addChildElement(chElement, DATA_SOURCE_ID, db.dataSourceID());
                addChildElement(chElement, DIMENSION_ID, db.dimensionID());
                db.persistence().ifPresent(v -> addChildElement(chElement, PERSISTENCE, v.getValue()));
                db.refreshPolicy().ifPresent(v -> addChildElement(chElement, REFRESH_POLICY, v.getValue()));
                db.refreshInterval().ifPresent(v -> addChildElement(chElement, REFRESH_INTERVAL, String.valueOf(v)));
            }
            if (it instanceof CubeDimensionBinding cdb) {
                addChildElement(chElement, DATA_SOURCE_ID, cdb.dataSourceID());
                addChildElement(chElement, CUBE_ID, cdb.cubeID());
                addChildElement(chElement, CUBE_DIMENSION_ID, cdb.cubeDimensionID());
                cdb.filter().ifPresent(v -> addChildElement(chElement, FILTER, v));
            }
            if (it instanceof MeasureGroupBinding mgb) {
                addChildElement(chElement, DATA_SOURCE_ID, mgb.dataSourceID());
                addChildElement(chElement, CUBE_ID, mgb.cubeID());
                addChildElement(chElement, MEASURE_GROUP_ID, mgb.measureGroupID());
                mgb.persistence().ifPresent(v -> addChildElement(chElement, PERSISTENCE, v.getValue()));
                mgb.refreshPolicy().ifPresent(v -> addChildElement(chElement, REFRESH_POLICY, v.getValue()));
                mgb.refreshInterval().ifPresent(v -> addChildElement(chElement, REFRESH_INTERVAL, convertDuration(v)));
                mgb.filter().ifPresent(v -> addChildElement(chElement, FILTER, v));
            }
            if (it instanceof MeasureGroupDimensionBinding mgdb) {
                addChildElement(chElement, CUBE_DIMENSION_ID, mgdb.cubeDimensionID());
            }
            if (it instanceof TimeBinding tb) {
                addChildElement(chElement, "CalendarStartDate", convertInstant(tb.calendarStartDate()));
                addChildElement(chElement, "CalendarEndDate", convertInstant(tb.calendarEndDate()));
                tb.firstDayOfWeek().ifPresent(v -> addChildElement(chElement, "FirstDayOfWeek", String.valueOf(v)));
                tb.calendarLanguage().ifPresent(v -> addChildElement(chElement, "CalendarLanguage", String.valueOf(v)));
                tb.fiscalFirstMonth().ifPresent(v -> addChildElement(chElement, "FiscalFirstMonth", String.valueOf(v)));
                tb.fiscalFirstDayOfMonth().ifPresent(v -> addChildElement(chElement, "FiscalFirstDayOfMonth",
                 String.valueOf(v)));
                tb.fiscalYearName().ifPresent(v -> addChildElement(chElement, "FiscalYearName", v.getValue()));
                tb.reportingFirstMonth().ifPresent(v -> addChildElement(chElement, "ReportingFirstMonth",
                 String.valueOf(v)));
                tb.reportingFirstWeekOfMonth().ifPresent(v -> addChildElement(chElement, "ReportingFirstWeekOfMonth",
                    v));
                tb.reportingWeekToMonthPattern().ifPresent(v -> addChildElement(chElement,
                    "ReportingWeekToMonthPattern", v.getValue()));
                tb.manufacturingFirstMonth().ifPresent(v -> addChildElement(chElement, "ManufacturingFirstMonth",
                 String.valueOf(v)));
                tb.manufacturingFirstWeekOfMonth().ifPresent(v -> addChildElement(chElement,
                "ManufacturingFirstWeekOfMonth", String.valueOf(v)));
                tb.manufacturingExtraMonthQuarter().ifPresent(v -> addChildElement(chElement,
                "ManufacturingExtraMonthQuarter", String.valueOf(v)));
            }
            if (it instanceof TimeAttributeBinding) {
            }
            if (it instanceof InheritedBinding) {
            }
            if (it instanceof CalculatedMeasureBinding cmb) {
                addChildElement(chElement, "MeasureName", cmb.measureName());
            }
            if (it instanceof RowNumberBinding) {
            }
            if (it instanceof ExpressionBinding eb) {
                addChildElement(chElement, EXPRESSION, eb.expression());
            }
        }
    }

    private String convertInstant(Instant instant) {
        if (instant != null) {
            return instant.toString();
        }
        return null;
    }

    private void setCubeAttributeBindingOrdinalList(SOAPElement element, List<BigInteger> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ORDINAL);
            list.forEach(it -> setCubeAttributeBindingOrdinal(chElement, it));
        }
    }

    private void setCubeAttributeBindingOrdinal(SOAPElement element, BigInteger it) {
        if (it != null) {
            addChildElement(element, ORDINAL, String.valueOf(it));
        }
    }

    private void setGroupList(SOAPElement element, List<Group> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Groups");
            list.forEach(it -> setGroup(chElement, it));
        }
    }

    private void setGroup(SOAPElement element, Group it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Group");
            addChildElement(chElement, "Name", it.name());
            SOAPElement membersElement = addChildElement(chElement, "Members");
            it.members().ifPresent(v -> v.forEach(m -> addChildElement(membersElement, "Member", m)));
        }
    }

    private void setErrorConfiguration(SOAPElement element, ErrorConfiguration it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "ErrorConfiguration");
            it.keyErrorLimit().ifPresent(v -> addChildElement(chElement, "KeyErrorLimit", String.valueOf(v)));
            it.keyErrorLogFile().ifPresent(v -> addChildElement(chElement, "KeyErrorLogFile", v));
            it.keyErrorAction().ifPresent(v -> addChildElement(chElement, "KeyErrorAction", v));
            it.keyErrorLimitAction().ifPresent(v -> addChildElement(chElement, "KeyErrorLimitAction", v));
            it.keyNotFound().ifPresent(v -> addChildElement(chElement, "KeyNotFound", v));
            it.keyDuplicate().ifPresent(v -> addChildElement(chElement, "KeyDuplicate", v));
            it.nullKeyConvertedToUnknown().ifPresent(v -> addChildElement(chElement, "NullKeyConvertedToUnknown", v));
            it.nullKeyNotAllowed().ifPresent(v -> addChildElement(chElement, "NullKeyNotAllowed", v));
            it.calculationError().ifPresent(v -> addChildElement(chElement, "CalculationError", v));
        }
    }

    private void setKpiList(SOAPElement element, List<Kpi> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Kpis");
            list.forEach(it -> setKpi(chElement, it));
        }
    }

    private void setKpi(SOAPElement element, Kpi it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Kpi");
            addChildElement(chElement, "Name", it.name());
            addChildElement(chElement, "ID", it.id());
            addChildElement(chElement, DESCRIPTION, it.description());
            setTranslationList(chElement, TRANSLATIONS, TRANSLATION, it.translations());
            addChildElement(chElement, DISPLAY_FOLDER, it.displayFolder());
            addChildElement(chElement, "AssociatedMeasureGroupID", it.associatedMeasureGroupID());
            addChildElement(chElement, VALUE2, it.value());
            addChildElement(chElement, "Goal", it.goal());
            addChildElement(chElement, "Status", it.status());
            addChildElement(chElement, "Trend", it.trend());
            addChildElement(chElement, "Weight", it.weight());
            addChildElement(chElement, "TrendGraphic", it.trendGraphic());
            addChildElement(chElement, "StatusGraphic", it.statusGraphic());
            addChildElement(chElement, "CurrentTimeMember", it.currentTimeMember());
            addChildElement(chElement, "ParentKpiID", it.parentKpiID());
            setAnnotationList(chElement, it.annotations());
        }
    }

    private void setProactiveCaching(SOAPElement element, ProactiveCaching proactiveCaching) {
        if (proactiveCaching != null) {
            SOAPElement chElement = addChildElement(element, "ProactiveCaching");
            proactiveCaching.onlineMode().ifPresent(v -> addChildElement(chElement, "OnlineMode", v));
            proactiveCaching.aggregationStorage().ifPresent(v -> addChildElement(chElement, "AggregationStorage", v));
            proactiveCaching.source().ifPresent(v -> setProactiveCachingBinding(chElement, v));
            proactiveCaching.silenceInterval().ifPresent(v -> addChildElement(chElement, "SilenceInterval",
             convertDuration(v)));
            proactiveCaching.latency().ifPresent(v -> addChildElement(chElement, "Latency", convertDuration(v)));
            proactiveCaching.silenceOverrideInterval().ifPresent(v -> addChildElement(chElement,
"SilenceOverrideInterval", convertDuration(v)));
            proactiveCaching.forceRebuildInterval().ifPresent(v -> addChildElement(chElement, "ForceRebuildInterval",
                convertDuration(v)));
            proactiveCaching.enabled().ifPresent(v -> addChildElement(chElement, "Enabled", String.valueOf(v)));
        }
    }

    private void setProactiveCachingBinding(SOAPElement element, ProactiveCachingBinding source) {
        if (source != null) {
            SOAPElement chElement = addChildElement(element, SOURCE);
            if (source instanceof ProactiveCachingIncrementalProcessingBinding pcipb) {
                pcipb.refreshInterval().ifPresent(v -> addChildElement(chElement, REFRESH_INTERVAL,
                    convertDuration(v)));
                setIncrementalProcessingNotificationList(chElement, pcipb.incrementalProcessingNotifications());
            }
            if (source instanceof ProactiveCachingQueryBinding pcqb) {
                pcqb.refreshInterval().ifPresent(v -> addChildElement(chElement, REFRESH_INTERVAL,
                    convertDuration(v)));
                setQueryNotificationList(chElement, pcqb.queryNotifications());
            }

        }
    }

    private void setQueryNotificationList(SOAPElement element, List<QueryNotification> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "QueryNotifications");
            list.forEach(it -> setQueryNotification(chElement, it));
        }
    }

    private void setQueryNotification(SOAPElement element, QueryNotification it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "QueryNotification");
            it.query().ifPresent(v -> addChildElement(chElement, "Query", v));
        }
    }

    private void setIncrementalProcessingNotificationList(
        SOAPElement element,
        List<IncrementalProcessingNotification> list
    ) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "IncrementalProcessingNotifications");
            list.forEach(it -> setIncrementalProcessingNotification(chElement, it));
        }
    }

    private void setIncrementalProcessingNotification(SOAPElement element, IncrementalProcessingNotification it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "IncrementalProcessingNotification");
            addChildElement(chElement, TABLE_ID, it.tableID());
            addChildElement(chElement, "ProcessingQuery", it.processingQuery());
        }
    }

    private String convertDuration(java.time.Duration duration) {
        if (duration != null) {
            return duration.toString();
        }
        return null;
    }

    private void setCubeStorageMode(SOAPElement element, Cube.StorageMode storageMode) {
        if (storageMode != null) {
            SOAPElement chElement = addChildElement(element, STORAGE_MODE);
            addChildElement(chElement, VALUENS, storageMode.valuens());
            addChildElement(chElement, VALUE, storageMode.value() == null ? null : storageMode.value().value());
        }

    }

    private void setDataSourceViewBinding(SOAPElement element, DataSourceViewBinding source) {
        if (source != null) {
            SOAPElement chElement = addChildElement(element, SOURCE);
            addChildElement(chElement, DATA_SOURCE_VIEW_ID, source.dataSourceViewID());
        }
    }

    private void setPerspectiveList(SOAPElement element, List<Perspective> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Perspectives");
            list.forEach(it -> setPerspective(chElement, it));
        }
    }

    private void setMdxScriptList(SOAPElement element, List<MdxScript> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "MdxScripts");
            list.forEach(it -> setMdxScript(chElement, it));
        }
    }

    private void setMeasureGroupList(SOAPElement element, List<MeasureGroup> measureGroups) {
        if (measureGroups != null) {
            SOAPElement chElement = addChildElement(element, "MeasureGroups");
            measureGroups.forEach(it -> setMeasureGroup(chElement, it));
        }
    }

    private void setCubePermissionList(SOAPElement element, List<CubePermission> cubePermissions) {
        if (cubePermissions != null) {
            SOAPElement chElement = addChildElement(element, "CubePermissions");
            cubePermissions.forEach(it -> setCubePermission(chElement, it));
        }

    }

    private void setCubePermission(SOAPElement element, CubePermission cubePermission) {
        SOAPElement dimensionElement = addChildElement(element, "CubePermission");
        cubePermission.readSourceData().ifPresent(v -> addChildElement(dimensionElement, "ReadSourceData", v));
        cubePermission.dimensionPermissions().ifPresent(v -> setCubeDimensionPermissionList(dimensionElement, v));
        cubePermission.cellPermissions().ifPresent(v -> setCellPermissionList(dimensionElement, v));
    }

    private void setCellPermissionList(SOAPElement element, List<CellPermission> v) {
        if (v != null) {
            SOAPElement chElement = addChildElement(element, "CellPermissions");
            v.forEach(it -> setCellPermission(chElement, it));
        }
    }

    private void setCellPermission(SOAPElement element, CellPermission it) {
        SOAPElement chElement = addChildElement(element, "CellPermission");
        it.access().ifPresent(v -> addChildElement(chElement, "Access", v.getValue()));
        it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
        it.expression().ifPresent(v -> addChildElement(chElement, EXPRESSION, v));
        it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
    }

    private void setCubeDimensionPermissionList(SOAPElement element, List<CubeDimensionPermission> v) {
        if (v != null) {
            SOAPElement chElement = addChildElement(element, "DimensionPermissions");
            v.forEach(it -> setCubeDimensionPermission(chElement, it));
        }
    }

    private void setCubeDimensionPermission(SOAPElement element, CubeDimensionPermission it) {
        SOAPElement chElement = addChildElement(element, "DimensionPermission");
        addChildElement(chElement, CUBE_DIMENSION_ID, it.cubeDimensionID());
        it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
        it.read().ifPresent(v -> addChildElement(chElement, "Read", v.getValue()));
        it.write().ifPresent(v -> addChildElement(chElement, WRITE, v.getValue()));
        it.attributePermissions().ifPresent(v -> setAttributePermissionList(chElement, v));
        it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
    }

    private void setAttributePermissionList(SOAPElement element, List<AttributePermission> v) {
        if (v != null) {
            SOAPElement chElement = addChildElement(element, "AttributePermissions");
            v.forEach(it -> setAttributePermission(chElement, it));
        }
    }

    private void setAttributePermission(SOAPElement element, AttributePermission it) {
        SOAPElement chElement = addChildElement(element, "AttributePermission");
        addChildElement(chElement, ATTRIBUTE_ID, it.attributeID());
        it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
        it.defaultMember().ifPresent(v -> addChildElement(chElement, DEFAULT_MEMBER, v));
        it.visualTotals().ifPresent(v -> addChildElement(chElement, "VisualTotals", v));
        it.allowedSet().ifPresent(v -> addChildElement(chElement, "AllowedSet", v));
        it.deniedSet().ifPresent(v -> addChildElement(chElement, "DeniedSet", v));
        it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
    }

    private void setCubeDimensionsList(SOAPElement cubeElement, List<CubeDimension> dimensions) {
        if (dimensions != null) {
            SOAPElement dimensionsElement = addChildElement(cubeElement, DIMENSIONS);
            dimensions.forEach(it -> setCubeDimension(dimensionsElement, it));
        }
    }

    private void setCubeDimension(SOAPElement dimensionsElement, CubeDimension cubeDimension) {
        SOAPElement dimensionElement = addChildElement(dimensionsElement, DIMENSION);
        addChildElement(dimensionElement, "ID", cubeDimension.id());
        addChildElement(dimensionElement, "Name", cubeDimension.name());
        addChildElement(dimensionElement, DESCRIPTION, cubeDimension.description());
        setTranslationList(dimensionElement, TRANSLATIONS, TRANSLATION, cubeDimension.translations());
        addChildElement(dimensionElement, DIMENSION_ID, cubeDimension.dimensionID());
        addChildElement(dimensionElement, VISIBLE, String.valueOf(cubeDimension.visible()));
        addChildElement(dimensionElement, "AllMemberAggregationUsage", cubeDimension.allMemberAggregationUsage());
        addChildElement(dimensionElement, "HierarchyUniqueNameStyle", cubeDimension.hierarchyUniqueNameStyle());
        addChildElement(dimensionElement, "MemberUniqueNameStyle", cubeDimension.memberUniqueNameStyle());
        setCubeAttributeList(dimensionElement, cubeDimension.attributes());
        setCubeHierarchyList(dimensionElement, cubeDimension.hierarchies());
        setAnnotationList(dimensionElement, cubeDimension.annotations());
    }

    private void setCubeHierarchyList(SOAPElement element, List<CubeHierarchy> hierarchies) {
        if (hierarchies != null) {
            SOAPElement hierarchiesElement = addChildElement(element, HIERARCHIES);
            hierarchies.forEach(v -> setCubeHierarchy(hierarchiesElement, v));
        }
    }

    private void setCubeHierarchy(SOAPElement element, CubeHierarchy v) {
        SOAPElement hierarchyElement = addChildElement(element, HIERARCHY);
        addChildElement(hierarchyElement, "HierarchyID", v.hierarchyID());
        addChildElement(hierarchyElement, "OptimizedState", v.optimizedState());
        addChildElement(hierarchyElement, VISIBLE, String.valueOf(v.visible()));
        addChildElement(hierarchyElement, "Enabled", String.valueOf(v.enabled()));
        setAnnotationList(hierarchyElement, v.annotations());
    }

    private void setCubeAttributeList(SOAPElement element, List<CubeAttribute> attributes) {
        if (attributes != null) {
            SOAPElement attributesElement = addChildElement(element, ATTRIBUTES);
            attributes.forEach(v -> setCubeAttribute(attributesElement, v));
        }
    }

    private void setCubeAttribute(SOAPElement element, CubeAttribute v) {
        SOAPElement attributeElement = addChildElement(element, ATTRIBUTE);
        addChildElement(attributeElement, ATTRIBUTE_ID, v.attributeID());
        addChildElement(attributeElement, "AggregationUsage", v.aggregationUsage());
        addChildElement(attributeElement, "AttributeHierarchyOptimizedState", v.attributeHierarchyOptimizedState());
        addChildElement(attributeElement, "AttributeHierarchyEnabled", String.valueOf(v.attributeHierarchyEnabled()));
        addChildElement(attributeElement, ATTRIBUTE_HIERARCHY_VISIBLE, String.valueOf(v.attributeHierarchyVisible()));
        setAnnotationList(attributeElement, v.annotations());
    }

    private void setTranslationList(
        SOAPElement cubeElement,
        String wrapperName,
        String tagName,
        List<Translation> translations
    ) {
        if (translations != null) {
            SOAPElement translationsElement = addChildElement(cubeElement, wrapperName);
            translations.forEach(it -> setTranslation(translationsElement, tagName, it));
        }
    }

    private void setTranslation(SOAPElement translationsElement, String tagName, Translation it) {
        if (it != null) {
            SOAPElement translationElement = addChildElement(translationsElement, tagName);
            addChildElement(translationElement, LANGUAGE, String.valueOf(it.language()));
            addChildElement(translationElement, CAPTION, String.valueOf(it.caption()));
            addChildElement(translationElement, DESCRIPTION, String.valueOf(it.description()));
            addChildElement(translationElement, DISPLAY_FOLDER, String.valueOf(it.displayFolder()));
            setAnnotationList(translationElement, it.annotations());
        }
    }

    private void setAssembly(SOAPElement objectDefinitionElement, Assembly assembly) {
        if (assembly != null) {
            SOAPElement assemblyElement = addChildElement(objectDefinitionElement, "Assembly");
            addChildElement(assemblyElement, "Name", assembly.name());
            addChildElement(assemblyElement, "ID", assembly.id());
            addChildElement(assemblyElement, CREATED_TIMESTAMP, assembly.createdTimestamp().toString());
            addChildElement(assemblyElement, LAST_SCHEMA_UPDATE, assembly.lastSchemaUpdate().toString());
            addChildElement(assemblyElement, DESCRIPTION, assembly.description());
            setAnnotationList(assemblyElement, assembly.annotations());
        }
    }

    private void setAnnotationList(SOAPElement element, List<Annotation> annotations) {
        if (annotations != null) {
            SOAPElement annotationsElement = addChildElement(element, "Annotations");
            annotations.forEach(it -> setAnnotation(annotationsElement, it));
        }
    }

    private void setAnnotation(SOAPElement annotationsElement, Annotation annotation) {
        if (annotation != null) {
            SOAPElement annotationElement = addChildElement(annotationsElement, "Annotation");
            addChildElement(annotationElement, "Name", annotation.name());
            annotation.visibility().ifPresent(v -> addChildElement(annotationElement, "Visibility", v));
            addChildElement(annotationElement, VALUE2, annotation.value().toString());
        }
    }

    private void setAggregationDesign(
        SOAPElement objectDefinitionElement,
        AggregationDesign aggregationDesign
    ) {
        if (aggregationDesign != null) {
            SOAPElement aggregationDesignEl = addChildElement(objectDefinitionElement, "AggregationDesign");
            aggregationDesign.estimatedRows().ifPresent(v -> addChildElement(aggregationDesignEl, ESTIMATED_ROWS,
                String.valueOf(v)));
            aggregationDesign.dimensions().ifPresent(v -> setAggregationDesignDimensionList(aggregationDesignEl, v));
            aggregationDesign.aggregations().ifPresent(v -> setAggregationList(aggregationDesignEl, v));
            aggregationDesign.estimatedPerformanceGain().ifPresent(v -> addChildElement(aggregationDesignEl,
                "EstimatedPerformanceGain", String.valueOf(v)));
        }
    }

    private void setAggregationDesignDimensionList(SOAPElement element, List<AggregationDesignDimension> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, DIMENSIONS);
            list.forEach(it -> setAggregationDesignDimension(chElement, it));
        }

    }

    private void setAggregationDesignDimension(SOAPElement element, AggregationDesignDimension it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, DIMENSION);
            addChildElement(chElement, CUBE_DIMENSION_ID, it.cubeDimensionID());
            it.attributes().ifPresent(v -> setAggregationDesignAttributeList(chElement, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setAggregationDesignAttributeList(SOAPElement element, List<AggregationDesignAttribute> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTES);
            list.forEach(it -> setAggregationDesignAttribute(chElement, it));
        }

    }

    private void setAggregationDesignAttribute(SOAPElement element, AggregationDesignAttribute it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTE);
            addChildElement(chElement, ATTRIBUTE_ID, it.attributeID());
            it.estimatedCount().ifPresent(v -> addChildElement(chElement, "EstimatedCount", String.valueOf(v)));
        }
    }

    private void setAggregationList(SOAPElement element, List<Aggregation> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, "Aggregations");
            list.forEach(it -> setAggregation(chElement, it));
        }
    }

    private void setAggregation(SOAPElement element, Aggregation it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, "Aggregation");
            it.id().ifPresent(v -> addChildElement(chElement, "ID", v));
            addChildElement(chElement, "Name", it.name());
            it.description().ifPresent(v -> addChildElement(chElement, DESCRIPTION, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
            it.dimensions().ifPresent(v -> setAggregationDimensionList(chElement, v));
        }

    }

    private void setAggregationDimensionList(SOAPElement element, List<AggregationDimension> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, DIMENSIONS);
            list.forEach(it -> setAggregationDimension(chElement, it));
        }
    }

    private void setAggregationDimension(SOAPElement element, AggregationDimension it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, DIMENSION);
            addChildElement(chElement, CUBE_DIMENSION_ID, it.cubeDimensionID());
            it.attributes().ifPresent(v -> setAggregationAttributeList(chElement, v));
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setAggregationAttributeList(SOAPElement element, List<AggregationAttribute> list) {
        if (list != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTES);
            list.forEach(it -> setAggregationAttribute(chElement, it));
        }
    }

    private void setAggregationAttribute(SOAPElement element, AggregationAttribute it) {
        if (it != null) {
            SOAPElement chElement = addChildElement(element, ATTRIBUTE);
            addChildElement(chElement, ATTRIBUTE_ID, it.attributeID());
            it.annotations().ifPresent(v -> setAnnotationList(chElement, v));
        }
    }

    private void setObject(SOAPElement element, ObjectReference reference) {
        if (reference != null) {
            SOAPElement chElement = addChildElement(element, "Object");
            addChildElement(chElement, SERVER_ID, reference.serverID());
            addChildElement(chElement, "DatabaseID", reference.databaseID());
            addChildElement(chElement, ROLE_ID, reference.roleID());
            addChildElement(chElement, "TraceID", reference.traceID());
            addChildElement(chElement, "AssemblyID", reference.assemblyID());
            addChildElement(chElement, DIMENSION_ID, reference.dimensionID());
            addChildElement(chElement, "DimensionPermissionID", reference.dimensionPermissionID());
            addChildElement(chElement, DATA_SOURCE_ID, reference.dataSourceID());
            addChildElement(chElement, "DataSourcePermissionID", reference.dataSourcePermissionID());
            addChildElement(chElement, "DatabasePermissionID", reference.databasePermissionID());
            addChildElement(chElement, DATA_SOURCE_VIEW_ID, reference.dataSourceViewID());
            addChildElement(chElement, CUBE_ID, reference.cubeID());
            addChildElement(chElement, "MiningStructureID", reference.miningStructureID());
            addChildElement(chElement, MEASURE_GROUP_ID, reference.measureGroupID());
            addChildElement(chElement, "PerspectiveID", reference.perspectiveID());
            addChildElement(chElement, "CubePermissionID", reference.cubePermissionID());
            addChildElement(chElement, "MdxScriptID", reference.mdxScriptID());
            addChildElement(chElement, "PartitionID", reference.partitionID());
            addChildElement(chElement, AGGREGATION_DESIGN_ID, reference.partitionID());
            addChildElement(chElement, MINING_MODEL_ID, reference.miningModelID());
            addChildElement(chElement, "MiningModelPermissionID", reference.miningModelPermissionID());
            addChildElement(chElement, "MiningStructurePermissionID", reference.miningStructureID());
        }
    }

    private Consumer<SOAPMessage> getConsumer(StatementRequest requestApi) {
        return message -> {
            try {
                Statement statement = requestApi.command();
                Properties properties = requestApi.properties();

                SOAPElement execute = message.getSOAPBody()
                    .addChildElement(EXECUTE);
                execute.addChildElement(COMMAND)
                    .addChildElement("Statement").setTextContent(statement.statement());

                SOAPElement propertyList = execute.addChildElement(PROPERTIES)
                    .addChildElement(PROPERTY_LIST);
                setPropertyList(propertyList, properties);

            } catch (SOAPException e) {
                LOGGER.error("ExecuteService StatementRequest accept error", e);
            }
        };

    }
}

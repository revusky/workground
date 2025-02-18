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

import static org.eclipse.daanse.xmla.ws.jakarta.basic.AnnotationConvertor.convertAnnotationList;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CommandConvertor.convertDataSource;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CommandConvertor.convertDataSourceView;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.ConvertorUtil.convertToInstant;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertCube;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertMdxScript;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertMeasureGroup;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertPartition;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertPerspective;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.DatabaseConvertor.convertDatabase;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.DimensionConvertor.convertDimension;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.MiningModelConvertor.convertMiningModel;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.MiningStructureConvertor.convertMiningStructure;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.PermissionConvertor.convertPermission;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.RoleConvertor.convertRole;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.ServerConvertor.convertServer;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.TraceConvertor.convertTrace;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.xmla.api.engine.ImpersonationInfo;
import org.eclipse.daanse.xmla.api.xmla.Aggregation;
import org.eclipse.daanse.xmla.api.xmla.AggregationAttribute;
import org.eclipse.daanse.xmla.api.xmla.AggregationDesign;
import org.eclipse.daanse.xmla.api.xmla.AggregationDesignAttribute;
import org.eclipse.daanse.xmla.api.xmla.AggregationDesignDimension;
import org.eclipse.daanse.xmla.api.xmla.AggregationDimension;
import org.eclipse.daanse.xmla.api.xmla.Assembly;
import org.eclipse.daanse.xmla.api.xmla.Cube;
import org.eclipse.daanse.xmla.api.xmla.DataSource;
import org.eclipse.daanse.xmla.api.xmla.DataSourceView;
import org.eclipse.daanse.xmla.api.xmla.Database;
import org.eclipse.daanse.xmla.api.xmla.Dimension;
import org.eclipse.daanse.xmla.api.xmla.MajorObject;
import org.eclipse.daanse.xmla.api.xmla.MdxScript;
import org.eclipse.daanse.xmla.api.xmla.MeasureGroup;
import org.eclipse.daanse.xmla.api.xmla.MiningModel;
import org.eclipse.daanse.xmla.api.xmla.MiningStructure;
import org.eclipse.daanse.xmla.api.xmla.Partition;
import org.eclipse.daanse.xmla.api.xmla.Permission;
import org.eclipse.daanse.xmla.api.xmla.Perspective;
import org.eclipse.daanse.xmla.api.xmla.Role;
import org.eclipse.daanse.xmla.api.xmla.Server;
import org.eclipse.daanse.xmla.api.xmla.Trace;
import org.eclipse.daanse.xmla.model.record.engine.ImpersonationInfoR;
import org.eclipse.daanse.xmla.model.record.xmla.AggregationAttributeR;
import org.eclipse.daanse.xmla.model.record.xmla.AggregationDesignAttributeR;
import org.eclipse.daanse.xmla.model.record.xmla.AggregationDesignDimensionR;
import org.eclipse.daanse.xmla.model.record.xmla.AggregationDesignR;
import org.eclipse.daanse.xmla.model.record.xmla.AggregationDimensionR;
import org.eclipse.daanse.xmla.model.record.xmla.AggregationR;
import org.eclipse.daanse.xmla.model.record.xmla.AssemblyR;
import org.eclipse.daanse.xmla.model.record.xmla.MajorObjectR;

public class MajorObjectConvertor {

	private MajorObjectConvertor() {
	}

    public static MajorObject convertMajorObject(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.MajorObject objectDefinition) {
        AggregationDesign aggregationDesign = convertAggregationDesign(objectDefinition.getAggregationDesign());
        Assembly assembly = convertAssembly(objectDefinition.getAssembly());
        Cube cube = convertCube(objectDefinition.getCube());
        Database database = convertDatabase(objectDefinition.getDatabase());
        DataSource dataSource = convertDataSource(objectDefinition.getDataSource());
        DataSourceView dataSourceView = convertDataSourceView(objectDefinition.getDataSourceView());
        Dimension dimension = convertDimension(objectDefinition.getDimension());
        MdxScript mdxScript = convertMdxScript(objectDefinition.getMdxScript());
        MeasureGroup measureGroup = convertMeasureGroup(objectDefinition.getMeasureGroup());
        MiningModel miningModel = convertMiningModel(objectDefinition.getMiningModel());
        MiningStructure miningStructure = convertMiningStructure(objectDefinition.getMiningStructure());
        Partition partition = convertPartition(objectDefinition.getPartition());
        Permission permission = convertPermission(objectDefinition.getPermission());
        Perspective perspective = convertPerspective(objectDefinition.getPerspective());
        Role role = convertRole(objectDefinition.getRole());
        Server server = convertServer(objectDefinition.getServer());
        Trace trace = convertTrace(objectDefinition.getTrace());

        return new MajorObjectR(
            aggregationDesign,
            assembly,
            cube,
            database,
            dataSource,
            dataSourceView,
            dimension,
            mdxScript,
            measureGroup,
            miningModel,
            miningStructure,
            partition,
            permission,
            perspective,
            role,
            server,
            trace);
    }

    public static Assembly convertAssembly(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Assembly assembly) {
        if (assembly != null) {
            return new AssemblyR(assembly.getID(),
                assembly.getName(),
                convertToInstant(assembly.getCreatedTimestamp()),
                convertToInstant(assembly.getLastSchemaUpdate()),
                assembly.getDescription(),
                convertAnnotationList(assembly.getAnnotations()),
                converImpersonationInfo(assembly.getImpersonationInfo()));
        }
        return null;
    }

    private static ImpersonationInfo converImpersonationInfo(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine.ImpersonationInfo impersonationInfo) {
        if (impersonationInfo != null) {
            return new ImpersonationInfoR(impersonationInfo.getImpersonationMode(),
                Optional.ofNullable(impersonationInfo.getAccount()),
                Optional.ofNullable(impersonationInfo.getPassword()),
                Optional.ofNullable(impersonationInfo.getImpersonationInfoSecurity()));
        }
        return null;
    }

    private static AggregationDesign convertAggregationDesign(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationDesign aggregationDesign) {
        if (aggregationDesign != null) {
            return new AggregationDesignR(aggregationDesign.getName(),
                Optional.ofNullable(aggregationDesign.getID()),
                Optional.ofNullable(convertToInstant(aggregationDesign.getCreatedTimestamp())),
                Optional.ofNullable(convertToInstant(aggregationDesign.getLastSchemaUpdate())),
                Optional.ofNullable(aggregationDesign.getDescription()),
                Optional.ofNullable(convertAnnotationList(aggregationDesign.getAnnotations() == null ? null :
                    aggregationDesign.getAnnotations())),
                Optional.ofNullable(aggregationDesign.getEstimatedRows()),
                Optional.ofNullable(convertAggregationDesignDimensionList(aggregationDesign.getDimensions())),
                Optional.ofNullable(convertAggregationList(aggregationDesign.getAggregations())),
                Optional.ofNullable(aggregationDesign.getEstimatedPerformanceGain()));
        }
        return null;
    }

    private static List<AggregationDesignDimension> convertAggregationDesignDimensionList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationDesignDimension> dimensionList) {
        if (dimensionList != null) {
            return dimensionList.stream().map(MajorObjectConvertor::convertAggregationDesignDimension).toList();
        }
        return List.of();
    }

    private static AggregationDesignDimension convertAggregationDesignDimension(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationDesignDimension aggregationDesignDimension) {
        if (aggregationDesignDimension != null) {
            return new AggregationDesignDimensionR(
                aggregationDesignDimension.getCubeDimensionID(),
                Optional.ofNullable(convertAggregationDesignAttributeList(aggregationDesignDimension.getAttributes())),
                Optional.ofNullable(convertAnnotationList(aggregationDesignDimension.getAnnotations())));
        }
        return null;
    }

    private static List<AggregationDesignAttribute> convertAggregationDesignAttributeList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationDesignAttribute> attributeList) {
        if (attributeList != null) {
            return attributeList.stream().map(MajorObjectConvertor::convertAggregationDesignAttribute).toList();
        }
        return List.of();
    }

    private static AggregationDesignAttribute convertAggregationDesignAttribute(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationDesignAttribute aggregationDesignAttribute) {
        if (aggregationDesignAttribute != null) {
            return new AggregationDesignAttributeR(aggregationDesignAttribute.getAttributeID(),
                Optional.ofNullable(aggregationDesignAttribute.getEstimatedCount()));
        }
        return null;

    }

    private static List<Aggregation> convertAggregationList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Aggregation> aggregationList) {
        if (aggregationList != null) {
            return aggregationList.stream().map(MajorObjectConvertor::convertAggregation).toList();
        }
        return List.of();
    }

    private static Aggregation convertAggregation(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Aggregation aggregation) {
        if (aggregation != null) {
            return new AggregationR(Optional.ofNullable(aggregation.getID()),
                aggregation.getName(),
                Optional.ofNullable(convertAggregationDimensionList(aggregation.getDimensions())),
                Optional.ofNullable(convertAnnotationList(aggregation.getAnnotations())),
                Optional.ofNullable(aggregation.getDescription()));
        }
        return null;
    }

    private static List<AggregationDimension> convertAggregationDimensionList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationDimension> dimensionList) {
        if (dimensionList != null) {
            return dimensionList.stream().map(MajorObjectConvertor::convertAggregationDimension).toList();
        }
        return List.of();
    }

    private static AggregationDimension convertAggregationDimension(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationDimension aggregationDimension) {
        if (aggregationDimension != null) {
            return new AggregationDimensionR(aggregationDimension.getCubeDimensionID(),
                Optional.ofNullable(convertAggregationAttributeList(aggregationDimension.getAttributes())),
                Optional.ofNullable(convertAnnotationList(aggregationDimension.getAnnotations())));
        }
        return null;
    }

    private static List<AggregationAttribute> convertAggregationAttributeList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationAttribute> attributeList) {
        if (attributeList != null) {
            return attributeList.stream().map(MajorObjectConvertor::convertAggregationAttribute).toList();
        }
        return List.of();
    }

    private static AggregationAttribute convertAggregationAttribute(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AggregationAttribute aggregationAttribute) {
        if (aggregationAttribute != null) {
            return new AggregationAttributeR(
                aggregationAttribute.getAttributeID(),
                Optional.ofNullable(convertAnnotationList(aggregationAttribute.getAnnotations())));
        }
        return null;
    }
}

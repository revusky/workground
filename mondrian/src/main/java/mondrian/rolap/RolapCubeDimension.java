/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2001-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// Copyright (C) 2021 Sergei Semenkov
// All Rights Reserved.
*/
package mondrian.rolap;

import java.util.List;

import org.eclipse.daanse.olap.api.model.Schema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.CubeDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.VirtualCubeDimension;

import mondrian.olap.DimensionType;
import mondrian.olap.HierarchyBase;

/**
 * RolapCubeDimension wraps a RolapDimension for a specific Cube.
 *
 * @author Will Gorman, 19 October 2007
 */
public class RolapCubeDimension extends RolapDimension {

    RolapCube cube;

    RolapDimension rolapDimension;
    int cubeOrdinal;
    CubeDimension xmlDimension;

    /**
     * Creates a RolapCubeDimension.
     *
     * @param cube Cube
     * @param rolapDim Dimension wrapped by this dimension
     * @param cubeDim XML element definition
     * @param name Name of dimension
     * @param cubeOrdinal Ordinal of dimension within cube
     * @param hierarchyList List of hierarchies in cube
     * @param highCardinality Whether high cardinality dimension
     */
    public RolapCubeDimension(
        RolapCube cube,
        RolapDimension rolapDim,
        CubeDimension cubeDim,
        String name,
        int cubeOrdinal,
        List<RolapHierarchy> hierarchyList,
        final boolean highCardinality)
    {
        super(
            null,
            name,
            cubeDim.caption() != null
                ? cubeDim.caption()
                : rolapDim.getCaption(),
            cubeDim.visible(),
            cubeDim.description() != null
                ? cubeDim.description()
                : rolapDim.getDescription(),
            null,
            highCardinality,
            (cubeDim.annotations() != null && !cubeDim.annotations().isEmpty())
                ? RolapHierarchy.createMetadataMap(cubeDim.annotations())
                : rolapDim.getMetadata());
        this.xmlDimension = cubeDim;
        this.rolapDimension = rolapDim;
        this.cubeOrdinal = cubeOrdinal;
        this.cube = cube;
        this.caption = cubeDim.caption();

        // create new hierarchies
        hierarchies = new RolapCubeHierarchy[rolapDim.getHierarchies().length];

        RolapCube factCube = null;
        if (cube.isVirtual()) {
          factCube = lookupFactCube(cubeDim, cube.getSchema());
        }
        for (int i = 0; i < rolapDim.getHierarchies().length; i++) {
          final RolapCubeHierarchy cubeHierarchy =
                new RolapCubeHierarchy(
                    this,
                    cubeDim,
                    (RolapHierarchy) rolapDim.getHierarchies()[i],
                    ((HierarchyBase) rolapDim.getHierarchies()[i]).getSubName(),
                    hierarchyList.size(),
                    factCube);
            hierarchies[i] = cubeHierarchy;
            hierarchyList.add(cubeHierarchy);
        }
    }

    RolapCube lookupFactCube(
        CubeDimension cubeDim, RolapSchema schema)
    {
      if (cubeDim instanceof VirtualCubeDimension virtualCubeDim && virtualCubeDim.cubeName() != null) {
          return schema.lookupCube(virtualCubeDim.cubeName());
      }
      return null;
    }

    public RolapCube getCube() {
        return cube;
    }

    @Override
	public Schema getSchema() {
        return rolapDimension.getSchema();
    }

    // this method should eventually replace the call below
    public int getOrdinal() {
        return cubeOrdinal;
    }

    @Override
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RolapCubeDimension that)) {
            return false;
        }

        if (!cube.equalsOlapElement(that.cube)) {
            return false;
        }
        return getUniqueName().equals(that.getUniqueName());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
	RolapCubeHierarchy newHierarchy(
        String subName, boolean hasAll, RolapHierarchy closureFor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
	public String getCaption() {
        if (caption != null) {
            return caption;
        }
        return this.name;
    }

    @Override
	public void setCaption(String caption) {
        if (true) {
            throw new UnsupportedOperationException();
        }
        rolapDimension.setCaption(caption);
    }

    @Override
	public DimensionType getDimensionType() {
        return rolapDimension.getDimensionType();
    }

}

/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2001-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/
package mondrian.rolap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.opencube.junit5.TestUtil.assertQueryReturns;

import javax.sql.DataSource;

import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.Level;
import org.eclipse.daanse.olap.rolap.dbmapper.api.CubeDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.api.Relation;
import org.eclipse.daanse.olap.rolap.dbmapper.api.RelationOrJoin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestingContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import mondrian.olap.SchemaReader;

public class RolapCubeHierarchyTest {

  @ParameterizedTest
  @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
  public void testMONDRIAN2535(TestingContext context) {
    assertQueryReturns(context.createConnection(),
        "Select\n"
        + "  [Customers].children on rows,\n"
        + "  [Gender].children on columns\n "
        + "From [Warehouse and Sales]",
        "Axis #0:\n"
        + "{}\n"
        + "Axis #1:\n"
        + "{[Gender].[F]}\n"
        + "{[Gender].[M]}\n"
        + "Axis #2:\n"
        + "{[Customers].[Canada]}\n"
        + "{[Customers].[Mexico]}\n"
        + "{[Customers].[USA]}\n"
        + "Row #0: \n"
        + "Row #0: \n"
        + "Row #1: \n"
        + "Row #1: \n"
        + "Row #2: 280,226.21\n"
        + "Row #2: 285,011.92\n");
  }

  @Test
  public void testInit_NoFactCube() {
    RolapCubeDimension cubeDimension = mock(RolapCubeDimension.class);
    RolapCube cubeDimension_cube = mock(RolapCube.class);
    boolean cubeDimension_cube_isVirtual = true;
    String cubeDimension_uniqueName = "TheDimUniqueName";
    RolapSchema cubeDimension_schema = mock(RolapSchema.class);
    RolapConnection cubeDimension_schema_connection =
        mock(RolapConnection.class);
    DataSource cubeDimension_schema_connection_DS = mock(DataSource.class);
    SchemaReader schemaReader = mock(SchemaReader.class);

    CubeDimension cubeDim = null;

    RolapHierarchy rolapHierarchy = mock(RolapHierarchy.class);
    Hierarchy rolapHierarchy_hierarchy = null;
    String rolapHierarchy_uniqueName = "TheDimUniqueName";
    Level[] rolapHierarchy_levels = new Level[]{};

    String subName = null;

    int ordinal = 0;

    RolapCube factCube = null;

    doReturn(cubeDimension_cube).when(cubeDimension).getCube();
    doReturn(cubeDimension_cube_isVirtual).when(cubeDimension_cube).isVirtual();
    doReturn(cubeDimension_schema).when(cubeDimension).getSchema();
    doReturn(schemaReader).when(cubeDimension_schema)
    .getSchemaReader();
    doReturn(cubeDimension_schema_connection).when(cubeDimension_schema)
      .getInternalConnection();
    doReturn(cubeDimension_schema_connection_DS)
      .when(cubeDimension_schema_connection).getDataSource();
    doReturn(cubeDimension_uniqueName).when(cubeDimension).getUniqueName();
    doReturn(rolapHierarchy_hierarchy).when(rolapHierarchy).getHierarchy();
    doReturn(rolapHierarchy_levels).when(rolapHierarchy).getLevels();
    doReturn(rolapHierarchy_uniqueName).when(rolapHierarchy).getUniqueName();

    RolapCubeHierarchy rch = new RolapCubeHierarchy(
        cubeDimension, cubeDim, rolapHierarchy, subName, ordinal, factCube);
    assertEquals(true, rch.isUsingCubeFact(), "If factCube is null");

    rch = new RolapCubeHierarchy(
        cubeDimension, cubeDim, rolapHierarchy, subName, ordinal);
    assertEquals(true, rch.isUsingCubeFact(), "If factCube is not specified");
  }

  @Test
  public void testInit_FactCube_NoFactTable() {
    RolapCubeDimension cubeDimension = mock(RolapCubeDimension.class);
    RolapCube cubeDimension_cube = mock(RolapCube.class);
    boolean cubeDimension_cube_isVirtual = true;
    String cubeDimension_uniqueName = "TheDimUniqueName";
    RolapSchema cubeDimension_schema = mock(RolapSchema.class);
    RolapConnection cubeDimension_schema_connection =
        mock(RolapConnection.class);
    DataSource cubeDimension_schema_connection_DS = mock(DataSource.class);
    SchemaReader schemaReader = mock(SchemaReader.class);

    CubeDimension cubeDim = null;

    RolapHierarchy rolapHierarchy = mock(RolapHierarchy.class);
    Hierarchy rolapHierarchy_hierarchy = null;
    String rolapHierarchy_uniqueName = "TheDimUniqueName";
    Level[] rolapHierarchy_levels = new Level[]{};

    String subName = null;

    int ordinal = 0;

    RolapCube factCube = mock(RolapCube.class);
    RolapCube factCube_Fact = null;

    doReturn(cubeDimension_cube).when(cubeDimension).getCube();
    doReturn(cubeDimension_cube_isVirtual).when(cubeDimension_cube).isVirtual();
    doReturn(cubeDimension_schema).when(cubeDimension).getSchema();
    doReturn(cubeDimension_schema_connection).when(cubeDimension_schema)
      .getInternalConnection();
    doReturn(schemaReader).when(cubeDimension_schema)
    .getSchemaReader();
    doReturn(cubeDimension_schema_connection_DS)
      .when(cubeDimension_schema_connection).getDataSource();
    doReturn(cubeDimension_uniqueName).when(cubeDimension).getUniqueName();
    doReturn(rolapHierarchy_hierarchy).when(rolapHierarchy).getHierarchy();
    doReturn(rolapHierarchy_levels).when(rolapHierarchy).getLevels();
    doReturn(rolapHierarchy_uniqueName).when(rolapHierarchy).getUniqueName();
    doReturn(factCube_Fact).when(factCube).getFact();

    RolapCubeHierarchy rch = new RolapCubeHierarchy(
        cubeDimension, cubeDim, rolapHierarchy, subName, ordinal, factCube);
    assertEquals(true, rch.isUsingCubeFact());
  }

  @Test
  public void testInit_FactCube_FactTableDiffers() {
    RolapCubeDimension cubeDimension = mock(RolapCubeDimension.class);
    RolapCube cubeDimension_cube = mock(RolapCube.class);
    boolean cubeDimension_cube_isVirtual = true;
    String cubeDimension_uniqueName = "TheDimUniqueName";
    RolapSchema cubeDimension_schema = mock(RolapSchema.class);
    RolapConnection cubeDimension_schema_connection =
        mock(RolapConnection.class);
    DataSource cubeDimension_schema_connection_DS = mock(DataSource.class);

    CubeDimension cubeDim = null;

    RolapHierarchy rolapHierarchy = mock(RolapHierarchy.class);
    Hierarchy rolapHierarchy_hierarchy = null;
    String rolapHierarchy_uniqueName = "TheDimUniqueName";
    Level[] rolapHierarchy_levels = new Level[]{};
    RelationOrJoin rolapHierarchy_relation = mock(Relation.class);
    SchemaReader schemaReader = mock(SchemaReader.class);

    String subName = null;

    int ordinal = 0;

    RolapCube factCube = mock(RolapCube.class);
    RelationOrJoin factCube_Fact = mock(Relation.class);
    boolean factCube_Fact_equals = false;

    // check
    assertEquals(
        factCube_Fact_equals, factCube_Fact.equals(rolapHierarchy_relation));
    assertEquals(
        factCube_Fact_equals, rolapHierarchy_relation.equals(factCube_Fact));

    doReturn(cubeDimension_cube).when(cubeDimension).getCube();
    doReturn(cubeDimension_cube_isVirtual).when(cubeDimension_cube).isVirtual();
    doReturn(cubeDimension_schema).when(cubeDimension).getSchema();
    doReturn(cubeDimension_schema_connection).when(cubeDimension_schema)
      .getInternalConnection();
    doReturn(schemaReader).when(cubeDimension_schema)
    .getSchemaReader();
    doReturn(cubeDimension_schema_connection_DS)
      .when(cubeDimension_schema_connection).getDataSource();
    doReturn(cubeDimension_uniqueName).when(cubeDimension).getUniqueName();
    doReturn(rolapHierarchy_hierarchy).when(rolapHierarchy).getHierarchy();
    doReturn(rolapHierarchy_levels).when(rolapHierarchy).getLevels();
    doReturn(rolapHierarchy_uniqueName).when(rolapHierarchy).getUniqueName();
    doReturn(rolapHierarchy_relation).when(rolapHierarchy).getRelation();
    doReturn(factCube_Fact).when(factCube).getFact();

    RolapCubeHierarchy rch = new RolapCubeHierarchy(
        cubeDimension, cubeDim, rolapHierarchy, subName, ordinal, factCube);
    assertEquals(false, rch.isUsingCubeFact());
  }

  @Test
  public void testInit_FactCube_FactTableEquals() {
    RolapCubeDimension cubeDimension = mock(RolapCubeDimension.class);
    RolapCube cubeDimension_cube = mock(RolapCube.class);
    boolean cubeDimension_cube_isVirtual = true;
    String cubeDimension_uniqueName = "TheDimUniqueName";
    RolapSchema cubeDimension_schema = mock(RolapSchema.class);
    RolapConnection cubeDimension_schema_connection =
        mock(RolapConnection.class);
    DataSource cubeDimension_schema_connection_DS = mock(DataSource.class);
    SchemaReader schemaReader = mock(SchemaReader.class);

    CubeDimension cubeDim = null;

    RolapHierarchy rolapHierarchy = mock(RolapHierarchy.class);
    Hierarchy rolapHierarchy_hierarchy = null;
    String rolapHierarchy_uniqueName = "TheDimUniqueName";
    Level[] rolapHierarchy_levels = new Level[]{};
    RelationOrJoin rolapHierarchy_relation = mock(Relation.class);

    String subName = null;

    int ordinal = 0;

    RolapCube factCube = mock(RolapCube.class);
    RelationOrJoin factCube_Fact = rolapHierarchy_relation;
    boolean factCube_Fact_equals = true;

    // check
    assertEquals(
        factCube_Fact_equals, factCube_Fact.equals(rolapHierarchy_relation));
    assertEquals(
        factCube_Fact_equals, rolapHierarchy_relation.equals(factCube_Fact));

    doReturn(cubeDimension_cube).when(cubeDimension).getCube();
    doReturn(cubeDimension_cube_isVirtual).when(cubeDimension_cube).isVirtual();
    doReturn(cubeDimension_schema).when(cubeDimension).getSchema();
    doReturn(cubeDimension_schema_connection).when(cubeDimension_schema)
      .getInternalConnection();
    doReturn(schemaReader).when(cubeDimension_schema)
    .getSchemaReader();
    doReturn(cubeDimension_schema_connection_DS)
      .when(cubeDimension_schema_connection).getDataSource();
    doReturn(cubeDimension_uniqueName).when(cubeDimension).getUniqueName();
    doReturn(rolapHierarchy_hierarchy).when(rolapHierarchy).getHierarchy();
    doReturn(rolapHierarchy_levels).when(rolapHierarchy).getLevels();
    doReturn(rolapHierarchy_uniqueName).when(rolapHierarchy).getUniqueName();
    doReturn(rolapHierarchy_relation).when(rolapHierarchy).getRelation();
    doReturn(factCube_Fact).when(factCube).getFact();

    RolapCubeHierarchy rch = new RolapCubeHierarchy(
        cubeDimension, cubeDim, rolapHierarchy, subName, ordinal, factCube);
    assertEquals(true, rch.isUsingCubeFact());
  }

}
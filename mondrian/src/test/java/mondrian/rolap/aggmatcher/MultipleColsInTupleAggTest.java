/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/
package mondrian.rolap.aggmatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opencube.junit5.TestUtil.assertQueryReturns;
import static org.opencube.junit5.TestUtil.executeAxis;
import static org.opencube.junit5.TestUtil.getDialect;

import org.eclipse.daanse.olap.api.result.Axis;
import org.eclipse.daanse.olap.api.result.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextArgumentsProvider;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestingContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import mondrian.enums.DatabaseProduct;
import mondrian.olap.MondrianProperties;
import mondrian.rolap.RolapAxis;
import mondrian.test.SqlPattern;

/**
 * Testcase for levels that contain multiple columns and are
 * collapsed in the agg table.
 *
 * @author Will Gorman
 */
class MultipleColsInTupleAggTest extends AggTableTestCase {

    @BeforeAll
    public static void beforeAll() {
        ContextArgumentsProvider.dockerWasChanged = true;
    }

    @Override
	@BeforeEach
    public void beforeEach() {
        super.beforeEach();
        propSaver.set(propSaver.properties.GenerateFormattedSql, true);
    }

    @Override
	@AfterEach
    public void afterEach() {
        propSaver.reset();
    }



    @Override
	protected String getFileName() {
        return "multiple_cols_in_tuple_agg.csv";
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testTotal(TestingContext context) throws Exception {
        prepareContext(context);
        if (!isApplicable(context.createConnection())) {
            return;
        }

        MondrianProperties props = MondrianProperties.instance();

        // get value without aggregates
        propSaver.set(props.UseAggregates, false);
        propSaver.set(props.ReadAggregates, false);

        String mdx =
            "select {[Measures].[Total]} on columns from [Fact]";
        Result result = executeQuery(mdx, context.createConnection());
        Object v = result.getCell(new int[]{0}).getValue();

        String mdx2 =
            "select {[Measures].[Total]} on columns from [Fact] where "
            + "{[Product].[Cat One].[Prod Cat One].[One]}";
        Result aresult = executeQuery(mdx2, context.createConnection());
        Object av = aresult.getCell(new int[]{0}).getValue();

        // unless there is a way to flush the cache,
        // I'm skeptical about these results
        propSaver.set(props.UseAggregates, true);
        propSaver.set(props.ReadAggregates, false);

        Result result1 = executeQuery(mdx, context.createConnection());
        Object v1 = result1.getCell(new int[]{0}).getValue();

        assertTrue(v.equals(v1));

        Result aresult2 = executeQuery(mdx2, context.createConnection());
        Object av1 = aresult2.getCell(new int[]{0}).getValue();

        assertTrue(av.equals(av1));
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testTupleSelection(TestingContext context) throws Exception {
        prepareContext(context);
        if (!isApplicable(context.createConnection())) {
            return;
        }

        String mdx =
            "select "
            + "{[Measures].[Total]} on columns, "
            + "non empty CrossJoin({[Product].[Cat One].[Prod Cat One]},"
            + "{[Store].[All Stores]}) on rows "
            + "from [Fact]";

        assertQueryReturns(context.createConnection(),
            mdx,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Total]}\n"
            + "Axis #2:\n"
            + "{[Product].[Cat One].[Prod Cat One],"
            + " [Store].[All Stores]}\n"
            + "Row #0: 15\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testNativeFilterWithoutMeasures(TestingContext context) throws Exception {
        prepareContext(context);
        if (!isApplicable(context.createConnection())) {
            return;
        }
        // Native filter without any measures hit an edge case that
        // could fail to include the Agg star in the WHERE clause,
        // and could also mishandle the field referred to in the native
        // HAVING clause.  ANALYZER-2655
        assertQueryReturns(context.createConnection(),
            "select "
            + "Filter([Product].[Category].members, "
            + "Product.CurrentMember.Caption MATCHES (\"(?i).*Two.*\") )"
            + " on columns "
            + "from [Fact]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Product].[Cat Two]}\n"
            + "Row #0: 33\n");
        //  CurrentMember.Name should map to
        // `test_lp_xxx_fact`.`product_category`, with 2 member matches
        assertQueryReturns(context.createConnection(),
            "select "
            + "Filter([Product].[Product Category].members, "
            + "Product.CurrentMember.Name MATCHES (\"(?i).*Two.*\") )"
            + " on columns "
            + "from [Fact]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Product].[Cat Two].[Prod Cat Two]}\n"
            + "{[Product].[Cat One].[Prod Cat Two]}\n"
            + "Row #0: 16\n"
            + "Row #0: 18\n");
        // .Caption is defined as `product_cat`.`cap`.
        // [Cat One].[Prod Cat Two] has just one caption matching -- "PCTwo"
        assertQueryReturns(context.createConnection(),
            "select "
            + "Filter([Product].[Product Category].Members, "
            + "Product.CurrentMember.Caption MATCHES (\"(?i).*Two.*\") )"
            + " on columns "
            + "from [Fact]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Product].[Cat One].[Prod Cat Two]}\n"
            + "Row #0: 18\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testNativeFilterWithoutMeasuresAndLevelWithProps(TestingContext context)
        throws Exception
    {
        prepareContext(context);
        if (!isApplicable(context.createConnection())) {
            return;
        }
        // similar to the previous test, but verifies a case where
        // a level property is the extra column that requires joining
        // agg star back to the dim table.  This test also uses the bottom
        // level of the dim
        final String query = "select "
            + "Filter([Product].[Product Name].members, "
            + "Product.CurrentMember.Caption MATCHES (\"(?i).*Two.*\") )"
            + " on columns "
            + "from [Fact] ";
        assertQueryReturns(context.createConnection(),
            query,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Product].[Cat One].[Prod Cat One].[Two]}\n"
            + "Row #0: 6\n");

        // check generated sql only for native evaluation
        if (MondrianProperties.instance().EnableNativeFilter.get()) {
          assertQuerySql(context.createConnection(),
              query,
              new SqlPattern[] {
                  new SqlPattern(
                      DatabaseProduct.MYSQL,
                  "select\n"
                  + "    `cat`.`cat` as `c0`,\n"
                  + "    `cat`.`cap` as `c1`,\n"
                  + "    `cat`.`ord` as `c2`,\n"
                  + "    `cat`.`name3` as `c3`,\n"
                  + "    `product_cat`.`name2` as `c4`,\n"
                  + "    `product_cat`.`cap` as `c5`,\n"
                  + "    `product_cat`.`ord` as `c6`,\n"
                  + "    `test_lp_xx2_fact`.`prodname` as `c7`,\n"
                  + "    `product_csv`.`color` as `c8`\n"
                  + "from\n"
                  + "    `product_csv` as `product_csv`,\n"
                  + "    `product_cat` as `product_cat`,\n"
                  + "    `cat` as `cat`,\n"
                  + "    `test_lp_xx2_fact` as `test_lp_xx2_fact`\n"
                  + "where\n"
                  + "    `product_cat`.`cat` = `cat`.`cat`\n"
                  + "and\n"
                  + "    `product_csv`.`prod_cat` = `product_cat`.`prod_cat`\n"
                  + "and\n"
                  + "    `product_csv`.`name1` = `test_lp_xx2_fact`.`prodname`\n"
                  + "group by\n"
                  + "    `cat`.`cat`,\n"
                  + "    `cat`.`cap`,\n"
                  + "    `cat`.`ord`,\n"
                  + "    `cat`.`name3`,\n"
                  + "    `product_cat`.`name2`,\n"
                  + "    `product_cat`.`cap`,\n"
                  + "    `product_cat`.`ord`,\n"
                  + "    `test_lp_xx2_fact`.`prodname`,\n"
                  + "    `product_csv`.`color`\n"
                  + "having\n"
                  + "    c7 IS NOT NULL AND UPPER(c7) REGEXP '.*TWO.*'\n"
                  + "order by\n"
                  + (getDialect(context.createConnection()).requiresOrderByAlias()
                      ? "    ISNULL(`c2`) ASC, `c2` ASC,\n"
                      + "    ISNULL(`c6`) ASC, `c6` ASC,\n"
                      + "    ISNULL(`c7`) ASC, `c7` ASC"
                      : "    ISNULL(`cat`.`ord`) ASC, `cat`.`ord` ASC,\n"
                      + "    ISNULL(`product_cat`.`ord`) ASC, `product_cat`.`ord` ASC,\n"
                      + "    ISNULL(`test_lp_xx2_fact`.`prodname`) ASC, "
                      + "`test_lp_xx2_fact`.`prodname` ASC"), null)});
        }
        Axis axis = executeAxis(context.createConnection(), "Fact",
            "Filter([Product].[Product Name].members, "
            + "Product.CurrentMember.Caption MATCHES (\"(?i).*Two.*\") )");
        assertEquals(
            "Black",
            ((RolapAxis) axis).getTupleList().get(0).get(0)
                .getPropertyValue("Product Color"), "Member property value was not loaded correctly.");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testChildSelection(TestingContext context) throws Exception {
        prepareContext(context);
        if (!isApplicable(context.createConnection())) {
            return;
        }

        String mdx = "select {[Measures].[Total]} on columns, "
            + "non empty [Product].[Cat One].Children on rows from [Fact]";
        assertQueryReturns(context.createConnection(),
            mdx,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Total]}\n"
            + "Axis #2:\n"
            + "{[Product].[Cat One].[Prod Cat Two]}\n"
            + "{[Product].[Cat One].[Prod Cat One]}\n"
            + "Row #0: 18\n"
            + "Row #1: 15\n");
    }

    @Override
	protected String getCubeDescription() {
        return "<Cube name='Fact'>\n"
           + "<Table name='fact'>\n"
           + " <AggName name='test_lp_xxx_fact'>\n"
           + "  <AggFactCount column='fact_count'/>\n"
           + "  <AggMeasure column='amount' name='[Measures].[Total]'/>\n"
           + "  <AggLevel column='category' name='[Product].[Category]'/>\n"
           + "  <AggLevel column='product_category' "
           + "            name='[Product].[Product Category]'/>\n"
           + " </AggName>\n"
            + " <AggName name='test_lp_xx2_fact'>\n"
            + "  <AggFactCount column='fact_count'/>\n"
            + "  <AggMeasure column='amount' name='[Measures].[Total]'/>\n"
            + "  <AggLevel column='prodname' name='[Product].[Product Name]' collapsed='false'/>\n"
            + " </AggName>\n"
           + "</Table>"
           + "<Dimension name='Store' foreignKey='store_id'>\n"
           + " <Hierarchy hasAll='true' primaryKey='store_id'>\n"
           + "  <Table name='store_csv'/>\n"
           + "  <Level name='Store Value' column='value' "
           + "         uniqueMembers='true'/>\n"
           + " </Hierarchy>\n"
           + "</Dimension>\n"
           + "<Dimension name='Product' foreignKey='prod_id'>\n"
           + " <Hierarchy hasAll='true' primaryKey='prod_id' "
           + "primaryKeyTable='product_csv'>\n"
           + " <Join leftKey='prod_cat' rightAlias='product_cat' "
           + "rightKey='prod_cat'>\n"
           + "  <Table name='product_csv'/>\n"
           + "  <Join leftKey='cat' rightKey='cat'>\n"
           + "   <Table name='product_cat'/>\n"
           + "   <Table name='cat'/>\n"
           + "  </Join>"
           + " </Join>\n"
           + " <Level name='Category' table='cat' column='cat' "
           + "ordinalColumn='ord' captionColumn='cap' nameColumn='name3' "
           + "uniqueMembers='false' type='Numeric'/>\n"
           + " <Level name='Product Category' table='product_cat' "
           + "column='name2' ordinalColumn='ord' captionColumn='cap' "
           + "uniqueMembers='false'/>\n"
           + " <Level name='Product Name' table='product_csv' column='name1' "
           + "uniqueMembers='true'>\n"
            + "<Property name='Product Color' table='product_csv' column='color' />"
            + "</Level>"
           + " </Hierarchy>\n"
           + "</Dimension>\n"
           + "<Measure name='Total' \n"
           + "    column='amount' aggregator='sum'\n"
           + "   formatString='#,###'/>\n"
           + "</Cube>";
    }
}

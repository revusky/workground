/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.test;

import static mondrian.enums.DatabaseProduct.getDatabaseProduct;
import static org.opencube.junit5.TestUtil.assertQueryReturns;
import static org.opencube.junit5.TestUtil.getDialect;
import static org.opencube.junit5.TestUtil.withSchema;

import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.SchemaUtil;
import org.opencube.junit5.TestUtil;
import org.opencube.junit5.context.TestingContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import mondrian.enums.DatabaseProduct;
import mondrian.olap.MondrianProperties;

/**
 * Unit test for the InlineTable element, defining tables whose values are held
 * in the Mondrian schema file, not in the database.
 *
 * @author jhyde
 */
class InlineTableTest {

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTable(TestingContext context) {
        final String cubeName = "Sales_inline";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Dimension name=\"Alternative Promotion\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"promo_id\">\n"
            + "      <InlineTable alias=\"alt_promotion\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"promo_id\" type=\"Numeric\"/>\n"
            + "          <ColumnDef name=\"promo_name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">0</Value>\n"
            + "            <Value column=\"promo_name\">Promo0</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">1</Value>\n"
            + "            <Value column=\"promo_name\">Promo1</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Alternative Promotion\" column=\"promo_id\" nameColumn=\"promo_name\" uniqueMembers=\"true\"/> \n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
        assertQueryReturns(context.createConnection(),
            "select {[Alternative Promotion].[All Alternative Promotions].children} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Alternative Promotion].[Promo0]}\n"
            + "{[Alternative Promotion].[Promo1]}\n"
            + "Row #0: 195,448\n"
            + "Row #0: \n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTableInSharedDim(TestingContext context) {
        final String cubeName = "Sales_inline_shared";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "  <Dimension name=\"Shared Alternative Promotion\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"promo_id\">\n"
            + "      <InlineTable alias=\"alt_promotion\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"promo_id\" type=\"Numeric\"/>\n"
            + "          <ColumnDef name=\"promo_name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">0</Value>\n"
            + "            <Value column=\"promo_name\">First promo</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">1</Value>\n"
            + "            <Value column=\"promo_name\">Second promo</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Alternative Promotion\" column=\"promo_id\" nameColumn=\"promo_name\" uniqueMembers=\"true\"/> \n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "<Cube name=\""
            + cubeName
            + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Shared Alternative Promotion\" source=\"Shared Alternative Promotion\" foreignKey=\"promotion_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
        assertQueryReturns(context.createConnection(),
            "select {[Shared Alternative Promotion].[All Shared Alternative Promotions].children} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Shared Alternative Promotion].[First promo]}\n"
            + "{[Shared Alternative Promotion].[Second promo]}\n"
            + "Row #0: 195,448\n"
            + "Row #0: \n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTableSnowflake(TestingContext context) {
        if (getDatabaseProduct(getDialect(context.createConnection()).getDialectName())
            == DatabaseProduct.INFOBRIGHT)
        {
            // Infobright has a bug joining an inline table. Gives error
            // "Illegal mix of collations (ascii_bin,IMPLICIT) and
            // (utf8_general_ci,COERCIBLE) for operation '='".
            return;
        }
        final String cubeName = "Sales_inline_snowflake";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Dimension name=\"Store\" foreignKeyTable=\"store\" foreignKey=\"store_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "      <Join leftKey=\"store_country\" rightKey=\"nation_name\">\n"
            + "      <Table name=\"store\"/>\n"
            + "        <InlineTable alias=\"nation\">\n"
            + "          <ColumnDefs>\n"
            + "            <ColumnDef name=\"nation_name\" type=\"String\"/>\n"
            + "            <ColumnDef name=\"nation_shortcode\" type=\"String\"/>\n"
            + "          </ColumnDefs>\n"
            + "          <Rows>\n"
            + "            <Row>\n"
            + "              <Value column=\"nation_name\">USA</Value>\n"
            + "              <Value column=\"nation_shortcode\">US</Value>\n"
            + "            </Row>\n"
            + "            <Row>\n"
            + "              <Value column=\"nation_name\">Mexico</Value>\n"
            + "              <Value column=\"nation_shortcode\">MX</Value>\n"
            + "            </Row>\n"
            + "            <Row>\n"
            + "              <Value column=\"nation_name\">Canada</Value>\n"
            + "              <Value column=\"nation_shortcode\">CA</Value>\n"
            + "            </Row>\n"
            + "          </Rows>\n"
            + "        </InlineTable>\n"
            + "      </Join>\n"
            + "      <Level name=\"Store Country\" table=\"nation\" column=\"nation_name\" nameColumn=\"nation_shortcode\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Store State\" table=\"store\" column=\"store_state\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Store City\" table=\"store\" column=\"store_city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Store Name\" table=\"store\" column=\"store_name\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
        assertQueryReturns(context.createConnection(),
            "select {[Store].children} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[CA]}\n"
            + "{[Store].[MX]}\n"
            + "{[Store].[US]}\n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: 266,773\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTableDate(TestingContext context) {
        final String cubeName = "Sales_Inline_Date";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Dimension name=\"Alternative Promotion\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"id\">\n"
            + "        <InlineTable alias=\"inline_promo\">\n"
            + "          <ColumnDefs>\n"
            + "            <ColumnDef name=\"id\" type=\"Numeric\"/>\n"
            + "            <ColumnDef name=\"date\" type=\"Date\"/>\n"
            + "          </ColumnDefs>\n"
            + "          <Rows>\n"
            + "            <Row>\n"
            + "              <Value column=\"id\">1</Value>\n"
            + "              <Value column=\"date\">2008-04-29</Value>\n"
            + "            </Row>\n"
            + "            <Row>\n"
            + "              <Value column=\"id\">2</Value>\n"
            + "              <Value column=\"date\">2007-01-20</Value>\n"
            + "            </Row>\n"
            + "          </Rows>\n"
            + "        </InlineTable>\n"
            + "      <Level name=\"Alternative Promotion\" column=\"id\" nameColumn=\"date\" uniqueMembers=\"true\"/> \n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);

        // With grouping sets, mondrian will join fact table to the inline
        // dimension table, them sum to compute the 'all' value. That semi-joins
        // away too many fact table rows, and the 'all' value comes out too low
        // (zero, in fact). It causes a test exception, but is valid mondrian
        // behavior. (Behavior is unspecified if schema does not have
        // referential integrity.)
        if (MondrianProperties.instance().EnableGroupingSets.get()) {
            return;
        }
        assertQueryReturns(context.createConnection(),
            "select {[Alternative Promotion].Members} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Alternative Promotion].[All Alternative Promotions]}\n"
            + "{[Alternative Promotion].[2008-04-29]}\n"
            + "{[Alternative Promotion].[2007-01-20]}\n"
            + "Row #0: 266,773\n"
            + "Row #0: \n"
            + "Row #0: \n");
    }
}

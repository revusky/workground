/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2019 Hitachi Vantara..  All rights reserved.
 */

package mondrian.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opencube.junit5.TestUtil.withSchemaProcessor;

import org.eclipse.daanse.olap.api.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestingContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import mondrian.olap.Util;
import mondrian.spi.DynamicSchemaProcessor;

/**
 * Unit test DynamicSchemaProcessor. Tests availability of properties that DSP's
 * are called, and used to modify the resulting Mondrian schema
 *
 * @author ngoodman
 */
class DynamicSchemaProcessorTest
{

    public static final String FRAGMENT_ONE =
            "<?xml version=\"1.0\"?>\n"
                    + "<Schema name=\"";

    public static final String FRAGMENT_TWO =
            "\">\n"
                    + "<Cube name=\"Sales\">\n"
                    + " <Table name=\"sales_fact_1997\">"
                    + "   <AggExclude name=\"agg_pl_01_sales_fact_1997\" />"
                    + "   <AggExclude name=\"agg_ll_01_sales_fact_1997\" />"
                    + "   <AggExclude name=\"agg_lc_100_sales_fact_1997\" />"
                    + "   <AggExclude name=\"agg_lc_06_sales_fact_1997\" />"
                    + "   <AggExclude name=\"agg_l_04_sales_fact_1997\" />"
                    + "   <AggExclude name=\"agg_l_03_sales_fact_1997\" />"
                    + "   <AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\" />"
                    + "   <AggExclude name=\"agg_c_10_sales_fact_1997\" />"
                    + "</Table>"
                    + " <Dimension name=\"Fake\"><Hierarchy hasAll=\"true\">"
                    + "  <Level name=\"blah\" column=\"store_id\"/>"
                    + " </Hierarchy></Dimension>"
                    + " <Measure name=\"c\" column=\"store_id\" aggregator=\"count\"/>"
                    + "</Cube>\n" + "</Schema>\n";

    public static final String TEMPLATE_SCHEMA =
            FRAGMENT_ONE
                    + "REPLACEME"
                    + FRAGMENT_TWO;

    /**
     * Tests to make sure that our base DynamicSchemaProcessor works, with no
     * replacement. Does not test Mondrian is able to connect with the schema
     * definition.
     */
    @Test
    void testDSPBasics() {
        DynamicSchemaProcessor dsp = new BaseDsp();
        Util.PropertyList dummy = new Util.PropertyList();
        String processedSchema = "";
        try {
            processedSchema = dsp.processSchema("", dummy);
        } catch (Exception e) {
            // TODO some other assert failure message
            assertEquals(0, 1);
        }
        assertEquals(TEMPLATE_SCHEMA, processedSchema);
    }

    /**
     * Tests to make sure that our base DynamicSchemaProcessor works, and
     * Mondrian is able to parse and connect to FoodMart with it
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testFoodmartDsp(TestingContext context) {
        withSchemaProcessor(context, BaseDsp.class);
        final Connection monConnection = context.createConnection();
        assertEquals(monConnection.getSchema().getName(), "REPLACEME");
    }

    /**
     * Our base, token replacing schema processor.
     *
     * @author ngoodman
     */
    public static class BaseDsp implements DynamicSchemaProcessor {
        // Determines the "cubeName"
        protected String replaceToken = "REPLACEME";

        public BaseDsp() {}

        @Override
		public String processSchema(
                String schemaUrl,
                Util.PropertyList connectInfo)
                throws Exception
        {
            return getSchema();
        }

        public String getSchema() throws Exception {
            return
                    DynamicSchemaProcessorTest.TEMPLATE_SCHEMA.replaceAll(
                            "REPLACEME",
                            this.replaceToken);
        }
    }

    /**
     * Tests to ensure we have access to Connect properies in a DSP
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testProviderTestDSP(TestingContext context) {
        withSchemaProcessor(context, ProviderTestDSP.class);
        Connection monConnection = context.createConnection();
        assertEquals(monConnection.getSchema().getName(), "mondrian");
    }

    /**
     * DSP that checks that replaces the Schema Name with the name of the
     * Provider property
     *
     * @author ngoodman
     *
     */
    public static class ProviderTestDSP extends BaseDsp {
        @Override
		public String processSchema(
                String schemaUrl,
                Util.PropertyList connectInfo)
                throws Exception
        {
            this.replaceToken = connectInfo.get("Provider");
            return getSchema();
        }
    }

    /**
     * Tests to ensure we have access to Connect properies in a DSP
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testDBInfoDSP(TestingContext context) {
        withSchemaProcessor(context, FoodMartCatalogDsp.class);
        final Connection monConnection = context.createConnection();
        assertEquals(monConnection.getSchema().getName(), "FoodmartFoundInCatalogProperty");
    }

    /**
     * Checks to make sure our Catalog property contains our FoodMart.xml VFS
     * URL
     *
     * @author ngoodman
     *
     */
    public static class FoodMartCatalogDsp extends BaseDsp {
        @Override
		public String processSchema(
                String schemaUrl,
                Util.PropertyList connectInfo)
                throws Exception
        {
            if (connectInfo.get("Catalog").indexOf("FoodMart.xml") <= 0) {
                this.replaceToken = "NoFoodmartFoundInCatalogProperty";
            } else {
                this.replaceToken = "FoodmartFoundInCatalogProperty";
            }

            return getSchema();
        }
    }

    /**
     * Tests to ensure we have access to Connect properties in a DSP
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    void testCheckJdbcPropertyDsp(TestingContext context) {
        withSchemaProcessor(context, CheckJdbcPropertyDsp.class);
        Connection monConnection = context.createConnection();
        assertEquals(monConnection.getSchema().getName(), CheckJdbcPropertyDsp.RETURNTRUESTRING);
    }

    /**
     * Ensures we have access to the JDBC URL. Note, since Foodmart can run on
     * multiple databases all we check in schema name is the first four
     * characters (JDBC)
     *
     * @author ngoodman
     *
     */
    public static class CheckJdbcPropertyDsp extends BaseDsp {
        public static String RETURNTRUESTRING = "true";
        public static String RETURNFALSESTRING = "false";

        @Override
		public String processSchema(
                String schemaUrl,
                Util.PropertyList connectInfo)
                throws Exception
        {
            String dataSource = null;
            String jdbc = null;

            dataSource = connectInfo.get("DataSource");
            jdbc = connectInfo.get("Jdbc");

            // If we're using a DataSource we might not get a Jdbc= property
            // trivially return true.
            if (dataSource != null && dataSource.length() > 0) {
                this.replaceToken = RETURNTRUESTRING;
                return getSchema();
            }

            // IF we're here, we don't have a DataSource and
            // our JDBC property should have jdbc: in the URL
            if (jdbc == null || !jdbc.startsWith("jdbc")) {
                this.replaceToken = RETURNFALSESTRING;
                return getSchema();
            } else {
                // If we're here, we have a JDBC url
                this.replaceToken = RETURNTRUESTRING;
                return getSchema();
            }
        }
    }

}

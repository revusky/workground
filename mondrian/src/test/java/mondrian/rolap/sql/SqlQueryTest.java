/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2004-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/
package mondrian.rolap.sql;

import static mondrian.enums.DatabaseProduct.MYSQL;
import static mondrian.enums.DatabaseProduct.POSTGRES;
import static mondrian.enums.DatabaseProduct.getDatabaseProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.opencube.junit5.TestUtil.assertQueryReturns;
import static org.opencube.junit5.TestUtil.getDialect;
import static org.opencube.junit5.TestUtil.withRole;
import static org.opencube.junit5.TestUtil.withSchema;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.daanse.db.dialect.api.Dialect;
import org.eclipse.daanse.db.dialect.db.common.JdbcDialectImpl;
import org.eclipse.daanse.olap.api.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.SchemaUtil;
import org.opencube.junit5.TestUtil;
import org.opencube.junit5.context.BaseTestContext;
import org.opencube.junit5.context.TestingContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;
import org.opencube.junit5.propupdator.SchemaUpdater;

import mondrian.enums.DatabaseProduct;
import mondrian.olap.MondrianProperties;
import mondrian.rolap.BatchTestCase;
import mondrian.test.PropertySaver5;
import mondrian.test.SqlPattern;

/**
 * Test for <code>SqlQuery</code>.
 *
 * @author Thiyagu
 * @since 06-Jun-2007
 */
class SqlQueryTest  extends BatchTestCase {
    //private String origWarnIfNoPatternForDialect;

    private MondrianProperties prop = MondrianProperties.instance();

    private PropertySaver5 propSaver;

    @BeforeEach
    public void beforeEach() {
        propSaver = new PropertySaver5();
        //origWarnIfNoPatternForDialect = prop.WarnIfNoPatternForDialect.get();
    }

    private void prepareContext(Connection connection) {
        // This test warns of missing sql patterns for MYSQL.
        final Dialect dialect = getDialect(connection);
        if (prop.WarnIfNoPatternForDialect.get().equals("ANY")
                || getDatabaseProduct(dialect.getDialectName()) == MYSQL)
        {
        	propSaver.set(prop.WarnIfNoPatternForDialect,
                    getDatabaseProduct(dialect.getDialectName()).toString());
        } else {
            // Do not warn unless the dialect is "MYSQL", or
            // if the test chooses to warn regardless of the dialect.
        	propSaver.set(prop.WarnIfNoPatternForDialect, "NONE");
        }

    }

    @AfterEach
    public void afterEach() {
        propSaver.reset();
        //prop.WarnIfNoPatternForDialect.set(origWarnIfNoPatternForDialect);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testToStringForSingleGroupingSetSql(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        if (!isGroupingSetsSupported(connection)) {
            return;
        }
        for (boolean b : new boolean[]{false, true}) {
            Dialect dialect = getDialect(connection);
            SqlQuery sqlQuery = new SqlQuery(dialect, b);
            sqlQuery.addSelect("c1", null);
            sqlQuery.addSelect("c2", null);
            sqlQuery.addGroupingFunction("gf0");
            sqlQuery.addFromTable("s", "t1", "t1alias", null, null, true);
            sqlQuery.addWhere("a=b");
            ArrayList<String> groupingsetsList = new ArrayList<>();
            groupingsetsList.add("gs1");
            groupingsetsList.add("gs2");
            groupingsetsList.add("gs3");
            sqlQuery.addGroupingSet(groupingsetsList);
            String expected;
            String lineSep = System.getProperty("line.separator");
            if (!b) {
                expected =
                    "select c1 as \"c0\", c2 as \"c1\", grouping(gf0) as \"g0\" "
                    + "from \"s\".\"t1\" =as= \"t1alias\" where a=b "
                    + "group by grouping sets ((gs1, gs2, gs3))";
            } else {
                expected =
                    "select" + lineSep
                    + "    c1 as \"c0\"," + lineSep
                    + "    c2 as \"c1\"," + lineSep
                    + "    grouping(gf0) as \"g0\"" + lineSep
                    + "from" + lineSep
                    + "    \"s\".\"t1\" =as= \"t1alias\"" + lineSep
                    + "where" + lineSep
                    + "    a=b" + lineSep
                    + "group by grouping sets (" + lineSep
                    + "    (gs1, gs2, gs3))";
            }
            assertEquals(
                dialectize(getDatabaseProduct(dialect.getDialectName()), expected),
                dialectize(
                    getDatabaseProduct(sqlQuery.getDialect().getDialectName()),
                    sqlQuery.toString()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testOrderBy(TestingContext context) throws SQLException {
        Connection connection = context.createConnection();
        prepareContext(connection);
        // Test with requireAlias = true
        assertEquals(
            queryUnixString("expr", "alias", true, true, true, true),
            "\norder by\n"
            + "    CASE WHEN alias IS NULL THEN 1 ELSE 0 END, alias ASC");
        // requireAlias = false
        assertEquals(
            "\norder by\n"
            + "    CASE WHEN expr IS NULL THEN 1 ELSE 0 END, expr ASC",
            queryUnixString("expr", "alias", true, true, true, false));
        //  nullable = false
        assertEquals(
            "\norder by\n"
            + "    expr ASC",
            queryUnixString("expr", "alias", true, false, true, false));
        //  ascending=false, collateNullsLast=false
        assertEquals(
            "\norder by\n"
            + "    CASE WHEN alias IS NULL THEN 0 ELSE 1 END, alias DESC",
            queryUnixString("expr", "alias", false, true, false, true));
    }

    /**
     * Builds a SqlQuery with flags set according to params.
     * Uses a Mockito spy to construct a dialect which will give the desired
     * boolean value for reqOrderByAlias.
     */

    private SqlQuery makeTestSqlQuery(
        String expr, String alias, boolean ascending,
        boolean nullable, boolean collateNullsLast, boolean reqOrderByAlias)
    {
        JdbcDialectImpl dialect = spy(new JdbcDialectImplForTest());
        when(dialect.requiresOrderByAlias()).thenReturn(reqOrderByAlias);
        SqlQuery query = new SqlQuery(dialect, true);
        query.addOrderBy(
            expr, alias, ascending, true, nullable, collateNullsLast);
        return query;
    }

    private String queryUnixString(
        String expr, String alias, boolean ascending,
        boolean nullable, boolean collateNullsLast, boolean reqOrderByAlias)
    {
        String sql = makeTestSqlQuery(
            expr, alias, ascending, nullable, collateNullsLast,
            reqOrderByAlias).toString();
        return sql.replaceAll("\\r", "");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testToStringForForcedIndexHint(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        Map<String, String> hints = new HashMap<>();
        hints.put("force_index", "myIndex");

        String unformattedMysql =
            "select c1 as `c0`, c2 as `c1` "
            + "from `s`.`t1` as `t1alias`"
            + " FORCE INDEX (myIndex)"
            + " where a=b";
        String formattedMysql =
            "select\n"
            + "    c1 as `c0`,\n"
            + "    c2 as `c1`\n"
            + "from\n"
            + "    `s`.`t1` as `t1alias` FORCE INDEX (myIndex)\n"
            + "where\n"
            + "    a=b";

        SqlPattern[] unformattedSqlPatterns = {
            new SqlPattern(
                MYSQL,
                unformattedMysql,
                null)};
        SqlPattern[] formattedSqlPatterns = {
            new SqlPattern(
                MYSQL,
                formattedMysql,
                null)};
        for (boolean formatted : new boolean[]{false, true}) {
            Dialect dialect = getDialect(connection);
            SqlQuery sqlQuery = new SqlQuery(dialect, formatted);
            sqlQuery.setAllowHints(true);
            sqlQuery.addSelect("c1", null);
            sqlQuery.addSelect("c2", null);
            sqlQuery.addGroupingFunction("gf0");
            sqlQuery.addFromTable("s", "t1", "t1alias", null, hints, true);
            sqlQuery.addWhere("a=b");
            SqlPattern[] expected;
            if (!formatted) {
                expected = unformattedSqlPatterns;
            } else {
                expected = formattedSqlPatterns;
            }
            assertSqlQueryToStringMatches(connection, sqlQuery, expected);
        }
    }

    private void assertSqlQueryToStringMatches(Connection connection,
        SqlQuery query,
        SqlPattern[] patterns)
    {
        Dialect dialect = getDialect(connection);
        DatabaseProduct d = getDatabaseProduct(dialect.getDialectName());
        boolean patternFound = false;
        for (SqlPattern sqlPattern : patterns) {
            if (!sqlPattern.hasDatabaseProduct(d)) {
                // If the dialect is not one in the pattern set, skip the
                // test. If in the end no pattern is located, print a warning
                // message if required.
                continue;
            }

            patternFound = true;

            String trigger = sqlPattern.getTriggerSql();

            trigger = dialectize(d, trigger);

            assertEquals(
                dialectize(getDatabaseProduct(dialect.getDialectName()), trigger),
                dialectize(
                    getDatabaseProduct(query.getDialect().getDialectName()),
                    query.toString()));
        }

        // Print warning message that no pattern was specified for the current
        // dialect.
        if (!patternFound) {
            String warnDialect =
                MondrianProperties.instance().WarnIfNoPatternForDialect.get();

            if (warnDialect.equals(d.toString())) {
                System.out.println(
                    "[No expected SQL statements found for dialect \""
                    + dialect.toString()
                    + "\" and test not run]");
            }
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testPredicatesAreOptimizedWhenPropertyIsTrue(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        if (prop.ReadAggregates.get() && prop.UseAggregates.get()) {
            // Sql pattner will be different if using aggregate tables.
            // This test cover predicate generation so it's sufficient to
            // only check sql pattern when aggregate tables are not used.
            return;
        }

        String mdx =
            "select {[Time].[1997].[Q1],[Time].[1997].[Q2],"
            + "[Time].[1997].[Q3]} on 0 from sales";

        String accessSql =
            "select `time_by_day`.`the_year` as `c0`, "
            + "`time_by_day`.`quarter` as `c1`, "
            + "sum(`sales_fact_1997`.`unit_sales`) as `m0` "
            + "from `sales_fact_1997` as `sales_fact_1997`, "
            + "`time_by_day` as `time_by_day` "
            + "where `sales_fact_1997`.`time_id` = "
            + "`time_by_day`.`time_id` and "
            + "`time_by_day`.`the_year` = 1997 group by "
            + "`time_by_day`.`the_year`, `time_by_day`.`quarter`";

        String mysqlSql =
            "select "
            + "`time_by_day`.`the_year` as `c0`, `time_by_day`.`quarter` as `c1`, "
            + "sum(`sales_fact_1997`.`unit_sales`) as `m0` "
            + "from "
            + "`sales_fact_1997` as `sales_fact_1997`, `time_by_day` as `time_by_day` "
            + "where "
            + "`sales_fact_1997`.`time_id` = `time_by_day`.`time_id` and "
            + "`time_by_day`.`the_year` = 1997 "
            + "group by `time_by_day`.`the_year`, `time_by_day`.`quarter`";

        SqlPattern[] sqlPatterns = {
            new SqlPattern(
                DatabaseProduct.ACCESS, accessSql, accessSql),
            new SqlPattern(MYSQL, mysqlSql, mysqlSql)};

        assertSqlEqualsOptimzePredicates(context, true, mdx, sqlPatterns);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testTableNameIsIncludedWithParentChildQuery(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        String sql =
            "select `employee`.`employee_id` as `c0`, "
            + "`employee`.`full_name` as `c1`, "
            + "`employee`.`marital_status` as `c2`, "
            + "`employee`.`position_title` as `c3`, "
            + "`employee`.`gender` as `c4`, "
            + "`employee`.`salary` as `c5`, "
            + "`employee`.`education_level` as `c6`, "
            + "`employee`.`management_role` as `c7` "
            + "from `employee` as `employee` "
            + "where `employee`.`supervisor_id` = 0 "
            + "group by `employee`.`employee_id`, `employee`.`full_name`, "
            + "`employee`.`marital_status`, `employee`.`position_title`, "
            + "`employee`.`gender`, `employee`.`salary`,"
            + " `employee`.`education_level`, `employee`.`management_role`"
            + " order by Iif(`employee`.`employee_id` IS NULL, 1, 0),"
            + " `employee`.`employee_id` ASC";

        final String mdx =
            "SELECT "
            + "  GENERATE("
            + "    {[Employees].[All Employees].[Sheri Nowmer]},"
            + "{"
            + "  {([Employees].CURRENTMEMBER)},"
            + "  HEAD("
            + "    ADDCALCULATEDMEMBERS([Employees].CURRENTMEMBER.CHILDREN), 51)"
            + "},"
            + "ALL"
            + ") DIMENSION PROPERTIES PARENT_LEVEL, CHILDREN_CARDINALITY, PARENT_UNIQUE_NAME ON AXIS(0) \n"
            + "FROM [HR]  CELL PROPERTIES VALUE, FORMAT_STRING";
        SqlPattern[] sqlPatterns = {
            new SqlPattern(DatabaseProduct.ACCESS, sql, sql)
        };
        assertQuerySql(context.createConnection(), mdx, sqlPatterns);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testPredicatesAreNotOptimizedWhenPropertyIsFalse(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        if (prop.ReadAggregates.get() && prop.UseAggregates.get()) {
            // Sql pattner will be different if using aggregate tables.
            // This test cover predicate generation so it's sufficient to
            // only check sql pattern when aggregate tables are not used.
            return;
        }

        String mdx =
            "select {[Time].[1997].[Q1],[Time].[1997].[Q2],"
            + "[Time].[1997].[Q3]} on 0 from sales";
        String accessSql =
            "select `time_by_day`.`the_year` as `c0`, "
            + "`time_by_day`.`quarter` as `c1`, "
            + "sum(`sales_fact_1997`.`unit_sales`) as `m0` "
            + "from `sales_fact_1997` as `sales_fact_1997`, "
            + "`time_by_day` as `time_by_day` "
            + "where `sales_fact_1997`.`time_id` = "
            + "`time_by_day`.`time_id` and `time_by_day`.`the_year` "
            + "= 1997 and `time_by_day`.`quarter` in "
            + "('Q1', 'Q2', 'Q3') group by "
            + "`time_by_day`.`the_year`, `time_by_day`.`quarter`";

        String mysqlSql =
            "select "
            + "`time_by_day`.`the_year` as `c0`, `time_by_day`.`quarter` as `c1`, "
            + "sum(`sales_fact_1997`.`unit_sales`) as `m0` "
            + "from "
            + "`sales_fact_1997` as `sales_fact_1997`, `time_by_day` as `time_by_day` "
            + "where "
            + "`sales_fact_1997`.`time_id` = `time_by_day`.`time_id` and "
            + "`time_by_day`.`the_year` = 1997 and "
            + "`time_by_day`.`quarter` in ('Q1', 'Q2', 'Q3') "
            + "group by `time_by_day`.`the_year`, `time_by_day`.`quarter`";

        SqlPattern[] sqlPatterns = {
            new SqlPattern(
                DatabaseProduct.ACCESS, accessSql, accessSql),
            new SqlPattern(MYSQL, mysqlSql, mysqlSql)};

        assertSqlEqualsOptimzePredicates(context, false, mdx, sqlPatterns);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testPredicatesAreOptimizedWhenAllTheMembersAreIncluded(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        if (prop.ReadAggregates.get() && prop.UseAggregates.get()) {
            // Sql pattner will be different if using aggregate tables.
            // This test cover predicate generation so it's sufficient to
            // only check sql pattern when aggregate tables are not used.
            return;
        }

        String mdx =
            "select {[Time].[1997].[Q1],[Time].[1997].[Q2],"
            + "[Time].[1997].[Q3],[Time].[1997].[Q4]} on 0 from sales";

        String accessSql =
            "select `time_by_day`.`the_year` as `c0`, "
            + "`time_by_day`.`quarter` as `c1`, "
            + "sum(`sales_fact_1997`.`unit_sales`) as `m0` from "
            + "`sales_fact_1997` as `sales_fact_1997,` `time_by_day` as `time_by_day`"
            + " where `sales_fact_1997`.`time_id`"
            + " = `time_by_day`.`time_id` and `time_by_day`."
            + "`the_year` = 1997 group by `time_by_day`.`the_year`,"
            + " `time_by_day`.`quarter`";

        String mysqlSql =
            "select "
            + "`time_by_day`.`the_year` as `c0`, `time_by_day`.`quarter` as `c1`, "
            + "sum(`sales_fact_1997`.`unit_sales`) as `m0` "
            + "from "
            + "`sales_fact_1997` as `sales_fact_1997`, `time_by_day` as `time_by_day` "
            + "where "
            + "`sales_fact_1997`.`time_id` = `time_by_day`.`time_id` and "
            + "`time_by_day`.`the_year` = 1997 "
            + "group by `time_by_day`.`the_year`, `time_by_day`.`quarter`";

        SqlPattern[] sqlPatterns = {
            new SqlPattern(
                DatabaseProduct.ACCESS, accessSql, accessSql),
            new SqlPattern(MYSQL, mysqlSql, mysqlSql)};

        assertSqlEqualsOptimzePredicates(context, true, mdx, sqlPatterns);
        assertSqlEqualsOptimzePredicates(context, false, mdx, sqlPatterns);
    }

    private void assertSqlEqualsOptimzePredicates(TestingContext context,
        boolean optimizePredicatesValue,
        String inputMdx,
        SqlPattern[] sqlPatterns)
    {
        boolean intialValueOptimize =
            prop.OptimizePredicates.get();

        try {
        	propSaver.set(prop.OptimizePredicates, optimizePredicatesValue);
            assertQuerySql(context.createConnection(), inputMdx, sqlPatterns);
        } finally {
        	propSaver.set(prop.OptimizePredicates, intialValueOptimize);
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testToStringForGroupingSetSqlWithEmptyGroup(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        if (!isGroupingSetsSupported(connection)) {
            return;
        }
        final Dialect dialect = getDialect(connection);
        for (boolean b : new boolean[]{false, true}) {
            SqlQuery sqlQuery = new SqlQuery(dialect, b);
            sqlQuery.addSelect("c1", null);
            sqlQuery.addSelect("c2", null);
            sqlQuery.addFromTable("s", "t1", "t1alias", null, null, true);
            sqlQuery.addWhere("a=b");
            sqlQuery.addGroupingFunction("g1");
            sqlQuery.addGroupingFunction("g2");
            ArrayList<String> groupingsetsList = new ArrayList<>();
            groupingsetsList.add("gs1");
            groupingsetsList.add("gs2");
            groupingsetsList.add("gs3");
            sqlQuery.addGroupingSet(new ArrayList<String>());
            sqlQuery.addGroupingSet(groupingsetsList);
            String expected;
            if (b) {
                expected =
                    "select\n"
                    + "    c1 as \"c0\",\n"
                    + "    c2 as \"c1\",\n"
                    + "    grouping(g1) as \"g0\",\n"
                    + "    grouping(g2) as \"g1\"\n"
                    + "from\n"
                    + "    \"s\".\"t1\" =as= \"t1alias\"\n"
                    + "where\n"
                    + "    a=b\n"
                    + "group by grouping sets (\n"
                    + "    (),\n"
                    + "    (gs1, gs2, gs3))";
            } else {
                expected =
                    "select c1 as \"c0\", c2 as \"c1\", grouping(g1) as \"g0\", "
                    + "grouping(g2) as \"g1\" from \"s\".\"t1\" =as= \"t1alias\" where a=b "
                    + "group by grouping sets ((), (gs1, gs2, gs3))";
            }
            assertEquals(
                dialectize(getDatabaseProduct(dialect.getDialectName()), expected),
                dialectize(
                    getDatabaseProduct(sqlQuery.getDialect().getDialectName()),
                    sqlQuery.toString()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testToStringForMultipleGroupingSetsSql(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        if (!isGroupingSetsSupported(connection)) {
            return;
        }
        final Dialect dialect = getDialect(connection);
        for (boolean b : new boolean[]{false, true}) {
            SqlQuery sqlQuery = new SqlQuery(dialect, b);
            sqlQuery.addSelect("c0", null);
            sqlQuery.addSelect("c1", null);
            sqlQuery.addSelect("c2", null);
            sqlQuery.addSelect("m1", null, "m1");
            sqlQuery.addFromTable("s", "t1", "t1alias", null, null, true);
            sqlQuery.addWhere("a=b");
            sqlQuery.addGroupingFunction("c0");
            sqlQuery.addGroupingFunction("c1");
            sqlQuery.addGroupingFunction("c2");
            ArrayList<String> groupingSetlist1 = new ArrayList<>();
            groupingSetlist1.add("c0");
            groupingSetlist1.add("c1");
            groupingSetlist1.add("c2");
            sqlQuery.addGroupingSet(groupingSetlist1);
            ArrayList<String> groupingsetsList2 = new ArrayList<>();
            groupingsetsList2.add("c1");
            groupingsetsList2.add("c2");
            sqlQuery.addGroupingSet(groupingsetsList2);
            String expected;
            if (b) {
                expected =
                    "select\n"
                    + "    c0 as \"c0\",\n"
                    + "    c1 as \"c1\",\n"
                    + "    c2 as \"c2\",\n"
                    + "    m1 as \"m1\",\n"
                    + "    grouping(c0) as \"g0\",\n"
                    + "    grouping(c1) as \"g1\",\n"
                    + "    grouping(c2) as \"g2\"\n"
                    + "from\n"
                    + "    \"s\".\"t1\" =as= \"t1alias\"\n"
                    + "where\n"
                    + "    a=b\n"
                    + "group by grouping sets (\n"
                    + "    (c0, c1, c2),\n"
                    + "    (c1, c2))";
            } else {
                expected =
                    "select c0 as \"c0\", c1 as \"c1\", c2 as \"c2\", m1 as \"m1\", "
                    + "grouping(c0) as \"g0\", grouping(c1) as \"g1\", grouping(c2) as \"g2\" "
                    + "from \"s\".\"t1\" =as= \"t1alias\" where a=b "
                    + "group by grouping sets ((c0, c1, c2), (c1, c2))";
            }
            assertEquals(
                dialectize(getDatabaseProduct(dialect.getDialectName()), expected),
                dialectize(
                    getDatabaseProduct(sqlQuery.getDialect().getDialectName()),
                    sqlQuery.toString()));
        }
    }

    /**
     * Verifies that the correct SQL string is generated for literals of
     * SQL type "double".
     *
     * <p>Mondrian only generates SQL DOUBLE values in a special format for
     * LucidDB; therefore, this test is a no-op on other databases.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testDoubleInList(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        final Dialect dialect = getDialect(context.createConnection());
        if (getDatabaseProduct(dialect.getDialectName()) != DatabaseProduct.LUCIDDB) {
            return;
        }

        propSaver.set(prop.IgnoreInvalidMembers, true);
        propSaver.set(prop.IgnoreInvalidMembersDuringQuery, true);

        // assertQuerySql(testContext, query, patterns);

        // Test when the double value itself cotnains "E".
        String dimensionSqlExpression =
            "cast(cast(\"salary\" as double)*cast(1000.0 as double)/cast(3.1234567890123456 as double) as double)\n";

        String cubeFirstPart =
            "<Cube name=\"Sales 3\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <Dimension name=\"StoreEmpSalary\" foreignKey=\"store_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Salary\" primaryKey=\"store_id\">\n"
            + "      <Table name=\"employee\"/>\n"
            + "      <Level name=\"Salary\" column=\"salary\" type=\"Numeric\" uniqueMembers=\"true\" approxRowCount=\"10000000\">\n"
            + "        <KeyExpression>\n"
            + "          <SQL dialect=\"luciddb\">\n";

        String cubeSecondPart =
            "          </SQL>\n"
            + "        </KeyExpression>\n"
            + "      </Level>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"/>\n"
            + "</Cube>";

        String cube =
            cubeFirstPart
            + dimensionSqlExpression
            + cubeSecondPart;

        String query =
            "select "
            + "{[StoreEmpSalary].[All Salary].[6403.162057613773],[StoreEmpSalary].[All Salary].[1184584.980658548],[StoreEmpSalary].[All Salary].[1344664.0320988924], "
            + " [StoreEmpSalary].[All Salary].[1376679.8423869612],[StoreEmpSalary].[All Salary].[1408695.65267503],[StoreEmpSalary].[All Salary].[1440711.462963099], "
            + " [StoreEmpSalary].[All Salary].[1456719.3681071333],[StoreEmpSalary].[All Salary].[1472727.2732511677],[StoreEmpSalary].[All Salary].[1488735.1783952022], "
            + " [StoreEmpSalary].[All Salary].[1504743.0835392366],[StoreEmpSalary].[All Salary].[1536758.8938273056],[StoreEmpSalary].[All Salary].[1600790.5144034433], "
            + " [StoreEmpSalary].[All Salary].[1664822.134979581],[StoreEmpSalary].[All Salary].[1888932.806996063],[StoreEmpSalary].[All Salary].[1952964.4275722008], "
            + " [StoreEmpSalary].[All Salary].[1984980.2378602696],[StoreEmpSalary].[All Salary].[2049011.8584364073],[StoreEmpSalary].[All Salary].[2081027.6687244761], "
            + " [StoreEmpSalary].[All Salary].[2113043.479012545],[StoreEmpSalary].[All Salary].[2145059.289300614],[StoreEmpSalary].[All Salary].[2.5612648230455093E7]} "
            + " on rows, {[Measures].[Store Cost]} on columns from [Sales 3]";

        // Notice there are a few members missing in this sql. This is a LucidDB
        // bug wrt comparison involving "approximate number literals".
        // Mondrian properties "IgnoreInvalidMembers" and
        // "IgnoreInvalidMembersDuringQuery" are required for this MDX to
        // finish, even though the the generated sql(below) and the final result
        // are both incorrect.
        String loadSqlLucidDB =
            "select cast(cast(\"salary\" as double)*cast(1000.0 as double)/cast(3.1234567890123456 as double) as double) as \"c0\", "
            + "sum(\"sales_fact_1997\".\"store_cost\") as \"m0\" "
            + "from \"employee\" as \"employee\", \"sales_fact_1997\" as \"sales_fact_1997\" "
            + "where \"sales_fact_1997\".\"store_id\" = \"employee\".\"store_id\" and "
            + "cast(cast(\"salary\" as double)*cast(1000.0 as double)/cast(3.1234567890123456 as double) as double) in "
            + "(6403.162057613773E0, 1184584.980658548E0, 1344664.0320988924E0, "
            + "1376679.8423869612E0, 1408695.65267503E0, 1440711.462963099E0, "
            + "1456719.3681071333E0, 1488735.1783952022E0, "
            + "1504743.0835392366E0, 1536758.8938273056E0, "
            + "1664822.134979581E0, 1888932.806996063E0, 1952964.4275722008E0, "
            + "1984980.2378602696E0, 2049011.8584364073E0, "
            + "2113043.479012545E0, 2145059.289300614E0, 2.5612648230455093E7) "
            + "group by cast(cast(\"salary\" as double)*cast(1000.0 as double)/cast(3.1234567890123456 as double) as double)";

        SqlPattern[] patterns = {
            new SqlPattern(
                DatabaseProduct.LUCIDDB,
                loadSqlLucidDB,
                loadSqlLucidDB)
        };

        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null,
                cube,
                null,
                null,
                null,
                null);
        withSchema(context, schema);
        assertQuerySql(connection, query, patterns);
    }

    /**
     * Testcase for
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-457">bug MONDRIAN-457,
     * "Strange SQL condition appears when executing query"</a>. The fix
     * implemented MatchType.EXACT_SCHEMA, which only
     * queries known schema objects. This prevents SQL such as
     * "UPPER(`store`.`store_country`) = UPPER('Time.Weekly')" from being
     * generated.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testInvalidSqlMemberLookup(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        String sqlMySql =
            "select `store`.`store_type` as `c0` from `store` as `store` "
            + "where UPPER(`store`.`store_type`) = UPPER('Time.Weekly') "
            + "group by `store`.`store_type` "
            + "order by ISNULL(`store`.`store_type`), `store`.`store_type` ASC";
        String sqlOracle =
            "select \"store\".\"store_type\" as \"c0\" from \"store\" \"store\" "
            + "where UPPER(\"store\".\"store_type\") = UPPER('Time.Weekly') "
            + "group by \"store\".\"store_type\" "
            + "order by \"store\".\"store_type\" ASC";

        SqlPattern[] patterns = {
            new SqlPattern(MYSQL, sqlMySql, sqlMySql),
            new SqlPattern(
                DatabaseProduct.ORACLE, sqlOracle, sqlOracle),
        };

        assertNoQuerySql(connection,
            "select {[Time.Weekly].[All Time.Weeklys]} ON COLUMNS from [Sales]",
            patterns);
    }

    /**
     * This test makes sure that a level which specifies an
     * approxRowCount property prevents Mondrian from executing a
     * count() sql query. It was discovered in bug MONDRIAN-711
     * that the aggregate tables predicates optimization code was
     * not considering the approxRowCount property. It is fixed and
     * this test will ensure it won't happen again.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testApproxRowCountOverridesCount(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        final String cubeSchema =
            "<Cube name=\"ApproxTest\"> \n"
            + "  <Table name=\"sales_fact_1997\"/> \n"
            + "  <Dimension name=\"Gender\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Gender\" primaryKey=\"customer_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\" approxRowCount=\"2\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"/> \n"
            + "</Cube>";

        final String mdxQuery =
            "SELECT {[Gender].[Gender].Members} ON ROWS, {[Measures].[Unit Sales]} ON COLUMNS FROM [ApproxTest]";

        final String forbiddenSqlOracle =
            "select count(distinct \"customer\".\"gender\") as \"c0\" from \"customer\" \"customer\"";

        final String forbiddenSqlMysql =
            "select count(distinct `customer`.`gender`) as `c0` from `customer` `customer`;";

        SqlPattern[] patterns = {
            new SqlPattern(
                DatabaseProduct.ORACLE, forbiddenSqlOracle, null),
            new SqlPattern(
                MYSQL, forbiddenSqlMysql, null)
        };

        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null,
                cubeSchema,
                null,
                null,
                null,
                null);
        withSchema(context, schema);
        assertQuerySqlOrNot(
        	context.createConnection(),
            mdxQuery,
            patterns,
            true,
            true,
            true);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testLimitedRollupMemberRetrievableFromCache(TestingContext context) throws Exception {
        Connection connection = context.createConnection();
        prepareContext(connection);
        final String mdx =
            "select NON EMPTY { [Store].[Store].[Store State].members } on 0 from [Sales]";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null, null, null, null, null,
                " <Role name='justCA'>\n"
                + " <SchemaGrant access='all'>\n"
                + " <CubeGrant cube='Sales' access='all'>\n"
                + " <HierarchyGrant hierarchy='[Store]' access='custom' rollupPolicy='partial'>\n"
                + " <MemberGrant member='[Store].[USA].[CA]' access='all'/>\n"
                + " </HierarchyGrant>\n"
                + " </CubeGrant>\n"
                + " </SchemaGrant>\n"
                + " </Role>\n");
        withSchema(context, schema);
        withRole(context,"justCA");

        String pgSql =
            "select \"store\".\"store_country\" as \"c0\","
            + " \"store\".\"store_state\" as \"c1\""
            + " from \"sales_fact_1997\" as \"sales_fact_1997\","
            + " \"store\" as \"store\" "
            + "where (\"store\".\"store_country\" = 'USA') "
            + "and (\"store\".\"store_state\" = 'CA') "
            + "and \"sales_fact_1997\".\"store_id\" = \"store\".\"store_id\" "
            + "group by \"store\".\"store_country\", \"store\".\"store_state\" "
            + "order by \"store\".\"store_country\" ASC NULLS LAST,"
            + " \"store\".\"store_state\" ASC NULLS LAST";
        SqlPattern pgPattern =
            new SqlPattern(POSTGRES, pgSql, pgSql.length());
        String mySql =
            "select `store`.`store_country` as `c0`,"
            + " `store`.`store_state` as `c1`"
            + " from `store` as `store`, `sales_fact_1997` as `sales_fact_1997` "
            + "where `sales_fact_1997`.`store_id` = `store`.`store_id` "
            + "and `store`.`store_country` = 'USA' "
            + "and `store`.`store_state` = 'CA' "
            + "group by `store`.`store_country`, `store`.`store_state` "
            + "order by ISNULL(`store`.`store_country`) ASC,"
            + " `store`.`store_country` ASC,"
            + " ISNULL(`store`.`store_state`) ASC, `store`.`store_state` ASC";
        SqlPattern myPattern = new SqlPattern(MYSQL, mySql, mySql.length());
        SqlPattern[] patterns = {pgPattern, myPattern};
        connection = context.createConnection();
        executeQuery(mdx, connection);
        assertQuerySqlOrNot(connection, mdx, patterns, true, false, false);
    }

    /**
     * This is a test for
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-1869">MONDRIAN-1869</a>
     *
     * <p>Avg Aggregates need to be computed in SQL to get correct values.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    void testAvgAggregator(TestingContext context) {
        Connection connection = context.createConnection();
        prepareContext(connection);
        propSaver.set(propSaver.properties.GenerateFormattedSql, true);
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            null,
            " <Measure name=\"Avg Sales\" column=\"unit_sales\" aggregator=\"avg\"\n"
            + " formatString=\"#.###\"/>",
            null,
            null));
        String mdx = "select measures.[avg sales] on 0 from sales"
                       + " where { time.[1997].q1, time.[1997].q2.[4] }";
        assertQueryReturns(context.createConnection(),
            mdx,
            "Axis #0:\n"
            + "{[Time].[1997].[Q1]}\n"
            + "{[Time].[1997].[Q2].[4]}\n"
            + "Axis #1:\n"
            + "{[Measures].[Avg Sales]}\n"
            + "Row #0: 3.069\n");
        String sql =
            "select\n"
            + "    avg(`sales_fact_1997`.`unit_sales`) as `m0`\n"
            + "from\n"
            + "    `sales_fact_1997` as `sales_fact_1997`,\n"
            + "    `time_by_day` as `time_by_day`\n"
            + "where\n"
            + "    `sales_fact_1997`.`time_id` = `time_by_day`.`time_id`\n"
            + "and\n"
            + "    ((`time_by_day`.`quarter` = 'Q1' and `time_by_day`.`the_year` = 1997) "
            + "or (`time_by_day`.`month_of_year` = 4 and `time_by_day`.`quarter` = 'Q2' "
            + "and `time_by_day`.`the_year` = 1997))";
        SqlPattern mySqlPattern =
            new SqlPattern(DatabaseProduct.MYSQL, sql, sql.length());
        assertQuerySql(context.createConnection(), mdx, new SqlPattern[]{mySqlPattern});
    }

    private boolean isGroupingSetsSupported(Connection connection) {
        return MondrianProperties.instance().EnableGroupingSets.get()
                && getDialect(connection).supportsGroupingSets();
    }

    public class JdbcDialectImplForTest extends JdbcDialectImpl{

        @Override
        protected boolean isSupportedProduct(String productName, String productVersion) {
            return true;
        }

        @Override
        public String getDialectName() {
            return null;
        }
    }
}

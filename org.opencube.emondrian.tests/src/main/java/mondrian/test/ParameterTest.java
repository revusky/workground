/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2003-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara
// All Rights Reserved.
*/
package mondrian.test;

import mondrian.olap.*;
import mondrian.rolap.RolapConnectionProperties;
import org.eigenbase.util.property.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.olap4j.impl.Olap4jUtil;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.SchemaUtil;
import org.opencube.junit5.TestUtil;
import org.opencube.junit5.context.Context;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

import static mondrian.test.FoodMartTestCase.executeExpr;
import static org.junit.jupiter.api.Assertions.*;
import static org.opencube.junit5.TestUtil.*;

/**
 * A <code>ParameterTest</code> is a test suite for functionality relating to
 * parameters.
 *
 * @author jhyde
 * @since Feb 13, 2003
 */
public class ParameterTest {

    // -- Helper methods ----------

    private void assertSetPropertyFails(Connection connection, String propName, String scope) {
        Query q = connection.parseQuery("select from [Sales]");
        try {
            q.setParameter(propName, "foo");
            fail(
                "expected exception, trying to set "
                + "non-overrideable property '" + propName + "'");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf(
                "Parameter '" + propName + "' (defined at '"
                + scope + "' scope) is not modifiable") >= 0);
        }
    }

    // -- Tests --------------

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testChangeable(Context context) {
        // jpivot needs to set a parameters value before the query is executed
        String mdx =
            "select {Parameter(\"Foo\",[Time],[Time].[1997],\"Foo\")} "
            + "ON COLUMNS from [Sales]";
        Query query = context.createConnection().parseQuery(mdx);
        SchemaReader sr = query.getSchemaReader(false).withLocus();
        Member m =
            sr.getMemberByUniqueName(
                Id.Segment.toList("Time", "1997", "Q2", "5"), true);
        Parameter p = sr.getParameter("Foo");
        p.setValue(m);
        assertEquals(m, p.getValue());
        query.resolve();
        p.setValue(m);
        assertEquals(m, p.getValue());
        mdx = query.toString();
        assertEqualsVerbose(
            "select {Parameter(\"Foo\", [Time], [Time].[1997].[Q2].[5], \"Foo\")} ON COLUMNS\n"
            + "from [Sales]\n",
            mdx);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterInFormatString(Context context) {
        assertQueryReturns(context.createConnection(),
            "with member [Measures].[X] as '[Measures].[Store Sales]',\n"
            + "format_string = Parameter(\"fmtstrpara\", STRING, \"#\")\n"
            + "select {[Measures].[X]} ON COLUMNS\n"
            + "from [Sales]",

            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[X]}\n"
            + "Row #0: 565238\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterInFormatString_Bug1584439(Context context) {
        String queryString =
            "with member [Measures].[X] as '[Measures].[Store Sales]',\n"
            + "format_string = Parameter(\"fmtstrpara\", STRING, \"#\")\n"
            + "select {[Measures].[X]} ON COLUMNS\n"
            + "from [Sales]";

        // this used to crash
        Connection connection = context.createConnection();
        Query query = connection.parseQuery(queryString);
        query.toString();
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterOnAxis(Context context) {
        assertQueryReturns(context.createConnection(),
            "select {[Measures].[Unit Sales]} on rows,\n"
            + " {Parameter(\"GenderParam\",[Gender],[Gender].[M],\"Which gender?\")} on columns\n"
            + "from Sales",

            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Gender].[M]}\n"
            + "Axis #2:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Row #0: 135,215\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testNumericParameter(Context context) {
        String s =
            executeExpr("Parameter(\"N\",NUMERIC,2+3,\"A numeric parameter\")");
        assertEquals("5", s);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testStringParameter(Context context) {
        String s =
            executeExpr(
                "Parameter(\"S\",STRING,\"x\" || \"y\","
                + "\"A string parameter\")");
        assertEquals("xy", s);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testStringParameterNull(Context context) {
        Connection connection = context.createConnection();
        assertParameterizedExprReturns(connection,
            "Parameter('foo', STRING, 'default')",
            "xxx",
            "foo", "xxx");
        // explicitly set parameter to null and you should not get default value
        assertParameterizedExprReturns(connection,
            "Parameter('foo', STRING, 'default')",
            "",
            "foo", null);
        assertParameterizedExprReturns(connection,
            "Len(Parameter('foo', STRING, 'default'))",
            "0",
            "foo", null);
        assertParameterizedExprReturns(connection,
            "Parameter('foo', STRING, 'default') = 'default'",
            "false",
            "foo", null);
        assertParameterizedExprReturns(connection,
            "Parameter('foo', STRING, 'default') = ''",
            "false",
            "foo", null);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testNumericParameterNull(Context context) {
        Connection connection = context.createConnection();
        assertParameterizedExprReturns(connection,
            "Parameter('foo', NUMERIC, 12.3)",
            "234",
            "foo", 234);
        // explicitly set parameter to null and you should not get default value
        assertParameterizedExprReturns(connection,
            "Parameter('foo', NUMERIC, 12.3)",
            "",
            "foo", null);
        assertParameterizedExprReturns(connection,
            "Parameter('foo', NUMERIC, 12.3) * 10",
            "",
            "foo", null);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testMemberParameterNull(Context context) {
        Connection connection = context.createConnection();
        assertParameterizedExprReturns(connection,
            "Parameter('foo', [Gender], [Gender].[F]).Name",
            "M",
            "foo", "[Gender].[M]");
        // explicitly set parameter to null and you should not get default value
        assertParameterizedExprReturns(connection,
            "Parameter('foo', [Gender], [Gender].[F]).Name",
            "#null",
            "foo", null);
        assertParameterizedExprReturns(connection,
            "Parameter('foo', [Gender], [Gender].[F]).Hierarchy.Name",
            "Gender",
            "foo", null);
        assertParameterizedExprReturns(connection,
            "Parameter('foo', [Gender], [Gender].[F]) is null",
            "true",
            "foo", null);
        assertParameterizedExprReturns(connection,
            "Parameter('foo', [Gender], [Gender].[F]) is [Gender].Parent",
            "true",
            "foo", null);

        // assign null then assign something else
        assertParameterizedExprReturns(connection,
            "Parameter('foo', [Gender], [Gender].[F]).Name",
            "M",
            "foo", null,
            "foo", "[Gender].[All Gender].[M]");
    }

    /**
     * Test case for bug
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-745">MONDRIAN-745,
     * "NullPointerException when passing in null param value"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testNullStrToMember(Context context) {
        Connection connection = context.createConnection();
        Query query = connection.parseQuery(
            "select NON EMPTY {[Time].[1997]} ON COLUMNS, "
            + "NON EMPTY {StrToMember(Parameter(\"sProduct\", STRING, \"[Gender].[Gender].[F]\"))} ON ROWS "
            + "from [Sales]");

        // Execute #1: Parameter unset
        Parameter[] parameters = query.getParameters();
        final Parameter parameter0 = parameters[0];
        assertFalse(parameter0.isSet());
        // ideally, parameter's default value would be available before
        // execution; but it is what it is
        assertNull(parameter0.getValue());
        Result result = connection.execute(query);
        assertEquals("[Gender].[Gender].[F]", parameter0.getValue());
        final String expected =
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Time].[1997]}\n"
            + "Axis #2:\n"
            + "{[Gender].[F]}\n"
            + "Row #0: 131,558\n";
        assertEqualsVerbose(expected, TestUtil.toString(result));

        // Execute #2: Parameter set to null
        assertFalse(parameter0.isSet());
        parameter0.setValue(null);
        assertTrue(parameter0.isSet());
        assertEquals(null, parameter0.getValue());
        Throwable throwable;
        try {
            result = connection.execute(query);
            Util.discard(result);
            throwable = null;
        } catch (Throwable e) {
            throwable = e;
        }
        checkThrowable(
            throwable,
            "An MDX expression was expected. An empty expression was specified.");

        // Execute #3: Parameter unset, reverts to default value
        assertTrue(parameter0.isSet());
        parameter0.unsetValue();
        assertFalse(parameter0.isSet());
        // ideally, parameter's default value would be available before
        // execution; but it is what it is
        assertNull(parameter0.getValue());
        result = connection.execute(query);
        assertEquals("[Gender].[Gender].[F]", parameter0.getValue());
        assertEqualsVerbose(expected, TestUtil.toString(result));
        assertFalse(parameter0.isSet());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSetUnsetParameter(Context context) {
        Connection connection = context.createConnection();
        Query query = connection.parseQuery(
            "with member [Measures].[Foo] as\n"
            + " len(Parameter(\"sProduct\", STRING, \"foobar\"))\n"
            + "select {[Measures].[Foo]} ON COLUMNS\n"
            + "from [Sales]");
        Parameter[] parameters = query.getParameters();
        final Parameter parameter0 = parameters[0];
        assertFalse(parameter0.isSet());
        if (new Random().nextBoolean()) {
            // harmless to unset a parameter which is unset
            parameter0.unsetValue();
        }
        final String expect6 =
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Foo]}\n"
            + "Row #0: 6\n";
        final String expect0 =
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Foo]}\n"
            + "Row #0: 0\n";
        final String expect3 =
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Foo]}\n"
            + "Row #0: 3\n";

        // before parameter is set, should get len of default value, viz 6
        Result result = connection.execute(query);
        assertEqualsVerbose(expect6, TestUtil.toString(result));

        // after parameter is set to null, should get len of null, viz 0
        parameter0.setValue(null);
        assertTrue(parameter0.isSet());
        result = connection.execute(query);
        assertEqualsVerbose(expect0, TestUtil.toString(result));
        assertTrue(parameter0.isSet());

        // after parameter is set to "foo", should get len of foo, viz 3
        parameter0.setValue("foo");
        assertTrue(parameter0.isSet());
        result = connection.execute(query);
        assertEqualsVerbose(expect3, TestUtil.toString(result));
        assertTrue(parameter0.isSet());

        // after unset, should get len of default value, viz 6
        parameter0.unsetValue();
        result = connection.execute(query);
        assertEqualsVerbose(expect6, TestUtil.toString(result));
        assertFalse(parameter0.isSet());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testNumericParameterStringValueFails(Context context) {
        Connection connection = context.createConnection();
        assertExprThrows(connection,
            "Parameter(\"S\",NUMERIC,\"x\" || \"y\",\"A string parameter\")",
            "java.lang.NumberFormatException: For input string: \"xy\"");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterDimensionWithTwoHierarchies(Context context) {
        Connection connection = context.createConnection();
        assertExprReturns(connection,
            "Parameter(\"Foo\",[Time],[Time].[1997],\"Foo\").Name", "1997");
        assertExprReturns(connection,
            "Parameter(\"Foo\",[Time],[Time].[1997].[Q2].[5],\"Foo\").Name",
            "5");
        // wrong dimension
        assertExprThrows(connection,
            "Parameter(\"Foo\",[Time],[Product].[All Products],\"Foo\").Name",
            "Default value of parameter 'Foo' is not consistent with the parameter type 'MemberType<hierarchy=[Time]>");
        // non-existent member
        assertExprThrows(connection,
            "Parameter(\"Foo\",[Time],[Time].[1997].[Q5],\"Foo\").Name",
            "MDX object '[Time].[1997].[Q5]' not found in cube 'Sales'");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterDimensionWithOneHierarchy(Context context) {
        Connection connection = context.createConnection();
      assertExprReturns(connection,
          "Parameter(\"Foo\",[Store],[Store].[USA],\"Foo\").Name", "USA");
      assertExprReturns(connection,
          "Parameter(\"Foo\",[Store],[Store].[USA].[OR].[Portland],\"Foo\").Name",
          "Portland");
      // wrong dimension
      assertExprThrows(connection,
          "Parameter(\"Foo\",[Store],[Product].[All Products],\"Foo\").Name",
          "Default value of parameter 'Foo' is not consistent with the parameter type 'MemberType<hierarchy=[Store]>");
      // non-existent member
      assertExprThrows(connection,
          "Parameter(\"Foo\",[Store],[Store].[USA].[NY],\"Foo\").Name",
          "MDX object '[Store].[USA].[NY]' not found in cube 'Sales'");
  }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterHierarchy(Context context) {
        Connection connection = context.createConnection();
        assertExprReturns(connection,
            "Parameter(\"Foo\", [Time.Weekly], [Time.Weekly].[1997].[40],\"Foo\").Name",
            "40");
        // right dimension, wrong hierarchy
        final String levelName =
            MondrianProperties.instance().SsasCompatibleNaming.get()
                ? "[Time].[Weekly]"
                : "[Time.Weekly]";
        assertExprThrows(connection,
            "Parameter(\"Foo\",[Time.Weekly],[Time].[1997].[Q1],\"Foo\").Name",
            "Default value of parameter 'Foo' is not consistent with the parameter type 'MemberType<hierarchy="
            + levelName
            + ">");
        // wrong dimension
        assertExprThrows(connection,
            "Parameter(\"Foo\",[Time.Weekly],[Product].[All Products],\"Foo\").Name",
            "Default value of parameter 'Foo' is not consistent with the parameter type 'MemberType<hierarchy="
            + levelName
            + ">");
        // garbage
        assertExprThrows(connection,
            "Parameter(\"Foo\",[Time.Weekly],[Widget].[All Widgets],\"Foo\").Name",
            "MDX object '[Widget].[All Widgets]' not found in cube 'Sales'");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterLevel(Context context) {
        Connection connection = context.createConnection();
        assertExprReturns(connection,
            "Parameter(\"Foo\",[Time].[Quarter], [Time].[1997].[Q3], \"Foo\").Name",
            "Q3");
        assertExprThrows(connection,
            "Parameter(\"Foo\",[Time].[Quarter], [Time].[1997].[Q3].[8], \"Foo\").Name",
            "Default value of parameter 'Foo' is not consistent with the parameter type 'MemberType<level=[Time].[Quarter]>");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterMemberFails(Context context) {
        // type of a param can be dimension, hierarchy, level but not member
        assertExprThrows(context.createConnection(),
            "Parameter(\"Foo\",[Time].[1997].[Q2],[Time].[1997],\"Foo\")",
            "Invalid type for parameter 'Foo'; expecting NUMERIC, STRING or a hierarchy");
    }

    /**
     * Tests that member parameter fails validation if the level name is
     * invalid.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterMemberFailsBadLevel(Context context) {
        Connection connection = context.createConnection();
        assertExprThrows(connection,
            "Parameter(\"Foo\", [Customers].[State], [Customers].[USA].[CA], \"\")",
            "MDX object '[Customers].[State]' not found in cube 'Sales'");
        assertExprReturns(connection,
            "Parameter(\"Foo\", [Customers].[State Province], [Customers].[USA].[CA], \"\")",
            "74,748");
    }

    /**
     * Tests that a dimension name can be used as the default value of a
     * member-valued parameter. It is interpreted to mean the default value of
     * that dimension.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterMemberDefaultValue(Context context) {
        // "[Time]" is shorthand for "[Time].CurrentMember"
        Connection connection = context.createConnection();
        assertExprReturns(connection,
            "Parameter(\"Foo\", [Time], [Time].[Time], \"Description\").UniqueName",
            "[Time].[1997]");

        assertExprReturns(connection,
            "Parameter(\"Foo\", [Time], [Time].[Time].Children.Item(2), \"Description\").UniqueName",
            "[Time].[1997].[Q3]");
    }

    /**
     * Non-trivial default value. Example shows how to set the parameter to
     * the last month that someone in Bellflower, CA had a good beer. You can
     * use it to solve the more common problem "How do I automatically set the
     * time dimension to the latest date for which there are transactions?".
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterMemberDefaultValue2(Context context) {
        Connection connection = context.createConnection();
        assertQueryReturns(connection,
            "select [Measures].[Unit Sales] on 0,\n"
            + " [Product].Children on 1\n"
            + "from [Sales]"
            + "where Parameter(\n"
            + "  \"Foo\",\n"
            + "   [Time].[Time],\n"
            + "   Tail(\n"
            + "     {\n"
            + "       [Time].[Time],\n"
            + "       Filter(\n"
            + "         [Time].[Month].Members,\n"
            + "         0 < ([Customers].[USA].[CA].[Bellflower],\n"
            + "           [Product].[Drink].[Alcoholic Beverages].[Beer and Wine].[Beer].[Good]))\n"
            + "     },\n"
            + "     1),\n"
            + "   \"Description\")",
            "Axis #0:\n"
            + "{[Time].[1997].[Q4].[11]}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product].[Drink]}\n"
            + "{[Product].[Food]}\n"
            + "{[Product].[Non-Consumable]}\n"
            + "Row #0: 2,344\n"
            + "Row #1: 18,278\n"
            + "Row #2: 4,648\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterWithExpressionForHierarchyFails(Context context) {
        Connection connection = context.createConnection();
        assertExprThrows(connection,
            "Parameter(\"Foo\",[Gender].DefaultMember.Hierarchy,[Gender].[M],\"Foo\")",
            "Invalid parameter 'Foo'. Type must be a NUMERIC, STRING, or a dimension, hierarchy or level");
    }

    /**
     * Tests a parameter derived from another parameter. OK as long as it is
     * not cyclic.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testDerivedParameter(Context context) {
        Connection connection = context.createConnection();
        assertExprReturns(connection,
            "Parameter(\"X\", NUMERIC, Parameter(\"Y\", NUMERIC, 1) + 2)",
            "3");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterInSlicer(Context context) {
        Connection connection = context.createConnection();
        assertQueryReturns(connection,
            "select {[Measures].[Unit Sales]} on rows,\n"
            + " {[Marital Status].children} on columns\n"
            + "from Sales where Parameter(\"GenderParam\",[Gender],[Gender].[M],\"Which gender?\")",
            "Axis #0:\n"
            + "{[Gender].[M]}\n"
            + "Axis #1:\n"
            + "{[Marital Status].[M]}\n"
            + "{[Marital Status].[S]}\n"
            + "Axis #2:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Row #0: 66,460\n"
            + "Row #0: 68,755\n");
    }

    /**
     * Parameter in slicer and expression on columns axis are both of [Gender]
     * hierarchy, which is illegal.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void _testParameterDuplicateDimensionFails(Context context) {
        Connection connection = context.createConnection();
        assertQueryThrows(connection,
            "select {[Measures].[Unit Sales]} on rows,\n"
            + " {[Gender].[F]} on columns\n"
            + "from Sales where Parameter(\"GenderParam\",[Gender],[Gender].[M],\"Which gender?\")",
            "Invalid hierarchy for parameter 'GenderParam'");
    }

    /** Mondrian can not handle forward references */
    @Test
    public void dontTestParamRef() {
        String s = executeExpr(
            "Parameter(\"X\",STRING,\"x\",\"A string\") || "
            + "ParamRef(\"Y\") || "
            + "\".\" ||"
            + "ParamRef(\"X\") || "
            + "Parameter(\"Y\",STRING,\"y\" || \"Y\",\"Other string\")");
        assertEquals("xyY.xyY", s);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParamRefWithoutParamFails(Context context) {
        assertExprThrows(context.createConnection(), "ParamRef(\"Y\")", "Unknown parameter 'Y'");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParamDefinedTwiceFails(Context context) {
        Connection connection = context.createConnection();
        assertQueryThrows(connection,
            "select {[Measures].[Unit Sales]} on rows,\n"
            + " {Parameter(\"P\",[Gender],[Gender].[M],\"Which gender?\"),\n"
            + "  Parameter(\"P\",[Gender],[Gender].[F],\"Which gender?\")} on columns\n"
            + "from Sales", "Parameter 'P' is defined more than once");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParamBadTypeFails(Context context) {
        Connection connection = context.createConnection();
        assertExprThrows(connection,
            "Parameter(\"P\", 5)",
            "No function matches signature 'Parameter(<String>, <Numeric Expression>)'");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParamCyclicOk(Context context) {
        Connection connection = context.createConnection();
        assertExprReturns(connection,
            "Parameter(\"P\", NUMERIC, ParamRef(\"Q\") + 1) + "
            + "Parameter(\"Q\", NUMERIC, Iif(1 = 0, ParamRef(\"P\"), 2))",
            "5");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParamCyclicFails(Context context) {
        assertExprThrows(context.createConnection(),
            "Parameter(\"P\", NUMERIC, ParamRef(\"Q\") + 1) + "
            + "Parameter(\"Q\", NUMERIC, Iif(1 = 1, ParamRef(\"P\"), 2))",
            "Cycle occurred while evaluating parameter 'P'");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParameterMetadata(Context context) {
        Connection connection = context.createConnection();
        Query query = connection.parseQuery(
            "with member [Measures].[A string] as \n"
            + "   Parameter(\"S\",STRING,\"x\" || \"y\",\"A string parameter\")\n"
            + " member [Measures].[A number] as \n"
            + "   Parameter(\"N\",NUMERIC,2+3,\"A numeric parameter\")\n"
            + "select {[Measures].[Unit Sales]} on rows,\n"
            + " {Parameter(\"P\",[Gender],[Gender].[F],\"Which gender?\"),\n"
            + "  Parameter(\"Q\",[Gender],[Gender].DefaultMember,\"Another gender?\")} on columns\n"
            + "from Sales");
        Parameter[] parameters = query.getParameters();
        assertEquals(4, parameters.length);
        assertEquals("S", parameters[0].getName());
        assertEquals("N", parameters[1].getName());
        assertEquals("P", parameters[2].getName());
        assertEquals("Q", parameters[3].getName());
        final Member member =
            query.getSchemaReader(true).getMemberByUniqueName(
                Id.Segment.toList("Gender", "M"), true);
        parameters[2].setValue(member);
        assertEqualsVerbose(
            "with member [Measures].[A string] as 'Parameter(\"S\", STRING, (\"x\" || \"y\"), \"A string parameter\")'\n"
            + "  member [Measures].[A number] as 'Parameter(\"N\", NUMERIC, (2 + 3), \"A numeric parameter\")'\n"
            + "select {Parameter(\"P\", [Gender], [Gender].[M], \"Which gender?\"), Parameter(\"Q\", [Gender], [Gender].DefaultMember, \"Another gender?\")} ON COLUMNS,\n"
            + "  {[Measures].[Unit Sales]} ON ROWS\n"
            + "from [Sales]\n",
            Util.unparse(query));
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testTwoParametersBug1425153(Context context) {
        Connection connection = context.createConnection();
        Query query = connection.parseQuery(
            "select \n"
            + "{[Measures].[Unit Sales]} on columns, \n"
            + "{Parameter(\"ProductMember\", [Product], [Product].[All Products].[Food], \"wat willste?\").children} ON rows \n"
            + "from Sales where Parameter(\"Time\",[Time],[Time].[1997].[Q1])");

        // Execute before setting parameters.
        Result result = connection.execute(query);
        String resultString = TestUtil.toString(result);
        assertEqualsVerbose(
            "Axis #0:\n"
            + "{[Time].[1997].[Q1]}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product].[Food].[Baked Goods]}\n"
            + "{[Product].[Food].[Baking Goods]}\n"
            + "{[Product].[Food].[Breakfast Foods]}\n"
            + "{[Product].[Food].[Canned Foods]}\n"
            + "{[Product].[Food].[Canned Products]}\n"
            + "{[Product].[Food].[Dairy]}\n"
            + "{[Product].[Food].[Deli]}\n"
            + "{[Product].[Food].[Eggs]}\n"
            + "{[Product].[Food].[Frozen Foods]}\n"
            + "{[Product].[Food].[Meat]}\n"
            + "{[Product].[Food].[Produce]}\n"
            + "{[Product].[Food].[Seafood]}\n"
            + "{[Product].[Food].[Snack Foods]}\n"
            + "{[Product].[Food].[Snacks]}\n"
            + "{[Product].[Food].[Starchy Foods]}\n"
            + "Row #0: 1,932\n"
            + "Row #1: 5,045\n"
            + "Row #2: 820\n"
            + "Row #3: 4,737\n"
            + "Row #4: 400\n"
            + "Row #5: 3,262\n"
            + "Row #6: 2,985\n"
            + "Row #7: 918\n"
            + "Row #8: 6,624\n"
            + "Row #9: 391\n"
            + "Row #10: 9,499\n"
            + "Row #11: 412\n"
            + "Row #12: 7,750\n"
            + "Row #13: 1,718\n"
            + "Row #14: 1,316\n",
            resultString);

        // Set one parameter and execute again.
        query.setParameter(
            "ProductMember", "[Product].[All Products].[Food].[Eggs]");
        result = connection.execute(query);
        resultString = TestUtil.toString(result);
        assertEqualsVerbose(
            "Axis #0:\n"
            + "{[Time].[1997].[Q1]}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product].[Food].[Eggs].[Eggs]}\n"
            + "Row #0: 918\n",
            resultString);

        // Now set both parameters and execute again.
        query.setParameter(
            "ProductMember", "[Product].[All Products].[Food].[Deli]");
        query.setParameter("Time", "[Time].[1997].[Q2].[4]");
        result = connection.execute(query);
        resultString = TestUtil.toString(result);
        assertEqualsVerbose(
            "Axis #0:\n"
            + "{[Time].[1997].[Q2].[4]}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product].[Food].[Deli].[Meat]}\n"
            + "{[Product].[Food].[Deli].[Side Dishes]}\n"
            + "Row #0: 621\n"
            + "Row #1: 187\n",
            resultString);
    }

    /**
     * Positive and negative tests assigning values to a parameter of type
     * NUMERIC.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testAssignNumericParameter(Context context) {
        final String para = "Parameter(\"x\", NUMERIC, 1)";
        Connection connection = context.createConnection();
        assertAssignParameter(connection, para, false, "8", null);
        assertAssignParameter(connection, para, false, "8.24", null);
        assertAssignParameter(connection, para, false, 8, null);
        assertAssignParameter(connection, para, false, -8.56, null);
        assertAssignParameter(connection, para, false, new BigDecimal("12.345"), null);
        assertAssignParameter(connection, para, false, new BigInteger("12345"), null);
        // Formatted date will depends on time zone. Only match part of message.
        assertAssignParameter(connection,
                para, false, new Date(),
            "' for parameter 'x', type NUMERIC");
        assertAssignParameter(connection,
                para, false, new Timestamp(new Date().getTime()),
            "' for parameter 'x', type NUMERIC");
        assertAssignParameter(connection,
                para, false, new Time(new Date().getTime()),
            "' for parameter 'x', type NUMERIC");
        // OK to assign null
        assertAssignParameter(connection, para, false, null, null);
    }

    /**
     * Positive and negative tests assigning values to a parameter of type
     * STRING.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testAssignStringParameter(Context context) {
        final String para = "Parameter(\"x\", STRING, 'xxx')";
        Connection connection = context.createConnection();
        assertAssignParameter(connection, para, false, "8", null);
        assertAssignParameter(connection, para, false, "8.24", null);
        assertAssignParameter(connection, para, false, 8, null);
        assertAssignParameter(connection, para, false, -8.56, null);
        assertAssignParameter(connection, para, false, new BigDecimal("12.345"), null);
        assertAssignParameter(connection, para, false, new BigInteger("12345"), null);
        assertAssignParameter(connection, para, false, new Date(), null);
        assertAssignParameter(connection,
                para, false, new Timestamp(new Date().getTime()), null);
        assertAssignParameter(connection,
                para, false, new Time(new Date().getTime()), null);
        assertAssignParameter(connection, para, false, null, null);
    }

    /**
     * Positive and negative tests assigning values to a parameter whose type is
     * a member.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testAssignMemberParameter(Context context) {
        final String para = "Parameter(\"x\", [Customers], [Customers].[USA])";
        Connection connection = context.createConnection();
        assertAssignParameter(connection,
                para, false, "8", "MDX object '8' not found in cube 'Sales'");
        assertAssignParameter(connection,
            para, false, "8.24",
            "MDX object '8.24' not found in cube 'Sales'");
        assertAssignParameter(connection,
            para, false, 8,
            "Invalid value '8' for parameter 'x',"
            + " type MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, false, -8.56,
            "Invalid value '-8.56' for parameter 'x',"
            + " type MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, false, new BigDecimal("12.345"),
            "Invalid value '12.345' for parameter 'x',"
            + " type MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, false, new Date(),
            "' for parameter 'x', type MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, false, new Timestamp(new Date().getTime()),
            "' for parameter 'x', type MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, false, new Time(new Date().getTime()),
            "' for parameter 'x', type MemberType<hierarchy=[Customers]>");

        // string is OK
        assertAssignParameter(connection, para, false, "[Customers].[Mexico]", null);
        // now with spurious 'all'
        assertAssignParameter(connection,
            para, false, "[Customers].[All Customers].[Canada].[BC]", null);
        // non-existent member
        assertAssignParameter(connection,
            para, false, "[Customers].[Canada].[Bear Province]",
            "MDX object '[Customers].[Canada].[Bear Province]' not found in "
            + "cube 'Sales'");

        // Valid to set to null. It means use the default member of the
        // hierarchy. (Not necessarily the same as the default value of the
        // parameter. There's no way to get back to the default value of a
        // parameter once you've set it -- even by setting it to null.)
        assertAssignParameter(connection, para, false, null, null);

        SchemaReader sr =
                connection
                .parseQuery("select from [Sales]").getSchemaReader(true)
                .withLocus();

        // Member of wrong hierarchy.
        assertAssignParameter(connection,
            para, false, sr.getMemberByUniqueName(
                Id.Segment.toList("Time", "1997", "Q2", "5"), true),
            "Invalid value '[Time].[1997].[Q2].[5]' for parameter 'x', "
            + "type MemberType<hierarchy=[Customers]>");

        // Member of right hierarchy.
        assertAssignParameter(connection,
            para, false, sr.getMemberByUniqueName(
                Id.Segment.toList("Customers", "All Customers"), true),
            null);

        // Member of wrong level of right hierarchy.
        assertAssignParameter(connection,
            "Parameter(\"x\", [Customers].[State Province], [Customers].[USA].[CA])",
            false,
            sr.getMemberByUniqueName(
                Id.Segment.toList("Customers", "USA"), true),
            "Invalid value '[Customers].[USA]' for parameter "
            + "'x', type MemberType<level=[Customers].[State Province]>");

        // Same, using string.
        assertAssignParameter(connection,
            "Parameter(\"x\", [Customers].[State Province], [Customers].[USA].[CA])",
            false, "[Customers].[USA]",
            "Invalid value '[Customers].[USA]' for parameter "
            + "'x', type MemberType<level=[Customers].[State Province]>");

        // Member of right level.
        assertAssignParameter(connection,
            "Parameter(\"x\", [Customers].[State Province], [Customers].[USA].[CA])",
            false,
            sr.getMemberByUniqueName(
                Id.Segment.toList("Customers", "USA", "OR"), true),
            null);
    }

    /**
     * Positive and negative tests assigning values to a parameter whose type is
     * a set of members.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testAssignSetParameter(Context context) {
        final String para =
            "Parameter(\"x\", [Customers], {[Customers].[USA], [Customers].[USA].[CA]})";
        Connection connection = context.createConnection();
        assertAssignParameter(connection,
            para, true, "8",
            "MDX object '8' not found in cube 'Sales'");
        assertAssignParameter(connection,
            para, true, "foobar",
            "MDX object 'foobar' not found in cube 'Sales'");
        assertAssignParameter(connection,
            para, true, 8,
            "Invalid value '8' for parameter 'x', type SetType<MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, true, -8.56,
            "Invalid value '-8.56' for parameter 'x', type SetType<MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, true, new BigDecimal("12.345"),
            "Invalid value '12.345' for parameter 'x', type SetType<MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, true, new Date(),
            "' for parameter 'x', type SetType<MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, true, new Timestamp(new Date().getTime()),
            "' for parameter 'x', type SetType<MemberType<hierarchy=[Customers]>");
        assertAssignParameter(connection,
            para, true, new Time(new Date().getTime()),
            "' for parameter 'x', type SetType<MemberType<hierarchy=[Customers]>");

        // strings are OK
        assertAssignParameter(connection,
            para, true,
            "{[Customers].[USA], [Customers].[All Customers].[Canada].[BC]}",
            null);
        // also OK without braces
        assertAssignParameter(connection,
            para, true,
            "[Customers].[USA], [Customers].[All Customers].[Canada].[BC]",
            null);
        // also OK with non-standard spacing
        assertAssignParameter(connection,
            para, true,
            "[Customers] . [USA] , [Customers].[Canada].[BC],[Customers].[Mexico]",
            null);
        // error if one of the members does not exist
        assertAssignParameter(connection,
            para, true,
            "{[Customers].[USA], [Customers].[Canada].[BC].[Bear City]}",
            "MDX object '[Customers].[Canada].[BC].[Bear City]' not found in cube 'Sales'");

        List<Member> list;
        SchemaReader sr =
                connection
                .parseQuery("select from [Sales]").getSchemaReader(true)
                .withLocus();

        // Empty list is OK.
        list = Collections.emptyList();
        assertAssignParameter(connection, para, true, list, null);

        // empty string is ok
        assertAssignParameter(connection, para, true, "", null);

        // empty string is ok
        assertAssignParameter(connection, para, true, "{}", null);

        // empty string is ok
        assertAssignParameter(connection, para, true, " { } ", null);

        // Not valid to set list to null.
        assertAssignParameter(connection,
                para, true, null,
            "Invalid value 'null' for parameter 'x', type SetType<MemberType<hierarchy=[Customers]>>");

        // List that contains one member of wrong hierarchy.
        list =
            Arrays.asList(
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Customers", "Mexico"), true),
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Time", "1997", "Q2", "5"), true));
        assertAssignParameter(connection,
                para, true, list,
            "Invalid value '[Time].[1997].[Q2].[5]' for parameter 'x', "
            + "type MemberType<hierarchy=[Customers]>");

        // as above, strings
        assertAssignParameter(connection,
                para, true,
            "{[Customers].[Mexico], [Time].[1997].[Q2].[5]}",
            "Invalid value '[Time].[1997].[Q2].[5]' for parameter 'x', "
            + "type MemberType<hierarchy=[Customers]>");

        // List that contains members of correct hierarchy.
        list =
            Arrays.asList(
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Customers", "Mexico"), true),
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Customers", "Canada"), true));
        assertAssignParameter(connection, para, true, list, null);

        // List that contains member of wrong level of right hierarchy.
        list =
            Arrays.asList(
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Customers", "USA", "CA"), true),
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Customers", "Mexico"), true));
        assertAssignParameter(connection,
                "Parameter(\"x\", [Customers].[State Province], {[Customers].[USA].[CA]})",
            true,
            list,
            "Invalid value '[Customers].[Mexico]' for parameter "
            + "'x', type MemberType<level=[Customers].[State Province]>");

        // as above, strings
        assertAssignParameter(connection,
                "Parameter(\"x\", [Customers].[State Province], {[Customers].[USA].[CA]})",
            true,
            "{[Customers].[USA].[CA], [Customers].[Mexico]}",
            "Invalid value '[Customers].[Mexico]' for parameter "
            + "'x', type MemberType<level=[Customers].[State Province]>");

        // List that contains members of right level, and a null member.
        list =
            Arrays.asList(
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Customers", "USA", "CA"), true),
                null,
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Customers", "USA", "OR"), true));
        assertAssignParameter(connection,
                "Parameter(\"x\", [Customers].[State Province], {[Customers].[USA].[CA]})",
            true,
            list,
            null);
    }

    /**
     * Checks that assigning a given value to a parameter does (or, if
     * {@code expectedMsg} is null, does not) give an error.
     *
     * @param parameterMdx MDX expression declaring parameter
     * @param set Whether parameter is a set (as opposed to a member or scalar)
     * @param value Value to assign to parameter
     * @param expectedMsg Expected message, or null if it should succeed
     */
    private void assertAssignParameter(Connection connection,
        String parameterMdx,
        boolean set,
        Object value,
        String expectedMsg)
    {
        try {
            String mdx = set
                ? "with set [Foo] as "
                  + parameterMdx
                  + " \n"
                  + "select [Foo] on columns,\n"
                  + "{Time.Time.Children} on rows\n"
                  + "from [Sales]"
                : "with member [Measures].[s] as "
                  + parameterMdx
                  + " \n"
                  + "select {[Measures].[s]} on columns,\n"
                  + "{Time.Time.Children} on rows\n"
                  + "from [Sales]";
            Query query = connection.parseQuery(mdx);
            if (expectedMsg == null) {
                query.setParameter("x", value);
                final Result result = connection.execute(query);
                assertNotNull(result);
            } else {
                try {
                    query.setParameter("x", value);
                    final Result result = connection.execute(query);
                    fail("expected error, got " + TestUtil.toString(result));
                } catch (Exception e) {
                    checkThrowable(e, expectedMsg);
                }
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Tests a parameter whose type is a set of members.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testParamSet(Context context) {
        Connection connection = context.createConnection();
        try {
            String mdx =
                "select [Measures].[Unit Sales] on 0,\n"
                + " Parameter(\"Foo\", [Time], {}, \"Foo\") on 1\n"
                + "from [Sales]";
            Query query = connection.parseQuery(mdx);
            SchemaReader sr = query.getSchemaReader(false);
            Member m1 =
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Time", "1997", "Q2", "5"), true);
            Member m2 =
                sr.getMemberByUniqueName(
                    Id.Segment.toList("Time", "1997", "Q3"), true);
            Parameter p = sr.getParameter("Foo");
            final List<Member> list = Arrays.asList(m1, m2);
            p.setValue(list);
            assertEquals(list, p.getValue());
            query.resolve();
            p.setValue(list);
            assertEquals(list, p.getValue());
            mdx = query.toString();
            assertEqualsVerbose(
                "select {[Measures].[Unit Sales]} ON COLUMNS,\n"
                + "  Parameter(\"Foo\", [Time], {[Time].[1997].[Q2].[5], [Time].[1997].[Q3]}, \"Foo\") ON ROWS\n"
                + "from [Sales]\n",
                mdx);

            final Result result = connection.execute(query);
            assertEqualsVerbose(
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Measures].[Unit Sales]}\n"
                + "Axis #2:\n"
                + "{[Time].[1997].[Q2].[5]}\n"
                + "{[Time].[1997].[Q3]}\n"
                + "Row #0: 21,081\n"
                + "Row #1: 65,848\n",
                TestUtil.toString(result));
        } finally {
            connection.close();
        }
    }

    // -- Tests for connection properties --------------

    /**
     * Tests that certain connection properties which should be null, are.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testConnectionPropsWhichShouldBeNull(Context context) {
        // properties which must always return null
        Connection connection = context.createConnection();
        assertExprReturns(connection, "ParamRef(\"JdbcPassword\")", "");
        assertExprReturns(connection, "ParamRef(\"CatalogContent\")", "");
    }

    /**
     * Tests that non-overrideable properties cannot be overridden in a
     * statement.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testConnectionPropsCannotBeOverridden(Context context) {
        Set<RolapConnectionProperties> overrideableProps = Olap4jUtil.enumSetOf(
            RolapConnectionProperties.Catalog,
            RolapConnectionProperties.Locale);
        for (RolapConnectionProperties prop
            : RolapConnectionProperties.class.getEnumConstants())
        {
            if (!overrideableProps.contains(prop)) {
                // try to override prop
                assertSetPropertyFails(context.createConnection(), prop.name(), "Connection");
            }
        }
    }

    // -- Tests for system properties --------------

    /**
     * Tests accessing system properties as parameters in a statement.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSystemPropsGet(Context context) {
        final List<Property> propertyList =
            MondrianProperties.instance().getPropertyList();
        for (Property property : propertyList) {
            assertExprReturns(context.createConnection(),
                "ParamRef("
                + Util.singleQuoteString(property.getPath())
                + ")",
                property.stringValue());
        }
    }

    /**
     * Tests getting a java system property is not possible
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSystemPropsNotAvailable(Context context) {
        assertExprThrows(context.createConnection(),
            "ParamRef(\"java.version\")",
            "Unknown parameter 'java.version'");
    }

    /**
     * Tests getting a mondrian property.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testMondrianPropsGetJava(Context context) {
        final String jdbcDrivers =
            MondrianProperties.instance().JdbcDrivers.get();
        assertExprReturns(context.createConnection(),
            "ParamRef(\"mondrian.jdbcDrivers\")", jdbcDrivers);
    }

    /**
     * Tests setting system properties.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSystemPropsSet(Context context) {
        final List<Property> propertyList =
            MondrianProperties.instance().getPropertyList();
        for (Property property : propertyList) {
            final String propName = property.getPath();
            assertSetPropertyFails(context.createConnection(), propName, "System");
        }
    }

    // -- Tests for schema properties --------------

    /**
     * Tests a schema property with a default value.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSchemaProp(Context context) {
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            "<Parameter name=\"prop\" type=\"String\" "
            + "defaultValue=\" 'foo bar' \" />",
            null,
            null,
            null, null, null);
        withSchema(context, schema);
        assertExprReturns(context.createConnection(), "ParamRef(\"prop\")", "foo bar");
    }

    /**
     * Tests a schema property with a default value.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSchemaPropDupFails(Context context) {
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            "<Parameter name=\"foo\" type=\"Numeric\" defaultValue=\"1\" />\n"
            + "<Parameter name=\"bar\" type=\"Numeric\" defaultValue=\"2\" />\n"
            + "<Parameter name=\"foo\" type=\"Numeric\" defaultValue=\"3\" />\n",
            null,
            null,
            null,
            null,
            null);
        withSchema(context, schema);
        assertExprThrows(context.createConnection(),
            "ParamRef(\"foo\")",
            "Duplicate parameter 'foo' in schema");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSchemaPropIllegalTypeFails(Context context) {
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            "<Parameter name=\"foo\" type=\"Bad type\" defaultValue=\"1\" />",
            null,
            null,
            null,
            null,
            null);
        withSchema(context, schema);
        assertExprThrows(context.createConnection(),
            "1",
            "In Schema: In Parameter: "
            + "Value 'Bad type' of attribute 'type' has illegal value 'Bad type'.  "
            + "Legal values: {String, Numeric, Integer, Boolean, Date, Time, Timestamp, Member}");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSchemaPropInvalidDefaultExpFails(Context context) {
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            "<Parameter name=\"Product Current Member\" type=\"Member\" defaultValue=\"[Product].DefaultMember.Children(2) \" />",
            null,
            null,
            null,
            null,
            null);
        withSchema(context,schema);
        assertExprThrows(context.createConnection(),
            "ParamRef(\"Product Current Member\")",
            "No function matches signature '<Member>.Children(<Numeric Expression>)'");
    }

    /**
     * Tests that a schema property fails if it references dimensions which
     * are not available.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSchemaPropContext(Context context) {
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            "<Parameter name=\"Customer Current Member\" type=\"Member\" defaultValue=\"[Customers].DefaultMember.Children.Item(2) \" />",
            null,
            null,
            null,
            null,
            null);
        withSchema(context,schema);
        assertQueryReturns(context.createConnection(),
            "with member [Measures].[Foo] as ' ParamRef(\"Customer Current Member\").Name '\n"
            + "select {[Measures].[Foo]} on columns\n"
            + "from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Foo]}\n"
            + "Row #0: USA\n");

        assertQueryThrows(context.createConnection(),
            "with member [Measures].[Foo] as ' ParamRef(\"Customer Current Member\").Name '\n"
            + "select {[Measures].[Foo]} on columns\n"
            + "from [Warehouse]",
            "MDX object '[Customers]' not found in cube 'Warehouse'");
    }
}

// End ParameterTest.java

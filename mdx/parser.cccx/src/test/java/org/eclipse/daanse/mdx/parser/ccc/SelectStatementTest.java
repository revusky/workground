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
package org.eclipse.daanse.mdx.parser.ccc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.mdx.parser.ccc.MdxTestUtils.checkAxis;
import static org.eclipse.daanse.mdx.parser.ccc.MdxTestUtils.checkNameObjectIdentifiers;
import static org.eclipse.daanse.mdx.parser.ccc.MdxTestUtils.checkSelectSubcubeClauseName;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClauseName;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClauseStatement;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.cccx.MdxParserWrapper;
import org.junit.jupiter.api.Test;

class SelectStatementTest {

    @Test
    void testSimpleStatement() throws MdxParserException {
        String mdx = """
            SELECT [Customer].[Gender].[Gender].Membmers ON COLUMNS,
                     {[Customer].[Customer].[Aaron A. Allen],
                      [Customer].[Customer].[Abigail Clark]} ON ROWS
               FROM [Adventure Works]
               WHERE [Measures].[Internet Sales Amount]
            """;

        SelectStatement selectStatement = new MdxParserWrapper(mdx).parseSelectStatement();
        assertThat(selectStatement).isNotNull();
        assertThat(selectStatement.selectWithClauses()).isEmpty();
        assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
        SelectQueryAxesClause selectQueryAxesClause = (SelectQueryAxesClause) selectStatement.selectQueryClause();
        assertThat(selectQueryAxesClause.selectQueryAxisClauses()).hasSize(2);
        SelectQueryAxisClause selectQueryAxisClause1 = selectQueryAxesClause.selectQueryAxisClauses().get(0);
        SelectQueryAxisClause selectQueryAxisClause2 = selectQueryAxesClause.selectQueryAxisClauses().get(1);
        checkSelectQueryAxisClause1(selectQueryAxisClause1);
        checkSelectQueryAxisClause2(selectQueryAxisClause2);
        checkSelectSubcubeClauseName(selectStatement.selectSubcubeClause(), "Adventure Works",
            ObjectIdentifier.Quoting.QUOTED);
        assertThat(selectStatement.selectSlicerAxisClause()).isPresent();
        assertThat(selectStatement.selectSlicerAxisClause().get().expression())
            .isNotNull().isInstanceOf(CompoundId.class);
        CompoundId compoundId = (CompoundId) selectStatement.selectSlicerAxisClause().get().expression();
        assertThat(compoundId.objectIdentifiers()).hasSize(2);
        checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 0, "Measures", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(
            compoundId.objectIdentifiers(), 1, "Internet Sales Amount", ObjectIdentifier.Quoting.QUOTED
        );
    }

    //{[Customer].[Customer].[Aaron A. Allen],[Customer].[Customer].[Abigail Clark]} ON ROWS
    private void checkSelectQueryAxisClause2(SelectQueryAxisClause selectQueryAxisClause2) {
        assertThat(selectQueryAxisClause2.nonEmpty()).isFalse();
        checkAxis(selectQueryAxisClause2.axis(), 1, true);
        assertThat(selectQueryAxisClause2.selectDimensionPropertyListClause()).isNull();
        assertThat(selectQueryAxisClause2.expression()).isNotNull().isInstanceOf(CallExpression.class);
        CallExpression callExpression = (CallExpression) selectQueryAxisClause2.expression();
        assertThat(callExpression.name()).isEqualTo("{}");
        assertThat(callExpression.type()).isEqualTo(CallExpression.Type.BRACES);
        assertThat(callExpression.expressions()).hasSize(2);
        assertThat(callExpression.expressions().get(0)).isNotNull().isInstanceOf(CompoundId.class);
        assertThat(callExpression.expressions().get(1)).isNotNull().isInstanceOf(CompoundId.class);
        CompoundId compoundId1 = (CompoundId) callExpression.expressions().get(0);
        CompoundId compoundId2 = (CompoundId) callExpression.expressions().get(1);
        assertThat(compoundId1.objectIdentifiers()).hasSize(3);
        assertThat(compoundId2.objectIdentifiers()).hasSize(3);

        checkNameObjectIdentifiers(compoundId1.objectIdentifiers(), 0, "Customer", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId1.objectIdentifiers(), 1, "Customer", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId1.objectIdentifiers(), 2, "Aaron A. Allen",
            ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId2.objectIdentifiers(), 0, "Customer", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId2.objectIdentifiers(), 1, "Customer", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId2.objectIdentifiers(), 2, "Abigail Clark",
            ObjectIdentifier.Quoting.QUOTED);
    }

    //[Customer].[Gender].[Gender].Membmers ON COLUMNS,
    private void checkSelectQueryAxisClause1(SelectQueryAxisClause selectQueryAxisClause) {
        assertThat(selectQueryAxisClause.nonEmpty()).isFalse();
        checkAxis(selectQueryAxisClause.axis(), 0, true);
        assertThat(selectQueryAxisClause.selectDimensionPropertyListClause()).isNull();
        assertThat(selectQueryAxisClause.expression()).isNotNull().isInstanceOf(CallExpression.class);
        CallExpression callExpression = (CallExpression) selectQueryAxisClause.expression();
        assertThat(callExpression.name()).isEqualTo("Membmers");
        assertThat(callExpression.type()).isEqualTo(CallExpression.Type.PROPERTY);
        assertThat(callExpression.expressions()).hasSize(1);
        assertThat(callExpression.expressions().get(0)).isNotNull().isInstanceOf(CompoundId.class);
        CompoundId compoundId = (CompoundId) callExpression.expressions().get(0);
        assertThat(compoundId.objectIdentifiers()).hasSize(3);
        checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 0, "Customer", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 1, "Gender", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 2, "Gender", ObjectIdentifier.Quoting.QUOTED);
    }

    @Test
    void testDimentionProperties() throws MdxParserException {
        String mdx = """
            SELECT [Store].[Store].Members DIMENSION PROPERTIES [Store].[Store].[Store Name].[Store Type] on 0
            from [Sales]
            """;

        SelectStatement selectStatement = new MdxParserWrapper(mdx).parseSelectStatement();
        assertThat(selectStatement).isNotNull();
        assertThat(selectStatement.selectWithClauses()).isNotNull().isEmpty();
        assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
        SelectQueryAxesClause selectQueryAxesClause = (SelectQueryAxesClause) selectStatement.selectQueryClause();
        assertThat(selectQueryAxesClause.selectQueryAxisClauses()).isNotNull().hasSize(1);
        assertThat(selectQueryAxesClause.selectQueryAxisClauses().get(0).nonEmpty()).isFalse();
        assertThat(selectQueryAxesClause.selectQueryAxisClauses().get(0).expression())
            .isInstanceOf(CallExpression.class);
        checkAxis(selectQueryAxesClause.selectQueryAxisClauses().get(0).axis(), 0, true);
        assertThat(selectQueryAxesClause.selectQueryAxisClauses().get(0).selectDimensionPropertyListClause().properties()).hasSize(1);
        List<? extends ObjectIdentifier> objectIdentifiers =
            selectQueryAxesClause.selectQueryAxisClauses().get(0).selectDimensionPropertyListClause().properties().get(0).objectIdentifiers();
        assertThat(objectIdentifiers).hasSize(4);
        checkNameObjectIdentifiers(objectIdentifiers, 0, "Store", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(objectIdentifiers, 1, "Store", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(objectIdentifiers, 2, "Store Name", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(objectIdentifiers, 3, "Store Type", ObjectIdentifier.Quoting.QUOTED);

        CallExpression callExpression =
            (CallExpression) selectQueryAxesClause.selectQueryAxisClauses().get(0).expression();
        assertThat(callExpression.name()).isEqualTo("Members");
        assertThat(callExpression.type()).isEqualTo(CallExpression.Type.PROPERTY);
        assertThat(callExpression.expressions()).hasSize(1);
        assertThat(callExpression.expressions().get(0)).isInstanceOf(CompoundId.class);
        CompoundId compoundId = (CompoundId) callExpression.expressions().get(0);
        assertThat(compoundId.objectIdentifiers()).hasSize(2);

        checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 0, "Store", ObjectIdentifier.Quoting.QUOTED);
        checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 0, "Store", ObjectIdentifier.Quoting.QUOTED);

        checkSelectSubcubeClauseName(selectStatement.selectSubcubeClause(), "Sales", ObjectIdentifier.Quoting.QUOTED);
        assertThat(selectStatement.selectSlicerAxisClause()).isNotPresent();
        assertThat(selectStatement.selectCellPropertyListClause()).isNotPresent();
    }

    @Test
    void testSubCube() throws MdxParserException {
        String mdx = """
            SELECT
            [Measures].[Internet Sales Amount] on 0,
            [Date].[Calendar].Members on 1
            FROM
            (
            SELECT {[Date].[Calendar].[Month].&[2001]&[7], [Date].[Calendar].[Month].&[2001]&[12]} on 0
            FROM
            (SELECT {[Date].[Calendar].[Calendar Year].&[2001]} ON 0 FROM [Adventure Works])
            )
                """;

        SelectStatement selectStatement = new MdxParserWrapper(mdx).parseSelectStatement();
        assertThat(selectStatement).isNotNull();
        // test sub cubes
        assertThat(selectStatement.selectSubcubeClause()).isNotNull().isInstanceOf(SelectSubcubeClauseStatement.class);
        SelectSubcubeClauseStatement selectSubcubeClauseStatement1 =
            (SelectSubcubeClauseStatement) selectStatement.selectSubcubeClause();
        assertThat(selectSubcubeClauseStatement1.selectSubcubeClause())
            .isNotNull().isInstanceOf(SelectSubcubeClauseStatement.class);
        SelectSubcubeClauseStatement selectSubcubeClauseStatement2 =
            (SelectSubcubeClauseStatement) selectSubcubeClauseStatement1.selectSubcubeClause();
        assertThat(selectSubcubeClauseStatement2.selectSubcubeClause())
            .isNotNull().isInstanceOf(SelectSubcubeClauseName.class);
        SelectSubcubeClauseName selectSubcubeClauseName =
            (SelectSubcubeClauseName) selectSubcubeClauseStatement2.selectSubcubeClause();
        assertThat(selectSubcubeClauseName.cubeName().name()).isEqualTo("Adventure Works");
        assertThat(selectSubcubeClauseName.cubeName().quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
    }

    @Test
    void testSelectStatement1() throws MdxParserException {
        assertParseQuery("""
            with member [Measures].__Foo as 1 + 2
            select __Foo on 0
            from _Bar_Baz"
            """);
    }

    @Test
    void testSelectStatement2() throws MdxParserException {
        String mdx = """
            with member [Measures].#_Foo as 1 + 2
            select __Foo on 0
            from _Bar#Baz
            """;
        assertParseQueryFails(mdx);
    }

    @Test
    void testSelectStatement3() throws MdxParserException {
        assertParseQuery("""
            with member [Measures].$Foo as 1 + 2
            select $Foo on 0
            from Bar$Baz
            """);
    }

    @Test
    void testSelectStatement4() throws MdxParserException {
        assertParseQuery("""
            select [measures].[$foo] on columns from sales
            """);
    }

    @Test
    void testSelectStatement5() throws MdxParserException {
        String mdx = """
            select { Customers].Children } on columns from [Sales]
            """;
        assertParseQueryFails(mdx);
    }

    @Test
    void testSelectStatement6() throws MdxParserException {
        assertParseQuery("""
            with member [Measures].[Foo] as ' 123 '
            select {[Measures].members} on columns,
            CrossJoin([Product].members, {[Gender].Children}) on rows
            from [Sales]
            where [Marital Status].[S]
            """);
    }

    @Test
    void testSelectStatement7() throws MdxParserException {
        assertParseQuery("select {[axis0mbr]} on axis(0), {[axis1mbr]} on axis(1) from cube1");
    }

    @Test
    void testSelectStatement8() throws MdxParserException {
        assertParseQuery("select {[axis1mbr]} on aXiS(1), {[axis0mbr]} on AxIs(0) from cube1");
    }

    @Test
    void testSwitch1() throws MdxParserException {
        assertParseQuery("""
            with member [Measures].[Foo] as
            ' case when x = y then \"eq\" when x < y then \"lt\" else \"gt\" end '
            select {[foo]} on axis(0) from [cube]
            """);
    }

    @Test
    void testSwitch2() throws MdxParserException {
        assertParseQuery("""
             with member [Measures].[Foo] as
            ' case x when 1 then 2 when 3 then 4 else 5 end '
              select {[foo]} on axis(0) from [cube]
            """);
    }

    /**
     * Parser should not require braces around range op in WITH SET.
     */
    @Test
    void testSetExpr() throws MdxParserException {
        assertParseQuery("""
            with set [Set1] as '[Product].[Drink]:[Product].[Food]'
            select [Set1] on columns, {[Measures].defaultMember} on rows
            from Sales
            """);

        // set expr in axes
        assertParseQuery("""
            select [Product].[Drink]:[Product].[Food] on columns,
                 {[Measures].defaultMember} on rows
                from Sales
            """);
    }

    @Test
    void testDimensionProperties() throws MdxParserException {
        assertParseQuery("""
            select {[foo]} properties p1,   p2 on columns from [cube]
            """);
    }

    @Test
    void testCellProperties() throws MdxParserException {
        assertParseQuery("""
            select {[foo]} on columns
            from [cube] CELL PROPERTIES FORMATTED_VALUE
            """);
    }

    /**
     * High precision number in MDX causes overflow
     * The problem was that "5000001234" exceeded the precision of the int being
     * used to gather the mantissa.
     */
    @Test
    void testLargePrecision() throws MdxParserException {
        // Now, a query with several numeric literals. This is the original
        // testcase for the bug.
        assertParseQuery("""
            with member [Measures].[Small Number] as '[Measures].[Store Sales] / 9000'
            select
            {[Measures].[Small Number]} on columns,
            {Filter([Product].[Product Department].members, [Measures].[Small Number] >= 0.3
            and [Measures].[Small Number] <= 0.5000001234)} on rows
            from Sales
            where ([Time].[1997].[Q2].[4])
            """);
    }

    /**
     * DrilldownLevelTop parser error.
     */
    @Test
    void testEmptyExpr() throws MdxParserException {
        assertParseQuery("""
            select NON EMPTY HIERARCHIZE(
            {DrillDownLevelTop(
            {[Product].[All Products]},3,,[Measures].[Unit Sales])}
            ) ON COLUMNS
            from [Sales]
            """);

        // more advanced; the actual test case in the bug
        assertParseQuery("""
            SELECT {[Measures].[NetSales]}
            DIMENSION PROPERTIES PARENT_UNIQUE_NAME ON COLUMNS ,
            NON EMPTY HIERARCHIZE(AddCalculatedMembers(
            {DrillDownLevelTop({[ProductDim].[Name].[All]}, 10, ,
            [Measures].[NetSales])}))
            DIMENSION PROPERTIES PARENT_UNIQUE_NAME ON ROWS
            FROM [cube]
            """);
    }

    /**
     * Test case for SELECT in the FROM clause.
     */
    @Test
    void testInnerSelect() throws MdxParserException {
        assertParseQuery("""
            SELECT FROM
            (SELECT ({[ProductDim].[Product Group].&[Mobile Phones]})
            ON COLUMNS FROM [cube]) CELL PROPERTIES VALUE
            """);
    }

    private void assertParseQuery(String mdx) throws MdxParserException {
        SelectStatement selectStatement = new MdxParserWrapper(mdx).parseSelectStatement();
        assertThat(selectStatement).isNotNull();
    }

    private void assertParseQueryFails(String s) throws MdxParserException {
        MdxParserWrapper parser = new MdxParserWrapper(s);
        assertThrows(MdxParserException.class, () -> parser.parseSelectStatement());
    }
}

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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.Expression;
import org.eclipse.daanse.mdx.model.api.expression.KeyObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.NullLiteral;
import org.eclipse.daanse.mdx.model.api.expression.NumericLiteral;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.model.api.expression.StringLiteral;
import org.eclipse.daanse.mdx.model.api.expression.SymbolLiteral;
//import org.eclipse.daanse.mdx.model.record.expression.CallExpressionR;
//import org.eclipse.daanse.mdx.model.record.expression.CompoundIdR;
//import org.eclipse.daanse.mdx.model.record.expression.NumericLiteralR;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.cccx.MdxParserWrapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExpressionTest {

	@Nested
	class CallExpressionTest {

		@Test
		void testCallExpressionFunctionWithArrayParam() throws MdxParserException {
			Expression clause = new MdxParserWrapper("FunctionName([arg1, arg2])").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.FUNCTION);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(1);
			checkArgument((CallExpression) clause, 0, "arg1, arg2");
		}

		@Test
		void testCallExpressionFunctionWithoutParams() throws MdxParserException {
			Expression clause = new MdxParserWrapper("FunctionName()").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.FUNCTION);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).isEmpty();
		}

		@Test
		void testCallExpressionFunctionWithOneParam() throws MdxParserException {
			Expression clause = new MdxParserWrapper("FunctionName(arg)").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.FUNCTION);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(1);
			checkArgument((CallExpression) clause, 0, "arg");
		}

		@Test
		void testCallExpressionFunctionWithSeveralParams() throws MdxParserException {
			Expression clause = new MdxParserWrapper("FunctionName(arg1, arg2)").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.FUNCTION);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "arg1");
			checkArgument((CallExpression) clause, 1, "arg2");
		}

		@Test
		void testCallExpressionFunctionWithSeveralParamsWithArray() throws MdxParserException {
			Expression clause = new MdxParserWrapper("FunctionName(arg1, [arg2, arg3])").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.FUNCTION);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "arg1");
			checkArgument((CallExpression) clause, 1, "arg2, arg3");
		}

		@Test
		void testCallExpressionEmpty() throws MdxParserException {
			Expression clause = new MdxParserWrapper("FunctionName(arg1, ,arg2)").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.FUNCTION);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(3);
			checkArgument((CallExpression) clause, 0, "arg1");
			checkArgument((CallExpression) clause, 2, "arg2");
			CallExpression callExpression = ((CallExpression) (((CallExpression) clause).expressions().get(1)));
			assertThat(callExpression.type()).isEqualTo(CallExpression.Type.EMPTY);
			assertThat(callExpression.name()).isEmpty();
		}

		@Test
		void testCallExpressionProperty() throws MdxParserException {
			Expression clause = new MdxParserWrapper("object.PROPERTY").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.PROPERTY);
			assertThat(((CallExpression) clause).name()).isEqualTo("PROPERTY");
			assertThat(((CallExpression) clause).expressions()).hasSize(1);
			checkArgument((CallExpression) clause, 0, "object");
		}

		@Test
		void testCallExpressionPropertyQuoted() throws MdxParserException {
			Expression clause = new MdxParserWrapper("object.&PROPERTY").parseExpression();
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.PROPERTY_QUOTED);
		}

		@Test
		void testCallExpressionPropertyAmpersAndQuoted() throws MdxParserException {
			Expression clause = new MdxParserWrapper("object.[&PROPERTY]").parseExpression();
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.PROPERTY_AMPERS_AND_QUOTED);
		}

		@Test
		void testCallExpressionMethod() throws MdxParserException {
			Expression clause = new MdxParserWrapper("object.FunctionName()").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.METHOD);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "object");
			CallExpression callExpression = ((CallExpression) (((CallExpression) clause).expressions().get(1)));
			assertThat(callExpression.type()).isEqualTo(CallExpression.Type.EMPTY);
			assertThat(callExpression.name()).isEmpty();
			assertThat(callExpression.expressions()).isNotNull().isEmpty();
		}

		@Test
		void testCallExpressionMethodWithParameter() throws MdxParserException {
			Expression clause = new MdxParserWrapper("object.FunctionName(arg)").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.METHOD);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "object");
			checkArgument((CallExpression) clause, 1, "arg");
		}

		@Test
		void testCallExpressionMethodWithParameterArray() throws MdxParserException {
			Expression clause = new MdxParserWrapper("object.FunctionName([arg1, arg2])").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.METHOD);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionName");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "object");
			checkArgument((CallExpression) clause, 1, "arg1, arg2");
		}

		@Test
		void testCallExpressionMethodWithInnerFunction() throws MdxParserException {
			Expression clause = new MdxParserWrapper("object.FunctionOuter(FunctionInner())").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.METHOD);
			assertThat(((CallExpression) clause).name()).isEqualTo("FunctionOuter");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "object");
//			assertThat(((CallExpressionR) clause).expressions().get(1)).isInstanceOf(CallExpressionR.class);
			assertThat(((CallExpression) clause).expressions().get(1)).isInstanceOf(CallExpression.class);
			CallExpression callExpression = ((CallExpression) (((CallExpression) clause).expressions().get(1)));
			assertThat(callExpression.type()).isEqualTo(CallExpression.Type.FUNCTION);
			assertThat(callExpression.name()).isEqualTo("FunctionInner");
			assertThat(callExpression.expressions()).isNotNull().isEmpty();
		}

		@Test
		void testCallExpressionTermCase() throws MdxParserException {
			Expression clause = new MdxParserWrapper("CASE a WHEN b THEN c END").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.TERM_CASE);
			assertThat(((CallExpression) clause).name()).isEqualTo("_CaseMatch");
			assertThat(((CallExpression) clause).expressions()).hasSize(3);
			checkArgument((CallExpression) clause, 0, "a");
			checkArgument((CallExpression) clause, 1, "b");
			checkArgument((CallExpression) clause, 2, "c");
		}

		@Test
		void testCallExpressionBraces1() throws MdxParserException {
			Expression clause = new MdxParserWrapper("{ expression }").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.BRACES);
			assertThat(((CallExpression) clause).name()).isEqualTo("{}");
			assertThat(((CallExpression) clause).expressions()).hasSize(1);
			checkArgument((CallExpression) clause, 0, "expression");
		}

		@Test
		void testCallExpressionBraces2() throws MdxParserException {
			Expression clause = new MdxParserWrapper("{ expression1, expression2 }").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.BRACES);
			assertThat(((CallExpression) clause).name()).isEqualTo("{}");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "expression1");
			checkArgument((CallExpression) clause, 1, "expression2");
		}

		@Test
		void testCallExpressionBraces3() throws MdxParserException {
			Expression clause = new MdxParserWrapper("{ [a] : [c] }").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.BRACES);
			assertThat(((CallExpression) clause).name()).isEqualTo("{}");
			assertThat(((CallExpression) clause).expressions()).hasSize(1);

//			assertThat(((CallExpressionR) clause).expressions().get(0)).isInstanceOf(CallExpressionR.class);
			assertThat(((CallExpression) clause).expressions().get(0)).isInstanceOf(CallExpression.class);
			CallExpression callExpression = ((CallExpression) (((CallExpression) clause).expressions().get(0)));
			assertThat(callExpression.type()).isEqualTo(CallExpression.Type.TERM_INFIX);
			assertThat(callExpression.name()).isEqualTo(":");
			assertThat(callExpression.expressions()).isNotNull().hasSize(2);
			checkArgument(callExpression, 0, "a");
			checkArgument(callExpression, 1, "c");

		}

		@Test
		void testCallExpressionBraces4() throws MdxParserException {
			Expression clause = new MdxParserWrapper("{ [a].[a], [a].[b], [a].[c] }").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			CallExpression callExpression = ((CallExpression) clause);
			assertThat(callExpression.type()).isEqualTo(CallExpression.Type.BRACES);
			assertThat(callExpression.name()).isEqualTo("{}");
			assertThat(callExpression.expressions()).hasSize(3);
//			assertThat(callExpression.expressions().get(0)).isInstanceOf(CompoundIdR.class);
			assertThat(callExpression.expressions().get(0)).isInstanceOf(CompoundId.class);

			CompoundId compoundId0 = (CompoundId) callExpression.expressions().get(0);
			assertThat(compoundId0.objectIdentifiers()).hasSize(2);
			checkCompoundId(compoundId0, 2, 0, "a");
			checkCompoundId(compoundId0, 2, 1, "a");

			CompoundId compoundId1 = (CompoundId) callExpression.expressions().get(1);
			assertThat(compoundId1.objectIdentifiers()).hasSize(2);
			checkCompoundId(compoundId1, 2, 0, "a");
			checkCompoundId(compoundId1, 2, 1, "b");

			CompoundId compoundId2 = (CompoundId) callExpression.expressions().get(2);
			assertThat(compoundId2.objectIdentifiers()).hasSize(2);
			checkCompoundId(compoundId2, 2, 0, "a");
			checkCompoundId(compoundId2, 2, 1, "c");
		}

		@Test
		void testCallExpressionParentheses() throws MdxParserException {
			Expression clause = new MdxParserWrapper("( arg1, arg2 )").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.PARENTHESES);
			assertThat(((CallExpression) clause).name()).isEqualTo("()");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "arg1");
			checkArgument((CallExpression) clause, 1, "arg2");
		}

		@Test
		void testCallExpressionParenthesesWithArray() throws MdxParserException {
			Expression clause = new MdxParserWrapper("( arg1, [arg2, arg3] )").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.PARENTHESES);
			assertThat(((CallExpression) clause).name()).isEqualTo("()");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "arg1");
			checkArgument((CallExpression) clause, 1, "arg2, arg3");
		}

		@Test
		void testCallExpressionTermPostfix() throws MdxParserException {
			Expression clause = new MdxParserWrapper("arg IS EMPTY").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.TERM_POSTFIX);
			assertThat(((CallExpression) clause).name()).isEqualTo("IS EMPTY");
			assertThat(((CallExpression) clause).expressions()).hasSize(1);
			checkArgument((CallExpression) clause, 0, "arg");
		}

		@Test
		void testCallExpressionTermPrefix() throws MdxParserException {
			Expression clause = new MdxParserWrapper("NOT arg").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.TERM_PREFIX);
			assertThat(((CallExpression) clause).name()).isEqualTo("NOT");
			assertThat(((CallExpression) clause).expressions()).hasSize(1);
			checkArgument((CallExpression) clause, 0, "arg");
		}

		@Test
		void testCallExpressionTermInfix() throws MdxParserException {
			Expression clause = new MdxParserWrapper("arg1 AND arg2").parseExpression();
//			assertThat(clause).isNotNull().isInstanceOf(CallExpressionR.class);
			assertThat(clause).isNotNull().isInstanceOf(CallExpression.class);
			assertThat(((CallExpression) clause).type()).isEqualTo(CallExpression.Type.TERM_INFIX);
			assertThat(((CallExpression) clause).name()).isEqualTo("AND");
			assertThat(((CallExpression) clause).expressions()).hasSize(2);
			checkArgument((CallExpression) clause, 0, "arg1");
			checkArgument((CallExpression) clause, 1, "arg2");
		}

		public static void checkArgument(CallExpression clause, int index, String arg) {
//			assertThat(clause.expressions().get(index)).isInstanceOf(CompoundIdR.class);
			assertThat(clause.expressions().get(index)).isInstanceOf(CompoundId.class);
			CompoundId compoundId = (CompoundId) (clause.expressions().get(index));
			checkCompoundId(compoundId, 1, 0, arg);
		}

		private static void checkCompoundId(CompoundId compoundId, int size, int index, String arg) {
			assertThat(compoundId.objectIdentifiers()).isNotNull().hasSize(size);
			assertThat(compoundId.objectIdentifiers().get(0)).isInstanceOf(NameObjectIdentifier.class);
			assertThat(((NameObjectIdentifier) (compoundId.objectIdentifiers().get(index))).name()).isEqualTo(arg);
		}
	}

	@Nested
	class LiteralTest {

		@Test
		void testNumericLiteral1() throws MdxParserException {
			Expression clause = new MdxParserWrapper("10").parseExpression();
			assertThat(clause).isInstanceOf(NumericLiteral.class);
			NumericLiteral numericLiteral = (NumericLiteral) clause;
			assertThat(numericLiteral.value()).isEqualTo(BigDecimal.valueOf(10));
		}

		@Test
		void testNumericLiteral2() throws MdxParserException {
			Expression clause = new MdxParserWrapper("10.25").parseExpression();
			assertThat(clause).isInstanceOf(NumericLiteral.class);
			NumericLiteral numericLiteral = (NumericLiteral) clause;
			assertThat(numericLiteral.value()).isEqualTo(BigDecimal.valueOf(10.25));
		}

		@Test
		void testNumericLiteral4() throws MdxParserException {
			Expression clause = new MdxParserWrapper("10e+5").parseExpression();
			assertThat(clause).isInstanceOf(NumericLiteral.class);
			NumericLiteral numericLiteral = (NumericLiteral) clause;
			assertThat(numericLiteral.value()).isEqualTo(new BigDecimal("10e+5"));
		}

		@Test
		void testNumericLiteral5() throws MdxParserException {
			Expression clause = new MdxParserWrapper("10e-5").parseExpression();
			assertThat(clause).isInstanceOf(NumericLiteral.class);
			NumericLiteral numericLiteral = (NumericLiteral) clause;
			assertThat(numericLiteral.value()).isEqualTo(new BigDecimal("10e-5"));
		}

		@Test
		void testNumericLiteral3() throws MdxParserException {
			Expression clause = new MdxParserWrapper("-10.25").parseExpression();
//			assertThat(clause).isInstanceOf(CallExpressionR.class);
			assertThat(clause).isInstanceOf(CallExpression.class);
			CallExpression callExpression = (CallExpression) clause;
			assertThat(callExpression.name()).isEqualTo("-");
			assertThat(callExpression.type()).isEqualTo(CallExpression.Type.TERM_PREFIX);
			assertThat(callExpression.expressions()).hasSize(1);
//			assertThat(callExpression.expressions().get(0)).isNotNull().isInstanceOf(NumericLiteralR.class);
			assertThat(callExpression.expressions().get(0)).isNotNull().isInstanceOf(NumericLiteral.class);
			NumericLiteral numericLiteral = (NumericLiteral) callExpression.expressions().get(0);
			assertThat(numericLiteral.value()).isEqualTo(BigDecimal.valueOf(10.25));
		}

		@ParameterizedTest
		@ValueSource(strings = { "null", "Null", "NULL" })
		void testNull(String exp) throws MdxParserException {
			Expression clause = new MdxParserWrapper(exp).parseExpression();
			assertThat(clause).isInstanceOf(NullLiteral.class);
		}

		@Test
		void testStringLiteral1() throws MdxParserException {
			Expression clause = new MdxParserWrapper("\"String'Literal\"").parseExpression();
			assertThat(clause).isInstanceOf(StringLiteral.class);
			StringLiteral numericLiteral = (StringLiteral) clause;
			assertThat(numericLiteral.value()).isEqualTo("String'Literal");
		}

		@Test
		void testStringLiteral2() throws MdxParserException {
			Expression clause = new MdxParserWrapper("'StringLiteral'").parseExpression();
			assertThat(clause).isInstanceOf(StringLiteral.class);
			StringLiteral numericLiteral = (StringLiteral) clause;
			assertThat(numericLiteral.value()).isEqualTo("StringLiteral");
		}

		@Test
		void testSymbolLiteral() throws MdxParserException {
			Expression clause = new MdxParserWrapper("cast(\"the_date\" as DATE)").parseExpression();
//			assertThat(clause).isInstanceOf(CallExpressionR.class);
			assertThat(clause).isInstanceOf(CallExpression.class);
			CallExpression callExpression = (CallExpression) clause;
			assertThat(callExpression.name()).isEqualTo("CAST");
			assertThat(callExpression.type()).isEqualTo(CallExpression.Type.CAST);
			assertThat(callExpression.expressions()).hasSize(2);
			assertThat(callExpression.expressions().get(0)).isNotNull().isInstanceOf(StringLiteral.class);
			StringLiteral stringLiteral = (StringLiteral) callExpression.expressions().get(0);
			assertThat(stringLiteral.value()).isEqualTo("the_date");
			assertThat(callExpression.expressions().get(1)).isNotNull().isInstanceOf(SymbolLiteral.class);
			SymbolLiteral symbolLiteral = (SymbolLiteral) callExpression.expressions().get(1);
			assertThat(symbolLiteral.value()).isEqualTo("DATE");
		}

		@Test
		void testSymbolLiteral1() throws MdxParserException {
			MdxParserWrapper parser = new MdxParserWrapper("cast(a, \"the_date\" as DATE)");
			assertThrows(MdxParserException.class, () -> parser.parseExpression());
		}

	}

	@Nested
	class ObjectIdentifierTest {

		@Test
		void testKeyObjectIdentifier() throws MdxParserException {
			Expression clause = new MdxParserWrapper("[x].&foo&[1]&bar.[y]").parseExpression();
			assertThat(clause).isInstanceOf(CompoundId.class);
			CompoundId compoundId = (CompoundId) clause;
			assertThat(compoundId.objectIdentifiers()).hasSize(3);
			assertThat(compoundId.objectIdentifiers().get(0)).isNotNull().isInstanceOf(NameObjectIdentifier.class);
			assertThat(compoundId.objectIdentifiers().get(1)).isNotNull().isInstanceOf(KeyObjectIdentifier.class);
			assertThat(compoundId.objectIdentifiers().get(2)).isNotNull().isInstanceOf(NameObjectIdentifier.class);

			NameObjectIdentifier nameObjectIdentifier00 = (NameObjectIdentifier) compoundId.objectIdentifiers().get(0);
			assertThat(nameObjectIdentifier00.name()).isEqualTo("x");
			assertThat(nameObjectIdentifier00.quoting()).isEqualTo(Quoting.QUOTED);

			KeyObjectIdentifier keyObjectIdentifier = (KeyObjectIdentifier) compoundId.objectIdentifiers().get(1);
			assertThat(keyObjectIdentifier.nameObjectIdentifiers()).isNotNull().hasSize(3);
			assertThat(keyObjectIdentifier.nameObjectIdentifiers().get(0)).isInstanceOf(NameObjectIdentifier.class);
			assertThat(keyObjectIdentifier.nameObjectIdentifiers().get(1)).isInstanceOf(NameObjectIdentifier.class);
			assertThat(keyObjectIdentifier.nameObjectIdentifiers().get(2)).isInstanceOf(NameObjectIdentifier.class);
			NameObjectIdentifier nameObjectIdentifier0 = (NameObjectIdentifier) keyObjectIdentifier
					.nameObjectIdentifiers().get(0);
			NameObjectIdentifier nameObjectIdentifier1 = (NameObjectIdentifier) keyObjectIdentifier
					.nameObjectIdentifiers().get(1);
			NameObjectIdentifier nameObjectIdentifier2 = (NameObjectIdentifier) keyObjectIdentifier
					.nameObjectIdentifiers().get(2);
			assertThat(nameObjectIdentifier0.name()).isEqualTo("foo");
			assertThat(nameObjectIdentifier1.name()).isEqualTo("1");
			assertThat(nameObjectIdentifier2.name()).isEqualTo("bar");
			assertThat(nameObjectIdentifier0.quoting()).isEqualTo(Quoting.UNQUOTED);
			assertThat(nameObjectIdentifier1.quoting()).isEqualTo(Quoting.QUOTED);
			assertThat(nameObjectIdentifier2.quoting()).isEqualTo(Quoting.UNQUOTED);

			NameObjectIdentifier nameObjectIdentifier22 = (NameObjectIdentifier) compoundId.objectIdentifiers().get(2);
			assertThat(nameObjectIdentifier22.name()).isEqualTo("y");
			assertThat(nameObjectIdentifier22.quoting()).isEqualTo(Quoting.QUOTED);
		}
	}

    @Test
    void testIsEmpty() throws MdxParserException {
        assertParseExpr("[Measures].[Unit Sales] IS EMPTY");

        assertParseExpr("[Measures].[Unit Sales] IS EMPTY AND 1 IS NULL");

        assertParseExpr("- x * 5 is empty is empty is null + 56");
    }

    @Test
    void testIs() throws MdxParserException {
        assertParseExpr(
            """
                [Measures].[Unit Sales] IS [Measures].[Unit Sales]
                AND [Measures].[Unit Sales] IS NULL
                """);
    }

    @Test
    void testIsNull() throws MdxParserException {
        assertParseExpr(
            "[Measures].[Unit Sales] IS NULL");

        assertParseExpr("[Measures].[Unit Sales] IS NULL AND 1 <> 2");

        assertParseExpr("x is null or y is null and z = 5");

        assertParseExpr("(x is null) + 56 > 6");

        assertParseExpr("x is null and a = b or c = d + 5 is null + 5");
    }

    @Test
    void testNull() throws MdxParserException {
        assertParseExpr("Filter({[Measures].[Foo]}, Iif(1 = 2, NULL, 'X'))");
    }

    @Test
    void testCast() throws MdxParserException {
        assertParseExpr("Cast([Measures].[Unit Sales] AS Numeric)");

        assertParseExpr("Cast(1 + 2 AS String)");
    }

    @Test
    void testBangFunction() throws MdxParserException {
        // Parser accepts '<id> [! <id>] *' as a function name, but ignores
        // all but last name.
        assertParseExpr("foo!bar!Exp(2.0)");
        assertParseExpr("1 + VBA!Exp(2.0 + 3)");
    }

    @Test
    void testId() throws MdxParserException {
        assertParseExpr("foo");
        assertParseExpr("fOo");
        assertParseExpr("[Foo].[Bar Baz]");
        assertParseExpr("[Foo].&[Bar]");
    }

    @Test
    void testIdComplex() throws MdxParserException {
        // simple key
        assertParseExpr(
            "[Foo].&[Key1]&[Key2].[Bar]");
        // compound key
        assertParseExpr(
            "[Foo].&[1]&[Key 2]&[3].[Bar]");
        // compound key sans brackets
        assertParseExpr(
            "[Foo].&Key1&Key2 + 4");
        // brackets are required for numbers

        if (false)
            assertParseExprFails(
                "[Foo].&[1]&[Key2]&^3.[Bar]");
        // space between ampersand and key is unacceptable
        assertParseExprFails(
            "[Foo].&^ [Key2].[Bar]");
        // underscore after ampersand is unacceptable
        assertParseExprFails(
            "[Foo].&^_Key2.[Bar]");
        // but underscore is OK within brackets
        assertParseExpr(
            "[Foo].&[_Key2].[Bar]");
    }

    /**
     * Tests parsing of numbers.
     */
    @Test
    void testNumbers() throws MdxParserException {
        // Number: [+-] <digits> [ . <digits> ] [e [+-] <digits> ]
        assertParseExpr("2");

        // leading '-' is treated as an operator -- that's ok
        assertParseExpr("-3");

        // leading '+' is ignored -- that's ok
        assertParseExpr("+45");

        // space bad
        assertParseExprFails(
            "4 5");

        assertParseExpr("3.14");
        assertParseExpr(".12345");

        // lots of digits left and right of point
        assertParseExpr("31415926535.89793");
        assertParseExpr(
            "31415926535897.9314159265358979");
        assertParseExpr("3.141592653589793");
        assertParseExpr(
            "-3141592653589793.14159265358979");

        // exponents akimbo
        assertParseExpr("1e2");

        assertParseExprFails("1e2e3");

        assertParseExpr("1.2e3");

        assertParseExpr("-1.2345e3");
        assertParseExprFails(
            "1.2e3.4");
        assertParseExpr(".00234e0003");
        assertParseExpr(
            ".00234e-0067");
    }

    /**
     * We give the AS operator low
     * precedence, so CAST works as it should but 'expr AS namedSet' does not.
     */
    @Test
    void testAsPrecedence() throws MdxParserException {
        // low precedence operator (AND) in CAST.
        assertParseExpr(
            "cast(a and b as string)");

        // medium precedence operator (:) in CAST
        assertParseExpr(
            "cast(a : b as string)");

        // high precedence operator (IS) in CAST
        assertParseExpr("cast(a is b as string)");

        // low precedence operator in axis expression. According to spec, 'AS'
        // has higher precedence than '*' but we give it lower. Bug.
        assertParseExpr("a * b as c");


        // Note that 'AS' has higher precedence than '*'.
        assertParseExpr("a * b as c * d");

        // Spec says that ':' has a higher precedence than '*'.
        // Mondrian currently does it wrong.
        assertParseExpr("a : b * c : d");

        // Note that 'AS' has higher precedence than ':', has higher
        // precedence than '*'.
        assertParseExpr("a : b as n * c : d as n2 as n3");
    }

    private void assertParseExprFails(String s) throws MdxParserException {
        MdxParserWrapper parser = new MdxParserWrapper(s);
        assertThrows(MdxParserException.class, () -> parser.parseExpression());
    }

    private void assertParseExpr(String s) throws MdxParserException {
        Expression clause = new MdxParserWrapper(s).parseExpression();
        assertThat(clause).isNotNull();
    }
}

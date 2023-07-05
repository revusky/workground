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

import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAsteriskClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryEmptyClause;
//import org.eclipse.daanse.mdx.model.record.select.SelectQueryAsteriskClauseR;
//import org.eclipse.daanse.mdx.model.record.select.SelectQueryAxesClauseR;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.cccx.MdxParserWrapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SelectQueryClauseTest {

	@Nested
	class SelectQueryAxesClauseClauseTest {

		@Test
		void testInStatement() throws MdxParserException {
			String mdx = """
					SELECT [Customer].[Gender].[Gender].Membmers ON COLUMNS,
					         {[Customer].[Customer].[Aaron A. Allen],
					          [Customer].[Customer].[Abigail Clark]} ON ROWS
					   FROM [c]
					""";

			SelectStatement selectStatement = new MdxParserWrapper(mdx).parseSelectStatement();
			System.out.println(selectStatement);
			assertThat(selectStatement).isNotNull();

		}

		@Test
		void testInClauseMultiple() throws MdxParserException {
			String mdx = """
					[Customer].[Gender].[Gender].Membmers ON COLUMNS,
					        {[Customer].[Customer].[Aaron A. Allen],
					         [Customer].[Customer].[Abigail Clark]} ON ROWS
					""";
			SelectQueryAxesClause clause = new MdxParserWrapper(mdx).parseSelectQueryAxesClause();
//			assertThat(clause).isNotNull().isInstanceOf(SelectQueryAxesClauseR.class);
			assertThat(clause).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
		}

		@Test
		void testInClauseSingle() throws MdxParserException {
			String mdx = "[Customer] ON COLUMNS";
			SelectQueryAxesClause clause = new MdxParserWrapper(mdx).parseSelectQueryAxesClause();
//			assertThat(clause).isNotNull().isInstanceOf(SelectQueryAxesClauseR.class);
			assertThat(clause).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
		}
	}

	@Nested
	class SelectQueryEmptyClauseTest {
		@Test
		void testInStatement() throws MdxParserException {
			String mdx = "SELECT FROM [c]";

			SelectStatement selectStatement = new MdxParserWrapper(mdx).parseSelectStatement();
			assertThat(selectStatement).isNotNull();
//			assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryEmptyClauseR.class);
			assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryEmptyClause.class);

		}
	}

	@Nested
	class SelectQueryAsteriskClauseTest {

		@Test
		void testAsteriskInStatement() throws MdxParserException {
			String mdx = "SELECT * FROM [c]";

			SelectStatement selectStatement = new MdxParserWrapper(mdx).parseSelectStatement();
			assertThat(selectStatement).isNotNull();
//			assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryAsteriskClauseR.class);
			assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryAsteriskClause.class);

		}

		@Test
		void testAsteriskInClause() throws MdxParserException {
			String mdx = "*";
			SelectQueryAsteriskClause clause = new MdxParserWrapper(mdx).parseSelectQueryAsteriskClause();
			assertThat(clause).isNotNull();
		}

	}
}

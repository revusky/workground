/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2015-2021 Hitachi Vantara.
// All rights reserved.
 */
package org.eclipse.daanse.db.dialect.db.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

import org.eclipse.daanse.db.dialect.api.Dialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MySqlDialect3Test {
    private static final String ILLEGAL_BOOLEAN_LITERAL = "illegal for this dialect boolean literal";
    private static final String ILLEGAL_BOOLEAN_LITERAL_MESSAGE = "Illegal BOOLEAN literal:  ";
    private static final String BOOLEAN_LITERAL_TRUE = "True";
    private static final String BOOLEAN_LITERAL_FALSE = "False";
    private static final String BOOLEAN_LITERAL_ONE = "1";
    private static final String BOOLEAN_LITERAL_ZERO = "0";
    private Connection connection = mock(Connection.class);
    private DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    Statement statmentMock = mock(Statement.class);
    private Dialect dialect;
    private StringBuilder buf;

    @BeforeEach
    protected void setUp() throws Exception {
        when(metaData.getDatabaseProductName()).thenReturn("MYSQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("5.0");
        when(statmentMock.execute(any())).thenReturn(false);
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.createStatement()).thenReturn(statmentMock);
        dialect = new MySqlDialect();
        buf = new StringBuilder();
    }

    @Test
    void testAllowsRegularExpressionInWhereClause() {
        assertTrue(dialect.allowsRegularExpressionInWhereClause());
    }

    @Test
    void testGenerateRegularExpression_InvalidRegex() throws Exception {
        assertNull(dialect.generateRegularExpression("table.column", "(a"), "Invalid regex should be ignored");
    }

    @Test
    void testGenerateRegularExpression_CaseInsensitive() throws Exception {
        String sql = dialect.generateRegularExpression("table.column", "(?i)|(?u).*a.*").toString();
        assertEquals("table.column IS NOT NULL AND UPPER(table.column) REGEXP '.*A.*'", sql);
    }

    @Test
    void testGenerateRegularExpression_CaseSensitive() throws Exception {
        String sql = dialect.generateRegularExpression("table.column", ".*a.*").toString();
        assertEquals("table.column IS NOT NULL AND table.column REGEXP '.*a.*'", sql);
    }

    @Test
    void testQuoteBooleanLiteral_True() throws Exception {
        assertEquals(0, buf.length());
        dialect.quoteBooleanLiteral(buf, BOOLEAN_LITERAL_TRUE);
        assertEquals(BOOLEAN_LITERAL_TRUE, buf.toString());
    }

    @Test
    void testQuoteBooleanLiteral_False() throws Exception {
        assertEquals(0, buf.length());
        dialect.quoteBooleanLiteral(buf, BOOLEAN_LITERAL_FALSE);
        assertEquals(BOOLEAN_LITERAL_FALSE, buf.toString());
    }

    @Test
    void testQuoteBooleanLiteral_One() throws Exception {
        assertEquals(0, buf.length());
        dialect.quoteBooleanLiteral(buf, BOOLEAN_LITERAL_ONE);
        assertEquals(BOOLEAN_LITERAL_ONE, buf.toString());
    }

    @Test
    void testQuoteBooleanLiteral_Zero() throws Exception {
        assertEquals(0, buf.length());
        dialect.quoteBooleanLiteral(buf, BOOLEAN_LITERAL_ZERO);
        assertEquals(BOOLEAN_LITERAL_ZERO, buf.toString());
    }

    @Test
    void testQuoteBooleanLiteral_TrowsException() throws Exception {
        assertEquals(0, buf.length());
        try {
            dialect.quoteBooleanLiteral(buf, ILLEGAL_BOOLEAN_LITERAL);
            fail("The illegal boolean literal exception should appear BUT it was not.");
        } catch (NumberFormatException e) {
            assertEquals(ILLEGAL_BOOLEAN_LITERAL_MESSAGE + ILLEGAL_BOOLEAN_LITERAL, e.getMessage());
        }
    }

}

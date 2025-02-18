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
package org.eclipse.daanse.mdx.unparser.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.DrillthroughStatement;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryClause;
import org.eclipse.daanse.mdx.model.record.DrillthroughStatementR;
import org.eclipse.daanse.mdx.model.record.SelectStatementR;
import org.eclipse.daanse.mdx.model.record.expression.CallExpressionR;
import org.eclipse.daanse.mdx.model.record.expression.KeyObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.select.AxisR;
import org.eclipse.daanse.mdx.model.record.select.SelectQueryAxesClauseR;
import org.eclipse.daanse.mdx.model.record.select.SelectQueryAxisClauseR;
import org.eclipse.daanse.mdx.model.record.select.SelectSubcubeClauseNameR;
import org.junit.jupiter.api.Test;

class SimpleUnparserDrillthroughStatementTest {

    private SimpleUnparser unparser = new SimpleUnparser();

    @Test
    void test() {

        SelectQueryClause selectQueryClause =
            new SelectQueryAxesClauseR(List.of(new SelectQueryAxisClauseR(false,
                new CallExpressionR("{}",
                    CallExpression.Type.BRACES,
                    List.of(
                        new NameObjectIdentifierR("Date", ObjectIdentifier.Quoting.QUOTED),
                        new NameObjectIdentifierR("Calendar", ObjectIdentifier.Quoting.QUOTED),
                        new NameObjectIdentifierR("Calendar Year", ObjectIdentifier.Quoting.QUOTED),
                        new KeyObjectIdentifierR(List.of(new NameObjectIdentifierR("2001",
                            ObjectIdentifier.Quoting.QUOTED)))
                    )),
                new AxisR(0, true),
                null)));
        SelectStatement selectStatement = new SelectStatementR(List.of(), selectQueryClause,
            new SelectSubcubeClauseNameR(new NameObjectIdentifierR("Adventure Works", ObjectIdentifier.Quoting.QUOTED)),
            Optional.ofNullable(null), Optional.ofNullable(null));
        DrillthroughStatement drillthroughStatement = new DrillthroughStatementR(
            Optional.of(10), Optional.of(1), selectStatement, null);

        assertThat(unparser.unparseDrillthroughStatement(drillthroughStatement)).asString()
            .isEqualTo("DRILLTHROUGH\r\n MAXROWS 10\r\n FIRSTROWSET 1\r\n SELECT {[Date],[Calendar],[Calendar Year],&[2001]} ON COLUMNS FROM [Adventure Works]");
    }
}

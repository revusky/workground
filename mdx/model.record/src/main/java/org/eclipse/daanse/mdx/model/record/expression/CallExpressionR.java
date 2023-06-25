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
package org.eclipse.daanse.mdx.model.record.expression;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.Expression;

public record CallExpressionR(String name,
                              CallExpression.Type type,
                              List<? extends Expression> expressions)
        implements CallExpression {

    public CallExpressionR {

        assert name != null;
        assert type != null;
        assert expressions != null;

        switch (type) {
        case BRACES:
            assert name.equals("{}");
            break;
        case PARENTHESES:
            assert name.equals("()");
            break;
        case INTERNAL:
            assert name.startsWith("$");
            break;
        case EMPTY:
            assert name.equals("");
            break;
        default:
            assert !name.startsWith("$") && !name.equals("{}") && !name.equals("()");
            break;
        }
    }
}

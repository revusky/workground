/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2002-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/

package mondrian.olap.fun;

import java.io.PrintWriter;

import mondrian.olap.Category;
import mondrian.olap.Exp;
import mondrian.olap.ExpBase;
import mondrian.olap.Syntax;
import mondrian.olap.Validator;
import mondrian.olap.type.Type;

/**
 * A <code>ValueFunDef</code> is a pseudo-function to evaluate a member or
 * a tuple. Similar to {@link TupleFunDef}.
 *
 * @author jhyde
 * @since Jun 14, 2002
 */
class ValueFunDef extends FunDefBase {
    private final int[] argTypes;

    ValueFunDef(int[] argTypes) {
        super(
            "_Value()",
            "_Value([<Member>, ...])",
            "Pseudo-function which evaluates a tuple.",
            Syntax.Parentheses,
            Category.NUMERIC,
            argTypes);
        this.argTypes = argTypes;
    }

    @Override
	public int getReturnCategory() {
        return Category.TUPLE;
    }

    @Override
	public int[] getParameterCategories() {
        return argTypes;
    }

    @Override
	public void unparse(Exp[] args, PrintWriter pw) {
        ExpBase.unparseList(pw, args, "(", ", ", ")");
    }

    @Override
	public Type getResultType(Validator validator, Exp[] args) {
        return null;
    }

}

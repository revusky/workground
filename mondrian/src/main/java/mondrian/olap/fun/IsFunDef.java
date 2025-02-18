/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun;

import org.eclipse.daanse.olap.api.model.Member;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.TupleCalc;
import mondrian.calc.impl.AbstractBooleanCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Category;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;

/**
 * Definition of the <code>IS</code> MDX function.
 *
 * @see IsNullFunDef
 * @author jhyde
 * @since Mar 23, 2006
 */
class IsFunDef extends FunDefBase {
    static final ReflectiveMultiResolver Resolver =
        new ReflectiveMultiResolver(
            "IS",
            "<Expression> IS <Expression>",
            "Returns whether two objects are the same",
            new String[] {"ibmm", "ibll", "ibhh", "ibdd", "ibtt"},
            IsFunDef.class);

    public IsFunDef(FunDef dummyFunDef) {
        super(dummyFunDef);
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final int category = call.getArg(0).getCategory();
        switch (category) {
        case Category.TUPLE:
            final TupleCalc tupleCalc0 = compiler.compileTuple(call.getArg(0));
            final TupleCalc tupleCalc1 = compiler.compileTuple(call.getArg(1));
            return new AbstractBooleanCalc(
            		call.getFunName(),call.getType(), new Calc[] {tupleCalc0, tupleCalc1})
            {
                @Override
				public boolean evaluateBoolean(Evaluator evaluator) {
                    Member[] o0 = tupleCalc0.evaluateTuple(evaluator);
                    Member[] o1 = tupleCalc1.evaluateTuple(evaluator);
                    return FunUtil.equalTuple(o0, o1);
                }
            };
        default:
            assert category == call.getArg(1).getCategory();
            final Calc calc0 = compiler.compile(call.getArg(0));
            final Calc calc1 = compiler.compile(call.getArg(1));
            return new AbstractBooleanCalc(call.getFunName(),call.getType(), new Calc[] {calc0, calc1}) {
                @Override
				public boolean evaluateBoolean(Evaluator evaluator) {
                    Object o0 = calc0.evaluate(evaluator);
                    Object o1 = calc1.evaluate(evaluator);
                    return o0.equals(o1);
                }
            };
        }
    }
}

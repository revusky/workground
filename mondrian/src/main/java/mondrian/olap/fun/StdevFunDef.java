/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun;

import org.eclipse.daanse.olap.api.model.Hierarchy;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ListCalc;
import mondrian.calc.TupleList;
import mondrian.calc.impl.AbstractCalc;
import mondrian.calc.impl.AbstractDoubleCalc;
import mondrian.calc.impl.ValueCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;

/**
 * Definition of the <code>Stdev</code> builtin MDX function, and its alias
 * <code>Stddev</code>.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
class StdevFunDef extends AbstractAggregateFunDef {
    static final ReflectiveMultiResolver StdevResolver =
        new ReflectiveMultiResolver(
            "Stdev",
            "Stdev(<Set>[, <Numeric Expression>])",
            "Returns the standard deviation of a numeric expression evaluated over a set (unbiased).",
            new String[]{"fnx", "fnxn"},
            StdevFunDef.class);

    static final ReflectiveMultiResolver StddevResolver =
        new ReflectiveMultiResolver(
            "Stddev",
            "Stddev(<Set>[, <Numeric Expression>])",
            "Alias for Stdev.",
            new String[]{"fnx", "fnxn"},
            StdevFunDef.class);

    public StdevFunDef(FunDef dummyFunDef) {
        super(dummyFunDef);
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final ListCalc listCalc =
            compiler.compileList(call.getArg(0));
        final Calc calc =
            call.getArgCount() > 1
            ? compiler.compileScalar(call.getArg(1), true)
            : new ValueCalc(call.getType());
        return new AbstractDoubleCalc(call.getFunName(),call.getType(), new Calc[] {listCalc, calc}) {
            @Override
			public double evaluateDouble(Evaluator evaluator) {
                TupleList memberList = AbstractAggregateFunDef.evaluateCurrentList(listCalc, evaluator);
                final int savepoint = evaluator.savepoint();
                try {
                    evaluator.setNonEmpty(false);
                    final double stdev =
                        (Double) FunUtil.stdev(
                            evaluator, memberList, calc, false);
                    return stdev;
                } finally {
                    evaluator.restore(savepoint);
                }
            }

            @Override
			public boolean dependsOn(Hierarchy hierarchy) {
                return AbstractCalc.anyDependsButFirst(getCalcs(), hierarchy);
            }
        };
    }
}

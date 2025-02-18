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

import org.eclipse.daanse.olap.api.model.Hierarchy;

import mondrian.calc.Calc;
import mondrian.calc.DoubleCalc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ListCalc;
import mondrian.calc.TupleList;
import mondrian.calc.impl.AbstractCalc;
import mondrian.calc.impl.AbstractDoubleCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;

/**
 * Definition of the <code>Percentile</code> MDX function.
 * <p>There is some discussion about what the "right" percentile function is.
 * Here is a <a href="http://cnx.org/content/m10805/latest/">good overview</a>.
 * Wikipedia also lists
 * <a href="http://en.wikipedia.org/wiki/Percentile">
 * methods of calculating percentile</a>.
 * </p>
 * <p>
 * This class based on MS Excel formulae:
 * </p>
 * <blockquote>rank = P / 100 * (N - 1) + 1</blockquote>
 * <blockquote>percentile = A[n]+d*(A[n+1]-A[n])</blockquote>
 * <p>Definition can also be found on
 * <a href="http://en.wikipedia.org/wiki/Percentile">Wikipedia</a></p>
 */
class PercentileFunDef extends AbstractAggregateFunDef {
    static final ReflectiveMultiResolver Resolver =
        new ReflectiveMultiResolver(
            "Percentile",
            "Percentile(<Set>, <Numeric Expression>, <Percent>)",
            "Returns the value of the tuple that is at a given percentile of a set.",
            new String[] {"fnxnn"},
            PercentileFunDef.class);

    public PercentileFunDef(FunDef dummyFunDef) {
        super(dummyFunDef);
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final ListCalc listCalc =
            compiler.compileList(call.getArg(0));
        final Calc calc =
            compiler.compileScalar(call.getArg(1), true);
        final DoubleCalc percentCalc =
            compiler.compileDouble(call.getArg(2));
        return new AbstractDoubleCalc(
        		call.getFunName(),call.getType(), new Calc[] {listCalc, calc, percentCalc})
        {
            @Override
			public double evaluateDouble(Evaluator evaluator) {
                TupleList list = AbstractAggregateFunDef.evaluateCurrentList(listCalc, evaluator);
                double percent = percentCalc.evaluateDouble(evaluator) * 0.01;
                final int savepoint = evaluator.savepoint();
                try {
                    evaluator.setNonEmpty(false);
                    final double percentile =
                        FunUtil.percentile(evaluator, list, calc, percent);
                    return percentile;
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

/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun.extra;

import org.eclipse.daanse.olap.api.model.Hierarchy;

import mondrian.calc.Calc;
import mondrian.calc.DoubleCalc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ListCalc;
import mondrian.calc.TupleList;
import mondrian.calc.impl.AbstractDoubleCalc;
import mondrian.calc.impl.ValueCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;
import mondrian.olap.fun.AbstractAggregateFunDef;
import mondrian.olap.fun.FunUtil;
import mondrian.olap.fun.MultiResolver;
import mondrian.olap.fun.ReflectiveMultiResolver;

/**
 * Definition of the <code>FirstQ</code> and <code>ThirdQ</code> MDX extension
 * functions.
 *
 * <p>These functions are not standard MDX.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
public class NthQuartileFunDef extends AbstractAggregateFunDef {
    private final int range;

    public static final MultiResolver ThirdQResolver =
        new ReflectiveMultiResolver(
            "ThirdQ",
            "ThirdQ(<Set>[, <Numeric Expression>])",
            "Returns the 3rd quartile value of a numeric expression evaluated over a set.",
            new String[]{"fnx", "fnxn"},
        NthQuartileFunDef.class);

    public static final MultiResolver FirstQResolver =
        new ReflectiveMultiResolver(
            "FirstQ",
            "FirstQ(<Set>[, <Numeric Expression>])",
            "Returns the 1st quartile value of a numeric expression evaluated over a set.",
            new String[]{"fnx", "fnxn"},
            NthQuartileFunDef.class);

    public NthQuartileFunDef(FunDef dummyFunDef) {
        super(dummyFunDef);
        this.range = dummyFunDef.getName().equals("FirstQ") ? 1 : 3;
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final ListCalc listCalc =
            compiler.compileList(call.getArg(0));
        final DoubleCalc doubleCalc =
            call.getArgCount() > 1
            ? compiler.compileDouble(call.getArg(1))
            : new ValueCalc(call.getType());
        return new AbstractDoubleCalc(call.getFunName(),call.getType(), new Calc[] {listCalc, doubleCalc}) {
            @Override
			public double evaluateDouble(Evaluator evaluator) {
                final int savepoint = evaluator.savepoint();
                try {
                    evaluator.setNonEmpty(false);
                    TupleList members =
                        evaluateCurrentList(listCalc, evaluator);
                    return
                        FunUtil.quartile(
                            evaluator, members, doubleCalc, range);
                } finally {
                    evaluator.restore(savepoint);
                }
            }

            @Override
			public boolean dependsOn(Hierarchy hierarchy) {
                return anyDependsButFirst(getCalcs(), hierarchy);
            }
        };
    }
}

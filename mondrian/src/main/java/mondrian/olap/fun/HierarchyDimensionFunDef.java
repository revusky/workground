/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun;

import org.eclipse.daanse.olap.api.model.Dimension;
import org.eclipse.daanse.olap.api.model.Hierarchy;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.HierarchyCalc;
import mondrian.calc.impl.AbstractDimensionCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.type.Type;

/**
 * Definition of the <code>&lt;Hierarchy&gt;.Dimension</code> MDX
 * builtin function.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
public class HierarchyDimensionFunDef extends FunDefBase {
    static final HierarchyDimensionFunDef instance =
        new HierarchyDimensionFunDef();

    private HierarchyDimensionFunDef() {
        super(
            "Dimension",
            "Returns the dimension that contains a specified hierarchy.",
            "pdh");
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final HierarchyCalc hierarchyCalc =
                compiler.compileHierarchy(call.getArg(0));
        return new CalcImpl(call.getType(), hierarchyCalc);
    }

    public static class CalcImpl extends AbstractDimensionCalc {
        private final HierarchyCalc hierarchyCalc;

        public CalcImpl(Type type, HierarchyCalc hierarchyCalc) {
            super("Dimension",type, new Calc[] {hierarchyCalc});
            this.hierarchyCalc = hierarchyCalc;
        }

        @Override
		public Dimension evaluateDimension(Evaluator evaluator) {
            Hierarchy hierarchy =
                    hierarchyCalc.evaluateHierarchy(evaluator);
            return hierarchy.getDimension();
        }
    }
}

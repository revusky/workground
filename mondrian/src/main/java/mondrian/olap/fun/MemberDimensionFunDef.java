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
import org.eclipse.daanse.olap.api.model.Member;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.MemberCalc;
import mondrian.calc.impl.AbstractDimensionCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;

/**
 * Definition of the <code>&lt;Measure&gt;.Dimension</code>
 * MDX builtin function.
 *
 * @author jhyde
 * @since Jul 20, 2009
 */
class MemberDimensionFunDef extends FunDefBase {
    public static final FunDefBase INSTANCE = new MemberDimensionFunDef();

    private MemberDimensionFunDef() {
        super(
            "Dimension",
            "Returns the dimension that contains a specified member.", "pdm");
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler)
    {
        final MemberCalc memberCalc =
            compiler.compileMember(call.getArg(0));
        return new AbstractDimensionCalc(call.getFunName(),call.getType(), new Calc[] {memberCalc})
        {
            @Override
			public Dimension evaluateDimension(Evaluator evaluator) {
                Member member = memberCalc.evaluateMember(evaluator);
                return member.getDimension();
            }
        };
    }
}

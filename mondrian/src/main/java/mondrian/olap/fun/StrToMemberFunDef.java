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
import mondrian.calc.StringCalc;
import mondrian.calc.impl.AbstractMemberCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;
import mondrian.resource.MondrianResource;

/**
 * Definition of the <code>StrToMember</code> MDX function.
 *
 * <p>Syntax:
 * <blockquote><code>StrToMember(&lt;String Expression&gt;)
 * </code></blockquote>
 */
class StrToMemberFunDef extends FunDefBase {
    public static final FunDef INSTANCE = new StrToMemberFunDef();

    private StrToMemberFunDef() {
        super(
            "StrToMember",
            "Returns a member from a unique name String in MDX format.",
            "fmS");
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final StringCalc memberNameCalc =
            compiler.compileString(call.getArg(0));
        return new AbstractMemberCalc(call.getFunName(),call.getType(), new Calc[] {memberNameCalc}) {
            @Override
			public Member evaluateMember(Evaluator evaluator) {
                String memberName =
                    memberNameCalc.evaluateString(evaluator);
                if (memberName == null) {
                    throw FunUtil.newEvalException(
                        MondrianResource.instance().NullValue.ex());
                }
                return FunUtil.parseMember(evaluator, memberName, null);
            }
        };
    }
}

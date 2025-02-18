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
import mondrian.calc.IntegerCalc;
import mondrian.calc.MemberCalc;
import mondrian.calc.TupleCalc;
import mondrian.calc.impl.AbstractMemberCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.Validator;
import mondrian.olap.type.MemberType;
import mondrian.olap.type.TupleType;
import mondrian.olap.type.Type;

/**
 * Definition of the <code>&lt;Tuple&gt;.Item</code> MDX function.
 *
 * <p>Syntax:
 * <blockquote><code>
 * &lt;Tuple&gt;.Item(&lt;Index&gt;)<br/>
 * </code></blockquote>
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
class TupleItemFunDef extends FunDefBase {
    static final TupleItemFunDef instance = new TupleItemFunDef();

    private TupleItemFunDef() {
        super(
            "Item",
            "Returns a member from the tuple specified in <Tuple>. The member to be returned is specified by the zero-based position of the member in the set in <Index>.",
            "mmtn");
    }

    @Override
	public Type getResultType(Validator validator, Exp[] args) {
        // Suppose we are called as follows:
        //   ([Gender].CurrentMember, [Store].CurrentMember).Item(n)
        //
        // We know that our result is a member type, but we don't
        // know which dimension.
        return MemberType.Unknown;
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final Type type = call.getArg(0).getType();
        if (type instanceof MemberType) {
            final MemberCalc memberCalc =
                compiler.compileMember(call.getArg(0));
            final IntegerCalc indexCalc =
                compiler.compileInteger(call.getArg(1));
            return new AbstractMemberCalc(
            		call.getFunName(),call.getType(), new Calc[] {memberCalc, indexCalc})
            {
                @Override
				public Member evaluateMember(Evaluator evaluator) {
                    final Member member =
                            memberCalc.evaluateMember(evaluator);
                    final int index =
                            indexCalc.evaluateInteger(evaluator);
                    if (index != 0) {
                        return null;
                    }
                    return member;
                }
            };
        } else {
            final TupleCalc tupleCalc =
                compiler.compileTuple(call.getArg(0));
            final IntegerCalc indexCalc =
                compiler.compileInteger(call.getArg(1));
            return new AbstractMemberCalc(
            		call.getFunName(),call.getType(), new Calc[] {tupleCalc, indexCalc})
            {
                final Member[] nullTupleMembers =
                        FunUtil.makeNullTuple((TupleType) tupleCalc.getType());
                @Override
				public Member evaluateMember(Evaluator evaluator) {
                    final Member[] members =
                            tupleCalc.evaluateTuple(evaluator);
                    assert members == null
                        || members.length == nullTupleMembers.length;
                    final int index = indexCalc.evaluateInteger(evaluator);
                    if (members == null) {
                        return nullTupleMembers[index];
                    }
                    if (index >= members.length || index < 0) {
                        return null;
                    }
                    return members[index];
                }
            };
        }
    }
}

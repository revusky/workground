/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2002-2017 Hitachi Vantara and others
// All Rights Reserved.
*/
package mondrian.olap.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.daanse.olap.api.model.Dimension;
import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.Member;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.MemberCalc;
import mondrian.calc.TupleCalc;
import mondrian.calc.impl.AbstractCalc;
import mondrian.calc.impl.GenericCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.type.TypeUtil;
import mondrian.resource.MondrianResource;
import mondrian.rolap.RolapCube;
import mondrian.rolap.RolapVirtualCubeMeasure;

/**
 * Definition of the <code>ValidMeasure</code> MDX function.
 *
 * <p>Returns a valid measure in a virtual cube by forcing inapplicable
 * dimensions to their top level.
 *
 * <p>Syntax:
 * <blockquote><code>
 * ValidMeasure(&lt;Tuple&gt;)
 * </code></blockquote>
 *
 * @author kwalker, mpflug
 */
public class ValidMeasureFunDef extends FunDefBase
{
    static final ValidMeasureFunDef instance = new ValidMeasureFunDef();

    private ValidMeasureFunDef() {
        super(
            "ValidMeasure",
                "Returns a valid measure in a virtual cube by forcing inapplicable dimensions to their top level.",
                "fnt");
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final Calc calc;
        final Exp arg = call.getArg(0);
        if (TypeUtil.couldBeMember(arg.getType())) {
            calc = compiler.compileMember(arg);
        } else {
            calc = compiler.compileTuple(arg);
        }
        return new CalcImpl(call, calc);
    }

    private static class CalcImpl
        extends GenericCalc
    {
        private final Calc calc;

        public CalcImpl(ResolvedFunCall call, Calc calc) {
            super(call.getFunName(),call.getType());
            this.calc = calc;
        }

        @Override
		public Object evaluate(Evaluator evaluator) {
            RolapCube baseCube;
            RolapCube virtualCube = (RolapCube) evaluator.getCube();
            final List<Member> memberList = getCalcsMembers(evaluator);

            if (memberList == null || memberList.size() == 0) {
              // there are no members in the ValidMeasure
              return null;
            }

            if (!virtualCube.isVirtual()) {
                // this is not a virtual cube,
                // there's nothing for ValidMeasure to do.
                // just evaluate sub-expression
                evaluator.setContext(memberList.toArray(
                    new Member[memberList.size()]));
                return evaluator.evaluateCurrent();
            }
            // find the measure in the tuple
            int measurePosition = -1;
            for (int i = 0; i < memberList.size(); i++) {
                if (memberList.get(i).getDimension().isMeasures()) {
                    measurePosition = i;
                    break;
                }
            }

            final Member vcMeasure = memberList.get(measurePosition);

            if (!RolapVirtualCubeMeasure.class
                .isAssignableFrom(vcMeasure.getClass()))
            {
                // Cannot use calculated members in ValidMeasure.
                throw MondrianResource.instance()
                    .ValidMeasureUsingCalculatedMember
                    .ex(vcMeasure.getUniqueName());
            }

            baseCube = ((RolapVirtualCubeMeasure)vcMeasure).getCube();

            List<Dimension> vMinusBDimensions =
                getDimensionsToForceToAllLevel(
                    virtualCube, baseCube, memberList);
            // declare members array and fill in with all needed members
            final List<Member> validMeasureMembers =
                new ArrayList<>(memberList);
            // start adding to validMeasureMembers at right place
            for (Dimension vMinusBDimension : vMinusBDimensions) {
                // add default|all member for each hierarchy
                for (final Hierarchy hierarchy
                    : vMinusBDimension.getHierarchies())
                {
                    if (hierarchy.hasAll()) {
                        validMeasureMembers.add(hierarchy.getAllMember());
                    } else {
                        validMeasureMembers.add(hierarchy.getDefaultMember());
                    }
                }
            }
            // this needs to be done before validmeasuremembers are set on the
            // context since calculated members defined on a non joining
            // dimension might have been pulled to default member
            List<Member> calculatedMembers =
                getCalculatedMembersFromContext(evaluator);

            evaluator.setContext(validMeasureMembers);
            evaluator.setContext(calculatedMembers);

            return evaluator.evaluateCurrent();
        }

        private List<Member> getCalcsMembers(Evaluator evaluator) {
            List<Member> memberList;
            if (calc.isWrapperFor(MemberCalc.class)) {
                memberList = Collections.singletonList(
                    calc.unwrap(MemberCalc.class).evaluateMember(evaluator));
            } else {
                final Member[] tupleMembers =
                    calc.unwrap((TupleCalc.class)).evaluateTuple(evaluator);
                if (tupleMembers == null) {
                  memberList = null;
                } else {
                  memberList = Arrays.asList(tupleMembers);
                }
            }
            return memberList;
        }

        private List<Member> getCalculatedMembersFromContext(
            Evaluator evaluator)
        {
            Member[] currentMembers = evaluator.getMembers();
            List<Member> calculatedMembers = new ArrayList<>();
            for (Member currentMember : currentMembers) {
                if (currentMember.isCalculated()) {
                    calculatedMembers.add(currentMember);
                }
            }
            return calculatedMembers;
        }

        @Override
		public Calc[] getCalcs() {
            return new Calc[]{calc};
        }

        private List<Dimension> getDimensionsToForceToAllLevel(
            RolapCube virtualCube,
            RolapCube baseCube,
            List<Member> memberList)
        {
            List<Dimension> vMinusBDimensions = new ArrayList<>();
            Set<Dimension> virtualCubeDims = new HashSet<>(Arrays.asList(virtualCube.getDimensions()));
            Set<Dimension> nonJoiningDims =
                baseCube.nonJoiningDimensions(virtualCubeDims);

            for (Dimension nonJoiningDim : nonJoiningDims) {
                if (!isDimInMembersList(memberList, nonJoiningDim)) {
                    vMinusBDimensions.add(nonJoiningDim);
                }
            }
            return vMinusBDimensions;
        }

        private boolean isDimInMembersList(
            List<Member> members,
            Dimension dimension)
        {
            for (Member member : members) {
                if (member.getName().equalsIgnoreCase(dimension.getName())) {
                    return true;
                }
            }
            return false;
        }

        @Override
		public boolean dependsOn(Hierarchy hierarchy) {
            // depends on all hierarchies
            return AbstractCalc.butDepends(getCalcs(), hierarchy);
        }
    }
}

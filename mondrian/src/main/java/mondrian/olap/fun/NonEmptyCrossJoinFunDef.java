/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2005-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// Copyright (C) 2004-2005 SAS Institute, Inc.
// All Rights Reserved.
*/

package mondrian.olap.fun;

import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.Member;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ListCalc;
import mondrian.calc.ResultStyle;
import mondrian.calc.TupleList;
import mondrian.calc.impl.AbstractListCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;
import mondrian.olap.NativeEvaluator;
import mondrian.olap.SchemaReader;
import mondrian.rolap.RolapEvaluator;


/**
 * Definition of the <code>NonEmptyCrossJoin</code> MDX function.
 *
 * @author jhyde
 * @since Mar 23, 2006
 *
 * author 16 December, 2004
 */
public class NonEmptyCrossJoinFunDef extends CrossJoinFunDef {
    static final ReflectiveMultiResolver Resolver = new ReflectiveMultiResolver(
        "NonEmptyCrossJoin",
            "NonEmptyCrossJoin(<Set1>, <Set2>)",
            "Returns the cross product of two sets, excluding empty tuples and tuples without associated fact table data.",
            new String[]{"fxxx"},
            NonEmptyCrossJoinFunDef.class);

    public NonEmptyCrossJoinFunDef(FunDef dummyFunDef) {
        super(dummyFunDef);
    }

    @Override
	public Calc compileCall(final ResolvedFunCall call, ExpCompiler compiler) {
        final ListCalc listCalc1 = compiler.compileList(call.getArg(0));
        final ListCalc listCalc2 = compiler.compileList(call.getArg(1));
        return new AbstractListCalc(
        		call.getFunName(),call.getType(), new Calc[] {listCalc1, listCalc2}, false)
        {
            @Override
			public TupleList evaluateList(Evaluator evaluator) {
                SchemaReader schemaReader = evaluator.getSchemaReader();

                // Evaluate the arguments in non empty mode, but remove from
                // the slicer any members that will be overridden by args to
                // the NonEmptyCrossjoin function. For example, in
                //
                //   SELECT NonEmptyCrossJoin(
                //       [Store].[USA].Children,
                //       [Product].[Beer].Children)
                //    FROM [Sales]
                //    WHERE [Store].[Mexico]
                //
                // we want all beers, not just those sold in Mexico.
                final int savepoint = evaluator.savepoint();
                try {
                    evaluator.setNonEmpty(true);
                    for (Member member
                        : ((RolapEvaluator) evaluator).getSlicerMembers())
                    {
                        if (getType().getElementType().usesHierarchy(
                                member.getHierarchy(), true))
                        {
                            evaluator.setContext(
                                member.getHierarchy().getAllMember());
                        }
                    }

                    NativeEvaluator nativeEvaluator =
                        schemaReader.getNativeSetEvaluator(
                            call.getFunDef(), call.getArgs(), evaluator, this);
                    if (nativeEvaluator != null) {
                        evaluator.restore(savepoint);
                        return
                            (TupleList) nativeEvaluator.execute(
                                ResultStyle.LIST);
                    }

                    final TupleList list1 = listCalc1.evaluateList(evaluator);
                    if (list1.isEmpty()) {
                        evaluator.restore(savepoint);
                        return list1;
                    }
                    final TupleList list2 = listCalc2.evaluateList(evaluator);
                    TupleList result = CrossJoinFunDef.mutableCrossJoin(list1, list2);

                    // remove any remaining empty crossings from the result
                    result = nonEmptyList(evaluator, result, call);
                    return result;
                } finally {
                    evaluator.restore(savepoint);
                }
            }

            @Override
			public boolean dependsOn(Hierarchy hierarchy) {
                if (super.dependsOn(hierarchy)) {
                    return true;
                }
                // Member calculations generate members, which mask the actual
                // expression from the inherited context.
                if (listCalc1.getType().usesHierarchy(hierarchy, true)) {
                    return false;
                }
                if (listCalc2.getType().usesHierarchy(hierarchy, true)) {
                    return false;
                }
                // The implicit value expression, executed to figure out
                // whether a given tuple is empty, depends upon all dimensions.
                return true;
            }
        };
    }

}

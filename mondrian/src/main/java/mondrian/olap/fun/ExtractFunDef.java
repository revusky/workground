/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.Member;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ListCalc;
import mondrian.calc.TupleCollections;
import mondrian.calc.TupleList;
import mondrian.calc.impl.AbstractListCalc;
import mondrian.mdx.DimensionExpr;
import mondrian.mdx.HierarchyExpr;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Category;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.FunDef;
import mondrian.olap.Syntax;
import mondrian.olap.Util;
import mondrian.olap.Validator;
import mondrian.olap.type.MemberType;
import mondrian.olap.type.SetType;
import mondrian.olap.type.TupleType;
import mondrian.olap.type.Type;

/**
 * Definition of the <code>Extract</code> MDX function.
 *
 * <p>Syntax:
 * <blockquote><code>Extract(&lt;Set&gt;, &lt;Hierarchy&gt;[,
 * &lt;Hierarchy&gt;...])</code></blockquote>
 *
 * @author jhyde
 * @since Jun 10, 2007
 */
class ExtractFunDef extends FunDefBase {
    static final ResolverBase Resolver = new ResolverBase(
        "Extract",
        "Extract(<Set>, <Hierarchy>[, <Hierarchy>...])",
        "Returns a set of tuples from extracted hierarchy elements. The opposite of Crossjoin.",
        Syntax.Function)
    {
        @Override
		public FunDef resolve(
            Exp[] args,
            Validator validator,
            List<Conversion> conversions)
        {
            if (args.length < 2) {
                return null;
            }
            if (!validator.canConvert(0, args[0], Category.SET, conversions)) {
                return null;
            }
            for (int i = 1; i < args.length; ++i) {
                if (!validator.canConvert(
                        0, args[i], Category.HIERARCHY, conversions))
                {
                    return null;
                }
            }

            // Find the dimensionality of the set expression.

            // Form a list of ordinals of the hierarchies being extracted.
            // For example, in
            //   Extract(X.Members * Y.Members * Z.Members, Z, X)
            // the hierarchy ordinals are X=0, Y=1, Z=2, and the extracted
            // ordinals are {2, 0}.
            //
            // Each hierarchy extracted must exist in the LHS,
            // and no hierarchy may be extracted more than once.
            List<Integer> extractedOrdinals = new ArrayList<>();
            final List<Hierarchy> extractedHierarchies =
                new ArrayList<>();
            ExtractFunDef.findExtractedHierarchies(
                args, extractedHierarchies, extractedOrdinals);
            int[] parameterTypes = new int[args.length];
            parameterTypes[0] = Category.SET;
            Arrays.fill(
                parameterTypes, 1, parameterTypes.length, Category.HIERARCHY);
            return new ExtractFunDef(this, Category.SET, parameterTypes);
        }
    };

    private ExtractFunDef(
        Resolver resolver, int returnType, int[] parameterTypes)
    {
        super(resolver, returnType, parameterTypes);
    }

    @Override
	public Type getResultType(Validator validator, Exp[] args) {
        final List<Hierarchy> extractedHierarchies =
            new ArrayList<>();
        final List<Integer> extractedOrdinals = new ArrayList<>();
        ExtractFunDef.findExtractedHierarchies(args, extractedHierarchies, extractedOrdinals);
        if (extractedHierarchies.size() == 1) {
            return new SetType(
                MemberType.forHierarchy(
                    extractedHierarchies.get(0)));
        } else {
            List<Type> typeList = new ArrayList<>();
            for (Hierarchy extractedHierarchy : extractedHierarchies) {
                typeList.add(
                    MemberType.forHierarchy(
                        extractedHierarchy));
            }
            return new SetType(
                new TupleType(
                    typeList.toArray(new Type[typeList.size()])));
        }
    }

    private static void findExtractedHierarchies(
        Exp[] args,
        List<Hierarchy> extractedHierarchies,
        List<Integer> extractedOrdinals)
    {
        SetType type = (SetType) args[0].getType();
        final List<Hierarchy> hierarchies;
        if (type.getElementType() instanceof TupleType tupleType) {
            hierarchies = tupleType.getHierarchies();
        } else {
            hierarchies = Collections.singletonList(type.getHierarchy());
        }
        for (Hierarchy hierarchy : hierarchies) {
            if (hierarchy == null) {
                throw new FunctionException(
                    "hierarchy of argument not known");
            }
        }

        for (int i = 1; i < args.length; i++) {
            Exp arg = args[i];
            Hierarchy extractedHierarchy = null;
            if (arg instanceof HierarchyExpr hierarchyExpr) {
                extractedHierarchy = hierarchyExpr.getHierarchy();
            } else if (arg instanceof DimensionExpr dimensionExpr) {
                extractedHierarchy =
                    dimensionExpr.getDimension().getHierarchy();
            }
            if (extractedHierarchy == null) {
                throw new FunctionException("not a constant hierarchy: " + arg);
            }
            int ordinal = hierarchies.indexOf(extractedHierarchy);
            if (ordinal == -1) {
                throw new FunctionException(
                    new StringBuilder("hierarchy ")
                    .append(extractedHierarchy.getUniqueName())
                    .append(" is not a hierarchy of the expression ").append(args[0]).toString());
            }
            if (extractedOrdinals.indexOf(ordinal) >= 0) {
                throw new FunctionException(
                    new StringBuilder("hierarchy ")
                    .append(extractedHierarchy.getUniqueName())
                    .append(" is extracted more than once").toString());
            }
            extractedOrdinals.add(ordinal);
            extractedHierarchies.add(extractedHierarchy);
        }
    }

    private static int[] toIntArray(List<Integer> integerList) {
        final int[] ints = new int[integerList.size()];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = integerList.get(i);
        }
        return ints;
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        List<Hierarchy> extractedHierarchyList = new ArrayList<>();
        List<Integer> extractedOrdinalList = new ArrayList<>();
        ExtractFunDef.findExtractedHierarchies(
            call.getArgs(),
            extractedHierarchyList,
            extractedOrdinalList);
        Util.assertTrue(
            extractedOrdinalList.size() == extractedHierarchyList.size());
        Exp arg = call.getArg(0);
        final ListCalc listCalc = compiler.compileList(arg, false);
        int inArity = arg.getType().getArity();
        final int outArity = extractedOrdinalList.size();
        if (inArity == 1) {
            // LHS is a set of members, RHS is the same hierarchy. Extract boils
            // down to eliminating duplicate members.
            Util.assertTrue(outArity == 1);
            return new DistinctFunDef.CalcImpl(call, listCalc);
        }
        final int[] extractedOrdinals = ExtractFunDef.toIntArray(extractedOrdinalList);
        return new AbstractListCalc(call.getFunName(),call.getType(), new Calc[]{listCalc}) {
            @Override
			public TupleList evaluateList(Evaluator evaluator) {
                TupleList result = TupleCollections.createList(outArity);
                TupleList list = listCalc.evaluateList(evaluator);
                Set<List<Member>> emittedTuples = new HashSet<>();
                for (List<Member> members : list.project(extractedOrdinals)) {
                    if (emittedTuples.add(members)) {
                        result.add(members);
                    }
                }
                return result;
            }
        };
    }
}

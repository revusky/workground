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
import java.util.List;

import org.eclipse.daanse.olap.api.model.Dimension;
import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.Member;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.StringCalc;
import mondrian.calc.impl.AbstractMemberCalc;
import mondrian.calc.impl.AbstractTupleCalc;
import mondrian.mdx.DimensionExpr;
import mondrian.mdx.HierarchyExpr;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Category;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.FunDef;
import mondrian.olap.Syntax;
import mondrian.olap.Validator;
import mondrian.olap.type.MemberType;
import mondrian.olap.type.NullType;
import mondrian.olap.type.StringType;
import mondrian.olap.type.TupleType;
import mondrian.olap.type.Type;
import mondrian.olap.type.TypeUtil;
import mondrian.resource.MondrianResource;

/**
 * Definition of the <code>StrToTuple</code> MDX function.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
class StrToTupleFunDef extends FunDefBase {
    static final ResolverImpl Resolver = new ResolverImpl();

    private StrToTupleFunDef(int[] parameterTypes) {
        super(
            "StrToTuple",
            null,
            "Constructs a tuple from a string.",
            Syntax.Function, Category.TUPLE, parameterTypes);
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final StringCalc stringCalc = compiler.compileString(call.getArg(0));
        Type elementType = call.getType();
        if (elementType instanceof MemberType) {
            final Hierarchy hierarchy = elementType.getHierarchy();
            return new AbstractMemberCalc(call.getFunName(),call.getType(), new Calc[] {stringCalc}) {
                @Override
				public Member evaluateMember(Evaluator evaluator) {
                    String string = stringCalc.evaluateString(evaluator);
                    if (string == null) {
                        throw FunUtil.newEvalException(
                            MondrianResource.instance().NullValue.ex());
                    }
                    return FunUtil.parseMember(evaluator, string, hierarchy);
                }
            };
        } else {
            TupleType tupleType = (TupleType) elementType;
            final List<Hierarchy> hierarchies = tupleType.getHierarchies();
            return new AbstractTupleCalc(call.getFunName(),call.getType(), new Calc[] {stringCalc}) {
                @Override
				public Member[] evaluateTuple(Evaluator evaluator) {
                    String string = stringCalc.evaluateString(evaluator);
                    if (string == null) {
                        throw FunUtil.newEvalException(
                            MondrianResource.instance().NullValue.ex());
                    }
                    return FunUtil.parseTuple(evaluator, string, hierarchies);
                }
            };
        }
    }

    @Override
	public Exp createCall(Validator validator, Exp[] args) {
        final int argCount = args.length;
        if (argCount <= 1) {
            throw MondrianResource.instance().MdxFuncArgumentsNum.ex(getName());
        }
        for (int i = 1; i < argCount; i++) {
            final Exp arg = args[i];
            if (arg instanceof DimensionExpr dimensionExpr) {
                Dimension dimension = dimensionExpr.getDimension();
                args[i] = new HierarchyExpr(dimension.getHierarchy());
            } else if (arg instanceof HierarchyExpr) {
                // nothing
            } else {
                throw MondrianResource.instance().MdxFuncNotHier.ex(
                    i + 1, getName());
            }
        }
        return super.createCall(validator, args);
    }

    @Override
	public Type getResultType(Validator validator, Exp[] args) {
        switch (args.length) {
        case 1:
            // This is a call to the standard version of StrToTuple,
            // which doesn't give us any hints about type.
            return new TupleType(null);

        case 2:
            final Type argType = args[1].getType();
            return new MemberType(
                argType.getDimension(),
                argType.getHierarchy(),
                argType.getLevel(),
                null);

        default: {
            // This is a call to Mondrian's extended version of
            // StrToTuple, of the form
            //   StrToTuple(s, <Hier1>, ... , <HierN>)
            //
            // The result is a tuple
            //  (<Hier1>, ... ,  <HierN>)
            final List<MemberType> list = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                Exp arg = args[i];
                final Type type = arg.getType();
                list.add(TypeUtil.toMemberType(type));
            }
            final MemberType[] types =
                list.toArray(new MemberType[list.size()]);
            TupleType.checkHierarchies(types);
            return new TupleType(types);
        }
        }
    }

    private static class ResolverImpl extends ResolverBase {
        ResolverImpl() {
            super(
                "StrToTuple",
                "StrToTuple(<String Expression>)",
                "Constructs a tuple from a string.",
                Syntax.Function);
        }

        @Override
		public FunDef resolve(
            Exp[] args,
            Validator validator,
            List<Conversion> conversions)
        {
            if (args.length < 1) {
                return null;
            }
            Type type = args[0].getType();
            if (!(type instanceof StringType)
                && !(type instanceof NullType))
            {
                return null;
            }
            for (int i = 1; i < args.length; i++) {
                Exp exp = args[i];
                if (!(exp instanceof DimensionExpr
                    || exp instanceof HierarchyExpr))
                {
                    return null;
                }
            }
            int[] argTypes = new int[args.length];
            argTypes[0] = Category.STRING;
            for (int i = 1; i < argTypes.length; i++) {
                argTypes[i] = Category.HIERARCHY;
            }
            return new StrToTupleFunDef(argTypes);
        }

        @Override
		public FunDef getRepresentativeFunDef() {
            return new StrToTupleFunDef(new int[] {Category.STRING});
        }
    }
}

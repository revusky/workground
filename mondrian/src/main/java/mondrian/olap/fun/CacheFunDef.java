/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun;

import java.io.PrintWriter;
import java.util.List;

import org.eigenbase.xom.XOMUtil;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ResultStyle;
import mondrian.calc.impl.GenericCalc;
import mondrian.calc.impl.GenericIterCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.ExpCacheDescriptor;
import mondrian.olap.FunDef;
import mondrian.olap.Syntax;
import mondrian.olap.Validator;
import mondrian.olap.type.SetType;
import mondrian.olap.type.Type;

/**
 * Definition of the <code>Cache</code> system function, which is smart enough
 * to evaluate its argument only once.
 *
 * @author jhyde
 * @since 2005/8/14
 */
public class CacheFunDef extends FunDefBase {
    static final String NAME = "Cache";
    private static final String SIGNATURE_VALUE = "Cache(<<Exp>>)";
    private static final String DESCRIPTION =
        "Evaluates and returns its sole argument, applying statement-level caching";
    private static final Syntax SYNTAX = Syntax.Function;
    static final CacheFunResolver Resolver = new CacheFunResolver();

    CacheFunDef(
        String name,
        String signature,
        String description,
        Syntax syntax,
        int category,
        Type type)
    {
        super(
            name, signature, description, syntax,
            category, new int[] {category});
        XOMUtil.discard(type);
    }

    @Override
	public void unparse(Exp[] args, PrintWriter pw) {
        args[0].unparse(pw);
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final Exp exp = call.getArg(0);
        final ExpCacheDescriptor cacheDescriptor =
                new ExpCacheDescriptor(exp, compiler);
        if (call.getType() instanceof SetType) {
            return new GenericIterCalc(call.getFunName(),call.getType()) {
                @Override
				public Object evaluate(Evaluator evaluator) {
                    return evaluator.getCachedResult(cacheDescriptor);
                }

                @Override
				public Calc[] getCalcs() {
                    return new Calc[] {cacheDescriptor.getCalc()};
                }

                @Override
				public ResultStyle getResultStyle() {
                    // cached lists are immutable
                    return ResultStyle.LIST;
                }
            };
        } else {
            return new GenericCalc(call.getFunName(),call.getType()) {
                @Override
				public Object evaluate(Evaluator evaluator) {
                    return evaluator.getCachedResult(cacheDescriptor);
                }

                @Override
				public Calc[] getCalcs() {
                    return new Calc[] {cacheDescriptor.getCalc()};
                }

                @Override
				public ResultStyle getResultStyle() {
                    return ResultStyle.VALUE;
                }
            };
        }
    }

    public static class CacheFunResolver extends ResolverBase {
        CacheFunResolver() {
            super(CacheFunDef.NAME, CacheFunDef.SIGNATURE_VALUE, CacheFunDef.DESCRIPTION, CacheFunDef.SYNTAX);
        }

        @Override
		public FunDef resolve(
            Exp[] args,
            Validator validator,
            List<Conversion> conversions)
        {
            if (args.length != 1) {
                return null;
            }
            final Exp exp = args[0];
            final int category = exp.getCategory();
            final Type type = exp.getType();
            return new CacheFunDef(
                CacheFunDef.NAME, CacheFunDef.SIGNATURE_VALUE, CacheFunDef.DESCRIPTION, CacheFunDef.SYNTAX,
                category, type);
        }

        @Override
		public boolean requiresExpression(int k) {
            return false;
        }
    }
}

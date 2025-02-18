/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.mdx;

import java.util.List;

import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.NamedSet;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ResultStyle;
import mondrian.calc.TupleIterable;
import mondrian.calc.impl.AbstractIterCalc;
import mondrian.olap.Category;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.ExpBase;
import mondrian.olap.Util;
import mondrian.olap.Validator;
import mondrian.olap.type.Type;

/**
 * Usage of a {@link org.eclipse.daanse.olap.api.model.NamedSet} in an MDX expression.
 *
 * @author jhyde
 * @since Sep 26, 2005
 */
public class NamedSetExpr extends ExpBase implements Exp {
    private final NamedSet namedSet;

    /**
     * Creates a usage of a named set.
     *
     * @param namedSet namedSet
     * @pre NamedSet != null
     */
    public NamedSetExpr(NamedSet namedSet) {
        Util.assertPrecondition(namedSet != null, "namedSet != null");
        this.namedSet = namedSet;
    }

    /**
     * Returns the named set.
     *
     * @post return != null
     */
    public NamedSet getNamedSet() {
        return namedSet;
    }

    @Override
	public String toString() {
        return namedSet.getUniqueName();
    }

    @Override
	public NamedSetExpr cloneExp() {
        return new NamedSetExpr(namedSet);
    }

    @Override
	public int getCategory() {
        return Category.SET;
    }

    @Override
	public Exp accept(Validator validator) {
        // A set is sometimes used in more than one cube. So, clone the
        // expression and re-validate every time it is used.
        //
        // But keep the expression wrapped in a NamedSet, so that the
        // expression is evaluated once per query. (We don't want the
        // expression to be evaluated context-sensitive.)
        NamedSet namedSet2 = namedSet.validate(validator);
        if (namedSet2 == namedSet) {
            return this;
        }
        return new NamedSetExpr(namedSet2);
    }

    @Override
	public Calc accept(ExpCompiler compiler) {
        // This is a deliberate breach of the usual rules for interpreting
        // acceptable result styles. Usually the caller gets to call the shots:
        // the callee iterates over the acceptable styles and implements in the
        // first style it is able to. But in this case, we return iterable if
        // the caller can handle it, even if it isn't the caller's first choice.
        // This is because the .current and .currentOrdinal functions only
        // work correctly on iterators.
        final List<ResultStyle> styleList =
            compiler.getAcceptableResultStyles();
        if (!styleList.contains(ResultStyle.ITERABLE)
            && !styleList.contains(ResultStyle.ANY))
        {
            return null;
        }

        return new AbstractIterCalc(
            getNamedSet().getName(),getType(),
            new Calc[]{/* todo: compile namedSet.getExp() */})
        {
            @Override
			public TupleIterable evaluateIterable(
                Evaluator evaluator)
            {
                final Evaluator.NamedSetEvaluator eval = getEval(evaluator);
                return eval.evaluateTupleIterable(evaluator);
            }

            @Override
			public boolean dependsOn(Hierarchy hierarchy) {
                // Given that a named set is never re-evaluated within the
                // scope of a query, effectively it's independent of all
                // dimensions.
                return false;
            }
        };
    }

    public Evaluator.NamedSetEvaluator getEval(Evaluator evaluator) {
        return evaluator.getNamedSetEvaluator(namedSet, true);
    }

    @Override
	public Object accept(MdxVisitor visitor) {
        Object o = visitor.visit(this);
        if (visitor.shouldVisitChildren()) {
            namedSet.getExp().accept(visitor);
        }
        return o;
    }

    @Override
	public Type getType() {
        return namedSet.getType();
    }

}

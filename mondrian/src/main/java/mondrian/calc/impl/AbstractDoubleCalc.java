/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package mondrian.calc.impl;

import mondrian.calc.Calc;
import mondrian.calc.DoubleCalc;
import mondrian.olap.Evaluator;
import mondrian.olap.fun.FunUtil;
import mondrian.olap.type.NumericType;
import mondrian.olap.type.Type;

/**
 * Abstract implementation of the {@link mondrian.calc.DoubleCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateDouble(mondrian.olap.Evaluator)} method,
 * and the {@link #evaluate(mondrian.olap.Evaluator)} method will call it.
 *
 * @author jhyde
 * @since Sep 27, 2005
 */
public abstract class AbstractDoubleCalc
extends AbstractCalc
implements DoubleCalc
{
    /**
     * Creates an AbstractDoubleCalc.
     *
     * @param exp Source expression
     * @param calcs Child compiled expressions
     */
    protected AbstractDoubleCalc(String name, Type type, Calc[] calcs) {
        super(name,type, calcs);
        assert getType() instanceof NumericType;
    }

    @Override
    public Object evaluate(Evaluator evaluator) {
        final double d = evaluateDouble(evaluator);
        if (d == FunUtil.DOUBLE_NULL) {
            return null;
        }
        return Double.valueOf(d);
    }
}

/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package mondrian.calc.impl;

import mondrian.calc.BooleanCalc;
import mondrian.calc.Calc;
import mondrian.olap.Evaluator;
import mondrian.olap.type.Type;

/**
 * Abstract implementation of the {@link mondrian.calc.BooleanCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateBoolean(mondrian.olap.Evaluator)} method,
 * and the {@link #evaluate(mondrian.olap.Evaluator)} method will call it.
 *
 * @author jhyde
 * @since Sep 26, 2005
 */
public abstract class AbstractBooleanCalc
extends AbstractCalc
implements BooleanCalc
{
    /**
     * Creates an AbstractBooleanCalc.
     *
     * @param name
     * @param calcs Child compiled expressions
     */
    protected AbstractBooleanCalc(String name, Type type,Calc[] calcs) {
        super(name,type, calcs);
    }

    @Override
    public Object evaluate(Evaluator evaluator) {
        return Boolean.valueOf(evaluateBoolean(evaluator));
    }
}

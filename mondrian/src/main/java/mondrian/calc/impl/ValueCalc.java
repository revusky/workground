/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package mondrian.calc.impl;

import org.eclipse.daanse.olap.api.model.Hierarchy;

import mondrian.calc.Calc;
import mondrian.olap.Evaluator;
import mondrian.olap.type.Type;

/**
 * Expression which yields the value of the current member in the current
 * dimensional context.
 *
 * @see mondrian.calc.impl.MemberValueCalc
 *
 * @author jhyde
 * @since Sep 27, 2005
 */
public class ValueCalc extends GenericCalc {
    /**
     * Creates a ValueCalc.
     *
     * @param exp Source expression
     */
    public ValueCalc( Type type) {
        super("ValueCalc",type, new Calc[0]);
    }

    @Override
    public Object evaluate(Evaluator evaluator) {
        return evaluator.evaluateCurrent();
    }

    @Override
    public boolean dependsOn(Hierarchy hierarchy) {
        return true;
    }
}

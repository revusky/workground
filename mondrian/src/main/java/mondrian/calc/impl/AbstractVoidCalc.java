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
import mondrian.calc.VoidCalc;
import mondrian.olap.Evaluator;
import mondrian.olap.type.Type;

/**
 * Abstract implementation of the {@link mondrian.calc.VoidCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateVoid(mondrian.olap.Evaluator)} method,
 * and the {@link #evaluate(mondrian.olap.Evaluator)} method will call it
 * and return <code>null</code>.
 *
 * @author jhyde
 * @since Sep 29, 2005
 */
public class AbstractVoidCalc extends GenericCalc implements VoidCalc {
    private final Calc[] calcs;

    protected AbstractVoidCalc(String name, Type type, Calc[] calcs) {
        super(name,type);
        this.calcs = calcs;
    }

    @Override
    public Object evaluate(Evaluator evaluator) {
        evaluateVoid(evaluator);
        return null;
    }

    @Override
    public Calc[] getCalcs() {
        return calcs;
    }
}

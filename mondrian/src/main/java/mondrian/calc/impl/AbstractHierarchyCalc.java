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
import mondrian.calc.HierarchyCalc;
import mondrian.olap.Evaluator;
import mondrian.olap.type.HierarchyType;
import mondrian.olap.type.Type;

/**
 * Abstract implementation of the {@link mondrian.calc.HierarchyCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateHierarchy(mondrian.olap.Evaluator)} method,
 * and the {@link #evaluate(mondrian.olap.Evaluator)} method will call it.
 *
 * @author jhyde
 * @since Sep 26, 2005
 */
public abstract class AbstractHierarchyCalc
extends AbstractCalc
implements HierarchyCalc
{
    /**
     * Creates an AbstractHierarchyCalc.
     *
     * @param exp Source expression
     * @param calcs Child compiled expressions
     */
    protected AbstractHierarchyCalc(String name, Type type, Calc[] calcs) {
        super(name,type, calcs);
        assert getType() instanceof HierarchyType;
    }

    @Override
    public Object evaluate(Evaluator evaluator) {
        return evaluateHierarchy(evaluator);
    }
}

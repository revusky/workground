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
import mondrian.calc.IterCalc;
import mondrian.calc.TupleCollections;
import mondrian.calc.TupleCursor;
import mondrian.calc.TupleIterable;
import mondrian.calc.TupleList;
import mondrian.olap.Evaluator;

/**
 * Adapter that converts a {@link mondrian.calc.IterCalc} to a
 * {@link mondrian.calc.ListCalc}.
 *
 * @author jhyde
 * @since Oct 23, 2008
 */
public class IterableListCalc extends AbstractListCalc {
    private final IterCalc iterCalc;

    /**
     * Creates an IterableListCalc.
     *
     * @param iterCalc Calculation that returns an iterable.
     */
    public IterableListCalc(IterCalc iterCalc) {
        super("IterableListCalc",iterCalc.getType(), new Calc[] {iterCalc});
        this.iterCalc = iterCalc;
    }

    @Override
    public TupleList evaluateList(Evaluator evaluator) {
        // A TupleIterCalc is allowed to return a list. If so, save the copy.
        final TupleIterable iterable =
                iterCalc.evaluateIterable(evaluator);
        if (iterable instanceof TupleList tupleList) {
            return tupleList;
        }

        final TupleList list = TupleCollections.createList(iterable.getArity());
        final TupleCursor tupleCursor = iterable.tupleCursor();
        while (tupleCursor.forward()) {
            // REVIEW: Worth creating TupleList.addAll(TupleCursor)?
            list.addCurrent(tupleCursor);
        }
        return list;
    }
}

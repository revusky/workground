/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
 */

package mondrian.calc.impl;

import mondrian.calc.Calc;
import mondrian.calc.ListCalc;
import mondrian.calc.ResultStyle;
import mondrian.calc.TupleIterable;
import mondrian.calc.TupleList;
import mondrian.olap.Evaluator;
import mondrian.olap.type.SetType;
import mondrian.olap.type.Type;

/**
 * Abstract implementation of the {@link mondrian.calc.ListCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateList(mondrian.olap.Evaluator)} method, and the {@link
 * #evaluate(mondrian.olap.Evaluator)} method will call it.
 *
 * @author jhyde
 * @since Sep 27, 2005
 */
public abstract class AbstractListCalc
extends AbstractCalc
implements ListCalc {
    private final boolean mutable;

    /**
     * Creates an abstract implementation of a compiled expression which returns a mutable list of tuples.
     *
     * @param exp   Expression which was compiled
     * @param calcs List of child compiled expressions (for dependency analysis)
     */
    protected AbstractListCalc( String name, Type type, Calc[] calcs ) {
        this( name,type, calcs, true );
    }

    /**
     * Creates an abstract implementation of a compiled expression which returns a list.
     *
     * @param exp     Expression which was compiled
     * @param calcs   List of child compiled expressions (for dependency analysis)
     * @param mutable Whether the list is mutable
     */
    protected AbstractListCalc( String name, Type type, Calc[] calcs, boolean mutable ) {
        super( name,type, calcs );
        this.mutable = mutable;
        assert type instanceof SetType : "expecting a set: " + getType();
    }

    @Override
    public SetType getType() {
        return (SetType) super.getType();
    }

    @Override
    public final Object evaluate( Evaluator evaluator ) {
        final TupleList tupleList = evaluateList( evaluator );
        assert tupleList != null : "null as empty tuple list is deprecated";
        return tupleList;
    }

    @Override
    public TupleIterable evaluateIterable( Evaluator evaluator ) {
        return evaluateList( evaluator );
    }

    @Override
    public ResultStyle getResultStyle() {
        return mutable
                ? ResultStyle.MUTABLE_LIST
                        : ResultStyle.LIST;
    }

    @Override
    public String toString() {
        return "AbstractListCalc object";
    }
}

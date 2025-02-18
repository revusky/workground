/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
 */

package mondrian.olap.fun;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ListCalc;
import mondrian.calc.TupleList;
import mondrian.calc.impl.AbstractListCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;
import mondrian.olap.fun.sort.Sorter;

/**
 * Definition of the <code>Hierarchize</code> MDX function.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
class HierarchizeFunDef extends FunDefBase {
  static final String[] prePost = { "PRE", "POST" };
  static final ReflectiveMultiResolver Resolver =
    new ReflectiveMultiResolver(
      "Hierarchize",
      "Hierarchize(<Set>[, POST])",
      "Orders the members of a set in a hierarchy.",
      new String[] { "fxx", "fxxy" },
      HierarchizeFunDef.class,
      HierarchizeFunDef.prePost );

  public HierarchizeFunDef( FunDef dummyFunDef ) {
    super( dummyFunDef );
  }

  @Override
public Calc compileCall( ResolvedFunCall call, ExpCompiler compiler ) {
    final ListCalc listCalc =
      compiler.compileList( call.getArg( 0 ), true );
    String order = FunUtil.getLiteralArg( call, 1, "PRE", HierarchizeFunDef.prePost );
    final boolean post = order.equals( "POST" );
    return new AbstractListCalc( call.getFunName(),call.getType(), new Calc[] { listCalc } ) {
      @Override
	public TupleList evaluateList( Evaluator evaluator ) {
        TupleList list = listCalc.evaluateList( evaluator );
        return Sorter.hierarchizeTupleList( list, post );
      }
    };
  }
}

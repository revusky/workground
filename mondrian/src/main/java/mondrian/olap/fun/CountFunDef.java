/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2021 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun;

import org.eclipse.daanse.olap.api.model.Hierarchy;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.IterCalc;
import mondrian.calc.ListCalc;
import mondrian.calc.ResultStyle;
import mondrian.calc.TupleIterable;
import mondrian.calc.TupleList;
import mondrian.calc.impl.AbstractIntegerCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Evaluator;
import mondrian.olap.FunDef;
import mondrian.olap.Literal;

/**
 * Definition of the <code>Count</code> MDX function.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
class CountFunDef extends AbstractAggregateFunDef {
  static final String[] ReservedWords = new String[] { "INCLUDEEMPTY", "EXCLUDEEMPTY" };

  static final ReflectiveMultiResolver Resolver =
      new ReflectiveMultiResolver( "Count", "Count(<Set>[, EXCLUDEEMPTY | INCLUDEEMPTY])",
          "Returns the number of tuples in a set, empty cells included unless the optional EXCLUDEEMPTY flag is used.",
          new String[] { "fnx", "fnxy" }, CountFunDef.class, CountFunDef.ReservedWords );
  private static final String TIMING_NAME = CountFunDef.class.getSimpleName();

  public CountFunDef( FunDef dummyFunDef ) {
    super( dummyFunDef );
  }

  @Override
public Calc compileCall( ResolvedFunCall call, ExpCompiler compiler ) {
    final Calc calc = compiler.compileAs( call.getArg( 0 ), null, ResultStyle.ITERABLE_ANY );
    final boolean includeEmpty =
        call.getArgCount() < 2 || ( (Literal) call.getArg( 1 ) ).getValue().equals( "INCLUDEEMPTY" );
    return new AbstractIntegerCalc( call.getFunName(),call.getType(), new Calc[] { calc } ) {
      @Override
	public int evaluateInteger( Evaluator evaluator ) {
        evaluator.getTiming().markStart( CountFunDef.TIMING_NAME );
        final int savepoint = evaluator.savepoint();
        try {
          evaluator.setNonEmpty( false );
          final int count;
          if ( calc instanceof IterCalc iterCalc ) {
            TupleIterable iterable = evaluateCurrentIterable( iterCalc, evaluator );
            count = FunUtil.count( evaluator, iterable, includeEmpty );
          } else {
            // must be ListCalc
            ListCalc listCalc = (ListCalc) calc;
            TupleList list = AbstractAggregateFunDef.evaluateCurrentList( listCalc, evaluator );
            count = FunUtil.count( evaluator, list, includeEmpty );
          }
          return count;
        } finally {
          evaluator.restore( savepoint );
          evaluator.getTiming().markEnd( CountFunDef.TIMING_NAME );
        }
      }

      @Override
	public boolean dependsOn( Hierarchy hierarchy ) {
        // COUNT(<set>, INCLUDEEMPTY) is straightforward -- it
        // depends only on the dimensions that <Set> depends
        // on.
        if ( super.dependsOn( hierarchy ) ) {
          return true;
        }
        if ( includeEmpty ) {
          return false;
        }
        // COUNT(<set>, EXCLUDEEMPTY) depends only on the
        // dimensions that <Set> depends on, plus all
        // dimensions not masked by the set.
        return !calc.getType().usesHierarchy( hierarchy, true );
      }
    };
  }
}

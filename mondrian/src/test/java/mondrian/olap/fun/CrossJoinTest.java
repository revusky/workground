/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
*/
package mondrian.olap.fun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.daanse.olap.api.Connection;
import org.eclipse.daanse.olap.api.model.Member;
import org.eclipse.daanse.olap.api.result.Position;
import org.eclipse.daanse.olap.api.result.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.TestUtil;
import org.opencube.junit5.context.TestingContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.TupleCursor;
import mondrian.calc.TupleIterable;
import mondrian.calc.TupleList;
import mondrian.calc.impl.ArrayTupleList;
import mondrian.calc.impl.UnaryTupleList;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Exp;
import mondrian.olap.FunDef;
import mondrian.olap.MondrianProperties;
import mondrian.olap.SchemaReader;
import mondrian.olap.Syntax;
import mondrian.olap.Util;
import mondrian.olap.Validator;
import mondrian.olap.type.MemberType;
import mondrian.olap.type.SetType;
import mondrian.olap.type.TupleType;
import mondrian.olap.type.Type;
import mondrian.rolap.RolapCube;
import mondrian.server.Execution;
import mondrian.server.Locus;
import mondrian.test.PropertySaver5;

/**
 * <code>CrossJoint</code> tests the collation order of positive and negative
 * infinity, and {@link Double#NaN}.
 *
 * @author <a>Richard M. Emberson</a>
 * @since Jan 14, 2007
 */

public class CrossJoinTest {

  private static final String SELECT_GENDER_MEMBERS =
    "select Gender.members on 0 from sales";

  private static final String SALES_CUBE = "Sales";

  private Execution excMock = mock( Execution.class );

  static List<List<Member>> m3 = Arrays.asList(
    Arrays.<Member>asList( new TestMember( "k" ), new TestMember( "l" ) ),
    Arrays.<Member>asList( new TestMember( "m" ), new TestMember( "n" ) ) );

  static List<List<Member>> m4 = Arrays.asList(
    Arrays.<Member>asList( new TestMember( "U" ), new TestMember( "V" ) ),
    Arrays.<Member>asList( new TestMember( "W" ), new TestMember( "X" ) ),
    Arrays.<Member>asList( new TestMember( "Y" ), new TestMember( "Z" ) ) );

  static final Comparator<List<Member>> memberComparator =
    new Comparator<>() {
      @Override
	public int compare( List<Member> ma1, List<Member> ma2 ) {
        for ( int i = 0; i < ma1.size(); i++ ) {
          int c = ma1.get( i ).compareTo( ma2.get( i ) );
          if ( c < 0 ) {
            return c;
          } else if ( c > 0 ) {
            return c;
          }
        }
        return 0;
      }
    };

  private CrossJoinFunDef crossJoinFunDef;

private PropertySaver5 propSaver;

  @BeforeEach
  protected void beforeEach() throws Exception {
    propSaver=new PropertySaver5();
    crossJoinFunDef = new CrossJoinFunDef( new NullFunDef() );
  }

  @AfterEach
  protected void afterEach() throws Exception {
	  propSaver.reset();
  }

  ////////////////////////////////////////////////////////////////////////
  // Iterable
  ////////////////////////////////////////////////////////////////////////

  @Test
  void testListTupleListTupleIterCalc() {
    if ( !Util.RETROWOVEN) {
      propSaver.set( propSaver.properties.CheckCancelOrTimeoutInterval, 0 );
      CrossJoinFunDef.CrossJoinIterCalc calc =
        crossJoinFunDef.new CrossJoinIterCalc( getResolvedFunCall(), null );

      doTupleTupleIterTest( calc, excMock );
    }
  }

  private void doTupleTupleIterTest(
    CrossJoinFunDef.CrossJoinIterCalc calc, Execution execution ) {
    TupleList l4 = makeListTuple( m4 );
    String s4 = toString( l4 );
    String e4 = "{[U, V], [W, X], [Y, Z]}";
    assertEquals( e4, s4 );

    TupleList l3 = makeListTuple( m3 );
    String s3 = toString( l3 );
    String e3 = "{[k, l], [m, n]}";
    assertEquals( e3, s3 );

    String s = Locus.execute(
      execution, "CrossJoinTest", new Locus.Action<String>() {
        @Override
		public String execute() {
          TupleIterable iterable = calc.makeIterable( l4, l3 );
          return CrossJoinTest.this.toString( iterable );
        }
      } );
    String e =
      "{[U, V, k, l], [U, V, m, n], [W, X, k, l], "
        + "[W, X, m, n], [Y, Z, k, l], [Y, Z, m, n]}";
    assertEquals( e, s );
  }

  // The test to verify that cancellation/timeout is checked
  // in CrossJoinFunDef$CrossJoinIterCalc$1$1.forward()
	@ParameterizedTest
	@ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
  void testCrossJoinIterCalc_IterationCancellationOnForward(TestingContext foodMartContext) {
    propSaver.set( propSaver.properties.CheckCancelOrTimeoutInterval, 1 );
    // Get product members as TupleList
   Connection con= foodMartContext.createConnection();
    RolapCube salesCube =
      (RolapCube) TestUtil.cubeByName( con, SALES_CUBE );
    SchemaReader salesCubeSchemaReader =
      salesCube.getSchemaReader( con.getRole() )
        .withLocus();
    TupleList productMembers =
    		TestUtil.productMembersPotScrubbersPotsAndPans( salesCubeSchemaReader );
    // Get genders members as TupleList
    Result genders = TestUtil.executeQuery(con, SELECT_GENDER_MEMBERS );
    TupleList genderMembers = getGenderMembers( genders );

    // Test execution to track cancellation/timeout calls
    Execution execution =
      spy( new Execution( genders.getQuery().getStatement(), 0 ) );
    // check no execution of checkCancelOrTimeout has been yet
    verify( execution, times( 0 ) ).checkCancelOrTimeout();
    Integer crossJoinIterCalc =
      crossJoinIterCalcIterate( productMembers, genderMembers, execution );

    // checkCancelOrTimeout should be called once for the left tuple
    //from CrossJoinIterCalc$1$1.forward() since phase
    // interval is 1
    verify( execution, times( productMembers.size() ) ).checkCancelOrTimeout();
    assertEquals(
      productMembers.size() * genderMembers.size(),
      crossJoinIterCalc.intValue() );
  }

  private static TupleList getGenderMembers( Result genders ) {
    TupleList genderMembers = new UnaryTupleList();
    for ( Position pos : genders.getAxes()[ 0 ].getPositions() ) {
      genderMembers.add( pos );
    }
    return genderMembers;
  }

  private Integer crossJoinIterCalcIterate(
    final TupleList list1, final TupleList list2,
    final Execution execution ) {
    return Locus.execute(
      execution, "CrossJoinTest", new Locus.Action<Integer>() {
        @Override
		public Integer execute() {
          TupleIterable iterable =
            crossJoinFunDef.new CrossJoinIterCalc(
              getResolvedFunCall(), null ).makeIterable( list1, list2 );
          TupleCursor tupleCursor = iterable.tupleCursor();
          // total count of all iterations
          int counter = 0;
          while ( tupleCursor.forward() ) {
            counter++;
          }
          return Integer.valueOf( counter );
        }
      } );
  }

  ////////////////////////////////////////////////////////////////////////
  // Immutable List
  ////////////////////////////////////////////////////////////////////////

  @Test
  void testImmutableListTupleListTupleListCalc() {
    CrossJoinFunDef.ImmutableListCalc calc =
      crossJoinFunDef.new ImmutableListCalc(
        getResolvedFunCall(), null );

    doTupleTupleListTest( calc );
  }

  protected void doTupleTupleListTest(
    CrossJoinFunDef.BaseListCalc calc ) {
    TupleList l4 = makeListTuple( m4 );
    String s4 = toString( l4 );
    String e4 = "{[U, V], [W, X], [Y, Z]}";
    assertEquals( e4, s4 );

    TupleList l3 = makeListTuple( m3 );
    String s3 = toString( l3 );
    String e3 = "{[k, l], [m, n]}";
    assertEquals( e3, s3 );

    TupleList list = calc.makeList( l4, l3 );
    String s = toString( list );
    String e =
      "{[U, V, k, l], [U, V, m, n], [W, X, k, l], "
        + "[W, X, m, n], [Y, Z, k, l], [Y, Z, m, n]}";
    assertEquals( e, s );

    TupleList subList = list.subList( 0, 6 );
    s = toString( subList );
    assertEquals( 6, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 0, 6 );
    s = toString( subList );
    assertEquals( 6, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 1, 5 );
    s = toString( subList );
    e = "{[U, V, m, n], [W, X, k, l], [W, X, m, n], [Y, Z, k, l]}";
    assertEquals( 4, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 2, 4 );
    s = toString( subList );
    e = "{[W, X, m, n], [Y, Z, k, l]}";
    assertEquals( 2, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 1, 2 );
    s = toString( subList );
    e = "{[Y, Z, k, l]}";
    assertEquals( 1, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 1, 4 );
    s = toString( subList );
    e = "{[U, V, m, n], [W, X, k, l], [W, X, m, n]}";
    assertEquals( 3, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 2, 4 );
    s = toString( subList );
    e = "{[W, X, k, l], [W, X, m, n]}";
    assertEquals( 2, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 2, 3 );
    s = toString( subList );
    e = "{[W, X, k, l]}";
    assertEquals( 1, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 4, 4 );
    s = toString( subList );
    e = "{}";
    assertEquals( 0, subList.size() );
    assertEquals( e, s );
  }


  ////////////////////////////////////////////////////////////////////////
  // Mutable List
  ////////////////////////////////////////////////////////////////////////
  @Test
  void testMutableListTupleListTupleListCalc() {
    CrossJoinFunDef.MutableListCalc calc =
      crossJoinFunDef.new MutableListCalc(
        getResolvedFunCall(), null );

    doMTupleTupleListTest( calc );
  }

  protected void doMTupleTupleListTest(
    CrossJoinFunDef.BaseListCalc calc ) {
    TupleList l1 = makeListTuple( m3 );
    String s1 = toString( l1 );
    String e1 = "{[k, l], [m, n]}";
    assertEquals( e1, s1 );

    TupleList l2 = makeListTuple( m4 );
    String s2 = toString( l2 );
    String e2 = "{[U, V], [W, X], [Y, Z]}";
    assertEquals( e2, s2 );

    TupleList list = calc.makeList( l1, l2 );
    String s = toString( list );
    String e = "{[k, l, U, V], [k, l, W, X], [k, l, Y, Z], "
      + "[m, n, U, V], [m, n, W, X], [m, n, Y, Z]}";
    assertEquals( e, s );

    if ( false ) {
      // Cannot apply Collections.reverse to TupleList
      // because TupleList.set always returns null.
      // (This is a violation of the List contract, but it is inefficient
      // to construct a list to return.)
      Collections.reverse( list );
      s = toString( list );
      e = "{[m, n, Y, Z], [m, n, W, X], [m, n, U, V], "
        + "[k, l, Y, Z], [k, l, W, X], [k, l, U, V]}";
      assertEquals( e, s );
    }

    // sort
    Collections.sort( list, memberComparator );
    s = toString( list );
    e = "{[k, l, U, V], [k, l, W, X], [k, l, Y, Z], "
      + "[m, n, U, V], [m, n, W, X], [m, n, Y, Z]}";
    assertEquals( e, s );

    List<Member> members = list.remove( 1 );
    s = toString( list );
    e = "{[k, l, U, V], [k, l, Y, Z], [m, n, U, V], "
      + "[m, n, W, X], [m, n, Y, Z]}";
    assertEquals( e, s );
  }

	@ParameterizedTest
	@ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
void testResultLimitWithinCrossjoin_1(TestingContext foodMartContext) {
	}


	@ParameterizedTest
	@ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
  void testResultLimitWithinCrossjoin(TestingContext foodMartContext) {
    propSaver.set( MondrianProperties.instance().ResultLimit, 1000 );
   Connection connection= foodMartContext.createConnection();
    TestUtil.assertAxisThrows(connection, "Hierarchize(Crossjoin(Union({[Gender].CurrentMember}, [Gender].Children), "
        + "Union({[Product].CurrentMember}, [Product].[Brand Name].Members)))",
      "result (1,539) exceeded limit (1,000)","Sales" );
  }

  ////////////////////////////////////////////////////////////////////////
  // Helper methods
  ////////////////////////////////////////////////////////////////////////
  protected String toString( TupleIterable l ) {
    StringBuffer buf = new StringBuffer( 100 );
    buf.append( '{' );
    int j = 0;
    for ( List<Member> o : l ) {
      if ( j++ > 0 ) {
        buf.append( ", " );
      }
      buf.append( o );
    }
    buf.append( '}' );
    return buf.toString();
  }

  protected TupleList makeListTuple( List<List<Member>> ms ) {
    final TupleList list = new ArrayTupleList( ms.get( 0 ).size() );
    for ( List<Member> m : ms ) {
      list.add( m );
    }
    return list;
  }

  protected ResolvedFunCall getResolvedFunCall() {
    FunDef funDef = new TestFunDef();
    Exp[] args = new Exp[ 0 ];
    Type returnType =
      new SetType(
        new TupleType(
          new Type[] {
            new MemberType( null, null, null, null ),
            new MemberType( null, null, null, null ) } ) );
    return new ResolvedFunCall( funDef, args, returnType );
  }

  ////////////////////////////////////////////////////////////////////////
  // Helper classes
  ////////////////////////////////////////////////////////////////////////
  public static class TestFunDef implements FunDef {
    TestFunDef() {
    }

    @Override
	public Syntax getSyntax() {
      throw new UnsupportedOperationException();
    }

    @Override
	public String getName() {
      return "SomeName";
    }

    @Override
	public String getDescription() {
      throw new UnsupportedOperationException();
    }

    @Override
	public int getReturnCategory() {
      throw new UnsupportedOperationException();
    }

    @Override
	public int[] getParameterCategories() {
      throw new UnsupportedOperationException();
    }

    @Override
	public Exp createCall( Validator validator, Exp[] args ) {
      throw new UnsupportedOperationException();
    }

    @Override
	public String getSignature() {
      throw new UnsupportedOperationException();
    }

    @Override
	public void unparse( Exp[] args, PrintWriter pw ) {
      throw new UnsupportedOperationException();
    }

    @Override
	public Calc compileCall( ResolvedFunCall call, ExpCompiler compiler ) {
      throw new UnsupportedOperationException();
    }
  }

  public static class NullFunDef implements FunDef {
    public NullFunDef() {
    }

    @Override
	public Syntax getSyntax() {
      return Syntax.Function;
    }

    @Override
	public String getName() {
      return "";
    }

    @Override
	public String getDescription() {
      return "";
    }

    @Override
	public int getReturnCategory() {
      return 0;
    }

    @Override
	public int[] getParameterCategories() {
      return new int[ 0 ];
    }

    @Override
	public Exp createCall( Validator validator, Exp[] args ) {
      return null;
    }

    @Override
	public String getSignature() {
      return "";
    }

    @Override
	public void unparse( Exp[] args, PrintWriter pw ) {
      //
    }

    @Override
	public Calc compileCall( ResolvedFunCall call, ExpCompiler compiler ) {
      return null;
    }
  }
}

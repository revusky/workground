/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.udf;

import java.util.regex.Pattern;

import aQute.bnd.annotation.spi.ServiceProvider;
import mondrian.olap.Evaluator;
import mondrian.olap.Syntax;
import mondrian.olap.type.BooleanType;
import mondrian.olap.type.StringType;
import mondrian.olap.type.Type;
import mondrian.spi.UserDefinedFunction;

/**
 * User-defined function <code>MATCHES</code>.
 *
 * @author schoi
 */
@ServiceProvider(value = UserDefinedFunction.class)
public class MatchesUdf implements UserDefinedFunction {

    @Override
	public Object execute(Evaluator evaluator, Argument[] arguments) {
        Object arg0 = arguments[0].evaluateScalar(evaluator);
        Object arg1 = arguments[1].evaluateScalar(evaluator);

        return Boolean.valueOf(Pattern.matches((String)arg1, (String)arg0));
    }

    @Override
	public String getDescription() {
        return "Returns true if the string matches the regular expression.";
    }

    @Override
	public String getName() {
        return "MATCHES";
    }

    @Override
	public Type[] getParameterTypes() {
        return new Type[] {
            new StringType(),
            new StringType()
        };
    }

    @Override
	public String[] getReservedWords() {
        // This function does not require any reserved words.
        return null;
    }

    @Override
	public Type getReturnType(Type[] parameterTypes) {
        return new BooleanType();
    }

    @Override
	public Syntax getSyntax() {
        return Syntax.Infix;
    }

}

/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.rolap;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.ParameterCompilable;
import mondrian.calc.impl.GenericCalc;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.Parameter;
import mondrian.olap.type.Type;
import mondrian.resource.MondrianResource;

/**
 * Parameter that is defined in a schema.
 *
 * @author jhyde
 * @since Jul 20, 2006
 */
public class RolapSchemaParameter implements Parameter, ParameterCompilable {
    private final RolapSchema schema;
    private final String name;
    private String description;
    private String defaultExpString;
    private Type type;
    private final boolean modifiable;
    private Object value;
    private boolean assigned;
    private Object cachedDefaultValue;

    RolapSchemaParameter(
        RolapSchema schema,
        String name,
        String defaultExpString,
        String description,
        Type type,
        boolean modifiable)
    {
        assert defaultExpString != null;
        assert name != null;
        assert schema != null;
        assert type != null;
        this.schema = schema;
        this.name = name;
        this.defaultExpString = defaultExpString;
        this.description = description;
        this.type = type;
        this.modifiable = modifiable;
        schema.parameterList.add(this);
    }

    RolapSchema getSchema() {
        return schema;
    }

    @Override
	public boolean isModifiable() {
        return modifiable;
    }

    @Override
	public Scope getScope() {
        return Scope.Schema;
    }

    @Override
	public Type getType() {
        return type;
    }

    @Override
	public Exp getDefaultExp() {
        throw new UnsupportedOperationException();
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public String getDescription() {
        return description;
    }

    @Override
	public Object getValue() {
        return value;
    }

    @Override
	public void setValue(Object value) {
        if (!modifiable) {
            throw MondrianResource.instance().ParameterIsNotModifiable.ex(
                getName(), getScope().name());
        }
        this.assigned = true;
        this.value = value;
    }

    @Override
	public boolean isSet() {
        return assigned;
    }

    @Override
	public void unsetValue() {
        if (!modifiable) {
            throw MondrianResource.instance().ParameterIsNotModifiable.ex(
                getName(), getScope().name());
        }
        assigned = false;
        value = null;
    }

    @Override
	public Calc compile(ExpCompiler compiler) {
        // Parse and compile the expression for the default value.
        Exp defaultExp = compiler.getValidator()
            .getQuery()
            .getConnection()
            .parseExpression(defaultExpString);
        defaultExp = compiler.getValidator().validate(defaultExp, true);
        final Calc defaultCalc = defaultExp.accept(compiler);

        // Generate a program which looks at the assigned value first,
        // and if it is not set, returns the default expression.
        return new GenericCalc("GenericCalc",defaultExp.getType()) {
            @Override
			public Calc[] getCalcs() {
                return new Calc[] {defaultCalc};
            }

            @Override
			public Object evaluate(Evaluator evaluator) {
                if (value != null) {
                    return value;
                }
                if (cachedDefaultValue == null) {
                    cachedDefaultValue = defaultCalc.evaluate(evaluator);
                }
                return cachedDefaultValue;
            }
        };
    }
}

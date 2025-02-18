/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.olap.fun;

import org.eclipse.daanse.olap.api.model.Hierarchy;
import org.eclipse.daanse.olap.api.model.OlapElement;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.StringCalc;
import mondrian.calc.impl.AbstractHierarchyCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Category;
import mondrian.olap.Evaluator;
import mondrian.olap.Exp;
import mondrian.olap.Util;
import mondrian.olap.Validator;
import mondrian.olap.type.HierarchyType;
import mondrian.olap.type.Type;

/**
 * Definition of the <code>Dimensions(&lt;String Expression&gt;)</code>
 * MDX builtin function.
 *
 * <p>NOTE: Actually returns a hierarchy. This is consistent with Analysis
 * Services.
 *
 * @author jhyde
 * @since Jul 20, 2009
 */
class DimensionsStringFunDef extends FunDefBase {
    public static final FunDefBase INSTANCE = new DimensionsStringFunDef();

    private DimensionsStringFunDef() {
        super(
            "Dimensions",
            "Returns the hierarchy whose name is specified by a string.",
            "fhS");
    }

    @Override
	public Type getResultType(Validator validator, Exp[] args) {
        return HierarchyType.Unknown;
    }

    @Override
	public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler)
    {
        final StringCalc stringCalc =
            compiler.compileString(call.getArg(0));
        return new AbstractHierarchyCalc(call.getFunName(),call.getType(), new Calc[] {stringCalc})
        {
            @Override
			public Hierarchy evaluateHierarchy(Evaluator evaluator) {
                String dimensionName =
                    stringCalc.evaluateString(evaluator);
                return findHierarchy(dimensionName, evaluator);
            }
        };
    }

    /**
     * Looks up a hierarchy in the current cube with a given name.
     *
     * @param name Hierarchy name
     * @param evaluator Evaluator
     * @return Hierarchy
     */
    Hierarchy findHierarchy(String name, Evaluator evaluator) {
        if (name.indexOf("[") == -1) {
            name = Util.quoteMdxIdentifier(name);
        }
        OlapElement o = evaluator.getSchemaReader().lookupCompound(
            evaluator.getCube(),
            Util.parseIdentifier(name),
            false,
            Category.HIERARCHY);
        if (o instanceof Hierarchy hierarchy) {
            return hierarchy;
        } else if (o == null) {
            throw FunUtil.newEvalException(
                this, new StringBuilder("Hierarchy '").append(name).append("' not found").toString());
        } else {
            throw FunUtil.newEvalException(
                this, new StringBuilder("Hierarchy(").append(name).append(") found ").append(o).toString());
        }
    }
}

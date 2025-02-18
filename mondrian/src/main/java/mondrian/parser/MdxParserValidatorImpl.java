/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.parser;

import mondrian.olap.Exp;
import mondrian.olap.FunTable;
import mondrian.olap.Parser;
import mondrian.olap.QueryPart;
import mondrian.server.Statement;

/**
 * Default implementation of {@link mondrian.parser.MdxParserValidator}.
 *
 * @author jhyde
 */
public class MdxParserValidatorImpl implements MdxParserValidator {
    /**
     * Creates a MdxParserValidatorImpl.
     */
    public MdxParserValidatorImpl() {
        // constructor
    }

    @Override
	public QueryPart parseInternal(
        Statement statement,
        String queryString,
        boolean debug,
        FunTable funTable,
        boolean strictValidation)
    {
        return new Parser().parseInternal(
            new Parser.FactoryImpl(),
            statement, queryString, debug, funTable, strictValidation);
    }

    @Override
	public Exp parseExpression(
        Statement statement,
        String queryString,
        boolean debug,
        FunTable funTable)
    {
        return new Parser().parseExpression(
            new Parser.FactoryImpl(),
            statement, queryString, debug, funTable);
    }
}

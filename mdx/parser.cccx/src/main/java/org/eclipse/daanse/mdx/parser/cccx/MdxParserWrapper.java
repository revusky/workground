/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.mdx.parser.cccx;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.DMVStatement;
import org.eclipse.daanse.mdx.model.api.DrillthroughStatement;
import org.eclipse.daanse.mdx.model.api.ExplainStatement;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.RefreshStatement;
import org.eclipse.daanse.mdx.model.api.ReturnItem;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.Expression;
import org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition;
import org.eclipse.daanse.mdx.model.api.select.SelectCellPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectDimensionPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAsteriskClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSlicerAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectWithClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;

public class MdxParserWrapper implements org.eclipse.daanse.mdx.parser.api.MdxParser {

    private MdxParser delegate;

    public MdxParserWrapper(CharSequence mdx) throws MdxParserException {

        if (mdx == null) {

            throw new MdxParserException("statement must not be null");
        } else if (mdx.length() == 0) {

            throw new MdxParserException("statement must not be empty");
        }
        try {
            delegate = new MdxParser(mdx);
        } catch (Exception e) {

            throw new MdxParserException("statement must not be empty");
        }
    }

    @Override
    public MdxStatement parseMdxStatement() throws MdxParserException {
        try {
            return delegate.parseMdxStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }

    }

    private void dump() {
        Node root = delegate.rootNode();
        if (root != null) {
            root.dump();
        }
    }

    @Override
    public SelectQueryAsteriskClause parseSelectQueryAsteriskClause() throws MdxParserException {
        try {
            //return delegate.parseSelectQueryAsteriskClause();
            delegate.parseSelectCellPropertyListClause();
            return (SelectQueryAsteriskClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }

    }

    @Override
    public SelectStatement parseSelectStatement() throws MdxParserException {
        try {
            delegate.SelectStatement();
            return (SelectStatement) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    @Override
    public SelectQueryAxesClause parseSelectQueryAxesClause() throws MdxParserException {
        try {
            return delegate.parseSelectQueryAxesClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    @Override
    public Expression parseExpression() throws MdxParserException {
        try {
            return delegate.parseExpression();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    @Override
    public SelectSubcubeClause parseSelectSubcubeClause() throws MdxParserException {
        try {
            delegate.parseSelectSubcubeClause();
            return (SelectSubcubeClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectWithClause parseSelectWithClause() throws MdxParserException {
        try {
            delegate.parseSelectWithClause();
            return (SelectWithClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectQueryAxisClause parseSelectQueryAxisClause() throws MdxParserException {
        try {
            delegate.SelectQueryAxisClause();
            return (SelectQueryAxisClause) peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public Optional<SelectSlicerAxisClause> parseSelectSlicerAxisClause() throws MdxParserException {
        try {
            delegate.parseSelectSlicerAxisClause();
            return Optional.of((SelectSlicerAxisClause) delegate.peekNode());
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectCellPropertyListClause parseSelectCellPropertyListClause() throws MdxParserException {
        try {
            return delegate.parseSelectCellPropertyListClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public DrillthroughStatement parseDrillthroughStatement() throws MdxParserException {
        try {
            delegate.DrillthroughStatement();
            return (DrillthroughStatement) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public ExplainStatement parseExplainStatement() throws MdxParserException {
        try {
            delegate.ExplainStatement();
            return (ExplainStatement) peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public List<? extends ReturnItem> parseReturnItems() throws MdxParserException {
        try {
            return delegate.parseReturnItems();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public MemberPropertyDefinition parseMemberPropertyDefinition() throws MdxParserException {
        try {
            return delegate.parseMemberPropertyDefinition();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectDimensionPropertyListClause parseSelectDimensionPropertyListClause() throws MdxParserException {
        try {
            delegate.parseSelectDimensionPropertyListClause();
            return (SelectDimensionPropertyListClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public RefreshStatement parseRefreshStatement() throws MdxParserException {
        try {
            return delegate.parseRefreshStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public DMVStatement parseDMVStatement() throws MdxParserException {
        try {
            return delegate.parseDMVStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }
}

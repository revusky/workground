/* Generated by: CongoCC Parser Generator. InvalidToken.java
*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors: see corresponding .ccc file
*/
package org.eclipse.daanse.mdx.parser.ccc;


/**
* Token subclass to represent lexically invalid input
*/
public class InvalidToken extends Token {

    public InvalidToken(MdxLexer tokenSource, int beginOffset, int endOffset) {
        super(TokenType.INVALID, tokenSource, beginOffset, endOffset);
    }

    @Override
	public String getNormalizedText() {
        return "Lexically Invalid Input:" + getImage();
    }

}


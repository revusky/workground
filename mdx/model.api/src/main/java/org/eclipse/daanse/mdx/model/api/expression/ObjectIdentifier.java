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
package org.eclipse.daanse.mdx.model.api.expression;

public /*sealed*/ interface ObjectIdentifier extends Expression /*permits KeyObjectIdentifier, NameObjectIdentifier*/ {

    public enum Quoting {

        UNQUOTED, QUOTED, KEY
    }

    public Quoting quoting();
}

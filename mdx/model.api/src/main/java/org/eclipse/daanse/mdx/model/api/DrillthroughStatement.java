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
package org.eclipse.daanse.mdx.model.api;

import java.util.List;
import java.util.Optional;

/**
 * DRILLTHROUGH Statement
 * Retrieves the underlying table rows that were used to create a specified cell in a cube.
 * Syntax
 * DRILLTHROUGH [MAXROWS Unsigned_Integer]
 * <MDX SELECT statement>
 * [RETURN Set_of_Attributes_and_Measures
 * ]
 * [,Set_of_Attributes_and_Measures ...]
 */
public /*non-sealed*/ interface DrillthroughStatement extends MdxStatement {

    Optional<Integer> maxRows();

    Optional<Integer> firstRowSet();

    /**
     * @return Any valid Multidimensional Expressions (MDX) expressions SELECT statement.
     */
    SelectStatement selectStatement();

    /**
     * @return A comma-separated list of dimension attributes and measures.
     */
    List<? extends ReturnItem> returnItems();
}

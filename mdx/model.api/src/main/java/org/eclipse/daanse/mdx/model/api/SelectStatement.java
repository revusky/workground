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

import org.eclipse.daanse.mdx.model.api.select.SelectCellPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSlicerAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectWithClause;

public non-sealed interface SelectStatement extends MdxStatement {

    List<? extends SelectWithClause> selectWithClauses();

    SelectQueryClause selectQueryClause();

    SelectSubcubeClause selectSubcubeClause();

    Optional<SelectSlicerAxisClause> selectSlicerAxisClause();

    Optional<SelectCellPropertyListClause> selectCellPropertyListClause();

}

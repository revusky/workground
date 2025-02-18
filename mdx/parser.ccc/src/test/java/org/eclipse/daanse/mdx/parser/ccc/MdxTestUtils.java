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
package org.eclipse.daanse.mdx.parser.ccc;

import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.Axis;

import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClauseName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MdxTestUtils {

    public static void checkNameObjectIdentifiers(
        List<? extends ObjectIdentifier> objectIdentifiers,
        int i,
        String name,
        ObjectIdentifier.Quoting quoted
    ) {
        assertThat(objectIdentifiers.get(i).quoting()).isEqualTo(quoted);
        assertThat(objectIdentifiers.get(i)).isInstanceOf(NameObjectIdentifier.class);
        NameObjectIdentifier noi1 = (NameObjectIdentifier) objectIdentifiers.get(i);
        assertThat(noi1.name()).isEqualTo(name);
    }

    public static void checkSelectSubcubeClauseName(
        SelectSubcubeClause selectSubcubeClause,
        String name,
        ObjectIdentifier.Quoting quoted
    ) {
        assertThat(selectSubcubeClause).isNotNull().isInstanceOf(SelectSubcubeClauseName.class);
        SelectSubcubeClauseName selectSubcubeClauseName = (SelectSubcubeClauseName) selectSubcubeClause;
        assertThat(selectSubcubeClauseName.cubeName()).isInstanceOf(NameObjectIdentifier.class);
        NameObjectIdentifier nameObjectIdentifier = selectSubcubeClauseName.cubeName();
        assertThat(nameObjectIdentifier.name()).isEqualTo(name);
        assertThat(nameObjectIdentifier.quoting()).isEqualTo(quoted);
    }

    public static void checkAxis(Axis axis, int ordinal, boolean named) {
        assertThat(axis).isNotNull();
        assertThat(axis.ordinal()).isEqualTo(ordinal);
        assertThat(axis.named()).isEqualTo(named);
    }
}

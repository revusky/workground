/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.olap.rolap.dbmapper.model.api;

import java.util.List;

public interface Cube {

    List<Annotation> annotations();

    List<CubeDimension> dimensionUsageOrDimensions();

    List<Measure> measures();

    List<CalculatedMember> calculatedMembers();

    List<NamedSet> namedSets();

    List<DrillThroughAction> drillThroughActions();

    List<WritebackTable> writebackTables();


    String name();

    String caption();

    String description();

    String defaultMeasure();

    boolean cache();

    boolean enabled();

    boolean visible();

    Relation fact();

    Iterable<Action> actions();
}

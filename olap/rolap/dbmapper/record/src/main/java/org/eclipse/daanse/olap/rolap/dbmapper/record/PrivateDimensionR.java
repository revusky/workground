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
package org.eclipse.daanse.olap.rolap.dbmapper.record;

import java.util.List;

import org.eclipse.daanse.olap.rolap.dbmapper.api.Annotation;
import org.eclipse.daanse.olap.rolap.dbmapper.api.PrivateDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.api.Schema;

public record PrivateDimensionR(String name,
                                String type,
                                String caption,
                                String description,
                                String foreignKey,
                                boolean highCardinality,
                                List<AnnotationR> annotations,
                                List<HierarchyR> hierarchy,
                                boolean visible,
                                List<? extends Annotation> annotation,
                                String usagePrefix
                                )
        implements PrivateDimension {

    @Override
    public PrivateDimension getDimension(Schema xmlSchema) {
        return null;
    }

}
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
package org.eclipse.daanse.xmla.model.record.xmla;

import org.eclipse.daanse.xmla.api.xmla.Annotation;
import org.eclipse.daanse.xmla.api.xmla.AttributeTranslation;

import java.util.List;

public record AttributeTranslationR(long language,
                                    String caption,
                                    String description,
                                    String displayFolder,
                                    AttributeTranslationR.Annotations annotations,
                                    DataItemR captionColumn,
                                    String membersWithDataCaption) implements AttributeTranslation {

    public record Annotations(List<Annotation> annotation) implements AttributeTranslation.Annotations {

    }

}
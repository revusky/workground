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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegularMeasureGroupDimension", propOrder = {"cardinality",
    "attributes"})
public class RegularMeasureGroupDimension extends MeasureGroupDimension {

    @XmlElement(name = "Cardinality")
    protected String cardinality;
    @XmlElement(name = "Attribute", required = true, type = MeasureGroupAttribute.class)
    @XmlElementWrapper(name = "Attributes", required = true)
    protected List<MeasureGroupAttribute> attributes;

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String value) {
        this.cardinality = value;
    }

    public List<MeasureGroupAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<MeasureGroupAttribute> value) {
        this.attributes = value;
    }

}

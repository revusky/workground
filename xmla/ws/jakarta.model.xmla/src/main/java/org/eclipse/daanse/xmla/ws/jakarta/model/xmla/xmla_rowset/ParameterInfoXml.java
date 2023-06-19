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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PARAMETERINFO")
public class ParameterInfoXml implements Serializable {
    @XmlTransient
    private static final long serialVersionUID = -2141241183459860742L;

    /**
     * The name of the parameter.
     */
    @XmlElement(name = "NAME")
    private String name;

    /**
     * The description of the parameter.
     */
    @XmlElement(name = "DESCRIPTION")
    private String description;

    /**
     * A Boolean that, when true, indicates that the parameter is
     * optional.
     */
    @XmlElement(name = "OPTIONAL")
    private Boolean optional;

    /**
     * A Boolean that, when true, indicates that multiple values can be
     * specified for this parameter.
     */
    @XmlElement(name = "REPEATABLE")
    private Boolean repeatable;

    /**
     * The index of the repeat group of this parameter.
     */
    @XmlElement(name = "REPEATGROUP")
    private Integer repeatGroup;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public Boolean getRepeatable() {
        return repeatable;
    }

    public void setRepeatable(Boolean repeatable) {
        this.repeatable = repeatable;
    }

    public Integer getRepeatGroup() {
        return repeatGroup;
    }

    public void setRepeatGroup(Integer repeatGroup) {
        this.repeatGroup = repeatGroup;
    }
}

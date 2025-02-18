
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
package org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Annotation;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Hierarchy;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.SharedDimension;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SharedDimension", propOrder = { "annotations", "hierarchies" })
public class SharedDimensionImpl implements SharedDimension {

    @XmlElement(name = "Annotation", type = AnnotationImpl.class)
    @XmlElementWrapper(name = "Annotations")
    protected List<Annotation> annotations;
    @XmlElement(name = "Hierarchy", required = true, type = HierarchyImpl.class)
    protected List<Hierarchy> hierarchies;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "caption")
    protected String caption;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "foreignKey")
    private String foreignKey;

    @Override
    public List<Annotation> annotations() {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        return this.annotations;
    }

    @Override
    public List<Hierarchy> hierarchies() {
        if (hierarchies == null) {
            hierarchies = new ArrayList<>();
        }
        return this.hierarchies;
    }

    @Override
    public String name() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @Override
    public String type() {
        if (type == null) {
            return "StandardDimension";
        } else {
            return type;
        }
    }

    public void setType(String value) {
        this.type = value;
    }

    @Override
    public String caption() {
        return caption;
    }

    public void setCaption(String value) {
        this.caption = value;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String foreignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public void setDescription(String value) {
        this.description = value;
    }
}

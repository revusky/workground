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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema;

import jakarta.xml.bind.annotation.*;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.Row;

import java.io.Serializable;

/**
 * This schema rowset describes the measure groups within a database.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MdSchemaMeasureGroupsResponseRowXml")
public class MdSchemaMeasureGroupsResponseRowXml extends Row implements Serializable {

    @XmlTransient
    private final static long serialVersionUID = 6352460080685075397L;

    /**
     * The name of the database.
     */
    @XmlElement(name = "CATALOG_NAME", required = false)
    private String catalogName;


    /**
     * The name of the schema.
     */
    @XmlElement(name = "SCHEMA_NAME", required = false)
    private String schemaName;

    /**
     * The name of the cube.
     */
    @XmlElement(name = "CUBE_NAME", required = false)
    private String cubeName;

    /**
     * The name of the measure group.
     */
    @XmlElement(name = "MEASUREGROUP_NAME", required = false)
    private String measureGroupName;

    /**
     * A description of the member.
     */
    @XmlElement(name = "DESCRIPTION", required = false)
    private String description;

    /**
     * When true, indicates that the measure group is write-
     * enabled; otherwise false.
     * Returns a value of true if the measure group is write-
     * enabled.
     */
    @XmlElement(name = "IS_WRITE_ENABLED", required = false)
    private Boolean isWriteEnabled;

    /**
     * The caption for the measure group.
     */
    @XmlElement(name = "MEASUREGROUP_CAPTION", required = false)
    private String measureGroupCaption;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getCubeName() {
        return cubeName;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public String getMeasureGroupName() {
        return measureGroupName;
    }

    public void setMeasureGroupName(String measureGroupName) {
        this.measureGroupName = measureGroupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getWriteEnabled() {
        return isWriteEnabled;
    }

    public void setWriteEnabled(Boolean writeEnabled) {
        isWriteEnabled = writeEnabled;
    }

    public String getMeasureGroupCaption() {
        return measureGroupCaption;
    }

    public void setMeasureGroupCaption(String measureGroupCaption) {
        this.measureGroupCaption = measureGroupCaption;
    }
}
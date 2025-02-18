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
import java.util.Objects;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.AggExclude;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.AggTable;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Hint;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Table;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Table", propOrder = { "sql", "aggExcludes", "aggTables", "hints" })
@XmlRootElement(name = "Table")
public class TableImpl implements Table {

    @XmlElement(name = "SQL")
    protected SQLImpl sql;
    @XmlElement(name = "AggExclude", type = AggExcludeImpl.class)
    protected List<AggExclude> aggExcludes;
    @XmlElements({ @XmlElement(name = "AggName", type = AggNameImpl.class),
            @XmlElement(name = "AggPattern", type = AggPatternImpl.class) })
    protected List<AggTable> aggTables;
    @XmlElement(name = "Hint", type = HintImpl.class)
    protected List<Hint> hints;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "schema")
    protected String schema;
    @XmlAttribute(name = "alias")
    protected String alias;

    @Override
    public SQLImpl sql() {
        return sql;
    }

    public void setSql(SQLImpl value) {
        this.sql = value;
    }

    @Override
    public List<AggExclude> aggExcludes() {
        if (aggExcludes == null) {
            aggExcludes = new ArrayList<>();
        }
        return this.aggExcludes;
    }

    @Override
    public List<AggTable> aggTables() {
        if (aggTables == null) {
            aggTables = new ArrayList<>();
        }
        return this.aggTables;
    }

    @Override
    public List<Hint> hints() {
        if (hints == null) {
            hints = new ArrayList<>();
        }
        return this.hints;
    }

    @Override
    public String name() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @Override
    public String schema() {
        return schema;
    }

    public void setSchema(String value) {
        this.schema = value;
    }

    @Override
    public String alias() {
        return alias;
    }

    public void setAlias(String value) {
        this.alias = value;
    }

    @Override
	public boolean equals(Object o) {
        if (o instanceof Table that) {
            return this.name.equals(that.name()) &&
                Objects.equals(this.alias, that.alias()) &&
                Objects.equals(this.schema, that.schema());
        } else {
            return false;
        }
    }

    @Override
	public String toString() {
        return (schema == null) ?
            name :
            new StringBuilder(schema).append(".").append(name).toString();
    }
    @Override
	public int hashCode() {
        return toString().hashCode();
    }
}

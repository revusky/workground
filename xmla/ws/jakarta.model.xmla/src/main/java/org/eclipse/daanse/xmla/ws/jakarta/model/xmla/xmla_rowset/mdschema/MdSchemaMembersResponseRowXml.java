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

import java.io.Serializable;

import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.MemberTypeEnum;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ScopeEnum;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * This schema rowset describes the members within a database.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MdSchemaMembersResponseRowXml")
public class MdSchemaMembersResponseRowXml extends AbstractMdSchemaResponseRowXml implements Serializable {

    @XmlTransient
    private static final long serialVersionUID = 4643755964294078116L;

    /**
     * The unique name of the dimension.
     */
    @XmlElement(name = "DIMENSION_UNIQUE_NAME", required = false)
    private String dimensionUniqueName;

    /**
     * The unique name of the hierarchy.
     */
    @XmlElement(name = "HIERARCHY_UNIQUE_NAME", required = false)
    private String hierarchyUniqueName;

    /**
     * The unique name of the level.
     */
    @XmlElement(name = "LEVEL_UNIQUE_NAME", required = false)
    private String levelUniqueName;

    /**
     * The distance of the member from the root of the
     * hierarchy. The root level is zero (0).
     */
    @XmlElement(name = "LEVEL_NUMBER", required = false)
    private Integer levelNumber;

    /**
     * The ordinal of the member in its level.
     */
    @XmlElement(name = "MEMBER_ORDINAL", required = false)
    private Integer memberOrdinal;

    /**
     * The name of the member.
     */
    @XmlElement(name = "MEMBER_NAME", required = false)
    private String memberName;

    /**
     * The unique name of the member.
     */
    @XmlElement(name = "MEMBER_UNIQUE_NAME", required = false)
    private String memberUniqueName;

    /**
     * The type of the member.<228
     * 1 - Is a regular member.
     * 2 - Is the All member.
     * 3 - Is a measure.
     * 4 - Is a formula.
     * 0 - Is of unknown type.
     */
    @XmlElement(name = "MEMBER_TYPE", required = false)
    private MemberTypeEnum memberType;

    /**
     * The GUID of the member.
     */
    @XmlElement(name = "MEMBER_GUID", required = false)
    private Integer memberGuid;

    /**
     * The caption of the member.
     */
    @XmlElement(name = "MEMBER_CAPTION", required = false)
    private String measureCaption;

    /**
     * The number of children that the member has.
     * This can be an estimate.
     */
    @XmlElement(name = "CHILDREN_CARDINALITY", required = false)
    private Integer childrenCardinality;

    /**
     * The distance of the member's parent from the
     * root level of the hierarchy. The root level is zero (0).
     */
    @XmlElement(name = "PARENT_LEVEL", required = false)
    private Integer parentLevel;

    /**
     * The unique name of the member's parent. NULL
     * is returned for any members at the root level.
     */
    @XmlElement(name = "PARENT_UNIQUE_NAME", required = false)
    private String parentUniqueName;

    /**
     * The number of parents that this member has.
     */
    @XmlElement(name = "PARENT_COUNT", required = false)
    private Integer parentCount;

    /**
     * The description of the member.
     */
    @XmlElement(name = "DESCRIPTION", required = false)
    private String description;

    /**
     * The expression for calculations, if the member is
     *     of type 4 (Formula).
     */
    @XmlElement(name = "EXPRESSION", required = false)
    private String expression;

    /**
     * The value of the member's key column. Returns
     * NULL if the member has a composite key.
     */
    @XmlElement(name = "MEMBER_KEY", required = false)
    private String memberKey;

    /**
     * When true, indicates that a member is a
     * placeholder member for an empty position in a
     * dimension hierarchy; otherwise false.
     * Yes
     * The caption of the member.
     * It is valid only if the MDX Compatibility property
     * has been set to 1.
     */
    @XmlElement(name = "IS_PLACEHOLDERMEMBER", required = false)
    private Boolean isPlaceHolderMember;

    /**
     * When true, indicates that the member is a data
     * member; otherwise false.
     */
    @XmlElement(name = "IS_DATAMEMBER", required = false)
    private Boolean isDataMember;

    /**
     * The scope of the member. The member can be a
     * session-calculated member or a global-calculated
     * member. The column returns NULL for non-
     * calculated members.
     * This column can have one of the following
     * values:
     * 1 – Global
     * 2 – Session
     */
    @XmlElement(name = "SCOPE", required = false)
    private ScopeEnum scope;

    public String getDimensionUniqueName() {
        return dimensionUniqueName;
    }

    public void setDimensionUniqueName(String dimensionUniqueName) {
        this.dimensionUniqueName = dimensionUniqueName;
    }

    public String getHierarchyUniqueName() {
        return hierarchyUniqueName;
    }

    public void setHierarchyUniqueName(String hierarchyUniqueName) {
        this.hierarchyUniqueName = hierarchyUniqueName;
    }

    public String getLevelUniqueName() {
        return levelUniqueName;
    }

    public void setLevelUniqueName(String levelUniqueName) {
        this.levelUniqueName = levelUniqueName;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
    }

    public Integer getMemberOrdinal() {
        return memberOrdinal;
    }

    public void setMemberOrdinal(Integer memberOrdinal) {
        this.memberOrdinal = memberOrdinal;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberUniqueName() {
        return memberUniqueName;
    }

    public void setMemberUniqueName(String memberUniqueName) {
        this.memberUniqueName = memberUniqueName;
    }

    public MemberTypeEnum getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberTypeEnum memberType) {
        this.memberType = memberType;
    }

    public Integer getMemberGuid() {
        return memberGuid;
    }

    public void setMemberGuid(Integer memberGuid) {
        this.memberGuid = memberGuid;
    }

    public String getMeasureCaption() {
        return measureCaption;
    }

    public void setMeasureCaption(String measureCaption) {
        this.measureCaption = measureCaption;
    }

    public Integer getChildrenCardinality() {
        return childrenCardinality;
    }

    public void setChildrenCardinality(Integer childrenCardinality) {
        this.childrenCardinality = childrenCardinality;
    }

    public Integer getParentLevel() {
        return parentLevel;
    }

    public void setParentLevel(Integer parentLevel) {
        this.parentLevel = parentLevel;
    }

    public String getParentUniqueName() {
        return parentUniqueName;
    }

    public void setParentUniqueName(String parentUniqueName) {
        this.parentUniqueName = parentUniqueName;
    }

    public Integer getParentCount() {
        return parentCount;
    }

    public void setParentCount(Integer parentCount) {
        this.parentCount = parentCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getMemberKey() {
        return memberKey;
    }

    public void setMemberKey(String memberKey) {
        this.memberKey = memberKey;
    }

    public Boolean getPlaceHolderMember() {
        return isPlaceHolderMember;
    }

    public void setPlaceHolderMember(Boolean placeHolderMember) {
        isPlaceHolderMember = placeHolderMember;
    }

    public Boolean getDataMember() {
        return isDataMember;
    }

    public void setDataMember(Boolean dataMember) {
        isDataMember = dataMember;
    }

    public ScopeEnum getScope() {
        return scope;
    }

    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }
}

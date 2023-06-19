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
package org.eclipse.daanse.xmla.api.discover.mdschema.members;

import java.util.Optional;

import org.eclipse.daanse.xmla.api.common.enums.MemberTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.ScopeEnum;

/**
 * This schema rowset describes the members within a database.
 */
public interface MdSchemaMembersResponseRow {


    /**
     * @return The name of the database.
     */
    Optional<String> catalogName();


    /**
     * @return The name of the schema.
     */
    Optional<String> schemaName();

    /**
     * @return The name of the cube.
     */
    Optional<String> cubeName();

    /**
     * The unique name of the dimension.
     */
    Optional<String> dimensionUniqueName();

    /**
     * The unique name of the hierarchy.
     */
    Optional<String> hierarchyUniqueName();

    /**
     * The unique name of the level.
     */
    Optional<String> levelUniqueName();

    /**
     * The distance of the member from the root of the
     * hierarchy. The root level is zero (0).
     */
    Optional<Integer> levelNumber();

    /**
     * @return The ordinal of the member in its level.
     */
    Optional<Integer> memberOrdinal();

    /**
     * @return The name of the member.
     */
    Optional<String> memberName();

    /**
     * @return The unique name of the member.
     */
    Optional<String> memberUniqueName();

    /**
     * @return The type of the member.<228>
     * 1 - Is a regular member.
     * 2 - Is the All member.
     * 3 - Is a measure.
     * 4 - Is a formula.
     * 0 - Is of unknown type.
     */
    Optional<MemberTypeEnum> memberType();

    /**
     * @return The GUID of the member.
     */
    Optional<Integer> memberGuid();

    /**
     * @return The caption of the member.
     */
    Optional<String> measureCaption();

    /**
     * @return The number of children that the member has.
     * This can be an estimate.
     */
    Optional<Integer> childrenCardinality();

    /**
     * @return The distance of the member's parent from the
     * root level of the hierarchy. The root level is zero (0).
     */
    Optional<Integer> parentLevel();

    /**
     * @return The unique name of the member's parent. NULL
     * is returned for any members at the root level.
     */
    Optional<String> parentUniqueName();

    /**
     * @return The number of parents that this member has.
     */
    Optional<Integer> parentCount();

    /**
     * @return The description of the member.
     */
    Optional<String> description();

    /**
     * @return The expression for calculations, if the member is
     *     of type 4 (Formula).
     */
    Optional<String> expression();

    /**
     * @return The value of the member's key column. Returns
     * NULL if the member has a composite key.
     */
    Optional<String> memberKey();

    /**
     * @return When true, indicates that a member is a
     * placeholder member for an empty position in a
     * dimension hierarchy; otherwise false.
     * Yes
     * The caption of the member.
     * It is valid only if the MDX Compatibility property
     * has been set to 1.
     */
    Optional<Boolean> isPlaceHolderMember();

    /**
     * @return When true, indicates that the member is a data
     * member; otherwise false.
     */
    Optional<Boolean> isDataMember();

    /**
     * @return The scope of the member. The member can be a
     * session-calculated member or a global-calculated
     * member. The column returns NULL for non-
     * calculated members.
     * This column can have one of the following
     * values:
     * 1 – Global
     * 2 – Session
     */
    Optional<ScopeEnum> scope();
}

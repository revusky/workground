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

import org.eclipse.daanse.xmla.api.common.enums.CubeSourceEnum;
import org.eclipse.daanse.xmla.api.common.enums.MemberTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TreeOpEnum;

public interface MdSchemaMembersRestrictions {

    String RESTRICTIONS_CATALOG_NAME = "CATALOG_NAME";
    String RESTRICTIONS_SCHEMA_NAME = "SCHEMA_NAME";
    String RESTRICTIONS_CUBE_NAME = "CUBE_NAME";
    String RESTRICTIONS_DIMENSION_UNIQUE_NAME = "DIMENSION_UNIQUE_NAME";
    String RESTRICTIONS_HIERARCHY_UNIQUE_NAME = "HIERARCHY_UNIQUE_NAME";
    String RESTRICTIONS_LEVEL_UNIQUE_NAME = "LEVEL_UNIQUE_NAME";
    String RESTRICTIONS_LEVEL_NUMBER = "LEVEL_NUMBER";
    String RESTRICTIONS_MEMBER_NAME = "MEMBER_NAME";
    String RESTRICTIONS_MEMBER_UNIQUE_NAME = "MEMBER_UNIQUE_NAME";
    String RESTRICTIONS_MEMBER_TYPE = "MEMBER_TYPE";
    String RESTRICTIONS_MEMBER_CAPTION = "MEMBER_CAPTION";
    String RESTRICTIONS_CUBE_SOURCE = "CUBE_SOURCE";
    String RESTRICTIONS_TREE_OP = "TREE_OP";

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
     * @return The caption of the member.
     */
    Optional<String> memberCaption();

    /**
     * @return bitmask with one of these valid values:
     * 0x01 - Cube
     * 0x02 - Dimension
     * The default restriction is a value of 1.
     */
    Optional<CubeSourceEnum> cubeSource();

    /**
     * Applies only to a single member:
     * 0x20 - Returns all of the ancestors.
     * 0x01 - Returns only the immediate children.
     * 0x02 - Returns members on the same level.
     * 0x04 - Returns only the immediate parent.
     * 0x08 – Returns only itself.
     * 0x10 - Returns all of the descendants.
     */
    Optional<TreeOpEnum> treeOp();
}

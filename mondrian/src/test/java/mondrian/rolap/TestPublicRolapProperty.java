/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * History:
 *  This files came from the mondrian project. Some of the Flies
 *  (mostly the Tests) did not have License Header.
 *  But the Project is EPL Header. 2002-2022 Hitachi Vantara.
 *
 * Contributors:
 *   Hitachi Vantara.
 *   SmartCity Jena - initial  Java 8, Junit5
 */
package mondrian.rolap;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Expression;

import mondrian.spi.PropertyFormatter;

public class TestPublicRolapProperty extends RolapProperty {

	TestPublicRolapProperty(String name, Datatype type, Expression exp, PropertyFormatter formatter, String caption,
                            Boolean dependsOnLevelValue, boolean internal, String description) {
		super(name, type, exp, formatter, caption, dependsOnLevelValue, internal, description);
	}

}

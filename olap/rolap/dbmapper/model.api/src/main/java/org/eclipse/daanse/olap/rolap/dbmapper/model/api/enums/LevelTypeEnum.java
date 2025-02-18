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
package org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums;

public enum LevelTypeEnum {

    REGULAR("Regular"), TIME_YEARS("TimeYears"), TIME_HALF_YEARS("TimeHalfYears"),
    TIME_HALF_YEAR("TimeHalfYear"), TIME_QUARTERS("TimeQuarters"), TIME_MONTHS("TimeMonths"),
    TIME_WEEKS("TimeWeeks"), TIME_DAYS("TimeDays"), TIME_HOURS("TimeHours"),
    TIME_MINUTES("TimeMinutes"), TIME_SECONDS("TimeSeconds"), TIME_UNDEFINED("TimeUndefined");

    private final String value;

    LevelTypeEnum(String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

    public static LevelTypeEnum fromValue(String v) {
        for (LevelTypeEnum c : LevelTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}

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
package org.eclipse.daanse.olap.rolap.dbmapper.model.jaxb.adapter;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.ParameterTypeEnum;

public class ParameterTypeAdaptor  extends Adaptor<ParameterTypeEnum> {

    protected ParameterTypeAdaptor() {
        super(ParameterTypeEnum.class);
    }

    @Override
    public Enum<ParameterTypeEnum> unmarshal(String v) {
        return ParameterTypeEnum.fromValue(v);
    }

    @Override
    public String marshal(Enum<ParameterTypeEnum> e) throws Exception {
        if (e != null) {
            return ParameterTypeEnum.valueOf(e.name()).getValue();
        }
        return null;
    }

}

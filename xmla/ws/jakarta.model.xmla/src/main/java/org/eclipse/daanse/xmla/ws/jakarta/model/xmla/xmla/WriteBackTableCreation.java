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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


@XmlType(name = "WriteBackTableCreation")
@XmlEnum
public enum WriteBackTableCreation {

  @XmlEnumValue("Create")
  CREATE("Create"), @XmlEnumValue("CreateAlways")
  CREATE_ALWAYS("CreateAlways"), @XmlEnumValue("UseExisting")
  USE_EXISTING("UseExisting");

  private final String value;

  WriteBackTableCreation(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static WriteBackTableCreation fromValue(String v) {
    for (WriteBackTableCreation c : WriteBackTableCreation.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}

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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Event", propOrder = {

})
public class Event {

  @XmlElement(name = "EventID", required = true)
  protected String eventID;
  @XmlElement(name = "Columns", required = true)
  protected EventColumnID columns;

  public String getEventID() {
    return eventID;
  }

  public void setEventID(String value) {
    this.eventID = value;
  }

  public EventColumnID getColumns() {
    return columns;
  }

  public void setColumns(EventColumnID value) {
    this.columns = value;
  }

}

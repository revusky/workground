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
*//*
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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * com.microsoft.schemas.analysisservices._2003.engine package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

  private static final QName _LastSchemaUpdate_QNAME = new QName(
      "http://schemas.microsoft.com/analysisservices/2003/engine", "LastSchemaUpdate");
  private static final QName _LastDataUpdate_QNAME = new QName(
      "http://schemas.microsoft.com/analysisservices/2003/engine", "LastDataUpdate");

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema
   * derived classes for package:
   * com.microsoft.schemas.analysisservices._2003.engine
   *
   */
  public ObjectFactory() {
      //constructor
  }

  /**
   * Create an instance of {@link ImpersonationInfo }
   *
   */
  public ImpersonationInfo createImpersonationInfo() {
    return new ImpersonationInfo();
  }

  /**
   * Create an instance of {@link JAXBElement
   * }{@code <}{@link XMLGregorianCalendar }{@code >}
   *
   * @param value Java instance representing xml element's value.
   * @return the new instance of {@link JAXBElement
   *         }{@code <}{@link XMLGregorianCalendar }{@code >}
   */
  @XmlElementDecl(namespace = "http://schemas.microsoft.com/analysisservices/2003/engine", name = "LastSchemaUpdate")
  public JAXBElement<XMLGregorianCalendar> createLastSchemaUpdate(XMLGregorianCalendar value) {
    return new JAXBElement<>(_LastSchemaUpdate_QNAME, XMLGregorianCalendar.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement
   * }{@code <}{@link XMLGregorianCalendar }{@code >}
   *
   * @param value Java instance representing xml element's value.
   * @return the new instance of {@link JAXBElement
   *         }{@code <}{@link XMLGregorianCalendar }{@code >}
   */
  @XmlElementDecl(namespace = "http://schemas.microsoft.com/analysisservices/2003/engine", name = "LastDataUpdate")
  public JAXBElement<XMLGregorianCalendar> createLastDataUpdate(XMLGregorianCalendar value) {
    return new JAXBElement<>(_LastDataUpdate_QNAME, XMLGregorianCalendar.class, null, value);
  }

}

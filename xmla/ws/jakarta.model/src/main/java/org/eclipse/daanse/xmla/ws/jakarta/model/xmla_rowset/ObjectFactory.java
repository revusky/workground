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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla_rowset;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.eclipse.daanse.xmla.ws.xmla_rs
 * package.
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

  private final static QName _Root_QNAME = new QName("urn:schemas-microsoft-com:xml-analysis:rowset", "root");

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema
   * derived classes for package: org.eclipse.daanse.xmla.ws.xmla_rs
   * 
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link Rowset }
   * 
   */
  public Rowset createRowset() {
    return new Rowset();
  }

  /**
   * Create an instance of {@link Row }
   * 
   */
  public Row createRow() {
    return new Row();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Rowset }{@code >}
   * 
   * @param value Java instance representing xml element's value.
   * @return the new instance of {@link JAXBElement }{@code <}{@link Rowset
   *         }{@code >}
   */
  @XmlElementDecl(namespace = "urn:schemas-microsoft-com:xml-analysis:rowset", name = "root")
  public JAXBElement<Rowset> createRoot(Rowset value) {
    return new JAXBElement<Rowset>(_Root_QNAME, Rowset.class, null, value);
  }

}
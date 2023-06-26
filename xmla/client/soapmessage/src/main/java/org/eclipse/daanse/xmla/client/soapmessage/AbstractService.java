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
package org.eclipse.daanse.xmla.client.soapmessage;

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import org.eclipse.daanse.xmla.api.discover.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.daanse.xmla.client.soapmessage.Constants.AXIS_FORMAT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CATALOG;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.CONTENT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.DATA_SOURCE_INFO;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.FORMAT;
import static org.eclipse.daanse.xmla.client.soapmessage.Constants.LOCALE_IDENTIFIER;

public abstract class AbstractService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);
    protected void setPropertyList(SOAPElement propertyList, Properties properties) {
        properties.localeIdentifier().ifPresent(v -> addChildElement(propertyList, LOCALE_IDENTIFIER, v.toString()));
        properties.dataSourceInfo().ifPresent(v -> addChildElement(propertyList, DATA_SOURCE_INFO, v));
        properties.content().ifPresent(v -> addChildElement(propertyList, CONTENT, v.getValue()));
        properties.format().ifPresent(v -> addChildElement(propertyList, FORMAT, v.getValue()));
        properties.catalog().ifPresent(v -> addChildElement(propertyList, CATALOG, v));
        properties.axisFormat().ifPresent(v -> addChildElement(propertyList, AXIS_FORMAT, v.getValue()));
    }

    protected void addChildElement(SOAPElement element, String childElementName, String value) {
        try {
            if (value != null) {
                element.addChildElement(childElementName).setTextContent(value);
            }
        } catch (SOAPException e) {
            LOGGER.error("addChildElement {} error", childElementName);
            throw new SoapClientException("addChildElement error", e);
        }
    }

    protected SOAPElement addChildElement(SOAPElement element, String childElementName) {
        try {
            return element.addChildElement(childElementName);
        } catch (SOAPException e) {
            LOGGER.error("addChildElement {} error", childElementName);
            throw new SoapClientException("addChildElement error", e);
        }
    }

}

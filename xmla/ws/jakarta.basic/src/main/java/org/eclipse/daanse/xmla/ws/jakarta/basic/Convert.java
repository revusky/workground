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
package org.eclipse.daanse.xmla.ws.jakarta.basic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.xmla.api.common.properties.Content;
import org.eclipse.daanse.xmla.api.common.properties.Format;
import org.eclipse.daanse.xmla.api.discover.dbschemacatalogs.DiscoverDbSchemaCatalogsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschemacatalogs.DiscoverDbSchemaCatalogsResponse;
import org.eclipse.daanse.xmla.api.discover.discoverproperties.DiscoverPropertiesRequest;
import org.eclipse.daanse.xmla.api.discover.discoverproperties.DiscoverPropertiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.discoverproperties.DiscoverPropertiesRestrictions;
import org.eclipse.daanse.xmla.api.discover.schemarowsets.DiscoverSchemaRowsetsRequest;
import org.eclipse.daanse.xmla.api.discover.schemarowsets.DiscoverSchemaRowsetsResponseRow;
import org.eclipse.daanse.xmla.api.discover.schemarowsets.DiscoverSchemaRowsetsRestrictions;
import org.eclipse.daanse.xmla.model.record.discover.PropertiesR;
import org.eclipse.daanse.xmla.model.record.discover.dbschemacatalogs.DbSchemaCatalogsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discoverproperties.DiscoverPropertiesRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discoverproperties.DiscoverPropertiesRestrictionsR;
import org.eclipse.daanse.xmla.model.record.discover.discoverschemarowsets.DiscoverSchemaRowsetsRequestR;
import org.eclipse.daanse.xmla.model.record.discover.discoverschemarowsets.DiscoverSchemaRowsetsRestrictionsR;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.Discover;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.DiscoverResponse;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.PropertyList;

public class Convert {
    private static Optional<Integer> localeIdentifier(Discover requestWs) {
        Optional<Integer> oLocaleIdentifier = Optional.ofNullable(propertyList(requestWs).getLocaleIdentifier());
        return oLocaleIdentifier;
    }

    private static Optional<Content> content(Discover requestWs) {
        String content = propertyList(requestWs).getContent();
        if (content != null) {
            return Optional.ofNullable(Content.valueOf(content));
        }
        return Optional.empty();
    }

    private static Optional<String> catalog(Discover requestWs) {
        Optional<String> catalog = Optional.ofNullable(propertyList(requestWs).getCatalog());
        return catalog;
    }

    private static Optional<String> dataSourceInfo(Discover requestWs) {
        Optional<String> dataSourceInfo = Optional.ofNullable(propertyList(requestWs).getDataSourceInfo());
        return dataSourceInfo;
    }

    private static Optional<Format> format(Discover requestWs) {
        String format = propertyList(requestWs).getFormat();
        if (format != null) {
            return Optional.ofNullable(Format.valueOf(format));
        }
        return Optional.empty();
    }

    private static PropertyList propertyList(Discover requestWs) {
        return requestWs.getProperties()
                .getPropertyList();
    }

    private static Map<String, String> restrictionsMap(Discover requestWs) {
        return requestWs.getRestrictions()
                .getRestrictionMap();
    }

    private static PropertiesR discoverProperties(Discover requestWs) {
        Optional<Integer> localeIdentifier = localeIdentifier(requestWs);
        Optional<Content> content = content(requestWs);
        Optional<Format> format = format(requestWs);
        Optional<String> dataSourceInfo = dataSourceInfo(requestWs);
        Optional<String> catalog = catalog(requestWs);

        return new PropertiesR(localeIdentifier, dataSourceInfo, content, format,catalog);
    }

    private static DiscoverPropertiesRestrictionsR discoverPropertiesRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String propertyName = map.get(DiscoverPropertiesRestrictions.RESTRICTIONS_PROPERTY_NAME);

        return new DiscoverPropertiesRestrictionsR(Optional.ofNullable(propertyName));
    }

    public static DiscoverPropertiesRequest fromDiscoverProperties(Discover requestWs) {

        System.out.println(requestWs);
        PropertiesR properties = discoverProperties(requestWs);
        DiscoverPropertiesRestrictionsR restrictions = discoverPropertiesRestrictions(requestWs);

        return new DiscoverPropertiesRequestR(properties, restrictions);

    }

    public static DiscoverResponse toDiscoverProperties(List<DiscoverPropertiesResponseRow> responseApi) {

        DiscoverResponse responseWs = new DiscoverResponse();
        return responseWs;
    }

    public static DiscoverDbSchemaCatalogsRequest fromDiscoverDbSchemaCatalogs(Discover requestWs) {
        return new DbSchemaCatalogsRequestR();
    }

    public static DiscoverResponse toDiscoverDbSchemaCatalogs(DiscoverDbSchemaCatalogsResponse responseApi) {
        DiscoverResponse responseWs = new DiscoverResponse();
        return responseWs;
    }



    public static DiscoverResponse toDiscoverSchemaRowsets(List<DiscoverSchemaRowsetsResponseRow> responseApi) {

        DiscoverResponse responseWs = new DiscoverResponse();
        return responseWs;
    }

    private static DiscoverSchemaRowsetsRestrictionsR discoverSchemaRowsetsRestrictions(Discover requestWs) {
        Map<String, String> map = restrictionsMap(requestWs);

        String schemaName = map.get(DiscoverSchemaRowsetsRestrictions.RESTRICTIONS_SCHEMA_NAME);

        return new DiscoverSchemaRowsetsRestrictionsR(Optional.ofNullable(schemaName));
    }

    public static DiscoverSchemaRowsetsRequest fromDiscoverSchemaRowsets(Discover requestWs) {

        PropertiesR properties = discoverProperties(requestWs);
        DiscoverSchemaRowsetsRestrictionsR restrictions = discoverSchemaRowsetsRestrictions(requestWs);

        return new DiscoverSchemaRowsetsRequestR(properties, restrictions);

    }

}
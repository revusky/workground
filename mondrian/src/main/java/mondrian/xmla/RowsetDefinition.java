/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2003-2005 Julian Hyde
// Copyright (C) 2005-2018 Hitachi Vantara
// Copyright (C) 2019 Topsoft
// Copyright (C) 2020 - 2022 Sergei Semenkov
// All Rights Reserved.
*/

package mondrian.xmla;

import static mondrian.olap.Util.filter;
import static mondrian.xmla.XmlaConstants.NS_XMLA_ROWSET;
import static mondrian.xmla.XmlaConstants.NS_XSD;
import static mondrian.xmla.XmlaConstants.NS_XSI;
import static mondrian.xmla.XmlaHandler.getExtra;
import static org.eigenbase.xom.XOMUtil.discard;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.impl.ArrayNamedListImpl;
import org.olap4j.impl.Olap4jUtil;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.mdx.IdentifierSegment;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Member.TreeOp;
import org.olap4j.metadata.MetadataElement;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.NamedSet;
import org.olap4j.metadata.Property;
import org.olap4j.metadata.Schema;
import org.olap4j.metadata.XmlaConstant;
import org.olap4j.metadata.XmlaConstants;

import mondrian.olap.Category;
import mondrian.olap.MondrianProperties;
import mondrian.olap.MondrianServer;
import mondrian.olap.SetBase;
import mondrian.olap.Util;
import mondrian.rolap.RolapHierarchy;
import mondrian.util.Composite;

/**
 * <code>RowsetDefinition</code> defines a rowset, including the columns it
 * should contain.
 *
 * <p>See "XML for Analysis Rowsets", page 38 of the XML for Analysis
 * Specification, version 1.1.
 *
 * @author jhyde
 */
public enum RowsetDefinition {
    /**
     * Returns a list of XML for Analysis data sources
     * available on the server or Web Service. (For an
     * example of how these may be published, see
     * "XML for Analysis Implementation Walkthrough"
     * in the XML for Analysis specification.)
     *
     *  http://msdn2.microsoft.com/en-us/library/ms126129(SQL.90).aspx
     *
     *
     * restrictions
     *
     * Not supported
     */
    DISCOVER_DATASOURCES(
        0,
        "06C03D41-F66D-49F3-B1B8-987F7AF4CF18",
        "Returns a list of XML for Analysis data sources available on the "
        + "server or Web Service.",
        new Column[] {
            DiscoverDatasourcesRowset.DataSourceName,
            DiscoverDatasourcesRowset.DataSourceDescription,
            DiscoverDatasourcesRowset.URL,
            DiscoverDatasourcesRowset.DataSourceInfo,
            DiscoverDatasourcesRowset.ProviderName,
            DiscoverDatasourcesRowset.ProviderType,
            DiscoverDatasourcesRowset.AuthenticationMode,
        },
        // XMLA does not specify a sort order, but olap4j does.
        new Column[] {
            DiscoverDatasourcesRowset.DataSourceName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DiscoverDatasourcesRowset(request, handler);
        }
    },

    /**
     * Note that SQL Server also returns the data-mining columns.
     *
     *
     * restrictions
     *
     * Not supported
     */
    DISCOVER_SCHEMA_ROWSETS(
        2,
        "EEA0302B-7922-4992-8991-0E605D0E5593",
        "Returns the names, values, and other information of all supported "
        + "RequestType enumeration values.",
        new Column[] {
            DiscoverSchemaRowsetsRowset.SCHEMA_NAME_COLUMN,
            DiscoverSchemaRowsetsRowset.SCHEMA_GUID_COLUMN,
            DiscoverSchemaRowsetsRowset.RESTRICTIONS_COLUMN,
            DiscoverSchemaRowsetsRowset.DESCRIPTION_COLUMN,
            DiscoverSchemaRowsetsRowset.RESTRICTIONS_MASK_COLUMN,
        },
        new Column[] {
            DiscoverSchemaRowsetsRowset.SCHEMA_NAME_COLUMN,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DiscoverSchemaRowsetsRowset(request, handler);
        }
        @Override
		protected void writeRowsetXmlSchemaRowDef(SaxWriter writer) {
            writer.startElement(
                "xsd:complexType",
                "name", "row");
            writer.startElement("xsd:sequence");
            for (Column column : columnDefinitions) {
                final String name =
                    XmlaUtil.ElementNameEncoder.INSTANCE.encode(column.name);

                if (column == DiscoverSchemaRowsetsRowset.RESTRICTIONS_COLUMN) {
                    writer.startElement(
                        "xsd:element",
                        "sql:field", column.name,
                        "name", name,
                        "minOccurs", 0,
                        "maxOccurs", "unbounded");
                    writer.startElement("xsd:complexType");
                    writer.startElement("xsd:sequence");
                    writer.element(
                        "xsd:element",
                        "name", "Name",
                        "type", "xsd:string",
                        "sql:field", "Name");
                    writer.element(
                        "xsd:element",
                        "name", "Type",
                        "type", "xsd:string",
                        "sql:field", "Type");

                    writer.endElement(); // xsd:sequence
                    writer.endElement(); // xsd:complexType
                    writer.endElement(); // xsd:element

                } else {
                    final String xsdType = column.type.columnType;

                    Object[] attrs;
                    if (column.nullable) {
                        if (column.unbounded) {
                            attrs = new Object[]{
                                "sql:field", column.name,
                                "name", name,
                                "type", xsdType,
                                "minOccurs", 0,
                                "maxOccurs", "unbounded"
                            };
                        } else {
                            attrs = new Object[]{
                                "sql:field", column.name,
                                "name", name,
                                "type", xsdType,
                                "minOccurs", 0
                            };
                        }
                    } else {
                        if (column.unbounded) {
                            attrs = new Object[]{
                                "sql:field", column.name,
                                "name", name,
                                "type", xsdType,
                                "maxOccurs", "unbounded"
                            };
                        } else {
                            attrs = new Object[]{
                                "sql:field", column.name,
                                "name", name,
                                "type", xsdType
                            };
                        }
                    }
                    writer.element("xsd:element", attrs);
                }
            }
            writer.endElement(); // xsd:sequence
            writer.endElement(); // xsd:complexType
        }
    },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     */
    DISCOVER_ENUMERATORS(
        3,
        "55A9E78B-ACCB-45B4-95A6-94C5065617A7",
        "Returns a list of names, data types, and enumeration values for "
        + "enumerators supported by the provider of a specific data source.",
        new Column[] {
            DiscoverEnumeratorsRowset.EnumName,
            DiscoverEnumeratorsRowset.EnumDescription,
            DiscoverEnumeratorsRowset.EnumType,
            DiscoverEnumeratorsRowset.ElementName,
            DiscoverEnumeratorsRowset.ElementDescription,
            DiscoverEnumeratorsRowset.ElementValue,
        },
        null /* not sorted */)
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DiscoverEnumeratorsRowset(request, handler);
        }
    },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     */
    DISCOVER_PROPERTIES(
        1,
        "4B40ADFB-8B09-4758-97BB-636E8AE97BCF",
        "Returns a list of information and values about the requested "
        + "properties that are supported by the specified data source "
        + "provider.",
        new Column[] {
            DiscoverPropertiesRowset.PropertyName,
            DiscoverPropertiesRowset.PropertyDescription,
            DiscoverPropertiesRowset.PropertyType,
            DiscoverPropertiesRowset.PropertyAccessType,
            DiscoverPropertiesRowset.IsRequired,
            DiscoverPropertiesRowset.Value,
        },
        null /* not sorted */)
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DiscoverPropertiesRowset(request, handler);
        }
    },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     */
    DISCOVER_KEYWORDS(
        4,
        "1426C443-4CDD-4A40-8F45-572FAB9BBAA1",
        "Returns an XML list of keywords reserved by the provider.",
        new Column[] {
            DiscoverKeywordsRowset.Keyword,
        },
        null /* not sorted */)
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DiscoverKeywordsRowset(request, handler);
        }
    },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     */
    DISCOVER_LITERALS(
        5,
        "C3EF5ECB-0A07-4665-A140-B075722DBDC2",
        "Returns information about literals supported by the provider.",
        new Column[] {
            DiscoverLiteralsRowset.LiteralName,
            DiscoverLiteralsRowset.LiteralValue,
            DiscoverLiteralsRowset.LiteralInvalidChars,
            DiscoverLiteralsRowset.LiteralInvalidStartingChars,
            DiscoverLiteralsRowset.LiteralMaxLength,
            DiscoverLiteralsRowset.LiteralNameEnumValue,
        },
        null /* not sorted */)
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DiscoverLiteralsRowset(request, handler);
        }
    },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     */
    DISCOVER_XML_METADATA(
        23,
        "3444B255-171E-4CB9-AD98-19E57888A75F",
        "Returns an XML document describing a requested object. " +
                "The rowset that is returned always consists of one row and one column.",
        new Column[] {
                DiscoverXmlMetadataRowset.METADATA,
                // Restrictions
                DiscoverXmlMetadataRowset.DatabaseID,
        },
        null /* not sorted */)
        {
            @Override
			public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
                return new DiscoverXmlMetadataRowset(request, handler);
            }
        },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     */
    DBSCHEMA_CATALOGS(
        6,
        "C8B52211-5CF3-11CE-ADE5-00AA0044773D",
        "Identifies the physical attributes associated with catalogs "
        + "accessible from the provider.",
        new Column[] {
            DbschemaCatalogsRowset.CatalogName,
            DbschemaCatalogsRowset.Description,
            DbschemaCatalogsRowset.Roles,
            DbschemaCatalogsRowset.DateModified,
        },
        new Column[] {
            DbschemaCatalogsRowset.CatalogName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DbschemaCatalogsRowset(request, handler);
        }
    },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     *    COLUMN_OLAP_TYPE
     */
    DBSCHEMA_COLUMNS(
        7,
            "C8B52214-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            DbschemaColumnsRowset.TableCatalog,
            DbschemaColumnsRowset.TableSchema,
            DbschemaColumnsRowset.TableName,
            DbschemaColumnsRowset.ColumnName,
            DbschemaColumnsRowset.OrdinalPosition,
            DbschemaColumnsRowset.ColumnHasDefault,
            DbschemaColumnsRowset.ColumnFlags,
            DbschemaColumnsRowset.IsNullable,
            DbschemaColumnsRowset.DataType,
            DbschemaColumnsRowset.CharacterMaximumLength,
            DbschemaColumnsRowset.CharacterOctetLength,
            DbschemaColumnsRowset.NumericPrecision,
            DbschemaColumnsRowset.NumericScale,
        },
        new Column[] {
            DbschemaColumnsRowset.TableCatalog,
            DbschemaColumnsRowset.TableSchema,
            DbschemaColumnsRowset.TableName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DbschemaColumnsRowset(request, handler);
        }
    },

    /**
     *
     *
     *
     * restrictions
     *
     * Not supported
     */
    DBSCHEMA_PROVIDER_TYPES(
        8, "C8B5222C-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            DbschemaProviderTypesRowset.TypeName,
            DbschemaProviderTypesRowset.DataType,
            DbschemaProviderTypesRowset.ColumnSize,
            DbschemaProviderTypesRowset.LiteralPrefix,
            DbschemaProviderTypesRowset.LiteralSuffix,
            DbschemaProviderTypesRowset.IsNullable,
            DbschemaProviderTypesRowset.CaseSensitive,
            DbschemaProviderTypesRowset.Searchable,
            DbschemaProviderTypesRowset.UnsignedAttribute,
            DbschemaProviderTypesRowset.FixedPrecScale,
            DbschemaProviderTypesRowset.AutoUniqueValue,
            DbschemaProviderTypesRowset.IsLong,
            DbschemaProviderTypesRowset.BestMatch,
        },
        new Column[] {
            DbschemaProviderTypesRowset.DataType,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DbschemaProviderTypesRowset(request, handler);
        }
    },

    DBSCHEMA_SCHEMATA(
        8, "c8b52225-5cf3-11ce-ade5-00aa0044773d", null,
        new Column[] {
            DbschemaSchemataRowset.CatalogName,
            DbschemaSchemataRowset.SchemaName,
            DbschemaSchemataRowset.SchemaOwner,
        },
        new Column[] {
            DbschemaSchemataRowset.CatalogName,
            DbschemaSchemataRowset.SchemaName,
            DbschemaSchemataRowset.SchemaOwner,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DbschemaSchemataRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126299(SQL.90).aspx
     *
     * restrictions:
     *   TABLE_CATALOG Optional
     *   TABLE_SCHEMA Optional
     *   TABLE_NAME Optional
     *   TABLE_TYPE Optional
     *   TABLE_OLAP_TYPE Optional
     *
     * Not supported
     */
    DBSCHEMA_TABLES(
        9, "C8B52229-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            DbschemaTablesRowset.TableCatalog,
            DbschemaTablesRowset.TableSchema,
            DbschemaTablesRowset.TableName,
            DbschemaTablesRowset.TableType,
            DbschemaTablesRowset.TableGuid,
            DbschemaTablesRowset.Description,
            DbschemaTablesRowset.TablePropId,
            DbschemaTablesRowset.DateCreated,
            DbschemaTablesRowset.DateModified,
            //TableOlapType,
        },
        new Column[] {
            DbschemaTablesRowset.TableType,
            DbschemaTablesRowset.TableCatalog,
            DbschemaTablesRowset.TableSchema,
            DbschemaTablesRowset.TableName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DbschemaTablesRowset(request, handler);
        }
    },

    DBSCHEMA_SOURCE_TABLES(
            23, "8c3f5858-2742-4976-9d65-eb4d493c693e", null,
            new Column[] {
                    DbschemaSourceTablesRowset.TableCatalog,
                    DbschemaSourceTablesRowset.TableSchema,
                    DbschemaSourceTablesRowset.TableName,
                    DbschemaSourceTablesRowset.TableType,
            },
            new Column[] {
                    DbschemaSourceTablesRowset.TableCatalog,
                    DbschemaSourceTablesRowset.TableSchema,
                    DbschemaSourceTablesRowset.TableName,
                    DbschemaSourceTablesRowset.TableType,
            })
            {
                @Override
				public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
                    return new DbschemaSourceTablesRowset(request, handler);
                }
            },

    /**
     * http://msdn.microsoft.com/library/en-us/oledb/htm/
     * oledbtables_info_rowset.asp
     *
     *
     * restrictions
     *
     * Not supported
     */
    DBSCHEMA_TABLES_INFO(
        10, "c8b522e0-5cf3-11ce-ade5-00aa0044773d", null,
        new Column[] {
            DbschemaTablesInfoRowset.TableCatalog,
            DbschemaTablesInfoRowset.TableSchema,
            DbschemaTablesInfoRowset.TableName,
            DbschemaTablesInfoRowset.TableType,
            DbschemaTablesInfoRowset.TableGuid,
            DbschemaTablesInfoRowset.Bookmarks,
            DbschemaTablesInfoRowset.BookmarkType,
            DbschemaTablesInfoRowset.BookmarkDataType,
            DbschemaTablesInfoRowset.BookmarkMaximumLength,
            DbschemaTablesInfoRowset.BookmarkInformation,
            DbschemaTablesInfoRowset.TableVersion,
            DbschemaTablesInfoRowset.Cardinality,
            DbschemaTablesInfoRowset.Description,
            DbschemaTablesInfoRowset.TablePropId,
        },
        null /* cannot find doc -- presume unsorted */)
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new DbschemaTablesInfoRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126032(SQL.90).aspx
     *
     * restrictions
     *   CATALOG_NAME Optional
     *   SCHEMA_NAME Optional
     *   CUBE_NAME Mandatory
     *   ACTION_NAME Optional
     *   ACTION_TYPE Optional
     *   COORDINATE Mandatory
     *   COORDINATE_TYPE Mandatory
     *   INVOCATION
     *      (Optional) The INVOCATION restriction column defaults to the
     *      value of MDACTION_INVOCATION_INTERACTIVE. To retrieve all
     *      actions, use the MDACTION_INVOCATION_ALL value in the
     *      INVOCATION restriction column.
     *   CUBE_SOURCE
     *      (Optional) A bitmap with one of the following valid values:
     *
     *      1 CUBE
     *      2 DIMENSION
     *
     *      Default restriction is a value of 1.
     *
     * Not supported
     */
    MDSCHEMA_ACTIONS(
        11, "A07CCD08-8148-11D0-87BB-00C04FC33942", null, new Column[] {
            MdschemaActionsRowset.CatalogName,
            MdschemaActionsRowset.SchemaName,
            MdschemaActionsRowset.CubeName,
            MdschemaActionsRowset.ActionName,
            MdschemaActionsRowset.Coordinate,
            MdschemaActionsRowset.CoordinateType,
        }, new Column[] {
            // Spec says sort on CATALOG_NAME, SCHEMA_NAME, CUBE_NAME,
            // ACTION_NAME.
            MdschemaActionsRowset.CatalogName,
            MdschemaActionsRowset.SchemaName,
            MdschemaActionsRowset.CubeName,
            MdschemaActionsRowset.ActionName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaActionsRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126271(SQL.90).aspx
     *
     * restrictions
     *   CATALOG_NAME Optional.
     *   SCHEMA_NAME Optional.
     *   CUBE_NAME Optional.
     *   CUBE_TYPE
     *      (Optional) A bitmap with one of these valid values:
     *      1 CUBE
     *      2 DIMENSION
     *     Default restriction is a value of 1.
     *   BASE_CUBE_NAME Optional.
     *
     * Not supported
     *   CREATED_ON
     *   LAST_SCHEMA_UPDATE
     *   SCHEMA_UPDATED_BY
     *   LAST_DATA_UPDATE
     *   DATA_UPDATED_BY
     *   ANNOTATIONS
     */
    MDSCHEMA_CUBES(
        12, "C8B522D8-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            MdschemaCubesRowset.CatalogName,
            MdschemaCubesRowset.SchemaName,
            MdschemaCubesRowset.CubeName,
            MdschemaCubesRowset.CubeType,
            MdschemaCubesRowset.CubeGuid,
            MdschemaCubesRowset.CreatedOn,
            MdschemaCubesRowset.LastSchemaUpdate,
            MdschemaCubesRowset.SchemaUpdatedBy,
            MdschemaCubesRowset.LastDataUpdate,
            MdschemaCubesRowset.DataUpdatedBy,
            MdschemaCubesRowset.Description,
            MdschemaCubesRowset.IsDrillthroughEnabled,
            MdschemaCubesRowset.IsLinkable,
            MdschemaCubesRowset.IsWriteEnabled,
            MdschemaCubesRowset.IsSqlEnabled,
            MdschemaCubesRowset.CubeCaption,
            MdschemaCubesRowset.BaseCubeName,
            MdschemaCubesRowset.Dimensions,
            MdschemaCubesRowset.Sets,
            MdschemaCubesRowset.Measures,
            MdschemaCubesRowset.CubeSource
        },
        new Column[] {
            MdschemaCubesRowset.CatalogName,
            MdschemaCubesRowset.SchemaName,
            MdschemaCubesRowset.CubeName,
            MdschemaCubesRowset.CubeType,
            MdschemaCubesRowset.BaseCubeName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaCubesRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126180(SQL.90).aspx
     * http://msdn2.microsoft.com/en-us/library/ms126180.aspx
     *
     * restrictions
     *    CATALOG_NAME Optional.
     *    SCHEMA_NAME Optional.
     *    CUBE_NAME Optional.
     *    DIMENSION_NAME Optional.
     *    DIMENSION_UNIQUE_NAME Optional.
     *    CUBE_SOURCE (Optional) A bitmap with one of the following valid
     *    values:
     *      1 CUBE
     *      2 DIMENSION
     *    Default restriction is a value of 1.
     *
     *    DIMENSION_VISIBILITY (Optional) A bitmap with one of the following
     *    valid values:
     *      1 Visible
     *      2 Not visible
     *    Default restriction is a value of 1.
     */
    MDSCHEMA_DIMENSIONS(
        13, "C8B522D9-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            MdschemaDimensionsRowset.CatalogName,
            MdschemaDimensionsRowset.SchemaName,
            MdschemaDimensionsRowset.CubeName,
            MdschemaDimensionsRowset.DimensionName,
            MdschemaDimensionsRowset.DimensionUniqueName,
            MdschemaDimensionsRowset.DimensionGuid,
            MdschemaDimensionsRowset.DimensionCaption,
            MdschemaDimensionsRowset.DimensionOrdinal,
            MdschemaDimensionsRowset.DimensionType,
            MdschemaDimensionsRowset.DimensionCardinality,
            MdschemaDimensionsRowset.DefaultHierarchy,
            MdschemaDimensionsRowset.Description,
            MdschemaDimensionsRowset.IsVirtual,
            MdschemaDimensionsRowset.IsReadWrite,
            MdschemaDimensionsRowset.DimensionUniqueSettings,
            MdschemaDimensionsRowset.DimensionMasterUniqueName,
            MdschemaDimensionsRowset.DimensionIsVisible,
            MdschemaDimensionsRowset.Hierarchies,
        },
        new Column[] {
            MdschemaDimensionsRowset.CatalogName,
            MdschemaDimensionsRowset.SchemaName,
            MdschemaDimensionsRowset.CubeName,
            MdschemaDimensionsRowset.DimensionName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaDimensionsRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126257(SQL.90).aspx
     *
     * restrictions
     *   LIBRARY_NAME Optional.
     *   INTERFACE_NAME Optional.
     *   FUNCTION_NAME Optional.
     *   ORIGIN Optional.
     *
     * Not supported
     *  DLL_NAME
     *    Optional
     *  HELP_FILE
     *    Optional
     *  HELP_CONTEXT
     *    Optional
     *    - SQL Server xml schema says that this must be present
     *  OBJECT
     *    Optional
     *  CAPTION The display caption for the function.
     */
    MDSCHEMA_FUNCTIONS(
        14, "A07CCD07-8148-11D0-87BB-00C04FC33942", null,
        new Column[] {
            MdschemaFunctionsRowset.FunctionName,
            MdschemaFunctionsRowset.Description,
            MdschemaFunctionsRowset.ParameterList,
            MdschemaFunctionsRowset.ReturnType,
            MdschemaFunctionsRowset.Origin,
            MdschemaFunctionsRowset.InterfaceName,
            MdschemaFunctionsRowset.LibraryName,
            MdschemaFunctionsRowset.Caption,
        },
        new Column[] {
            MdschemaFunctionsRowset.LibraryName,
            MdschemaFunctionsRowset.InterfaceName,
            MdschemaFunctionsRowset.FunctionName,
            MdschemaFunctionsRowset.Origin,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaFunctionsRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126062(SQL.90).aspx
     *
     * restrictions
     *    CATALOG_NAME Optional.
     *    SCHEMA_NAME Optional.
     *    CUBE_NAME Optional.
     *    DIMENSION_UNIQUE_NAME Optional.
     *    HIERARCHY_NAME Optional.
     *    HIERARCHY_UNIQUE_NAME Optional.
     *    HIERARCHY_ORIGIN
     *       (Optional) A default restriction is in effect
     *       on MD_USER_DEFINED and MD_SYSTEM_ENABLED.
     *    CUBE_SOURCE
     *      (Optional) A bitmap with one of the following valid values:
     *      1 CUBE
     *      2 DIMENSION
     *      Default restriction is a value of 1.
     *    HIERARCHY_VISIBILITY
     *      (Optional) A bitmap with one of the following valid values:
     *      1 Visible
     *      2 Not visible
     *      Default restriction is a value of 1.
     *
     * Not supported
     *  HIERARCHY_ORIGIN
     *  HIERARCHY_DISPLAY_FOLDER
     *  INSTANCE_SELECTION
     */
    MDSCHEMA_HIERARCHIES(
        15, "C8B522DA-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            MdschemaHierarchiesRowset.CatalogName,
            MdschemaHierarchiesRowset.SchemaName,
            MdschemaHierarchiesRowset.CubeName,
            MdschemaHierarchiesRowset.DimensionUniqueName,
            MdschemaHierarchiesRowset.HierarchyName,
            MdschemaHierarchiesRowset.HierarchyUniqueName,
            MdschemaHierarchiesRowset.HierarchyGuid,
            MdschemaHierarchiesRowset.HierarchyCaption,
            MdschemaHierarchiesRowset.DimensionType,
            MdschemaHierarchiesRowset.HierarchyCardinality,
            MdschemaHierarchiesRowset.DefaultMember,
            MdschemaHierarchiesRowset.AllMember,
            MdschemaHierarchiesRowset.Description,
            MdschemaHierarchiesRowset.Structure,
            MdschemaHierarchiesRowset.IsVirtual,
            MdschemaHierarchiesRowset.IsReadWrite,
            MdschemaHierarchiesRowset.DimensionUniqueSettings,
            MdschemaHierarchiesRowset.DimensionIsVisible,
            MdschemaHierarchiesRowset.HierarchyOrdinal,
            MdschemaHierarchiesRowset.DimensionIsShared,
            MdschemaHierarchiesRowset.HierarchyIsVisibile,
            MdschemaHierarchiesRowset.HierarchyOrigin,
            MdschemaHierarchiesRowset.DisplayFolder,
            MdschemaHierarchiesRowset.CubeSource,
            MdschemaHierarchiesRowset.HierarchyVisibility,
            MdschemaHierarchiesRowset.ParentChild,
            MdschemaHierarchiesRowset.Levels,
        },
        new Column[] {
            MdschemaHierarchiesRowset.CatalogName,
            MdschemaHierarchiesRowset.SchemaName,
            MdschemaHierarchiesRowset.CubeName,
            MdschemaHierarchiesRowset.DimensionUniqueName,
            MdschemaHierarchiesRowset.HierarchyName,
            MdschemaHierarchiesRowset.HierarchyUniqueName,
            MdschemaHierarchiesRowset.HierarchyOrigin,
            MdschemaHierarchiesRowset.CubeSource,
            MdschemaHierarchiesRowset.HierarchyVisibility,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaHierarchiesRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126038(SQL.90).aspx
     *
     * restriction
     *   CATALOG_NAME Optional.
     *   SCHEMA_NAME Optional.
     *   CUBE_NAME Optional.
     *   DIMENSION_UNIQUE_NAME Optional.
     *   HIERARCHY_UNIQUE_NAME Optional.
     *   LEVEL_NAME Optional.
     *   LEVEL_UNIQUE_NAME Optional.
     *   LEVEL_ORIGIN
     *       (Optional) A default restriction is in effect
     *       on MD_USER_DEFINED and MD_SYSTEM_ENABLED
     *   CUBE_SOURCE
     *       (Optional) A bitmap with one of the following valid values:
     *       1 CUBE
     *       2 DIMENSION
     *       Default restriction is a value of 1.
     *   LEVEL_VISIBILITY
     *       (Optional) A bitmap with one of the following values:
     *       1 Visible
     *       2 Not visible
     *       Default restriction is a value of 1.
     *
     * Not supported
     *  CUSTOM_ROLLUP_SETTINGS
     *  LEVEL_UNIQUE_SETTINGS
     *  LEVEL_ORDERING_PROPERTY
     *  LEVEL_DBTYPE
     *  LEVEL_MASTER_UNIQUE_NAME
     *  LEVEL_NAME_SQL_COLUMN_NAME Customers:(All)!NAME
     *  LEVEL_KEY_SQL_COLUMN_NAME Customers:(All)!KEY
     *  LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME Customers:(All)!UNIQUE_NAME
     *  LEVEL_ATTRIBUTE_HIERARCHY_NAME
     *  LEVEL_KEY_CARDINALITY
     *  LEVEL_ORIGIN
     *
     */
    MDSCHEMA_LEVELS(
        16, "C8B522DB-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            MdschemaLevelsRowset.CatalogName,
            MdschemaLevelsRowset.SchemaName,
            MdschemaLevelsRowset.CubeName,
            MdschemaLevelsRowset.DimensionUniqueName,
            MdschemaLevelsRowset.HierarchyUniqueName,
            MdschemaLevelsRowset.LevelName,
            MdschemaLevelsRowset.LevelUniqueName,
            MdschemaLevelsRowset.LevelGuid,
            MdschemaLevelsRowset.LevelCaption,
            MdschemaLevelsRowset.LevelNumber,
            MdschemaLevelsRowset.LevelCardinality,
            MdschemaLevelsRowset.LevelType,
            MdschemaLevelsRowset.CustomRollupSettings,
            MdschemaLevelsRowset.LevelUniqueSettings,
            MdschemaLevelsRowset.LevelIsVisible,
            MdschemaLevelsRowset.Description,
            MdschemaLevelsRowset.LevelOrigin,
            MdschemaLevelsRowset.CubeSource,
            MdschemaLevelsRowset.LevelVisibility,
        },
        new Column[] {
            MdschemaLevelsRowset.CatalogName,
            MdschemaLevelsRowset.SchemaName,
            MdschemaLevelsRowset.CubeName,
            MdschemaLevelsRowset.DimensionUniqueName,
            MdschemaLevelsRowset.HierarchyUniqueName,
            MdschemaLevelsRowset.LevelNumber,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaLevelsRowset(request, handler);
        }
    },

    MDSCHEMA_MEASUREGROUP_DIMENSIONS(
        13, "a07ccd33-8148-11d0-87bb-00c04fc33942", null,
        new Column[] {
                MdschemaMeasuregroupDimensionsRowset.CatalogName,
                MdschemaMeasuregroupDimensionsRowset.SchemaName,
                MdschemaMeasuregroupDimensionsRowset.CubeName,
                MdschemaMeasuregroupDimensionsRowset.MeasuregroupName,
                MdschemaMeasuregroupDimensionsRowset.MeasuregroupCardinality,
                MdschemaMeasuregroupDimensionsRowset.DimensionUniqueName,
                MdschemaMeasuregroupDimensionsRowset.DimensionCardinality,
                MdschemaMeasuregroupDimensionsRowset.DimensionIsVisible,
                MdschemaMeasuregroupDimensionsRowset.DimensionIsFactDimension,
                MdschemaMeasuregroupDimensionsRowset.DimensionPath,
                MdschemaMeasuregroupDimensionsRowset.DimensionGranularity,
        },
        new Column[] {
                MdschemaMeasuregroupDimensionsRowset.CatalogName,
                MdschemaMeasuregroupDimensionsRowset.SchemaName,
                MdschemaMeasuregroupDimensionsRowset.CubeName,
                MdschemaMeasuregroupDimensionsRowset.MeasuregroupName,
                MdschemaMeasuregroupDimensionsRowset.DimensionUniqueName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaMeasuregroupDimensionsRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126250(SQL.90).aspx
     *
     * restrictions
     *   CATALOG_NAME Optional.
     *   SCHEMA_NAME Optional.
     *   CUBE_NAME Optional.
     *   MEASURE_NAME Optional.
     *   MEASURE_UNIQUE_NAME Optional.
     *   CUBE_SOURCE
     *     (Optional) A bitmap with one of the following valid values:
     *     1 CUBE
     *     2 DIMENSION
     *     Default restriction is a value of 1.
     *   MEASURE_VISIBILITY
     *     (Optional) A bitmap with one of the following valid values:
     *     1 Visible
     *     2 Not Visible
     *     Default restriction is a value of 1.
     *
     * Not supported
     *  MEASURE_GUID
     *  NUMERIC_PRECISION
     *  NUMERIC_SCALE
     *  MEASURE_UNITS
     *  EXPRESSION
     *  MEASURE_NAME_SQL_COLUMN_NAME
     *  MEASURE_UNQUALIFIED_CAPTION
     *  MEASUREGROUP_NAME
     *  MEASURE_DISPLAY_FOLDER
     *  DEFAULT_FORMAT_STRING
     */
    MDSCHEMA_MEASURES(
        17, "C8B522DC-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            MdschemaMeasuresRowset.CatalogName,
            MdschemaMeasuresRowset.SchemaName,
            MdschemaMeasuresRowset.CubeName,
            MdschemaMeasuresRowset.MeasureName,
            MdschemaMeasuresRowset.MeasureUniqueName,
            MdschemaMeasuresRowset.MeasureCaption,
            MdschemaMeasuresRowset.MeasureGuid,
            MdschemaMeasuresRowset.MeasureAggregator,
            MdschemaMeasuresRowset.DataType,
            MdschemaMeasuresRowset.MeasureIsVisible,
            MdschemaMeasuresRowset.LevelsList,
            MdschemaMeasuresRowset.Description,
            MdschemaMeasuresRowset.MeasuregroupName,
            MdschemaMeasuresRowset.DisplayFolder,
            MdschemaMeasuresRowset.FormatString,
            MdschemaMeasuresRowset.CubeSource,
            MdschemaMeasuresRowset.MeasureVisiblity,
        },
        new Column[] {
            MdschemaMeasuresRowset.CatalogName,
            MdschemaMeasuresRowset.SchemaName,
            MdschemaMeasuresRowset.CubeName,
            MdschemaMeasuresRowset.MeasureName,
            MdschemaMeasuresRowset.MeasureUniqueName,
            MdschemaMeasuresRowset.MeasuregroupName,
            MdschemaMeasuresRowset.CubeSource,
            MdschemaMeasuresRowset.MeasureVisiblity,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaMeasuresRowset(request, handler);
        }
    },

    /**
     *
     * http://msdn2.microsoft.com/es-es/library/ms126046.aspx
     *
     *
     * restrictions
     *   CATALOG_NAME Optional.
     *   SCHEMA_NAME Optional.
     *   CUBE_NAME Optional.
     *   DIMENSION_UNIQUE_NAME Optional.
     *   HIERARCHY_UNIQUE_NAME Optional.
     *   LEVEL_UNIQUE_NAME Optional.
     *   LEVEL_NUMBER Optional.
     *   MEMBER_NAME Optional.
     *   MEMBER_UNIQUE_NAME Optional.
     *   MEMBER_CAPTION Optional.
     *   MEMBER_TYPE Optional.
     *   TREE_OP (Optional) Only applies to a single member:
     *      MDTREEOP_ANCESTORS (0x20) returns all of the ancestors.
     *      MDTREEOP_CHILDREN (0x01) returns only the immediate children.
     *      MDTREEOP_SIBLINGS (0x02) returns members on the same level.
     *      MDTREEOP_PARENT (0x04) returns only the immediate parent.
     *      MDTREEOP_SELF (0x08) returns itself in the list of
     *                 returned rows.
     *      MDTREEOP_DESCENDANTS (0x10) returns all of the descendants.
     *   CUBE_SOURCE (Optional) A bitmap with one of the
     *      following valid values:
     *        1 CUBE
     *        2 DIMENSION
     *      Default restriction is a value of 1.
     *
     * Not supported
     */
    MDSCHEMA_MEMBERS(
        18, "C8B522DE-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            MdschemaMembersRowset.CatalogName,
            MdschemaMembersRowset.SchemaName,
            MdschemaMembersRowset.CubeName,
            MdschemaMembersRowset.DimensionUniqueName,
            MdschemaMembersRowset.HierarchyUniqueName,
            MdschemaMembersRowset.LevelUniqueName,
            MdschemaMembersRowset.LevelNumber,
            MdschemaMembersRowset.MemberOrdinal,
            MdschemaMembersRowset.MemberName,
            MdschemaMembersRowset.MemberUniqueName,
            MdschemaMembersRowset.MemberType,
            MdschemaMembersRowset.MemberGuid,
            MdschemaMembersRowset.MemberCaption,
            MdschemaMembersRowset.ChildrenCardinality,
            MdschemaMembersRowset.ParentLevel,
            MdschemaMembersRowset.ParentUniqueName,
            MdschemaMembersRowset.ParentCount,
            MdschemaMembersRowset.TreeOp_,
            MdschemaMembersRowset.Depth,
        },
        new Column[] {
            MdschemaMembersRowset.CatalogName,
            MdschemaMembersRowset.SchemaName,
            MdschemaMembersRowset.CubeName,
            MdschemaMembersRowset.DimensionUniqueName,
            MdschemaMembersRowset.HierarchyUniqueName,
            MdschemaMembersRowset.LevelUniqueName,
            MdschemaMembersRowset.LevelNumber,
            MdschemaMembersRowset.MemberOrdinal,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaMembersRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126309(SQL.90).aspx
     *
     * restrictions
     *    CATALOG_NAME Mandatory
     *    SCHEMA_NAME Optional
     *    CUBE_NAME Optional
     *    DIMENSION_UNIQUE_NAME Optional
     *    HIERARCHY_UNIQUE_NAME Optional
     *    LEVEL_UNIQUE_NAME Optional
     *
     *    MEMBER_UNIQUE_NAME Optional
     *    PROPERTY_NAME Optional
     *    PROPERTY_TYPE Optional
     *    PROPERTY_CONTENT_TYPE
     *       (Optional) A default restriction is in place on MDPROP_MEMBER
     *       OR MDPROP_CELL.
     *    PROPERTY_ORIGIN
     *       (Optional) A default restriction is in place on MD_USER_DEFINED
     *       OR MD_SYSTEM_ENABLED
     *    CUBE_SOURCE
     *       (Optional) A bitmap with one of the following valid values:
     *       1 CUBE
     *       2 DIMENSION
     *       Default restriction is a value of 1.
     *    PROPERTY_VISIBILITY
     *       (Optional) A bitmap with one of the following valid values:
     *       1 Visible
     *       2 Not visible
     *       Default restriction is a value of 1.
     *
     * Not supported
     *    PROPERTY_ORIGIN
     *    CUBE_SOURCE
     *    PROPERTY_VISIBILITY
     *    CHARACTER_MAXIMUM_LENGTH
     *    CHARACTER_OCTET_LENGTH
     *    NUMERIC_PRECISION
     *    NUMERIC_SCALE
     *    DESCRIPTION
     *    SQL_COLUMN_NAME
     *    LANGUAGE
     *    PROPERTY_ATTRIBUTE_HIERARCHY_NAME
     *    PROPERTY_CARDINALITY
     *    MIME_TYPE
     *    PROPERTY_IS_VISIBLE
     */
    MDSCHEMA_PROPERTIES(
        19, "C8B522DD-5CF3-11CE-ADE5-00AA0044773D", null,
        new Column[] {
            MdschemaPropertiesRowset.CatalogName,
            MdschemaPropertiesRowset.SchemaName,
            MdschemaPropertiesRowset.CubeName,
            MdschemaPropertiesRowset.DimensionUniqueName,
            MdschemaPropertiesRowset.HierarchyUniqueName,
            MdschemaPropertiesRowset.LevelUniqueName,
            MdschemaPropertiesRowset.MemberUniqueName,
            MdschemaPropertiesRowset.PropertyType,
            MdschemaPropertiesRowset.PropertyName,
            MdschemaPropertiesRowset.PropertyCaption,
            MdschemaPropertiesRowset.DataType,
            MdschemaPropertiesRowset.PropertyContentType,
            MdschemaPropertiesRowset.Description,
        },
            null /* not sorted */)
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaPropertiesRowset(request, handler);
        }
    },

    /**
     * http://msdn2.microsoft.com/en-us/library/ms126290(SQL.90).aspx
     *
     * restrictions
     *    CATALOG_NAME Optional.
     *    SCHEMA_NAME Optional.
     *    CUBE_NAME Optional.
     *    SET_NAME Optional.
     *    SCOPE Optional.
     *    HIERARCHY_UNIQUE_NAME Optional.
     *    CUBE_SOURCE Optional.
     *        Note: Only one hierarchy can be included, and only those named
     *        sets whose hierarchies exactly match the restriction are
     *        returned.
     *
     * Not supported
     *    EXPRESSION
     *    DIMENSIONS
     *    SET_DISPLAY_FOLDER
     */
    MDSCHEMA_SETS(
        20, "A07CCD0B-8148-11D0-87BB-00C04FC33942", null,
        new Column[] {
            MdschemaSetsRowset.CatalogName,
            MdschemaSetsRowset.SchemaName,
            MdschemaSetsRowset.CubeName,
            MdschemaSetsRowset.SetName,
            MdschemaSetsRowset.Scope,
            MdschemaSetsRowset.Description,
            MdschemaSetsRowset.Expression,
            MdschemaSetsRowset.Dimensions,
            MdschemaSetsRowset.SetCaption,
            MdschemaSetsRowset.DisplayFolder,
//            MdschemaSetsRowset.EvaluationContext,
        },
        new Column[] {
            MdschemaSetsRowset.CatalogName,
            MdschemaSetsRowset.SchemaName,
            MdschemaSetsRowset.CubeName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaSetsRowset(request, handler);
        }
    },

    /**
     */
    MDSCHEMA_KPIS(
        21, "2AE44109-ED3D-4842-B16F-B694D1CB0E3F", null,
        new Column[] {
            MdschemaKpisRowset.CatalogName,
            MdschemaKpisRowset.SchemaName,
            MdschemaKpisRowset.CubeName,
            MdschemaKpisRowset.MeasuregroupName,
            MdschemaKpisRowset.KpiName,
            MdschemaKpisRowset.KpiCaption,
            MdschemaKpisRowset.KpiDescription,
            MdschemaKpisRowset.KpiDisplayFolder,
            MdschemaKpisRowset.KpiValue,
            MdschemaKpisRowset.KpiGoal,
            MdschemaKpisRowset.KpiStatus,
            MdschemaKpisRowset.KpiTrend,
            MdschemaKpisRowset.KpiStatusGraphic,
            MdschemaKpisRowset.KpiTrendGraphic,
            MdschemaKpisRowset.KpiWeight,
            MdschemaKpisRowset.KpiCurrentTimeMember,
            MdschemaKpisRowset.KpiParentKpiName,
            MdschemaKpisRowset.Scope,
    },
        new Column[] {
            MdschemaKpisRowset.CatalogName,
            MdschemaKpisRowset.SchemaName,
            MdschemaKpisRowset.CubeName,
            MdschemaKpisRowset.MeasuregroupName,
            MdschemaKpisRowset.KpiName,
        })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
          return new MdschemaKpisRowset(request, handler);
        }
    },

    /**
     */
    MDSCHEMA_MEASUREGROUPS(
        22, "E1625EBF-FA96-42FD-BEA6-DB90ADAFD96B", null,
        new Column[] {
                MdschemaMeasuregroupsRowset.CatalogName,
                MdschemaMeasuregroupsRowset.SchemaName,
                MdschemaMeasuregroupsRowset.CubeName,
                MdschemaMeasuregroupsRowset.MeasuregroupName,
                MdschemaMeasuregroupsRowset.Description,
                MdschemaMeasuregroupsRowset.IsWriteEnabled,
                MdschemaMeasuregroupsRowset.MeasuregroupCaption,
    },
        new Column[] {
                MdschemaKpisRowset.CatalogName,
                MdschemaKpisRowset.SchemaName,
                MdschemaKpisRowset.CubeName,
                MdschemaKpisRowset.MeasuregroupName,
    })
    {
        @Override
		public Rowset getRowset(XmlaRequest request, XmlaHandler handler) {
            return new MdschemaMeasuregroupsRowset(request, handler);
        }
    };

    final transient Column[] columnDefinitions;
    final transient Column[] sortColumnDefinitions;

    /**
     * Date the schema was last modified.
     *
     * <p>TODO: currently schema grammar does not support modify date
     * so we return just some date for now.
     */
    private static final String DATE_MODIFIED = "2005-01-25T17:35:32";
    private final String description;
    private final String schemaGuid;

    static final String UUID_PATTERN =
        "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
        + "[0-9a-fA-F]{12}";

    /**
     * Creates a rowset definition.
     *
     * @param ordinal Rowset ordinal, per OLE DB for OLAP
     * @param description Description
     * @param columnDefinitions List of column definitions
     * @param sortColumnDefinitions List of column definitions to sort on,
     */
    RowsetDefinition(
        int ordinal,
        String schemaGuid,
        String description,
        Column[] columnDefinitions,
        Column[] sortColumnDefinitions)
    {
        discard(ordinal);
        this.schemaGuid = schemaGuid;
        this.description = description;
        this.columnDefinitions = columnDefinitions;
        this.sortColumnDefinitions = sortColumnDefinitions;
    }

    public abstract Rowset getRowset(XmlaRequest request, XmlaHandler handler);

    public Column lookupColumn(String name) {
        for (Column columnDefinition : columnDefinitions) {
            if (columnDefinition.name.equals(name)) {
                return columnDefinition;
            }
        }
        return null;
    }

    /**
     * Returns a comparator with which to sort rows of this rowset definition.
     * The sort order is defined by the {@link #sortColumnDefinitions} field.
     * If the rowset is not sorted, returns null.
     */
    Comparator<Rowset.Row> getComparator() {
        if (sortColumnDefinitions == null) {
            return null;
        }
        return new Comparator<>() {
            @Override
			public int compare(Rowset.Row row1, Rowset.Row row2) {
                // A faster implementation is welcome.
                for (Column sortColumn : sortColumnDefinitions) {
                    Comparable val1 = (Comparable) row1.get(sortColumn.name);
                    Comparable val2 = (Comparable) row2.get(sortColumn.name);
                    if ((val1 == null) && (val2 == null)) {
                        // columns can be optional, compare next column
                        continue;
                    } else if (val1 == null) {
                        return -1;
                    } else if (val2 == null) {
                        return 1;
                    } else if (val1 instanceof String strVal1
                       && val2 instanceof String strVal2)
                    {
                        int v =
                            strVal1.compareToIgnoreCase(strVal2);
                        // if equal (= 0), compare next column
                        if (v != 0) {
                            return v;
                        }
                    } else {
                        int v = val1.compareTo(val2);
                        // if equal (= 0), compare next column
                        if (v != 0) {
                            return v;
                        }
                    }
                }
                return 0;
            }
        };
    }

    /**
     * Generates an XML schema description to the writer.
     * This is broken into top, Row definition and bottom so that on a
     * case by case basis a RowsetDefinition can redefine the Row
     * definition output. The default assumes a flat set of elements, but
     * for example, SchemaRowsets has a element with child elements.
     *
     * @param writer SAX writer
     * @see XmlaHandler#writeDatasetXmlSchema(SaxWriter, mondrian.xmla.XmlaHandler.SetType)
     */
    void writeRowsetXmlSchema(SaxWriter writer) {
        writeRowsetXmlSchemaTop(writer);
        writeRowsetXmlSchemaRowDef(writer);
        writeRowsetXmlSchemaBottom(writer);
    }

    protected void writeRowsetXmlSchemaTop(SaxWriter writer) {
        writer.startElement(
            "xsd:schema",
            "xmlns:xsd", NS_XSD,
            "xmlns", NS_XMLA_ROWSET,
            "xmlns:xsi", NS_XSI,
            "xmlns:sql", "urn:schemas-microsoft-com:xml-sql",
            "targetNamespace", NS_XMLA_ROWSET,
            "elementFormDefault", "qualified");

        writer.startElement(
            "xsd:element",
            "name", "root");
        writer.startElement("xsd:complexType");
        writer.startElement("xsd:sequence");
        writer.element(
            "xsd:element",
            "name", "row",
            "type", "row",
            "minOccurs", 0,
            "maxOccurs", "unbounded");
        writer.endElement(); // xsd:sequence
        writer.endElement(); // xsd:complexType
        writer.endElement(); // xsd:element

        // MS SQL includes this in its schema section even thought
        // its not need for most queries.
        writer.startElement(
            "xsd:simpleType",
            "name", "uuid");
        writer.startElement(
            "xsd:restriction",
            "base", "xsd:string");
        writer.element(
            "xsd:pattern",
            "value", UUID_PATTERN);

        writer.endElement(); // xsd:restriction
        writer.endElement(); // xsd:simpleType
    }

    protected void writeRowsetXmlSchemaRowDef(SaxWriter writer) {
        writer.startElement(
            "xsd:complexType",
            "name", "row");
        writer.startElement("xsd:sequence");
        for (Column column : columnDefinitions) {
            final String name =
                XmlaUtil.ElementNameEncoder.INSTANCE.encode(column.name);
            final String xsdType = column.type.columnType;

            Object[] attrs;
            if (column.nullable) {
                if (column.unbounded) {
                    attrs = new Object[]{
                        "sql:field", column.name,
                        "name", name,
                        "type", xsdType,
                        "minOccurs", 0,
                        "maxOccurs", "unbounded"
                    };
                } else {
                    attrs = new Object[]{
                        "sql:field", column.name,
                        "name", name,
                        "type", xsdType,
                        "minOccurs", 0
                    };
                }
            } else {
                if (column.unbounded) {
                    attrs = new Object[]{
                        "sql:field", column.name,
                        "name", name,
                        "type", xsdType,
                        "maxOccurs", "unbounded"
                    };
                } else {
                    attrs = new Object[]{
                        "sql:field", column.name,
                        "name", name,
                        "type", xsdType
                    };
                }
            }
            writer.element("xsd:element", attrs);
        }
        writer.endElement(); // xsd:sequence
        writer.endElement(); // xsd:complexType
    }

    protected void writeRowsetXmlSchemaBottom(SaxWriter writer) {
        writer.endElement(); // xsd:schema
    }

    enum Type {
        STRING("string","xsd:string"),
        STRING_ARRAY("StringArray","xsd:string"),
        ARRAY("Array","xsd:string"),
        ENUMERATION("Enumeration","xsd:string"),
        ENUMERATION_ARRAY("EnumerationArray","xsd:string"),
        ENUM_STRING("EnumString","xsd:string"),
        BOOLEAN("Boolean","xsd:boolean"),
        STRING_SOMETIMES_ARRAY("StringSometimesArray","xsd:string"),
        INTEGER("Integer","xsd:int"),
        UNSIGNED_INTEGER("UnsignedInteger","xsd:unsignedInt"),
        DOUBLE("Double","xsd:double"),
        DATE_TIME("DateTime","xsd:dateTime"),
        ROW_SET("Rowset",null),
        SHORT("Short","xsd:short"),
        UUID("UUID","uuid"),
        UNSIGNED_SHORT("UnsignedShort","xsd:unsignedShort"),
        LONG("Long","xsd:long"),
        UNSIGNED_LONG("UnsignedLong","xsd:unsignedLong");

        public final String columnType;
        public final String value;

        Type(String value, String columnType) {
            this.value = value;
            this.columnType = columnType;
        }

        boolean isEnum() {
            return this == ENUMERATION
               || this == ENUMERATION_ARRAY
               || this == ENUM_STRING;
        }

        String getName() {
            return value;
        }
    }

    private static XmlaConstants.DBType getDBTypeFromProperty(Property prop) {
        switch (prop.getDatatype()) {
        case STRING:
            return XmlaConstants.DBType.WSTR;
        case INTEGER, UNSIGNED_INTEGER, DOUBLE:
            return XmlaConstants.DBType.R8;
        case BOOLEAN:
            return XmlaConstants.DBType.BOOL;
        default:
            // TODO: what type is it really, its not a string
            return XmlaConstants.DBType.WSTR;
        }
    }

    static class Column {

        /**
         * This is used as the true value for the restriction parameter.
         */
        static final boolean RESTRICTION_TRUE = true;
        /**
         * This is used as the false value for the restriction parameter.
         */
        static final boolean RESTRICTION_FALSE = false;

        /**
         * This is used as the false value for the nullable parameter.
         */
        static final boolean REQUIRED = false;
        /**
         * This is used as the true value for the nullable parameter.
         */
        static final boolean OPTIONAL = true;

        /**
         * This is used as the false value for the unbounded parameter.
         */
        static final boolean ONE_MAX = false;
        /**
         * This is used as the true value for the unbounded parameter.
         */
        static final boolean UNBOUNDED_TRUE = true;

        final String name;
        final Type type;
        final Enumeration enumeration;
        final String description;
        final boolean restriction;
        final boolean nullable;
        final boolean unbounded;
        final int restrictionOrder;

        /**
         * Creates a column.
         *
         * @param name Name of column
         * @param type A {@link mondrian.xmla.RowsetDefinition.Type} value
         * @param enumeratedType Must be specified for enumeration or array
         *                       of enumerations
         * @param description Description of column
         * @param restriction Whether column can be used as a filter on its
         *     rowset
         * @param nullable Whether column can contain null values
         * @pre type != null
         * @pre (type == Type.Enumeration
         *  || type == Type.EnumerationArray
         *  || type == Type.EnumString)
         *  == (enumeratedType != null)
         * @pre description == null || description.indexOf('\r') == -1
         */
        Column(
            String name,
            Type type,
            Enumeration enumeratedType,
            boolean restriction,
            boolean nullable,
            String description)
        {
            this(
                name, type, enumeratedType,
                restriction, 0, nullable, ONE_MAX, description);
        }

        Column(
                String name,
                Type type,
                Enumeration enumeratedType,
                boolean restriction,
                int restrictionOrder,
                boolean nullable,
                String description)
        {
            this(
                    name, type, enumeratedType,
                    restriction, restrictionOrder, nullable, ONE_MAX, description);
        }

        Column(
            String name,
            Type type,
            Enumeration enumeratedType,
            boolean restriction,
            boolean nullable,
            boolean unbounded,
            String description)
        {
            this(
                    name, type, enumeratedType,
                    restriction, 0, nullable, unbounded, description);
        }

        Column(
                String name,
                Type type,
                Enumeration enumeratedType,
                boolean restriction,
                int restrictionOrder,
                boolean nullable,
                boolean unbounded,
                String description)
        {
            assert type != null;
            assert (type == Type.ENUMERATION
                    || type == Type.ENUMERATION_ARRAY
                    || type == Type.ENUM_STRING)
                    == (enumeratedType != null);
            // Line endings must be UNIX style (LF) not Windows style (LF+CR).
            // Thus the client will receive the same XML, regardless
            // of the server O/S.
            assert description == null || description.indexOf('\r') == -1;
            this.name = name;
            this.type = type;
            this.enumeration = enumeratedType;
            this.description = description;
            this.restriction = restriction;
            this.nullable = nullable;
            this.unbounded = unbounded;
            this.restrictionOrder = restrictionOrder;
        }

        /**
         * Retrieves a value of this column from a row. The base implementation
         * uses reflection to call an accessor method; a derived class may
         * provide a different implementation.
         *
         * @param row Row
         */
        protected Object get(Object row) {
            return getFromAccessor(row);
        }

        /**
         * Retrieves the value of this column "MyColumn" from a field called
         * "myColumn".
         *
         * @param row Current row
         * @return Value of given this property of the given row
         */
        protected final Object getFromField(Object row) {
            try {
                String javaFieldName =
                    name.substring(0, 1).toLowerCase()
                    + name.substring(1);
                Field field = row.getClass().getField(javaFieldName);
                return field.get(row);
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
                throw Util.newInternal(
                    e, "Error while accessing rowset column " + name);
            }
        }

        /**
         * Retrieves the value of this column "MyColumn" by calling a method
         * called "getMyColumn()".
         *
         * @param row Current row
         * @return Value of given this property of the given row
         */
        protected final Object getFromAccessor(Object row) {
            try {
                String javaMethodName = "get" + name;
                Method method = row.getClass().getMethod(javaMethodName);
                return method.invoke(row);
            } catch (SecurityException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
                throw Util.newInternal(
                    e, "Error while accessing rowset column " + name);
            }
        }

        public String getColumnType() {
            if (type.isEnum()) {
                return enumeration.type.columnType;
            }
            return type.columnType;
        }
    }

    // -------------------------------------------------------------------------
    // From this point on, just rowset classess.

    static class DiscoverDatasourcesRowset extends Rowset {
        private static final Column DataSourceName =
            new Column(
                "DataSourceName",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the data source, such as FoodMart 2000.");
        private static final Column DataSourceDescription =
            new Column(
                "DataSourceDescription",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A description of the data source, as entered by the "
                + "publisher.");
        private static final Column URL =
            new Column(
                "URL",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The unique path that shows where to invoke the XML for "
                + "Analysis methods for that data source.");
        private static final Column DataSourceInfo =
            new Column(
                "DataSourceInfo",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A string containing any additional information required to "
                + "connect to the data source. This can include the Initial "
                + "Catalog property or other information for the provider.\n"
                + "Example: \"Provider=MSOLAP;Data Source=Local;\"");
        private static final Column ProviderName =
            new Column(
                "ProviderName",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the provider behind the data source.\n"
                + "Example: \"MSDASQL\"");
        private static final Column ProviderType =
            new Column(
                "ProviderType",
                Type.ENUMERATION_ARRAY,
                Enumeration.PROVIDER_TYPE,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                Column.UNBOUNDED_TRUE,
                "The types of data supported by the provider. May include one "
                + "or more of the following types. Example follows this "
                + "table.\n"
                + "TDP: tabular data provider.\n"
                + "MDP: multidimensional data provider.\n"
                + "DMP: data mining provider. A DMP provider implements the "
                + "OLE DB for Data Mining specification.");
        private static final Column AuthenticationMode =
            new Column(
                "AuthenticationMode",
                Type.ENUM_STRING,
                Enumeration.AUTHENTICATION_MODE,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Specification of what type of security mode the data source "
                + "uses. Values can be one of the following:\n"
                + "Unauthenticated: no user ID or password needs to be sent.\n"
                + "Authenticated: User ID and Password must be included in the "
                + "information required for the connection.\n"
                + "Integrated: the data source uses the underlying security to "
                + "determine authorization, such as Integrated Security "
                + "provided by Microsoft Internet Information Services (IIS).");

        public DiscoverDatasourcesRowset(
            XmlaRequest request, XmlaHandler handler)
        {
            super(DISCOVER_DATASOURCES, request, handler);
        }

        private static final Column[] columns = {
            DataSourceName,
            DataSourceDescription,
            URL,
            DataSourceInfo,
            ProviderName,
            ProviderType,
            AuthenticationMode,
        };

        @Override
		public void populateImpl(
            XmlaResponse response, OlapConnection connection, List<Row> rows)
            throws XmlaException, SQLException
        {
            if (needConnection()) {
                final XmlaHandler.XmlaExtra extra = getExtra(connection);
                for (Map<String, Object> ds : extra.getDataSources(connection))
                {
                    Row row = new Row();
                    for (Column column : columns) {
                        row.set(column.name, ds.get(column.name));
                    }
                    addRow(row, rows);
                }
            } else {
                // using pre-configured discover datasources response
                Row row = new Row();
                Map<String, Object> map =
                    this.handler.connectionFactory
                        .getPreConfiguredDiscoverDatasourcesResponse();
                for (Column column : columns) {
                    row.set(column.name, map.get(column.name));
                }
                addRow(row, rows);
            }
        }

        @Override
        protected boolean needConnection() {
            // If the olap connection factory has a pre configured response,
            // we don't need to connect to find metadata. This is good.
            return this.handler.connectionFactory
                       .getPreConfiguredDiscoverDatasourcesResponse() == null;
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DiscoverSchemaRowsetsRowset extends Rowset {
        private static final Column SCHEMA_NAME_COLUMN =
            new Column(
                "SchemaName",
                Type.STRING_ARRAY,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the schema/request. This returns the values in "
                + "the RequestTypes enumeration, plus any additional types "
                + "supported by the provider. The provider defines rowset "
                + "structures for the additional types");
        private static final Column SCHEMA_GUID_COLUMN =
            new Column(
                "SchemaGuid",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The GUID of the schema.");
        private static final Column RESTRICTIONS_MASK_COLUMN =
            new Column(
                "RestrictionsMask",
                Type.UNSIGNED_LONG,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "");
        private static final Column RESTRICTIONS_COLUMN =
            new Column(
                "Restrictions",
                Type.ARRAY,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "An array of the restrictions suppoted by provider. An example "
                + "follows this table.");
        private static final Column DESCRIPTION_COLUMN =
            new Column(
                "Description",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A localizable description of the schema");

        public DiscoverSchemaRowsetsRowset(
            XmlaRequest request, XmlaHandler handler)
        {
            super(DISCOVER_SCHEMA_ROWSETS, request, handler);
        }

        @Override
		public void populateImpl(
            XmlaResponse response, OlapConnection connection, List<Row> rows)
            throws XmlaException
        {
            RowsetDefinition[] rowsetDefinitions =
                RowsetDefinition.class.getEnumConstants().clone();
            Arrays.sort(
                rowsetDefinitions,
                new Comparator<RowsetDefinition>() {
                    @Override
					public int compare(
                        RowsetDefinition o1,
                        RowsetDefinition o2)
                    {
                        return o1.name().compareTo(o2.name());
                    }
                });

            List<String> restrictionSchemaNames = null;
            if(restrictions.containsKey(SCHEMA_NAME_COLUMN.name)) {
                restrictionSchemaNames = (List<String>)restrictions.get(SCHEMA_NAME_COLUMN.name);
            }

            for (RowsetDefinition rowsetDefinition : rowsetDefinitions) {
                if(restrictionSchemaNames == null || restrictionSchemaNames.contains(rowsetDefinition.name())) {
                    Row row = new Row();
                    row.set(SCHEMA_NAME_COLUMN.name, rowsetDefinition.name());

                    row.set(SCHEMA_GUID_COLUMN.name, rowsetDefinition.schemaGuid);

                    row.set(RESTRICTIONS_COLUMN.name, getRestrictions(rowsetDefinition));

                    String desc = rowsetDefinition.getDescription();
                    row.set(DESCRIPTION_COLUMN.name, (desc == null) ? "" : desc);
                    addRow(row, rows);
                }
            }
        }

        private List<XmlElement> getRestrictions(
            RowsetDefinition rowsetDefinition)
        {
            List<XmlElement> restrictionList = new ArrayList<>();
            final Column[] columns = rowsetDefinition.columnDefinitions.clone();
            Arrays.sort(
                    columns,
                    new Comparator<Column>() {
                        @Override
						public int compare(
                                Column c1,
                                Column c2)
                        {
                            return Integer.compare(c1.restrictionOrder, c2.restrictionOrder);
                        }
                    });
            for (Column column : columns) {
                if (column.restriction) {
                    restrictionList.add(
                        new XmlElement(
                            RESTRICTIONS_COLUMN.name,
                            null,
                            new XmlElement[]{
                                new XmlElement("Name", null, column.name),
                                new XmlElement(
                                    "Type",
                                    null,
                                    column.getColumnType())}));
                }
            }
            return restrictionList;
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    public String getDescription() {
        return description;
    }

    static class DiscoverPropertiesRowset extends Rowset {
        private final Predicate<PropertyDefinition> propNameCond;
        private String properetyCatalog = null;

        DiscoverPropertiesRowset(XmlaRequest request, XmlaHandler handler) {
            super(DISCOVER_PROPERTIES, request, handler);
            propNameCond = makeCondition(PROPDEF_NAME_GETTER, PropertyName);

            if(request.getProperties().containsKey(PropertyDefinition.Catalog.name())) {
                properetyCatalog = request.getProperties().get(PropertyDefinition.Catalog.name());
            }
        }

        private static final Column PropertyName =
            new Column(
                "PropertyName",
                Type.STRING_SOMETIMES_ARRAY,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the property.");
        private static final Column PropertyDescription =
            new Column(
                "PropertyDescription",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A localizable text description of the property.");
        private static final Column PropertyType =
            new Column(
                "PropertyType",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The XML data type of the property.");
        private static final Column PropertyAccessType =
            new Column(
                "PropertyAccessType",
                Type.ENUM_STRING,
                Enumeration.ACCESS,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Access for the property. The value can be Read, Write, or "
                + "ReadWrite.");
        private static final Column IsRequired =
            new Column(
                "IsRequired",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "True if a property is required, false if it is not required.");
        private static final Column Value =
            new Column(
                "Value",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The current value of the property.");

        @Override
		public void populateImpl(
            XmlaResponse response, OlapConnection connection, List<Row> rows)
            throws XmlaException, OlapException
        {
            for (PropertyDefinition propertyDefinition
                : PropertyDefinition.class.getEnumConstants())
            {
                if (!propNameCond.test(propertyDefinition)) {
                    continue;
                }
                Row row = new Row();
                row.set(PropertyName.name, propertyDefinition.name());
                row.set(
                    PropertyDescription.name, propertyDefinition.description);
                row.set(PropertyType.name, propertyDefinition.type.getName());
                row.set(PropertyAccessType.name, propertyDefinition.access);
                row.set(IsRequired.name, false);

                String propertyValue = "";
                if(propertyDefinition.name().equals(PropertyDefinition.Catalog.name())) {
                    List<Catalog> catalogs = connection.getOlapCatalogs();
                    if(this.properetyCatalog != null) {
                        for(Catalog currentCatalog: catalogs) {
                            if(currentCatalog.getName().equals(this.properetyCatalog)) {
                                propertyValue = currentCatalog.getName();
                                break;
                            }
                        }
                    }
                    else if(!catalogs.isEmpty()){
                        propertyValue = catalogs.get(0).getName();
                    }
                }
                else {
                    propertyValue = propertyDefinition.value;
                }
                row.set(Value.name, propertyValue);

                addRow(row, rows);
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DiscoverEnumeratorsRowset extends Rowset {
        DiscoverEnumeratorsRowset(XmlaRequest request, XmlaHandler handler) {
            super(DISCOVER_ENUMERATORS, request, handler);
        }

        private static final Column EnumName =
            new Column(
                "EnumName",
                Type.STRING_ARRAY,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the enumerator that contains a set of values.");
        private static final Column EnumDescription =
            new Column(
                "EnumDescription",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A localizable description of the enumerator.");
        private static final Column EnumType =
            new Column(
                "EnumType",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The data type of the Enum values.");
        private static final Column ElementName =
            new Column(
                "ElementName",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The name of one of the value elements in the enumerator set.\n"
                + "Example: TDP");
        private static final Column ElementDescription =
            new Column(
                "ElementDescription",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A localizable description of the element (optional).");
        private static final Column ElementValue =
            new Column(
                "ElementValue",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The value of the element.\nExample: 01");

        @Override
		public void populateImpl(
            XmlaResponse response, OlapConnection connection, List<Row> rows)
            throws XmlaException
        {
            List<Enumeration> enumerators = getEnumerators();
            for (Enumeration enumerator : enumerators) {
                final List<? extends Enum> values = enumerator.getValues();
                for (Enum<?> value : values) {
                    Row row = new Row();
                    row.set(EnumName.name, enumerator.name);
                    row.set(EnumDescription.name, enumerator.description);

                    // Note: SQL Server always has EnumType string
                    // Need type of element of array, not the array
                    // it self.
                    row.set(EnumType.name, "string");

                    final String name =
                        (value instanceof XmlaConstant xmlaConstant)
                            ? xmlaConstant.xmlaName()
                            : value.name();
                    row.set(ElementName.name, name);

                    final String description =
                     (value instanceof XmlaConstant xmlaConstant)
                        ? xmlaConstant.getDescription()
                         : (value instanceof XmlaConstants.EnumWithDesc enumWithDesc)
                        ? enumWithDesc.getDescription()
                             : null;
                    if (description != null) {
                        row.set(
                            ElementDescription.name,
                            description);
                    }

                    switch (enumerator.type) {
                    case STRING, STRING_ARRAY:
                        // these don't have ordinals
                        break;
                    default:
                        final int ordinal =
                            (value instanceof XmlaConstant xmlaConstant
                             && xmlaConstant.xmlaOrdinal() != -1)
                                ? xmlaConstant.xmlaOrdinal()
                                : value.ordinal();
                        row.set(ElementValue.name, ordinal);
                        break;
                    }
                    addRow(row, rows);
                }
            }
        }

        private static List<Enumeration> getEnumerators() {
            // Build a set because we need to eliminate duplicates.
            SortedSet<Enumeration> enumeratorSet = new TreeSet<>(
                new Comparator<Enumeration>() {
                    @Override
					public int compare(Enumeration o1, Enumeration o2) {
                        return o1.name.compareTo(o2.name);
                    }
                }
            );
            for (RowsetDefinition rowsetDefinition
                : RowsetDefinition.class.getEnumConstants())
            {
                for (Column column : rowsetDefinition.columnDefinitions) {
                    if (column.enumeration != null) {
                        enumeratorSet.add(column.enumeration);
                    }
                }
            }
            return new ArrayList<>(enumeratorSet);
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DiscoverKeywordsRowset extends Rowset {
        DiscoverKeywordsRowset(XmlaRequest request, XmlaHandler handler) {
            super(DISCOVER_KEYWORDS, request, handler);
        }

        private static final Column Keyword =
            new Column(
                "Keyword",
                Type.STRING_SOMETIMES_ARRAY,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "A list of all the keywords reserved by a provider.\n"
                + "Example: AND");

        @Override
		public void populateImpl(
            XmlaResponse response, OlapConnection connection, List<Row> rows)
            throws XmlaException
        {
            MondrianServer mondrianServer = MondrianServer.forId(null);
            for (String keyword : mondrianServer.getKeywords()) {
                Row row = new Row();
                row.set(Keyword.name, keyword);
                addRow(row, rows);
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DiscoverLiteralsRowset extends Rowset {
        DiscoverLiteralsRowset(XmlaRequest request, XmlaHandler handler) {
            super(DISCOVER_LITERALS, request, handler);
        }

        private static final Column LiteralName = new Column(
            "LiteralName",
            Type.STRING_SOMETIMES_ARRAY,
            null,
            Column.RESTRICTION_TRUE,
            Column.REQUIRED,
            "The name of the literal described in the row.\n"
            + "Example: DBLITERAL_LIKE_PERCENT");

        private static final Column LiteralValue = new Column(
            "LiteralValue",
            Type.STRING,
            null,
            Column.RESTRICTION_FALSE,
            Column.OPTIONAL,
            "Contains the actual literal value.\n"
            + "Example, if LiteralName is DBLITERAL_LIKE_PERCENT and the "
            + "percent character (%) is used to match zero or more characters "
            + "in a LIKE clause, this column's value would be \"%\".");

        private static final Column LiteralInvalidChars = new Column(
            "LiteralInvalidChars",
            Type.STRING,
            null,
            Column.RESTRICTION_FALSE,
            Column.OPTIONAL,
            "The characters, in the literal, that are not valid.\n"
            + "For example, if table names can contain anything other than a "
            + "numeric character, this string would be \"0123456789\".");

        private static final Column LiteralInvalidStartingChars = new Column(
            "LiteralInvalidStartingChars",
            Type.STRING,
            null,
            Column.RESTRICTION_FALSE,
            Column.OPTIONAL,
            "The characters that are not valid as the first character of the "
            + "literal. If the literal can start with any valid character, "
            + "this is null.");

        private static final Column LiteralMaxLength = new Column(
            "LiteralMaxLength",
            Type.INTEGER,
            null,
            Column.RESTRICTION_FALSE,
            Column.OPTIONAL,
            "The maximum number of characters in the literal. If there is no "
            + "maximum or the maximum is unknown, the value is ?1.");

        private static final Column LiteralNameEnumValue = new Column(
                "LiteralNameEnumValue",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "");
        @Override
		public void populateImpl(
            XmlaResponse response, OlapConnection connection, List<Row> rows)
            throws XmlaException
        {
            populate(
                mondrian.xmla.XmlaConstants.Literal.class,
                rows,
                new Comparator<mondrian.xmla.XmlaConstants.Literal>() {
                @Override
				public int compare(
                    mondrian.xmla.XmlaConstants.Literal o1,
                    mondrian.xmla.XmlaConstants.Literal o2)
                {
                    return o1.name().compareTo(o2.name());
                }
            });
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DiscoverXmlMetadataRowset extends Rowset {
        private final Predicate<Catalog> catalogNameCond;

        DiscoverXmlMetadataRowset(XmlaRequest request, XmlaHandler handler) {
            super(DISCOVER_XML_METADATA, request, handler);
            catalogNameCond = makeCondition(CATALOG_NAME_GETTER, DatabaseID);
        }

        private static final Column METADATA = new Column(
                "METADATA",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "An XML document that describes the object requested by the restriction.");

        private static final Column DatabaseID = new Column(
                "DatabaseID",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                null);

        @Override
		public void populateImpl(
                XmlaResponse response, OlapConnection connection, List<Row> rows)
                throws XmlaException
        {
            if (catIter(connection, catalogNameCond).iterator().hasNext()) {
                String catalogStr;
                try {
                    final String catalogUrl = ((mondrian.olap4j.MondrianOlap4jConnection)connection)
                            .getMondrianConnection().getCatalogName();
                    catalogStr = Util.readVirtualFileAsString(catalogUrl);
                } catch (OlapException | IOException e) {
                    throw new RuntimeException(e);
                }
                Row row = new Row();
                row.set(METADATA.name, catalogStr);
                addRow(row, rows);
            }
        }

        @Override
		protected void setProperty(
                PropertyDefinition propertyDef,
                String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                    super.setProperty(propertyDef, value);
            }
        }
    }

    static class DbschemaCatalogsRowset extends Rowset {
        private final Predicate<Catalog> catalogNameCond;

        DbschemaCatalogsRowset(XmlaRequest request, XmlaHandler handler) {
            super(DBSCHEMA_CATALOGS, request, handler);
            catalogNameCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
        }

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Catalog name. Cannot be NULL.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Human-readable description of the catalog.");
        private static final Column Roles =
            new Column(
                "ROLES",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A comma delimited list of roles to which the current user "
                + "belongs. An asterisk (*) is included as a role if the "
                + "current user is a server or database administrator. "
                + "Username is appended to ROLES if one of the roles uses "
                + "dynamic security.");
        private static final Column DateModified =
            new Column(
                "DATE_MODIFIED",
                Type.DATE_TIME,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The date that the catalog was last modified.");

        @Override
		public void populateImpl(
            XmlaResponse response, OlapConnection connection, List<Row> rows)
            throws XmlaException, SQLException
        {
            //Don't need catNameCond() condition. Have to return all catalogs.
            //Catalog in the properties has not filter DBSCHEMA_CATALOGS. Only if is set in restrictions.
            for (Catalog catalog
                : catIter(connection, catalogNameCond))
            {
                for (Schema schema : catalog.getSchemas()) {
                    Row row = new Row();
                    row.set(CatalogName.name, catalog.getName());

                    // TODO: currently schema grammar does not support a
                    // description
                    row.set(Description.name, "No description available");

                    // get Role names
                    StringBuilder buf = new StringBuilder(100);
                    List<String> roleNames =
                        getExtra(connection).getSchemaRoleNames(schema);
                    serialize(buf, roleNames);
                    row.set(Roles.name, buf.toString());

                    //If it is ROLAP - return when data was last changed or just current date time.
                    //It could be Session start date
                    Format formatter =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String formattedDate = formatter.format(new Date());
                    row.set(DateModified.name, formattedDate);

                    addRow(row, rows);
                }
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DbschemaColumnsRowset extends Rowset {
        private final Predicate<Catalog> tableCatalogCond;
        private final Predicate<Cube> tableNameCond;
        private final Predicate<String> columnNameCond;

        DbschemaColumnsRowset(XmlaRequest request, XmlaHandler handler) {
            super(DBSCHEMA_COLUMNS, request, handler);
            tableCatalogCond = makeCondition(CATALOG_NAME_GETTER, TableCatalog);
            tableNameCond = makeCondition(ELEMENT_NAME_GETTER, TableName);
            columnNameCond = makeCondition(ColumnName);
        }

        private static final Column TableCatalog =
            new Column(
                "TABLE_CATALOG",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the Database.");
        private static final Column TableSchema =
            new Column(
                "TABLE_SCHEMA",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                null);
        private static final Column TableName =
            new Column(
                "TABLE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the cube.");
        private static final Column ColumnName =
            new Column(
                "COLUMN_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the attribute hierarchy or measure.");
        private static final Column OrdinalPosition =
            new Column(
                "ORDINAL_POSITION",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The position of the column, beginning with 1.");
        private static final Column ColumnHasDefault =
            new Column(
                "COLUMN_HAS_DEFAULT",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Not supported.");
        /*
         *  A bitmask indicating the information stored in
         *      DBCOLUMNFLAGS in OLE DB.
         *  1 = Bookmark
         *  2 = Fixed length
         *  4 = Nullable
         *  8 = Row versioning
         *  16 = Updateable column
         *
         * And, of course, MS SQL Server sometimes has the value of 80!!
        */
        private static final Column ColumnFlags =
            new Column(
                "COLUMN_FLAGS",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A DBCOLUMNFLAGS bitmask indicating column properties.");
        private static final Column IsNullable =
            new Column(
                "IS_NULLABLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Always returns false.");
        private static final Column DataType =
            new Column(
                "DATA_TYPE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The data type of the column. Returns a string for dimension "
                + "columns and a variant for measures.");
        private static final Column CharacterMaximumLength =
            new Column(
                "CHARACTER_MAXIMUM_LENGTH",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The maximum possible length of a value within the column.");
        private static final Column CharacterOctetLength =
            new Column(
                "CHARACTER_OCTET_LENGTH",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The maximum possible length of a value within the column, in "
                + "bytes, for character or binary columns.");
        private static final Column NumericPrecision =
            new Column(
                "NUMERIC_PRECISION",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The maximum precision of the column for numeric data types "
                + "other than DBTYPE_VARNUMERIC.");
        private static final Column NumericScale =
            new Column(
                "NUMERIC_SCALE",
                Type.SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The number of digits to the right of the decimal point for "
                + "DBTYPE_DECIMAL, DBTYPE_NUMERIC, DBTYPE_VARNUMERIC. "
                + "Otherwise, this is NULL.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, OlapException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), tableCatalogCond))
            {
                // By definition, mondrian catalogs have only one
                // schema. It is safe to use get(0)
                final Schema schema = catalog.getSchemas().get(0);
                final boolean emitInvisibleMembers =
                    XmlaUtil.shouldEmitInvisibleMembers(request);
                int ordinalPosition = 1;
                Row row;

                for (Cube cube : filter(sortedCubes(schema), tableNameCond)) {
                    for (Dimension dimension : cube.getDimensions()) {
                        for (Hierarchy hierarchy : dimension.getHierarchies()) {
                            ordinalPosition =
                                populateHierarchy(
                                    cube, hierarchy,
                                    ordinalPosition, rows);
                        }
                    }

                    List<Measure> rms = cube.getMeasures();
                    for (int k = 1; k < rms.size(); k++) {
                        Measure member = rms.get(k);

                        // null == true for regular cubes
                        // virtual cubes do not set the visible property
                        // on its measures so it might be null.
                        Boolean visible = (Boolean)
                            member.getPropertyValue(
                                Property.StandardMemberProperty.$visible);
                        if (visible == null) {
                            visible = true;
                        }
                        if (!emitInvisibleMembers && !visible) {
                            continue;
                        }

                        String memberName = member.getName();
                        final String columnName = "Measures:" + memberName;
                        if (!columnNameCond.test(columnName)) {
                            continue;
                        }

                        row = new Row();
                        row.set(TableCatalog.name, catalog.getName());
                        row.set(TableName.name, cube.getName());
                        row.set(ColumnName.name, columnName);
                        row.set(OrdinalPosition.name, ordinalPosition++);
                        row.set(ColumnHasDefault.name, false);
                        row.set(ColumnFlags.name, 0);
                        row.set(IsNullable.name, false);
                        // TODO: here is where one tries to determine the
                        // type of the column - since these are all
                        // Measures, aggregate Measures??, maybe they
                        // are all numeric? (or currency)
                        row.set(
                            DataType.name,
                            XmlaConstants.DBType.R8.xmlaOrdinal());
                        // TODO: 16/255 seems to be what MS SQL Server
                        // always returns.
                        row.set(NumericPrecision.name, 16);
                        row.set(NumericScale.name, 255);
                        addRow(row, rows);
                    }
                }
            }
        }

        private int populateHierarchy(
            Cube cube,
            Hierarchy hierarchy,
            int ordinalPosition,
            List<Row> rows)
        {
            String schemaName = cube.getSchema().getName();
            String cubeName = cube.getName();
            String hierarchyName = hierarchy.getName();

            if (hierarchy.hasAll()) {
                Row row = new Row();
                row.set(TableCatalog.name, schemaName);
                row.set(TableName.name, cubeName);
                row.set(ColumnName.name, new StringBuilder(hierarchyName).append(":(All)!NAME").toString());
                row.set(OrdinalPosition.name, ordinalPosition++);
                row.set(ColumnHasDefault.name, false);
                row.set(ColumnFlags.name, 0);
                row.set(IsNullable.name, false);
                // names are always WSTR
                row.set(DataType.name, XmlaConstants.DBType.WSTR.xmlaOrdinal());
                row.set(CharacterMaximumLength.name, 0);
                row.set(CharacterOctetLength.name, 0);
                addRow(row, rows);

                row = new Row();
                row.set(TableCatalog.name, schemaName);
                row.set(TableName.name, cubeName);
                row.set(ColumnName.name, new StringBuilder(hierarchyName).append(":(All)!UNIQUE_NAME").toString());
                row.set(OrdinalPosition.name, ordinalPosition++);
                row.set(ColumnHasDefault.name, false);
                row.set(ColumnFlags.name, 0);
                row.set(IsNullable.name, false);
                // names are always WSTR
                row.set(DataType.name, XmlaConstants.DBType.WSTR.xmlaOrdinal());
                row.set(CharacterMaximumLength.name, 0);
                row.set(CharacterOctetLength.name, 0);
                addRow(row, rows);

                if (false) {
                    // TODO: SQLServer outputs this hasall KEY column name -
                    // don't know what it's for
                    row = new Row();
                    row.set(TableCatalog.name, schemaName);
                    row.set(TableName.name, cubeName);
                    row.set(ColumnName.name, new StringBuilder(hierarchyName).append(":(All)!KEY").toString());
                    row.set(OrdinalPosition.name, ordinalPosition++);
                    row.set(ColumnHasDefault.name, false);
                    row.set(ColumnFlags.name, 0);
                    row.set(IsNullable.name, false);
                    // names are always BOOL
                    row.set(
                        DataType.name, XmlaConstants.DBType.BOOL.xmlaOrdinal());
                    row.set(NumericPrecision.name, 255);
                    row.set(NumericScale.name, 255);
                    addRow(row, rows);
                }
            }

            for (Level level : hierarchy.getLevels()) {
                ordinalPosition =
                    populateLevel(
                        cube, hierarchy, level, ordinalPosition, rows);
            }
            return ordinalPosition;
        }

        private int populateLevel(
            Cube cube,
            Hierarchy hierarchy,
            Level level,
            int ordinalPosition,
            List<Row> rows)
        {
            String schemaName = cube.getSchema().getName();
            String cubeName = cube.getName();
            String hierarchyName = hierarchy.getName();
            String levelName = level.getName();

            Row row = new Row();
            row.set(TableCatalog.name, schemaName);
            row.set(TableName.name, cubeName);
            row.set(
                ColumnName.name,
                new StringBuilder(hierarchyName).append(':').append(levelName).append("!NAME").toString());
            row.set(OrdinalPosition.name, ordinalPosition++);
            row.set(ColumnHasDefault.name, false);
            row.set(ColumnFlags.name, 0);
            row.set(IsNullable.name, false);
            // names are always WSTR
            row.set(DataType.name, XmlaConstants.DBType.WSTR.xmlaOrdinal());
            row.set(CharacterMaximumLength.name, 0);
            row.set(CharacterOctetLength.name, 0);
            addRow(row, rows);

            row = new Row();
            row.set(TableCatalog.name, schemaName);
            row.set(TableName.name, cubeName);
            row.set(
                ColumnName.name,
                new StringBuilder(hierarchyName).append(':')
                    .append(levelName).append("!UNIQUE_NAME").toString());
            row.set(OrdinalPosition.name, ordinalPosition++);
            row.set(ColumnHasDefault.name, false);
            row.set(ColumnFlags.name, 0);
            row.set(IsNullable.name, false);
            // names are always WSTR
            row.set(DataType.name, XmlaConstants.DBType.WSTR.xmlaOrdinal());
            row.set(CharacterMaximumLength.name, 0);
            row.set(CharacterOctetLength.name, 0);
            addRow(row, rows);

/*
TODO: see above
            row = new Row();
            row.set(TableCatalog.name, schemaName);
            row.set(TableName.name, cubeName);
            row.set(ColumnName.name,
                hierarchyName + ":" + levelName + "!KEY");
            row.set(OrdinalPosition.name, ordinalPosition++);
            row.set(ColumnHasDefault.name, false);
            row.set(ColumnFlags.name, 0);
            row.set(IsNullable.name, false);
            // names are always BOOL
            row.set(DataType.name, DBType.BOOL.ordinal());
            row.set(NumericPrecision.name, 255);
            row.set(NumericScale.name, 255);
            addRow(row, rows);
*/
            NamedList<Property> props = level.getProperties();
            for (Property prop : props) {
                String propName = prop.getName();

                row = new Row();
                row.set(TableCatalog.name, schemaName);
                row.set(TableName.name, cubeName);
                row.set(
                    ColumnName.name,
                    hierarchyName + ':' + levelName + '!' + propName);
                row.set(OrdinalPosition.name, ordinalPosition++);
                row.set(ColumnHasDefault.name, false);
                row.set(ColumnFlags.name, 0);
                row.set(IsNullable.name, false);

                XmlaConstants.DBType dbType = getDBTypeFromProperty(prop);
                row.set(DataType.name, dbType.xmlaOrdinal());

                switch (prop.getDatatype()) {
                case STRING:
                    row.set(CharacterMaximumLength.name, 0);
                    row.set(CharacterOctetLength.name, 0);
                    break;
                case INTEGER, UNSIGNED_INTEGER, DOUBLE:
                    // TODO: 16/255 seems to be what MS SQL Server
                    // always returns.
                    row.set(NumericPrecision.name, 16);
                    row.set(NumericScale.name, 255);
                    break;
                case BOOLEAN:
                    row.set(NumericPrecision.name, 255);
                    row.set(NumericScale.name, 255);
                    break;
                default:
                    // TODO: what type is it really, its
                    // not a string
                    row.set(CharacterMaximumLength.name, 0);
                    row.set(CharacterOctetLength.name, 0);
                    break;
                }
                addRow(row, rows);
            }
            return ordinalPosition;
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DbschemaProviderTypesRowset extends Rowset {
        private final Predicate<Integer> dataTypeCond;

        DbschemaProviderTypesRowset(XmlaRequest request, XmlaHandler handler) {
            super(DBSCHEMA_PROVIDER_TYPES, request, handler);
            dataTypeCond = makeCondition(DataType);
        }

        /*
        DATA_TYPE DBTYPE_UI2
        BEST_MATCH DBTYPE_BOOL
        Column(String name, Type type, Enumeration enumeratedType,
        boolean restriction, boolean nullable, String description)
        */
        /*
         * These are the columns returned by SQL Server.
         */
        private static final Column TypeName =
            new Column(
                "TYPE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The provider-specific data type name.");
        private static final Column DataType =
            new Column(
                "DATA_TYPE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The indicator of the data type.");
        private static final Column ColumnSize =
            new Column(
                "COLUMN_SIZE",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The length of a non-numeric column. If the data type is "
                + "numeric, this is the upper bound on the maximum precision "
                + "of the data type.");
        private static final Column LiteralPrefix =
            new Column(
                "LITERAL_PREFIX",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The character or characters used to prefix a literal of this "
                + "type in a text command.");
        private static final Column LiteralSuffix =
            new Column(
                "LITERAL_SUFFIX",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The character or characters used to suffix a literal of this "
                + "type in a text command.");
        private static final Column IsNullable =
            new Column(
                "IS_NULLABLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the data type is nullable. "
                + "NULL-- indicates that it is not known whether the data type "
                + "is nullable.");
        private static final Column CaseSensitive =
            new Column(
                "CASE_SENSITIVE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the data type is a "
                + "characters type and case-sensitive.");
        private static final Column Searchable =
            new Column(
                "SEARCHABLE",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "An integer indicating how the data type can be used in "
                + "searches if the provider supports ICommandText; otherwise, "
                + "NULL.");
        private static final Column UnsignedAttribute =
            new Column(
                "UNSIGNED_ATTRIBUTE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the data type is unsigned.");
        private static final Column FixedPrecScale =
            new Column(
                "FIXED_PREC_SCALE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the data type has a fixed "
                + "precision and scale.");
        private static final Column AutoUniqueValue =
            new Column(
                "AUTO_UNIQUE_VALUE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the data type is "
                + "autoincrementing.");
        private static final Column IsLong =
            new Column(
                "IS_LONG",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the data type is a binary "
                + "large object (BLOB) and has very long data.");
        private static final Column BestMatch =
            new Column(
                "BEST_MATCH",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the data type is a best "
                + "match.");

        @Override
        protected boolean needConnection() {
            return false;
        }

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException
        {
            // Identifies the (base) data types supported by the data provider.
            Row row;

            // i4
            Integer dt = XmlaConstants.DBType.I4.xmlaOrdinal();
            if (dataTypeCond.test(dt)) {
                row = new Row();
                row.set(TypeName.name, XmlaConstants.DBType.I4.userName);
                row.set(DataType.name, dt);
                row.set(ColumnSize.name, 8);
                row.set(IsNullable.name, true);
                row.set(Searchable.name, null);
                row.set(UnsignedAttribute.name, false);
                row.set(FixedPrecScale.name, false);
                row.set(AutoUniqueValue.name, false);
                row.set(IsLong.name, false);
                row.set(BestMatch.name, true);
                addRow(row, rows);
            }

            // R8
            dt = XmlaConstants.DBType.R8.xmlaOrdinal();
            if (dataTypeCond.test(dt)) {
                row = new Row();
                row.set(TypeName.name, XmlaConstants.DBType.R8.userName);
                row.set(DataType.name, dt);
                row.set(ColumnSize.name, 16);
                row.set(IsNullable.name, true);
                row.set(Searchable.name, null);
                row.set(UnsignedAttribute.name, false);
                row.set(FixedPrecScale.name, false);
                row.set(AutoUniqueValue.name, false);
                row.set(IsLong.name, false);
                row.set(BestMatch.name, true);
                addRow(row, rows);
            }

            // CY
            dt = XmlaConstants.DBType.CY.xmlaOrdinal();
            if (dataTypeCond.test(dt)) {
                row = new Row();
                row.set(TypeName.name, XmlaConstants.DBType.CY.userName);
                row.set(DataType.name, dt);
                row.set(ColumnSize.name, 8);
                row.set(IsNullable.name, true);
                row.set(Searchable.name, null);
                row.set(UnsignedAttribute.name, false);
                row.set(FixedPrecScale.name, false);
                row.set(AutoUniqueValue.name, false);
                row.set(IsLong.name, false);
                row.set(BestMatch.name, true);
                addRow(row, rows);
            }

            // BOOL
            dt = XmlaConstants.DBType.BOOL.xmlaOrdinal();
            if (dataTypeCond.test(dt)) {
                row = new Row();
                row.set(TypeName.name, XmlaConstants.DBType.BOOL.userName);
                row.set(DataType.name, dt);
                row.set(ColumnSize.name, 1);
                row.set(IsNullable.name, true);
                row.set(Searchable.name, null);
                row.set(UnsignedAttribute.name, false);
                row.set(FixedPrecScale.name, false);
                row.set(AutoUniqueValue.name, false);
                row.set(IsLong.name, false);
                row.set(BestMatch.name, true);
                addRow(row, rows);
            }

            // I8
            dt = XmlaConstants.DBType.I8.xmlaOrdinal();
            if (dataTypeCond.test(dt)) {
                row = new Row();
                row.set(TypeName.name, XmlaConstants.DBType.I8.userName);
                row.set(DataType.name, dt);
                row.set(ColumnSize.name, 16);
                row.set(IsNullable.name, true);
                row.set(Searchable.name, null);
                row.set(UnsignedAttribute.name, false);
                row.set(FixedPrecScale.name, false);
                row.set(AutoUniqueValue.name, false);
                row.set(IsLong.name, false);
                row.set(BestMatch.name, true);
                addRow(row, rows);
            }

            // WSTR
            dt = XmlaConstants.DBType.WSTR.xmlaOrdinal();
            if (dataTypeCond.test(dt)) {
                row = new Row();
                row.set(TypeName.name, XmlaConstants.DBType.WSTR.userName);
                row.set(DataType.name, dt);
                // how big are the string columns in the db
                row.set(ColumnSize.name, 255);
                row.set(LiteralPrefix.name, "\"");
                row.set(LiteralSuffix.name, "\"");
                row.set(IsNullable.name, true);
                row.set(CaseSensitive.name, false);
                row.set(Searchable.name, null);
                row.set(FixedPrecScale.name, false);
                row.set(AutoUniqueValue.name, false);
                row.set(IsLong.name, false);
                row.set(BestMatch.name, true);
                addRow(row, rows);
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DbschemaSchemataRowset extends Rowset {
        private final Predicate<Catalog> catalogNameCond;

        DbschemaSchemataRowset(XmlaRequest request, XmlaHandler handler) {
            super(DBSCHEMA_SCHEMATA, request, handler);
            catalogNameCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
        }

        /*
         * These are the columns returned by SQL Server.
         */
        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The provider-specific data type name.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The indicator of the data type.");
        private static final Column SchemaOwner =
            new Column(
                "SCHEMA_OWNER",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The length of a non-numeric column. If the data type is "
                + "numeric, this is the upper bound on the maximum precision "
                + "of the data type.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, OlapException
        {
            for (Catalog catalog
                : catIter(connection, catalogNameCond, catNameCond()))
            {
                for (Schema schema : catalog.getSchemas()) {
                    Row row = new Row();
                    row.set(CatalogName.name, catalog.getName());
                    row.set(SchemaName.name, schema.getName());
                    row.set(SchemaOwner.name, "");
                    addRow(row, rows);
                }
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DbschemaTablesRowset extends Rowset {
        private final Predicate<Catalog> tableCatalogCond;
        private final Predicate<Cube> tableNameCond;
        private final Predicate<String> tableTypeCond;

        DbschemaTablesRowset(XmlaRequest request, XmlaHandler handler) {
            super(DBSCHEMA_TABLES, request, handler);
            tableCatalogCond = makeCondition(CATALOG_NAME_GETTER, TableCatalog);
            tableNameCond = makeCondition(ELEMENT_NAME_GETTER, TableName);
            tableTypeCond = makeCondition(TableType);
        }

        private static final Column TableCatalog =
            new Column(
                "TABLE_CATALOG",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the catalog to which this object belongs.");
        private static final Column TableSchema =
            new Column(
                "TABLE_SCHEMA",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the cube to which this object belongs.");
        private static final Column TableName =
            new Column(
                "TABLE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the object, if TABLE_TYPE is TABLE.");
        private static final Column TableType =
            new Column(
                "TABLE_TYPE",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The type of the table. TABLE indicates the object is a "
                + "measure group. SYSTEM TABLE indicates the object is a "
                + "dimension.");

        private static final Column TableGuid =
            new Column(
                "TABLE_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Not supported.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A human-readable description of the object.");
        private static final Column TablePropId =
            new Column(
                "TABLE_PROPID",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Not supported.");
        private static final Column DateCreated =
            new Column(
                "DATE_CREATED",
                Type.DATE_TIME,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Not supported.");
        private static final Column DateModified =
            new Column(
                "DATE_MODIFIED",
                Type.DATE_TIME,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The date the object was last modified.");

        /*
        private static final Column TableOlapType =
            new Column(
                "TABLE_OLAP_TYPE",
                Type.String,
                null,
                Column.RESTRICTION,
                Column.OPTIONAL,
                "The OLAP type of the object.  MEASURE_GROUP indicates the "
                + "object is a measure group.  CUBE_DIMENSION indicated the "
                + "object is a dimension.");
        */

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, OlapException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), tableCatalogCond))
            {
                // By definition, mondrian catalogs have only one
                // schema. It is safe to use get(0)
                final Schema schema = catalog.getSchemas().get(0);
                Row row;
                for (Cube cube : filter(sortedCubes(schema), tableNameCond)) {
                    String desc = cube.getDescription();
                    if (desc == null) {
                        //TODO: currently this is always null
                        desc =
                            new StringBuilder(catalog.getName()).append(" - ")
                                .append(cube.getName()).append(" Cube").toString();
                    }

                    if (tableTypeCond.test("TABLE")) {
                        row = new Row();
                        row.set(TableCatalog.name, catalog.getName());
                        row.set(TableName.name, cube.getName());
                        row.set(TableType.name, "TABLE");
                        row.set(Description.name, desc);
                        if (false) {
                            row.set(DateModified.name, DATE_MODIFIED);
                        }
                        addRow(row, rows);
                    }


                    if (tableTypeCond.test("SYSTEM TABLE")) {
                        for (Dimension dimension : cube.getDimensions()) {
                            if (dimension.getDimensionType()
                                == Dimension.Type.MEASURE)
                            {
                                continue;
                            }
                            for (Hierarchy hierarchy
                                : dimension.getHierarchies())
                            {
                                populateHierarchy(
                                    cube, hierarchy, rows);
                            }
                        }
                    }
                }
            }
        }

        private void populateHierarchy(
            Cube cube, Hierarchy hierarchy, List<Row> rows)
        {
/*
            String schemaName = cube.getSchema().getName();
            String cubeName = cube.getName();
            String hierarchyName = hierarchy.getName();

            String desc = hierarchy.getDescription();
            if (desc == null) {
                //TODO: currently this is always null
                desc = schemaName +
                    " - " +
                    cubeName +
                    " Cube - " +
                    hierarchyName +
                    " Hierarchy";
            }

            if (hierarchy.hasAll()) {
                String tableName = cubeName +
                    ':' + hierarchyName + ':' + "(All)";

                Row row = new Row();
                row.set(TableCatalog.name, schemaName);
                row.set(TableName.name, tableName);
                row.set(TableType.name, "SYSTEM TABLE");
                row.set(Description.name, desc);
                row.set(DateModified.name, dateModified);
                addRow(row, rows);
            }
*/
            for (Level level : hierarchy.getLevels()) {
                populateLevel(cube, hierarchy, level, rows);
            }
        }

        private void populateLevel(
            Cube cube,
            Hierarchy hierarchy,
            Level level,
            List<Row> rows)
        {
            String schemaName = cube.getSchema().getName();
            String cubeName = cube.getName();
            String hierarchyName = getHierarchyName(hierarchy);
            String levelName = level.getName();

            String tableName =
                cubeName + ':' + hierarchyName + ':' + levelName;

            String desc = level.getDescription();
            if (desc == null) {
                //TODO: currently this is always null
                desc =
                    new StringBuilder(schemaName).append(" - ")
                        .append(cubeName).append(" Cube - ")
                        .append(hierarchyName).append(" Hierarchy - ")
                        .append(levelName).append(" Level").toString();
            }

            Row row = new Row();
            row.set(TableCatalog.name, schemaName);
            row.set(TableName.name, tableName);
            row.set(TableType.name, "SYSTEM TABLE");
            row.set(Description.name, desc);
            if (false) {
                row.set(DateModified.name, DATE_MODIFIED);
            }
            addRow(row, rows);
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class DbschemaSourceTablesRowset extends Rowset {
        DbschemaSourceTablesRowset(XmlaRequest request, XmlaHandler handler) {
            super(DBSCHEMA_SOURCE_TABLES, request, handler);
        }

        private static final Column TableCatalog =
                new Column(
                        "TABLE_CATALOG",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "Catalog name. NULL if the provider does not support "
                                + "catalogs.");
        private static final Column TableSchema =
                new Column(
                        "TABLE_SCHEMA",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "Unqualified schema name. NULL if the provider does not "
                                + "support schemas.");
        private static final Column TableName =
                new Column(
                        "TABLE_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.REQUIRED,
                        "Table name.");
        private static final Column TableType =
                new Column(
                        "TABLE_TYPE",
                        Type.STRING_SOMETIMES_ARRAY,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.REQUIRED,
                        "Table type. One of the following or a provider-specific "
                                + "value: ALIAS, TABLE, SYNONYM, SYSTEM TABLE, VIEW, GLOBAL "
                                + "TEMPORARY, LOCAL TEMPORARY, EXTERNAL TABLE, SYSTEM VIEW");

        @Override
		public void populateImpl(
                XmlaResponse response,
                OlapConnection connection,
                List<Row> rows)
                throws XmlaException, SQLException
        {
            mondrian.rolap.RolapConnection rolapConnection =
                    ((mondrian.olap4j.MondrianOlap4jConnection)connection).getMondrianConnection();
            try (java.sql.Connection sqlConnection = rolapConnection.getDataSource().getConnection()) {

                java.sql.DatabaseMetaData databaseMetaData = sqlConnection.getMetaData();
                String[] tableTypeRestriction = null;
                List<String> tableTypeRestrictionList = getRestriction(TableType);
                if (tableTypeRestrictionList != null) {
                    tableTypeRestriction = tableTypeRestrictionList.toArray(new String[0]);
                }
                java.sql.ResultSet resultSet = databaseMetaData.getTables(null, null, null, tableTypeRestriction);
//            java.sql.ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
                while (resultSet.next()) {

                    final String tableCatalog = resultSet.getString("TABLE_CAT");
                    final String tableSchema = resultSet.getString("TABLE_SCHEM");
                    final String tableName = resultSet.getString("TABLE_NAME");
                    final String tableType = resultSet.getString("TABLE_TYPE");

                    Row row = new Row();
                    row.set(TableCatalog.name, tableCatalog);
                    row.set(TableSchema.name, tableSchema);
                    row.set(TableName.name, tableName);
                    row.set(TableType.name, tableType);
                    addRow(row, rows);
                }
            }
        }

        @Override
		protected void setProperty(
                PropertyDefinition propertyDef,
                String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                    super.setProperty(propertyDef, value);
            }
        }
    }


    // TODO: Is this needed????
    static class DbschemaTablesInfoRowset extends Rowset {
        DbschemaTablesInfoRowset(XmlaRequest request, XmlaHandler handler) {
            super(DBSCHEMA_TABLES_INFO, request, handler);
        }

        private static final Column TableCatalog =
            new Column(
                "TABLE_CATALOG",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "Catalog name. NULL if the provider does not support "
                + "catalogs.");
        private static final Column TableSchema =
            new Column(
                "TABLE_SCHEMA",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "Unqualified schema name. NULL if the provider does not "
                + "support schemas.");
        private static final Column TableName =
            new Column(
                "TABLE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Table name.");
        private static final Column TableType =
            new Column(
                "TABLE_TYPE",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Table type. One of the following or a provider-specific "
                + "value: ALIAS, TABLE, SYNONYM, SYSTEM TABLE, VIEW, GLOBAL "
                + "TEMPORARY, LOCAL TEMPORARY, EXTERNAL TABLE, SYSTEM VIEW");
        private static final Column TableGuid =
            new Column(
                "TABLE_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "GUID that uniquely identifies the table. Providers that do "
                + "not use GUIDs to identify tables should return NULL in this "
                + "column.");

        private static final Column Bookmarks =
            new Column(
                "BOOKMARKS",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Whether this table supports bookmarks. Allways is false.");
        private static final Column BookmarkType =
            new Column(
                "BOOKMARK_TYPE",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Default bookmark type supported on this table.");
        private static final Column BookmarkDataType =
            new Column(
                "BOOKMARK_DATATYPE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The indicator of the bookmark's native data type.");
        private static final Column BookmarkMaximumLength =
            new Column(
                "BOOKMARK_MAXIMUM_LENGTH",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Maximum length of the bookmark in bytes.");
        private static final Column BookmarkInformation =
            new Column(
                "BOOKMARK_INFORMATION",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A bitmask specifying additional information about bookmarks "
                + "over the rowset. ");
        private static final Column TableVersion =
            new Column(
                "TABLE_VERSION",
                Type.LONG,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Version number for this table or NULL if the provider does "
                + "not support returning table version information.");
        private static final Column Cardinality =
            new Column(
                "CARDINALITY",
                Type.UNSIGNED_LONG,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Cardinality (number of rows) of the table.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Human-readable description of the table.");
        private static final Column TablePropId =
            new Column(
                "TABLE_PROPID",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Property ID of the table. Return null.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, OlapException
        {
            for (Catalog catalog : catIter(connection, catNameCond())) {
                // By definition, mondrian catalogs have only one
                // schema. It is safe to use get(0)
                final Schema schema = catalog.getSchemas().get(0);
                //TODO: Is this cubes or tables? SQL Server returns what
                // in foodmart are cube names for TABLE_NAME
                for (Cube cube : sortedCubes(schema)) {
                    String cubeName = cube.getName();
                    String desc = cube.getDescription();
                    if (desc == null) {
                        //TODO: currently this is always null
                        desc = catalog.getName() + " - " + cubeName + " Cube";
                    }
                    //TODO: SQL Server returns 1000000 for all tables
                    int cardinality = 1000000;
                    String version = "null";

                    Row row = new Row();
                    row.set(TableCatalog.name, catalog.getName());
                    row.set(TableName.name, cubeName);
                    row.set(TableType.name, "TABLE");
                    row.set(Bookmarks.name, false);
                    row.set(TableVersion.name, version);
                    row.set(Cardinality.name, cardinality);
                    row.set(Description.name, desc);
                    addRow(row, rows);
                }
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class MdschemaActionsRowset extends Rowset {
        MdschemaActionsRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_ACTIONS, request, handler);
        }

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the catalog to which this action belongs.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the schema to which this action belongs.");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the cube to which this action belongs.");
        private static final Column ActionName =
            new Column(
                "ACTION_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the action.");
        private static final Column Coordinate =
            new Column(
                "COORDINATE",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                null);
        private static final Column CoordinateType =
            new Column(
                "COORDINATE_TYPE",
                Type.INTEGER,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                null);
        /*
            TODO: optional columns
        ACTION_TYPE
        INVOCATION
        CUBE_SOURCE
    */

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException
        {
            // mondrian doesn't support actions. It's not an error to ask for
            // them, there just aren't any
        }
    }

    public static class MdschemaCubesRowset extends Rowset {
        private final Predicate<Catalog> catalogNameCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;

        MdschemaCubesRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_CUBES, request, handler);
            catalogNameCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
        }

        public static final String MD_CUBTYPE_CUBE = "CUBE";
        public static final String MD_CUBTYPE_VIRTUAL_CUBE = "VIRTUAL CUBE";

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the catalog to which this cube belongs.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the schema to which this cube belongs.");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Name of the cube.");
        private static final Column CubeType =
            new Column(
                "CUBE_TYPE",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Cube type.");
        private static final Column BaseCubeName =
            new Column(
                "BASE_CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the source cube if this cube is a perspective cube.");
        private static final Column CubeGuid =
            new Column(
                "CUBE_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Cube type.");
        private static final Column CreatedOn =
            new Column(
                "CREATED_ON",
                Type.DATE_TIME,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Date and time of cube creation.");
        private static final Column LastSchemaUpdate =
            new Column(
                "LAST_SCHEMA_UPDATE",
                Type.DATE_TIME,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Date and time of last schema update.");
        private static final Column SchemaUpdatedBy =
            new Column(
                "SCHEMA_UPDATED_BY",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "User ID of the person who last updated the schema.");
        private static final Column LastDataUpdate =
            new Column(
                "LAST_DATA_UPDATE",
                Type.DATE_TIME,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Date and time of last data update.");
        private static final Column DataUpdatedBy =
            new Column(
                "DATA_UPDATED_BY",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "User ID of the person who last updated the data.");
        private static final Column IsDrillthroughEnabled =
            new Column(
                "IS_DRILLTHROUGH_ENABLED",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Describes whether DRILLTHROUGH can be performed on the "
                + "members of a cube");
        private static final Column IsWriteEnabled =
            new Column(
                "IS_WRITE_ENABLED",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Describes whether a cube is write-enabled");
        private static final Column IsLinkable =
            new Column(
                "IS_LINKABLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Describes whether a cube can be used in a linked cube");
        private static final Column IsSqlEnabled =
            new Column(
                "IS_SQL_ENABLED",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Describes whether or not SQL can be used on the cube");
        private static final Column CubeCaption =
            new Column(
                "CUBE_CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The caption of the cube.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A user-friendly description of the dimension.");
        private static final Column Dimensions =
            new Column(
                "DIMENSIONS",
                Type.ROW_SET,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Dimensions in this cube.");
        private static final Column Sets =
            new Column(
                "SETS",
                Type.ROW_SET,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Sets in this cube.");
        private static final Column Measures =
            new Column(
                "MEASURES",
                Type.ROW_SET,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Measures in this cube.");
        private static final Column CubeSource =
            new Column(
                "CUBE_SOURCE",
                Type.INTEGER,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bitmap with one of these valid values:\n" +
                        "\n" +
                        "1 CUBE\n" +
                        "\n" +
                        "2 DIMENSION");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogNameCond))
            {
                for (Schema schema
                    : filter(catalog.getSchemas(), schemaNameCond))
                {
                    for (Cube cube : filter(sortedCubes(schema), cubeNameCond))
                    {
                        //Could also process  "Show Hidden Cubes" connection string setting
                        if(cube.isVisible()) {
                            String desc = cube.getDescription();
                            if (desc == null) {
                                desc =
                                        catalog.getName() + " Schema - "
                                                + cube.getName() + " Cube";
                            }

                            Row row = new Row();
                            row.set(CatalogName.name, catalog.getName());
                            row.set(SchemaName.name, schema.getName());
                            row.set(CubeName.name, cube.getName());
                            final XmlaHandler.XmlaExtra extra =
                                    getExtra(connection);
                            row.set(CubeType.name, extra.getCubeType(cube));
                            //row.set(CubeGuid.name, "");
                            //row.set(CreatedOn.name, "");
                            //row.set(SchemaUpdatedBy.name, "");
                            //row.set(DataUpdatedBy.name, "");
                            row.set(IsDrillthroughEnabled.name, true);
                            row.set(IsWriteEnabled.name, false);
                            row.set(IsLinkable.name, false);
                            row.set(IsSqlEnabled.name, false);
                            row.set(CubeCaption.name, cube.getCaption());
                            row.set(Description.name, desc);
                            row.set(CubeSource.name, 1);
                            Format formatter =
                                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            String formattedDate =
                                    formatter.format(
                                            extra.getSchemaLoadDate(schema));
                            row.set(LastSchemaUpdate.name, formattedDate);
                            //It could be a last source update. Now it's just current time.
                            formattedDate =
                                    formatter.format(new Date());
                            row.set(LastDataUpdate.name, formattedDate);
                            if (deep) {
                                row.set(
                                        Dimensions.name,
                                        new MdschemaDimensionsRowset(
                                                wrapRequest(
                                                        request,
                                                        Olap4jUtil.mapOf(
                                                                MdschemaDimensionsRowset
                                                                        .CatalogName,
                                                                catalog.getName(),
                                                                MdschemaDimensionsRowset.SchemaName,
                                                                schema.getName(),
                                                                MdschemaDimensionsRowset.CubeName,
                                                                cube.getName())),
                                                handler));
                                row.set(
                                        Sets.name,
                                        new MdschemaSetsRowset(
                                                wrapRequest(
                                                        request,
                                                        Olap4jUtil.mapOf(
                                                                MdschemaSetsRowset.CatalogName,
                                                                catalog.getName(),
                                                                MdschemaSetsRowset.SchemaName,
                                                                schema.getName(),
                                                                MdschemaSetsRowset.CubeName,
                                                                cube.getName())),
                                                handler));
                                row.set(
                                        Measures.name,
                                        new MdschemaMeasuresRowset(
                                                wrapRequest(
                                                        request,
                                                        Olap4jUtil.mapOf(
                                                                MdschemaMeasuresRowset.CatalogName,
                                                                catalog.getName(),
                                                                MdschemaMeasuresRowset.SchemaName,
                                                                schema.getName(),
                                                                MdschemaMeasuresRowset.CubeName,
                                                                cube.getName())),
                                                handler));
                            }
                            addRow(row, rows);
                        }
                    }
                }
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class MdschemaDimensionsRowset extends Rowset {
        private final Predicate<Catalog> catalogNameCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<Dimension> dimensionUnameCond;
        private final Predicate<Dimension> dimensionNameCond;

        MdschemaDimensionsRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_DIMENSIONS, request, handler);
            catalogNameCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            dimensionUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, DimensionUniqueName);
            dimensionNameCond =
                makeCondition(ELEMENT_NAME_GETTER, DimensionName);
        }

        public static final int MD_DIMTYPE_OTHER = 3;
        public static final int MD_DIMTYPE_MEASURE = 2;
        public static final int MD_DIMTYPE_TIME = 1;

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the database.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "Not supported.");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the cube.");
        private static final Column DimensionName =
            new Column(
                "DIMENSION_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the dimension.");
        private static final Column DimensionUniqueName =
            new Column(
                "DIMENSION_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The unique name of the dimension.");
        private static final Column DimensionGuid =
            new Column(
                "DIMENSION_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Not supported.");
        private static final Column DimensionCaption =
            new Column(
                "DIMENSION_CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The caption of the dimension.");
        private static final Column DimensionOrdinal =
            new Column(
                "DIMENSION_ORDINAL",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The position of the dimension within the cube.");
        /*
         * SQL Server returns values:
         *   MD_DIMTYPE_TIME (1)
         *   MD_DIMTYPE_MEASURE (2)
         *   MD_DIMTYPE_OTHER (3)
         */
        private static final Column DimensionType =
            new Column(
                "DIMENSION_TYPE",
                Type.SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The type of the dimension.");
        private static final Column DimensionCardinality =
            new Column(
                "DIMENSION_CARDINALITY",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The number of members in the key attribute.");
        private static final Column DefaultHierarchy =
            new Column(
                "DEFAULT_HIERARCHY",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A hierarchy from the dimension. Preserved for backwards "
                + "compatibility.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A user-friendly description of the dimension.");
        private static final Column IsVirtual =
            new Column(
                "IS_VIRTUAL",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Always FALSE.");
        private static final Column IsReadWrite =
            new Column(
                "IS_READWRITE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A Boolean that indicates whether the dimension is "
                + "write-enabled.");
        /*
         * SQL Server returns values: 0 or 1
         */
        private static final Column DimensionUniqueSettings =
            new Column(
                "DIMENSION_UNIQUE_SETTINGS",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A bitmap that specifies which columns contain unique values "
                + "if the dimension contains only members with unique names.");
        private static final Column DimensionMasterUniqueName =
            new Column(
                "DIMENSION_MASTER_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Always NULL.");
        private static final Column DimensionIsVisible =
            new Column(
                "DIMENSION_IS_VISIBLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Always TRUE.");
        private static final Column Hierarchies =
            new Column(
                "HIERARCHIES",
                Type.ROW_SET,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Hierarchies in this dimension.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogNameCond))
            {
                populateCatalog(connection, catalog, rows);
            }
        }

        protected void populateCatalog(
            OlapConnection connection,
            Catalog catalog,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    populateCube(connection, catalog, cube, rows);
                }
            }
        }

        protected void populateCube(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Dimension dimension
                : filter(
                    cube.getDimensions(),
                    dimensionNameCond,
                    dimensionUnameCond))
            {
                populateDimension(
                    connection, catalog, cube, dimension, rows);
            }
        }

        protected void populateDimension(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Dimension dimension,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            String desc = dimension.getDescription();
            if (desc == null) {
                desc =
                    cube.getName() + " Cube - "
                    + dimension.getName() + " Dimension";
            }

            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(DimensionName.name, dimension.getName());
            row.set(DimensionUniqueName.name, dimension.getUniqueName());
            row.set(DimensionCaption.name, dimension.getCaption());
            row.set(
                DimensionOrdinal.name, cube.getDimensions().indexOf(dimension));
            row.set(DimensionType.name, getDimensionType(dimension));

            //Is this the number of primaryKey members there are??
            // According to microsoft this is:
            //    "The number of members in the key attribute."
            // There may be a better way of doing this but
            // this is what I came up with. Note that I need to
            // add '1' to the number inorder for it to match
            // match what microsoft SQL Server is producing.
            // The '1' might have to do with whether or not the
            // hierarchy has a 'all' member or not - don't know yet.
            // large data set total for Orders cube 0m42.923s
            Hierarchy firstHierarchy = dimension.getHierarchies().get(0);
            NamedList<Level> levels = firstHierarchy.getLevels();
            Level lastLevel = levels.get(levels.size() - 1);

            /*
            if override config setting is set
                if approxRowCount has a value
                    use it
            else
                                    do default
            */

            // Added by TWI to returned cached row numbers

            int n = getExtra(connection).getLevelCardinality(lastLevel);
            row.set(DimensionCardinality.name, n + 1);

            row.set(DefaultHierarchy.name, firstHierarchy.getUniqueName());
            row.set(Description.name, desc);
            row.set(IsVirtual.name, false);
            // SQL Server always returns false
            row.set(IsReadWrite.name, false);
            // TODO: don't know what to do here
            // Are these the levels with uniqueMembers == true?
            // How are they mapped to specific column numbers?
            row.set(DimensionUniqueSettings.name, 0);
            row.set(DimensionIsVisible.name, dimension.isVisible());
            if (deep) {
                row.set(
                    Hierarchies.name,
                    new MdschemaHierarchiesRowset(
                        wrapRequest(
                            request,
                            Olap4jUtil.mapOf(
                                MdschemaHierarchiesRowset.CatalogName,
                                catalog.getName(),
                                MdschemaHierarchiesRowset.SchemaName,
                                cube.getSchema().getName(),
                                MdschemaHierarchiesRowset.CubeName,
                                cube.getName(),
                                MdschemaHierarchiesRowset.DimensionUniqueName,
                                dimension.getUniqueName())),
                        handler));
            }

            addRow(row, rows);
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static int getDimensionType(Dimension dim) throws OlapException {
        switch (dim.getDimensionType()) {
        case MEASURE:
            return MdschemaDimensionsRowset.MD_DIMTYPE_MEASURE;
        case TIME:
            return MdschemaDimensionsRowset.MD_DIMTYPE_TIME;
        default:
            return MdschemaDimensionsRowset.MD_DIMTYPE_OTHER;
        }
    }

    static class MdschemaMeasuregroupDimensionsRowset extends Rowset {
        private final Predicate<Catalog> catalogNameCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<Dimension> dimensionUnameCond;

        MdschemaMeasuregroupDimensionsRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_MEASUREGROUP_DIMENSIONS, request, handler);
            catalogNameCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            dimensionUnameCond =
                    makeCondition(ELEMENT_UNAME_GETTER, DimensionUniqueName);
        }

        private static final Column CatalogName =
                new Column(
                        "CATALOG_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "The name of the database.");
        private static final Column SchemaName =
                new Column(
                        "SCHEMA_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "Not supported.");
        private static final Column CubeName =
                new Column(
                        "CUBE_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.REQUIRED,
                        "The name of the cube.");
        private static final Column MeasuregroupName =
                new Column(
                        "MEASUREGROUP_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "The name of the measure group.");
        private static final Column MeasuregroupCardinality =
                new Column(
                        "MEASUREGROUP_CARDINALITY",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "The number of instances a measure in the measure group can have for a single dimension member." +
                                " Possible values include:\n" +
                                "ONE\n" +
                                "MANY");
        private static final Column DimensionUniqueName =
                new Column(
                        "DIMENSION_UNIQUE_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "The unique name of the dimension.");
        private static final Column DimensionCardinality =
                new Column(
                        "DIMENSION_CARDINALITY",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "The number of instances a dimension member can have for a single instance of a measure group measure.\n" +
                                "Possible values include:\n" +
                                "ONE\n" +
                                "MANY");
        private static final Column DimensionIsVisible =
                new Column(
                        "DIMENSION_IS_VISIBLE",
                        Type.BOOLEAN,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "A Boolean that indicates whether hieararchies in the dimension are visible.\n" +
                        "Returns TRUE if one or more hierarchies in the dimension is visible; otherwise, FALSE.");
        private static final Column DimensionIsFactDimension =
                new Column(
                        "DIMENSION_IS_FACT_DIMENSION",
                        Type.BOOLEAN,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "");
        private static final Column DimensionPath =
                new Column(
                        "DIMENSION_PATH",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "A list of dimensions for the reference dimension.");
        private static final Column DimensionGranularity =
                new Column(
                        "DIMENSION_GRANULARITY",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "The unique name of the granularity hierarchy.");

        @Override
		public void populateImpl(
                XmlaResponse response,
                OlapConnection connection,
                List<Row> rows)
                throws XmlaException, SQLException
        {
            for (Catalog catalog
                    : catIter(connection, catNameCond(), catalogNameCond))
            {
                populateCatalog(connection, catalog, rows);
            }
        }

        protected void populateCatalog(
                OlapConnection connection,
                Catalog catalog,
                List<Row> rows)
                throws XmlaException, SQLException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    populateCube(connection, catalog, cube, rows);
                }
            }
        }

        protected void populateCube(
                OlapConnection connection,
                Catalog catalog,
                Cube cube,
                List<Row> rows)
                throws XmlaException
        {
            for (Dimension dimension
                    : filter(
                    cube.getDimensions(),
                    dimensionUnameCond))
            {
                populateMeasuregroupDimension(
                        connection, catalog, cube, dimension, rows);
            }
        }

        protected void populateMeasuregroupDimension(
                OlapConnection connection,
                Catalog catalog,
                Cube cube,
                Dimension dimension,
                List<Row> rows)
                throws XmlaException
        {
            String desc = dimension.getDescription();
            if (desc == null) {
                desc =
                        cube.getName() + " Cube - "
                                + dimension.getName() + " Dimension";
            }

            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(MeasuregroupName.name, cube.getName());
            row.set(MeasuregroupCardinality.name, "ONE");
            row.set(DimensionUniqueName.name, dimension.getUniqueName());
            row.set(DimensionCardinality.name, "MANY");
            row.set(DimensionIsVisible.name, dimension.isVisible());
            row.set(DimensionIsFactDimension.name, "0");
            row.set(DimensionPath.name, "");
            row.set(DimensionGranularity.name, "");

            addRow(row, rows);
        }

        @Override
		protected void setProperty(
                PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    public static class MdschemaFunctionsRowset extends Rowset {
        /**
         * http://www.csidata.com/custserv/onlinehelp/VBSdocs/vbs57.htm
         */
        public enum VarType {
            Empty("Uninitialized (default)"),
            Null("Contains no valid data"),
            Integer("Integer subtype"),
            Long("Long subtype"),
            Single("Single subtype"),
            Double("Double subtype"),
            Currency("Currency subtype"),
            Date("Date subtype"),
            String("String subtype"),
            Object("Object subtype"),
            Error("Error subtype"),
            Boolean("Boolean subtype"),
            Variant("Variant subtype"),
            DataObject("DataObject subtype"),
            Decimal("Decimal subtype"),
            Byte("Byte subtype"),
            Array("Array subtype");

            public static VarType forCategory(int category) {
                switch (category) {
                case Category.UNKNOWN:
                    // expression == unknown ???
                    // case Category.Expression:
                    return Empty;
                case Category.ARRAY:
                    return Array;
                case Category.DIMENSION,
                Category.HIERARCHY,
                Category.LEVEL,
                Category.MEMBER,
                Category.SET,
                Category.TUPLE,
                Category.CUBE,
                Category.VALUE:
                    return Variant;
                case Category.LOGICAL:
                    return Boolean;
                case Category.NUMERIC:
                    return Double;
                case Category.STRING, Category.SYMBOL, Category.CONSTANT:
                    return String;
                case Category.DATE_TIME:
                    return Date;
                case Category.INTEGER, Category.MASK:
                    return Integer;
                default:
                    break;
                }
                // NOTE: this should never happen
                return Empty;
            }

            VarType(String description) {
                discard(description);
            }
        }

        private final Predicate<String> functionNameCond;

        MdschemaFunctionsRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_FUNCTIONS, request, handler);
            functionNameCond = makeCondition(FunctionName);
        }

        private static final Column FunctionName =
            new Column(
                "FUNCTION_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the function.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A description of the function.");
        private static final Column ParameterList =
            new Column(
                "PARAMETER_LIST",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A comma delimited list of parameters.");
        private static final Column ReturnType =
            new Column(
                "RETURN_TYPE",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The VARTYPE of the return data type of the function.");
        private static final Column Origin =
            new Column(
                "ORIGIN",
                Type.INTEGER,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The origin of the function:  1 for MDX functions.  2 for "
                + "user-defined functions.");
        private static final Column InterfaceName =
            new Column(
                "INTERFACE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the interface for user-defined functions");
        private static final Column LibraryName =
            new Column(
                "LIBRARY_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the type library for user-defined functions. "
                + "NULL for MDX functions.");
        private static final Column Caption =
            new Column(
                "CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The display caption for the function.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            final XmlaHandler.XmlaExtra extra = getExtra(connection);
            for (Catalog catalog : catIter(connection, catNameCond())) {
                // By definition, mondrian catalogs have only one
                // schema. It is safe to use get(0)
                final Schema schema = catalog.getSchemas().get(0);
                List<XmlaHandler.XmlaExtra.FunctionDefinition> funDefs =
                    new ArrayList<>();

                // olap4j does not support describing functions. Call an
                // auxiliary method.
                extra.getSchemaFunctionList(
                    funDefs,
                    schema,
                    functionNameCond);
                for (XmlaHandler.XmlaExtra.FunctionDefinition funDef : funDefs)
                {
                    Row row = new Row();
                    row.set(FunctionName.name, funDef.functionName);
                    row.set(Description.name, funDef.description);
                    row.set(ParameterList.name, funDef.parameterList);
                    row.set(ReturnType.name, funDef.returnType);
                    row.set(Origin.name, funDef.origin);
                    //row.set(LibraryName.name, "");
                    row.set(InterfaceName.name, funDef.interfaceName);
                    row.set(Caption.name, funDef.caption);
                    addRow(row, rows);
                }
            }
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class MdschemaHierarchiesRowset extends Rowset {
        private final Predicate<Catalog> catalogCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<Dimension> dimensionUnameCond;
        private final Predicate<Hierarchy> hierarchyUnameCond;
        private final Predicate<Hierarchy> hierarchyNameCond;

        MdschemaHierarchiesRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_HIERARCHIES, request, handler);
            catalogCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            dimensionUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, DimensionUniqueName);
            hierarchyUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, HierarchyUniqueName);
            hierarchyNameCond =
                makeCondition(ELEMENT_NAME_GETTER, HierarchyName);
        }

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the catalog to which this hierarchy belongs.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "Not supported");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the cube to which this hierarchy belongs.");
        private static final Column DimensionUniqueName =
            new Column(
                "DIMENSION_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The unique name of the dimension to which this hierarchy "
                + "belongs.");
        private static final Column HierarchyName =
            new Column(
                "HIERARCHY_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the hierarchy. Blank if there is only a single "
                + "hierarchy in the dimension.");
        private static final Column HierarchyUniqueName =
            new Column(
                "HIERARCHY_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The unique name of the hierarchy.");

        private static final Column HierarchyGuid =
            new Column(
                "HIERARCHY_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Hierarchy GUID.");

        private static final Column HierarchyCaption =
            new Column(
                "HIERARCHY_CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A label or a caption associated with the hierarchy.");
        private static final Column DimensionType =
            new Column(
                "DIMENSION_TYPE",
                Type.SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The type of the dimension.");
        private static final Column HierarchyCardinality =
            new Column(
                "HIERARCHY_CARDINALITY",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The number of members in the hierarchy.");
        private static final Column DefaultMember =
            new Column(
                "DEFAULT_MEMBER",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The default member for this hierarchy.");
        private static final Column AllMember =
            new Column(
                "ALL_MEMBER",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The member at the highest level of rollup in the hierarchy.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A human-readable description of the hierarchy. NULL if no "
                + "description exists.");
        private static final Column Structure =
            new Column(
                "STRUCTURE",
                Type.SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The structure of the hierarchy.");
        private static final Column IsVirtual =
            new Column(
                "IS_VIRTUAL",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Always returns False.");
        private static final Column IsReadWrite =
            new Column(
                "IS_READWRITE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A Boolean that indicates whether the Write Back to dimension "
                + "column is enabled.");
        private static final Column DimensionUniqueSettings =
            new Column(
                "DIMENSION_UNIQUE_SETTINGS",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Always returns MDDIMENSIONS_MEMBER_KEY_UNIQUE (1).");
        private static final Column DimensionIsVisible =
            new Column(
                "DIMENSION_IS_VISIBLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A Boolean that indicates whether the parent dimension is visible.");
        private static final Column HierarchyIsVisibile =
            new Column(
                "HIERARCHY_IS_VISIBLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A Boolean that indicates whether the hieararchy is visible.");
        private static final Column HierarchyOrigin =
            new Column(
                "HIERARCHY_ORIGIN",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bit mask that determines the source of the hierarchy:\n" +
                        "MD_ORIGIN_USER_DEFINED identifies levels in a user defined hierarchy (0x0000001).\n" +
                        "MD_ORIGIN_ATTRIBUTE identifies levels in an attribute hierarchy (0x0000002).\n" +
                        "MD_ORIGIN_INTERNAL identifies levels in attribute hierarchies that are not enabled (0x0000004).\n" +
                        "MD_ORIGIN_KEY_ATTRIBUTE identifies levels in a key attribute hierarchy (0x0000008).\n");
        private static final Column DisplayFolder =
            new Column(
                "HIERARCHY_DISPLAY_FOLDER",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The path to be used when displaying the hierarchy in the user interface. Folder names will be separated by a semicolon (;). Nested folders are indicated by a backslash (\\).");
        private static final Column CubeSource =
            new Column(
                "CUBE_SOURCE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bitmap with one of the following valid values:\n" +
                        "1 CUBE\n" +
                        "2 DIMENSION\n" +
                        "Default restriction is a value of 1.");
        private static final Column HierarchyVisibility =
            new Column(
                "HIERARCHY_VISIBILITY",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bitmap with one of the following valid values: 1 Visible, 2 Not visible.");
        private static final Column HierarchyOrdinal =
            new Column(
                "HIERARCHY_ORDINAL",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The ordinal number of the hierarchy across all hierarchies of "
                + "the cube.");
        private static final Column DimensionIsShared =
            new Column(
                "DIMENSION_IS_SHARED",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Always returns true.");
        private static final Column Levels =
            new Column(
                "LEVELS",
                Type.ROW_SET,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Levels in this hierarchy.");


        /*
         * NOTE: This is non-standard, where did it come from?
         */
        private static final Column ParentChild =
            new Column(
                "PARENT_CHILD",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Is hierarchy a parent.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogCond))
            {
                populateCatalog(connection, catalog, rows);
            }
        }

        protected void populateCatalog(
            OlapConnection connection,
            Catalog catalog,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    populateCube(connection, catalog, cube, rows);
                }
            }
        }

        protected void populateCube(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            int ordinal = 0;
            for (Dimension dimension : cube.getDimensions()) {
                // Must increment ordinal for all dimensions but
                // only output some of them.
                boolean genOutput = dimensionUnameCond.test(dimension);
                if (genOutput) {
                    populateDimension(
                        connection, catalog, cube, dimension, ordinal, rows);
                }
                ordinal += dimension.getHierarchies().size();
            }
        }

        protected void populateDimension(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Dimension dimension,
            int ordinal,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            final NamedList<Hierarchy> hierarchies = dimension.getHierarchies();
            for (Hierarchy hierarchy
                : filter(hierarchies, hierarchyNameCond, hierarchyUnameCond))
            {
                populateHierarchy(
                    connection,
                    catalog,
                    cube,
                    dimension,
                    hierarchy,
                    ordinal + hierarchies.indexOf(hierarchy),
                    rows);
            }
        }

        protected void populateHierarchy(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Dimension dimension,
            Hierarchy hierarchy,
            int ordinal,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            final XmlaHandler.XmlaExtra extra = getExtra(connection);
            String desc = hierarchy.getDescription();
            if (desc == null) {
                desc =
                    cube.getName() + " Cube - "
                    + getHierarchyName(hierarchy) + " Hierarchy";
            }

            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(DimensionUniqueName.name, dimension.getUniqueName());
            row.set(HierarchyName.name, hierarchy.getName());
            row.set(HierarchyUniqueName.name, hierarchy.getUniqueName());
            //row.set(HierarchyGuid.name, "");

            row.set(HierarchyCaption.name, hierarchy.getCaption());
            row.set(DimensionType.name, getDimensionType(dimension));
            // The number of members in the hierarchy. Because
            // of the presence of multiple hierarchies, this number
            // might not be the same as DIMENSION_CARDINALITY. This
            // value can be an approximation of the real
            // cardinality. Consumers should not assume that this
            // value is accurate.
            int cardinality = extra.getHierarchyCardinality(hierarchy);
            row.set(HierarchyCardinality.name, cardinality);

            row.set(
                DefaultMember.name,
                hierarchy.getDefaultMember().getUniqueName());
            if (hierarchy.hasAll()) {
                row.set(
                    AllMember.name,
                    hierarchy.getRootMembers().get(0).getUniqueName());
            }
            row.set(Description.name, desc);

            //TODO: only support:
            // MD_STRUCTURE_FULLYBALANCED (0)
            // MD_STRUCTURE_RAGGEDBALANCED (1)
            row.set(Structure.name, extra.getHierarchyStructure(hierarchy));

            row.set(IsVirtual.name, false);
            row.set(IsReadWrite.name, false);

            // NOTE that SQL Server returns '0' not '1'.
            row.set(DimensionUniqueSettings.name, 0);

            row.set(DimensionIsVisible.name, dimension.isVisible());

            row.set(HierarchyIsVisibile.name, hierarchy.isVisible());
            row.set(HierarchyVisibility.name, hierarchy.isVisible()?1:2);

            mondrian.olap4j.MondrianOlap4jHierarchy mondrianOlap4jHierarchy =
                    (mondrian.olap4j.MondrianOlap4jHierarchy)hierarchy;

            // Bitmask
            // MD_ORIGIN_USER_DEFINED 0x00000001
            // MD_ORIGIN_ATTRIBUTE 0x00000002
            // MD_ORIGIN_KEY_ATTRIBUTE 0x00000004
            // MD_ORIGIN_INTERNAL 0x00000008
            int hierarchyOrigin;
            if(dimension.getUniqueName().equals(org.eclipse.daanse.olap.api.model.Dimension.MEASURES_UNIQUE_NAME)){
                hierarchyOrigin = 6;
            }
            else {
                RolapHierarchy rolapHierarchy = (RolapHierarchy)mondrianOlap4jHierarchy.getHierarchy();
                org.eclipse.daanse.olap.rolap.dbmapper.model.api.Hierarchy xmlHierarchy = rolapHierarchy.getXmlHierarchy();
                try {
                    hierarchyOrigin = Integer.parseInt(xmlHierarchy.origin());
                }
                catch (NumberFormatException e) {
                    hierarchyOrigin =  1;
                }
            }
            row.set(HierarchyOrigin.name, hierarchyOrigin);

            row.set(HierarchyOrdinal.name, ordinal);


            String displayFolder = mondrianOlap4jHierarchy.getDisplayFolder();
            if(displayFolder == null) { displayFolder = ""; }
            row.set(DisplayFolder.name, displayFolder);

            // always true
            row.set(DimensionIsShared.name, true);

            row.set(ParentChild.name, extra.isHierarchyParentChild(hierarchy));
            if (deep) {
                row.set(
                    Levels.name,
                    new MdschemaLevelsRowset(
                        wrapRequest(
                            request,
                            Olap4jUtil.mapOf(
                                MdschemaLevelsRowset.CatalogName,
                                catalog.getName(),
                                MdschemaLevelsRowset.SchemaName,
                                cube.getSchema().getName(),
                                MdschemaLevelsRowset.CubeName,
                                cube.getName(),
                                MdschemaLevelsRowset.DimensionUniqueName,
                                dimension.getUniqueName(),
                                MdschemaLevelsRowset.HierarchyUniqueName,
                                hierarchy.getUniqueName())),
                        handler));
            }
            addRow(row, rows);
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class MdschemaLevelsRowset extends Rowset {
        private final Predicate<Catalog> catalogCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<Dimension> dimensionUnameCond;
        private final Predicate<Hierarchy> hierarchyUnameCond;
        private final Predicate<Level> levelUnameCond;
        private final Predicate<Level> levelNameCond;

        MdschemaLevelsRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_LEVELS, request, handler);
            catalogCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            dimensionUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, DimensionUniqueName);
            hierarchyUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, HierarchyUniqueName);
            levelUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, LevelUniqueName);
            levelNameCond = makeCondition(ELEMENT_NAME_GETTER, LevelName);
        }

        public static final int MDLEVEL_TYPE_UNKNOWN = 0x0000;
        public static final int MDLEVEL_TYPE_REGULAR = 0x0000;
        public static final int MDLEVEL_TYPE_ALL = 0x0001;
        public static final int MDLEVEL_TYPE_CALCULATED = 0x0002;
        public static final int MDLEVEL_TYPE_TIME = 0x0004;
        public static final int MDLEVEL_TYPE_RESERVED1 = 0x0008;
        public static final int MDLEVEL_TYPE_TIME_YEARS = 0x0014;
        public static final int MDLEVEL_TYPE_TIME_HALF_YEAR = 0x0024;
        public static final int MDLEVEL_TYPE_TIME_QUARTERS = 0x0044;
        public static final int MDLEVEL_TYPE_TIME_MONTHS = 0x0084;
        public static final int MDLEVEL_TYPE_TIME_WEEKS = 0x0104;
        public static final int MDLEVEL_TYPE_TIME_DAYS = 0x0204;
        public static final int MDLEVEL_TYPE_TIME_HOURS = 0x0304;
        public static final int MDLEVEL_TYPE_TIME_MINUTES = 0x0404;
        public static final int MDLEVEL_TYPE_TIME_SECONDS = 0x0804;
        public static final int MDLEVEL_TYPE_TIME_UNDEFINED = 0x1004;

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the catalog to which this level belongs.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the schema to which this level belongs.");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the cube to which this level belongs.");
        private static final Column DimensionUniqueName =
            new Column(
                "DIMENSION_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The unique name of the dimension to which this level "
                + "belongs.");
        private static final Column HierarchyUniqueName =
            new Column(
                "HIERARCHY_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The unique name of the hierarchy.");
        private static final Column LevelName =
            new Column(
                "LEVEL_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the level.");
        private static final Column LevelUniqueName =
            new Column(
                "LEVEL_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The properly escaped unique name of the level.");
        private static final Column LevelGuid =
            new Column(
                "LEVEL_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Level GUID.");
        private static final Column LevelCaption =
            new Column(
                "LEVEL_CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A label or caption associated with the hierarchy.");
        private static final Column LevelNumber =
            new Column(
                "LEVEL_NUMBER",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The distance of the level from the root of the hierarchy. "
                + "Root level is zero (0).");
        private static final Column LevelCardinality =
            new Column(
                "LEVEL_CARDINALITY",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The number of members in the level. This value can be an "
                + "approximation of the real cardinality.");
        private static final Column LevelType =
            new Column(
                "LEVEL_TYPE",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Type of the level");
        private static final Column CustomRollupSettings =
            new Column(
                "CUSTOM_ROLLUP_SETTINGS",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A bitmap that specifies the custom rollup options.");
        private static final Column LevelUniqueSettings =
            new Column(
                "LEVEL_UNIQUE_SETTINGS",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A bitmap that specifies which columns contain unique values, "
                + "if the level only has members with unique names or keys.");
        private static final Column LevelIsVisible =
            new Column(
                "LEVEL_IS_VISIBLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A Boolean that indicates whether the level is visible.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A human-readable description of the level. NULL if no "
                + "description exists.");
        private static final Column LevelOrigin =
            new Column(
                "LEVEL_ORIGIN",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bit map that defines how the level was sourced:\n" +
                        "MD_ORIGIN_USER_DEFINED identifies levels in a user defined hierarchy.\n" +
                        "MD_ORIGIN_ATTRIBUTE identifies levels in an attribute hierarchy.\n" +
                        "MD_ORIGIN_KEY_ATTRIBUTE identifies levels in a key attribute hierarchy.\n" +
                        "MD_ORIGIN_INTERNAL identifies levels in attribute hierarchies that are not enabled.\n");
        private static final Column CubeSource =
            new Column(
                "CUBE_SOURCE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bitmap with one of the following valid values:\n" +
                        "1 CUBE\n" +
                        "2 DIMENSION\n" +
                        "Default restriction is a value of 1.");
        private static final Column LevelVisibility =
            new Column(
                "LEVEL_VISIBILITY",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bitmap with one of the following values:\n" +
                        "1 Visible\n" +
                        "2 Not visible\n" +
                        "Default restriction is a value of 1.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogCond))
            {
                populateCatalog(connection, catalog, rows);
            }
        }

        protected void populateCatalog(
            OlapConnection connection,
            Catalog catalog,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    populateCube(connection, catalog, cube, rows);
                }
            }
        }

        protected void populateCube(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Dimension dimension
                : filter(cube.getDimensions(), dimensionUnameCond))
            {
                populateDimension(
                    connection, catalog, cube, dimension, rows);
            }
        }

        protected void populateDimension(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Dimension dimension,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Hierarchy hierarchy
                : filter(dimension.getHierarchies(), hierarchyUnameCond))
            {
                populateHierarchy(
                    connection, catalog, cube, hierarchy, rows);
            }
        }

        protected void populateHierarchy(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Hierarchy hierarchy,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Level level
                : filter(hierarchy.getLevels(), levelUnameCond, levelNameCond))
            {
                outputLevel(
                    connection, catalog, cube, hierarchy, level, rows);
            }
        }

        /**
         * Outputs a level.
         *
         * @param catalog Catalog name
         * @param cube Cube definition
         * @param hierarchy Hierarchy
         * @param level Level
         * @param rows List of rows to output to
         * @return whether the level is visible
         * @throws XmlaException If error occurs
         */
        protected boolean outputLevel(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Hierarchy hierarchy,
            Level level,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            final XmlaHandler.XmlaExtra extra = getExtra(connection);
            String desc = level.getDescription();
            if (desc == null) {
                desc =
                    cube.getName() + " Cube - "
                    + getHierarchyName(hierarchy) + " Hierarchy - "
                    + level.getName() + " Level";
            }

            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(
                DimensionUniqueName.name,
                hierarchy.getDimension().getUniqueName());
            row.set(HierarchyUniqueName.name, hierarchy.getUniqueName());
            row.set(LevelName.name, level.getName());
            row.set(LevelUniqueName.name, level.getUniqueName());
            //row.set(LevelGuid.name, "");
            row.set(LevelCaption.name, level.getCaption());
            // see notes on this #getDepth()
            row.set(LevelNumber.name, level.getDepth());

            // Get level cardinality
            // According to microsoft this is:
            //   "The number of members in the level."
            int n = extra.getLevelCardinality(level);
            row.set(LevelCardinality.name, n);

            row.set(LevelType.name, getLevelType(level));

            // TODO: most of the time this is correct
            row.set(CustomRollupSettings.name, 0);

            int uniqueSettings = 0;
            if (level.getLevelType() == Level.Type.ALL) {
                uniqueSettings |= 2;
            }
            if (extra.isLevelUnique(level)) {
                uniqueSettings |= 1;
            }
            row.set(LevelUniqueSettings.name, uniqueSettings);
            row.set(LevelIsVisible.name, level.isVisible());
            row.set(Description.name, desc);
            row.set(LevelOrigin.name, 0);
            addRow(row, rows);
            return true;
        }

        private int getLevelType(Level lev) {
            int ret = 0;

            switch (lev.getLevelType()) {
            case ALL:
                ret |= MDLEVEL_TYPE_ALL;
                break;
            case REGULAR:
                ret |= MDLEVEL_TYPE_REGULAR;
                break;
            case TIME_YEARS:
                ret |= MDLEVEL_TYPE_TIME_YEARS;
                break;
            case TIME_HALF_YEAR:
                ret |= MDLEVEL_TYPE_TIME_HALF_YEAR;
                break;
            case TIME_QUARTERS:
                ret |= MDLEVEL_TYPE_TIME_QUARTERS;
                break;
            case TIME_MONTHS:
                ret |= MDLEVEL_TYPE_TIME_MONTHS;
                break;
            case TIME_WEEKS:
                ret |= MDLEVEL_TYPE_TIME_WEEKS;
                break;
            case TIME_DAYS:
                ret |= MDLEVEL_TYPE_TIME_DAYS;
                break;
            case TIME_HOURS:
                ret |= MDLEVEL_TYPE_TIME_HOURS;
                break;
            case TIME_MINUTES:
                ret |= MDLEVEL_TYPE_TIME_MINUTES;
                break;
            case TIME_SECONDS:
                ret |= MDLEVEL_TYPE_TIME_SECONDS;
                break;
            case TIME_UNDEFINED:
                ret |= MDLEVEL_TYPE_TIME_UNDEFINED;
                break;
            default:
                ret |= MDLEVEL_TYPE_UNKNOWN;
            }

            return ret;
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }


    public static class MdschemaMeasuresRowset extends Rowset {
        public static final int MDMEASURE_AGGR_UNKNOWN = 0;
        public static final int MDMEASURE_AGGR_SUM = 1;
        public static final int MDMEASURE_AGGR_COUNT = 2;
        public static final int MDMEASURE_AGGR_MIN = 3;
        public static final int MDMEASURE_AGGR_MAX = 4;
        public static final int MDMEASURE_AGGR_AVG = 5;
        public static final int MDMEASURE_AGGR_VAR = 6;
        public static final int MDMEASURE_AGGR_STD = 7;
        public static final int MDMEASURE_AGGR_CALCULATED = 127;

        private final Predicate<Catalog> catalogCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<Measure> measureUnameCond;
        private final Predicate<Measure> measureNameCond;

        MdschemaMeasuresRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_MEASURES, request, handler);
            catalogCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            measureNameCond = makeCondition(ELEMENT_NAME_GETTER, MeasureName);
            measureUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, MeasureUniqueName);
        }

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the catalog to which this measure belongs.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the schema to which this measure belongs.");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the cube to which this measure belongs.");
        private static final Column MeasureName =
            new Column(
                "MEASURE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The name of the measure.");
        private static final Column MeasureUniqueName =
            new Column(
                "MEASURE_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The Unique name of the measure.");
        private static final Column MeasureCaption =
            new Column(
                "MEASURE_CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A label or caption associated with the measure.");
        private static final Column MeasureGuid =
            new Column(
                "MEASURE_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Measure GUID.");
        private static final Column MeasureAggregator =
            new Column(
                "MEASURE_AGGREGATOR",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "How a measure was derived.");
        private static final Column DataType =
            new Column(
                "DATA_TYPE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Data type of the measure.");
        private static final Column MeasureIsVisible =
            new Column(
                "MEASURE_IS_VISIBLE",
                Type.BOOLEAN,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "A Boolean that always returns True. If the measure is not "
                + "visible, it will not be included in the schema rowset.");
        private static final Column LevelsList =
            new Column(
                "LEVELS_LIST",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A string that always returns NULL. EXCEPT that SQL Server "
                + "returns non-null values!!!");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A human-readable description of the measure.");
        private static final Column MeasuregroupName =
            new Column(
                "MEASUREGROUP_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the measure group to which the measure belongs.");
        private static final Column DisplayFolder =
            new Column(
                "MEASURE_DISPLAY_FOLDER",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The path to be used when displaying the measure in the user interface. Folder names will be separated by a semicolon. Nested folders are indicated by a backslash (\\).");
        private static final Column FormatString =
            new Column(
                "DEFAULT_FORMAT_STRING",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The default format string for the measure.");
        private static final Column MeasureVisiblity =
            new Column(
                "MEASURE_VISIBILITY",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bitmap with one of the following valid values: 1 Visible, 2 Not visible.");
        private static final Column CubeSource =
            new Column(
                "CUBE_SOURCE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "A bitmap with one of the following valid values:\n" +
                        "1 CUBE\n" +
                        "2 DIMENSION\n" +
                        "Default restriction is a value of 1.");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogCond))
            {
                populateCatalog(connection, catalog, rows);
            }
        }

        protected void populateCatalog(
            OlapConnection connection,
            Catalog catalog,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            // SQL Server actually includes the LEVELS_LIST row
            StringBuilder buf = new StringBuilder(100);

            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    buf.setLength(0);

                    int j = 0;
                    for (Dimension dimension : cube.getDimensions()) {
                        if (dimension.getDimensionType()
                            == Dimension.Type.MEASURE)
                        {
                            continue;
                        }
                        for (Hierarchy hierarchy : dimension.getHierarchies()) {
                            NamedList<Level> levels = hierarchy.getLevels();
                            Level lastLevel = levels.get(levels.size() - 1);
                            if (j++ > 0) {
                                buf.append(',');
                            }
                            buf.append(lastLevel.getUniqueName());
                        }
                    }
                    String levelListStr = buf.toString();

                    List<Member> calcMembers = new ArrayList<>();
                    for (Measure measure
                        : filter(
                            cube.getMeasures(),
                            measureNameCond,
                            measureUnameCond))
                    {
                        if (measure.isCalculated()) {
                            // Output calculated measures after stored
                            // measures.
                            calcMembers.add(measure);
                        } else {
                            populateMember(
                                connection, catalog,
                                measure, cube, levelListStr, rows);
                        }
                    }

                    for (Member member : calcMembers) {
                        populateMember(
                            connection, catalog, member, cube, null, rows);
                    }
                }
            }
        }

        private void populateMember(
            OlapConnection connection,
            Catalog catalog,
            Member member,
            Cube cube,
            String levelListStr,
            List<Row> rows)
            throws SQLException
        {
            Boolean visible =
                (Boolean) member.getPropertyValue(
                    Property.StandardMemberProperty.$visible);
            if (visible == null) {
                visible = true;
            }
            if (!visible && !XmlaUtil.shouldEmitInvisibleMembers(request)) {
                return;
            }

            //TODO: currently this is always null
            String desc = member.getDescription();
            if (desc == null) {
                desc =
                    cube.getName() + " Cube - "
                    + member.getName() + " Member";
            }
            final String formatString =
                (String) member.getPropertyValue(
                    Property.StandardCellProperty.FORMAT_STRING);

            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(MeasureName.name, member.getName());
            row.set(MeasureUniqueName.name, member.getUniqueName());
            row.set(MeasureCaption.name, member.getCaption());
            //row.set(MeasureGuid.name, "");

            final XmlaHandler.XmlaExtra extra = getExtra(connection);
            row.set(MeasureAggregator.name, extra.getMeasureAggregator(member));

            // DATA_TYPE DBType best guess is string
            XmlaConstants.DBType dbType = XmlaConstants.DBType.WSTR;
            String datatype = (String)
                member.getPropertyValue(Property.StandardCellProperty.DATATYPE);
            if (datatype != null) {
                if (datatype.equals("Integer")) {
                    dbType = XmlaConstants.DBType.I4;
                } else if (datatype.equals("Numeric")) {
                    dbType = XmlaConstants.DBType.R8;
                } else {
                    dbType = XmlaConstants.DBType.WSTR;
                }
            }
            row.set(DataType.name, dbType.xmlaOrdinal());
            row.set(MeasureIsVisible.name, visible);

            row.set(MeasuregroupName.name, cube.getName());

            String displayFolder = extra.getMeasureDisplayFolder(member);
            if(displayFolder == null) {
                displayFolder = "";
            }
            row.set(DisplayFolder.name, displayFolder);

            row.set(MeasureVisiblity.name, visible?1:2);

            if (levelListStr != null) {
                row.set(LevelsList.name, levelListStr);
            }

            row.set(Description.name, desc);
            row.set(FormatString.name, formatString);
            addRow(row, rows);
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef, String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class MdschemaMembersRowset extends Rowset {
        private final Predicate<Catalog> catalogCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<Dimension> dimensionUnameCond;
        private final Predicate<Hierarchy> hierarchyUnameCond;
        private final Predicate<Member> memberNameCond;
        private final Predicate<Member> memberUnameCond;
        private final Predicate<Member> memberTypeCond;

        MdschemaMembersRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_MEMBERS, request, handler);
            catalogCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            dimensionUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, DimensionUniqueName);
            hierarchyUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, HierarchyUniqueName);
            memberNameCond = makeCondition(ELEMENT_NAME_GETTER, MemberName);
            memberUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, MemberUniqueName);
            memberTypeCond = makeCondition(MEMBER_TYPE_GETTER, MemberType);
        }

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the catalog to which this member belongs.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "The name of the schema to which this member belongs.");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Name of the cube to which this member belongs.");
        private static final Column DimensionUniqueName =
            new Column(
                "DIMENSION_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Unique name of the dimension to which this member belongs.");
        private static final Column HierarchyUniqueName =
            new Column(
                "HIERARCHY_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Unique name of the hierarchy. If the member belongs to more "
                + "than one hierarchy, there is one row for each hierarchy to "
                + "which it belongs.");
        private static final Column LevelUniqueName =
            new Column(
                "LEVEL_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                " Unique name of the level to which the member belongs.");
        private static final Column LevelNumber =
            new Column(
                "LEVEL_NUMBER",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "The distance of the member from the root of the hierarchy.");
        private static final Column MemberOrdinal =
            new Column(
                "MEMBER_ORDINAL",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Ordinal number of the member. Sort rank of the member when "
                + "members of this dimension are sorted in their natural sort "
                + "order. If providers do not have the concept of natural "
                + "ordering, this should be the rank when sorted by "
                + "MEMBER_NAME.");
        private static final Column MemberName =
            new Column(
                "MEMBER_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Name of the member.");
        private static final Column MemberUniqueName =
            new Column(
                "MEMBER_UNIQUE_NAME",
                Type.STRING_SOMETIMES_ARRAY,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                " Unique name of the member.");
        private static final Column MemberType =
            new Column(
                "MEMBER_TYPE",
                Type.INTEGER,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "Type of the member.");
        private static final Column MemberGuid =
            new Column(
                "MEMBER_GUID",
                Type.UUID,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Memeber GUID.");
        private static final Column MemberCaption =
            new Column(
                "MEMBER_CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                Column.REQUIRED,
                "A label or caption associated with the member.");
        private static final Column ChildrenCardinality =
            new Column(
                "CHILDREN_CARDINALITY",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Number of children that the member has.");
        private static final Column ParentLevel =
            new Column(
                "PARENT_LEVEL",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "The distance of the member's parent from the root level of "
                + "the hierarchy.");
        private static final Column ParentUniqueName =
            new Column(
                "PARENT_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "Unique name of the member's parent.");
        private static final Column ParentCount =
            new Column(
                "PARENT_COUNT",
                Type.UNSIGNED_INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.REQUIRED,
                "Number of parents that this member has.");
        private static final Column TreeOp_ =
            new Column(
                "TREE_OP",
                Type.ENUMERATION,
                Enumeration.TREE_OP,
                Column.RESTRICTION_TRUE,
                Column.OPTIONAL,
                "Tree Operation");
        /* Mondrian specified member properties. */
        private static final Column Depth =
            new Column(
                "DEPTH",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "depth");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogCond))
            {
                populateCatalog(connection, catalog, rows);
            }
        }

        protected void populateCatalog(
            OlapConnection connection,
            Catalog catalog,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    if (isRestricted(MemberUniqueName)) {
                        // NOTE: it is believed that if MEMBER_UNIQUE_NAME is
                        // a restriction, then none of the remaining possible
                        // restrictions other than TREE_OP are relevant
                        // (or allowed??).
                        outputUniqueMemberName(
                            connection, catalog, cube, rows);
                    } else {
                        populateCube(connection, catalog, cube, rows);
                    }
                }
            }
        }

        protected void populateCube(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            if (isRestricted(LevelUniqueName)) {
                // Note: If the LEVEL_UNIQUE_NAME has been specified, then
                // the dimension and hierarchy are specified implicitly.
                String levelUniqueName =
                    getRestrictionValueAsString(LevelUniqueName);
                if (levelUniqueName == null) {
                    // The query specified two or more unique names
                    // which means that nothing will match.
                    return;
                }

                Level level = lookupLevel(cube, levelUniqueName);
                if (level != null) {
                    // Get members of this level, without access control, but
                    // including calculated members.
                    List<Member> members = level.getMembers();
                    outputMembers(connection, members, catalog, cube, rows);
                }
            } else {
                for (Dimension dimension
                    : filter(cube.getDimensions(), dimensionUnameCond))
                {
                    populateDimension(
                        connection, catalog, cube, dimension, rows);
                }
            }
        }

        protected void populateDimension(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Dimension dimension,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Hierarchy hierarchy
                : filter(dimension.getHierarchies(), hierarchyUnameCond))
            {
                populateHierarchy(
                    connection, catalog, cube, hierarchy, rows);
            }
        }

        protected void populateHierarchy(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Hierarchy hierarchy,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            if (isRestricted(LevelNumber)) {
                int levelNumber = getRestrictionValueAsInt(LevelNumber);
                if (levelNumber == -1) {
                    LOGGER.warn(
                        "RowsetDefinition.populateHierarchy: "
                        + "LevelNumber invalid");
                    return;
                }
                NamedList<Level> levels = hierarchy.getLevels();
                if (levelNumber >= levels.size()) {
                    LOGGER.warn(
                        "RowsetDefinition.populateHierarchy: "
                        + "LevelNumber ("
                        + levelNumber
                        + ") is greater than number of levels ("
                        + levels.size()
                        + ") for hierarchy \""
                        + hierarchy.getUniqueName()
                        + "\"");
                    return;
                }

                Level level = levels.get(levelNumber);
                List<Member> members = level.getMembers();
                outputMembers(connection, members, catalog, cube, rows);
            } else {
                // At this point we get ALL of the members associated with
                // the Hierarchy (rather than getting them one at a time).
                // The value returned is not used at this point but they are
                // now cached in the SchemaReader.
                for (Level level : hierarchy.getLevels()) {
                    outputMembers(
                        connection, level.getMembers(),
                        catalog, cube, rows);
                }
            }
        }

        /**
         * Returns whether a value contains all of the bits in a mask.
         */
        private static boolean mask(int value, int mask) {
            return (value & mask) == mask;
        }

        /**
         * Adds a member to a result list and, depending upon the
         * <code>treeOp</code> parameter, other relatives of the member. This
         * method recursively invokes itself to walk up, down, or across the
         * hierarchy.
         */
        private void populateMember(
            OlapConnection connection,
            Catalog catalog,
            Cube cube,
            Member member,
            int treeOp,
            List<Row> rows)
            throws SQLException
        {
            // Visit node itself.
            if (mask(treeOp, TreeOp.SELF.xmlaOrdinal())) {
                outputMember(connection, member, catalog, cube, rows);
            }
            // Visit node's siblings (not including itself).
            if (mask(treeOp, TreeOp.SIBLINGS.xmlaOrdinal())) {
                final List<Member> siblings;
                final Member parent = member.getParentMember();
                if (parent == null) {
                    siblings = member.getHierarchy().getRootMembers();
                } else {
                    siblings = Olap4jUtil.cast(parent.getChildMembers());
                }
                for (Member sibling : siblings) {
                    if (sibling.equals(member)) {
                        continue;
                    }
                    populateMember(
                        connection, catalog,
                        cube, sibling,
                        TreeOp.SELF.xmlaOrdinal(), rows);
                }
            }
            // Visit node's descendants or its immediate children, but not both.
            if (mask(treeOp, TreeOp.DESCENDANTS.xmlaOrdinal())) {
                for (Member child : member.getChildMembers()) {
                    populateMember(
                        connection, catalog,
                        cube, child,
                        TreeOp.SELF.xmlaOrdinal() |
                        TreeOp.DESCENDANTS.xmlaOrdinal(),
                        rows);
                }
            } else if (mask(
                    treeOp, TreeOp.CHILDREN.xmlaOrdinal()))
            {
                for (Member child : member.getChildMembers()) {
                    populateMember(
                        connection, catalog,
                        cube, child,
                        TreeOp.SELF.xmlaOrdinal(), rows);
                }
            }
            // Visit node's ancestors or its immediate parent, but not both.
            if (mask(treeOp, TreeOp.ANCESTORS.xmlaOrdinal())) {
                final Member parent = member.getParentMember();
                if (parent != null) {
                    populateMember(
                        connection, catalog,
                        cube, parent,
                        TreeOp.SELF.xmlaOrdinal() |
                        TreeOp.ANCESTORS.xmlaOrdinal(), rows);
                }
            } else if (mask(treeOp, TreeOp.PARENT.xmlaOrdinal())) {
                final Member parent = member.getParentMember();
                if (parent != null) {
                    populateMember(
                        connection, catalog,
                        cube, parent,
                        TreeOp.SELF.xmlaOrdinal(), rows);
                }
            }
        }

        @Override
		protected ArrayList<Column> pruneRestrictions(ArrayList<Column> list) {
            // If they've restricted TreeOp, we don't want to literally filter
            // the result on TreeOp (because it's not an output column) or
            // on MemberUniqueName (because TreeOp will have caused us to
            // generate other members than the one asked for).
            if (list.contains(TreeOp_)) {
                list.remove(TreeOp_);
                list.remove(MemberUniqueName);
            }
            return list;
        }

        private void outputMembers(
            OlapConnection connection,
            List<Member> members,
            final Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws SQLException
        {
            for (Member member : members) {
                outputMember(connection, member, catalog, cube, rows);
            }
        }

        private void outputUniqueMemberName(
            final OlapConnection connection,
            final Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws SQLException
        {
            final Object unameRestrictions =
                restrictions.get(MemberUniqueName.name);
            List<String> list;
            if (unameRestrictions instanceof String str) {
                list = Collections.singletonList(str);
            } else {
                list = (List<String>) unameRestrictions;
            }
            for (String memberUniqueName : list) {
                final IdentifierNode identifierNode =
                    IdentifierNode.parseIdentifier(memberUniqueName);
                Member member =
                    cube.lookupMember(identifierNode.getSegmentList());
                if (member == null) {
                    return;
                }
                if (isRestricted(TreeOp_)) {
                    int treeOp = getRestrictionValueAsInt(TreeOp_);
                    if (treeOp == -1) {
                        return;
                    }
                    populateMember(
                        connection, catalog,
                        cube, member, treeOp, rows);
                } else {
                    outputMember(connection, member, catalog, cube, rows);
                }
            }
        }

        private void outputMember(
            OlapConnection connection,
            Member member,
            final Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws SQLException
        {
            if (!memberNameCond.test(member)) {
                return;
            }
            if (!memberTypeCond.test(member)) {
                return;
            }

            getExtra(connection).checkMemberOrdinal(member);

            // Check whether the member is visible, otherwise do not dump.
            Boolean visible =
                (Boolean) member.getPropertyValue(
                    Property.StandardMemberProperty.$visible);
            if (visible == null) {
                visible = true;
            }
            if (!visible && !XmlaUtil.shouldEmitInvisibleMembers(request)) {
                return;
            }

            final Level level = member.getLevel();
            final Hierarchy hierarchy = level.getHierarchy();
            final Dimension dimension = hierarchy.getDimension();

            int adjustedLevelDepth = level.getDepth();

            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(DimensionUniqueName.name, dimension.getUniqueName());
            row.set(HierarchyUniqueName.name, hierarchy.getUniqueName());
            row.set(LevelUniqueName.name, level.getUniqueName());
            row.set(LevelNumber.name, adjustedLevelDepth);
            row.set(MemberOrdinal.name, 0);
            row.set(MemberName.name, member.getName());
            row.set(MemberUniqueName.name, member.getUniqueName());
            row.set(MemberType.name, member.getMemberType().ordinal());
            //row.set(MemberGuid.name, "");
            row.set(MemberCaption.name, member.getCaption());
            row.set(
                ChildrenCardinality.name,
                member.getPropertyValue(
                    Property.StandardMemberProperty.CHILDREN_CARDINALITY));
            row.set(ChildrenCardinality.name, 100);

            if (adjustedLevelDepth == 0) {
                row.set(ParentLevel.name, 0);
            } else {
                row.set(ParentLevel.name, adjustedLevelDepth - 1);
                final Member parentMember = member.getParentMember();
                if (parentMember != null) {
                    row.set(
                        ParentUniqueName.name, parentMember.getUniqueName());
                }
            }

            row.set(ParentCount.name, member.getParentMember() == null ? 0 : 1);

            row.set(Depth.name, member.getDepth());
            addRow(row, rows);
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    static class MdschemaSetsRowset extends Rowset {
        private final Predicate<Catalog> catalogCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<NamedSet> setNameCond;
        private static final String GLOBAL_SCOPE = "1";

        MdschemaSetsRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_SETS, request, handler);
            catalogCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            setNameCond = makeCondition(ELEMENT_NAME_GETTER, SetName);
        }

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                true,
                true,
                null);
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                true,
                true,
                null);
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                true,
                false,
                null);
        private static final Column SetName =
            new Column(
                "SET_NAME",
                Type.STRING,
                null,
                true,
                false,
                null);
        private static final Column SetCaption =
            new Column(
                "SET_CAPTION",
                Type.STRING,
                null,
                true,
                true,
                null);
        private static final Column Scope =
            new Column(
                "SCOPE",
                Type.INTEGER,
                null,
                true,
                false,
                null);
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A human-readable description of the measure.");
        private static final Column Expression =
            new Column(
                "EXPRESSION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The expression for the set.");
        private static final Column Dimensions =
            new Column(
                "DIMENSIONS",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A comma delimited list of hierarchies included in the set.");
        private static final Column DisplayFolder =
            new Column(
                "SET_DISPLAY_FOLDER",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "A string that identifies the path of the display folder that the client application " +
                "uses to show the set. The folder level separator is defined by the client application. " +
                "For the tools and clients supplied by Analysis Services, the backslash (\\) is the level separator. " +
                "To provide multiple display folders, use a semicolon (;) to separate the folders.");
        private static final Column EvaluationContext =
            new Column(
                "SET_EVALUATION_CONTEXT",
                Type.INTEGER,
                null,
                Column.RESTRICTION_FALSE,
                Column.OPTIONAL,
                "The context for the set. The set can be static or dynamic.\n" +
                "This column can have one of the following values:\n" +
                "MDSET_RESOLUTION_STATIC=1\n" +
                "MDSET_RESOLUTION_DYNAMIC=2");

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, OlapException
        {
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogCond))
            {
                processCatalog(connection, catalog, rows);
            }
        }

        private void processCatalog(
            OlapConnection connection,
            Catalog catalog,
            List<Row> rows)
            throws OlapException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filter(sortedCubes(schema), cubeNameCond)) {
                    populateNamedSets(cube, catalog, rows);
                }
            }
        }

        private void populateNamedSets(
            Cube cube,
            Catalog catalog,
            List<Row> rows)
        {
            for (NamedSet namedSet : filter(cube.getSets(), setNameCond)) {
                mondrian.olap4j.MondrianOlap4jNamedSet mondrianOlap4jNamedSet =
                        (mondrian.olap4j.MondrianOlap4jNamedSet)namedSet;

                SetBase setBase = (SetBase)mondrianOlap4jNamedSet.getNamedSet();
                String dimensions = setBase.getHierarchies()
                    .stream().map(it -> it.getUniqueName()).collect(Collectors.joining(","));

                Row row = new Row();
                row.set(CatalogName.name, catalog.getName());
                row.set(SchemaName.name, cube.getSchema().getName());
                row.set(CubeName.name, cube.getName());
                row.set(SetName.name, namedSet.getName());
                //TODO: 2 (SESSION_SCOPE) for session sets
                row.set(Scope.name, GLOBAL_SCOPE);
                row.set(Description.name, namedSet.getDescription());
                row.set(Dimensions.name, dimensions);

                row.set(Expression.name, setBase.getExp().toString());

                row.set(SetCaption.name, namedSet.getCaption());

                row.set(DisplayFolder.name, setBase.getDisplayFolder());
                row.set(EvaluationContext.name, "1");
                addRow(row, rows);
            }
        }
    }

    static class MdschemaKpisRowset extends Rowset {
        private final Predicate<Catalog> catalogCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<NamedSet> kpiNameCond;

        MdschemaKpisRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_KPIS, request, handler);
            catalogCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            kpiNameCond = makeCondition(ELEMENT_NAME_GETTER, KpiName);
        }

        private static final Column CatalogName =
                new Column(
                        "CATALOG_NAME",
                        Type.STRING,
                        null,
                        true,
                        true,
                        null);
        private static final Column SchemaName =
                new Column(
                        "SCHEMA_NAME",
                        Type.STRING,
                        null,
                        true,
                        true,
                        null);
        private static final Column CubeName =
                new Column(
                        "CUBE_NAME",
                        Type.STRING,
                        null,
                        true,
                        true,
                        null);
        private static final Column MeasuregroupName =
                new Column(
                        "MEASUREGROUP_NAME",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiName =
                new Column(
                        "KPI_NAME",
                        Type.STRING,
                        null,
                        true,
                        true,
                        null);
        private static final Column KpiCaption =
                new Column(
                        "KPI_CAPTION",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiDescription =
                new Column(
                        "KPI_DESCRIPTION",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiDisplayFolder =
                new Column(
                        "KPI_DISPLAY_FOLDER",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiValue =
                new Column(
                        "KPI_VALUE",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiGoal =
                new Column(
                        "KPI_GOAL",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiStatus =
                new Column(
                        "KPI_STATUS",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiTrend =
                new Column(
                        "KPI_TREND",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiStatusGraphic =
                new Column(
                        "KPI_STATUS_GRAPHIC",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiTrendGraphic =
                new Column(
                        "KPI_TREND_GRAPHIC",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiWeight =
                new Column(
                        "KPI_WEIGHT",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiCurrentTimeMember =
                new Column(
                        "KPI_CURRENT_TIME_MEMBER",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column KpiParentKpiName =
                new Column(
                        "KPI_PARENT_KPI_NAME",
                        Type.STRING,
                        null,
                        false,
                        false,
                        null);
        private static final Column Scope =
                new Column(
                        "SCOPE",
                        Type.INTEGER,
                        null,
                        false,
                        false,
                        null);

        @Override
		public void populateImpl(
                XmlaResponse response,
                OlapConnection connection,
                List<Row> rows)
                throws XmlaException, OlapException
        {
            for (Catalog catalog
                    : catIter(connection, catNameCond(), catalogCond))
            {
                processCatalog(connection, catalog, rows);
            }
        }

        private void processCatalog(
                OlapConnection connection,
                Catalog catalog,
                List<Row> rows)
                throws OlapException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filter(sortedCubes(schema), cubeNameCond)) {
                    populateKpis(cube, catalog, rows);
                }
            }
        }

        private void populateKpis(
                Cube cube,
                Catalog catalog,
                List<Row> rows)
        {
//            for (Kpi kpi : filter(cube.getKpis(), kpiNameCond)) {
//                Row row = new Row();
//                row.set(CatalogName.name, catalog.getName());
//                addRow(row, rows);
//            }
        }
    }

    static class MdschemaMeasuregroupsRowset extends Rowset {
        private final Predicate<Catalog> catalogNameCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;

        MdschemaMeasuregroupsRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_MEASUREGROUPS, request, handler);
            catalogNameCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
        }

        private static final Column CatalogName =
                new Column(
                        "CATALOG_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "The name of the catalog to which this measure group belongs. " +
                                "NULL if the provider does not support catalogs.");
        private static final Column SchemaName =
                new Column(
                        "SCHEMA_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "Not supported.");
        private static final Column CubeName =
                new Column(
                        "CUBE_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "The name of the cube to which this measure group belongs.");
        private static final Column MeasuregroupName =
                new Column(
                        "MEASUREGROUP_NAME",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_TRUE,
                        Column.OPTIONAL,
                        "The name of the measure group.");
        private static final Column Description =
                new Column(
                        "DESCRIPTION",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "A human-readable description of the measure group.");
        private static final Column IsWriteEnabled =
                new Column(
                        "IS_WRITE_ENABLED",
                        Type.BOOLEAN,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "A Boolean that indicates whether the measure group is write-enabled.");
        private static final Column MeasuregroupCaption =
                new Column(
                        "MEASUREGROUP_CAPTION",
                        Type.STRING,
                        null,
                        Column.RESTRICTION_FALSE,
                        Column.OPTIONAL,
                        "The display caption for the measure group.");

        @Override
		public void populateImpl(
                XmlaResponse response,
                OlapConnection connection,
                List<Row> rows)
                throws XmlaException, SQLException
        {
            for (Catalog catalog
                    : catIter(connection, catNameCond(), catalogNameCond))
            {
                populateCatalog(connection, catalog, rows);
            }
        }

        protected void populateCatalog(
                OlapConnection connection,
                Catalog catalog,
                List<Row> rows)
                throws XmlaException, SQLException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    if (!(cube instanceof SharedDimensionHolderCube)) {
                        populateCube(connection, catalog, cube, rows);
                    }
                }
            }
        }

        protected void populateCube(
                OlapConnection connection,
                Catalog catalog,
                Cube cube,
                List<Row> rows)
                throws XmlaException
        {
//            for (Measuregroup measuregroup
//                    : filter(
//                    cube.getMeasuregroups(),
//                    measuregroupCond))
//            {
//                populateMeasuregroup(
//                        connection, catalog, cube, measuregroup, rows);
//            }
            populateMeasuregroup(
                    connection, catalog, cube, rows);
        }

        protected void populateMeasuregroup(
                OlapConnection connection,
                Catalog catalog,
                Cube cube,
                List<Row> rows)
                throws XmlaException
        {
            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(MeasuregroupName.name, cube.getName());
            row.set(Description.name, "");
            row.set(IsWriteEnabled.name, false);
            row.set(MeasuregroupCaption.name, cube.getName());

            addRow(row, rows);
        }
    }

    static class MdschemaPropertiesRowset extends Rowset {
        private final Predicate<Catalog> catalogCond;
        private final Predicate<Schema> schemaNameCond;
        private final Predicate<Cube> cubeNameCond;
        private final Predicate<Dimension> dimensionUnameCond;
        private final Predicate<Hierarchy> hierarchyUnameCond;
        private final Predicate<Property> propertyNameCond;

        MdschemaPropertiesRowset(XmlaRequest request, XmlaHandler handler) {
            super(MDSCHEMA_PROPERTIES, request, handler);
            catalogCond = makeCondition(CATALOG_NAME_GETTER, CatalogName);
            schemaNameCond = makeCondition(SCHEMA_NAME_GETTER, SchemaName);
            cubeNameCond = makeCondition(ELEMENT_NAME_GETTER, CubeName);
            dimensionUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, DimensionUniqueName);
            hierarchyUnameCond =
                makeCondition(ELEMENT_UNAME_GETTER, HierarchyUniqueName);
            propertyNameCond = makeCondition(ELEMENT_NAME_GETTER, PropertyName);
        }

        private static final Column CatalogName =
            new Column(
                "CATALOG_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                0,
                Column.OPTIONAL,
                "The name of the database.");
        private static final Column SchemaName =
            new Column(
                "SCHEMA_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                1,
                Column.OPTIONAL,
                "The name of the schema to which this property belongs.");
        private static final Column CubeName =
            new Column(
                "CUBE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                2,
                Column.OPTIONAL,
                "The name of the cube.");
        private static final Column DimensionUniqueName =
            new Column(
                "DIMENSION_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                3,
                Column.OPTIONAL,
                "The unique name of the dimension.");
        private static final Column HierarchyUniqueName =
            new Column(
                "HIERARCHY_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                4,
                Column.OPTIONAL,
                "The unique name of the hierarchy.");
        private static final Column LevelUniqueName =
            new Column(
                "LEVEL_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                5,
                Column.OPTIONAL,
                "The unique name of the level to which this property belongs.");
        // According to MS this should not be nullable
        private static final Column MemberUniqueName =
            new Column(
                "MEMBER_UNIQUE_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                6,
                Column.OPTIONAL,
                "The unique name of the member to which the property belongs.");
        private static final Column PropertyType =
            new Column(
                "PROPERTY_TYPE",
                Type.SHORT,
                null,
                Column.RESTRICTION_TRUE,
                8,
                Column.REQUIRED,
                "A bitmap that specifies the type of the property");
        private static final Column PropertyName =
            new Column(
                "PROPERTY_NAME",
                Type.STRING,
                null,
                Column.RESTRICTION_TRUE,
                7,
                Column.REQUIRED,
                "Name of the property.");
        private static final Column PropertyCaption =
            new Column(
                "PROPERTY_CAPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                10,
                Column.REQUIRED,
                "A label or caption associated with the property, used "
                        + "primarily for display purposes.");
        private static final Column DataType =
            new Column(
                "DATA_TYPE",
                Type.UNSIGNED_SHORT,
                null,
                Column.RESTRICTION_FALSE,
                11,
                Column.REQUIRED,
                "Data type of the property.");
        private static final Column PropertyContentType =
            new Column(
                "PROPERTY_CONTENT_TYPE",
                Type.SHORT,
                null,
                Column.RESTRICTION_TRUE,
                9,
                Column.OPTIONAL,
                "The type of the property.");
        private static final Column Description =
            new Column(
                "DESCRIPTION",
                Type.STRING,
                null,
                Column.RESTRICTION_FALSE,
                12,
                Column.OPTIONAL,
                "A human-readable description of the measure.");

        @Override
		protected boolean needConnection() {
            return false;
        }

        @Override
		public void populateImpl(
            XmlaResponse response,
            OlapConnection connection,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            // Default PROPERTY_TYPE is MDPROP_MEMBER.
            @SuppressWarnings({"unchecked"})
            final List<String> list =
                (List<String>) restrictions.get(PropertyType.name);
            Set<Property.TypeFlag> typeFlags;
            if (list == null) {
                typeFlags =
                    Olap4jUtil.enumSetOf(
                        Property.TypeFlag.MEMBER);
            } else {
                typeFlags =
                    Property.TypeFlag.getDictionary().forMask(
                        Integer.valueOf(list.get(0)));
            }

            for (Property.TypeFlag typeFlag : typeFlags) {
                switch (typeFlag) {
                case MEMBER:
                    populateMember(rows);
                    break;
                case CELL:
                    populateCell(rows);
                    break;
                case SYSTEM, BLOB:
                default:
                    break;
                }
            }
        }

        private void populateCell(List<Row> rows) {
            for (Property.StandardCellProperty property
                : Property.StandardCellProperty.values())
            {
                Row row = new Row();
                row.set(
                    PropertyType.name,
                    Property.TypeFlag.getDictionary()
                        .toMask(
                            property.getType()));
                row.set(PropertyName.name, property.name());
                row.set(PropertyCaption.name, property.getCaption());
                row.set(DataType.name, property.getDatatype().xmlaOrdinal());
                addRow(row, rows);
            }
        }

        private void populateMember(List<Row> rows) throws SQLException {
            OlapConnection connection =
                handler.getConnection(
                    request,
                    Collections.<String, String>emptyMap());
            for (Catalog catalog
                : catIter(connection, catNameCond(), catalogCond))
            {
                populateCatalog(catalog, rows);
            }
        }

        protected void populateCatalog(
            Catalog catalog,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            for (Schema schema : filter(catalog.getSchemas(), schemaNameCond)) {
                for (Cube cube : filteredCubes(schema, cubeNameCond)) {
                    populateCube(catalog, cube, rows);
                }
            }
        }

        protected void populateCube(
            Catalog catalog,
            Cube cube,
            List<Row> rows)
            throws XmlaException, SQLException
        {
            if (cube instanceof SharedDimensionHolderCube) {
                return;
            }
            if (isRestricted(LevelUniqueName)) {
                // Note: If the LEVEL_UNIQUE_NAME has been specified, then
                // the dimension and hierarchy are specified implicitly.
                String levelUniqueName =
                    getRestrictionValueAsString(LevelUniqueName);
                if (levelUniqueName == null) {
                    // The query specified two or more unique names
                    // which means that nothing will match.
                    return;
                }
                Level level = lookupLevel(cube, levelUniqueName);
                if (level == null) {
                    return;
                }
                populateLevel(
                    catalog, cube, level, rows);
            } else {
                for (Dimension dimension
                    : filter(cube.getDimensions(), dimensionUnameCond))
                {
                    populateDimension(
                        catalog, cube, dimension, rows);
                }
            }
        }

        private void populateDimension(
            Catalog catalog,
            Cube cube,
            Dimension dimension,
            List<Row> rows)
            throws SQLException
        {
            for (Hierarchy hierarchy
                : filter(dimension.getHierarchies(), hierarchyUnameCond))
            {
                populateHierarchy(
                    catalog, cube, hierarchy, rows);
            }
        }

        private void populateHierarchy(
            Catalog catalog,
            Cube cube,
            Hierarchy hierarchy,
            List<Row> rows)
            throws SQLException
        {
            for (Level level : hierarchy.getLevels()) {
                populateLevel(catalog, cube, level, rows);
            }
        }

        private void populateLevel(
            Catalog catalog,
            Cube cube,
            Level level,
            List<Row> rows)
            throws SQLException
        {
            final XmlaHandler.XmlaExtra extra =
                getExtra(catalog.getMetaData().getConnection());
            for (Property property
                : filter(extra.getLevelProperties(level), propertyNameCond))
            {
                if (extra.isPropertyInternal(property)) {
                    continue;
                }
                outputProperty(
                    property, catalog, cube, level, rows);
            }
        }

        private void outputProperty(
            Property property,
            Catalog catalog,
            Cube cube,
            Level level,
            List<Row> rows)
        {
            Hierarchy hierarchy = level.getHierarchy();
            Dimension dimension = hierarchy.getDimension();

            String propertyName = property.getName();

            Row row = new Row();
            row.set(CatalogName.name, catalog.getName());
            row.set(SchemaName.name, cube.getSchema().getName());
            row.set(CubeName.name, cube.getName());
            row.set(DimensionUniqueName.name, dimension.getUniqueName());
            row.set(HierarchyUniqueName.name, hierarchy.getUniqueName());
            row.set(LevelUniqueName.name, level.getUniqueName());
            //TODO: what is the correct value here
            //row.set(MemberUniqueName.name, "");

            row.set(PropertyName.name, propertyName);
            // Only member properties now
            row.set(
                PropertyType.name,
                Property.TypeFlag.MEMBER.xmlaOrdinal());
            row.set(
                PropertyContentType.name,
                Property.ContentType.REGULAR.xmlaOrdinal());
            row.set(PropertyCaption.name, property.getCaption());
            XmlaConstants.DBType dbType = getDBTypeFromProperty(property);
            row.set(DataType.name, dbType.xmlaOrdinal());

            String desc =
                cube.getName() + " Cube - "
                + getHierarchyName(hierarchy) + " Hierarchy - "
                + level.getName() + " Level - "
                + property.getName() + " Property";
            row.set(Description.name, desc);

            addRow(row, rows);
        }

        @Override
		protected void setProperty(
            PropertyDefinition propertyDef,
            String value)
        {
            if (!PropertyDefinition.Content.equals(propertyDef)) {
                super.setProperty(propertyDef, value);
            }
        }
    }

    public static final Function<Catalog,String> CATALOG_NAME_GETTER =
        new Function<>() {
            @Override
			public String apply(Catalog catalog) {
                return catalog.getName();
            }
        };

    public static final Function<Schema,String> SCHEMA_NAME_GETTER =
        new Function<>() {
            @Override
			public String apply(Schema schema) {
                return schema.getName();
            }
        };

    public static final Function<MetadataElement,String>
        ELEMENT_NAME_GETTER =
        new Function<>() {
            @Override
			public String apply(MetadataElement element) {
                return element.getName();
            }
        };

    public static final Function< MetadataElement,String>
        ELEMENT_UNAME_GETTER =
        new Function<>() {
            @Override
			public String apply(MetadataElement element) {
                return element.getUniqueName();
            }
        };

    public static final Function<Member,Member.Type>
        MEMBER_TYPE_GETTER =
        new Function<>() {
            @Override
			public Member.Type apply(Member member) {
                return member.getMemberType();
            }
        };

    public static final Function< PropertyDefinition,String>
        PROPDEF_NAME_GETTER =
        new Function<>() {
            @Override
			public String apply(PropertyDefinition property) {
                return property.name();
            }
        };

    static void serialize(StringBuilder buf, Collection<String> strings) {
        buf.append(strings.stream().sorted().collect(Collectors.joining(",")));
    }

    private static Level lookupLevel(Cube cube, String levelUniqueName) {
        for (Dimension dimension : cube.getDimensions()) {
            for (Hierarchy hierarchy : dimension.getHierarchies()) {
                for (Level level : hierarchy.getLevels()) {
                    if (level.getUniqueName().equals(levelUniqueName)) {
                        return level;
                    }
                }
            }
        }
        return null;
    }

    static Iterable<Cube> sortedCubes(Schema schema) throws OlapException {
        return Util.sort(
            schema.getCubes(),
            new Comparator<Cube>() {
                @Override
				public int compare(Cube o1, Cube o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            }
        );
    }

    static Iterable<Cube> filteredCubes(
        final Schema schema,
        Predicate<Cube> cubeNameCond)
        throws OlapException
    {
        final Iterable<Cube> iterable =
            filter(sortedCubes(schema), cubeNameCond);
        if (!cubeNameCond.test(new SharedDimensionHolderCube(schema))) {
            return iterable;
        }
        return Composite.of(
            Collections.singletonList(
                new SharedDimensionHolderCube(schema)),
            iterable);
    }

    private static String getHierarchyName(Hierarchy hierarchy) {
        String hierarchyName = hierarchy.getName();
        if (MondrianProperties.instance().SsasCompatibleNaming.get()
            && !hierarchyName.equals(hierarchy.getDimension().getName()))
        {
            hierarchyName =
                hierarchy.getDimension().getName() + "." + hierarchyName;
        }
        return hierarchyName;
    }

    private static XmlaRequest wrapRequest(
        XmlaRequest request, Map<Column, String> map)
    {
        final Map<String, Object> restrictionsMap =
            new HashMap<>(request.getRestrictions());
        for (Map.Entry<Column, String> entry : map.entrySet()) {
            restrictionsMap.put(
                entry.getKey().name,
                Collections.singletonList(entry.getValue()));
        }

        return new DelegatingXmlaRequest(request) {
            @Override
            public Map<String, Object> getRestrictions() {
                return restrictionsMap;
            }
        };
    }

    /**
     * Returns an iterator over the catalogs in a connection, setting the
     * connection's catalog to each successful catalog in turn.
     *
     * @param connection Connection
     * @param conds Zero or more conditions to be applied to catalogs
     * @return Iterator over catalogs
     */
    private static Iterable<Catalog> catIter(
        final OlapConnection connection,
        final Predicate<Catalog>... conds)
    {
        return new Iterable<>() {
            @Override
			public Iterator<Catalog> iterator() {
                try {
                    return new Iterator<>() {
                        final Iterator<Catalog> catalogIter =
                            Util.filter(
                                connection.getOlapCatalogs(),
                                conds).iterator();

                        @Override
						public boolean hasNext() {
                            return catalogIter.hasNext();
                        }

                        @Override
						public Catalog next() {
                            Catalog catalog = catalogIter.next();
                            try {
                                connection.setCatalog(catalog.getName());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            return catalog;
                        }

                        @Override
						public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                } catch (OlapException e) {
                    throw new RuntimeException(
                        "Failed to obtain a list of catalogs form the connection object.",
                        e);
                }
            }
        };
    }

    private static class DelegatingXmlaRequest implements XmlaRequest {
        protected final XmlaRequest request;

        public DelegatingXmlaRequest(XmlaRequest request) {
            this.request = request;
        }

        @Override
		public XmlaConstants.Method getMethod() {
            return request.getMethod();
        }

        @Override
		public Map<String, String> getProperties() {
            return request.getProperties();
        }

        @Override
		public Map<String, Object> getRestrictions() {
            return request.getRestrictions();
        }

        @Override
		public String getStatement() {
            return request.getStatement();
        }

        @Override
		public String getRoleName() {
            return request.getRoleName();
        }

        @Override
		public String getRequestType() {
            return request.getRequestType();
        }

        @Override
		public boolean isDrillThrough() {
            return request.isDrillThrough();
        }

        @Override
		public String getUsername() {
            return request.getUsername();
        }

        @Override
		public String getPassword() {
            return request.getPassword();
        }

        @Override
		public String getSessionId() {
            return request.getSessionId();
        }
    }

    /**
     * Dummy implementation of {@link Cube} that holds all shared dimensions
     * in a given schema. Less error-prone than requiring all generator code
     * to cope with a null Cube.
     */
    private static class SharedDimensionHolderCube implements Cube {
        private final Schema schema;

        public SharedDimensionHolderCube(Schema schema) {
            this.schema = schema;
        }

        @Override
		public Schema getSchema() {
            return schema;
        }

        @Override
		public NamedList<Dimension> getDimensions() {
            try {
                return schema.getSharedDimensions();
            } catch (OlapException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
		public NamedList<Hierarchy> getHierarchies() {
            final NamedList<Hierarchy> hierarchyList =
                new ArrayNamedListImpl<>() {
                    @Override
					public String getName(Object hierarchy) {
                        return ((Hierarchy)hierarchy).getName();
                    }
                };
            for (Dimension dimension : getDimensions()) {
                hierarchyList.addAll(dimension.getHierarchies());
            }
            return hierarchyList;
        }

        @Override
		public List<Measure> getMeasures() {
            return Collections.emptyList();
        }

        @Override
		public NamedList<NamedSet> getSets() {
            throw new UnsupportedOperationException();
        }

        @Override
		public Collection<Locale> getSupportedLocales() {
            throw new UnsupportedOperationException();
        }

        @Override
		public Member lookupMember(List<IdentifierSegment> identifierSegments)
            throws org.olap4j.OlapException
        {
            throw new UnsupportedOperationException();
        }

        @Override
		public List<Member> lookupMembers(
            Set<Member.TreeOp> treeOps,
            List<IdentifierSegment> identifierSegments)
            throws org.olap4j.OlapException
        {
            throw new UnsupportedOperationException();
        }

        @Override
		public boolean isDrillThroughEnabled() {
            return false;
        }

        @Override
		public String getName() {
            return "";
        }

        @Override
		public String getUniqueName() {
            return "";
        }

        @Override
		public String getCaption() {
            return "";
        }

        @Override
		public String getDescription() {
            return "";
        }

        @Override
		public boolean isVisible() {
            return false;
        }
    }
}

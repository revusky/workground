/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2003-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara
// All Rights Reserved.
*/
package mondrian.xmla;

import mondrian.olap.MondrianException;
import mondrian.olap.Util;
import mondrian.util.XmlParserFactoryProducer;
import mondrian.xmla.impl.DefaultXmlaResponse;
import org.olap4j.OlapConnection;
import org.olap4j.impl.LcidLocale;
import org.olap4j.metadata.XmlaConstants.Format;
import org.olap4j.metadata.XmlaConstants.Method;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Utility methods for XML/A implementation.
 *
 * @author Gang Chen
 */
public class XmlaUtil implements XmlaConstants {

    /**
     * Invalid characters for XML element name.
     *
     * <p>XML element name:
     *
     * Char ::= #x9 | #xA | #xD | [#x20-#xD7FF]
     *        | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * S ::= (#x20 | #x9 | #xD | #xA)+
     * NameChar ::= Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar
     *        | Extender
     * Name ::= (Letter | '_' | ':') (NameChar)*
     * Names ::= Name (#x20 Name)*
     * Nmtoken ::= (NameChar)+
     * Nmtokens ::= Nmtoken (#x20 Nmtoken)*
     *
     */
    private static final String VALID_CHARACTERS_EXP = "^[:A-Z_a-z\u00C0\u00D6\u00D8-\u00F6\u00F8-\u02ff\u0370-\u037d"
            + "\u037f-\u1fff\u200c\u200d\u2070-\u218f\u2c00-\u2fef\u3001-\ud7ff"
            + "\uf900-\ufdcf\ufdf0-\ufffd]"
            + "[:A-Z_a-z\u00C0\u00D6\u00D8-\u00F6"
            + "\u00F8-\u02ff\u0370-\u037d\u037f-\u1fff\u200c\u200d\u2070-\u218f"
            + "\u2c00-\u2fef\u3001-\udfff\uf900-\ufdcf\ufdf0-\ufffd\\-\\.0-9"
            + "\u00b7\u0300-\u036f\u203f-\u2040]*\\Z";
    private static Pattern validCharactersPatern = Pattern.compile(VALID_CHARACTERS_EXP);

    private XmlaUtil() {
        // constructor
    }

    private static String encodeChar(char c) {
        StringBuilder buf = new StringBuilder();
        buf.append("_x");
        String str = Integer.toHexString(c);
        for (int i = 4 - str.length(); i > 0; i--) {
            buf.append("0");
        }
        return buf.append(str).append("_").toString();
    }

    /**
     * Encodes an XML element name.
     *
     * <p>This function is mainly for encode element names in result of Drill
     * Through execute, because its element names come from database, we cannot
     * make sure they are valid XML contents.
     *
     * <p>Quoth the <a href="http://xmla.org">XML/A specification</a>, version
     * 1.1:
     * <blockquote>
     * XML does not allow certain characters as element and attribute names.
     * XML for Analysis supports encoding as defined by SQL Server 2000 to
     * address this XML constraint. For column names that contain invalid XML
     * name characters (according to the XML 1.0 specification), the nonvalid
     * Unicode characters are encoded using the corresponding hexadecimal
     * values. These are escaped as _x<i>HHHH_</i> where <i>HHHH</i> stands for
     * the four-digit hexadecimal UCS-2 code for the character in
     * most-significant bit first order. For example, the name "Order Details"
     * is encoded as Order_<i>x0020</i>_Details, where the space character is
     * replaced by the corresponding hexadecimal code.
     * </blockquote>
     *
     * @param name Name of XML element
     * @return encoded name
     */
    private static String encodeElementName(String name) {
        StringBuilder buf = new StringBuilder();
        char[] nameChars = name.toCharArray();
        for (char ch : nameChars) {
            boolean b = validCharactersPatern.matcher("" + ch).matches();
            if(b) {
                buf.append(ch);
            } else {
                buf.append(encodeChar(ch));
            }
        }
        return buf.toString();
    }


    public static void element2Text(Element elem, final StringWriter writer)
        throws XmlaException
    {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer();
            transformer.transform(
                new DOMSource(elem),
                new StreamResult(writer));
        } catch (Exception e) {
            throw new XmlaException(
                CLIENT_FAULT_FC,
                USM_DOM_PARSE_CODE,
                USM_DOM_PARSE_FAULT_FS,
                e);
        }
    }

    public static Element text2Element(String text)
        throws XmlaException
    {
        return toElement(new InputSource(new StringReader(text)));
    }

    public static Element stream2Element(InputStream stream)
        throws XmlaException
    {
        return toElement(new InputSource(stream));
    }

    private static Element toElement(InputSource source)
        throws XmlaException
    {
        try {
            DocumentBuilderFactory factory =
                XmlParserFactoryProducer.createSecureDocBuilderFactory();
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(source);
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new XmlaException(
                CLIENT_FAULT_FC,
                USM_DOM_PARSE_CODE,
                USM_DOM_PARSE_FAULT_FS,
                e);
        }
    }

    public static Element[] filterChildElements(
        Element parent,
        String ns,
        String lname)
    {

        List<Element> elems = new ArrayList<>();
        NodeList nlst = parent.getChildNodes();
        for (int i = 0, nlen = nlst.getLength(); i < nlen; i++) {
            Node n = nlst.item(i);
            if (n instanceof Element e && (ns == null || ns.equals(e.getNamespaceURI()))
                    && (lname == null || lname.equals(e.getLocalName())))
            {
                elems.add(e);
            }
        }
        return elems.toArray(new Element[elems.size()]);
    }

    public static String textInElement(Element elem) {
        StringBuilder buf = new StringBuilder(100);
        elem.normalize();
        NodeList nlst = elem.getChildNodes();
        for (int i = 0, nlen = nlst.getLength(); i < nlen ; i++) {
            Node n = nlst.item(i);
            if (n instanceof Text text) {
                final String data = text.getData();
                buf.append(data);
            }
        }
        return buf.toString();
    }

    /**
     * Finds root MondrianException in exception chain if exists,
     * otherwise the input throwable.
     *
     * @param throwable Exception
     * @return Root exception
     */
    public static Throwable rootThrowable(Throwable throwable) {
        Throwable rootThrowable = throwable.getCause();
        if (rootThrowable instanceof MondrianException)
        {
            return rootThrowable(rootThrowable);
        }
        return throwable;
    }

    /**
     * Corrects for the differences between numeric strings arising because
     * JDBC drivers use different representations for numbers
     * ({@link Double} vs. {@link java.math.BigDecimal}) and
     * these have different toString() behavior.
     *
     * <p>If it contains a decimal point, then
     * strip off trailing '0's. After stripping off
     * the '0's, if there is nothing right of the
     * decimal point, then strip off decimal point.
     *
     * @param numericStr Numeric string
     * @return Normalized string
     */
    public static String normalizeNumericString(String numericStr) {
        int index = numericStr.indexOf('.');
        if (index > 0) {
            // If it uses exponential notation, 1.0E4, then it could
            // have a trailing '0' that should not be stripped of,
            // e.g., 1.0E10. This would be rather bad.
            if ((numericStr.indexOf('e') != -1) || (numericStr.indexOf('E') != -1)) {
                return numericStr;
            }

            boolean found = false;
            int p = numericStr.length();
            char c = numericStr.charAt(p - 1);
            while (c == '0') {
                found = true;
                p--;
                c = numericStr.charAt(p - 1);
            }
            if (c == '.') {
                p--;
            }
            if (found) {
                return numericStr.substring(0, p);
            }
        }
        return numericStr;
    }

    /**
     * Returns a set of column headings and rows for a given metadata request.
     *
     * <p/>Leverages mondrian's implementation of the XML/A specification, and
     * is exposed here for use by mondrian's olap4j driver.
     *
     * @param connection Connection
     * @param methodName Metadata method name per XMLA (e.g. "MDSCHEMA_CUBES")
     * @param restrictionMap Restrictions
     * @return Set of rows and column headings
     */
    public static MetadataRowset getMetadataRowset(
        final OlapConnection connection,
        String methodName,
        final Map<String, Object> restrictionMap)
    {
        RowsetDefinition rowsetDefinition =
            RowsetDefinition.valueOf(methodName);

        final XmlaHandler.ConnectionFactory connectionFactory =
            new XmlaHandler.ConnectionFactory() {
                @Override
				public OlapConnection getConnection(
                    String catalog, String schema, String roleName,
                    Properties props)
                    throws SQLException
                {
                    return connection;
                }

                @Override
				public Map<String, Object>
                getPreConfiguredDiscoverDatasourcesResponse()
                {
                    // This method should not be used by the olap4j xmla
                    // servlet. For the mondrian xmla servlet we don't provide
                    // the "pre configured discover datasources" feature.
                    return Map.of();
                }
            };
        final XmlaRequest request = new XmlaRequest() {
            @Override
			public Method getMethod() {
                return Method.DISCOVER;
            }

            @Override
			public Map<String, String> getProperties() {
                return Collections.emptyMap();
            }

            @Override
			public Map<String, Object> getRestrictions() {
                return restrictionMap;
            }

            @Override
			public String getStatement() {
                return null;
            }

            @Override
			public String getRoleName() {
                return null;
            }

            @Override
			public String getRequestType() {
                throw new UnsupportedOperationException();
            }

            @Override
			public boolean isDrillThrough() {
                throw new UnsupportedOperationException();
            }

            public Format getFormat() {
                throw new UnsupportedOperationException();
            }

            @Override
			public String getUsername() {
                return null;
            }

            @Override
			public String getPassword() {
                return null;
            }

            @Override
			public String getSessionId() {
                return null;
            }
        };
        final Rowset rowset =
            rowsetDefinition.getRowset(
                request,
                new XmlaHandler(
                    connectionFactory,
                    "xmla")
                {
                    @Override
                    public OlapConnection getConnection(
                        XmlaRequest request,
                        Map<String, String> propMap)
                    {
                        return connection;
                    }
                }
            );
        List<Rowset.Row> rowList = new ArrayList<>();
        rowset.populate(
            new DefaultXmlaResponse(
                new ByteArrayOutputStream(),
                Charset.defaultCharset().name(),
                Enumeration.ResponseMimeType.SOAP),
            connection,
            rowList);
        MetadataRowset result = new MetadataRowset();
        final List<RowsetDefinition.Column> colDefs =
            new ArrayList<>();
        for (RowsetDefinition.Column columnDefinition
            : rowsetDefinition.columnDefinitions)
        {
            if (columnDefinition.type == RowsetDefinition.Type.ROW_SET) {
                // olap4j does not support the extended columns, e.g.
                // Cube.Dimensions
                continue;
            }
            colDefs.add(columnDefinition);
        }
        for (Rowset.Row row : rowList) {
            Object[] values = new Object[colDefs.size()];
            int k = -1;
            for (RowsetDefinition.Column colDef : colDefs) {
                Object o = row.get(colDef.name);
                if (o instanceof List) {
                    o = toString((List<String>) o);
                } else if (o instanceof String[] arr) {
                    o = toString(Arrays.asList(arr));
                }
                values[++k] = o;
            }
            result.rowList.add(Arrays.asList(values));
        }
        for (RowsetDefinition.Column colDef : colDefs) {
            String columnName = colDef.name;
            columnName = Util.camelToUpper(columnName);
            // VALUE is a SQL reserved word
            if (columnName.equals("VALUE")) {
                columnName = "PROPERTY_VALUE";
            }
            result.headerList.add(columnName);
        }
        return result;
    }

    private static <T> String toString(List<T> list) {
        StringBuilder buf = new StringBuilder();
        int k = -1;
        for (T t : list) {
            if (++k > 0) {
                buf.append(", ");
            }
            buf.append(t);
        }
        return buf.toString();
    }

    /**
     * Chooses the appropriate response mime type given an HTTP "Accept" header.
     *
     * <p>The header can contain a list of mime types and optional qualities,
     * for example "text/html,application/xhtml+xml,application/xml;q=0.9"
     *
     * @param accept Accept header
     * @return Mime type, or null if none is acceptable
     */
    public static Enumeration.ResponseMimeType chooseResponseMimeType(
        String accept)
    {
        for (String s : accept.split(",")) {
            s = s.trim();
            final int semicolon = s.indexOf(";");
            if (semicolon >= 0) {
                s = s.substring(0, semicolon);
            }
            Enumeration.ResponseMimeType mimeType =
                Enumeration.ResponseMimeType.MAP.get(s);
            if (mimeType != null) {
                return mimeType;
            }
        }
        return null;
    }

    /**
     * Returns whether an XMLA request should return invisible members.
     *
     * <p>According to the XMLA spec, it should not. But we allow the client to
     * specify different behavior. In particular, the olap4j driver for XMLA
     * may need to access invisible members.
     *
     * <p>Returns true if the EmitInvisibleMembers property is specified and
     * equal to "true".
     *
     * @param request XMLA request
     * @return Whether to return invisible members
     */
    public static boolean shouldEmitInvisibleMembers(XmlaRequest request) {
        final String value =
            request.getProperties().get(
                PropertyDefinition.EmitInvisibleMembers.name());
        return Boolean.parseBoolean(value);
    }

    /**
     * Result of a metadata query.
     */
    public static class MetadataRowset {
        public final List<String> headerList = new ArrayList<>();
        public final List<List<Object>> rowList = new ArrayList<>();
    }

    /**
     * Wrapper which indicates that a restriction is to be treated as a
     * SQL-style wildcard match.
     */
    public static class Wildcard {
        public final String pattern;

        public Wildcard(String pattern) {
            this.pattern = pattern;
        }
    }

    public static Locale convertToLocale(String value) {
        if (value != null) {
            try {
                // First check for a numeric locale id (LCID) as used by
                // Windows.
                final short lcid = Short.parseShort(value);
                return LcidLocale.lcidToLocale(lcid);
            } catch (NumberFormatException nfe) {
                // Since value is not a valid LCID, now see whether it is a
                // locale name, e.g. "en_US". This behavior is an
                // extension to the XMLA spec.
                try {
                    return Util.parseLocale(value);
                } catch (RuntimeException re) {
                    // probably a bad locale string; fall through
                }
            } catch (RuntimeException re) {
                // probably a bad locale string; fall through
            }
        }
        return null;
    }

    public static class ElementNameEncoder {
        private final Map<String, String> map =
            new ConcurrentHashMap<>();
        public static final ElementNameEncoder INSTANCE =
            new ElementNameEncoder();

        public String encode(String name) {
            return map.computeIfAbsent(name, XmlaUtil::encodeElementName);
        }
    }
}

/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2002-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/

package mondrian.web.taglib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Holds compiled stylesheets.
 *
 * @author Andreas Voss, 22 March, 2002
 */
public class ApplResources implements Listener.ApplicationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplResources.class);
    private static final String ATTRNAME = "mondrian.web.taglib.ApplResources";
    private ServletContext context;

    /**
     * Creates a <code>ApplResources</code>. Only {@link Listener} calls this;
     * you should probably call {@link #getInstance}.
     */
    public ApplResources() {
        // constructor
    }

    /**
     * Retrieves the one and only instance of <code>ApplResources</code> in
     * this servlet's context.
     */
    public static ApplResources getInstance(ServletContext context) {
        return (ApplResources)context.getAttribute(ATTRNAME);
    }

    private HashMap<String, Templates> templatesCache = new HashMap<>();
    public Transformer getTransformer(String xsltURI, boolean useCache) {
        try {
            Templates templates = null;
            if (useCache) {
                templates = templatesCache.get(xsltURI);
            }
            if (templates == null) {
                TransformerFactory tf = TransformerFactory.newInstance();
                InputStream input = context.getResourceAsStream(xsltURI);
                templates = tf.newTemplates(new StreamSource(input));
                if (useCache) {
                    templatesCache.put(xsltURI, templates);
                }
            }
            return templates.newTransformer();
        } catch (TransformerConfigurationException e) {
            LOGGER.error("getTransformer error");
            throw new RuntimeException(e.toString());
        }
    }

    // implement ApplicationContext
    @Override
	public void init(ServletContextEvent event) {
        this.context = event.getServletContext();
        context.setAttribute(ATTRNAME, this);
    }

    @Override
	public void destroy(ServletContextEvent ev) {
        //destroy empty
    }


}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.bind.v2.util;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.Messages;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Provides helper methods for creating properly configured XML parser 
 * factory instances with namespace support turned on and configured for 
 * security.
 * @author snajper
 */
public class XmlFactory {
    
    public static final Logger logger = Logger.getLogger(XmlFactory.class.getName());
            
    public static final boolean DISABLE_SECURE_PROCESSING = 
            Boolean.parseBoolean(Util.getSystemProperty(JAXBContextImpl.DISABLE_XML_SECURITY));

    private static boolean xmlFeatureValue(boolean runtimeSetting) {
        return !(DISABLE_SECURE_PROCESSING || (!DISABLE_SECURE_PROCESSING && runtimeSetting));
    }

    public static Exception er = new Exception();

    /**
     * Returns properly configured (e.g. security features) schema factory 
     * - namespaceAware == true
     * - securityProcessing == is set based on security processing property, default is true
     */
    public static final SchemaFactory createSchemaFactory(final String language, boolean disableSecureProcessing) throws IllegalStateException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(language);
            if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "SchemaFactory instance: {0}", factory);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, xmlFeatureValue(disableSecureProcessing));
            return factory;
        } catch (SAXNotRecognizedException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        } catch (SAXNotSupportedException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        } catch (AbstractMethodError er) {
            logger.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(), er);
        }
    }

    /**
     * Returns properly configured (e.g. security features) parser factory 
     * - namespaceAware == true
     * - securityProcessing == is set based on security processing property, default is true
     */
    public static final SAXParserFactory createParserFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "SAXParserFactory instance: {0}", factory);
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, xmlFeatureValue(disableSecureProcessing));
            return factory;
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException( ex);
        } catch (SAXNotRecognizedException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException( ex);
        } catch (SAXNotSupportedException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException( ex);
        } catch (AbstractMethodError er) {
            logger.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(), er);
        }
    }

    /**
     * Returns properly configured (e.g. security features) factory 
     * - securityProcessing == is set based on security processing property, default is true
     */
    public static final XPathFactory createXPathFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "XPathFactory instance: {0}", factory);            
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, xmlFeatureValue(disableSecureProcessing));
            return factory;
        } catch (XPathFactoryConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException( ex);
        } catch (AbstractMethodError er) {
            logger.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(), er);
        }
    }

    /**
     * Returns properly configured (e.g. security features) factory 
     * - securityProcessing == is set based on security processing property, default is true
     */
    public static final TransformerFactory createTransformerFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "TransformerFactory instance: {0}", factory);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, xmlFeatureValue(disableSecureProcessing));
            return factory;
        } catch (TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException( ex);
        } catch (AbstractMethodError er) {
            logger.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(), er);
        }
    }
    
    /**
     * Returns properly configured (e.g. security features) factory 
     * - namespaceAware == true
     * - securityProcessing == is set based on security processing property, default is true
     */
    public static final DocumentBuilderFactory createDocumentBuilderFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "DocumentBuilderFactory instance: {0}", factory);
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, xmlFeatureValue(disableSecureProcessing));
            return factory;
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new IllegalStateException( ex);
        } catch (AbstractMethodError er) {
            logger.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(), er);
        }
    }

}

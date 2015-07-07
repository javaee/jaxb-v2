/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import com.sun.xml.bind.v2.util.XmlFactory;
import javax.xml.XMLConstants;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import static com.sun.xml.bind.v2.util.XmlFactory.allowExternalAccess;

/**
 * Wraps a JAXP {@link Schema} object and lazily instantiate it.
 *
 * This object is thread-safe. There should be only one instance of
 * this for the whole VM.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SchemaCache {

    private final boolean createResolver;
    private final String resourceName;
    private final Class<?> clazz;

    private Schema schema;

    public SchemaCache(String resourceName, Class<?> classToResolveResources) {
        this(resourceName, classToResolveResources, false);
    }

    public SchemaCache(String resourceName, Class<?> classToResolveResources, boolean createResolver) {
        this.resourceName = resourceName;
        this.createResolver = createResolver;
        this.clazz = classToResolveResources;
    }

    public ValidatorHandler newValidator() {
        if (schema==null) {
            synchronized (this) {
                if (schema == null) {

                    ResourceResolver resourceResolver = null;
                    try (InputStream is = clazz.getResourceAsStream(resourceName)) {

                        StreamSource source = new StreamSource(is);
                        source.setSystemId(resourceName);
                        // do not disable secure processing - these are well-known schemas

                        SchemaFactory sf = XmlFactory.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI, false);
                        SchemaFactory schemaFactory = allowExternalAccess(sf, "file", false);

                        if (createResolver) {
                            resourceResolver = new ResourceResolver(clazz);
                            schemaFactory.setResourceResolver(resourceResolver);
                        }
                        schema = schemaFactory.newSchema(source);

                    } catch (IOException | SAXException e) {
                        throw new InternalError(e);
                    } finally {
                        if (resourceResolver != null) resourceResolver.closeStreams();
                    }
                }
            }
        }
        return schema.newValidatorHandler();
    }

    class ResourceResolver implements LSResourceResolver {

        private List<InputStream> streamsToClose = Collections.synchronizedList(new ArrayList<InputStream>());
        private Class<?> clazz;

        ResourceResolver(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            // XSOM passes the namespace URI to the publicID parameter.
            // we do the same here .
            InputStream is = clazz.getResourceAsStream(systemId);
            streamsToClose.add(is);
            return new Input(is, publicId, systemId);
        }

        void closeStreams() {
            for (InputStream is : streamsToClose) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // nothing to do ...
                    }
                }
            }
        }
    }

}

class Input implements LSInput {

    private InputStream is;
    private String publicId;
    private String systemId;

    public Input(InputStream is, String publicId, String systemId) {
        this.is = is;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    @Override
    public Reader getCharacterStream() {
        return null;
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
    }

    @Override
    public InputStream getByteStream() {
        return is;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
    }

    @Override
    public String getStringData() {
        return null;
    }

    @Override
    public void setStringData(String stringData) {
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public void setSystemId(String systemId) {
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Override
    public void setBaseURI(String baseURI) {
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public void setEncoding(String encoding) {
    }

    @Override
    public boolean getCertifiedText() {
        return false;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
    }
}



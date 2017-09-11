/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.Util;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Receives SAX events and convert them to our internal events.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SAXConnector implements UnmarshallerHandler {

    private LocatorEx loc;

    private static final Logger logger = Util.getClassLogger();
    
    /**
     * SAX may fire consecutive characters event, but we don't allow it.
     * so use this buffer to perform buffering.
     */
    private final StringBuilder buffer = new StringBuilder();

    private final XmlVisitor next;
    private final UnmarshallingContext context;
    private final XmlVisitor.TextPredictor predictor;

    private static final class TagNameImpl extends TagName {
        String qname;
        @Override
        public String getQname() {
            return qname;
        }
    }

    private final TagNameImpl tagName = new TagNameImpl();

    /**
     * @param externalLocator
     *      If the caller is producing SAX events from sources other than Unicode and angle brackets,
     *      the caller can override the default SAX {@link Locator} object by this object
     *      to provide better location information.
     */
    public SAXConnector(XmlVisitor next, LocatorEx externalLocator ) {
        this.next = next;
        this.context = next.getContext();
        this.predictor = next.getPredictor();
        this.loc = externalLocator;
    }

    @Override
    public Object getResult() throws JAXBException, IllegalStateException {
        return context.getResult();
    }

    public UnmarshallingContext getContext() {
        return context;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        if(loc!=null)
            return; // we already have an external locator. ignore.

        this.loc = new LocatorExWrapper(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startDocument");
        }
        next.startDocument(loc,null);
    }

    @Override
    public void endDocument() throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.endDocument");
        }
        next.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startPrefixMapping: {0}:{1}", new Object[]{prefix, uri});
        }
        next.startPrefixMapping(prefix,uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.endPrefixMapping: {0}", new Object[]{prefix});
        }
        next.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startElement: {0}:{1}:{2}, attrs: {3}", new Object[]{uri, local, qname, atts});
        }
        // work gracefully with misconfigured parsers that don't support namespaces
        if( uri==null || uri.length()==0 )
            uri="";
        if( local==null || local.length()==0 )
            local=qname;
        if( qname==null || qname.length()==0 )
            qname=local;

        processText(!context.getCurrentState().isMixed());

        tagName.uri = uri;
        tagName.local = local;
        tagName.qname = qname;
        tagName.atts = atts;
        next.startElement(tagName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startElement: {0}:{1}:{2}", new Object[]{uri, localName, qName});
        }
        processText(false);
        tagName.uri = uri;
        tagName.local = localName;
        tagName.qname = qName;
        next.endElement(tagName);
    }


    @Override
    public final void characters( char[] buf, int start, int len ) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "SAXConnector.characters: {0}", buf);
        }
        if( predictor.expectText() )
            buffer.append(buf,start,len);
    }

    @Override
    public final void ignorableWhitespace( char[] buf, int start, int len ) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "SAXConnector.characters{0}", buf);
        }
        characters(buf,start,len);
    }

    @Override
    public void processingInstruction(String target, String data) {
        // nop
    }

    @Override
    public void skippedEntity(String name) {
        // nop
    }

    private void processText( boolean ignorable ) throws SAXException {
        if (predictor.expectText() && (!ignorable || !WhiteSpaceProcessor.isWhiteSpace(buffer)))
            next.text(buffer);
        buffer.setLength(0);
    }

}

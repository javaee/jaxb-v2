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

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;

import com.sun.xml.bind.v2.WellKnownNamespace;

import org.xml.sax.SAXException;

/**
 * Decorator of {@link XmlVisitor} that performs XOP processing.
 * Used to support MTOM.
 *
 * @author Kohsuke Kawaguchi
 */
final class MTOMDecorator implements XmlVisitor {

    private final XmlVisitor next;

    private final AttachmentUnmarshaller au;

    private UnmarshallerImpl parent;

    private final Base64Data base64data = new Base64Data();

    /**
     * True if we are between the start and the end of xop:Include
     */
    private boolean inXopInclude;

    /**
     * UGLY HACK: we need to ignore the whitespace that follows
     * the attached base64 image.
     *
     * This happens twice; once before {@code </xop:Include>}, another
     * after {@code </xop:Include>}. The spec guarantees that
     * no valid pcdata can follow {@code </xop:Include>}.
     */
    private boolean followXop;

    public MTOMDecorator(UnmarshallerImpl parent,XmlVisitor next, AttachmentUnmarshaller au) {
        this.parent = parent;
        this.next = next;
        this.au = au;
    }

    public void startDocument(LocatorEx loc, NamespaceContext nsContext) throws SAXException {
        next.startDocument(loc,nsContext);
    }

    public void endDocument() throws SAXException {
        next.endDocument();
    }

    public void startElement(TagName tagName) throws SAXException {
        if(tagName.local.equals("Include") && tagName.uri.equals(WellKnownNamespace.XOP)) {
            // found xop:Include
            String href = tagName.atts.getValue("href");
            DataHandler attachment = au.getAttachmentAsDataHandler(href);
            if(attachment==null) {
                // report an error and ignore
                parent.getEventHandler().handleEvent(null);
                // TODO
            }
            base64data.set(attachment);
            next.text(base64data);
            inXopInclude = true;
            followXop = true;
        } else
            next.startElement(tagName);
    }

    public void endElement(TagName tagName) throws SAXException {
        if(inXopInclude) {
            // consume </xop:Include> by ourselves.
            inXopInclude = false;
            followXop = true;
            return;
        }
        next.endElement(tagName);
    }

    public void startPrefixMapping(String prefix, String nsUri) throws SAXException {
        next.startPrefixMapping(prefix,nsUri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        next.endPrefixMapping(prefix);
    }

    public void text( CharSequence pcdata ) throws SAXException {
        if(!followXop)
            next.text(pcdata);
        else
            followXop = false;
    }

    public UnmarshallingContext getContext() {
        return next.getContext();
    }

    public TextPredictor getPredictor() {
        return next.getPredictor();
    }
}

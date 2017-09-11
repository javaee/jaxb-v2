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

package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;

import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;

import org.xml.sax.SAXException;

/**
 * {@link XmlOutput} decorator that supports MTOM.
 *
 * @author Kohsuke Kawaguchi
 */
public final class MTOMXmlOutput extends XmlOutputAbstractImpl {

    private final XmlOutput next;

    /**
     * Remembers the last namespace URI and local name so that we can pass them to
     * {@link AttachmentMarshaller}.
     */
    private String nsUri,localName;

    public MTOMXmlOutput(XmlOutput next) {
        this.next = next;
    }

    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer,fragment,nsUriIndex2prefixIndex, nsContext);
        next.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
    }

    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        next.endDocument(fragment);
        super.endDocument(fragment);
    }

    public void beginStartTag(Name name) throws IOException, XMLStreamException {
        next.beginStartTag(name);
        this.nsUri = name.nsUri;
        this.localName = name.localName;
    }

    public void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException {
        next.beginStartTag(prefix, localName);
        this.nsUri = nsContext.getNamespaceURI(prefix);
        this.localName = localName;
    }

    public void attribute( Name name, String value ) throws IOException, XMLStreamException {
        next.attribute(name, value);
    }

    public void attribute( int prefix, String localName, String value ) throws IOException, XMLStreamException {
        next.attribute(prefix, localName, value);
    }

    public void endStartTag() throws IOException, SAXException {
        next.endStartTag();
    }

    public void endTag(Name name) throws IOException, SAXException, XMLStreamException {
        next.endTag(name);
    }

    public void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException {
        next.endTag(prefix, localName);
    }

    public void text( String value, boolean needsSeparatingWhitespace ) throws IOException, SAXException, XMLStreamException {
        next.text(value,needsSeparatingWhitespace);
    }

    public void text( Pcdata value, boolean needsSeparatingWhitespace ) throws IOException, SAXException, XMLStreamException {
        if(value instanceof Base64Data && !serializer.getInlineBinaryFlag()) {
            Base64Data b64d = (Base64Data) value;
            String cid;
            if(b64d.hasData())
                cid = serializer.attachmentMarshaller.addMtomAttachment(
                                b64d.get(),0,b64d.getDataLen(),b64d.getMimeType(),nsUri,localName);
            else
                cid = serializer.attachmentMarshaller.addMtomAttachment(
                    b64d.getDataHandler(),nsUri,localName);

            if(cid!=null) {
                nsContext.getCurrent().push();
                int prefix = nsContext.declareNsUri(WellKnownNamespace.XOP,"xop",false);
                beginStartTag(prefix,"Include");
                attribute(-1,"href",cid);
                endStartTag();
                endTag(prefix,"Include");
                nsContext.getCurrent().pop();
                return;
            }
        }
        next.text(value, needsSeparatingWhitespace);
    }
}

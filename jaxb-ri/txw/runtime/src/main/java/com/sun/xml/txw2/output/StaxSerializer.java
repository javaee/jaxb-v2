/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.txw2.output;

import com.sun.xml.txw2.TxwException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML serializer for StAX XMLStreamWriter.
 *
 * TODO: add support for XMLEventWriter (if it makes sense)
 *
 * @author Ryan.Shoemaker@Sun.COM
 */

public class StaxSerializer implements XmlSerializer {
    private final XMLStreamWriter out;

    public StaxSerializer(XMLStreamWriter writer) {
        this(writer,true);
    }

    public StaxSerializer(XMLStreamWriter writer, boolean indenting) {
        if(indenting)
            writer = new IndentingXMLStreamWriter(writer);
        this.out = writer;
    }

    public void startDocument() {
        try {
            out.writeStartDocument();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void beginStartTag(String uri, String localName, String prefix) {
        try {
            out.writeStartElement(prefix, localName, uri);
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void writeAttribute(String uri, String localName, String prefix, StringBuilder value) {
        try {
            out.writeAttribute(prefix, uri, localName, value.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void writeXmlns(String prefix, String uri) {
        try {
            if (prefix.length() == 0) {
                out.setDefaultNamespace(uri);
            } else {
                out.setPrefix(prefix, uri);
            }

            // this method handles "", null, and "xmlns" prefixes properly
            out.writeNamespace(prefix, uri);
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void endStartTag(String uri, String localName, String prefix) {
        // NO-OP
    }

    public void endTag() {
        try {
            out.writeEndElement();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void text(StringBuilder text) {
        try {
            out.writeCharacters(text.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void cdata(StringBuilder text) {
        try {
            out.writeCData(text.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void comment(StringBuilder comment) {
        try {
            out.writeComment(comment.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void endDocument() {
        try {
            out.writeEndDocument();
            out.flush();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void flush() {
        try {
            out.flush();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }
}

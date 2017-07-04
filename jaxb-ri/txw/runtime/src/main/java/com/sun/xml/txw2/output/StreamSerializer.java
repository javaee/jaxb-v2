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

import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;

/**
 * {@link XmlSerializer} for {@link javax.xml.transform.stream.StreamResult}.
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public class StreamSerializer implements XmlSerializer {

    // delegate to SaxSerializer
    private final SaxSerializer serializer;

    private final XMLWriter writer;

    public StreamSerializer(OutputStream out) {
        this(createWriter(out));
    }

    public StreamSerializer(OutputStream out,String encoding) throws UnsupportedEncodingException {
        this(createWriter(out,encoding));
    }

    public StreamSerializer(Writer out) {
        this(new StreamResult(out));
    }

    public StreamSerializer(StreamResult streamResult) {
        // if this method opened a stream, let it close it
        final OutputStream[] autoClose = new OutputStream[1];

        if (streamResult.getWriter() != null)
            writer = createWriter(streamResult.getWriter());
        else if (streamResult.getOutputStream() != null)
            writer = createWriter(streamResult.getOutputStream());
        else if (streamResult.getSystemId() != null) {
            String fileURL = streamResult.getSystemId();

            fileURL = convertURL(fileURL);

            try {
                FileOutputStream fos = new FileOutputStream(fileURL);
                autoClose[0] = fos;
                writer = createWriter(fos);
            } catch (IOException e) {
                throw new TxwException(e);
            }
        } else
            throw new IllegalArgumentException();

        // now delegate to the SaxSerializer
        serializer = new SaxSerializer(writer,writer,false) {
            public void endDocument() {
                super.endDocument();
                if(autoClose[0]!=null) {
                    try {
                        autoClose[0].close();
                    } catch (IOException e) {
                        throw new TxwException(e);
                    }
                    autoClose[0] = null;
                }
            }
        };
    }

    private StreamSerializer(XMLWriter writer) {
        this.writer = writer;
        // now delegate to the SaxSerializer
        serializer = new SaxSerializer(writer,writer,false);
    }

    private String convertURL(String url) {
        url = url.replace('\\', '/');
        url = url.replaceAll("//","/");
        url = url.replaceAll("//","/");
        if (url.startsWith("file:/")) {
            if (url.substring(6).indexOf(":") > 0)
                url = url.substring(6);
            else
                url = url.substring(5);
        } // otherwise assume that it's a file name
        return url;
    }

    // XmlSerializer api's - delegate to SaxSerializer
    public void startDocument() {
        serializer.startDocument();
    }

    public void beginStartTag(String uri, String localName, String prefix) {
        serializer.beginStartTag(uri, localName, prefix);
    }

    public void writeAttribute(String uri, String localName, String prefix, StringBuilder value) {
        serializer.writeAttribute(uri, localName, prefix, value);
    }

    public void writeXmlns(String prefix, String uri) {
        serializer.writeXmlns(prefix, uri);
    }

    public void endStartTag(String uri, String localName, String prefix) {
        serializer.endStartTag(uri, localName, prefix);
    }

    public void endTag() {
        serializer.endTag();
    }

    public void text(StringBuilder text) {
        serializer.text(text);
    }

    public void cdata(StringBuilder text) {
        serializer.cdata(text);
    }

    public void comment(StringBuilder comment) {
        serializer.comment(comment);
    }

    public void endDocument() {
        serializer.endDocument();
    }

    public void flush() {
        serializer.flush();
        try {
            writer.flush();
        } catch (IOException e) {
            throw new TxwException(e);
        }
    }

    // other supporting code
    private static XMLWriter createWriter(Writer w) {
        // buffering improves the performance
        DataWriter dw = new DataWriter(new BufferedWriter(w));
        dw.setIndentStep("  ");
        return dw;
    }

    private static XMLWriter createWriter(OutputStream os, String encoding) throws UnsupportedEncodingException {
        XMLWriter writer = createWriter(new OutputStreamWriter(os,encoding));
        writer.setEncoding(encoding);
        return writer;
    }

    private static XMLWriter createWriter(OutputStream os) {
        try {
            return createWriter(os,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is supported on all platforms.
            throw new Error(e);
        }
    }

}

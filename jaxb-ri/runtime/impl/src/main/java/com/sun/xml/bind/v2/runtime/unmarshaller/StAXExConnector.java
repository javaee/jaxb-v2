/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jvnet.staxex.XMLStreamReaderEx;
import org.xml.sax.SAXException;

/**
 * Reads XML from StAX {@link XMLStreamReader} and
 * feeds events to {@link XmlVisitor}.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @author Kohsuke Kawaguchi
 * @version JAXB 2.0
 */
final class StAXExConnector extends StAXStreamConnector {

    // StAX event source
    private final XMLStreamReaderEx in;

    public StAXExConnector(XMLStreamReaderEx in, XmlVisitor visitor) {
        super(in,visitor);
        this.in = in;
    }

    @Override
    protected void handleCharacters() throws XMLStreamException, SAXException {
        if( predictor.expectText() ) {
            CharSequence pcdata = in.getPCDATA();
            if(pcdata instanceof org.jvnet.staxex.Base64Data) {
                org.jvnet.staxex.Base64Data bd = (org.jvnet.staxex.Base64Data) pcdata;
                Base64Data binary = new Base64Data();
                if(!bd.hasData())
                    binary.set(bd.getDataHandler());
                else
                    binary.set( bd.get(), bd.getDataLen(), bd.getMimeType() );
                // we make an assumption here that the binary data shows up on its own
                // not adjacent to other text. So it's OK to fire it off right now.
                visitor.text(binary);
                textReported = true;
            } else {
                buffer.append(pcdata);
            }
        }
    }
}

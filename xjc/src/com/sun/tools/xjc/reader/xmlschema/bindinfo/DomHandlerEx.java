/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import com.sun.xml.bind.marshaller.SAX2DOMEx;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * {@link DomHandler} that produces a W3C DOM but with a location information.
 *
 * @author Kohsuke Kawaguchi
 */
final class DomHandlerEx implements DomHandler<DomHandlerEx.DomAndLocation,DomHandlerEx.ResultImpl> {

    public static final class DomAndLocation {
        public final Element element;
        public final Locator loc;

        public DomAndLocation(Element element, Locator loc) {
            this.element = element;
            this.loc = loc;
        }
    }

    public ResultImpl createUnmarshaller(ValidationEventHandler errorHandler) {
        return new ResultImpl();
    }

    public DomAndLocation getElement(ResultImpl r) {
        return new DomAndLocation( ((Document)r.s2d.getDOM()).getDocumentElement(), r.location );
    }

    public Source marshal(DomAndLocation domAndLocation, ValidationEventHandler errorHandler) {
        return new DOMSource(domAndLocation.element);
    }

    public static final class ResultImpl extends SAXResult {
        final SAX2DOMEx s2d;

        Locator location = null;

        ResultImpl() {
            try {
                s2d = new SAX2DOMEx();
            } catch (ParserConfigurationException e) {
                throw new AssertionError(e);    // impossible
            }

            XMLFilterImpl f = new XMLFilterImpl() {
                public void setDocumentLocator(Locator locator) {
                    super.setDocumentLocator(locator);
                    location = new LocatorImpl(locator);
                }
            };
            f.setContentHandler(s2d);

            setHandler(f);
        }

    }
}

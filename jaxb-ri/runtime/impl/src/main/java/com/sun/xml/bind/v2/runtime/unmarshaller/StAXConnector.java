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

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class StAXConnector {
    public abstract void bridge() throws XMLStreamException;


    // event sink
    protected final XmlVisitor visitor;

    protected final UnmarshallingContext context;
    protected final XmlVisitor.TextPredictor predictor;

    private final class TagNameImpl extends TagName {
        public String getQname() {
            return StAXConnector.this.getCurrentQName();
        }
    }

    protected final TagName tagName = new TagNameImpl();

    protected StAXConnector(XmlVisitor visitor) {
        this.visitor = visitor;
        context = visitor.getContext();
        predictor = visitor.getPredictor();
    }

    /**
     * Gets the {@link Location}. Used for implementing the line number information.
     * @return must not null.
     */
    protected abstract Location getCurrentLocation();

    /**
     * Gets the QName of the current element.
     */
    protected abstract String getCurrentQName();

    protected final void handleStartDocument(NamespaceContext nsc) throws SAXException {
        visitor.startDocument(new LocatorEx() {
            public ValidationEventLocator getLocation() {
                return new ValidationEventLocatorImpl(this);
            }
            public int getColumnNumber() {
                return getCurrentLocation().getColumnNumber();
            }
            public int getLineNumber() {
                return getCurrentLocation().getLineNumber();
            }
            public String getPublicId() {
                return getCurrentLocation().getPublicId();
            }
            public String getSystemId() {
                return getCurrentLocation().getSystemId();
            }
        },nsc);
    }

    protected final void handleEndDocument() throws SAXException {
        visitor.endDocument();
    }

    protected static String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }

    protected final String getQName(String prefix, String localName) {
        if(prefix==null || prefix.length()==0)
            return localName;
        else
            return prefix + ':' + localName;
    }
}

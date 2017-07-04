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

package com.sun.xml.txw2;

import com.sun.xml.txw2.output.XmlSerializer;
import com.sun.xml.txw2.output.TXWSerializer;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.annotation.XmlNamespace;

import javax.xml.namespace.QName;

/**
 * Entry point to TXW.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TXW {
    private TXW() {}    // no instanciation please


    /*package*/ static QName getTagName( Class<?> c ) {
        String localName="";
        String nsUri="##default";

        XmlElement xe = c.getAnnotation(XmlElement.class);
        if(xe!=null) {
            localName = xe.value();
            nsUri = xe.ns();
        }

        if(localName.length()==0) {
            localName = c.getName();
            int idx = localName.lastIndexOf('.');
            if(idx>=0)
                localName = localName.substring(idx+1);

            localName = Character.toLowerCase(localName.charAt(0))+localName.substring(1);
        }

        if(nsUri.equals("##default")) {
            Package pkg = c.getPackage();
            if(pkg!=null) {
                XmlNamespace xn = pkg.getAnnotation(XmlNamespace.class);
                if(xn!=null)
                    nsUri = xn.value();
            }
        }
        if(nsUri.equals("##default"))
            nsUri = "";

        return new QName(nsUri,localName);
    }

    /**
     * Creates a new {@link TypedXmlWriter} to write a new instance of a document.
     *
     * @param rootElement
     *      The {@link TypedXmlWriter} interface that declares the content model of the root element.
     *      This interface must have {@link XmlElement} annotation on it to designate the tag name
     *      of the root element.
     * @param out
     *      The target of the writing.
     */
    public static <T extends TypedXmlWriter> T create( Class<T> rootElement, XmlSerializer out ) {
        if (out instanceof TXWSerializer) {
            TXWSerializer txws = (TXWSerializer) out;
            return txws.txw._element(rootElement);
        }

        Document doc = new Document(out);
        QName n = getTagName(rootElement);
        return new ContainerElement(doc,null,n.getNamespaceURI(),n.getLocalPart())._cast(rootElement);
    }

    /**
     * Creates a new {@link TypedXmlWriter} to write a new instance of a document.
     *
     * <p>
     * Similar to the other method, but this version allows the caller to set the
     * tag name at the run-time.
     *
     * @param tagName
     *      The tag name of the root document.
     *
     * @see #create(Class,XmlSerializer)
     */
    public static <T extends TypedXmlWriter> T create( QName tagName, Class<T> rootElement, XmlSerializer out ) {
        if (out instanceof TXWSerializer) {
            TXWSerializer txws = (TXWSerializer) out;
            return txws.txw._element(tagName,rootElement);
        }
        return new ContainerElement(new Document(out),null,tagName.getNamespaceURI(),tagName.getLocalPart())._cast(rootElement);
    }
}

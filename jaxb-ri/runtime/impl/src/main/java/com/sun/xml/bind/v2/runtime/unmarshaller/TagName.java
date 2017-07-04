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

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.runtime.Name;

import org.xml.sax.Attributes;

/**
 * Represents an XML tag name (and attributes for start tags.)
 *
 * <p>
 * This object is used so reduce the number of method call parameters
 * among unmarshallers.
 *
 * An instance of this is expected to be reused by the caller of
 * {@link XmlVisitor}. Note that the rest of the unmarshaller may
 * modify any of the fields while processing an event (such as to
 * intern strings, replace attributes),
 * so {@link XmlVisitor} should reset all fields for each use.
 *
 * <p>
 * The 'qname' parameter, which holds the qualified name of the tag
 * (such as 'foo:bar' or 'zot'), is not used in the typical unmarshalling
 * route and it's also expensive to compute for some input.
 * Thus this parameter is computed lazily.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"StringEquality"})
public abstract class TagName {
    /**
     * URI of the attribute/element name.
     *
     * Can be empty, but never null. Interned.
     */
    public String uri;
    /**
     * Local part of the attribute/element name.
     *
     * Never be null. Interned.
     */
    public String local;

    /**
     * Used only for the enterElement event.
     * Otherwise the value is undefined.
     *
     * This might be {@link AttributesEx}.
     */
    public Attributes atts;

    public TagName() {
    }

    /**
     * Checks if the given name pair matches this name.
     */
    public final boolean matches( String nsUri, String local ) {
        return this.uri==nsUri && this.local==local;
    }

    /**
     * Checks if the given name pair matches this name.
     */
    public final boolean matches( Name name ) {
        return this.local==name.localName && this.uri==name.nsUri;
    }

//    /**
//     * @return
//     *      Can be empty but always non-null. NOT interned.
//     */
//    public final String getPrefix() {
//        int idx = qname.indexOf(':');
//        if(idx<0)   return "";
//        else        return qname.substring(0,idx);
//    }

    public String toString() {
        return '{'+uri+'}'+local;
    }

    /**
     * Gets the qualified name of the tag.
     *
     * @return never null.
     */
    public abstract String getQname();

    /**
     * Gets the prefix. This is slow.
     *
     * @return can be "" but never null.
     */
    public String getPrefix() {
        String qname = getQname();
        int idx = qname.indexOf(':');
        if(idx<0)   return "";
        else        return qname.substring(0,idx);
    }

    /**
     * Creates {@link QName}.
     */
    public QName createQName() {
        return new QName(uri,local,getPrefix());
    }
}

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

package com.sun.xml.bind.v2.runtime;

import javax.xml.namespace.QName;

/**
 * The internal representation of an XML name.
 *
 * <p>
 * This class keeps indicies for URI and local name for enabling faster processing.
 *
 * <p>
 * {@link Name}s are ordered lexicographically (nsUri first, local name next.)
 * This is the same order required by canonical XML.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Name implements Comparable<Name> {
    /**
     * Namespace URI. interned.
     */
    public final String nsUri;

    /**
     * Local name. interned.
     */
    public final String localName;

    /**
     * Index -1 is reserved for representing the empty namespace URI of attributes.
     */
    public final short nsUriIndex;
    public final short localNameIndex;

    /**
     * Index of the Name for an EII or AII
     */
    public final short qNameIndex;
            
    /**
     * Specifies if the Name is associated with an EII or AII
     */
    public final boolean isAttribute;
    
    Name(int qNameIndex, int nsUriIndex, String nsUri, int localIndex, String localName, boolean isAttribute) {
        this.qNameIndex = (short)qNameIndex;
        this.nsUri = nsUri;
        this.localName = localName;
        this.nsUriIndex = (short)nsUriIndex;
        this.localNameIndex = (short)localIndex;
        this.isAttribute = isAttribute;
    }

    public String toString() {
        return '{'+nsUri+'}'+localName;
    }

    /**
     * Creates a {@link QName} from this.
     */
    public QName toQName() {
        return new QName(nsUri,localName);
    }

    public boolean equals( String nsUri, String localName ) {
        return localName.equals(this.localName) && nsUri.equals(this.nsUri);
    }

    public int compareTo(Name that) {
        int r = this.nsUri.compareTo(that.nsUri);
        if(r!=0)    return r;
        return this.localName.compareTo(that.localName);
    }
}

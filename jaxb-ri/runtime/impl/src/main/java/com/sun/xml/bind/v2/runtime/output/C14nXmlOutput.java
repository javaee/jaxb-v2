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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

/**
 * {@link XmlOutput} that generates canonical XML.
 *
 * @author Kohsuke Kawaguchi
 */
public class C14nXmlOutput extends UTF8XmlOutput {
    public C14nXmlOutput(OutputStream out, Encoded[] localNames, boolean namedAttributesAreOrdered, CharacterEscapeHandler escapeHandler) {
        super(out, localNames, escapeHandler);
        this.namedAttributesAreOrdered = namedAttributesAreOrdered;

        for( int i=0; i<staticAttributes.length; i++ )
            staticAttributes[i] = new StaticAttribute();
    }

    /**
     * Hosts statically known attributes.
     *
     * {@link StaticAttribute} instances are reused.
     */
    private StaticAttribute[] staticAttributes = new StaticAttribute[8];
    private int len = 0;

    /**
     * Used to sort namespace declarations. Reused.
     */
    private int[] nsBuf = new int[8];

    /**
     * Hosts other attributes whose name are not statically known
     * (AKA attribute wildcard.)
     *
     * As long as this map is empty, there's no need for sorting.
     */
    private final FinalArrayList<DynamicAttribute> otherAttributes = new FinalArrayList<DynamicAttribute>();

    /**
     * True if {@link JAXBRIContext} is created with c14n support on,
     * in which case all named attributes are sorted by the marshaller
     * and we won't have to do it here.
     */
    private final boolean namedAttributesAreOrdered;

    final class StaticAttribute implements Comparable<StaticAttribute> {
        Name name;
        String value;

        public void set(Name name, String value) {
            this.name = name;
            this.value = value;
        }

        void write() throws IOException {
            C14nXmlOutput.super.attribute(name,value);
        }

        DynamicAttribute toDynamicAttribute() {
            int nsUriIndex = name.nsUriIndex;
            int prefix;
            if(nsUriIndex==-1)
                prefix = -1;
            else
                prefix = nsUriIndex2prefixIndex[nsUriIndex];
            return new DynamicAttribute(
                prefix, name.localName, value );
        }

        public int compareTo(StaticAttribute that) {
            return this.name.compareTo(that.name);
        }

    }

    final class DynamicAttribute implements Comparable<DynamicAttribute> {
        final int prefix;
        final String localName;
        final String value;

        public DynamicAttribute(int prefix, String localName, String value) {
            this.prefix = prefix;
            this.localName = localName;
            this.value = value;
        }

        private String getURI() {
            if(prefix==-1)  return "";
            else            return nsContext.getNamespaceURI(prefix);
        }

        public int compareTo(DynamicAttribute that) {
            int r = this.getURI().compareTo(that.getURI());
            if(r!=0)    return r;
            return this.localName.compareTo(that.localName);
        }
    }

    @Override
    public void attribute(Name name, String value) throws IOException {
        if(staticAttributes.length==len) {
            // reallocate
            int newLen = len*2;
            StaticAttribute[] newbuf = new StaticAttribute[newLen];
            System.arraycopy(staticAttributes,0,newbuf,0,len);
            for(int i=len;i<newLen;i++)
                staticAttributes[i] = new StaticAttribute();
            staticAttributes = newbuf;
        }

        staticAttributes[len++].set(name,value);
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException {
        otherAttributes.add(new DynamicAttribute(prefix,localName,value));
    }

    @Override
    public void endStartTag() throws IOException {
        if(otherAttributes.isEmpty()) {
            if(len!=0) {
                // sort is expensive even for size 0 array,
                // so it's worth checking len==0
                if(!namedAttributesAreOrdered)
                    Arrays.sort(staticAttributes,0,len);
                // this is the common case
                for( int i=0; i<len; i++ )
                    staticAttributes[i].write();
                len = 0;
            }
        } else {
            // this is the exceptional case

            // sort all the attributes, not just the other attributes
            for( int i=0; i<len; i++ )
                otherAttributes.add(staticAttributes[i].toDynamicAttribute());
            len = 0;
            Collections.sort(otherAttributes);

            // write them all
            int size = otherAttributes.size();
            for( int i=0; i<size; i++ ) {
                DynamicAttribute a = otherAttributes.get(i);
                super.attribute(a.prefix,a.localName,a.value);
            }
            otherAttributes.clear();
        }
        super.endStartTag();
    }

    /**
     * Write namespace declarations after sorting them.
     */
    @Override
    protected void writeNsDecls(int base) throws IOException {
        int count = nsContext.getCurrent().count();

        if(count==0)
            return; // quickly reject the most common case

        if(count>nsBuf.length)
            nsBuf = new int[count];

        for( int i=count-1; i>=0; i-- )
            nsBuf[i] = base+i;

        // do a bubble sort. Hopefully # of ns decls are small enough to justify bubble sort.
        // faster algorithm is more compliated to implement
        for( int i=0; i<count; i++ ) {
            for( int j=i+1; j<count; j++ ) {
                String p = nsContext.getPrefix(nsBuf[i]);
                String q = nsContext.getPrefix(nsBuf[j]);
                if( p.compareTo(q) > 0 ) {
                    // swap
                    int t = nsBuf[j];
                    nsBuf[j] = nsBuf[i];
                    nsBuf[i] = t;
                }
            }
        }

        // write them out
        for( int i=0; i<count; i++ )
            writeNsDecl(nsBuf[i]);
    }
}

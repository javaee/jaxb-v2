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

import java.util.Map;
import java.util.HashMap;

/**
 * Coordinates the entire writing process.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class Document {

    private final XmlSerializer out;

    /**
     * Set to true once we invoke {@link XmlSerializer#startDocument()}.
     *
     * <p>
     * This is so that we can defer the writing as much as possible.
     */
    private boolean started=false;

    /**
     * Currently active writer.
     *
     * <p>
     * This points to the last written token.
     */
    private Content current = null;

    private final Map<Class,DatatypeWriter> datatypeWriters = new HashMap<Class,DatatypeWriter>();

    /**
     * Used to generate unique namespace prefix.
     */
    private int iota = 1;

    /**
     * Used to keep track of in-scope namespace bindings declared in ancestors.
     */
    private final NamespaceSupport inscopeNamespace = new NamespaceSupport();

    /**
     * Remembers the namespace declarations of the last unclosed start tag,
     * so that we can fix up dummy prefixes in {@link Pcdata}.
     */
    private NamespaceDecl activeNamespaces;


    Document(XmlSerializer out) {
        this.out = out;
        for( DatatypeWriter dw : DatatypeWriter.BUILTIN )
            datatypeWriters.put(dw.getType(),dw);
    }

    void flush() {
        out.flush();
    }

    void setFirstContent(Content c) {
        assert current==null;
        current = new StartDocument();
        current.setNext(this,c);
    }

    /**
     * Defines additional user object {@code ->} string conversion logic.
     *
     * <p>
     * Applications can add their own {@link DatatypeWriter} so that
     * application-specific objects can be turned into {@link String}
     * for output.
     *
     * @param dw
     *      The {@link DatatypeWriter} to be added. Must not be null.
     */
    public void addDatatypeWriter( DatatypeWriter<?> dw ) {
        datatypeWriters.put(dw.getType(),dw);
    }

    /**
     * Performs the output as much as possible
     */
    void run() {
        while(true) {
            Content next = current.getNext();
            if(next==null || !next.isReadyToCommit())
                return;
            next.accept(visitor);
            next.written();
            current = next;
        }
    }

    /**
     * Appends the given object to the end of the given buffer.
     *
     * @param nsResolver
     *      use
     */
    void writeValue( Object obj, NamespaceResolver nsResolver, StringBuilder buf ) {
        if(obj==null)
            throw new IllegalArgumentException("argument contains null");

        if(obj instanceof Object[]) {
            for( Object o : (Object[])obj )
                writeValue(o,nsResolver,buf);
            return;
        }
        if(obj instanceof Iterable) {
            for( Object o : (Iterable<?>)obj )
                writeValue(o,nsResolver,buf);
            return;
        }

        if(buf.length()>0)
            buf.append(' ');

        Class c = obj.getClass();
        while(c!=null) {
            DatatypeWriter dw = datatypeWriters.get(c);
            if(dw!=null) {
                dw.print(obj,nsResolver,buf);
                return;
            }
            c = c.getSuperclass();
        }

        // if nothing applies, just use toString
        buf.append(obj);
    }

    // I wanted to hide those write method from users
    private final ContentVisitor visitor = new ContentVisitor() {
        public void onStartDocument() {
            // the startDocument token is used as the sentry, so this method shall never
            // be called.
            // out.startDocument() is invoked when we write the start tag of the root element.
            throw new IllegalStateException();
        }

        public void onEndDocument() {
            out.endDocument();
        }

        public void onEndTag() {
            out.endTag();
            inscopeNamespace.popContext();
            activeNamespaces = null;
        }

        public void onPcdata(StringBuilder buffer) {
            if(activeNamespaces!=null)
                buffer = fixPrefix(buffer);
            out.text(buffer);
        }

        public void onCdata(StringBuilder buffer) {
            if(activeNamespaces!=null)
                buffer = fixPrefix(buffer);
            out.cdata(buffer);
        }

        public void onComment(StringBuilder buffer) {
            if(activeNamespaces!=null)
                buffer = fixPrefix(buffer);
            out.comment(buffer);
        }

        public void onStartTag(String nsUri, String localName, Attribute attributes, NamespaceDecl namespaces) {
            assert nsUri!=null;
            assert localName!=null;

            activeNamespaces = namespaces;

            if(!started) {
                started = true;
                out.startDocument();
            }

            inscopeNamespace.pushContext();

            // declare the explicitly bound namespaces
            for( NamespaceDecl ns=namespaces; ns!=null; ns=ns.next ) {
                ns.declared = false;    // reset this flag

                if(ns.prefix!=null) {
                    String uri = inscopeNamespace.getURI(ns.prefix);
                    if(uri!=null && uri.equals(ns.uri))
                        ; // already declared
                    else {
                        // declare this new binding
                        inscopeNamespace.declarePrefix(ns.prefix,ns.uri);
                        ns.declared = true;
                    }
                }
            }

            // then use in-scope namespace to assign prefixes to others
            for( NamespaceDecl ns=namespaces; ns!=null; ns=ns.next ) {
                if(ns.prefix==null) {
                    if(inscopeNamespace.getURI("").equals(ns.uri))
                        ns.prefix="";
                    else {
                        String p = inscopeNamespace.getPrefix(ns.uri);
                        if(p==null) {
                            // assign a new one
                            while(inscopeNamespace.getURI(p=newPrefix())!=null)
                                ;
                            ns.declared = true;
                            inscopeNamespace.declarePrefix(p,ns.uri);
                        }
                        ns.prefix = p;
                    }
                }
            }

            // the first namespace decl must be the one for the element
            assert namespaces.uri.equals(nsUri);
            assert namespaces.prefix!=null : "a prefix must have been all allocated";
            out.beginStartTag(nsUri,localName,namespaces.prefix);

            // declare namespaces
            for( NamespaceDecl ns=namespaces; ns!=null; ns=ns.next ) {
                if(ns.declared)
                    out.writeXmlns( ns.prefix, ns.uri );
            }

            // writeBody attributes
            for( Attribute a=attributes; a!=null; a=a.next) {
                String prefix;
                if(a.nsUri.length()==0) prefix="";
                else                    prefix=inscopeNamespace.getPrefix(a.nsUri);
                out.writeAttribute( a.nsUri, a.localName, prefix, fixPrefix(a.value) );
            }

            out.endStartTag(nsUri,localName,namespaces.prefix);
        }
    };

    /**
     * Used by {@link #newPrefix()}.
     */
    private final StringBuilder prefixSeed = new StringBuilder("ns");

    private int prefixIota = 0;

    /**
     * Allocates a new unique prefix.
     */
    private String newPrefix() {
        prefixSeed.setLength(2);
        prefixSeed.append(++prefixIota);
        return prefixSeed.toString();
    }

    /**
     * Replaces dummy prefixes in the value to the real ones
     * by using {@link #activeNamespaces}.
     *
     * @return
     *      the buffer passed as the {@code buf} parameter.
     */
    private StringBuilder fixPrefix(StringBuilder buf) {
        assert activeNamespaces!=null;

        int i;
        int len=buf.length();
        for(i=0;i<len;i++)
            if( buf.charAt(i)==MAGIC )
                break;
        // typically it doens't contain any prefix.
        // just return the original buffer in that case
        if(i==len)
            return buf;

        while(i<len) {
            char uriIdx = buf.charAt(i+1);
            NamespaceDecl ns = activeNamespaces;
            while(ns!=null && ns.uniqueId!=uriIdx)
                ns=ns.next;
            if(ns==null)
                throw new IllegalStateException("Unexpected use of prefixes "+buf);

            int length = 2;
            String prefix = ns.prefix;
            if(prefix.length()==0) {
                if(buf.length()<=i+2 || buf.charAt(i+2)!=':')
                    throw new IllegalStateException("Unexpected use of prefixes "+buf);
                length=3;
            }

            buf.replace(i,i+length,prefix);
            len += prefix.length()-length;

            while(i<len && buf.charAt(i)!=MAGIC)
                i++;
        }

        return buf;
    }

    /**
     * The first char of the dummy prefix.
     */
    static final char MAGIC = '\u0000';

    char assignNewId() {
        return (char)iota++;
    }
}

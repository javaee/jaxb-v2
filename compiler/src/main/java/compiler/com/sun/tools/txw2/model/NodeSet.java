/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.txw2.model;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.tools.txw2.NameUtil;
import com.sun.tools.txw2.TxwOptions;
import com.sun.xml.txw2.annotation.XmlNamespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Root of the model.
 * 
 * @author Kohsuke Kawaguchi
 */
public class NodeSet extends LinkedHashSet<WriterNode> {

    /*package*/ final TxwOptions opts;
    /*package*/ final JCodeModel codeModel;

    /**
     * Set of all the {@link Element}s that can be root.
     */
    private final Set<Element> rootElements = new HashSet<Element>();

    /** The namespace URI declared in {@link XmlNamespace}. */
    /*package*/ final String defaultNamespace;

    public NodeSet(TxwOptions opts, Leaf entry) {
        this.opts = opts;
        this.codeModel = opts.codeModel;
        addAll(entry.siblings());
        markRoot(entry.siblings(),rootElements);

        // decide what to put in @XmlNamespace
        Set<String> ns = new HashSet<String>();
        for( Element e : rootElements )
            ns.add(e.name.getNamespaceURI());

        if(ns.size()!=1 || opts.noPackageNamespace || opts._package.isUnnamed())
            defaultNamespace = null;
        else {
            defaultNamespace = ns.iterator().next();

            opts._package.annotate(XmlNamespace.class)
                .param("value",defaultNamespace);
        }
    }

    /**
     * Marks all the element children as root.
     */
    private void markRoot(Iterable<Leaf> c, Set<Element> rootElements) {
        for( Leaf l : c ) {
            if( l instanceof Element ) {
                Element e = (Element)l;
                rootElements.add(e);
                e.isRoot = true;
            }
            if( l instanceof Ref ) {
                markRoot(((Ref)l).def,rootElements);
            }
        }
    }

    private void addAll(Iterable<Leaf> c) {
        for( Leaf l : c ) {
            if(l instanceof Element)
                if(add((Element)l))
                    addAll((Element)l);
            if(l instanceof Grammar) {
                Grammar g = (Grammar)l;
                for( Define d : g.getDefinitions() )
                    add(d);
            }
            if(l instanceof Ref) {
                Ref r = (Ref)l;
                Define def = r.def;
//                if(def instanceof Grammar) {
//                    for( Define d : ((Grammar)def).getDefinitions() )
//                        if(add(d))
//                            addAll(d);
//                }
                add(def);
            }
        }
    }

    private boolean add(Define def) {
        boolean b = super.add(def);
        if(b)
            addAll(def);
        return b;
    }

    public <T extends WriterNode> Collection<T> subset(Class<T> t) {
        ArrayList<T> r = new ArrayList<T>(size());
        for( WriterNode n : this )
            if(t.isInstance(n))
                r.add((T)n);
        return r;
    }

    /**
     * Generate code
     */
    public void write(TxwOptions opts) {
        for( WriterNode n : this )
            n.prepare(this);
        for( WriterNode n : this )
            n.declare(this);
        for( WriterNode n : this )
            n.generate(this);
    }

    /*package*/ final JDefinedClass createClass(String name) {
        try {
            return opts._package._class(
                JMod.PUBLIC, NameUtil.toClassName(name), ClassType.INTERFACE );
        } catch (JClassAlreadyExistsException e) {
            for( int i=2; true; i++ ) {
                try {
                    return opts._package._class(
                        JMod.PUBLIC, NameUtil.toClassName(name+String.valueOf(i)), ClassType.INTERFACE );
                } catch (JClassAlreadyExistsException e1) {
                    ; // continue
                }
            }
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.txw2.model.prop.Prop;
import com.sun.tools.txw2.model.prop.ValueProp;
import com.sun.xml.txw2.annotation.XmlValue;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.xml.sax.Locator;

import java.util.Iterator;
import java.util.Set;

/**
 * {@link Leaf}s form a set (by a cyclic doubly-linked list.)
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Leaf implements ParsedPattern {
    private Leaf next;
    private Leaf prev;

    /**
     * Source location where this leaf was defined.
     */
    public Locator location;

    protected Leaf(Locator location) {
        this.location = location;
        prev = next = this;
    }

    public final Leaf getNext() {
        assert next!=null;
        assert next.prev == this;
        return next;
    }

    public final Leaf getPrev() {
        assert prev!=null;
        assert prev.next == this;
        return prev;
    }

    /**
     * Combines two sets into one set.
     *
     * @return this
     */
    public final Leaf merge(Leaf that) {
        Leaf n1 = this.next;
        Leaf n2 = that.next;

        that.next = n1;
        that.next.prev = that;
        this.next = n2;
        this.next.prev = this;

        return this;
    }

    /**
     * Returns the collection of all the siblings
     * (including itself)
     */
    public final Iterable<Leaf> siblings() {
        return new Iterable<Leaf>() {
            public Iterator<Leaf> iterator() {
                return new CycleIterator(Leaf.this);
            }
        };
    }

    /**
     * Populate the body of the writer class.
     *
     * @param props
     *      captures the generatesd {@link Prop}s to
     */
    abstract void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props);




    /**
     * Creates a prop of the data value method.
     */
    protected final void createDataMethod(JDefinedClass clazz, JType valueType, NodeSet nset, Set<Prop> props) {
        if(!props.add(new ValueProp(valueType)))
            return;

        JMethod m = clazz.method(JMod.PUBLIC,
            nset.opts.chainMethod? (JType)clazz : nset.codeModel.VOID,
            "_text");
        m.annotate(XmlValue.class);
        m.param(valueType,"value");
    }
}

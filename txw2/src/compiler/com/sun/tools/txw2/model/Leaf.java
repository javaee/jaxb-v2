/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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

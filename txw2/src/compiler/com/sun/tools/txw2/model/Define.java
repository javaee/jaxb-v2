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
import com.sun.tools.txw2.model.prop.Prop;
import com.sun.xml.txw2.TypedXmlWriter;

import java.util.HashSet;
import java.util.Set;


/**
 * A named pattern.
 *
 * @author Kohsuke Kawaguchi
 */
public class Define extends WriterNode {
    public final Grammar scope;
    public final String name;

    JDefinedClass clazz;

    public Define(Grammar scope, String name) {
        super(null,null);
        if(scope==null)     scope = (Grammar)this;  // hack for start pattern
        this.scope = scope;
        this.name = name;
        assert name!=null;
    }

    /**
     * Returns true if this define only contains
     * one child (and thus considered inlinable.)
     *
     * A pattern definition is also inlineable if
     * it's the start of the grammar (because "start" isn't a meaningful name)
     */
    public boolean isInline() {
        return hasOneChild() || name==Grammar.START;
    }

    void declare(NodeSet nset) {
        if(isInline())  return;

        clazz = nset.createClass(name);
        clazz._implements(TypedXmlWriter.class);
    }

    void generate(NodeSet nset) {
        if(clazz==null)     return;

        HashSet<Prop> props = new HashSet<Prop>();
        for( Leaf l : this )
            l.generate(clazz,nset,props);
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        if(isInline()) {
            for( Leaf l : this )
                l.generate(clazz,nset, props);
        } else {
            assert this.clazz!=null;
            clazz._implements(this.clazz);
        }
    }

    void prepare(NodeSet nset) {
        if(isInline() && leaf instanceof WriterNode && !name.equals(Grammar.START))
            ((WriterNode)leaf).alternativeName = name;
    }

    public String toString() {
        return "Define "+name;
    }
}

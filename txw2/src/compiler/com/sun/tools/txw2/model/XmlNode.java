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

import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Either an {@link Element} or {@link Attribute}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class XmlNode extends WriterNode {
    /**
     * Name of the attribute/element.
     *
     * In TXW, we ignore all infinite names.
     * (finite name class will be expanded to a list of {@link XmlNode}s.
     */
    public final QName name;

    protected XmlNode(Locator location, QName name, Leaf leaf) {
        super(location, leaf);
        this.name = name;
    }

    /**
     * Expand all refs and collect all children.
     */
    protected final Set<Leaf> collectChildren() {
        Set<Leaf> result = new HashSet<Leaf>();

        Stack<Node> work = new Stack<Node>();
        work.push(this);

        while(!work.isEmpty()) {
            for( Leaf l : work.pop() ) {
                if( l instanceof Ref ) {
                    work.push( ((Ref)l).def );
                } else {
                    result.add(l);
                }
            }
        }

        return result;
    }
}

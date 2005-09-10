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

/**
 * The {@link Node} that maps to the program element.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class WriterNode extends Node {
    /**
     * If this node is the sole child of a pattern block,
     * this field points to its name.
     *
     * <p>
     * When the element names are in conflict, this can be used.
     */
    protected String alternativeName;

    public WriterNode(Locator location, Leaf leaf) {
        super(location, leaf);
    }

    /**
     * Declares the class without its contents.
     *
     * The first step of the code generation.
     */
    abstract void declare(NodeSet nset);

    /**
     * Generates the contents.
     */
    abstract void generate(NodeSet nset);

    /**
     * Prepares for the code generation.
     */
    void prepare(NodeSet nset) {}
}

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
import com.sun.codemodel.JType;
import com.sun.tools.txw2.model.prop.Prop;
import org.xml.sax.Locator;

import java.util.Set;

/**
 * A constant value.
 *
 * @author Kohsuke Kawaguchi
 */
public class Value extends Leaf implements Text {
    /**
     * The underlying datatype, in case
     * we need to revert to {@link Data}.
     */
    public final JType type;
    /**
     * Constant name.
     */
    public final String name;

    public Value(Locator location, JType type, String name) {
        super(location);
        this.type = type;
        this.name = name;
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        createDataMethod(clazz,type,nset,props);
    }

    public JType getDatatype(NodeSet nset) {
        // TODO: enum support
        return type;
    }
}

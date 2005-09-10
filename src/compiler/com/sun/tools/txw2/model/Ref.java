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
import org.xml.sax.Locator;

import java.util.Set;

/**
 * A reference to a named pattern.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Ref extends Leaf {
    public final Define def;

    public Ref(Locator location, Grammar scope, String name) {
        super(location);
        this.def = scope.get(name);
    }

    public Ref(Locator location, Define def) {
        super(location);
        this.def = def;
    }

    public boolean isInline() {
        return def.isInline();
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        def.generate(clazz,nset,props);
    }
}

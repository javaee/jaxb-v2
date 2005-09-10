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

package com.sun.tools.txw2.model.prop;

import com.sun.codemodel.JType;

/**
 * @author Kohsuke Kawaguchi
 */
public class ValueProp extends Prop {
    private final JType type;

    public ValueProp(JType type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ValueProp)) return false;

        final ValueProp that = (ValueProp) o;

        return type.equals(that.type);
    }

    public int hashCode() {
        return type.hashCode();
    }
}

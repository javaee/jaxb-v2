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

import javax.xml.namespace.QName;

/**
 * Common implementation between elements and attributes.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class XmlItemProp extends Prop {
    private final QName name;
    private final JType type;

    public XmlItemProp(QName name, JType valueType) {
        this.name = name;
        this.type = valueType;
    }

    public final boolean equals(Object o) {
        if (this.getClass()!=o.getClass()) return false;

        XmlItemProp that = (XmlItemProp)o;

        return this.name.equals(that.name)
            && this.type.equals(that.type);
    }

    public final int hashCode() {
        return name.hashCode()*29 + type.hashCode();
    }
}

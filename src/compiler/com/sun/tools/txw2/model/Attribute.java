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

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.txw2.NameUtil;
import com.sun.tools.txw2.model.prop.AttributeProp;
import com.sun.tools.txw2.model.prop.Prop;
import com.sun.xml.txw2.annotation.XmlAttribute;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 * Attribute declaration.
 * 
 * @author Kohsuke Kawaguchi
 */
public class Attribute extends XmlNode {
    public Attribute(Locator location, QName name, Leaf leaf) {
        super(location, name, leaf);
    }

    void declare(NodeSet nset) {
        ; // attributes won't produce a class
    }

    void generate(NodeSet nset) {
        ; // nothing
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        Set<JType> types = new HashSet<JType>();

        for( Leaf l : collectChildren() ) {
            if (l instanceof Text) {
                types.add(((Text)l).getDatatype(nset));
            }
        }

        String methodName = NameUtil.toMethodName(name.getLocalPart());

        for( JType t : types ) {
            if(!props.add(new AttributeProp(name,t)))
                continue;

            JMethod m = clazz.method(JMod.PUBLIC,
                nset.opts.chainMethod? (JType)clazz : nset.codeModel.VOID,
                methodName);
            m.param(t,"value");

            JAnnotationUse a = m.annotate(XmlAttribute.class);
            if(!methodName.equals(name.getLocalPart()))
                a.param("value",name.getLocalPart());
            if(!name.getNamespaceURI().equals(""))
                a.param("ns",name.getNamespaceURI());

        }
    }

    public String toString() {
        return "Attribute "+name;
    }
}

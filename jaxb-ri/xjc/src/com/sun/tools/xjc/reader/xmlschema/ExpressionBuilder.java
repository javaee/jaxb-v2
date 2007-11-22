/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.xjc.reader.xmlschema;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.reader.gbind.Choice;
import com.sun.tools.xjc.reader.gbind.Element;
import com.sun.tools.xjc.reader.gbind.Expression;
import com.sun.tools.xjc.reader.gbind.OneOrMore;
import com.sun.tools.xjc.reader.gbind.Sequence;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;

/**
 * Visits {@link XSParticle} and creates a corresponding {@link Expression} tree.
 * @author Kohsuke Kawaguchi
 */
public final class ExpressionBuilder implements XSTermFunction<Expression> {

    public static Expression createTree(XSParticle p) {
        return new ExpressionBuilder().particle(p);
    }

    private ExpressionBuilder() {}

    /**
     * Wildcard instance needs to be consolidated to one,
     * and this is such instance (if any.)
     */
    private GWildcardElement wildcard = null;

    private final Map<QName,GElementImpl> decls = new HashMap<QName,GElementImpl>();

    private XSParticle current;

    /**
     * We can only have one {@link XmlAnyElement} property,
     * so all the wildcards need to be treated as one node.
     */
    public Expression wildcard(XSWildcard wc) {
        if(wildcard==null)
            wildcard = new GWildcardElement();
        wildcard.merge(wc);
        wildcard.particles.add(current);
        return wildcard;
    }

    public Expression modelGroupDecl(XSModelGroupDecl decl) {
        return modelGroup(decl.getModelGroup());
    }

    public Expression modelGroup(XSModelGroup group) {
        XSModelGroup.Compositor comp = group.getCompositor();
        if(comp==XSModelGroup.CHOICE) {
            // empty choice is not epsilon, but empty set,
            // so this initial value is incorrect. But this
            // kinda works.
            // properly handling empty set requires more work.
            Expression e = Expression.EPSILON;
            for (XSParticle p : group.getChildren()) {
                if(e==null)     e = particle(p);
                else            e = new Choice(e,particle(p));
            }
            return e;
        } else {
            Expression e = Expression.EPSILON;
            for (XSParticle p : group.getChildren()) {
                if(e==null)     e = particle(p);
                else            e = new Sequence(e,particle(p));
            }
            return e;
        }
    }

    public Element elementDecl(XSElementDecl decl) {
        QName n = BGMBuilder.getName(decl);

        GElementImpl e = decls.get(n);
        if(e==null)
            decls.put(n,e=new GElementImpl(n,decl));

        e.particles.add(current);
        assert current.getTerm()==decl;

        return e;
    }

    public Expression particle(XSParticle p) {
        current = p;
        Expression e = p.getTerm().apply(this);

        if(p.isRepeated())
            e = new OneOrMore(e);

        if(p.getMinOccurs()==0)
            e = new Choice(e,Expression.EPSILON);

        return e;
    }

}

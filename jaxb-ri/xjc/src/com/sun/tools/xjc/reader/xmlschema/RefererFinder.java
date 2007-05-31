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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * Finds which {@link XSComponent}s refer to which {@link XSComplexType}s.
 *
 * @author Kohsuke Kawaguchi
 */
final class RefererFinder implements XSVisitor {
    private final Set<Object> visited = new HashSet<Object>();

    private final Map<XSComponent,Set<XSComponent>> referers = new HashMap<XSComponent,Set<XSComponent>>();

    public Set<XSComponent> getReferer(XSComponent src) {
        Set<XSComponent> r = referers.get(src);
        if(r==null) return Collections.emptySet();
        return r;
    }


    public void schemaSet(XSSchemaSet xss) {
        if(!visited.add(xss))       return;

        for (XSSchema xs : xss.getSchemas()) {
            schema(xs);
        }
    }

    public void schema(XSSchema xs) {
        if(!visited.add(xs))       return;

        for (XSComplexType ct : xs.getComplexTypes().values()) {
            complexType(ct);
        }

        for (XSElementDecl e : xs.getElementDecls().values()) {
            elementDecl(e);
        }
    }

    public void elementDecl(XSElementDecl e) {
        if(!visited.add(e))       return;

        refer(e,e.getType());
        e.getType().visit(this);
    }

    public void complexType(XSComplexType ct) {
        if(!visited.add(ct))       return;

        refer(ct,ct.getBaseType());
        ct.getBaseType().visit(this);
        ct.getContentType().visit(this);
    }

    public void modelGroupDecl(XSModelGroupDecl decl) {
        if(!visited.add(decl))  return;

        modelGroup(decl.getModelGroup());
    }

    public void modelGroup(XSModelGroup group) {
        if(!visited.add(group))  return;

        for (XSParticle p : group.getChildren()) {
            particle(p);
        }
    }

    public void particle(XSParticle particle) {
        // since the particle method is side-effect free, no need to check for double-visit.
        particle.getTerm().visit(this);
    }


    // things we don't care
    public void simpleType(XSSimpleType simpleType) {}
    public void annotation(XSAnnotation ann) {}
    public void attGroupDecl(XSAttGroupDecl decl) {}
    public void attributeDecl(XSAttributeDecl decl) {}
    public void attributeUse(XSAttributeUse use) {}
    public void facet(XSFacet facet) {}
    public void notation(XSNotation notation) {}
    public void identityConstraint(XSIdentityConstraint decl) {}
    public void xpath(XSXPath xp) {}
    public void wildcard(XSWildcard wc) {}
    public void empty(XSContentType empty) {}

    /**
     * Called for each reference to record the fact.
     *
     * So far we only care about references to types.
     */
    private void refer(XSComponent source, XSType target) {
        Set<XSComponent> r = referers.get(target);
        if(r==null) {
            r = new HashSet<XSComponent>();
            referers.put(target,r);
        }
        r.add(source);
    }
}

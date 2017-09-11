/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.tools.txw2.builder.xsd;

import com.sun.codemodel.JType;
import com.sun.tools.txw2.TxwOptions;
import com.sun.tools.txw2.builder.relaxng.DatatypeFactory;
import com.sun.tools.txw2.model.Attribute;
import com.sun.tools.txw2.model.Data;
import com.sun.tools.txw2.model.Define;
import com.sun.tools.txw2.model.Empty;
import com.sun.tools.txw2.model.Grammar;
import com.sun.tools.txw2.model.Leaf;
import com.sun.tools.txw2.model.List;
import com.sun.tools.txw2.model.NodeSet;
import com.sun.tools.txw2.model.Element;
import com.sun.tools.txw2.model.Ref;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Kohsuke Kawaguchi
 */
public final class XmlSchemaBuilder implements XSFunction<Leaf>, XSSimpleTypeFunction<Leaf> {
    public static NodeSet build( XSSchemaSet xs, TxwOptions opts ) {
        XmlSchemaBuilder builder = new XmlSchemaBuilder(xs,opts);
        builder.build(xs);
        return builder.nodeSet;
    }

    private void build(XSSchemaSet xs) {
        // make sure that we bind all complex types
        for( XSSchema s : xs.getSchemas() ) {
            for( XSComplexType t : s.getComplexTypes().values() ) {
                t.apply(this);
            }
        }

        nodeSet.addAll(complexTypes.values());
        nodeSet.addAll(modelGroups.values());
        nodeSet.addAll(attGroups.values());
    }

    public Leaf simpleType(XSSimpleType simpleType) {
        return simpleType.apply((XSSimpleTypeFunction<Leaf>)this);
    }

    public Leaf particle(XSParticle particle) {
        return particle.getTerm().apply(this);
    }

    public Leaf empty(XSContentType empty) {
        return new Empty(empty.getLocator());
    }

    public Attribute attributeDecl(XSAttributeDecl decl) {
        return new Attribute(decl.getLocator(),
                        getQName(decl),
                        simpleType(decl.getType()));
    }

    public Attribute attributeUse(XSAttributeUse use) {
        return attributeDecl(use.getDecl());
    }

    public Leaf wildcard(XSWildcard wc) {
        // wildcard can be always written through the well-formedness method.
        // no need to generate anything for this.
        return new Empty(wc.getLocator());
    }

    public Leaf modelGroupDecl(XSModelGroupDecl mg) {
        Define def = modelGroups.get(mg);
        if(def==null) {
            def = grammar.get(mg.getName()); // TODO: name collision detection and avoidance
            modelGroups.put(mg,def);

            def.addChild(mg.getModelGroup().apply(this));
        }
        return new Ref(mg.getLocator(),def);
    }

    public Leaf modelGroup(XSModelGroup mg) {
        XSParticle[] children = mg.getChildren();
        if(children.length==0)  return new Empty(mg.getLocator());

        Leaf l = particle(children[0]);
        for( int i=1; i<children.length; i++ )
            l.merge(particle(children[i]));
        return l;
    }

    public Leaf elementDecl(XSElementDecl e) {
        Element el = new Element(e.getLocator(),getQName(e),e.getType().apply(this));
        nodeSet.add(el);
        return el;
    }

    public Leaf complexType(XSComplexType ct) {
        Define def = complexTypes.get(ct);
        if(def==null) {
            // TODO: consider name collision and such
            String name = ct.getName();
            if(ct.isLocal()) {
                name = ct.getScope().getName();
            }
            def = grammar.get(name);
            complexTypes.put(ct,def);

            XSType baseType = ct.getBaseType();
            if(baseType.isComplexType() && !isAnyType(baseType)) {
                // copy inheritance
                def.addChild(baseType.apply(this));

                if(ct.getDerivationMethod()==XSType.EXTENSION) {
                    XSContentType explicitContent = ct.getExplicitContent();
                    if(explicitContent!=null)
                        def.addChild(explicitContent.apply(this));
                    attHolder(ct, def);
                }
            } else {
                // just start from fresh
                def.addChild(ct.getContentType().apply(this));
                attHolder(ct, def);
            }
        }

        return new Ref(ct.getLocator(),def);
    }

    private void attHolder(XSAttContainer ct, Define def) {
        for( XSAttributeUse use : ct.getDeclaredAttributeUses() ) {
            def.addChild(attributeUse(use));
        }
        for (XSAttGroupDecl ag : ct.getAttGroups()) {
            def.addChild(attGroupDecl(ag));
        }
    }

    public Leaf attGroupDecl(XSAttGroupDecl ag) {
        Define def = attGroups.get(ag);
        if(def==null) {
            def = grammar.get(ag.getName());
            attGroups.put(ag,def);
            attHolder(ag,def);
        }
        return new Ref(ag.getLocator(),def);
    }

    private boolean isAnyType(XSType t) {
        return t.getName().equals("anyType") && t.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema");
    }

    public Leaf restrictionSimpleType(XSRestrictionSimpleType rst) {
        JType t = dtf.getType(rst.getTargetNamespace(),rst.getName());
        if(t!=null) return new Data(rst.getLocator(),t);
        return simpleType(rst.getSimpleBaseType());
    }

    public Leaf unionSimpleType(XSUnionSimpleType st) {
        Leaf l = simpleType(st.getMember(0));
        for( int i=1; i<st.getMemberSize(); i++ )
            l.merge(simpleType(st.getMember(i)));
        return l;
    }

    public Leaf listSimpleType(XSListSimpleType st) {
        return new List(st.getLocator(),simpleType(st.getItemType()));
    }

    private QName getQName(XSDeclaration decl) {
        return new QName(decl.getTargetNamespace(),decl.getName());
    }

    protected final XSSchemaSet schemaSet;

    protected final NodeSet nodeSet;

    private final DatatypeFactory dtf;

    /**
     * We map model groups to interfaces.
     */
    private final Map<XSModelGroupDecl,Define> modelGroups = new HashMap<XSModelGroupDecl, Define>();

    /**
     * We map complex types to interfaces.
     */
    private final Map<XSComplexType,Define> complexTypes = new HashMap<XSComplexType,Define>();

    /**
     * ... and attribute groups
     */
    private final Map<XSAttGroupDecl,Define> attGroups = new HashMap<XSAttGroupDecl,Define>();

    private final Grammar grammar = new Grammar();

    private XmlSchemaBuilder(XSSchemaSet xs,TxwOptions opts) {
        this.schemaSet = xs;
        grammar.addChild(new Empty(null));
        this.nodeSet = new NodeSet(opts,grammar);
        this.dtf = new DatatypeFactory(opts.codeModel);
    }



// won't be used
    public Leaf annotation(XSAnnotation xsAnnotation) {
        throw new IllegalStateException();
    }

    public Leaf schema(XSSchema xsSchema) {
        throw new IllegalStateException();
    }

    public Leaf facet(XSFacet xsFacet) {
        throw new IllegalStateException();
    }

    public Leaf notation(XSNotation xsNotation) {
        throw new IllegalStateException();
    }

    public Leaf identityConstraint(XSIdentityConstraint xsIdentityConstraint) {
        throw new IllegalStateException();
    }

    public Leaf xpath(XSXPath xsxPath) {
        throw new IllegalStateException();
    }
}

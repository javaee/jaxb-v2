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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * Reports all unacknowledged customizations as errors.
 *
 * <p>
 * Since we scan the whole content tree, we use this to check for unused
 * <tt>xmime:expectedContentTypes</tt> attributes. TODO: if we find this kind of error checks more
 * common, use the visitors so that we don't have to mix everything in one class.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class UnusedCustomizationChecker extends BindingComponent implements XSVisitor, XSSimpleTypeVisitor {
    private final BGMBuilder builder = Ring.get(BGMBuilder.class);
    private final SimpleTypeBuilder stb = Ring.get(SimpleTypeBuilder.class);

    private final Set<XSComponent> visitedComponents = new HashSet<XSComponent>();

    /**
     * Runs the check.
     */
    void run() {
        for( XSSchema s : Ring.get(XSSchemaSet.class).getSchemas() ) {
            schema(s);
            run( s.getAttGroupDecls() );
            run( s.getAttributeDecls() );
            run( s.getComplexTypes() );
            run( s.getElementDecls() );
            run( s.getModelGroupDecls() );
            run( s.getNotations() );
            run( s.getSimpleTypes() );
        }
    }
    
    private void run( Map<String,? extends XSComponent> col ) {
        for( XSComponent c : col.values() )
            c.visit(this);
    }


    /**
     * Checks unused customizations on this component
     * and returns true if this is the first time this
     * component is checked.
     */
    private boolean check( XSComponent c ) {
        if( !visitedComponents.add(c) )
            return false;   // already processed

        for( BIDeclaration decl : builder.getBindInfo(c).getDecls() )
            check(decl, c);

        checkExpectedContentTypes(c);

        return true;
    }

    private void checkExpectedContentTypes(XSComponent c) {
        if(c.getForeignAttribute(WellKnownNamespace.XML_MIME_URI, Const.EXPECTED_CONTENT_TYPES)==null)
            return; // no such attribute
        if(c instanceof XSParticle)
            return; // particles get the same foreign attributes as local element decls,
                    // so we need to skip them

        if(!stb.isAcknowledgedXmimeContentTypes(c)) {
            // this is not used
            getErrorReporter().warning(c.getLocator(),Messages.WARN_UNUSED_EXPECTED_CONTENT_TYPES);
        }
    }

    private void check(BIDeclaration decl, XSComponent c) {
        if( !decl.isAcknowledged() ) {
            getErrorReporter().error(
                decl.getLocation(),
                Messages.ERR_UNACKNOWLEDGED_CUSTOMIZATION,
                decl.getName().getLocalPart()
                );
            getErrorReporter().error(
                c.getLocator(),
                Messages.ERR_UNACKNOWLEDGED_CUSTOMIZATION_LOCATION);
            // mark it as acknowledged to avoid
            // duplicated error messages.
            decl.markAsAcknowledged();
        }
        for (BIDeclaration d : decl.getChildren())
            check(d,c);
    }


    public void annotation(XSAnnotation ann) {}
    
    public void attGroupDecl(XSAttGroupDecl decl) {
        if(check(decl))
            attContainer(decl);
    }

    public void attributeDecl(XSAttributeDecl decl) {
        if(check(decl))
            decl.getType().visit((XSSimpleTypeVisitor)this);
    }

    public void attributeUse(XSAttributeUse use) {
        if(check(use))
            use.getDecl().visit(this);
    }

    public void complexType(XSComplexType type) {
        if(check(type)) {
            // don't need to check the base type -- it must be global, thus
            // it is covered already
            type.getContentType().visit(this);
            attContainer(type);
        }
    }
    
    private void attContainer( XSAttContainer cont ) {
        for( Iterator itr = cont.iterateAttGroups(); itr.hasNext(); )
            ((XSAttGroupDecl)itr.next()).visit(this);
            
        for( Iterator itr = cont.iterateDeclaredAttributeUses(); itr.hasNext(); )
            ((XSAttributeUse)itr.next()).visit(this);
        
        XSWildcard wc = cont.getAttributeWildcard();
        if(wc!=null)        wc.visit(this);
    }

    public void schema(XSSchema schema) {
        check(schema);
    }

    public void facet(XSFacet facet) {
        check(facet);
    }

    public void notation(XSNotation notation) {
        check(notation);
    }

    public void wildcard(XSWildcard wc) {
        check(wc);
    }

    public void modelGroupDecl(XSModelGroupDecl decl) {
        if(check(decl))
            decl.getModelGroup().visit(this);
    }

    public void modelGroup(XSModelGroup group) {
        if(check(group)) {
            for( int i=0; i<group.getSize(); i++ )
                group.getChild(i).visit(this);
        }
    }

    public void elementDecl(XSElementDecl decl) {
        if(check(decl)) {
            decl.getType().visit(this);
            for( XSIdentityConstraint id : decl.getIdentityConstraints() )
                id.visit(this);
        }
    }

    public void simpleType(XSSimpleType simpleType) {
        if(check(simpleType))
            simpleType.visit( (XSSimpleTypeVisitor)this );
    }

    public void particle(XSParticle particle) {
        if(check(particle))
            particle.getTerm().visit(this);
    }

    public void empty(XSContentType empty) {
        check(empty);
    }

    public void listSimpleType(XSListSimpleType type) {
        if(check(type))
            type.getItemType().visit((XSSimpleTypeVisitor)this);
    }

    public void restrictionSimpleType(XSRestrictionSimpleType type) {
        if(check(type))
            type.getBaseType().visit(this);
    }

    public void unionSimpleType(XSUnionSimpleType type) {
        if(check(type)) {
            for( int i=0; i<type.getMemberSize(); i++ )
                type.getMember(i).visit((XSSimpleTypeVisitor)this);
        }
    }

    public void identityConstraint(XSIdentityConstraint id) {
        if(check(id)) {
            id.getSelector().visit(this);
            for( XSXPath xp : id.getFields() )
                xp.visit(this);
        }
    }

    public void xpath(XSXPath xp) {
        check(xp);
    }

}

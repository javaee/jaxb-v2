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
package com.sun.tools.xjc.reader.xmlschema.ct;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;

import org.kohsuke.rngom.nc.ChoiceNameClass;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.SimpleNameClass;

/**
 * Binds a complex type derived from another complex type by extension.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractExtendedComplexTypeBuilder extends CTBuilder {

    /**
     * Map from {@link XSComplexType} to {@link NameClass}[2] that
     * represents the names used in its child elements [0] and
     * attributes [1].
     */
    protected final Map<XSComplexType, NameClass[]> characteristicNameClasses = new HashMap<XSComplexType, NameClass[]>();

    /**
     * Computes a name class that represents everything in a given content model.
     */
    protected final XSTermFunction<NameClass> contentModelNameClassBuilder = new XSTermFunction<NameClass>() {
        public NameClass wildcard(XSWildcard wc) {
            return WildcardNameClassBuilder.build(wc);
        }

        public NameClass modelGroupDecl(XSModelGroupDecl decl) {
            return modelGroup(decl.getModelGroup());
        }

        public NameClass modelGroup(XSModelGroup group) {
            NameClass nc = NameClass.NULL;
            for( int i=0; i<group.getSize(); i++ )
                nc = new ChoiceNameClass(nc, group.getChild(i).getTerm().apply(this));
            return nc;
        }

        public NameClass elementDecl(XSElementDecl decl) {
            return getNameClass(decl);
        }
    };

    /**
     * Checks if the particles/attributes defined in the type parameter
     * collides with the name classes of anc/enc.
     *
     * @return true if there's a collision.
     */
    protected boolean checkCollision(NameClass anc, NameClass enc, XSComplexType type) {
        NameClass[] chnc = characteristicNameClasses.get(type);
        if (chnc == null) {
            chnc = new NameClass[2];
            chnc[0] = getNameClass(type.getContentType());

            // build attribute name classes
            NameClass nc = NameClass.NULL;
            Iterator itr = type.iterateAttributeUses();
            while( itr.hasNext() )
                anc = new ChoiceNameClass(anc, getNameClass(((XSAttributeUse) itr.next()).getDecl()));
            XSWildcard wc = type.getAttributeWildcard();
            if(wc!=null)
                nc = new ChoiceNameClass(nc, WildcardNameClassBuilder.build(wc));
            chnc[1] = nc;

            characteristicNameClasses.put(type, chnc);
        }

        return chnc[0].hasOverlapWith(enc) || chnc[1].hasOverlapWith(anc);
    }

    /**
     * Looks for the derivation chain t_1 > t_2 > ... > t
     * and find t_i such that t_i derives by restriction but
     * for every j>i, t_j derives by extension.
     *
     * @return null
     *      If there's no such t_i or if t_i is any type.
     */
    protected XSComplexType getLastRestrictedType(XSComplexType t) {
        if (t.getBaseType() == schemas.getAnyType()) {
            return null;   // we don't count the restriction from anyType
        }
        if (t.getDerivationMethod() == XSType.RESTRICTION) {
            return t;
        }

        XSComplexType baseType = t.getBaseType().asComplexType();
        if (baseType != null) {
            return getLastRestrictedType(baseType);
        } else {
            return null;
        }
    }

    /**
     * Checks if this new extension is safe.
     *
     * UGLY.
     * <p>
     * If you have ctA extending ctB and ctB restricting ctC, our
     * Java classes will look like CtAImpl extending CtBImpl
     * extending CtCImpl.
     *
     * <p>
     * Since a derived class unmarshaller uses the base class unmarshaller,
     * this could potentially result in incorrect unmarshalling.
     * We used to just reject such a case, but then we found that
     * there are schemas that are using it.
     *
     * <p>
     * One generalized observation that we reached is that if the extension
     * is only adding new elements/attributes which has never been used
     * in any of its base class (IOW, if none of the particle / attribute use /
     * attribute wildcard can match the name of newly added elements/attributes)
     * then it is safe to add them.
     *
     * <p>
     * This function checks if the derivation chain to this type is
     * not using restriction, and if it is, then checks if it is safe
     * according to the above condition.
     *
     * @return false
     *      If this complex type needs to be rejected.
     */
    protected boolean checkIfExtensionSafe(XSComplexType baseType, XSComplexType thisType) {
        XSComplexType lastType = getLastRestrictedType(baseType);

        if (lastType == null) {
            return true;    // no restriction in derivation chain
        }
        NameClass anc = NameClass.NULL;
        // build name class for attributes in new complex type
        Iterator itr = thisType.iterateDeclaredAttributeUses();
        while (itr.hasNext()) {
            anc = new ChoiceNameClass(anc, getNameClass(((XSAttributeUse) itr.next()).getDecl()));
        }
        // TODO: attribute wildcard

        NameClass enc = getNameClass(thisType.getExplicitContent());

        // check against every base type ... except the root anyType
        while (lastType != lastType.getBaseType()) {
            if (checkCollision(anc, enc, lastType)) {
                return false;
            }

            if (lastType.getBaseType().isSimpleType()) // if the base type is a simple type, there won't be
            // any further name collision.
            {
                return true;
            }

            lastType = lastType.getBaseType().asComplexType();
        }

        return true;    // OK
    }

    /**
     * Gets a {@link NameClass} that represents all the terms in the given content type.
     * If t is not a particle, just return an empty name class.
     */
    private NameClass getNameClass(XSContentType t) {
        if(t==null) return NameClass.NULL;
        XSParticle p = t.asParticle();
        if(p==null) return NameClass.NULL;
        else        return p.getTerm().apply(contentModelNameClassBuilder);
    }

    /**
     * Gets a {@link SimpleNameClass} from the name of a {@link XSDeclaration}.
     */
    private NameClass getNameClass(XSDeclaration decl) {
        return new SimpleNameClass(decl.getTargetNamespace(), decl.getName());
    }

}
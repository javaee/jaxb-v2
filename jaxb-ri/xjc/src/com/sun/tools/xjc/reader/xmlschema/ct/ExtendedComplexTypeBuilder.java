/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.ct;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.util.NameClassCollisionChecker;
import com.sun.tools.xjc.model.CClassInfo;
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

/**
 * Binds a complex type derived from another complex type by extension.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ExtendedComplexTypeBuilder extends CTBuilder {

    /**
     * Map from {@link XSComplexType} to {@link NameClass}[2] that
     * represents the names used in its child elements [0] and
     * attributes [1].
     */
    private final Map<XSComplexType,NameClass[]> characteristicNameClasses = new HashMap<XSComplexType,NameClass[]>();

    public boolean isApplicable(XSComplexType ct) {
        XSType baseType = ct.getBaseType();
        return baseType!=schemas.getAnyType()
            &&  baseType.isComplexType()
            &&  ct.getDerivationMethod()==XSType.EXTENSION;
    }

    public void build(XSComplexType ct) {
        XSComplexType baseType = ct.getBaseType().asComplexType();

        // build the base class
        CClassInfo baseClass = selector.bindToType(baseType,true);
        assert baseClass!=null;   // global complex type must map to a class

        selector.getCurrentBean().setBaseClass(baseClass);

        // derivation by extension.
        ComplexTypeBindingMode baseTypeFlag = builder.getBindingMode(baseType);

        XSContentType explicitContent = ct.getExplicitContent();

        if(!checkIfExtensionSafe(baseType,ct)) {
            // error. We can't handle any further extension
            errorReceiver.error( ct.getLocator(),
                Messages.ERR_NO_FURTHER_EXTENSION.format(
                    baseType.getName(), ct.getName() )
            );
            return;
        }


        // explicit content is always either empty or a particle.
        if( explicitContent!=null && explicitContent.asParticle()!=null ) {

            if( baseTypeFlag==ComplexTypeBindingMode.NORMAL) {
                // if we have additional explicit content, process them.

                builder.recordBindingMode(ct,
                    getParticleBinder().checkFallback(explicitContent.asParticle())
                    ?ComplexTypeBindingMode.FALLBACK_REST
                    :ComplexTypeBindingMode.NORMAL);

                getParticleBinder().build(explicitContent.asParticle());

            } else {
                // the base class has already done the fallback.
                // don't add anything new
                builder.recordBindingMode(ct, baseTypeFlag );
            }
        } else {
            // if it's empty, no additional processing is necessary
            builder.recordBindingMode(ct, baseTypeFlag );
        }

        // adds attributes and we are through.
        green.attContainer(ct);
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
    private boolean checkIfExtensionSafe( XSComplexType baseType, XSComplexType thisType ) {
        XSComplexType lastType = getLastRestrictedType(baseType);

        if(lastType==null)
            return true;    // no restriction in derivation chain

        NameClass anc = NameClass.NONE;
        // build name class for attributes in new complex type
        Iterator itr = thisType.iterateDeclaredAttributeUses();
        while( itr.hasNext() )
            anc = new ChoiceNameClass( anc, getNameClass(((XSAttributeUse)itr.next()).getDecl()) );
        // TODO: attribute wildcard
        anc = anc.simplify();

        NameClass enc = getNameClass(thisType.getExplicitContent()).simplify();

        // check against every base type ... except the root anyType
        while(lastType!=lastType.getBaseType()) {
            if(checkCollision(anc,enc,lastType))
                return false;

            if(lastType.getBaseType().isSimpleType())
                // if the base type is a simple type, there won't be
                // any further name collision.
                return true;

            lastType = lastType.getBaseType().asComplexType();
        }



        return true;    // OK
    }

    /**
     * Checks if the particles/attributes defined in the type parameter
     * collides with the name classes of anc/enc.
     *
     * @return true if there's a collision.
     */
    private boolean checkCollision(NameClass anc, NameClass enc, XSComplexType type) {
        NameClass[] chnc = characteristicNameClasses.get(type);
        if(chnc==null) {
            chnc = new NameClass[2];
            chnc[0] = getNameClass(type.getContentType());

            // build attribute name classes
            NameClass nc = NameClass.NONE;
            Iterator itr = type.iterateAttributeUses();
            while( itr.hasNext() )
                anc = new ChoiceNameClass( anc, getNameClass(((XSAttributeUse)itr.next()).getDecl()) );
            XSWildcard wc = type.getAttributeWildcard();
            if(wc!=null)
                nc = new ChoiceNameClass( nc, WildcardNameClassBuilder.build(wc) );
            chnc[1] = nc;

            characteristicNameClasses.put(type,chnc);
        }

        return collisionChecker.check( chnc[0], enc ) || collisionChecker.check( chnc[1], anc );
    }

    /**
     * Gets a {@link NameClass} that represents all the terms in the given content type.
     * If t is not a particle, just return an empty name class.
     */
    private NameClass getNameClass( XSContentType t ) {
        if(t==null) return NameClass.NONE;
        XSParticle p = t.asParticle();
        if(p==null) return NameClass.NONE;
        else        return p.getTerm().apply(contentModelNameClassBuilder);
    }

    /**
     * Gets a {@link SimpleNameClass} from the name of a {@link XSDeclaration}.
     */
    private NameClass getNameClass( XSDeclaration decl ) {
        return new SimpleNameClass(decl.getTargetNamespace(),decl.getName());
    }

    private final NameClassCollisionChecker collisionChecker = new NameClassCollisionChecker();
    /**
     * Computes a name class that represents everything in a given content model.
     */
    private final XSTermFunction<NameClass> contentModelNameClassBuilder = new XSTermFunction<NameClass>() {
        public NameClass wildcard(XSWildcard wc) {
            return WildcardNameClassBuilder.build(wc);
        }

        public NameClass modelGroupDecl(XSModelGroupDecl decl) {
            return modelGroup(decl.getModelGroup());
        }

        public NameClass modelGroup(XSModelGroup group) {
            NameClass nc = NameClass.NONE;
            for( int i=0; i<group.getSize(); i++ )
                nc = new ChoiceNameClass(nc,(NameClass)group.getChild(i).getTerm().apply(this));
            return nc;
        }

        public NameClass elementDecl(XSElementDecl decl) {
            return getNameClass(decl);
        }
    };


    /**
     * Looks for the derivation chain t_1 > t_2 > ... > t
     * and find t_i such that t_i derives by restriction but
     * for every j>i, t_j derives by extension.
     *
     * @return null
     *      If there's no such t_i or if t_i is any type.
     */
    private XSComplexType getLastRestrictedType( XSComplexType t ) {
        if( t.getBaseType()==schemas.getAnyType() )
            return null;   // we don't count the restriction from anyType
        if( t.getDerivationMethod()==XSType.RESTRICTION )
            return t;

        XSComplexType baseType = t.getBaseType().asComplexType();
        if(baseType!=null)
            return getLastRestrictedType(baseType);
        else
            return null;
    }
}
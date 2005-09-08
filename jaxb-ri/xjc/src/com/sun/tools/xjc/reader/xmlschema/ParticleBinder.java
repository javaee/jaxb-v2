/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermVisitor;

/**
 * Binds the content models.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ParticleBinder extends BindingComponent {

    private final BGMBuilder builder = Ring.get(BGMBuilder.class);

    /**
     * Builds the {@link CPropertyInfo}s from the given particle
     * (and its descendants), and set them to the class returned by
     * {@link ClassSelector#getCurrentBean()}.
     */
    public void build( XSParticle p ) {
        build(p,Collections.<XSParticle>emptySet());
    }

    /**
     * The version of the build method that forces a specified set of particles
     * to become a property.
     */
    public void build( XSParticle p, Collection<XSParticle> forcedProps ) {
        Checker checker = checkCollision(p,forcedProps);

        if(checker.hasNameCollision()) {
            CReferencePropertyInfo prop = new CReferencePropertyInfo(
                getCurrentBean().getBaseClass()==null?"Content":"Rest",
                true, false, p,
                builder.getBindInfo(p).toCustomizationList(),
                p.getLocator() );
            RawTypeSetBuilder.build(p,false).addTo(prop);
            prop.javadoc = Messages.format( Messages.MSG_FALLBACK_JAVADOC,
                    checker.getCollisionInfo().toString() );

            getCurrentBean().addProperty(prop);
        } else {
            new ParticleBinder.Builder(checker.markedParticles).particle(p);
        }
    }

    /**
     * Similar to the build method but this method only checks if
     * the BGM that will be built by the build method will
     * do the fallback (map all the properties into one list) or not.
     *
     * @return
     *      false if the fallback will not happen.
     */
    public boolean checkFallback( XSParticle p ) {
        return checkCollision(p,Collections.<XSParticle>emptyList()).hasNameCollision();
    }

    /**
     * Gets the BIProperty object that applies to the given particle.
     */
    private final BIProperty getLocalPropCustomization( XSParticle p ) {
        return getLocalCustomization(p,BIProperty.class);
    }

    private final <T extends BIDeclaration> T getLocalCustomization( XSParticle p, Class<T> type ) {
        // check the property customization of this component first
        T cust = builder.getBindInfo(p).get(type);
        if(cust!=null)  return cust;

        // if not, the term might have one.
        cust = builder.getBindInfo(p.getTerm()).get(type);
        if(cust!=null)  return cust;

        return null;
    }


    /**
     * Computes the label of a given particle.
     * Usually, the getLabel method should be used instead.
     */
    private final String computeLabel( XSParticle p ) {
        // if the particle carries a customization, use that value.
        // since we are binding content models, it's always non-constant properties.
        BIProperty cust = getLocalPropCustomization(p);
        if(cust!=null && cust.getPropertyName(false)!=null)
            return cust.getPropertyName(false);

        // no explicit property name is given. Compute one.

        XSTerm t = p.getTerm();

//        // first, check if a term is going to be a class, if so, use that name.
//        ClassItem ci = owner.selector.select(t);
//        if(ci!=null) {
//            return makeJavaName(ci.getTypeAsDefined().name());
//        }

        // if it fails, compute the default name according to the spec.
        if(t.isElementDecl())
            // for element, take the element name.
            return makeJavaName(t.asElementDecl().getName());
        if(t.isModelGroupDecl())
            // for named model groups, take that name
            return makeJavaName(t.asModelGroupDecl().getName());
        if(t.isWildcard())
            // the spec says it will map to "any" by default.
            return "Any";
        if(t.isModelGroup()) {
            try {
                return builder.getSpecDefaultName(t.asModelGroup());
            } catch( ParseException e ) {
                // unable to generate a name.
                getErrorReporter().error(t.getLocator(),
                    Messages.ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP);
                return "undefined"; // recover from error by assuming something
            }
        }


        // there are only four types of XSTerm.
        assert false;
        throw new IllegalStateException();
    }

    /** Converts an XML name to the corresponding Java name. */
    private String makeJavaName( String xmlName ) {
        return builder.getNameConverter().toPropertyName(xmlName);
    }

    private CClassInfo getCurrentBean() {
        return getClassSelector().getCurrentBean();
    }

    private Checker checkCollision( XSParticle p, Collection<XSParticle> forcedProps ) {
        // scan the tree by a checker.
        Checker checker = new Checker(forcedProps);

        CClassInfo superClass = getCurrentBean().getBaseClass();

        if(superClass!=null)
            checker.readSuperClass(superClass);
        checker.particle(p);

        return checker;
    }










    /**
     * Marks particles that need to be mapped to properties,
     * by reading customization info.
     */
    private final class Checker implements XSTermVisitor {

        Checker(Collection<XSParticle> forcedProps) {
            this.forcedProps = forcedProps;
        }

        boolean hasNameCollision() {
            return collisionInfo!=null;
        }

        CollisionInfo getCollisionInfo() {
            return collisionInfo;
        }

        /**
         * If a collision is found, this field will be non-null.
         */
        private CollisionInfo collisionInfo = null;

        /** Used to check name collision. */
        private final NameCollisionChecker cchecker = new NameCollisionChecker();

        /**
         * @see ParticleBinder#build(XSParticle, Collection<com.sun.xml.xsom.XSParticle>)
         */
        private final Collection<XSParticle> forcedProps;

        public void particle( XSParticle p ) {

            if(getLocalPropCustomization(p)!=null
            || builder.getLocalDomCustomization(p)!=null) {
                // if a property customization is specfied,
                // check that value and turn around.
                check(p);
                mark(p);
                return;
            }

            XSTerm t = p.getTerm();

            if(p.getMaxOccurs()!=1
            &&(t.isModelGroup() || t.isModelGroupDecl())) {
                // a repeated model group gets its own property
                mark(p);
                return;
            }

            if(forcedProps.contains(p)) {
                // this particle must become a property
                mark(p);
                return;
            }

            outerParticle = p;
            t.visit(this);
        }

        /**
         * This field points to the parent XSParticle.
         * The value is only valid when we are processing XSTerm.
         */
        private XSParticle outerParticle;

        public void elementDecl(XSElementDecl decl) {
            check(outerParticle);
            mark(outerParticle);
        }

        public void modelGroup(XSModelGroup mg) {
            for( XSParticle child : mg.getChildren() )
                particle(child);
        }

        public void modelGroupDecl(XSModelGroupDecl decl) {
            modelGroup(decl.getModelGroup());
        }

        public void wildcard(XSWildcard wc) {
            mark(outerParticle);
        }

        void readSuperClass( CClassInfo ci ) {
            cchecker.readSuperClass(ci);
        }




        /**
         * Checks the name collision of a newly found particle.
         */
        private void check( XSParticle p ) {
            if( collisionInfo==null )
                collisionInfo = cchecker.check(p);
        }

        /**
         * Marks a particle that it's going to be mapped to a property.
         */
        private void mark( XSParticle p ) {
            markedParticles.put(p,computeLabel(p));
        }

        /**
         * Marked particles.
         *
         * A map from XSParticle to its label.
         */
        public final Map<XSParticle,String> markedParticles = new HashMap<XSParticle,String>();


        /**
         * Checks name collisions among particles that belong to sequences.
         */
        private final class NameCollisionChecker {

            /**
             * Checks the label conflict of a particle.
             * This method shall be called for each marked particle.
             *
             * @return
             *      a description of a collision if a name collision is
             *      found. Otherwise null.
             */
            CollisionInfo check( XSParticle p ) {
                // this can be used for particles with a property customization,
                // which may not have element declaration as its term.
//                // we only check particles with element declarations.
//                _assert( p.getTerm().isElementDecl() );

                String label = computeLabel(p);
                if( occupiedLabels.containsKey(label) ) {
                    // collide with occupied labels
                    return new CollisionInfo(label,p.getLocator(),
                            occupiedLabels.get(label).locator);
                }

                for( XSParticle jp : particles ) {
                    if(!check( p, jp )) {
                        // problem was found. no need to check further
                        return new CollisionInfo( label, p.getLocator(), jp.getLocator() );
                    }
                }
                particles.add(p);
                return null;
            }

            /** List of particles reported through the check method. */
            private final List<XSParticle> particles = new ArrayList<XSParticle>();

            /**
             * Label names already used in the base type.
             */
            private final Map<String,CPropertyInfo> occupiedLabels = new HashMap<String,CPropertyInfo>();

            /**
             * Checks the conflict of two particles.
             * @return
             *      true if the check was successful.
             */
            private boolean check( XSParticle p1, XSParticle p2 ) {
                return !computeLabel(p1).equals(computeLabel(p2));
            }

            /**
             * Reads fields of the super class and includes them
             * to name collision tests.
             */
            void readSuperClass( CClassInfo base ) {
                for( ; base!=null; base=base.getBaseClass() ) {
                    for( CPropertyInfo p : base.getProperties() )
                        occupiedLabels.put(p.getName(true),p);
                }
            }
        }





        /** Keep the computed label names for particles. */
        private final Map<XSParticle,String> labelCache = new Hashtable<XSParticle,String>();

        /**
         * Hides the computeLabel method of the outer class
         * and adds caching.
         */
        private String computeLabel( XSParticle p ) {
            String label = labelCache.get(p);
            if(label==null)
                labelCache.put( p, label=ParticleBinder.this.computeLabel(p) );
            return label;
        }
    }












    /**
     * Builds properties by using the result computed by Checker
     */
    private final class Builder implements XSTermVisitor {
        Builder( Map<XSParticle,String> markedParticles ) {
            this.markedParticles = markedParticles;
        }

        /** All marked particles. */
        private final Map<XSParticle,String/*label*/> markedParticles;

        /**
         * When we are visiting inside an optional particle, this flag
         * is set to true.
         *
         * <p>
         * This allows us to correctly generate primitive/boxed types.
         */
        private boolean insideOptionalParticle;


        /** Returns true if a particle is marked. */
        private boolean marked( XSParticle p ) {
            return markedParticles.containsKey(p);
        }
        /** Gets a label of a particle. */
        private String getLabel( XSParticle p ) {
            return markedParticles.get(p);
        }

        public void particle( XSParticle p ) {
            XSTerm t = p.getTerm();

            if(marked(p)) {
                boolean nillable = false;
                if(p.getTerm().isElementDecl())
                    nillable = p.getTerm().asElementDecl().isNillable();

                BIProperty cust = BIProperty.getCustomization(p);
                CPropertyInfo prop = cust.createElementOrReferenceProperty(
                    getLabel(p), false, p, RawTypeSetBuilder.build(p,insideOptionalParticle), nillable );
                getCurrentBean().addProperty(prop);
            } else {
                // repeated model groups should have been marked already
                assert p.getMaxOccurs()==0 || p.getMaxOccurs()==1;

                boolean oldIOP = insideOptionalParticle;
                insideOptionalParticle |= p.getMinOccurs()==0;
                // this is an unmarked particle
                t.visit(this);
                insideOptionalParticle = oldIOP;
            }
        }

        public void elementDecl( XSElementDecl e ) {
            // because the corresponding particle must be marked.
            assert false;
        }

        public void wildcard( XSWildcard wc ) {
            // because the corresponding particle must be marked.
            assert false;
        }

        public void modelGroupDecl( XSModelGroupDecl decl ) {
            modelGroup(decl.getModelGroup());
        }

        public void modelGroup( XSModelGroup mg ) {
            boolean oldIOP = insideOptionalParticle;
            insideOptionalParticle |= mg.getCompositor()==XSModelGroup.CHOICE;

            for( XSParticle p : mg.getChildren())
                particle(p);

            insideOptionalParticle = oldIOP;
        }
    }
}

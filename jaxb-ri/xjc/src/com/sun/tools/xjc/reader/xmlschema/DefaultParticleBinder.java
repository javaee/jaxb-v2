/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermVisitor;

/**
 * Performs the default binding on {@link XSParticle}.
 *
 * <p>
 * Note that the terminology "infoset" has almost nothing to do with
 * XML "infoset". Just consider it as a coincidence.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class DefaultParticleBinder extends ParticleBinder {

    // inherited
    public void build( XSParticle p ) {
        Checker checker = checkCollision(p);

        if(checker.hasNameCollision()) {
            // TODO: we also need to check whether this particle has
            // any name collision with that of the base class.
            CReferencePropertyInfo prop = new CReferencePropertyInfo(
                getCurrentBean().getBaseClass()==null?"Content":"Rest",
                true, false,
                    builder.getBindInfo(p).toCustomizationList(),
                p.getLocator() );
            RawTypeSetBuilder.build(p,false).addTo(prop);
            prop.javadoc = Messages.format( Messages.MSG_FALLBACK_JAVADOC,
                    checker.getCollisionInfo().toString() );

            getCurrentBean().addProperty(prop);
        } else {
            new Builder(checker.markedParticles).particle(p);
        }
    }

    private CClassInfo getCurrentBean() {
        return getClassSelector().getCurrentBean();
    }

    private Checker checkCollision( XSParticle p ) {
        // scan the tree by a checker.
        Checker checker = new Checker();

        CClassInfo superClass = getCurrentBean().getBaseClass();

        if(superClass!=null)
            checker.readSuperClass(superClass);
        checker.particle(p);

        return checker;
    }

    public boolean checkFallback( XSParticle p ) {
        return checkCollision(p).hasNameCollision();
    }









    /**
     * Marks particles that need to be mapped to properties,
     * by reading customization info.
     */
    private class Checker implements XSTermVisitor {

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
        private final NameCollisionChecker cchecker
            = new NameCollisionChecker();

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
                labelCache.put( p, label=DefaultParticleBinder.this.computeLabel(p) );
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

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;

import java.text.ParseException;

import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;

/**
 * Base interface for various algorithms that bind a particle.
 *
 * TODO: do we still have more than one ParticleBinder?
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ParticleBinder extends BindingComponent {
    /**
     * Builds the {@link CPropertyInfo}s from the given particle
     * (and its descendants), and set them to the class returned by
     * {@link ClassSelector#getCurrentBean()}.
     */
    public abstract void build( XSParticle p );

    /**
     * Similar to the build method but this method only checks if
     * the BGM that will be built by the build method will
     * do the fallback (map all the properties into one list) or not.
     *
     * @return
     *      false if the fallback will not happen.
     */
    public abstract boolean checkFallback( XSParticle p );




//
// utility methods
//
    protected ParticleBinder() {
        Ring.add(ParticleBinder.class,this);
    }

    protected final BGMBuilder builder = Ring.get(BGMBuilder.class);

    /**
     * Checks if a reference to the given term needs "skipping"
     * (in stead of referencing the term itself, the "skip" will refer
     * to the complex type of the term, which must be a global element decl.)
     */
    protected final boolean needSkip(XSTerm t) {
        return isGlobalElementDecl(t); // && getClassSelector().bindToType(t) instanceof ClassItem;
    }

    protected final boolean isGlobalElementDecl( XSTerm t ) {
        XSElementDecl e = t.asElementDecl();
        return e!=null && e.isGlobal();
    }


    /**
     * Gets the BIProperty object that applies to the given particle.
     */
    protected final BIProperty getLocalPropCustomization( XSParticle p ) {
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
    protected final String computeLabel( XSParticle p ) {
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
}

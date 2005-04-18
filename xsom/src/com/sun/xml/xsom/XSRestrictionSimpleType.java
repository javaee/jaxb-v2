/*
 * @(#)$Id: XSRestrictionSimpleType.java,v 1.2 2005-04-18 18:14:20 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

import java.util.Iterator;
import java.util.Collection;
import java.util.List;

/**
 * Restriction simple type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSRestrictionSimpleType extends XSSimpleType {
    // TODO
    
    /** Iterates facets that are specified in this step of derivation. */
    public Iterator<XSFacet> iterateDeclaredFacets();

    public Collection<? extends XSFacet> getDeclaredFacets();

    /**
     * Gets the declared facet object of the given name.
     * 
     * <p>
     * This method returns a facet object that is added in this
     * type and does not recursively check the ancestors.
     * 
     * <p>
     * For those facets that can have multiple values
     * (pattern facets and enumeration facets), this method
     * will return only the first one.
     *
     * @return
     *      Null if the facet is not specified in the last step
     *      of derivation.
     */
    XSFacet getDeclaredFacet( String name );

    /**
     * Gets the declared facets of the given name.
     *
     * This method is for those facets (such as 'pattern') that
     * can be specified multiple times on a simple type.
     *
     * @return
     *      can be empty but never be null.
     */
    List<XSFacet> getDeclaredFacets( String name );
}

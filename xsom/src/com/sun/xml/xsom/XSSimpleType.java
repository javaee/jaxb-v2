/*
 * @(#)$Id: XSSimpleType.java,v 1.2 2005-05-20 23:27:26 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;

/**
 * Simple type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSSimpleType extends XSType, XSContentType
{
    /**
     * Gets the base type as XSSimpleType.
     * 
     * Equivalent to
     * <code>
     * (XSSimpleType)getBaseType()
     * </code>
     * Since this is a simple type, we know that the base type
     * is also a simple type.
     *
     * The only exception is xs:anySimpleType, which has xs:anyType
     * as the base type.
     *
     * @return
     *      null if this is xs:anySimpleType. Otherwise non-null.
     */
    XSSimpleType getSimpleBaseType();
    
    /**
     * Gets the variety of this simple type.
     */
    XSVariety getVariety();
    
    
    /**
     * Gets the effective facet object of the given name.
     * 
     * <p>
     * For example, if a simple type "foo" is derived from
     * xs:string by restriction with the "maxLength" facet and
     * another simple type "bar" is derived from "foo" by
     * restriction with another "maxLength" facet, this method
     * will return the latter one, because that is the most
     * restrictive, effective facet.
     * 
     * <p>
     * For those facets that can have multiple values
     * (pattern facets and enumeration facets), this method
     * will return only the first one.
     * TODO: allow clients to access all of them by some means.
     * 
     * @return
     *      If this datatype has a facet of the given name,
     *      return that object. If the facet is not specified
     *      anywhere in its derivation chain, null will be returned.
     */
    XSFacet getFacet( String name );
    
    
    
    void visit( XSSimpleTypeVisitor visitor );
    <T> T apply( XSSimpleTypeFunction<T> function );
    
    /** Returns true if <code>this instanceof XSRestrictionSimpleType</code>. */
    boolean isRestriction();
    /** Returns true if <code>this instanceof XSListSimpleType</code>. */
    boolean isList();
    /** Returns true if <code>this instanceof XSUnionSimpleType</code>. */
    boolean isUnion();
    
    XSRestrictionSimpleType asRestriction();
    XSListSimpleType asList();
    XSUnionSimpleType asUnion();
}

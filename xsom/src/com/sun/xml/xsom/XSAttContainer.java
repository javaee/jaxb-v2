/*
 * @(#)$Id: XSAttContainer.java,v 1.1 2005-04-14 22:06:18 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

import java.util.Iterator;

/**
 * Common aspect of {@link XSComplexType} and {@link XSAttGroupDecl}
 * as the container of attribute uses/attribute groups.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSAttContainer extends XSDeclaration {
    XSWildcard getAttributeWildcard();
    
    /**
     * Looks for the attribute use with the specified name from
     * all the attribute uses that are directly/indirectly
     * referenced from this component.
     * 
     * <p>
     * This is the exact implementation of the "attribute use"
     * schema component.
     */
    XSAttributeUse getAttributeUse( String nsURI, String localName );
    
    /**
     * Lists all the attribute uses that are directly/indirectly
     * referenced from this component.
     * 
     * <p>
     * This is the exact implementation of the "attribute use"
     * schema component.
     */
    Iterator iterateAttributeUses();
    
    /**
     * Looks for the attribute use with the specified name from
     * the attribute uses which are declared in this complex type.
     * 
     * This does not include att uses declared in att groups that
     * are referenced from this complex type, nor does include
     * att uses declared in base types.
     */
    XSAttributeUse getDeclaredAttributeUse( String nsURI, String localName );
    
    /**
     * Lists all the attribute uses that are declared in this complex type.
     */
    Iterator iterateDeclaredAttributeUses();
    
    /**
     * Iterates all AttGroups which are directly referenced from
     * this component.
     */
    Iterator iterateAttGroups();
}

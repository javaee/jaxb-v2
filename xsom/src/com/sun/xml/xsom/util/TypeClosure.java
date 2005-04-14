/*
 * $Id: TypeClosure.java,v 1.1 2005-04-14 22:06:37 kohsuke Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSType;

/**
 * Perform a transitive closure operation on a type to determine if it
 * belongs to this set. 
 * 
 * The contains method returns true iff the TypeSet contains an instance
 * of the specified XSType or any of the base types of the XSType.
 * 
 * @author <a href="mailto:Ryan.Shoemaker@Sun.COM">Ryan Shoemaker</a>, Sun Microsystems, Inc.
 */
public class TypeClosure extends TypeSet {

    private final TypeSet typeSet;
    
    public TypeClosure(TypeSet typeSet) {
        this.typeSet = typeSet;
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.xsom.util.TypeSet#contains(com.sun.xml.xsom.XSDeclaration)
     * 
     * transitive closure variation on the contains method.
     */
    public boolean contains(XSType type) {
        if( typeSet.contains(type) ) {
            return true;
        } else {
            XSType baseType = type.getBaseType();
            if( baseType == null ) {
                return false;
            } else {
                // climb the super type hierarchy
                return contains(baseType);
            }
        }
    }

}

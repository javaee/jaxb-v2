/*
 * $Id: SimpleTypeSet.java,v 1.1 2005-04-14 22:06:37 kohsuke Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.util;

import java.util.Set;

import com.sun.xml.xsom.XSType;

/**
 * A very simple TypeSet.
 * 
 * The contains method returns true iff the set explicitly contains an
 * instance of the specified XSType.
 * 
 * @author <a href="mailto:Ryan.Shoemaker@Sun.COM">Ryan Shoemaker</a>, Sun Microsystems, Inc.
 */
public class SimpleTypeSet extends TypeSet {

    private final Set typeSet;
    
    public SimpleTypeSet(Set s) {
        typeSet = s;
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.xsom.util.TypeSet#contains(com.sun.xml.xsom.XSDeclaration)
     */
    public boolean contains(XSType type) {
        return typeSet.contains(type);
    }

}

/*
 * @(#)$Id: JGenerifiable.java,v 1.1 2005-04-15 20:02:51 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.codemodel;

/**
 * Declarations that can have type variables.
 * 
 * Something that can be made into a generic.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface JGenerifiable {
    /**
     * Adds a new type variable to this declaration.
     */
    JTypeVar generify( String name );
    
    /**
     * Adds a new type variable to this declaration with a bound.
     */
    JTypeVar generify( String name, Class bound );
    
    /**
     * Adds a new type variable to this declaration with a bound.
     */
    JTypeVar generify( String name, JClass bound );
    
    /**
     * Iterates all the type parameters of this class/interface.
     */
    JTypeVar[] typeParams();
}

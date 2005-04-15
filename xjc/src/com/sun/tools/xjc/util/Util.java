/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.util;


/**
 * Other miscellaneous utility methods. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class Util {
    private Util() {}   // no instanciation please
    
    /**
     * An easier-to-use version of the System.getProperty method
     * that doesn't throw an exception even if a property cannot be
     * read.
     */
    public static final String getSystemProperty( String name ) {
        try {
            return System.getProperty(name); 
        } catch( SecurityException e ) {
            return null;
        }
    }
    
    /**
     * Calls the other getSystemProperty method with
     * "[clazz]&#x2E;[name].
     */
    public static final String getSystemProperty( Class clazz, String name ) {
        return getSystemProperty( clazz.getName()+'.'+name );
    }
    
    /** 
     * Calculate an appropriate initialCapacity for a HashMap to avoid 
     * rehashing at runtime.
     * @param count the number of expected items in the HashMap
     * @param loadFactor the desired loadFactor of the HahsMap 
     */
    public static int calculateInitialHashMapCapacity(int count, float loadFactor) {
        int initialCapacity = (int)Math.ceil( count / loadFactor ) + 1;
        
        if( initialCapacity < 16 ) {
            return 16; // default hashmap capacity
        } else {
            return initialCapacity; 
        }
    }
}

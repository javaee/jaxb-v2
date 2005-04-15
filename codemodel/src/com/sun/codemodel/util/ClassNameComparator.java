/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.util;

import java.util.Comparator;

import com.sun.codemodel.JClass;

/**
 * Comparator object that sorts {@link JClass}es in the order
 * of their names.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ClassNameComparator implements Comparator {
    private ClassNameComparator() {}
    
    public int compare(Object l, Object r) {
        return ((JClass)l).fullName().compareTo(
            ((JClass)r).fullName());
    }

    public static final Comparator theInstance = new ClassNameComparator();
}

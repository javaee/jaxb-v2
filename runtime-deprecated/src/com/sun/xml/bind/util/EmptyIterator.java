/*
 * @(#)$Id: EmptyIterator.java,v 1.1 2005-04-15 20:03:33 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator over an empty list.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class EmptyIterator implements Iterator {

    private EmptyIterator() {}

    public static final Iterator theInstance = new EmptyIterator();
    
    public boolean hasNext() {
        return false;
    }

    public Object next() {
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new IllegalStateException();
    }

}

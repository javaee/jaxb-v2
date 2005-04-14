/*
 * @(#)$Id: FilterIterator.java,v 1.1 2005-04-14 22:06:32 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.util;

import java.util.Iterator;

/**
 * {@link Iterator} that works as a filter to another {@link Iterator}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class FilterIterator implements Iterator {
    
    private final Iterator core;
    private Object next;
    
    protected FilterIterator( Iterator core ) {
        this.core = core;
    }
    
    /**
     * Implemented by the derived class to filter objects.
     * 
     * @return true
     *      to let the iterator return the object to the client.
     */
    protected abstract boolean allows( Object o );
    
    public boolean hasNext() {
        while(next==null && core.hasNext()) {
            // fetch next
            Object o = core.next();
            if( allows(o) )
                next = o;
        }
        return next!=null;
    }

    public Object next() {
        Object r = next;
        next = null;
        return r;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

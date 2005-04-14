/*
 * @(#)$Id: ConcatIterator.java,v 1.1 2005-04-14 22:06:32 kohsuke Exp $
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
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ConcatIterator implements Iterator {
    
    private Iterator lhs,rhs;
    
    public ConcatIterator(Iterator _lhs, Iterator _rhs) {
        this.lhs = _lhs;
        this.rhs = _rhs;
    }
    
    public boolean hasNext() {
        if( lhs!=null ) {
            if( lhs.hasNext() ) return true;
            lhs = null; // no more item in lhs
        }
        return rhs.hasNext();
    }

    public Object next() {
        if( lhs!=null )     return lhs.next();
        else                return rhs.next();
    }

    public void remove() {
        if( lhs!=null )     lhs.remove();
        else                rhs.remove();
    }

}

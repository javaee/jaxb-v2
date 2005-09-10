/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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

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

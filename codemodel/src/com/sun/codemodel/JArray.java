/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * JArray creation and initialization
 */
public class JArray extends JExpressionImpl {

    private final JType type;
    private final JExpression size;
    private List exprs = null;

    /**
     * Add an element to the array initializer
     */
    public JArray add(JExpression e) {
        if (exprs == null)
            exprs = new ArrayList();
        exprs.add(e);
        return this;
    }

    JArray(JType type, JExpression size) {
        this.type = type;
        this.size = size;
    }

    public void generate(JFormatter f) {
        
        // generally we produce new T[x], but when T is an array type (T=T'[])
        // then new T'[][x] is wrong. It has to be new T'[x][].
        int arrayCount = 0;
        JType t = type;
        
        while( t.isArray() ) {
            t = t.elementType();
            arrayCount++;
        }
        
        f.p("new").g(t).p('[');
        if (size != null)
            f.g(size);
        f.p(']');
        
        for( int i=0; i<arrayCount; i++ )
            f.p("[]");
        
        if ((size == null) || (exprs != null))
            f.p('{');
        if (exprs != null) {
            boolean first = true;
            if (exprs.size() > 0) {
                for (Iterator i = exprs.iterator(); i.hasNext();) {
                    if (!first)
                        f.p(',');
                    f.g((JExpression) (i.next()));
                    first = false;
                }
            }
        } else {
            f.p(' ');
        }
        if ((size == null) || (exprs != null))
            f.p('}');
    }

}

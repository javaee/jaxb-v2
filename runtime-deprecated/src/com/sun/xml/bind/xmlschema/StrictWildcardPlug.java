/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.xmlschema;

import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.xml.bind.GrammarImpl;

/**
 * Plug that implements the semantics of strict wildcard of XML Schema.
 * 
 * @since JAXB1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class StrictWildcardPlug extends GrammarImpl.Plug {
    
    /**
     * NameClass object that determines what this wildcard should allow.
     */
    protected final NameClass namespaces;
    
    private ExpressionPool pool;
    
    
    public StrictWildcardPlug( NameClass namespaces ) {
        this.namespaces = namespaces;
        this.exp = Expression.nullSet;
    }
    
    /**
     * Look for elements that belong to the wildcard and pick them up.
     */
    public void connect(ExpressionPool pool, Grammar[] others) {
        this.exp = Expression.nullSet;
        this.pool = pool;
        
        Walker walker = new Walker();
        for( int i=0; i<others.length; i++ )
            others[i].getTopLevel().visit(walker);
        
        this.pool = null;
    }
    
    protected void onElementFound( ElementExp elem ) {
        // root element
        if( namespaces.includes(elem.getNameClass()) ) {
            // add this element
            this.exp = pool.createChoice( this.exp, elem );
        }
    }
    
    private class Walker extends ExpressionWalker {
        public void onElement(ElementExp exp) {
            onElementFound(exp);
        }
    }
}

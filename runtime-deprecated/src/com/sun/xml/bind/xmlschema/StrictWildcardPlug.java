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

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.xmlschema;

import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;

/**
 * Plug that implements the semantics of the lax wildcard of XML Schema.
 * 
 * @since JAXB1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class LaxWildcardPlug extends StrictWildcardPlug {
    
    /**
     * Union of all the name classes found so far.
     */
    private NameClass found;
    
    
    /**
     * Constructor for LaxWildcardPlug.
     * @param namespaces
     */
    public LaxWildcardPlug(NameClass namespaces) {
        super(namespaces);
    }

    public void connect(ExpressionPool pool, Grammar[] others) {
        found = null;
        super.connect(pool,others);
        
        NameClass rest = new DifferenceNameClass(namespaces,found).simplify();
        // build up lax content model
        ReferenceExp r = new ReferenceExp("lax");
        r.exp = pool.createZeroOrMore(pool.createChoice(
            pool.createAnyString(),
            pool.createChoice(
                pool.createAttribute(NameClass.ALL),
                new ElementPattern(NameClass.ALL,r))));
        
        
        this.exp = pool.createChoice( this.exp,
            new ElementPattern(rest,r));            
    }
    
    protected void onElementFound( ElementExp elem ) {
        super.onElementFound(elem);
        
        // record what are found
        if(found==null)     found = elem.getNameClass();
        else                found = new ChoiceNameClass(found,elem.getNameClass());
    }

}

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

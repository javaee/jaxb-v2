/*
 * @(#)$Id: AttGroupDeclImpl.java,v 1.2 2005-04-21 16:42:12 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.parser.DelayedRef;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

import java.util.Iterator;

public class AttGroupDeclImpl extends AttributesHolder implements XSAttGroupDecl
{
    public AttGroupDeclImpl( SchemaImpl _parent, AnnotationImpl _annon,
        Locator _loc, ForeignAttributesImpl _fa, String _name, WildcardImpl _wildcard ) {
        
        this(_parent,_annon,_loc,_fa,_name);
        setWildcard(_wildcard);
    }
        
    public AttGroupDeclImpl( SchemaImpl _parent, AnnotationImpl _annon, 
        Locator _loc, ForeignAttributesImpl _fa, String _name ) {
            
        super(_parent,_annon,_loc,_fa,_name,false);
    }
    
    
    private WildcardImpl wildcard;
    public void setWildcard( WildcardImpl wc ) { wildcard=wc; }
    public XSWildcard getAttributeWildcard() { return wildcard; }

    public XSAttributeUse getAttributeUse( String nsURI, String localName ) {
        UName name = new UName(nsURI,localName);
        XSAttributeUse o=null;
        
        Iterator itr = iterateAttGroups();
        while(itr.hasNext() && o==null)
            o = ((XSAttGroupDecl)itr.next()).getAttributeUse(nsURI,localName);
        
        if(o==null)     o = attributes.get(name);
        
        return o;
    }
    
    public void redefine( AttGroupDeclImpl ag ) {
        for (Iterator itr = attGroups.iterator(); itr.hasNext();) {
            DelayedRef.AttGroup r = (DelayedRef.AttGroup) itr.next();
            r.redefine(ag);
        }
    }
    
    public void visit( XSVisitor visitor ) {
        visitor.attGroupDecl(this);
    }
    public Object apply( XSFunction function ) {
        return function.attGroupDecl(this);
    }
}

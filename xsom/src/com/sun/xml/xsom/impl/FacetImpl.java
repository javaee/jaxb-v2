/*
 * @(#)$Id: FacetImpl.java,v 1.1 2005-04-14 22:06:25 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public class FacetImpl extends ComponentImpl implements XSFacet {
    public FacetImpl( SchemaImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa,
        String _name, String _value,
        ValidationContext _context, boolean _fixed ) {
        
        super(owner,_annon,_loc,_fa);
        
        this.name = _name;
        this.value = _value;
        this.context = _context;
        this.fixed = _fixed;
    }
    
    private final String name;
    public String getName() { return name; }
    
    private final String value;
    public String getValue() { return value; }

    private final ValidationContext context;
    public ValidationContext getContext() { return context; }
    
    private boolean fixed;
    public boolean isFixed() { return fixed; }
    
    
    public void visit( XSVisitor visitor ) {
        visitor.facet(this);
    }
    public Object apply( XSFunction function ) {
        return function.facet(this);
    }
}

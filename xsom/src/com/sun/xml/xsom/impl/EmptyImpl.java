/*
 * @(#)$Id: EmptyImpl.java,v 1.1 2005-04-14 22:06:25 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class EmptyImpl extends ComponentImpl implements ContentTypeImpl {
    public EmptyImpl() { super(null,null,null,null); }
    
    public XSSimpleType asSimpleType()  { return null; }
    public XSParticle asParticle()      { return null; }
    public XSContentType asEmpty()      { return this; }
    
    public Object apply( XSContentTypeFunction function ) {
        return function.empty(this);
    }
    public Object apply( XSFunction function ) {
        return function.empty(this);
    }
    public void visit( XSVisitor visitor ) {
        visitor.empty(this);
    }
    public void visit( XSContentTypeVisitor visitor ) {
        visitor.empty(this);
    }
    
    public XSContentType getContentType() { return this; }
}
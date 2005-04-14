/*
 * @(#)$Id: NotationImpl.java,v 1.1 2005-04-14 22:06:26 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NotationImpl extends DeclarationImpl implements XSNotation {
    
    public NotationImpl( SchemaImpl owner, AnnotationImpl _annon,
        Locator _loc, ForeignAttributesImpl _fa, String _tns, String _name,
        String _publicId, String _systemId ) {
        super(owner,_annon,_loc,_fa,_tns,_name,false);
        
        this.publicId = _publicId;
        this.systemId = _systemId;
    }
    
    private final String publicId;
    private final String systemId;
    
    public String getPublicId() { return publicId; }
    public String getSystemId() { return systemId; }

    public void visit(XSVisitor visitor) {
        visitor.notation(this);
    }

    public Object apply(XSFunction function) {
        return function.notation(this);
    }

}

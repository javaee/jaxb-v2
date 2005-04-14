/*
 * @(#)$Id: XSFunction.java,v 1.1 2005-04-14 22:06:39 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.impl.IdentityConstraintImpl;
import com.sun.xml.xsom.impl.XPathImpl;

/**
 * Function object that works on the entire XML Schema components.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSFunction<T> extends XSContentTypeFunction<T>, XSTermFunction<T> {
        
    T annotation( XSAnnotation ann );
    T attGroupDecl( XSAttGroupDecl decl );
    T attributeDecl( XSAttributeDecl decl );
    T attributeUse( XSAttributeUse use );
    T complexType( XSComplexType type );
    T schema( XSSchema schema );
//    T schemaSet( XSSchemaSet schema );
    T facet( XSFacet facet );
    T notation( XSNotation notation );
    T identityConstraint(XSIdentityConstraint decl);
    T xpath(XSXPath xpath);
}

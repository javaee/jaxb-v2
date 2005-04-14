/*
 * @(#)$Id: SubstGroupBaseTypeRef.java,v 1.1 2005-04-14 22:06:31 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.Ref;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SubstGroupBaseTypeRef implements Ref.Type {
    private final Ref.Element e;
    
    public SubstGroupBaseTypeRef( Ref.Element _e ) {
        this.e = _e;
    }

    public XSType getType() {
        return e.get().getType();
    }
}

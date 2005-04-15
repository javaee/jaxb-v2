/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.unmarshaller;

import org.xml.sax.Attributes;

/**
 * ContentHandlerEx implementation that discards the whole sub-tree.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
class Discarder extends ContentHandlerEx {
    
    // nest level of elements.
    private int depth = 0;
        
    public Discarder(UnmarshallingContext _ctxt) {
        // this class don't use the state, but to make the base class happy,
        // we need to pretend that we are always in the state #0.
        super(_ctxt, "-");
    }

    public void enterAttribute(String uri, String local) throws UnreportedException {
        return;
    }

    public void enterElement(String uri, String local, Attributes atts) throws UnreportedException {
        depth++;
        return;
    }

    public void leaveAttribute(String uri, String local) throws UnreportedException {
        return;
    }

    public void leaveElement(String uri, String local) throws UnreportedException {
        depth--;
        if(depth==0)
            context.popContentHandler();
        return;
    }

    protected UnmarshallableObject owner() {
        return null;
    }

    public void text(String s) throws UnreportedException {
        return;
    }

}

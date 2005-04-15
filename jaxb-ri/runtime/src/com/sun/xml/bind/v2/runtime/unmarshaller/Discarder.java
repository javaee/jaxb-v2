/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: Discarder.java,v 1.1 2005-04-15 20:04:40 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

/**
 * {@link UnmarshallingEventHandler} implementation that discards the whole sub-tree.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class Discarder implements UnmarshallingEventHandler {
    
    // nest level of elements.
    private int depth = 0;
    
    
    public Discarder() {
    }

    public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        depth++;
    }

    public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        depth--;
        if(depth==0)
            context.popContentHandler();
    }

    public void text(UnmarshallingContext context, CharSequence s) throws SAXException {
    }

    public void leaveChild(UnmarshallingContext context, Object child) throws SAXException {
    }

    public void activate(UnmarshallingContext context) throws SAXException {
    }

    public void deactivated(UnmarshallingContext context) throws SAXException {
    }
}

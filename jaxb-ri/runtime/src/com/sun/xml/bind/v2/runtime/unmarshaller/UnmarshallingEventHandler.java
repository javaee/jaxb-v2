/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: UnmarshallingEventHandler.java,v 1.1 2005-04-15 20:04:43 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

/**
 * Implemented by the generated code to unmarshall an object
 * from unmarshaller events.
 * 
 * <p>
 * Any {@link SAXException} throws from handler methods must have been
 * already reported to the context.
 *
 * <p>
 * The design of this interface allows {@link UnmarshallingEventHandler}
 * implementations to be immutable (thus reusable across multiple threads
 * that are unmarshalling objects simultaneously), but it can be mutable
 * if so desired.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface UnmarshallingEventHandler {

    /**
     * Receives notification of the start of an element.
     */
    void enterElement(UnmarshallingContext context, EventArg arg ) throws SAXException;

    /**
     * Receives notification of the end of an element.
     */
    void leaveElement(UnmarshallingContext context, EventArg arg ) throws SAXException;

    /**
     * Receives notification of a character data.
     */
    void text(UnmarshallingContext context, CharSequence s) throws SAXException;

    /**
     * Receives notification of a completed unmarshalling of a child object.
     */
    void leaveChild(UnmarshallingContext context, Object child) throws SAXException;

    /**
     * Called when a handler is pushed to the stack
     * {@link UnmarshallingContext#pushContentHandler}
     * or when it replaces another handler by
     * {@link UnmarshallingContext#setCurrentHandler}.
     */
    void activate(UnmarshallingContext context) throws SAXException;

    /**
     * When a handler is removed from the stack by
     * {@link UnmarshallingContext#popContentHandler}
     * or when it is replaced by
     * {@link UnmarshallingContext#setCurrentHandler},
     * the old handler receives this notification.
     */
    void deactivated(UnmarshallingContext context) throws SAXException;
}
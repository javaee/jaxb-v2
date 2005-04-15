/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id:
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;


/**
 * Pass around a 'ticket dispenser' when creating new
 * unmarshallers. This controls the index of the slot
 * allocated to the chain of handlers.

 *
 * <p>
 * A ticket dispenser also maintains the offset for handlers
 * to access state slots. A handler records this value when it's created.
 * 
 *
 */
public final class UnmarshallerChain {
    /**
     * This will point to the tail of the {@link Unmarshaller.Handler}
     * This will help when creating child unmarshallers
     */
    public Unmarshaller.Handler tail;

    /**
     * This will help maintain the position
     * relative to the stack in the UnmarshallingContext
     */
    private int offset = 0;

    public final JAXBContextImpl context;

    public UnmarshallerChain(JAXBContextImpl context) {
        this.context = context;
    }

    /**
     * Allocates a new scope offset.
     */
    public int allocateOffset() {
        return offset++;
    }

    /**
     * Gets the number of total scope offset allocated.
     */
    public int getScopeSize() {
        return offset;
    }
}


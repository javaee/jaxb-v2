/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

/*
 * @(#)$Id:
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.Scope;


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
     * This offset allows child unmarshallers to have its own {@link Scope} without colliding with siblings.
     */
    private int offset = 0;

    public final JAXBContextImpl context;

    public UnmarshallerChain(JAXBContextImpl context) {
        this.context = context;
    }

    /**
     * Allocates a new {@link Scope} offset.
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


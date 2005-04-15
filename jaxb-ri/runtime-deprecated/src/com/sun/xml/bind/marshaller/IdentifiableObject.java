/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.marshaller;

/**
 * This interface will be implemented by content tree classes
 * with ID, so that the marshaller can properly serialize ID value.
 * 
 * <p>
 * deprecated in 2.0
 *      We no longer expect the generated beans to implement
 *      any of the marker interfaces.
 * 
 * @since 1.0
 */
public interface IdentifiableObject
{
    /**
     * Gets the value of ID of this object.
     * 
     * To forestall the possibility of name collision with the
     * generated class, the method name is intentionally made long.
     */
    String ____jaxb____getId();
}

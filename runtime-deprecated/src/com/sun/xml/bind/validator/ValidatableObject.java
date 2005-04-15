/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.validator;

import com.sun.msv.verifier.DocumentDeclaration;
import com.sun.xml.bind.serializer.XMLSerializable;

/**
 * This interface is implemented by generated classes
 * to indicate that the class supports validation.
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface ValidatableObject extends XMLSerializable
{
    /** Gets the schema fragment associated with this class. */
    DocumentDeclaration createRawValidator();
    
    /**
     * Gets the main interface that this object implements.
     * 
     * For example, <code>FooImpl</code> will return <code>Foo</code>
     * from this method.
     */
    Class getPrimaryInterface();
}

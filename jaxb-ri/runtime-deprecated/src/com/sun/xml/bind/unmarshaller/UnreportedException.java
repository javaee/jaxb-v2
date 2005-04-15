/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.unmarshaller;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.Locator;

/**
 * A problem thrown in the form of this exception
 * has not been reported to the client application.
 * 
 * When you catch this exception, you must report it to
 * the client application before re-wrap it to UnmarshalException.
 * 
 * @deprecated in 1.0.1.
 *      Error should be reported to the appropriate error handler
 *      as soon as it is detected.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class UnreportedException extends Exception {
    public UnreportedException( String msg, Locator locator ) {
        this(msg,locator,null);
    }
    
    public UnreportedException( String msg, Locator locator, Exception nestedException ) {
        super(msg);
        this.locator = locator;
        this.nestedException = nestedException;
    }
    
    private final Locator locator;
    private final Exception nestedException;
    
    /**
     * Makes a ValidationEvent that holds the same information.
     */
    public ValidationEvent createValidationEvent() {
        return new ValidationEventImpl(
            ValidationEvent.ERROR,
            getMessage(),
            new ValidationEventLocatorImpl( locator ),
            nestedException );
    }
    
    /**
     * Makes an UnmarshalException from this.
     */
    public UnmarshalException createUnmarshalException() {
        return new UnmarshalException( getMessage(), nestedException );
    }
}

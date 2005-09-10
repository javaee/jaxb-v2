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

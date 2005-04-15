/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventImpl;

import com.sun.xml.bind.validator.Locator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An ErrorHandler that accepts a SAXParseException from the ValidatingUnmarshaller, 
 * creates a ValidationEvent, and passes it onto the client app's ValidationEventHandler.
 *
 * @see com.sun.xml.bind.v2.runtime.ValidatingUnmarshaller
 * @since 1.0
 */
public class ErrorHandlerToEventHandler implements org.xml.sax.ErrorHandler {
    
    /** the client event handler that will receive the validation events */
    private ValidationEventHandler veh = null;
    
    /** the locator object responsible for filling in the validation event
     *  location info **/
    private Locator locator = null;
   
    public ErrorHandlerToEventHandler( ValidationEventHandler handler, 
                                       Locator locator ) {
        veh = handler;
        this.locator = locator;
    }
    
    public void error(SAXParseException exception) 
        throws SAXException {
            
        propagateEvent( ValidationEvent.ERROR, exception );
    }
    
    public void warning(SAXParseException exception) 
        throws SAXException {
            
        propagateEvent( ValidationEvent.WARNING, exception );
    }
    
    public void fatalError(SAXParseException exception) 
        throws SAXException {
            
        propagateEvent( ValidationEvent.FATAL_ERROR, exception );
    }
    
    private void propagateEvent( int severity, SAXParseException saxException ) 
        throws SAXException {
            
        // get location info:
        //     sax locators simply use the location info embedded in the 
        //     sax exception, dom locators keep a reference to their DOMScanner
        //     and call back to figure out where the error occurred.
        ValidationEventLocator vel = 
            locator.getLocation( saxException );

        ValidationEventImpl ve = 
            new ValidationEventImpl( severity, saxException.getMessage(), vel  );

        Exception e = saxException.getException();
        if( e != null ) {
            ve.setLinkedException( e );
        } else {
            ve.setLinkedException( saxException );
        }
        
        // call the client's event handler.  If it returns false, then bail-out
        // and terminate the unmarshal operation.
        boolean result;
        try {
            result = veh.handleEvent( ve );
        } catch( RuntimeException re ) {
            // if the client's event handler causes a RuntimeException, then
            // we have to return false.
            result = false;
        }
        if( ! result ) {
            // bail-out of the parse with a SAX exception, but convert it into 
            // an UnmarshalException back in in the AbstractUnmarshaller
            throw saxException; 
        }
    }
    
}
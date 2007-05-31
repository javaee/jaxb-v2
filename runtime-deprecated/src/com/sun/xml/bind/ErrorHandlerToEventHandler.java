/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
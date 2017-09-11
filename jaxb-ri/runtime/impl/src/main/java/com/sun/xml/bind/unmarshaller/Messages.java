/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.bind.unmarshaller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 * 
 * @since JAXB 1.0
 */
public class Messages
{
    public static String format( String property ) {
        return format( property, null );
    }
    
    public static String format( String property, Object arg1 ) {
        return format( property, new Object[]{arg1} );
    }
    
    public static String format( String property, Object arg1, Object arg2 ) {
        return format( property, new Object[]{arg1,arg2} );
    }
    
    public static String format( String property, Object arg1, Object arg2, Object arg3 ) {
        return format( property, new Object[]{arg1,arg2,arg3} );
    }
    
    // add more if necessary.
    
    /** Loads a string resource and formats it with specified arguments. */
    public static String format( String property, Object[] args ) {
        String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
        return MessageFormat.format(text,args);
    }
    
//
//
// Message resources
//
//
    public static final String UNEXPECTED_ENTER_ELEMENT =  // arg:2
        "ContentHandlerEx.UnexpectedEnterElement";

    public static final String UNEXPECTED_LEAVE_ELEMENT =  // arg:2
        "ContentHandlerEx.UnexpectedLeaveElement";

    public static final String UNEXPECTED_ENTER_ATTRIBUTE =// arg:2
        "ContentHandlerEx.UnexpectedEnterAttribute";

    public static final String UNEXPECTED_LEAVE_ATTRIBUTE =// arg:2
        "ContentHandlerEx.UnexpectedLeaveAttribute";

    public static final String UNEXPECTED_TEXT =// arg:1
        "ContentHandlerEx.UnexpectedText";
        
    public static final String UNEXPECTED_LEAVE_CHILD = // 0 args
        "ContentHandlerEx.UnexpectedLeaveChild";
        
    public static final String UNEXPECTED_ROOT_ELEMENT = // 1 arg
        "SAXUnmarshallerHandlerImpl.UnexpectedRootElement";

    // Usage not found. TODO Remove
    // public static final String UNEXPECTED_ROOT_ELEMENT2 = // 3 arg
    //    "SAXUnmarshallerHandlerImpl.UnexpectedRootElement2";
        
    public static final String UNDEFINED_PREFIX = // 1 arg
        "Util.UndefinedPrefix";

    public static final String NULL_READER = // 0 args
        "Unmarshaller.NullReader";
    
    public static final String ILLEGAL_READER_STATE = // 1 arg
        "Unmarshaller.IllegalReaderState";
    
}

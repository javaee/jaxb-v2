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

package com.sun.xml.bind.marshaller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 * 
 * @since JAXB1.0
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
    static String format( String property, Object[] args ) {
        String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
        return MessageFormat.format(text,args);
    }
    
//
//
// Message resources
//
//
    public static final String NOT_MARSHALLABLE = // 0 args
        "MarshallerImpl.NotMarshallable";
        
    public static final String UNSUPPORTED_RESULT = // 0 args
        "MarshallerImpl.UnsupportedResult";
        
    public static final String UNSUPPORTED_ENCODING = // 1 arg
        "MarshallerImpl.UnsupportedEncoding";
    
    public static final String NULL_WRITER = // 0 args
        "MarshallerImpl.NullWriterParam";
    
    public static final String ASSERT_FAILED = // 0 args
        "SAXMarshaller.AssertFailed";
    
    /**
     * @deprecated use ERR_MISSING_OBJECT2
     */
    public static final String ERR_MISSING_OBJECT = // 0 args
        "SAXMarshaller.MissingObject";
    
    /**
     * @deprecated
     *  use {@link com.sun.xml.bind.v2.runtime.XMLSerializer#reportMissingObjectError(String)} 
     */
    public static final String ERR_MISSING_OBJECT2 = // 1 arg
        "SAXMarshaller.MissingObject2";
    
    /**
     * @deprecated only used from 1.0
     */
    public static final String ERR_DANGLING_IDREF = // 1 arg
        "SAXMarshaller.DanglingIDREF";

    /**
     * @deprecated only used from 1.0
     */
    public static final String ERR_NOT_IDENTIFIABLE = // 0 args
        "SAXMarshaller.NotIdentifiable";

    public static final String DOM_IMPL_DOESNT_SUPPORT_CREATELEMENTNS = // 2 args
        "SAX2DOMEx.DomImplDoesntSupportCreateElementNs";
}

/*
 * @(#)$Id: EncoderFactory.java,v 1.1 2005-04-15 20:03:24 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.marshaller;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Creates {@link java.nio.charset.CharsetEncoder} from a charset name.
 * 
 * Fixes a MS1252 handling bug in JDK1.4.2.
 * 
 * <p>
 * No generated code is directly depending on this, so we can potentially
 * remove this in future.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class EncoderFactory {
    static CharsetEncoder createEncoder( String encodin ) {
        Charset cs = Charset.forName(System.getProperty("file.encoding"));
        CharsetEncoder encoder = cs.newEncoder();
        
        if( cs.getClass().getName().equals("sun.nio.cs.MS1252") ) {
            try {
                // at least JDK1.4.2_01 has a bug in MS1252 encoder.
                // specifically, it returns true for any character.
                // return a correct encoder to workaround this problem
                
                // statically binding to MS1252Encoder will cause a Link error
                // (at least in IBM JDK1.4.1)
                Class ms1252encoder = Class.forName("com.sun.xml.bind.marshaller.MS1252Encoder");
                Constructor c = ms1252encoder.getConstructor(new Class[]{
                    Charset.class
                });
                return (CharsetEncoder)c.newInstance(new Object[]{cs});
            } catch( Throwable t ) {
                // if something funny happens, ignore it and fall back to
                // a broken MS1252 encoder. It's probably still better
                // than choking here.
                return encoder;
            }
        }
        
        return encoder;
    }
}

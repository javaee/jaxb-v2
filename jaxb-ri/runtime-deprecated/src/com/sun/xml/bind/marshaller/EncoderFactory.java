/*
 * @(#)$Id: EncoderFactory.java,v 1.2 2005-09-10 19:07:48 kohsuke Exp $
 */

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

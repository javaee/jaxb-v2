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

package com.sun.xml.bind.v2.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Kohsuke Kawaguchi
 */
public class RuntimeUtil {
    /**
     * XmlAdapter for printing arbitrary object by using {@link Object#toString()}.
     */
    public static final class ToStringAdapter extends XmlAdapter<String,Object> {
        public Object unmarshal(String s) {
            throw new UnsupportedOperationException();
        }

        public String marshal(Object o) {
            if(o==null)     return null;
            return o.toString();
        }
    }

    /**
     * Map from {@link Class} objects representing primitive types
     * to {@link Class} objects representing their boxed types.
     * <p>
     * e.g., {@code int -> Integer}.
     */
    public static final Map<Class,Class> boxToPrimitive;

    /**
     * Reverse map of {@link #boxToPrimitive}.
     */
    public static final Map<Class,Class> primitiveToBox;

    static {
        Map<Class,Class> b = new HashMap<Class,Class>();
        b.put(Byte.TYPE,Byte.class);
        b.put(Short.TYPE,Short.class);
        b.put(Integer.TYPE,Integer.class);
        b.put(Long.TYPE,Long.class);
        b.put(Character.TYPE,Character.class);
        b.put(Boolean.TYPE,Boolean.class);
        b.put(Float.TYPE,Float.class);
        b.put(Double.TYPE,Double.class);
        b.put(Void.TYPE,Void.class);

        primitiveToBox = Collections.unmodifiableMap(b);

        Map<Class,Class> p = new HashMap<Class,Class>();
        for( Map.Entry<Class,Class> e :  b.entrySet() )
            p.put(e.getValue(),e.getKey());

        boxToPrimitive = Collections.unmodifiableMap(p);
    }

    /**
     * Reports a print conversion error while marshalling.
     */
/*
    public static void handlePrintConversionException(
        Object caller, Exception e, XMLSerializer serializer ) throws SAXException {

        if( e instanceof SAXException )
            // assume this exception is not from application.
            // (e.g., when a marshaller aborts the processing, this exception
            //        will be thrown)
            throw (SAXException)e;

        ValidationEvent ve = new PrintConversionEventImpl(
            ValidationEvent.ERROR, e.getMessage(),
            new ValidationEventLocatorImpl(caller), e );
        serializer.reportError(ve);
    }
*/

    /**
     * Reports that the type of an object in a property is unexpected.
     */
/*
    public static void handleTypeMismatchError( XMLSerializer serializer,
            Object parentObject, String fieldName, Object childObject ) throws SAXException {

         ValidationEvent ve = new ValidationEventImpl(
            ValidationEvent.ERROR, // maybe it should be a fatal error.
             Messages.TYPE_MISMATCH.format(
                getTypeName(parentObject),
                fieldName,
                getTypeName(childObject) ),
            new ValidationEventLocatorExImpl(parentObject,fieldName) );

        serializer.reportError(ve);
    }
*/

    private static String getTypeName( Object o ) {
        return o.getClass().getName();
    }
}

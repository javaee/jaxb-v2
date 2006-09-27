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
package com.sun.tools.xjc.util;

import org.xml.sax.Locator;


/**
 * Other miscellaneous utility methods. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class Util {
    private Util() {}   // no instanciation please

    /**
     * An easier-to-use version of the System.getProperty method
     * that doesn't throw an exception even if a property cannot be
     * read.
     */
    public static String getSystemProperty( String name ) {
        try {
            return System.getProperty(name);
        } catch( SecurityException e ) {
            return null;
        }
    }

    /**
     * Compares if two {@link Locator}s point to the exact same position.
     */
    public static boolean equals(Locator lhs, Locator rhs) {
        return lhs.getLineNumber()==rhs.getLineNumber()
        && lhs.getColumnNumber()==rhs.getColumnNumber()
        && equals(lhs.getSystemId(),rhs.getSystemId())
        && equals(lhs.getPublicId(),rhs.getPublicId());
    }

    private static boolean equals(String lhs, String rhs) {
        if(lhs==null && rhs==null)  return true;
        if(lhs==null || rhs==null)  return false;
        return lhs.equals(rhs);
    }

    /**
     * Calls the other getSystemProperty method with
     * "[clazz]&#x2E;[name].
     */
    public static String getSystemProperty( Class clazz, String name ) {
        return getSystemProperty( clazz.getName()+'.'+name );
    }
}

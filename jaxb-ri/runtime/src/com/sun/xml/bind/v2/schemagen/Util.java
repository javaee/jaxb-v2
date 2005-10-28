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
package com.sun.xml.bind.v2.schemagen;


/**
 * TODO: JAX-WS dependes on this class - consider moving it somewhere more stable, Notify JAX-WS before modifying anything...
 *
 * Other miscellaneous utility methods. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class Util {
    private Util() {}   // no instanciation please
    
    /**
     * Escape any characters that would cause the single arg constructor
     * of java.net.URI to complain about illegal chars.
     *
     * @param s source string to be escaped
     */
    public static String escapeURI(String s) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt(i);
            if(Character.isSpaceChar(c)) {
                sb.append("%20");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Calculate the parent URI path of the given URI path.
     *
     * @param uriPath the uriPath (as returned by java.net.URI#getPath()
     * @return the parent URI path of the given URI path
     */
    public static String getParentUriPath(String uriPath) {
        int idx = uriPath.lastIndexOf('/');

        if (uriPath.endsWith("/")) {
            uriPath = uriPath.substring(0,idx); // trim trailing slash
            idx = uriPath.lastIndexOf('/'); // move idx to parent context
        }

        return uriPath.substring(0, idx)+"/";
    }

    /**
     * Calculate the normalized form of the given uriPath.
     *
     * For example:
     *    /a/b/c/ -> /a/b/c/
     *    /a/b/c  -> /a/b/
     *    /a/     -> /a/
     *    /a      -> /
     *
     * @param uriPath path of a URI (as returned by java.net.URI#getPath()
     * @return the normalized uri path
     */
    public static String normalizeUriPath(String uriPath) {
        if (uriPath.endsWith("/"))
            return uriPath;

        // the uri path should always have at least a leading slash,
        // so no need to make sure that ( idx == -1 )
        int idx = uriPath.lastIndexOf('/');
        return uriPath.substring(0, idx+1);
    }

    /**
     * determine if two Strings are equal ignoring case allowing null values
     *
     * @param s string 1
     * @param t string 2
     * @return true iff the given strings are equal ignoring case, false if they aren't
     * equal or either of them are null.
     */
    public static boolean equalsIgnoreCase(String s, String t) {
        if (s == t) return true;
        if ((s != null) && (t != null)) {
            return s.equalsIgnoreCase(t);
        }
        return false;
    }

    /**
     * determine if two Strings are iqual allowing null values
     *
     * @param s string 1
     * @param t string 2
     * @return true iff the strings are equal, false if they aren't equal or either of
     * them are null.
     */
    public static boolean equal(String s, String t) {
        if (s == t) return true;
        if ((s != null) && (t != null)) {
            return s.equals(t);
        }
        return false;
    }    
}

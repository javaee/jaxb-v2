/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.sun.codemodel.JJavaName;

import org.xml.sax.InputSource;

public class Util
{
    /**
     * Parses the specified string either as an {@link URL} or as a {@link File}.
     *
     * @throws IOException
     *      if the parameter is neither.
     */
    public static Object getFileOrURL(String fileOrURL) throws IOException {
        try {
            return new URL(fileOrURL).toExternalForm();
        } catch (MalformedURLException e) {
            return new File(fileOrURL).getCanonicalFile();
        }
    }
    /**
     * Gets an InputSource from a string, which contains either
     * a file name or an URL.
     */
    public static InputSource getInputSource(String fileOrURL) {
        try {
            Object o = getFileOrURL(fileOrURL);
            if(o instanceof URL) {
                return new InputSource(escapeSpace(((URL)o).toExternalForm()));
            } else {
                String url = ((File)o).toURL().toExternalForm();
                return new InputSource(escapeSpace(url));
            }
        } catch (IOException e) {
            return new InputSource(fileOrURL);
        }
    }

    public static String escapeSpace( String url ) {
        // URLEncoder didn't work.
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            // TODO: not sure if this is the only character that needs to be escaped.
            if (url.charAt(i) == ' ')
                buf.append("%20");
            else
                buf.append(url.charAt(i));
        }
        return buf.toString();
    }
    
    
    /**
     * Computes a Java package name from a namespace URI,
     * as specified in the spec.
     * 
     * @return
     *      null if it fails to derive a package name.
     */
    public static String getPackageNameFromNamespaceURI( String nsUri ) {
        // remove scheme and :, if present
        // spec only requires us to remove 'http' and 'urn'...
        int idx = nsUri.indexOf(':');
        String scheme = "";
        if(idx>=0) {
            scheme = nsUri.substring(0,idx);
            if( scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("urn") )
                nsUri = nsUri.substring(idx+1);
        }
        
        // tokenize string
        ArrayList<String> tokens = tokenize( nsUri, "/: " );
        if( tokens.size() == 0 ) {
            return null;
        }
        
        // remove trailing file type, if necessary
        if( tokens.size() > 1 ) {
            // for uri's like "www.foo.com" and "foo.com", there is no trailing
            // file, so there's no need to look at the last '.' and substring
            // otherwise, we loose the "com" (which would be wrong) 
            String lastToken = tokens.get( tokens.size()-1 );
            idx = lastToken.lastIndexOf( '.' );
            if( idx > 0 ) {
                lastToken = lastToken.substring( 0, idx );
                tokens.set( tokens.size()-1, lastToken );
            }
        }
        
        // tokenize domain name and reverse.  Also remove :port if it exists
        String domain = tokens.get( 0 );
        idx = domain.indexOf(':');
        if( idx >= 0) domain = domain.substring(0, idx);
        ArrayList<String> r = reverse( tokenize( domain, scheme.equals("urn")?".-":"." ) );
        if( r.get( r.size()-1 ).equalsIgnoreCase( "www" ) ) {
            // remove leading www
            r.remove( r.size()-1 );
        }
        
        // replace the domain name with tokenized items
        tokens.addAll( 1, r );
        tokens.remove( 0 );            
        
        // iterate through the tokens and apply xml->java name algorithm
        for( int i = 0; i < tokens.size(); i++ ) {
            
            // get the token and remove illegal chars
            String token = tokens.get( i );
            token = removeIllegalIdentifierChars( token );

            // this will check for reserved keywords
            if( !JJavaName.isJavaIdentifier( token ) ) {
                token = '_' + token;
            }

            tokens.set( i, token.toLowerCase() );
        }
        
        // concat all the pieces and return it
        return combine( tokens, '.' );
    }

    private static String removeIllegalIdentifierChars(String token) {
        StringBuffer newToken = new StringBuffer();
        for( int i = 0; i < token.length(); i++ ) {
            char c = token.charAt( i );
            
            if( i ==0 && !Character.isJavaIdentifierStart( c ) ) {
                // prefix an '_' if the first char is illegal
                newToken.append( "_" + c );
            } else if( !Character.isJavaIdentifierPart( c ) ) {
                // replace the char with an '_' if it is illegal
                newToken.append( '_' );
            } else {
                // add the legal char
                newToken.append( c );
            }
        }
        return newToken.toString();
    }

    
    private static ArrayList<String> tokenize( String str, String sep ) {
        StringTokenizer tokens = new StringTokenizer(str,sep);
        ArrayList<String> r = new ArrayList<String>();
        
        while(tokens.hasMoreTokens())
            r.add( tokens.nextToken() );
        
        return r;
    }

    private static <T> ArrayList<T> reverse( List<T> a ) {
        ArrayList<T> r = new ArrayList<T>();
        
        for( int i=a.size()-1; i>=0; i-- )
            r.add( a.get(i) );
        
        return r;
    }
    
    private static String combine( List r, char sep ) {
        StringBuilder buf = new StringBuilder(r.get(0).toString());
        
        for( int i=1; i<r.size(); i++ ) {
            buf.append(sep);
            buf.append(r.get(i));   
        }
        
        return buf.toString();
    }
    
}

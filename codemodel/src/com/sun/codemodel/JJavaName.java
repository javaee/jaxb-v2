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

package com.sun.codemodel;

import java.util.HashSet;

/**
 * Utility methods that convert arbitrary strings into Java identifiers.
 */
public class JJavaName {


    /**
     * Checks if a given string is usable as a Java identifier.
     */
    public static boolean isJavaIdentifier(String s) {
        if(s.length()==0)   return false;
        if( reservedKeywords.contains(s) )  return false;
        
        if(!Character.isJavaIdentifierStart(s.charAt(0)))   return false;
        
        for (int i = 1; i < s.length(); i++)
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
                return false;
                
        return true;
    }
    
    /**
     * Checks if the given string is a valid Java package name.
     */
    public static boolean isJavaPackageName(String s) {
        while(s.length()!=0) {
            int idx = s.indexOf('.');
            if(idx==-1) idx=s.length();
            if( !isJavaIdentifier(s.substring(0,idx)) )
                return false;
            
            s = s.substring(idx);
            if(s.length()!=0)    s = s.substring(1);    // remove '.'
        }
        return true;
    }
    
    
    /** All reserved keywords of Java. */
    private static HashSet<String> reservedKeywords = new HashSet<String>();
    
    static {
        // see http://java.sun.com/docs/books/tutorial/java/nutsandbolts/_keywords.html
        String[] words = new String[]{
            "abstract",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while",
            
            // technically these are not reserved words but they cannot be used as identifiers.
            "true",
            "false",
            "null",
            
            // and I believe assert is also a new keyword
            "assert",

            // and 5.0 keywords
            "enum"
            };
        for (String w : words)
            reservedKeywords.add(w);
    }
}

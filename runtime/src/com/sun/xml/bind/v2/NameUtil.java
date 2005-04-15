package com.sun.xml.bind.v2;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Methods that convert strings into various formats.
 *
 * <p>
 * What JAX-RPC name binding tells us is that even such basic method
 * like "isLetter" can be different depending on the situation.
 *
 * For this reason, a whole lot of methods are made non-static,
 * even though they look like they should be static.
 */
class NameUtil {
    protected boolean isPunct(char c) {
        return (c == '-' || c == '.' || c == ':' || c == '_' || c == '\u00b7'
                || c == '\u0387' || c == '\u06dd' || c == '\u06de');
    }

    protected boolean isDigit(char c) {
        return ((c >= '0' && c <= '9') || Character.isDigit(c));
    }

    protected boolean isUpper(char c) {
        return ((c >= 'A' && c <= 'Z') || Character.isUpperCase(c));
    }

    protected boolean isLower(char c) {
        return ((c >= 'a' && c <= 'z') || Character.isLowerCase(c));
    }

    protected boolean isLetter(char c) {
        return ((c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || Character.isLetter(c));
    }

    /**
     * Capitalizes the first character of the specified string,
     * and de-capitalize the rest of characters.
     */
    public String capitalize(String s) {
        if (!isLower(s.charAt(0)))
            return s;
        StringBuilder sb = new StringBuilder(s.length());
        sb.append(Character.toUpperCase(s.charAt(0)));
        sb.append(s.substring(1).toLowerCase());
        return sb.toString();
    }

    // Precondition: s[start] is not punctuation
    protected int nextBreak(String s, int start) {
        int n = s.length();
        for (int i = start; i < n; i++) {
            char c0 = s.charAt(i);
            if (i < n - 1) {
                char c1 = s.charAt(i + 1);
                if (isPunct(c1)) return i + 1;
                if (isDigit(c0) && !isDigit(c1)) return i + 1;
                if (!isDigit(c0) && isDigit(c1)) return i + 1;
                if (isLower(c0) && !isLower(c1)) return i + 1;
                if (isLetter(c0) && !isLetter(c1)) return i + 1;
                if (!isLetter(c0) && isLetter(c1)) return i + 1;
                if (i < n - 2) {
                    char c2 = s.charAt(i + 2);
                    if (isUpper(c0) && isUpper(c1) && isLower(c2))
                        return i + 1;
                }
            }
        }
        return -1;
    }


    /**
     * Tokenizes a string into words and capitalizes the first
     * character of each word.
     *
     * <p>
     * This method uses a change in character type as a splitter
     * of two words. For example, "abc100ghi" will be splitted into
     * {"Abc", "100","Ghi"}.
     */
    public String[] toWordList(String s) {
        ArrayList<String> ss = new ArrayList<String>();
        int n = s.length();
        for (int i = 0; i < n;) {

            // Skip punctuation
            while (i < n) {
                if (!isPunct(s.charAt(i)))
                    break;
                i++;
            }
            if (i >= n) break;

            // Find next break and collect word
            int b = nextBreak(s, i);
            String w = (b == -1) ? s.substring(i) : s.substring(i, b);
            ss.add(escape(capitalize(w)));
            if (b == -1) break;
            i = b;
        }

//      we can't guarantee a valid Java identifier anyway,
//      so there's not much point in rejecting things in this way.
//        if (ss.size() == 0)
//            throw new IllegalArgumentException("Zero-length identifier");
        return ss.toArray(new String[0]);
    }

    protected String toMixedCaseName(String[] ss, boolean startUpper) {
        StringBuilder sb = new StringBuilder();
        if(ss.length>0) {
            sb.append(startUpper ? ss[0] : ss[0].toLowerCase());
            for (int i = 1; i < ss.length; i++)
                sb.append(ss[i]);
        }
        return sb.toString();
    }

    protected String toMixedCaseVariableName(String[] ss,
                                                  boolean startUpper,
                                                  boolean cdrUpper) {
        if (cdrUpper)
            for (int i = 1; i < ss.length; i++)
                ss[i] = capitalize(ss[i]);
        StringBuilder sb = new StringBuilder();
        if( ss.length>0 ) {
            sb.append(startUpper ? ss[0] : ss[0].toLowerCase());
            for (int i = 1; i < ss.length; i++)
                sb.append(ss[i]);
        }
        return sb.toString();
    }


    /**
     * Formats a string into "THIS_KIND_OF_FORMAT_ABC_DEF".
     *
     * @return
     *      Always return a string but there's no guarantee that
     *      the generated code is a valid Java identifier.
     */
    public String toConstantName(String s) {
        return toConstantName(toWordList(s));
    }

    /**
     * Formats a string into "THIS_KIND_OF_FORMAT_ABC_DEF".
     *
     * @return
     *      Always return a string but there's no guarantee that
     *      the generated code is a valid Java identifier.
     */
    public String toConstantName(String[] ss) {
        StringBuilder sb = new StringBuilder();
        if( ss.length>0 ) {
            sb.append(ss[0].toUpperCase());
            for (int i = 1; i < ss.length; i++) {
                sb.append('_');
                sb.append(ss[i].toUpperCase());
            }
        }
        return sb.toString();
    }



    /**
     * Escapes characters is the given string so that they can be
     * printed by only using US-ASCII characters.
     *
     * The escaped characters will be appended to the given
     * StringBuffer.
     *
     * @param sb
     *      StringBuffer that receives escaped string.
     * @param s
     *      String to be escaped. <code>s.substring(start)</code>
     *      will be escaped and copied to the string buffer.
     */
    public static void escape(StringBuilder sb, String s, int start) {
        int n = s.length();
        for (int i = start; i < n; i++) {
            char c = s.charAt(i);
            if (Character.isJavaIdentifierPart(c))
                sb.append(c);
            else {
                sb.append('_');
                if (c <= '\u000f') sb.append("000");
                else if (c <= '\u00ff') sb.append("00");
                else if (c <= '\u0fff') sb.append('0');
                sb.append(Integer.toString(c, 16));
            }
        }
    }

    /**
     * Escapes characters that are unusable as Java identifiers
     * by replacing unsafe characters with safe characters.
     */
    private static String escape(String s) {
        int n = s.length();
        for (int i = 0; i < n; i++)
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                StringBuilder sb = new StringBuilder(s.substring(0, i));
                escape(sb, s, i);
                return sb.toString();
            }
        return s;
    }


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
        for( int i=0; i<words.length; i++ )
            reservedKeywords.add(words[i]);
    }
}

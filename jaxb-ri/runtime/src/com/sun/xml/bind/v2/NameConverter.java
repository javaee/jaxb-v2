package com.sun.xml.bind.v2;

/**
 * Converts aribitrary strings into Java identifiers.
 *
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface NameConverter
{
    /**
     * converts a string into an identifier suitable for classes.
     *
     * In general, this operation should generate "NamesLikeThis".
     */
    String toClassName( String token );

    /**
     * converts a string into an identifier suitable for interfaces.
     *
     * In general, this operation should generate "NamesLikeThis".
     * But for example, it can prepend every interface with 'I'.
     */
    String toInterfaceName( String token );

    /**
     * converts a string into an identifier suitable for properties.
     *
     * In general, this operation should generate "NamesLikeThis",
     * which will be used with known prefixes like "get" or "set".
     */
    String toPropertyName( String token );

    /**
     * converts a string into an identifier suitable for constants.
     *
     * In the standard Java naming convention, this operation should
     * generate "NAMES_LIKE_THIS".
     */
    String toConstantName( String token );

    /**
     * Converts a string into an identifier suitable for variables.
     *
     * In general it should generate "namesLikeThis".
     */
    String toVariableName( String token );

    /**
     * Converts a string into a package name.
     * This method should expect input like "org", "ACME", or "Foo"
     * and return something like "org", "acme", or "foo" respectively
     * (assuming that it follows the standard Java convention.)
     */
    String toPackageName( String token );

    /**
     * The name converter implemented by Code Model.
     *
     * This is the standard name conversion for JAXB.
     */
    public static final NameConverter standard = new Standard();

    static class Standard extends NameUtil implements NameConverter {
        public String toClassName(String s) {
            return toMixedCaseName(toWordList(s), true);
        }
        public String toVariableName(String s) {
            return toMixedCaseName(toWordList(s), false);
        }
        public String toInterfaceName( String token ) {
            return toClassName(token);
        }
        public String toPropertyName(String s) {
            return toClassName(s);
        }
        public String toConstantName( String token ) {
            return super.toConstantName(token);
        }
        public String toPackageName( String s ) {
            return toMixedCaseName(toWordList(s), false );
        }
    };

    /**
     * JAX-PRC compatible name converter implementation.
     *
     * The only difference is that we treat '_' as a valid character
     * and not as a word separator.
     */
    public static final NameConverter jaxrpcCompatible = new Standard() {
        protected boolean isPunct(char c) {
            return (c == '.' || c == '-' || c == ';' /*|| c == '_'*/ || c == '\u00b7'
                    || c == '\u0387' || c == '\u06dd' || c == '\u06de');
        }
        protected boolean isLetter(char c) {
            return super.isLetter(c) || c=='_';
        }

        protected int classify(char c0) {
            if(c0=='_') return OTHER_LETTER;
            return super.classify(c0);
        }
    };

    /**
     * Smarter converter used for RELAX NG support.
     */
    public static final NameConverter smart = new Standard() {
        public String toConstantName( String token ) {
            String name = super.toConstantName(token);
            if( isJavaIdentifier(name) )
                return name;
            else
                return '_'+name;
        }
    };
}

package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class ParsedElementAnnotationHost implements ParsedElementAnnotation {
    final ParsedElementAnnotation lhs;
    final ParsedElementAnnotation rhs;
    
    ParsedElementAnnotationHost( ParsedElementAnnotation lhs, ParsedElementAnnotation rhs ) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

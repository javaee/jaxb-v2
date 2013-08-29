package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.ParsedPattern;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class ParsedPatternHost implements ParsedPattern {
    public final ParsedPattern lhs;
    public final ParsedPattern rhs;
    
    ParsedPatternHost( ParsedPattern lhs, ParsedPattern rhs ) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

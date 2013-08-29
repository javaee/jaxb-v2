package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.ParsedNameClass;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class ParsedNameClassHost implements ParsedNameClass {
    final ParsedNameClass lhs;
    final ParsedNameClass rhs;
    
    ParsedNameClassHost( ParsedNameClass lhs, ParsedNameClass rhs ) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

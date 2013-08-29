package org.kohsuke.rngom.ast.util;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.binary.SchemaBuilderImpl;
import org.kohsuke.rngom.binary.SchemaPatternBuilder;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.host.ParsedPatternHost;
import org.kohsuke.rngom.parse.host.SchemaBuilderHost;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;

/**
 * Wraps a {@link SchemaBuilder} and does all the semantic checks
 * required by the RELAX NG spec.
 * 
 * <h2>Usage</h2>
 * <p>
 * Whereas you normally write it as follows:
 * <pre>
 * YourParsedPattern r = (YourParsedPattern)parseable.parse(sb);
 * </pre>
 * write this as follows:
 * <pre>
 * YourParsedPattern r = (YourParsedPattern)parseable.parse(new CheckingSchemaBuilder(sb,eh));
 * </pre>
 * 
 * <p>
 * The checking is done by using the <tt>rngom.binary</tt> package, so if you are using
 * that package for parsing schemas, then there's no need to use this.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class CheckingSchemaBuilder extends SchemaBuilderHost {
    /**
     * 
     * @param sb
     *      Your {@link SchemaBuilder} that parses RELAX NG schemas.
     * @param eh
     *      All the errors found will be sent to this handler.
     */
    public CheckingSchemaBuilder( SchemaBuilder sb, ErrorHandler eh ) {
        super(new SchemaBuilderImpl(eh),sb);
    }
    public CheckingSchemaBuilder( SchemaBuilder sb, ErrorHandler eh, DatatypeLibraryFactory dlf ) {
        super(new SchemaBuilderImpl(eh,dlf,new SchemaPatternBuilder()),sb);
    }
    
    public ParsedPattern expandPattern(ParsedPattern p)
        throws BuildException, IllegalSchemaException {
        
        // just return the result from the user-given SchemaBuilder
        ParsedPatternHost r = (ParsedPatternHost)super.expandPattern(p);
        return r.rhs;
    }
}

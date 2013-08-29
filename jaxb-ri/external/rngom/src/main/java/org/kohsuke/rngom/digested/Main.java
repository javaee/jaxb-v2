package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.util.CheckingSchemaBuilder;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Parseable p;

        ErrorHandler eh = new DefaultHandler() {
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }
        };

        // the error handler passed to Parseable will receive parsing errors.
        if(args[0].endsWith(".rng"))
            p = new SAXParseable(new InputSource(args[0]),eh);
        else
            p = new CompactParseable(new InputSource(args[0]),eh);

        // the error handler passed to CheckingSchemaBuilder will receive additional
        // errors found during the RELAX NG restrictions check.
        // typically you'd want to pass in the same error handler,
        // as there's really no distinction between those two kinds of errors.
        SchemaBuilder sb = new CheckingSchemaBuilder(new DSchemaBuilderImpl(),eh);
        try {
            // run the parser
            p.parse(sb);
        } catch( BuildException e ) {
            // I found that Crimson doesn't show the proper stack trace
            // when a RuntimeException happens inside a SchemaBuilder.
            // the following code shows the actual exception that happened.
            if( e.getCause() instanceof SAXException ) {
                SAXException se = (SAXException) e.getCause();
                if(se.getException()!=null)
                    se.getException().printStackTrace();
            }
            throw e;
        }
    }
}

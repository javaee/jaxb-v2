/*
 * @(#)$Id: RelaxNgCompactSyntaxVerifierFactory.java,v 1.1 2005-04-15 20:08:00 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.thaiopensource.relaxng.jarv;

import java.io.IOException;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.impl.SchemaBuilderImpl;
import com.thaiopensource.relaxng.impl.SchemaPatternBuilder;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.xml.sax.DraconianErrorHandler;

/**
 * {@link org.iso_relax.verifier.VerifierFactory} implementation
 * for RELAX NG Compact Syntax.
 * 
 * <p>
 * The reason why this class is in this package is to access
 * some of the package-private classes.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class RelaxNgCompactSyntaxVerifierFactory extends VerifierFactory {
    private final DatatypeLibraryFactory dlf = new DatatypeLibraryLoader();
    private final ErrorHandler eh = new DraconianErrorHandler();

    public Schema compileSchema(InputSource is) throws VerifierConfigurationException, SAXException, IOException {
        SchemaPatternBuilder spb = new SchemaPatternBuilder();
        Parseable parseable = new CompactParseable(is, eh);
        try {
          return new SchemaImpl(SchemaBuilderImpl.parse(parseable, eh, dlf, spb, false), spb);
        }
        catch (IncorrectSchemaException e) {
          throw new SAXException("unreported schema error");
        }
    }

}

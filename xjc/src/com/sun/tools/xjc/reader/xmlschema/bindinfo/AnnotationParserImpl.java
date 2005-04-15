/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.io.IOException;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.grammar.relaxng.datatype.BuiltinDatatypeLibrary;
import com.sun.msv.verifier.jarv.RELAXNGFactoryImpl;
import com.sun.relaxng.javadt.DatatypeLibraryImpl;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.parser.AnnotationState;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;

import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.impl.ForkContentHandler;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * Implementation of {@link AnnotationParser} of XSOM that
 * parses JAXB customization declarations.
 * 
 * <p>
 * This object returns a Hashtable as a parsed result of annotation.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
final class AnnotationParserImpl extends AnnotationParser {
    AnnotationParserImpl( JCodeModel cm, Options opts ) {
        this.codeModel=cm;
        this.options=opts;
    }

    private AnnotationState parser = null;
    private final JCodeModel codeModel;
    private final Options options;
    
    public ContentHandler getContentHandler(
        AnnotationContext context, String parentElementName,
        ErrorHandler errorHandler, EntityResolver entityResolver ) {
        
        // return a ContentHandler that validates the customization and also
        // parses them into the internal structure.
        try {
            if(parser!=null)
                // interface contract violation.
                // this method will be called only once.
                throw new AssertionError();
            
            // set up the actual parser.
            NGCCRuntimeEx runtime = new NGCCRuntimeEx(codeModel,options,errorHandler);
            parser = new AnnotationState(runtime);
            runtime.setRootHandler(parser);
            
            // set up validator
            VerifierFactory factory = new RELAXNGFactoryImpl(); // we need to use a private property exposed.
            factory.setProperty("datatypeLibraryFactory",new DatatypeLibraryFactoryImpl());
            Verifier v = factory.newVerifier(AnnotationParserImpl.class.getResourceAsStream("binding.purified.rng"));
            v.setErrorHandler(errorHandler);

            // the validator will receive events first, then the parser.
            return new ForkContentHandler( v.getVerifierHandler(), runtime );
        } catch( VerifierConfigurationException e ) {
            // there must be something wrong with the deployment.
            e.printStackTrace();
            throw new InternalError();
        } catch( SAXException e ) {
            e.printStackTrace();
            throw new InternalError();
        } catch( IOException e ) {
            e.printStackTrace();
            throw new InternalError();
        }
        
    }

    public Object getResult( Object existing ) {
        if(parser==null)
            // interface contract violation.
            // the getContentHandler method must have been called.
            throw new AssertionError();
        
        if(existing!=null) {
            BindInfo bie = (BindInfo)existing;
            bie.absorb(parser.bi);
            return bie;
        } else
            return parser.bi;
    }
    
    private static class DatatypeLibraryFactoryImpl implements DatatypeLibraryFactory {
        public DatatypeLibrary createDatatypeLibrary(String namespaceURI) {
            if( namespaceURI.equals("http://www.w3.org/2001/XMLSchema-datatypes") )
                return new com.sun.msv.datatype.xsd.ngimpl.DataTypeLibraryImpl();
            if( namespaceURI.equals("") )
                return BuiltinDatatypeLibrary.theInstance;
            if( namespaceURI.equals("http://java.sun.com/xml/ns/relaxng/java-datatypes") )
                return new DatatypeLibraryImpl();
            return null;
        }
    }
}


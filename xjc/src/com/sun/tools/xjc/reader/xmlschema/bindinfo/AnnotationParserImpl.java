/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.validation.ValidatorHandler;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.parser.AnnotationState;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

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
        validator = bindingFileSchema.newValidator();
    }

    private AnnotationState parser = null;
    private final JCodeModel codeModel;
    private final Options options;
    private final ValidatorHandler validator;

    public ContentHandler getContentHandler(
        AnnotationContext context, String parentElementName,
        ErrorHandler errorHandler, EntityResolver entityResolver ) {
        
        // return a ContentHandler that validates the customization and also
        // parses them into the internal structure.
        if(parser!=null)
            // interface contract violation.
            // this method will be called only once.
            throw new AssertionError();

        // set up the actual parser.
        NGCCRuntimeEx runtime = new NGCCRuntimeEx(codeModel,options,errorHandler);
        parser = new AnnotationState(runtime);
        runtime.setRootHandler(parser);

        // set up validator
        validator.setErrorHandler(errorHandler);
        validator.setContentHandler(runtime);

        // the validator will receive events first, then the parser.
        return validator;
    }

    /**
     * Lazily parsed schema for the binding file.
     */
    private static SchemaCache bindingFileSchema = new SchemaCache(AnnotationParserImpl.class.getResource("binding.xsd"));

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
}


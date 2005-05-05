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
import com.sun.tools.xjc.reader.Const;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.AnnotationContext;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Implementation of XSOM {@link AnnotationParserFactory} that
 * parses JAXB customization declarations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AnnotationParserFactoryImpl implements AnnotationParserFactory {
    public AnnotationParserFactoryImpl( JCodeModel cm, Options opts ) {
        this.codeModel=cm;
        this.options=opts;
    }
    
    private final JCodeModel codeModel;
    private final Options options;
    /**
     * Lazily created validator, so that the schema for binding won't be
     * prepared unless absolutely necessary.
     */
    private ValidatorHandler validator;

    /**
     * Lazily parsed schema for the binding file.
     */
    private static SchemaCache bindingFileSchema = new SchemaCache(AnnotationParserFactoryImpl.class.getResource("binding.xsd"));

    public AnnotationParser create() {
        return new AnnotationParser() {
            private AnnotationState parser = null;

            public ContentHandler getContentHandler(
                AnnotationContext context, String parentElementName,
                final ErrorHandler errorHandler, EntityResolver entityResolver ) {

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

                // configure so that the validator will receive events for JAXB islands
                return new ForkingFilter(runtime) {
                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                        super.startElement(uri, localName, qName, atts);
                        if(uri.equals(Const.JAXB_NSURI) || uri.equals(Const.XJC_EXTENSION_URI) && getSideHandler()==null) {
                            // set up validator
                            if(validator==null)
                                validator = bindingFileSchema.newValidator();
                            validator.setErrorHandler(errorHandler);
                            startForking(uri,localName,qName,atts,validator);
                        }
                    }
                };
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
                } else {
                    if(parser.bi.size()>0)
                        return parser.bi;   // just annotation. no meaningful customization
                    else
                        return null;
                }
            }
        };
    }
}

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.ValidatorHandler;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.reader.Const;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Implementation of XSOM {@link AnnotationParserFactory} that
 * parses JAXB customization declarations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AnnotationParserFactoryImpl implements AnnotationParserFactory {
    public AnnotationParserFactoryImpl(Options opts) {
        this.options=opts;
    }

    private final Options options;
    /**
     * Lazily created validator, so that the schema for binding won't be
     * prepared unless absolutely necessary.
     */
    private ValidatorHandler validator;

    /**
     * Lazily parsed schema for the binding file.
     */
    private static final SchemaCache bindingFileSchema = new SchemaCache(AnnotationParserFactoryImpl.class.getResource("binding.xsd"));

    /**
     * Lazily prepared {@link JAXBContext}.
     */
    private static JAXBContextImpl customizationContext;

    private static JAXBContextImpl getJAXBContext() {
        synchronized(AnnotationParserFactoryImpl.class) {
            try {
                if(customizationContext==null)
                    customizationContext = new JAXBContextImpl(
                        new Class[] {
                            BindInfo.class, // for xs:annotation
                            BIClass.class,
                            BIConversion.User.class,
                            BIDom.class,
                            BIEnum.class,
                            BIEnumMember.class,
                            BIGlobalBinding.class,
                            BIProperty.class,
                            BISchemaBinding.class
                        }, Collections.<TypeReference>emptyList(), null
                    );
                return customizationContext;
            } catch (JAXBException e) {
                throw new AssertionError(e);
            }
        }
    }

    public AnnotationParser create() {
        return new AnnotationParser() {
            private Unmarshaller u = getJAXBContext().createUnmarshaller();

            private UnmarshallerHandler handler;

            public ContentHandler getContentHandler(
                AnnotationContext context, String parentElementName,
                final ErrorHandler errorHandler, EntityResolver entityResolver ) {

                // return a ContentHandler that validates the customization and also
                // parses them into the internal structure.
                if(handler!=null)
                    // interface contract violation.
                    // this method will be called only once.
                    throw new AssertionError();

                if(options.debugMode)
                    try {
                        u.setEventHandler(new DefaultValidationEventHandler());
                    } catch (JAXBException e) {
                        throw new AssertionError(e);    // ridiculous!
                    }

                handler = u.getUnmarshallerHandler();

                // configure so that the validator will receive events for JAXB islands
                return new ForkingFilter(handler) {
                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                        super.startElement(uri, localName, qName, atts);
                        if((uri.equals(Const.JAXB_NSURI) || uri.equals(Const.XJC_EXTENSION_URI))
                        && getSideHandler()==null) {
                            // set up validator
                            if(validator==null)
                                validator = bindingFileSchema.newValidator();
                            validator.setErrorHandler(errorHandler);
                            startForking(uri,localName,qName,atts,new ValidatorProtecter(validator));
                        }
                    }
                };
            }

            public Object getResult( Object existing ) {
                if(handler==null)
                    // interface contract violation.
                    // the getContentHandler method must have been called.
                    throw new AssertionError();

                try {
                    BindInfo result = (BindInfo)handler.getResult();

                    if(existing!=null) {
                        BindInfo bie = (BindInfo)existing;
                        bie.absorb(result);
                        return bie;
                    } else {
                        if(result.size()>0)
                            return result;   // just annotation. no meaningful customization
                        else
                            return null;
                    }
                } catch (JAXBException e) {
                    throw new AssertionError(e);
                }
            }
        };
    }

    private static final class ValidatorProtecter extends XMLFilterImpl {
        public ValidatorProtecter(ContentHandler h) {
            setContentHandler(h);
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // work around a bug in the validator implementation in Tiger
            super.startPrefixMapping(prefix.intern(),uri);
        }
    }
}

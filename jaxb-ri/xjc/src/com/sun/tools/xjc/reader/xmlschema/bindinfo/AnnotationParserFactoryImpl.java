/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.validation.ValidatorHandler;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.SchemaCache;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;

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
        validator = bindingFileSchema.newValidator();
    }
    
    private final JCodeModel codeModel;
    private final Options options;
    private final ValidatorHandler validator;

    /**
     * Lazily parsed schema for the binding file.
     */
    private static SchemaCache bindingFileSchema = new SchemaCache(AnnotationParserFactoryImpl.class.getResource("binding.xsd"));

    public AnnotationParser create() {
        return new AnnotationParserImpl(codeModel,options,validator);
    }
}

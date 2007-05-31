/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.ValidatorHandler;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.reader.Const;
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

    public AnnotationParser create() {
        return new AnnotationParser() {
            private Unmarshaller u = BindInfo.getJAXBContext().createUnmarshaller();

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
                                validator = BindInfo.bindingFileSchema.newValidator();
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
                        if(!result.isPointless())
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

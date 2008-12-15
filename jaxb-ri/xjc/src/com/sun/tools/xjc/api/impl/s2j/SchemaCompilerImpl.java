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
package com.sun.tools.xjc.api.impl.s2j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.SchemaFactory;

import com.sun.codemodel.JCodeModel;
import com.sun.istack.NotNull;
import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.ModelLoader;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.api.ClassNameAllocator;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.SpecVersion;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.SCDBasedBindingSet;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.xsom.XSSchemaSet;

import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

/**
 * {@link SchemaCompiler} implementation.
 * 
 * This class builds a {@link DOMForest} until the {@link #bind()} method,
 * then this method does the rest of the hard work.
 * 
 * @see ModelLoader
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class SchemaCompilerImpl extends ErrorReceiver implements SchemaCompiler {

    /**
     * User-specified error receiver.
     * This field can be null, in which case errors need to be discarded.
     */
    private ErrorListener errorListener;

    protected final Options opts = new Options();

    protected @NotNull DOMForest forest;

    /**
     * Set to true once an error is found.
     */
    private boolean hadError;

    public SchemaCompilerImpl() {
        opts.compatibilityMode = Options.EXTENSION;
        resetSchema();

        if(System.getProperty("xjc-api.test")!=null) {
            opts.debugMode = true;
            opts.verbose = true;
        }
    }

    @NotNull
    public Options getOptions() {
        return opts;
    }

    public ContentHandler getParserHandler( String systemId ) {
        return forest.getParserHandler(systemId,true);
    }

    public void parseSchema( String systemId, Element element ) {
        checkAbsoluteness(systemId);
        try {
            DOMScanner scanner = new DOMScanner();

            // use a locator that sets the system ID correctly
            // so that we can resolve relative URLs in most of the case.
            // it still doesn't handle xml:base and XInclude and all those things
            // correctly. There's just no way to make all those things work with DOM!
            LocatorImpl loc = new LocatorImpl();
            loc.setSystemId(systemId);
            scanner.setLocator(loc);

            scanner.setContentHandler(getParserHandler(systemId));
            scanner.scan(element);
        } catch (SAXException e) {
            // since parsing DOM shouldn't cause a SAX exception
            // and our handler will never throw it, it's not clear
            // if this will ever happen.
            fatalError(new SAXParseException2(
                e.getMessage(), null, systemId,-1,-1, e));
        }
    }
    
    public void parseSchema(InputSource source) {
        checkAbsoluteness(source.getSystemId());
        try {
            forest.parse(source,true);
        } catch (SAXException e) {
            // parsers are required to report an error to ErrorHandler,
            // so we should never see this error.
            e.printStackTrace();
        }
    }

    public void setTargetVersion(SpecVersion version) {
        if(version==null)
            version = SpecVersion.LATEST;
        opts.target = version;
    }

    public void parseSchema(String systemId, XMLStreamReader reader) throws XMLStreamException {
        checkAbsoluteness(systemId);
        forest.parse(systemId,reader,true);
    }

    /**
     * Checks if the system ID is absolute.
     */
    private void checkAbsoluteness(String systemId) {
        // we need to be able to handle system IDs like "urn:foo", which java.net.URL can't process,
        // but OTOH we also need to be able to process system IDs like "file://a b c/def.xsd",
        // which java.net.URI can't process. So for now, let's fail only if both of them fail.
        // eventually we need a proper URI class that works for us.
        try {
            new URL(systemId);
        } catch( MalformedURLException _ ) {
            try {
                new URI(systemId);
            } catch (URISyntaxException e ) {
                throw new IllegalArgumentException("system ID '"+systemId+"' isn't absolute",e);
            }
        }
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        forest.setEntityResolver(entityResolver);
        opts.entityResolver = entityResolver; 
    }

    public void setDefaultPackageName(String packageName) {
        opts.defaultPackage2 = packageName;
    }

    public void forcePackageName(String packageName) {
        opts.defaultPackage = packageName;
    }

    public void setClassNameAllocator(ClassNameAllocator allocator) {
        opts.classNameAllocator = allocator;
    }

    public void resetSchema() {
        forest = new DOMForest(new XMLSchemaInternalizationLogic());
        forest.setErrorHandler(this);
        forest.setEntityResolver(opts.entityResolver);
    }


    public JAXBModelImpl bind() {
        // this has been problematic. turn it off.
//        if(!forest.checkSchemaCorrectness(this))
//            return null;

        // parse all the binding files given via XJC -b options.
        // this also takes care of the binding files given in the -episode option.
        for (InputSource is : opts.getBindFiles())
            parseSchema(is);
        
        // internalization
        SCDBasedBindingSet scdBasedBindingSet = forest.transform(opts.isExtensionMode());

        if(!NO_CORRECTNESS_CHECK) {
            // correctness check
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            sf.setErrorHandler(new DowngradingErrorHandler(this));
            forest.weakSchemaCorrectnessCheck(sf);
            if(hadError)
                return null;    // error in the correctness check. abort now
        }

        JCodeModel codeModel = new JCodeModel();

        ModelLoader gl = new ModelLoader(opts,codeModel,this);

        try {
            XSSchemaSet result = gl.createXSOM(forest, scdBasedBindingSet);
            if(result==null)
                return null;


            // we need info about each field, so we go ahead and generate the
            // skeleton at this point.
            // REVISIT: we should separate FieldRenderer and FieldAccessor
            // so that accessors can be used before we build the code.
            Model model = gl.annotateXMLSchema(result);
            if(model==null)   return null;

            if(hadError)        return null;    // if we have any error by now, abort

            Outline context = model.generateCode(opts,this);
            if(context==null)   return null;

            if(hadError)        return null;

            return new JAXBModelImpl(context);
        } catch( SAXException e ) {
            // since XSOM uses our parser that scans DOM,
            // no parser error is possible.
            // all the other errors will be directed to ErrorReceiver
            // before it's thrown, so when the exception is thrown
            // the error should have already been reported.

            // thus ignore.
            return null;
        }
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }


    public void info(SAXParseException exception) {
        if(errorListener!=null)
            errorListener.info(exception);
    }
    public void warning(SAXParseException exception) {
        if(errorListener!=null)
            errorListener.warning(exception);
    }
    public void error(SAXParseException exception) {
        hadError = true;
        if(errorListener!=null)
            errorListener.error(exception);
    }
    public void fatalError(SAXParseException exception) {
        hadError = true;
        if(errorListener!=null)
            errorListener.fatalError(exception);
    }

    /**
     * We use JAXP 1.3 to do a schema correctness check, but we know
     * it doesn't always work. So in case some people hit the problem,
     * this switch is here so that they can turn it off as a workaround.
     */
    private static boolean NO_CORRECTNESS_CHECK = false;

    static {
        try {
            NO_CORRECTNESS_CHECK = Boolean.getBoolean(SchemaCompilerImpl.class.getName()+".noCorrectnessCheck");
        } catch( Throwable t) {
            // ignore
        }
    }
}

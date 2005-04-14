package com.sun.xml.xsom.impl.parser;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * Provides context information to be used by {@link NGCCRuntimeEx}s.
 * 
 * <p>
 * This class does the actual processing for {@link XSOMParser},
 * but to hide the details from the public API, this class in
 * a different package.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ParserContext {

    /** SchemaSet to which a newly parsed schema is put in. */
    public final SchemaSetImpl schemaSet = new SchemaSetImpl();
    
    private final XSOMParser owner;
    
    final XMLParser parser;

    
    private final Vector patchers = new Vector();
    
    /**
     * Documents that are parsed already. Used to avoid cyclic inclusion/double
     * inclusion of schemas. Set of {@link DocumentIdentity}s.
     */
    protected final Set parsedDocuments = new HashSet();
    
    protected static final class DocumentIdentity
    {
        /**
         * The current target namespace URI. The document is parsed
         * to be included into this namespace. Null if the document
         * is being imported.
         */
        private final String targetNamespaceUri;
        /**
         * URI of the schema document to be parsed.
         */
        private final String schemaDocumentURI;
        
        protected DocumentIdentity(String _targetNamespaceUri, String _schemaDocumentURI) {
            this.targetNamespaceUri = _targetNamespaceUri;
            this.schemaDocumentURI = _schemaDocumentURI;
        }
        public boolean equals(Object o) {
            DocumentIdentity rhs = (DocumentIdentity) o;
            if( !schemaDocumentURI.equals(rhs.schemaDocumentURI) )
                return false;
            if( targetNamespaceUri==null && rhs.targetNamespaceUri==null )
                return true;
            if( targetNamespaceUri==null || rhs.targetNamespaceUri==null )
                return false;
            return targetNamespaceUri.equals(rhs.targetNamespaceUri);
        }
        public int hashCode() {
            return schemaDocumentURI.hashCode()^
                (targetNamespaceUri==null?0:targetNamespaceUri.hashCode());
        }
    }
    

    public ParserContext( XSOMParser owner, XMLParser parser ) {
        this.owner = owner;
        this.parser = parser;

        try {
            parse(new InputSource(ParserContext.class.getResource("datatypes.xsd").toExternalForm()));
            
            SchemaImpl xs = (SchemaImpl)
                schemaSet.getSchema("http://www.w3.org/2001/XMLSchema");
            xs.addSimpleType(schemaSet.anySimpleType);
            xs.addComplexType(schemaSet.anyType);
        } catch( SAXException e ) {
            // this must be a bug of XSOM
            if(e.getException()!=null)
                e.getException().printStackTrace();
            else
                e.printStackTrace();
            throw new InternalError();
        }
    }
    
    public EntityResolver getEntityResolver() {
        return owner.getEntityResolver();
    }
    
    public AnnotationParserFactory getAnnotationParserFactory() {
        return owner.getAnnotationParserFactory();
    }
    
    /**
     * Parses a new XML Schema document.
     */
    public void parse( InputSource source ) throws SAXException {
        newNGCCRuntime().parseEntity(source,false,null,null);
    }
    
    
    public XSSchemaSet getResult() throws SAXException {
        // run all the patchers
        Iterator itr = patchers.iterator();
        while(itr.hasNext())
            ((Patch)itr.next()).run();
        patchers.clear();
        
        // build the element substitutability map
        itr = schemaSet.iterateElementDecls();
        while(itr.hasNext())
            ((ElementDecl)itr.next()).updateSubstitutabilityMap();
        
        if(hadError)    return null;
        else            return schemaSet;
    }

    public NGCCRuntimeEx newNGCCRuntime() {
        return new NGCCRuntimeEx(this);
    }
   
    
    
    /** Once an error is detected, this flag is set to true. */
    private boolean hadError = false;
    
    /** Turns on the error flag. */
    void setErrorFlag() { hadError=true; }
    
    /**
     * PatchManager implementation, which is accessible only from
     * NGCCRuntimEx.
     */
    final PatcherManager patcherManager = new PatcherManager() {
        public void addPatcher( Patch patch ) {
            patchers.add(patch);
        }
        public void reportError( String msg, Locator src ) throws SAXException {
            // set a flag to true to avoid returning a corrupted object.
            setErrorFlag();
            
            SAXParseException e = new SAXParseException(msg,src);
            if(errorHandler==null)
                throw e;
            else
                errorHandler.error(e);
        }
    };
    
    /**
     * ErrorHandler proxy to turn on the hadError flag when an error
     * is found.
     */
    final ErrorHandler errorHandler = new ErrorHandler() {
        private ErrorHandler getErrorHandler() {
            if( owner.getErrorHandler()==null )
                return noopHandler;
            else
                return owner.getErrorHandler();
        }
        
        public void warning(SAXParseException e) throws SAXException {
            getErrorHandler().warning(e);
        }

        public void error(SAXParseException e) throws SAXException {
            setErrorFlag();
            getErrorHandler().error(e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            setErrorFlag();
            getErrorHandler().fatalError(e);
        }
    };

    /**
     * {@link ErrorHandler} that does nothing.
     */
    final ErrorHandler noopHandler = new ErrorHandler() {
        public void warning(SAXParseException e) throws SAXException {
        }
        public void error(SAXParseException e) throws SAXException {
        }
        public void fatalError(SAXParseException e) throws SAXException {
            setErrorFlag();
        }
    };
}

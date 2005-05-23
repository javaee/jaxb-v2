/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: UnmarshallerImpl.java,v 1.11 2005-05-23 15:15:31 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.bind.helpers.AbstractUnmarshallerImpl;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.validation.Schema;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.unmarshaller.Messages;
import com.sun.xml.bind.v2.AssociationMap;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.stax.XMLEventReaderToContentHandler;
import com.sun.xml.bind.v2.stax.XMLStreamReaderToContentHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default Unmarshaller implementation.
 * 
 * <p>
 * This class can be extended by the generated code to provide
 * type-safe unmarshall methods.
 *
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class UnmarshallerImpl extends AbstractUnmarshallerImpl
{
    /** Owning {@link JAXBContext} */
    protected final JAXBContextImpl context;

    /**
     * schema which will be used to validate during calls to unmarshal
     */
    private Schema schema;

    /**
     * If non-null, this unmarshaller will unmarshal {@code JAXBElement<EXPECTEDTYPE>}
     * regardless of the tag name, as opposed to deciding the root object by using
     * the tag name.
     *
     * <p>
     * This property can be set by the {@link #setProperty(String, Object)}.
     *
     * The property has a package-level access, because we cannot copy this value
     * to {@link UnmarshallingContext} when it is created. The property
     * on {@link Unmarshaller} could be changed after the handler is created.
     */
    /*package*/ JaxBeanInfo expectedType;

    public final UnmarshallingContext coordinator;

    /**
     * The attachment unmarshaller used to support MTOM and swaRef.
     */
    private AttachmentUnmarshaller attachmentUnmarshaller;

    public UnmarshallerImpl( JAXBContextImpl context, AssociationMap assoc ) {
        this.context = context;
        this.coordinator = new UnmarshallingContext( this, assoc );

        // initialize datatype converter with ours
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);

        try {
            setEventHandler(context);
        } catch (JAXBException e) {
            throw new AssertionError(e);    // impossible
        }
    }
    
    public UnmarshallerHandler getUnmarshallerHandler() {
        return new SAXConnector(new InterningXmlVisitor(
            createUnmarshallerHandler(null,false)),null);
    }
    
    
    
    /**
     * Creates and configures a new unmarshalling pipe line.
     * Depending on the setting, we put a validator as a filter.
     * 
     * @return
     *      A component that implements both {@link UnmarshallerHandler}
     *      and {@link ValidationEventHandler}. All the parsing errors
     *      should be reported to this error handler for the unmarshalling
     *      process to work correctly.
     * 
     *      Also, returned handler expects all the XML names to be interned.
     *
     */
    public final XmlVisitor createUnmarshallerHandler(InfosetScanner scanner, boolean inplace ) {

        coordinator.reset(scanner,inplace);
        XmlVisitor unmarshaller = coordinator;

        // delegate to JAXP 1.3 for validation if the client provided a schema
        if (schema != null)
            unmarshaller = new ValidatingUnmarshaller(schema,unmarshaller);

        if(attachmentUnmarshaller!=null && attachmentUnmarshaller.isXOPPackage())
            unmarshaller = new MTOMDecorator(this,unmarshaller,attachmentUnmarshaller);

        return unmarshaller;
    }

    private static final DefaultHandler dummyHandler = new DefaultHandler();

    public static XmlVisitor adapt( XMLReader reader, XmlVisitor visitor ) {
        // attempt to set it to true, which could fail
        try {
            reader.setFeature("http://xml.org/sax/features/string-interning",true);
        } catch (SAXException e) {
            ;
        }

        try {
            if( reader.getFeature("http://xml.org/sax/features/string-interning") )
                return visitor;  // no need for wrapping
        } catch (SAXException e) {
            ; // unrecognized/unsupported
        }
        // otherwise we have to wrap
        return new InterningXmlVisitor(visitor);
    }

    protected Object unmarshal( XMLReader reader, InputSource source ) throws JAXBException {
        
        XmlVisitor handler = createUnmarshallerHandler(null,false);
        handler = adapt(reader,handler);

        SAXConnector connector = new SAXConnector(handler,null);


        reader.setContentHandler(connector);
        // saxErrorHandler will be set by the createUnmarshallerHandler method.
        // configure XMLReader so that the error will be sent to it.
        // This is essential for the UnmarshallerHandler to be able to abort
        // unmarshalling when an error is found.
        //
        // Note that when this XMLReader is provided by the client code,
        // it might be already configured to call a client error handler.
        // This will clobber such handler, if any.
        //
        // Ryan noted that we might want to report errors to such a client
        // error handler as well.
        reader.setErrorHandler(handler.getContext());

        try {
            reader.parse(source);
        } catch( IOException e ) {
            throw new JAXBException(e);
        } catch( SAXException e ) {
            throw createUnmarshalException(e);
        }
        
        Object result = handler.getContext().getResult();
        
        // avoid keeping unnecessary references too long to let the GC
        // reclaim more memory.
        // setting null upsets some parsers, so use a dummy instance instead.
        reader.setContentHandler(dummyHandler);
        reader.setErrorHandler(dummyHandler);
        
        return result;
    }

    public final ValidationEventHandler getEventHandler() {
        try {
            return super.getEventHandler();
        } catch (JAXBException e) {
            // impossible
            throw new AssertionError();
        }
    }

    public final Object unmarshal( Node node ) throws JAXBException {
        try {
            final DOMScanner scanner = new DOMScanner();

            InterningXmlVisitor handler = new InterningXmlVisitor(createUnmarshallerHandler(null,false));

            scanner.setContentHandler(new SAXConnector(handler,scanner));

            if(node instanceof Element)
                scanner.scan((Element)node);
            else
            if(node instanceof Document)
                scanner.scan((Document)node);
            else
                // no other type of input is supported
                throw new IllegalArgumentException();
            
            return handler.getContext().getResult();
        } catch( SAXException e ) {
            throw createUnmarshalException(e);
        }
    }
    

    /* (non-Javadoc)
     * @see javax.xml.bind.Unmarshaller#unmarshal(javax.xml.stream.XMLStreamReader)
     */
    public Object unmarshal(XMLStreamReader reader) throws JAXBException {
        if (reader == null) {
            throw new IllegalArgumentException(
                Messages.format(Messages.NULL_READER));
        }

        int eventType = reader.getEventType();
        if (eventType != XMLStreamConstants.START_ELEMENT
            && eventType != XMLStreamConstants.START_DOCUMENT) {
            // TODO: convert eventType into event name
            throw new IllegalStateException(
                Messages.format(Messages.ILLEGAL_READER_STATE,eventType));
        }
        
        UnmarshallerHandler h = getUnmarshallerHandler();
        try {
            new XMLStreamReaderToContentHandler(reader,h).bridge();
        } catch (XMLStreamException e) {
            throw handleStreamException(e);
        }
        return h.getResult();
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.Unmarshaller#unmarshal(javax.xml.stream.XMLEventReader)
     */
    public Object unmarshal(XMLEventReader reader) throws JAXBException {
        if (reader == null) {
            throw new IllegalArgumentException(
                    Messages.format(Messages.NULL_READER));
        }

        try {
            XMLEvent event = reader.peek();

            if (!event.isStartElement() && !event.isStartDocument()) {
                // TODO: convert event into event name
                throw new IllegalStateException(
                    Messages.format(
                        Messages.ILLEGAL_READER_STATE,event.getEventType()));
            }

            UnmarshallerHandler h = getUnmarshallerHandler();
            new XMLEventReaderToContentHandler(reader, h).bridge();
            return h.getResult();
        } catch (XMLStreamException e) {
            throw handleStreamException(e);
        }
    }

    private static JAXBException handleStreamException(XMLStreamException e) {
        // XMLStreamReaderToContentHandler wraps SAXException to XMLStreamException.
        // XMLStreamException doesn't print its nested stack trace when it prints
        // its stack trace, so if we wrap XMLStreamException in JAXBException,
        // it becomes harder to find out the real problem.
        // So we unwrap them here. But we don't want to unwrap too eagerly, because
        // that could throw away some meaningful exception information.
        Throwable ne = e.getNestedException();
        if(ne instanceof JAXBException)
            return (JAXBException)ne;
        if(ne instanceof SAXException)
            return new JAXBException(ne);
        return new JAXBException(e);
    }

    public void setExpectedType(JaxBeanInfo bi) {
        expectedType = bi;
    }

    public void setProperty(String name, Object value) throws PropertyException {
        if(name.equals(EXPECTED_TYPE)) {
            if(value==null) {
                expectedType = null;
                return;
            }
            if(!(value instanceof Class))
                throw new IllegalArgumentException();

            try {
                expectedType = context.getBeanInfo((Class)value,true);
            } catch (JAXBException e) {
                throw new PropertyException(e);
            }
            return;
        }
        if(name.equals(FACTORY)) {
            coordinator.setFactories(value);
            return;
        }

        super.setProperty(name, value);
    }

    public Object getProperty(String name) throws PropertyException {
        if(name.equals(EXPECTED_TYPE)) {
            if(expectedType==null)  return null;
            else    return expectedType.jaxbType;
        }
        return super.getProperty(name);
    }

    public static final String EXPECTED_TYPE = "com.sun.xml.bind.expectedType";
    public static final String FACTORY = "com.sun.xml.bind.ObjectFactory";

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public AttachmentUnmarshaller getAttachmentUnmarshaller() {
        return attachmentUnmarshaller;
    }

    @Override
    public void setAttachmentUnmarshaller(AttachmentUnmarshaller au) {
        this.attachmentUnmarshaller = au;
    }

    /**
     * @deprecated since 2.0
     */
    @Override
    public boolean isValidating() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated since 2.0
     */
    @Override
    public void setValidating(boolean validating) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        if(type==null)
            throw new IllegalArgumentException();
        coordinator.putAdapter(type,adapter);
    }

    @Override
    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        if(type==null)
            throw new IllegalArgumentException();
        if(coordinator.containsAdapter(type))
            // so as not to create a new instance when this method is called
            return coordinator.getAdapter(type);
        else
            return null;
    }

    // opening up for public use
    public UnmarshalException createUnmarshalException( SAXException e ) {
        return super.createUnmarshalException(e);
    }

}

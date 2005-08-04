/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: UnmarshallerImpl.java,v 1.18 2005-08-04 21:33:30 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.bind.helpers.AbstractUnmarshallerImpl;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.unmarshaller.Messages;
import com.sun.xml.bind.v2.AssociationMap;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.stax.StAXConnector;
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
public final class UnmarshallerImpl extends AbstractUnmarshallerImpl implements ValidationEventHandler
{
    /** Owning {@link JAXBContext} */
    protected final JAXBContextImpl context;

    /**
     * schema which will be used to validate during calls to unmarshal
     */
    private Schema schema;


    public final UnmarshallingContext coordinator;

    /** Unmarshaller.Listener */
    private Listener externalListener;

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
            setEventHandler(this);
        } catch (JAXBException e) {
            throw new AssertionError(e);    // impossible
        }
    }

    public UnmarshallerHandler getUnmarshallerHandler() {
        return getUnmarshallerHandler(true,null);
    }

    private SAXConnector getUnmarshallerHandler( boolean intern, JaxBeanInfo expectedType ) {
        XmlVisitor h = createUnmarshallerHandler(null,false,expectedType);
        if(intern)
            h = new InterningXmlVisitor(h);
        return new SAXConnector(h,null);
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
    public final XmlVisitor createUnmarshallerHandler(InfosetScanner scanner, boolean inplace, JaxBeanInfo expectedType ) {

        coordinator.reset(scanner,inplace,expectedType);
        XmlVisitor unmarshaller = coordinator;

        // delegate to JAXP 1.3 for validation if the client provided a schema
        if (schema != null)
            unmarshaller = new ValidatingUnmarshaller(schema,unmarshaller);

        if(attachmentUnmarshaller!=null && attachmentUnmarshaller.isXOPPackage())
            unmarshaller = new MTOMDecorator(this,unmarshaller,attachmentUnmarshaller);

        return unmarshaller;
    }

    private static final DefaultHandler dummyHandler = new DefaultHandler();

    public static boolean needsInterning( XMLReader reader ) {
        // attempt to set it to true, which could fail
        try {
            reader.setFeature("http://xml.org/sax/features/string-interning",true);
        } catch (SAXException e) {
            ;
        }

        try {
            if( reader.getFeature("http://xml.org/sax/features/string-interning") )
                return false;   // no need for intern
        } catch (SAXException e) {
            ; // unrecognized/unsupported
        }
        // otherwise we need intern
        return true;
    }

    protected Object unmarshal( XMLReader reader, InputSource source ) throws JAXBException {
        return unmarshal0(reader,source,null);
    }

    protected <T> JAXBElement<T> unmarshal( XMLReader reader, InputSource source, Class<T> expectedType ) throws JAXBException {
        if(expectedType==null)
            throw new IllegalArgumentException();
        return (JAXBElement)unmarshal0(reader,source,getBeanInfo(expectedType));
    }
    private Object unmarshal0( XMLReader reader, InputSource source, JaxBeanInfo expectedType ) throws JAXBException {

        SAXConnector connector = getUnmarshallerHandler(needsInterning(reader),expectedType);

        reader.setContentHandler(connector);
        // saxErrorHandler will be set by the getUnmarshallerHandler method.
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
        reader.setErrorHandler(coordinator);

        try {
            reader.parse(source);
        } catch( IOException e ) {
            throw new JAXBException(e);
        } catch( SAXException e ) {
            throw createUnmarshalException(e);
        }

        Object result = connector.getResult();

        // avoid keeping unnecessary references too long to let the GC
        // reclaim more memory.
        // setting null upsets some parsers, so use a dummy instance instead.
        reader.setContentHandler(dummyHandler);
        reader.setErrorHandler(dummyHandler);

        return result;
    }

    @Override
    public <T> JAXBElement<T> unmarshal( Source source, Class<T> expectedType ) throws JAXBException {
        if(source instanceof SAXSource) {
            SAXSource ss = (SAXSource)source;

            XMLReader reader = ss.getXMLReader();
            if( reader == null )
                reader = getXMLReader();

            return unmarshal( reader, ss.getInputSource(), expectedType );
        }
        if(source instanceof StreamSource) {
            return unmarshal( getXMLReader(), streamSourceToInputSource((StreamSource)source), expectedType );
        }
        if(source instanceof DOMSource)
            return unmarshal( ((DOMSource)source).getNode(), expectedType );

        // we don't handle other types of Source
        throw new IllegalArgumentException();
    }


    public final ValidationEventHandler getEventHandler() {
        try {
            return super.getEventHandler();
        } catch (JAXBException e) {
            // impossible
            throw new AssertionError();
        }
    }

    @Override
    public <T> JAXBElement<T> unmarshal(Node node, Class<T> expectedType) throws JAXBException {
        if(expectedType==null)
            throw new IllegalArgumentException();
        return (JAXBElement)unmarshal0(node,getBeanInfo(expectedType));
    }

    public final Object unmarshal( Node node ) throws JAXBException {
        return unmarshal0(node,null);
    }

    public final Object unmarshal0( Node node, JaxBeanInfo expectedType ) throws JAXBException {
        try {
            final DOMScanner scanner = new DOMScanner();

            InterningXmlVisitor handler = new InterningXmlVisitor(createUnmarshallerHandler(null,false,expectedType));
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

    @Override
    public Object unmarshal(XMLStreamReader reader) throws JAXBException {
        return unmarshal0(reader,null);
    }

    @Override
    public <T> JAXBElement<T> unmarshal(XMLStreamReader reader, Class<T> expectedType) throws JAXBException {
        if(expectedType==null)
            throw new IllegalArgumentException();
        return (JAXBElement)unmarshal0(reader,getBeanInfo(expectedType));
    }

    public Object unmarshal0(XMLStreamReader reader, JaxBeanInfo expectedType) throws JAXBException {
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

        StAXConnector connector=null;
        UnmarshallingContext context=null;

        if (reader.getClass()==FI_STAX_READER_CLASS && FI_CONNECTOR_CTOR!=null) {
            // check if this is FI.
            XmlVisitor h = createUnmarshallerHandler(null,false,expectedType);
            context = h.getContext();

            try {
                connector = FI_CONNECTOR_CTOR.newInstance(reader,h);
            } catch (Exception t) {
                ; // default to the normal codepath
            }
        }

        if(connector==null) {
            // this is the normal StAX codepath

            // Quick hack until SJSXP fixes 6270116
            boolean isZephyr = reader.getClass().getName().equals("com.sun.xml.stream.XMLReaderImpl");
            SAXConnector h = getUnmarshallerHandler(!isZephyr,expectedType);
            connector = new XMLStreamReaderToContentHandler(reader,h);
            context = h.getContext();
        }

        try {
            connector.bridge();
        } catch (XMLStreamException e) {
            throw handleStreamException(e);
        }

        return context.getResult();
    }

    @Override
    public <T> JAXBElement<T> unmarshal(XMLEventReader reader, Class<T> expectedType) throws JAXBException {
        if(expectedType==null)
            throw new IllegalArgumentException();
        return (JAXBElement)unmarshal0(reader,getBeanInfo(expectedType));
    }

    @Override
    public Object unmarshal(XMLEventReader reader) throws JAXBException {
        return unmarshal0(reader,null);
    }

    private Object unmarshal0(XMLEventReader reader,JaxBeanInfo expectedType) throws JAXBException {
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

            UnmarshallerHandler h = getUnmarshallerHandler(true,expectedType);
            new XMLEventReaderToContentHandler(reader, h).bridge();
            return h.getResult();
        } catch (XMLStreamException e) {
            throw handleStreamException(e);
        }
    }

    // remove this method when you remove this use from BridgeImpl
    public Object unmarshal0( URL input, JaxBeanInfo expectedType ) throws JAXBException {
        return unmarshal0(getXMLReader(),new InputSource(input.toExternalForm()),expectedType);
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

    public void setProperty(String name, Object value) throws PropertyException {
        if(name.equals(FACTORY)) {
            coordinator.setFactories(value);
            return;
        }
        super.setProperty(name, value);
    }

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


    /**
     * Default error handling behavior fot {@link Unmarshaller}.
     */
    public boolean handleEvent(ValidationEvent event) {
        return event.getSeverity()!=ValidationEvent.FATAL_ERROR;
    }

    private static InputSource streamSourceToInputSource( StreamSource ss ) {
        InputSource is = new InputSource();
        is.setSystemId( ss.getSystemId() );
        is.setByteStream( ss.getInputStream() );
        is.setCharacterStream( ss.getReader() );

        return is;
    }

    public <T> JaxBeanInfo<T> getBeanInfo(Class<T> clazz) throws JAXBException {
        return context.getBeanInfo(clazz,true);
    }

    @Override
    public Listener getListener() {
        return externalListener;
    }

    @Override
    public void setListener(Listener listener) {
        externalListener = listener;
    }

    /**
     * Reference to FI's StAXReader class, if FI can be loaded.
     */
    private static final Class FI_STAX_READER_CLASS = initFIStAXReaderClass();
    private static final Constructor<? extends StAXConnector> FI_CONNECTOR_CTOR = initFastInfosetConnectorClass();

    private static Class initFIStAXReaderClass() {
        try {
            return UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
        } catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends StAXConnector> initFastInfosetConnectorClass() {
        try {
            Class c = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.bind.v2.runtime.unmarshaller.FastInfosetConnector");
            return c.getConstructor(FI_STAX_READER_CLASS,XmlVisitor.class);
        } catch (Throwable e) {
            return null;
        }
    }
}

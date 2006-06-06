package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import java.lang.reflect.Method;

import org.xml.sax.SAXException;

/**
 * {@link XmlOutput} that writes to StAX {@link XMLStreamWriter}.
 *
 * @author Kohsuke Kawaguchi
 */
public class XMLStreamWriterOutput extends XmlOutputAbstractImpl {

    /**
     * Creates a new {@link XmlOutput} from a {@link XMLStreamWriter}.
     * This method recognizes an FI StAX writer.
     */
    public static XmlOutput create(XMLStreamWriter out) {
        // try optimized path
        if(out.getClass()==FI_STAX_WRITER_CLASS) {
            try {
                return FI_OUTPUT_CTOR.newInstance(out);
            } catch (Exception e) {
            }
        }
        if(STAXEX_WRITER_CLASS!=null && STAXEX_WRITER_CLASS.isAssignableFrom(out.getClass())) {
            try {
                return STAXEX_OUTPUT_CTOR.newInstance(out);
            } catch (Exception e) {
            }
        }

        // otherwise the normal writer.
        return new XMLStreamWriterOutput(out);
    }


    private final XMLStreamWriter out;

    private final char[] buf = new char[256];

    protected XMLStreamWriterOutput(XMLStreamWriter out) {
        this.out = out;
    }

    // not called if we are generating fragments
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment,nsUriIndex2prefixIndex,nsContext);
        if(!fragment)
            out.writeStartDocument();
    }

    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        if(!fragment) {
            out.writeEndDocument();
            out.flush();
        }
        super.endDocument(fragment);
    }

    public void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException {
        out.writeStartElement(
            nsContext.getPrefix(prefix),
            localName,
            nsContext.getNamespaceURI(prefix));

        NamespaceContextImpl.Element nse = nsContext.getCurrent();
        if(nse.count()>0) {
            for( int i=nse.count()-1; i>=0; i-- ) {
                String uri = nse.getNsUri(i);
                if(uri.length()==0 && nse.getBase()==1)
                    continue;   // no point in definint xmlns='' on the root
                out.writeNamespace(nse.getPrefix(i),uri);
            }
        }
    }

    public void attribute(int prefix, String localName, String value) throws IOException, XMLStreamException {
        if(prefix==-1)
            out.writeAttribute(localName,value);
        else
            out.writeAttribute(
                    nsContext.getPrefix(prefix),
                    nsContext.getNamespaceURI(prefix),
                    localName, value);
    }

    public void endStartTag() throws IOException, SAXException {
        // noop
    }

    public void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException {
        out.writeEndElement();
    }

    public void text(String value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        if(needsSeparatingWhitespace)
            out.writeCharacters(" ");
        out.writeCharacters(value);
    }

    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        if(needsSeparatingWhitespace)
            out.writeCharacters(" ");

        int len = value.length();
        if(len <buf.length) {
            value.writeTo(buf,0);
            out.writeCharacters(buf,0,len);
        } else {
            out.writeCharacters(value.toString());
        }
    }

    /**
     * The Fast Infoset class loader.
     */
    private static ClassLoader FI_CLASS_LOADER;
    
    /**
     * Get the Fast Infoset class loader.
     */
    private static ClassLoader getFIClassLoader() {
        if (FI_CLASS_LOADER == null) {
            try {
                // Obtain the class loader to use for loading Fast Infoset classes
                Class clazz = Class.forName("com.sun.fastinfoset.runtime.FastInfosetRuntime");
                Method m = clazz.getDeclaredMethod("getClassLoader", 
                        new Class[] { ClassLoader.class });
                FI_CLASS_LOADER = (ClassLoader)m.invoke(null, 
                        new Object[] { MarshallerImpl.class.getClassLoader() });
            } catch (Throwable e) {
            }
        }
        
        return FI_CLASS_LOADER;
    }

    /**
     * Reference to FI's XMLStreamWriter class, if FI can be loaded.
     */
    private static final Class FI_STAX_WRITER_CLASS = 
            initFIStAXWriterClass(getFIClassLoader());
    private static final Constructor<? extends XmlOutput> FI_OUTPUT_CTOR = 
            initFastInfosetOutputClass(getFIClassLoader());

    private static Class initFIStAXWriterClass(ClassLoader cl) {
        try {
            return Class.forName(
                    "com.sun.xml.fastinfoset.stax.StAXDocumentSerializer",
                    true, cl);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends XmlOutput> initFastInfosetOutputClass(
            ClassLoader cl) {
        try {
            Class c = Class.forName(
                    "com.sun.xml.bind.v2.runtime.output.FastInfosetStreamWriterOutput",
                    true, cl);
            return c.getConstructor(FI_STAX_WRITER_CLASS);
        } catch (Throwable e) {
            return null;
        }
    }

    //
    // StAX-ex
    //
    private static final Class STAXEX_WRITER_CLASS = initStAXExWriterClass();
    private static final Constructor<? extends XmlOutput> STAXEX_OUTPUT_CTOR = initStAXExOutputClass();

    private static Class initStAXExWriterClass() {
        try {
            return MarshallerImpl.class.getClassLoader().loadClass("org.jvnet.staxex.XMLStreamWriterEx");
        } catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends XmlOutput> initStAXExOutputClass() {
        try {
            Class c = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.bind.v2.runtime.output.StAXExStreamWriterOutput");
            return c.getConstructor(STAXEX_WRITER_CLASS);
        } catch (Throwable e) {
            return null;
        }
    }
}

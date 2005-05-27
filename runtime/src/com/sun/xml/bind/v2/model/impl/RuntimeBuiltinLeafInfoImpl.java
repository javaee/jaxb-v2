package com.sun.xml.bind.v2.model.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ByteArrayDataSource;
import com.sun.xml.bind.v2.DataSourceSource;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.ByteArrayOutputStreamEx;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class RuntimeBuiltinLeafInfoImpl<T> extends BuiltinLeafInfoImpl<Type,Class>
    implements RuntimeBuiltinLeafInfo, Transducer<T> {

    private RuntimeBuiltinLeafInfoImpl(Class type, QName... typeNames) {
        super(type, typeNames);
        LEAVES.put(type,this);
    }

    public final Class getClazz() {
        return (Class)getType();
    }


    public final Transducer<T> getTransducer() {
        return this;
    }

    public boolean useNamespace() {
        return false;
    }

    public final boolean isDefault() {
        return true;
    }

    public void declareNamespace(T o, XMLSerializer w) throws AccessorException {
    }


    /**
     * All instances of {@link RuntimeBuiltinLeafInfoImpl}s keyed by their type.
     */
    public static final Map<Type,RuntimeBuiltinLeafInfoImpl<?>> LEAVES = new HashMap<Type, RuntimeBuiltinLeafInfoImpl<?>>();

    public static final RuntimeBuiltinLeafInfoImpl<String> STRING = new RuntimeBuiltinLeafInfoImpl<String>(String.class,
        createXS("string"),
        createXS("normalizedString"),
        createXS("token"),
        createXS("language"),
        createXS("Name"),
        createXS("NCName"),
        createXS("NMTOKEN"),
        createXS("ENTITY")
        ) {
        public String parse(CharSequence text) {
            return text.toString();
        }
        public String print(String s) {
            return s;
        }
    };

    /**
     * List of all {@link RuntimeBuiltinLeafInfoImpl}s.
     *
     * <p>
     * This corresponds to the built-in Java classes that are specified to be
     * handled differently than ordinary classes. See table 8-2 "Mapping of Standard Java classes".
     */
    public static final RuntimeBuiltinLeafInfoImpl<?>[] builtinBeanInfos = new RuntimeBuiltinLeafInfoImpl<?>[] {

        /*
            There are cases where more than one Java classes map to the same XML type.
            But when we see the same XML type in an incoming document, we only pick
            one of those Java classes to unmarshal. This Java class is called 'primary'.
            The rest are called 'secondary'.

            Currently we lack the proper infrastructure to handle those nicely.
            For now, we rely on a hack.

            We define secondary mappings first, then primary ones later. GrammarInfo
            builds a map from type name to BeanInfo. By defining primary ones later,
            those primary bindings will overwrite the secondary ones.
        */

        /*
            secondary bindings
        */
        new RuntimeBuiltinLeafInfoImpl<Character>(Character.class, createXS("unsignedShort")) {
            public Character parse(CharSequence text) {
                // TODO.checkSpec("default mapping for char is not defined yet");
                return (char)DatatypeConverterImpl._parseInt(text);
            }
            public String print(Character v) {
                return Integer.toString(v.charValue());
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Calendar>(Calendar.class, createXS("dateTime")) {
            public Calendar parse(CharSequence text) {
                return DatatypeConverterImpl._parseDateTime(text.toString());
            }
            public String print(Calendar v) {
                return DatatypeConverterImpl._printDateTime(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<GregorianCalendar>(GregorianCalendar.class, createXS("dateTime")) {
            public GregorianCalendar parse(CharSequence text) {
                return DatatypeConverterImpl._parseDateTime(text.toString());
            }
            public String print(GregorianCalendar v) {
                return DatatypeConverterImpl._printDateTime(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Date>(Date.class, createXS("dateTime")) {
            public Date parse(CharSequence text) {
                return DatatypeConverterImpl._parseDateTime(text.toString()).getTime();
            }
            public String print(Date v) {
                GregorianCalendar cal = new GregorianCalendar(0,0,0);
                cal.setTime(v);
                return DatatypeConverterImpl._printDateTime(cal);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<File>(File.class, createXS("string")) {
            public File parse(CharSequence text) {
                return new File(WhiteSpaceProcessor.trim(text).toString());
            }
            public String print(File v) {
                return v.getPath();
            }
        },
        new RuntimeBuiltinLeafInfoImpl<URL>(URL.class, createXS("anyURI")) {
            public URL parse(CharSequence text) throws SAXException {
                TODO.checkSpec("JSR222 Issue #42");
                try {
                    return new URL(WhiteSpaceProcessor.trim(text).toString());
                } catch (MalformedURLException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }
            public String print(URL v) {
                return v.toExternalForm();
            }
        },
        new RuntimeBuiltinLeafInfoImpl<UUID>(UUID.class, createXS("string")) {
            public UUID parse(CharSequence text) throws SAXException {
                TODO.checkSpec("JSR222 Issue #42");
                try {
                    return UUID.fromString(WhiteSpaceProcessor.trim(text).toString());
                } catch (IllegalArgumentException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }
            public String print(UUID v) {
                return v.toString();
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Class>(Class.class, createXS("string")) {
            public Class parse(CharSequence text) throws SAXException {
                TODO.checkSpec("JSR222 Issue #42");
                try {
                    String name = WhiteSpaceProcessor.trim(text).toString();
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    if(cl!=null)
                        return cl.loadClass(name);
                    else
                        return Class.forName(name);
                } catch (ClassNotFoundException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }
            public String print(Class v) {
                return v.getName();
            }
        },

        /*
            classes that map to base64Binary / MTOM related classes.
            a part of the secondary binding.
        */
        new RuntimeBuiltinLeafInfoImpl<Image>(Image.class, createXS("base64Binary")) {
            public Image parse(CharSequence text) throws SAXException  {
                try {
                    InputStream is;
                    if(text instanceof Base64Data)
                        is = ((Base64Data)text).getInputStream();
                    else
                        is = new ByteArrayInputStream(decodeBase64(text)); // TODO: buffering is inefficient

                    // technically we should check the MIME type here, but
                    // normally images can be content-sniffed.
                    // so the MIME type check will only make us slower and draconian, both of which
                    // JAXB 2.0 isn't interested.
                    return ImageIO.read(is);
                } catch (IOException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            private BufferedImage convertToBufferedImage(Image image) throws IOException {
                if (image instanceof BufferedImage) {
                    return (BufferedImage)image;

                } else {
                    MediaTracker tracker = new MediaTracker(null/*not sure how this is used*/);
                    tracker.addImage(image, 0);
                    try {
                        tracker.waitForAll();
                    } catch (InterruptedException e) {
                        throw new IOException(e.getMessage());
                    }
                    BufferedImage bufImage = new BufferedImage(
                            image.getWidth(null),
                            image.getHeight(null),
                            BufferedImage.TYPE_INT_RGB);

                    Graphics g = bufImage.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    return bufImage;
                }
            }

            public CharSequence print(Image v) {
                ByteArrayOutputStreamEx imageData = new ByteArrayOutputStreamEx();
                XMLSerializer xs = XMLSerializer.getInstance();

                try {
                    String mimeType = xs.getXMIMEContentType();
                    if(mimeType==null)
                        // because PNG is lossless, it's a good default
                        mimeType = "image/png";

                    Iterator<ImageWriter> itr = ImageIO.getImageWritersByMIMEType(mimeType);
                    if(itr.hasNext()) {
                        ImageWriter w = itr.next();
                        w.setOutput(ImageIO.createImageOutputStream(imageData));
                        w.write(convertToBufferedImage(v));
                        w.dispose();
                    } else {
                        // no encoder
                        xs.handleEvent(new ValidationEventImpl(
                            ValidationEvent.ERROR,
                            Messages.NO_IMAGE_WRITER.format(mimeType),
                            xs.getCurrentLocation(null) ));
                        // TODO: proper error reporting
                        throw new RuntimeException();
                    }
                } catch (IOException e) {
                    xs.handleError(e);
                    // TODO: proper error reporting
                    throw new RuntimeException(e);
                }
                Base64Data bd = xs.getCachedBase64DataInstance();
                imageData.set(bd);
                return bd;
            }
        },
        new RuntimeBuiltinLeafInfoImpl<DataHandler>(DataHandler.class, createXS("base64Binary")) {
            public DataHandler parse(CharSequence text) {
                if(text instanceof Base64Data)
                    return ((Base64Data)text).getData();
                else
                    return new DataHandler(new ByteArrayDataSource(decodeBase64(text),
                        UnmarshallingContext.getInstance().getXMIMEContentType()));
            }

            public CharSequence print(DataHandler v) {
                Base64Data bd = XMLSerializer.getInstance().getCachedBase64DataInstance();
                bd.set(v);
                return bd;
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Source>(Source.class, createXS("base64Binary")) {
            public Source parse(CharSequence text) throws SAXException  {
                try {
                    if(text instanceof Base64Data)
                        return new DataSourceSource( ((Base64Data)text).getData() );
                    else
                        return new DataSourceSource(new ByteArrayDataSource(decodeBase64(text),
                            UnmarshallingContext.getInstance().getXMIMEContentType()));
                } catch (MimeTypeParseException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            public CharSequence print(Source v) {
                XMLSerializer xs = XMLSerializer.getInstance();
                Base64Data bd = xs.getCachedBase64DataInstance();

                String contentType = xs.getXMIMEContentType();
                MimeType mt = null;
                if(contentType!=null)
                    try {
                        mt = new MimeType(contentType);
                    } catch (MimeTypeParseException e) {
                        xs.handleError(e);
                        // recover by ignoring the content type specification
                    }

                if( v instanceof DataSourceSource ) {
                    // if so, we already have immutable DataSource so
                    // this can be done efficiently
                    DataSource ds = ((DataSourceSource)v).getDataSource();

                    String dsct = ds.getContentType();
                    if(dsct!=null && (contentType==null || contentType.equals(dsct))) {
                        bd.set(new DataHandler(ds));
                        return bd;
                    }
                }

                // general case. slower.

                // find out the encoding
                String charset=null;
                if(mt!=null)
                    charset = mt.getParameter("charset");
                if(charset==null)
                    charset = "UTF-8";

                try {
                    ByteArrayOutputStreamEx baos = new ByteArrayOutputStreamEx();
                    xs.getIdentityTransformer().transform(v,
                        new StreamResult(new OutputStreamWriter(baos,charset)));
                    baos.set(bd);
                    return bd;
                } catch (TransformerException e) {
                    // TODO: marshaller error handling
                    xs.handleError(e);
                } catch (UnsupportedEncodingException e) {
                    xs.handleError(e);
                }
                return "";  // recover
            }
        },

        /*
            primary bindings
        */
        STRING,
        new RuntimeBuiltinLeafInfoImpl<Boolean>(Boolean.class,
            createXS("boolean")
            ) {
            public Boolean parse(CharSequence text) {
                return DatatypeConverterImpl._parseBoolean(text);
            }

            public String print(Boolean v) {
                return v.toString();
            }
        },
        new RuntimeBuiltinLeafInfoImpl<byte[]>(byte[].class,
            createXS("base64Binary"),
            createXS("hexBinary")
            ) {
            public byte[] parse(CharSequence text) {
                return decodeBase64(text);
            }

            public CharSequence print(byte[] v) {
                Base64Data bd = XMLSerializer.getInstance().getCachedBase64DataInstance();
                bd.set(v);
                return bd;
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Byte>(Byte.class,
            createXS("byte"),
            createXS("unsignedShort")
            ) {
            public Byte parse(CharSequence text) {
                return DatatypeConverterImpl._parseByte(text);
            }

            public String print(Byte v) {
                return DatatypeConverterImpl._printByte(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Short>(Short.class,
            createXS("short"),
            createXS("unsignedInt")
            ) {
            public Short parse(CharSequence text) {
                return DatatypeConverterImpl._parseShort(text);
            }

            public String print(Short v) {
                return DatatypeConverterImpl._printShort(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Integer>(Integer.class,
            createXS("int"),
            createXS("unsignedShort")
            ) {
            public Integer parse(CharSequence text) {
                return DatatypeConverterImpl._parseInt(text);
            }

            public String print(Integer v) {
                return DatatypeConverterImpl._printInt(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Long>(Long.class,
            createXS("long"),
            createXS("unsignedInt")
            ) {
            public Long parse(CharSequence text) {
                return DatatypeConverterImpl._parseLong(text);
            }

            public String print(Long v) {
                return DatatypeConverterImpl._printLong(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Float>(Float.class,
            createXS("float")
            ) {
            public Float parse(CharSequence text) {
                return DatatypeConverterImpl._parseFloat(text.toString());
            }

            public String print(Float v) {
                return DatatypeConverterImpl._printFloat(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Double>(Double.class,
            createXS("double")
            ) {
            public Double parse(CharSequence text) {
                return DatatypeConverterImpl._parseDouble(text);
            }

            public String print(Double v) {
                return DatatypeConverterImpl._printDouble(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<BigInteger>(BigInteger.class,
            createXS("integer"),
            createXS("positiveInteger"),
            createXS("negativeInteger"),
            createXS("nonPositiveInteger"),
            createXS("nonNegativeInteger"),
            createXS("unsignedLong")
            ) {
            public BigInteger parse(CharSequence text) {
                return DatatypeConverterImpl._parseInteger(text);
            }

            public String print(BigInteger v) {
                return DatatypeConverterImpl._printInteger(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<BigDecimal>(BigDecimal.class,
            createXS("decimal")
            ) {
            public BigDecimal parse(CharSequence text) {
                return DatatypeConverterImpl._parseDecimal(text.toString());
            }

            public String print(BigDecimal v) {
                return DatatypeConverterImpl._printDecimal(v);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<QName>(QName.class,
            createXS("QName")
            ) {
            public QName parse(CharSequence text) {
                return DatatypeConverterImpl._parseQName(text.toString(),UnmarshallingContext.getInstance());
            }

            public String print(QName v) {
                return DatatypeConverterImpl._printQName(v,XMLSerializer.getInstance().getNamespaceContext());
            }

            public boolean useNamespace() {
                return true;
            }

            public void declareNamespace(QName v, XMLSerializer w) {
                w.getNamespaceContext().declareNamespace(v.getNamespaceURI(),v.getPrefix(),false);
            }
        },
        new RuntimeBuiltinLeafInfoImpl<URI>(URI.class, createXS("anyURI")) {
            public URI parse(CharSequence text) throws SAXException {
                TODO.checkSpec("JSR222 Issue #42");
                try {
                    return new URI(text.toString());
                } catch (URISyntaxException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            public String print(URI v) {
                return v.toString();
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Duration>(Duration.class,  createXS("duration")) {
            public String print(Duration duration) {
                return duration.toString();
            }

            public Duration parse(CharSequence lexical) {
                TODO.checkSpec("JSR222 Issue #42");
                return datatypeFactory.newDuration(lexical.toString());
            }
        },
        new RuntimeBuiltinLeafInfoImpl<XMLGregorianCalendar>(XMLGregorianCalendar.class, createXS("dateTime")) {

            public String print(XMLGregorianCalendar xmlGregorianCalendar) {
                // TODO: this doesn't guarantee the calendar to be printed in the xs:dateTime format.
                TODO.prototype();
                return xmlGregorianCalendar.toXMLFormat(); // ???
            }

            public XMLGregorianCalendar parse(CharSequence lexical) {
                TODO.checkSpec("JSR222 Issue #42");
                return datatypeFactory.newXMLGregorianCalendar(lexical.toString());
            }
        },
        new RuntimeBuiltinLeafInfoImpl<Void>(Void.class) {
            // 'void' binding isn't defined by the spec, but when the JAX-RPC processes user-defined
            // methods like "int actionFoo()", they need this pseudo-void property.

            public String print(Void value) {
                return "";
            }

            public Void parse(CharSequence lexical) {
                return null;
            }
        }
        // TODO: complete this table
    };

    private static byte[] decodeBase64(CharSequence text) {
        if (text instanceof Base64Data) {
            Base64Data base64Data = (Base64Data) text;
            return base64Data.getExact();
        } else {
            return DatatypeConverterImpl._parseBase64Binary(text);
        }
    }


    private static QName createXS(String typeName) {
        return new QName(WellKnownNamespace.XML_SCHEMA,typeName);
    }

    /**
     * Cached instance of {@link DatatypeFactory} to create
     * {@link XMLGregorianCalendar} and {@link Duration}.
     */
    private static final DatatypeFactory datatypeFactory = init();

    private static DatatypeFactory init() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new Error(Messages.FAILED_TO_INITIALE_DATATYPE_FACTORY.format(),e);
        }
    }

    // TODO: think about how to handle those primitive types
    // shall we just create BeanInfo for them and that will be it?

//        // we don't want to have multiple Java classes for the same XML type,
//        // so those "secondary" types are listed here out side of built-in BeanInfo.
//        add( leaves, factory, nav.getPrimitive(Short.TYPE), "short" );
//        add( leaves, factory, nav.getPrimitive(Byte.TYPE), "byte" );
//        add( leaves, factory, nav.getPrimitive(Integer.TYPE), "int" );
//        add( leaves, factory, nav.getPrimitive(Long.TYPE), "long" );
//        add( leaves, factory, nav.getPrimitive(Boolean.TYPE), "boolean" );
//        add( leaves, factory, nav.getPrimitive(Float.TYPE), "float" );
//        add( leaves, factory, nav.getPrimitive(Double.TYPE), "double" );
//        TODO.checkSpec("default mapping for char is not defined yet");
//        add( leaves, factory, nav.getPrimitive(Character.TYPE), "unsignedShort" );
}

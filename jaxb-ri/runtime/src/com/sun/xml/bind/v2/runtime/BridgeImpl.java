package com.sun.xml.bind.v2.runtime;

import java.io.OutputStream;
import java.net.URL;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.marshaller.SAX2DOMEx;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;
import com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;

import org.w3c.dom.Node;

/**
 * {@link Bridge} implementaiton.
 *
 * @author Kohsuke Kawaguchi
 */
final class BridgeImpl<T> extends Bridge<T> {

    /**
     * Tag name associated with this {@link Bridge}.
     * Used for marshalling.
     */
    private final Name tagName;
    private final JaxBeanInfo<T> bi;
    private final TypeReference typeRef;

    public BridgeImpl(Name tagName, JaxBeanInfo<T> bi,TypeReference typeRef) {
        this.tagName = tagName;
        this.bi = bi;
        this.typeRef = typeRef;
    }

    public void marshal(BridgeContext context, T t, XMLStreamWriter output) throws JAXBException {
        MarshallerImpl m = ((BridgeContextImpl)context).marshaller;
        m.write(tagName,bi,t,new XMLStreamWriterOutput(output),new StAXPostInitAction(output,m.serializer));
    }

    public void marshal(BridgeContext context, T t, OutputStream output) throws JAXBException {
        MarshallerImpl m = ((BridgeContextImpl)context).marshaller;
        m.write(tagName,bi,t,m.createWriter(output),null);
    }

    public void marshal(BridgeContext context, T t, Node output) throws JAXBException {
        MarshallerImpl m = ((BridgeContextImpl)context).marshaller;
        m.write(tagName,bi,t,new SAXOutput(new SAX2DOMEx(output)),new DomPostInitAction(output,m.serializer));
    }

    public T unmarshal(BridgeContext context, XMLStreamReader in) throws JAXBException {
        UnmarshallerImpl u = ((BridgeContextImpl)context).unmarshaller;
        return ((JAXBElement<T>)u.unmarshal0(in,bi)).getValue();
    }

    public T unmarshal(BridgeContext context, URL in) throws JAXBException {
        UnmarshallerImpl u = ((BridgeContextImpl)context).unmarshaller;
        // remove this unmarshal0 method when you remove this method
        return ((JAXBElement<T>)u.unmarshal0(in,bi)).getValue();
    }

    public TypeReference getTypeReference() {
        return typeRef;
    }

}

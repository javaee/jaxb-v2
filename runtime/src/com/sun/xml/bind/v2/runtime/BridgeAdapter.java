package com.sun.xml.bind.v2.runtime;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

/**
 * {@link Bridge} decorator for {@link XmlAdapter}.
 *
 * @author Kohsuke Kawaguchi
 */
final class BridgeAdapter<OnWire,InMemory> extends InternalBridge<InMemory> {
    private final InternalBridge<OnWire> core;
    private final Class<? extends XmlAdapter<OnWire,InMemory>> adapter;

    public BridgeAdapter(InternalBridge<OnWire> core, Class<? extends XmlAdapter<OnWire,InMemory>> adapter) {
        this.core = core;
        this.adapter = adapter;
    }

    private BridgeContextImpl getImpl(BridgeContext bc) {
        return (BridgeContextImpl)bc;
    }

    public void marshal(BridgeContext context, InMemory inMemory, XMLStreamWriter output) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),output);
    }

    public void marshal(BridgeContext context, InMemory inMemory, OutputStream output, NamespaceContext nsc) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),output,nsc);
    }

    public void marshal(BridgeContext context, InMemory inMemory, Node output) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),output);
    }

    public void marshal(BridgeContext context, InMemory inMemory, ContentHandler contentHandler) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),contentHandler);
    }

    public void marshal(BridgeContext context, InMemory inMemory, Result result) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),result);
    }

    private OnWire adaptM(BridgeContext context,InMemory v) throws JAXBException {
        MarshallerImpl m = getImpl(context).marshaller;
        XMLSerializer serializer = m.serializer;
        serializer.setThreadAffinity();
        serializer.pushCoordinator();
        try {
            return _adaptM(serializer, v);
        } finally {
            serializer.popCoordinator();
            serializer.resetThreadAffinity();
        }
    }

    private OnWire _adaptM(XMLSerializer serializer, InMemory v) throws MarshalException {
        XmlAdapter<OnWire,InMemory> a = serializer.getAdapter(adapter);
        try {
            return a.marshal(v);
        } catch (Exception e) {
            serializer.handleError(e,v,null);
            throw new MarshalException(e);
        }
    }


    public InMemory unmarshal(BridgeContext context, XMLStreamReader in) throws JAXBException {
        return adaptU(context, core.unmarshal(context,in));
    }

    public InMemory unmarshal(BridgeContext context, Source in) throws JAXBException {
        return adaptU(context, core.unmarshal(context,in));
    }

    public InMemory unmarshal(BridgeContext context, InputStream in) throws JAXBException {
        return adaptU(context, core.unmarshal(context,in));
    }

    public InMemory unmarshal(BridgeContext context, Node n) throws JAXBException {
        return adaptU(context, core.unmarshal(context,n));
    }

    public TypeReference getTypeReference() {
        return core.getTypeReference();
    }

    private InMemory adaptU(BridgeContext context, OnWire v) throws JAXBException {
        UnmarshallerImpl u = getImpl(context).unmarshaller;
        XmlAdapter<OnWire,InMemory> a = u.coordinator.getAdapter(adapter);
        u.coordinator.setThreadAffinity();
        u.coordinator.pushCoordinator();
        try {
            return a.unmarshal(v);
        } catch (Exception e) {
            throw new UnmarshalException(e);
        } finally {
            u.coordinator.popCoordinator();
            u.coordinator.resetThreadAffinity();
        }
    }

    void marshal(InMemory o, XMLSerializer out) throws IOException, SAXException, XMLStreamException {
        try {
            core.marshal(_adaptM( XMLSerializer.getInstance(), o ), out );
        } catch (MarshalException e) {
            ; // recover from error by not marshalling this element.
        }
    }
}

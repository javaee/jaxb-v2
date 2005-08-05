package com.sun.xml.bind.v2.runtime;

import java.io.OutputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * {@link Bridge} decorator for {@link XmlAdapter}.
 *
 * @author Kohsuke Kawaguchi
 */
final class BridgeAdapter<OnWire,InMemory> extends Bridge<InMemory> {
    private final Bridge<OnWire> core;
    private final Class<? extends XmlAdapter<OnWire,InMemory>> adapter;

    public BridgeAdapter(Bridge<OnWire> core, Class<? extends XmlAdapter<OnWire,InMemory>> adapter) {
        this.core = core;
        this.adapter = adapter;
    }

    private BridgeContextImpl getImpl(BridgeContext bc) {
        return (BridgeContextImpl)bc;
    }

    public void marshal(BridgeContext context, InMemory inMemory, XMLStreamWriter output) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),output);
    }

    public void marshal(BridgeContext context, InMemory inMemory, OutputStream output) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),output);
    }

    public void marshal(BridgeContext context, InMemory inMemory, Node output) throws JAXBException {
        core.marshal(context,adaptM(context,inMemory),output);
    }

    private OnWire adaptM(BridgeContext context,InMemory v) throws JAXBException {
        MarshallerImpl m = getImpl(context).marshaller;
        XmlAdapter<OnWire,InMemory> a = m.serializer.getAdapter(adapter);
        m.serializer.setThreadAffinity();
        m.serializer.pushCoordinator();
        try {
            return a.marshal(v);
        } catch (Exception e) {
            m.serializer.handleError(e,v,null);
            throw new MarshalException(e);
        } finally {
            m.serializer.popCoordinator();
            m.serializer.resetThreadAffinity();
        }
    }


    public InMemory unmarshal(BridgeContext context, XMLStreamReader in) throws JAXBException {
        return adaptU(context, core.unmarshal(context,in));
    }

    public InMemory unmarshal(BridgeContext context, URL url) throws JAXBException {
        return adaptU(context, core.unmarshal(context,url));
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
            try {
                u.coordinator.handleError(e);
            } catch (SAXException e1) {
                throw new UnmarshalException(e1);
            }
            throw new UnmarshalException(e);
        } finally {
            u.coordinator.popCoordinator();
            u.coordinator.resetThreadAffinity();
        }
    }
}

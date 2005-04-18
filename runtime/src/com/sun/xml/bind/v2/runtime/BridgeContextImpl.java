package com.sun.xml.bind.v2.runtime;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;

/**
 * {@link BridgeContext} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
final class BridgeContextImpl extends BridgeContext {

    protected final UnmarshallerImpl unmarshaller;
    protected final MarshallerImpl marshaller;

    BridgeContextImpl(JAXBContextImpl context) {
        unmarshaller = context.createUnmarshaller();
        marshaller = context.createMarshaller();
    }

    public void setErrorHandler(ValidationEventHandler handler) {
        try {
            unmarshaller.setEventHandler(handler);
            marshaller.setEventHandler(handler);
        } catch (JAXBException e) {
            // impossible
            throw new Error(e);
        }
    }

    public void setAttachmentMarshaller(AttachmentMarshaller m) {
        marshaller.setAttachmentMarshaller(m);
    }

    public void setAttachmentUnmarshaller(AttachmentUnmarshaller u) {
        unmarshaller.setAttachmentUnmarshaller(u);
    }
}

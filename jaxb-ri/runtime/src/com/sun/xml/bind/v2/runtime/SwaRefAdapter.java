package com.sun.xml.bind.v2.runtime;


import javax.activation.DataHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;


/**
 * {@link XmlAdapter} that binds the value as a SOAP attachment.
 *
 * This adapter needs to be instanciated by the implementation.
 *
 * @see http://webservices.xml.com/pub/a/ws/2003/09/16/wsbp.html?page=2
 *
 * @author Kohsuke Kawaguchi
 */
public final class SwaRefAdapter extends XmlAdapter<String,DataHandler> {

    private final AttachmentUnmarshaller au;
    private final AttachmentMarshaller am;

    public SwaRefAdapter(AttachmentUnmarshaller au) {
        this.au = au;
        this.am = null;
    }

    public SwaRefAdapter(AttachmentMarshaller am) {
        this.au = null;
        this.am = am;
    }

    public DataHandler unmarshal(String cid) {
        return au.getAttachmentAsDataHandler(cid);
    }

    public String marshal(DataHandler data) {
        return am.addSwaRefAttachment(data);
    }
}

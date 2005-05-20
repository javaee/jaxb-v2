/*
 * @(#)$Id: BinderImpl.java,v 1.6 2005-05-20 20:57:19 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.bind.v2.runtime;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.Binder;
import javax.xml.validation.Schema;

import javax.xml.bind.Binder;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.v2.AssociationMap;
import com.sun.xml.bind.v2.runtime.unmarshaller.InterningXmlVisitor;
import com.sun.xml.bind.v2.runtime.unmarshaller.SAXConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.output.DOMOutput;

import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Implementation of {@link Binder}.
 * 
 * TODO: investigate how much in-place unmarshalling is implemented
 *      - some preliminary work is there. Probably buggy.
 * TODO: work on the marshaller side.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BinderImpl<XmlNode> extends Binder<XmlNode> {

    /**
     * The parent context object.
     */
    private final JAXBContextImpl context;
    
    /**
     * Lazily created unmarshaller to do XML->Java binding.
     * @see #getUnmarshaller()
     */
    private UnmarshallerImpl unmarshaller;

    /**
     * Lazily create marshaller to do Java->XML binding.
     * @see #getMarshaller()
     */
    private MarshallerImpl marshaller;

    private final InfosetScanner<XmlNode> scanner;
    
    /**
     * A {@link Binder} always works with the same
     * association map.
     */
    private final AssociationMap<XmlNode> assoc = new AssociationMap<XmlNode>();
    
    BinderImpl(JAXBContextImpl _context,InfosetScanner<XmlNode> scanner) {
        this.context = _context;
        this.scanner = scanner;
    }
    
    private UnmarshallerImpl getUnmarshaller() {
        if(unmarshaller==null)
            unmarshaller = new UnmarshallerImpl(context,assoc);
        return unmarshaller;
    }

    private MarshallerImpl getMarshaller() {
        if(marshaller==null)
            marshaller = new MarshallerImpl(context,assoc);
        return marshaller;
    }

    public void bindFromJava(Object jaxbObject, XmlNode xmlNode) throws JAXBException {
        getMarshaller().marshal(jaxbObject,createOutput(xmlNode));
    }

    // TODO move this to a sub class once we support something other than W3C DOM
    private DOMOutput createOutput(XmlNode xmlNode) {
        return new DOMOutput((Node)xmlNode,assoc);
    }


    public Object bindFromXml( XmlNode xmlNode ) throws JAXBException {
        return associativeUnmarshal(xmlNode,false);
    }

    public Object updateJava(XmlNode xmlNode) throws JAXBException {
        return associativeUnmarshal(xmlNode,true);
    }

    public void setSchema(Schema schema) throws JAXBException {
        getUnmarshaller().setSchema(schema);
    }

    public Schema getSchema() throws JAXBException {
        return getUnmarshaller().getSchema();
    }

    private Object associativeUnmarshal(XmlNode xmlNode, boolean inplace) throws JAXBException {
        InterningXmlVisitor handler = new InterningXmlVisitor(getUnmarshaller().createUnmarshallerHandler(scanner, inplace ));
        scanner.setContentHandler(new SAXConnector(handler,scanner.getLocator()));
        try {
            scanner.scan(xmlNode);
        } catch( SAXException e ) {
            throw unmarshaller.createUnmarshalException(e);
        }
        
        return handler.getContext().getResult();
    }

    public XmlNode getXmlNode(Object jaxbObject) {
        AssociationMap.Entry<XmlNode> e = assoc.byPeer(jaxbObject);
        if(e==null)     return null;
        return e.element();
    }

    public Object getJavaNode(XmlNode xmlNode) {
        AssociationMap.Entry e = assoc.byElement(xmlNode);
        if(e==null)     return null;
        if(e.outer()!=null)     return e.outer();
        return e.inner();
    }

    public XmlNode updateXml(Object jaxbObject) throws JAXBException {
        return updateXml(jaxbObject,getXmlNode(jaxbObject));
    }

    public XmlNode updateXml(Object jaxbObject, XmlNode xmlNode) throws JAXBException {
        if(jaxbObject==null || xmlNode==null)   throw new IllegalArgumentException();

        // TODO
        // for now just marshal
        // TODO: object model independenc
        Element e = (Element)xmlNode;
        Node ns = e.getNextSibling();
        Node p = e.getParentNode();
        p.removeChild(e);
        getMarshaller().marshal(jaxbObject,p);
        Node newNode = p.getLastChild();
        p.removeChild(newNode);
        p.insertBefore(newNode,ns);

        return (XmlNode)newNode;
    }

    public void setEventHandler(ValidationEventHandler handler) throws JAXBException {
        getUnmarshaller().setEventHandler(handler);
        getMarshaller().setEventHandler(handler);
    }

    public ValidationEventHandler getEventHandler() {
        return getUnmarshaller().getEventHandler();
    }
}

/*
 * @(#)$Id: BinderImpl.java,v 1.1 2005-04-15 20:04:21 kohsuke Exp $
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

import com.sun.xml.bind.annotation.Binder;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.v2.AssociationMap;
import com.sun.xml.bind.v2.runtime.unmarshaller.InterningXmlVisitor;
import com.sun.xml.bind.v2.runtime.unmarshaller.SAXConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;

import org.xml.sax.SAXException;

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
     * Lazily created unmarshaller to
     * do XML->Java binding.
     */
    private UnmarshallerImpl unmarshaller;
    
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
            unmarshaller = new UnmarshallerImpl( context, assoc );
        return unmarshaller;
    }

    public void bindFromJava(Object jaxbObject, XmlNode xmlNode) throws JAXBException {
        throw new UnsupportedOperationException();
    }

    public void bindFromJava(Object jaxbObject, XmlNode xmlNode, int n) throws JAXBException {
        throw new UnsupportedOperationException();
    }
    
    public Object bindFromXml( XmlNode xmlNode ) throws JAXBException {
        return associativeUnmarshal(xmlNode,false);
    }

    public Object updateJava(XmlNode xmlNode) throws JAXBException {
        return associativeUnmarshal(xmlNode,true);
    }

    public Object updateJava(XmlNode xmlNode, Object jaxbObject) throws JAXBException {
        assoc.addOuter(xmlNode,jaxbObject); // ... so that this bean will be used to unmarshal this element.
        return associativeUnmarshal(xmlNode,true);
    }

    private Object associativeUnmarshal(XmlNode xmlNode, boolean inplace) throws JAXBException {
        InterningXmlVisitor handler = new InterningXmlVisitor(getUnmarshaller().createUnmarshallerHandler(scanner, inplace ));
        scanner.setContentHandler(new SAXConnector(handler,null));
        try {
            scanner.scan(xmlNode);
        } catch( SAXException e ) {
            throw new JAXBException(e);
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
        // TODO
        throw new UnsupportedOperationException();
    }

    public XmlNode updateXml(Object jaxbObject, XmlNode xmlNode) throws JAXBException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void setValidating(boolean validating) throws JAXBException {
        getUnmarshaller().setValidating(true);
    }

    public boolean isValidating() throws JAXBException {
        return getUnmarshaller().isValidating();
    }

    public void setEventHandler(ValidationEventHandler handler) throws JAXBException {
        getUnmarshaller().setEventHandler(handler);
    }

    public ValidationEventHandler getEventHandler() {
        return getUnmarshaller().getEventHandler();
    }
}

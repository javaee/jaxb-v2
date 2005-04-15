/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.bind.serializer;

import javax.xml.bind.ValidationEvent;

import org.xml.sax.SAXException;

/**
 * Receives XML serialization event
 * 
 * <p>
 * This object coordinates the overall marshalling efforts across different
 * content-tree objects and different target formats.
 * 
 * <p>
 * The following CFG gives the proper sequence of method invocation.
 * 
 * <pre>
 * MARSHALLING  :=  ELEMENT
 * ELEMENT      :=  "startElement" ATTRIBUTES "endAttributes" BODY "endElement"
 * 
 * ATTRIBUTES   :=  ( ATTRIBUTE | NSDECL )*
 * ATTRIBUTE    :=  "startAttribute" ATTVALUES "endAttribute"
 * ATTVALUES    :=  ( "text" | NSDECL )*
 * 
 * NSDECL       :=  "declareNamespace"
 * 
 * BODY         :=  ( "text" | ELEMENT )*
 * </pre>
 * 
 * <p>
 * A marshalling of one element consists of two stages. The first stage is
 * for marshalling attributes and collecting namespace declarations.
 * The second stage is for marshalling characters/child elements of that element.
 * 
 * <p>
 * Observe that multiple invocation of "text" is allowed.
 * 
 * <p>
 * Also observe that the namespace declarations are allowed only between
 * "startElement" and "endAttributes".
 * 
 * 
 * @author  Kohsuke Kawaguchi
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface XMLSerializer
{
    /**
     * Errors detected by the XMLSerializable should be either thrown
     * as {@link SAXException} or reported through this method.
     * 
     * The callee should report an error to the client application
     * and 
     */
    void reportError( ValidationEvent e ) throws AbortSerializationException;
    
    /**
     * Starts marshalling of an element.
     * Calling this method will push the internal state into the
     * internal stack.
     */
    void startElement( String uri, String local ) throws SAXException;
    
    /**
     * Switches to the mode to marshal child texts/elements.
     * This method has to be called after the 1st pass is completed.
     */
    void endAttributes() throws SAXException;
    
    /**
     * Ends marshalling of an element.
     * Pops the internal stack.
     */
    void endElement() throws SAXException;
    
    
    /**
     * Marshalls text.
     * 
     * <p>
     * This method can be called (i) after the startAttribute method
     * and (ii) before the endAttribute method, to marshal attribute values.
     * If the method is called more than once, those texts are considered
     * as separated by whitespaces. For example,
     * 
     * <pre>
     * c.startAttribute();
     * c.text("abc");
     * c.text("def");
     * c.endAttribute("","foo");
     * </pre>
     * 
     * will generate foo="abc def".
     * 
     * <p>
     * Similarly, this method can be called after the endAttributes
     * method to marshal texts inside elements. The same rule about
     * multiple invokations apply to this case, too. For example,
     * 
     * <pre>
     * c.startElement("","foo");
     * c.endAttributes();
     * c.text("abc");
     * c.text("def");
     *   c.startElement("","bar");
     *   c.endAttributes();
     *   c.endElement();
     * c.text("ghi");
     * c.endElement();
     * </pre>
     * 
     * will generate <code>&lt;foo>abc def&lt;bar/>ghi&lt;/foo></code>.
     */
    void text( String text ) throws SAXException;
    
    
    /**
     * Starts marshalling of an attribute.
     * 
     * The marshalling of an attribute will be done by
     * <ol>
     *  <li>call the startAttribute method
     *  <li>call the text method (several times if necessary)
     *  <li>call the endAttribute method
     * </ol>
     * 
     * No two attributes can be marshalled at the same time.
     * Note that the whole attribute marshalling must be happened
     * after the startElement method and before the endAttributes method.
     */
    void startAttribute( String uri, String local ) throws SAXException;

    void endAttribute() throws SAXException;
    
    /**
     * Obtains a namespace context object, which is used to
     * declare/obtain namespace bindings.
     */
    NamespaceContext2 getNamespaceContext();
    
    
    /**
     * Notifies the serializer that an ID value has just marshalled.
     * 
     * The serializer may or may not check the consistency of ID/IDREFs
     * and may throw a SAXException.
     * 
     * @return
     *      Return the value parameter without any modification,
     *      so that the invocation of this method can be done transparently
     *      by a transducer.
     */
    String onID( String value ) throws SAXException;
    
    /**
     * Notifies the serializer that an IDREF value has just marshalled.
     * 
     * The serializer may or may not check the consistency of ID/IDREFs
     * and may throw a SAXException.
     * 
     * @return
     *      Return the value parameter without any modification.
     *      so that the invocation of this method can be done transparently
     *      by a transducer.
     */
    String onIDREF( String value ) throws SAXException;
    
    
    // TODO: think about the exception handling.
    // I suppose we don't want to use SAXException. -kk
    
    /**
     * This method is called when an XMLSerializable object is found
     * while the marshaller is in the "element" mode (i.e. marshalling
     * a content model of an element)
     */
    void childAsElements( XMLSerializable o ) throws SAXException;
    
    /**
     * This method is called when an XMLSerializable object is found
     * while the marshaller is in the "attribute" mode (i.e. marshalling
     * attributes of an element)
     */
    void childAsAttributes( XMLSerializable o ) throws SAXException;
    
    /**
     * This method is called when an XMLSerializable object is found
     * while the marshaller is in the "attribute body" mode (i.e. marshalling
     * a body of an attribute.)
     */
    void childAsAttributeBodies( XMLSerializable o ) throws SAXException;
}

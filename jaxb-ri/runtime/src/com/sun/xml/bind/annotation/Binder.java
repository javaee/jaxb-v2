package com.sun.xml.bind.annotation;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;

import org.w3c.dom.Node;

/**
 * Manages in-memory association between XML nodes and JAXB objects.
 *
 * <p>
 * This interface provides functionalities necessary to support
 * updatable partial binding,
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class Binder<XmlNode> {
    /**
     * Takes an XML node (such as DOM element) and binds it
     * (and its descendants) to a JAXB object tree.
     *
     * <p>
     * This is very similar to {@link Unmarshaller#unmarshal(Node)}
     * but in addition to unmarshalling, this operation remembers
     * the association between XML nodes and the produced JAXB objects,
     * and enables laterupdate operations.
     *
     * @param xmlNode
     *      The XML node to be bound to JAXB.
     *      Although the signature is {@link Object}, this method only
     *      accepts an XML element or an XML document of the object model
     *      specified when creating a {@link Binder} instance.
     *
     * @return
     *      Always return a non-null valid JAXB object.
     */
    public abstract Object bindFromXml( XmlNode xmlNode ) throws JAXBException;

    /**
     * Takes an JAXB object and binds it
     * (and its descendants) to a new XML document.
     *
     * <p>
     * This is very similar to {@link Marshaller#marshal(Object, Node)}
     * but in addition to marshalling, this operation remembers
     * the association between JAXB objects and the produced XML nodes,
     * and enables later update operations.
     *
     * @param xmlNode
     *      The XML node that receives the produced XML.
     *      Although the signature is {@link Object}, this method only
     *      accepts an XML element or an XML document of the object model
     *      specified when creating a {@link Binder} instance.
     */
    public abstract void bindFromJava( Object jaxbObject, XmlNode xmlNode ) throws JAXBException;

    /**
     * Takes an JAXB object and binds it
     * (and its descendants) to a new XML document.
     *
     * <p>
     * Instead of adding the new child at the end of the parent node
     * (as in {@link #bindFromJava(Object, Object)}, this operation
     * adds the new child so that it will become the <i>n</i>-th child
     * among the children of <i>xmlNode</i>.
     */
    public abstract void bindFromJava( Object jaxbObject, XmlNode xmlNode, int n ) throws JAXBException;

    /**
     * Gets the XML element associated with the given JAXB object.
     *
     * <p>
     * Once a JAXB object tree is associated with an XML fragment,
     * you can use this method to navigate between the two trees.
     *
     * <p>
     * An association between an XML element and a JAXB object can be
     * established by the bind methods and the update methods.
     *
     * <p>
     * But note that this association is partial; not all XML elements
     * have associated JAXB objects, and not all JAXB objects have
     * associated XML elements.
     *
     * @return
     *      null if the specified JAXB object is not known to this
     *      {@link Binder}, or if it is not associated with an
     *      XML element.
     */
    public abstract XmlNode getXmlNode( Object jaxbObject );

    /**
     * Gets the JAXB object associated with the given XML element.
     *
     * <p>
     * Once a JAXB object tree is associated with an XML fragment,
     * you can use this method to navigate between the two trees.
     *
     * <p>
     * An association between an XML element and a JAXB object can be
     * established by the bind methods and the update methods.
     *
     * <p>
     * But note that this association is partial; not all XML elements
     * have associated JAXB objects, and not all JAXB objects have
     * associated XML elements.
     *
     * @return
     *      null if the specified XML node is not known to this
     *      {@link Binder}, or if it is not associated with a
     *      JAXB object.
     */
    public abstract Object getJavaNode( XmlNode xmlNode );

    /**
     * Takes an JAXB object and updates
     * its associated XML node and its descendants.
     *
     * <p>
     * This is a convenience method of:
     * <pre>
     * updateXml( jaxbObject, getXmlNode(jaxbObject) );
     * </pre>
     */
    public abstract XmlNode updateXml( Object jaxbObject ) throws JAXBException;

    /**
     * Takes an JAXB object tree and updates an XML tree.
     *
     * <p>
     * This operation can be thought of as an "in-place" marshalling.
     * The difference is that instead of creating a whole new XML tree,
     * this operation updates an existing tree while trying to preserve
     * the XML as much as possible.
     *
     * <p>
     * For example, unknown elements/attributes in XML that were not bound
     * to JAXB will be left untouched (whereas a marshalling operation
     * would create a new tree that doesn't contain any of those.)s.
     *
     * <p>
     * As a side-effect, this operation updates the association between
     * XML nodes and JAXB objects.
     *
     * @return
     *      Returns the updated XML node. Typically, this is the same
     *      node you passed in as <i>xmlNode</i>, but it maybe
     *      a different object, for example when the tag name of the object
     *      has changed.
     */
    public abstract XmlNode updateXml( Object jaxbObject, XmlNode xmlNode ) throws JAXBException;

    /**
     * Takes an XML node and updates
     * its associated JAXB object and its descendants.
     *
     * <p>
     * This operation can be thought of as an "in-place" unmarshalling.
     * The difference is that instead of creating a whole new JAXB tree,
     * this operation updates an existing tree, reusing as much JAXB objects
     * as possible.
     *
     * <p>
     * As a side-effect, this operation updates the association between
     * XML nodes and JAXB objects.
     *
     * @return
     *      Returns the updated JAXB object. Typically, this is the same
     *      object that was returned from earlier
     *      {@link #bindFromXml(Object)} or
     *      {@link #updateJava(Object)} method invocation,
     *      but it maybe
     *      a different object, for example when the name of the XML
     *      element has changed.
     */
    public abstract Object updateJava( XmlNode xmlNode ) throws JAXBException;


    /**
     * Specifies whether or not the default validation mechanism of the
     * <tt>Unmarshaller</tt> should validate during unmarshal operations.
     * By default, the <tt>Unmarshaller</tt> does not validate.
     * <p>
     * This method may only be invoked before or after calling one of the
     * unmarshal methods.
     * <p>
     * This method only controls the JAXB Provider's default unmarshal-time
     * validation mechanism - it has no impact on clients that specify their
     * own validating SAX 2.0 compliant parser.  Clients that specify their
     * own unmarshal-time validation mechanism may wish to turn off the JAXB
     * Provider's default validation mechanism via this API to avoid "double
     * validation".
     *
     * @param validating true if the Unmarshaller should validate during
     *        unmarshal, false otherwise
     * @throws JAXBException if an error occurred while enabling or disabling
               validation at unmarshal time
     */
    public abstract void setValidating( boolean validating ) throws JAXBException;

    /**
     * Indicates whether or not the <tt>Unmarshaller</tt> is configured to
     * validate during unmarshal operations.
     *
     * <p>
     * This API returns the state of the JAXB Provider's default unmarshal-time
     * validation mechanism.
     *
     * @return true if the Unmarshaller is configured to validate during
     *         unmarshal operations, false otherwise
     * @throws JAXBException if an error occurs while retrieving the validating
     *         flag
     */
    public abstract boolean isValidating() throws JAXBException;

    /**
     * Allow an application to register a <tt>ValidationEventHandler</tt>.
     * <p>
     * The <tt>ValidationEventHandler</tt> will be called by the JAXB Provider
     * if any validation errors are encountered during calls to any of the
     * unmarshal methods.  If the client application does not register a
     * <tt>ValidationEventHandler</tt> before invoking the unmarshal methods,
     * then <tt>ValidationEvents</tt> will be handled by the default event
     * handler which will terminate the unmarshal operation after the first
     * error or fatal error is encountered.
     * <p>
     * Calling this method with a null parameter will cause the Unmarshaller
     * to revert back to the default default event handler.
     *
     * @param handler the validation event handler
     * @throws JAXBException if an error was encountered while setting the
     *         event handler
     */
    public abstract void setEventHandler( ValidationEventHandler handler ) throws JAXBException;

    /**
     * Return the current event handler or the default event handler if one
     * hasn't been set.
     *
     * @return the current ValidationEventHandler or the default event handler
     *         if it hasn't been set
     * @throws JAXBException if an error was encountered while getting the
     *         current event handler
     */
    public abstract ValidationEventHandler getEventHandler() throws JAXBException;

    /**
     * Set the particular property in the underlying implementation of
     * <tt>Unmarshaller</tt>.  This method can only be used to set one of
     * the standard JAXB defined properties above or a provider specific
     * property.  Attempting to set an undefined property will result in
     * a PropertyException being thrown.  See <a href="#supportedProps">
     * Supported Properties</a>.
     *
     * @param name the name of the property to be set. This value can either
     *              be specified using one of the constant fields or a user
     *              supplied string.
     * @param value the value of the property to be set
     *
     * @throws PropertyException when there is an error processing the given
     *                            property or value
     * @throws IllegalArgumentException
     *      If the name parameter is null
     */
    public void setProperty( String name, Object value ) throws PropertyException {
        throw new PropertyException(name);
    }

    /**
     * Get the particular property in the underlying implementation of
     * <tt>Unmarshaller</tt>.  This method can only be used to get one of
     * the standard JAXB defined properties above or a provider specific
     * property.  Attempting to get an undefined property will result in
     * a PropertyException being thrown.  See <a href="#supportedProps">
     * Supported Properties</a>.
     *
     * @param name the name of the property to retrieve
     * @return the value of the requested property
     *
     * @throws PropertyException
     *      when there is an error retrieving the given property or value
     *      property name
     * @throws IllegalArgumentException
     *      If the name parameter is null
     */
    public Object getProperty( String name ) throws PropertyException {
        if(name==null)  throw new IllegalArgumentException();
        throw new PropertyException(name);
    }

}
package com.sun.xml.bind.api;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Node;

/**
 * Mini-marshaller/unmarshaller that is specialized for a particular
 * element name and a type.
 *
 * <p>
 * Instances of this class is stateless and multi-thread safe.
 * They are reentrant.
 *
 * <p>
 * Every marshalling/unmarshalling operation requires a {@link BridgeContext},
 * and a
 *
 * <p>
 * <b>Subject to change without notice</b>.
 *
 * @since JAXB 2.0 EA1
 * @author Kohsuke Kawaguchi
 */
public abstract class Bridge<T> {
    protected Bridge() {}

    /**
     *
     * @throws JAXBException
     *      if there was an error while marshalling.
     *
     * @since 2.0 EA1
     */
    public abstract void marshal(BridgeContext context,T object,XMLStreamWriter output) throws JAXBException;

    /**
     * Marshals the specified type object with the implicit element name
     * associated with this instance of {@link Bridge}.
     *
     * @param nsContext
     *      if this marshalling is done to marshal a subelement, this {@link NamespaceContext}
     *      represents in-scope namespace bindings available for that element. Can be null,
     *      in which case JAXB assumes no in-scope namespaces.
     * @throws JAXBException
     *      if there was an error while marshalling.
     *
     * @since 2.0 EA1
     */
    public abstract void marshal(BridgeContext context,T object,OutputStream output, NamespaceContext nsContext) throws JAXBException;

    public abstract void marshal(BridgeContext context,T object,Node output) throws JAXBException;

    /**
     * Unmarshals the specified type object.
     *
     * @param in
     *      the parser must be pointing at a start tag
     *      that encloses the XML type that this {@link Bridge} is
     *      instanciated for.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 EA1
     */
    public abstract T unmarshal(BridgeContext context,XMLStreamReader in) throws JAXBException;

    /**
     * Unmarshals the specified type object.
     *
     * @param in
     *      the parser must be pointing at a start tag
     *      that encloses the XML type that this {@link Bridge} is
     *      instanciated for.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 EA1
     */
    public abstract T unmarshal(BridgeContext context, Source in) throws JAXBException;

    /**
     * Unmarshals the specified type object.
     *
     * @param in
     *      the parser must be pointing at a start tag
     *      that encloses the XML type that this {@link Bridge} is
     *      instanciated for.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 EA1
     */
    public abstract T unmarshal(BridgeContext context, InputStream in) throws JAXBException;

    /**
     * Gets the {@link TypeReference} from which this bridge was created.
     */
    public abstract TypeReference getTypeReference();
}

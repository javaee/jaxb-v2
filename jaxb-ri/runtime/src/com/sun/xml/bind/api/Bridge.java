package com.sun.xml.bind.api;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import com.sun.istack.NotNull;
import com.sun.xml.bind.v2.runtime.BridgeContextImpl;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Mini-marshaller/unmarshaller that is specialized for a particular
 * element name and a type.
 *
 * <p>
 * Instances of this class is stateless and multi-thread safe.
 * They are reentrant.
 *
 * <p>
 * All the marshal operation generates fragments.
 *
 * <p>
 * <b>Subject to change without notice</b>.
 *
 * @since JAXB 2.0 EA1
 * @author Kohsuke Kawaguchi
 */
public abstract class Bridge<T> {
    protected Bridge(JAXBContextImpl context) {
        this.context = context;
    }

    protected final JAXBContextImpl context;

    /**
     *
     * @throws JAXBException
     *      if there was an error while marshalling.
     *
     * @since 2.0 EA1
     */
    public final void marshal(T object,XMLStreamWriter output) throws JAXBException {
        Marshaller m = context.marshallerPool.take();
        marshal(m,object,output);
        context.marshallerPool.recycle(m);
    }

    public final void marshal(@NotNull BridgeContext context,T object,XMLStreamWriter output) throws JAXBException {
        marshal( ((BridgeContextImpl)context).marshaller, object, output );
    }

    public abstract void marshal(@NotNull Marshaller m,T object,XMLStreamWriter output) throws JAXBException;


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
    public void marshal(T object,OutputStream output, NamespaceContext nsContext) throws JAXBException {
        Marshaller m = context.marshallerPool.take();
        marshal(m,object,output,nsContext);
        context.marshallerPool.recycle(m);
    }

    public final void marshal(@NotNull BridgeContext context,T object,OutputStream output, NamespaceContext nsContext) throws JAXBException {
        marshal( ((BridgeContextImpl)context).marshaller, object, output, nsContext );
    }

    public abstract void marshal(@NotNull Marshaller m,T object,OutputStream output, NamespaceContext nsContext) throws JAXBException;


    public final void marshal(T object,Node output) throws JAXBException {
        Marshaller m = context.marshallerPool.take();
        marshal(m,object,output);
        context.marshallerPool.recycle(m);
    }

    public final void marshal(@NotNull BridgeContext context,T object,Node output) throws JAXBException {
        marshal( ((BridgeContextImpl)context).marshaller, object, output );
    }

    public abstract void marshal(@NotNull Marshaller m,T object,Node output) throws JAXBException;


    /**
     * @since 2.0 EA4
     */
    public final void marshal(T object, ContentHandler contentHandler) throws JAXBException {
        Marshaller m = context.marshallerPool.take();
        marshal(m,object,contentHandler);
        context.marshallerPool.recycle(m);
    }
    public final void marshal(@NotNull BridgeContext context,T object, ContentHandler contentHandler) throws JAXBException {
        marshal( ((BridgeContextImpl)context).marshaller, object, contentHandler );
    }
    public abstract void marshal(@NotNull Marshaller m,T object, ContentHandler contentHandler) throws JAXBException;

    /**
     * @since 2.0 EA4
     */
    public final void marshal(T object, Result result) throws JAXBException {
        Marshaller m = context.marshallerPool.take();
        marshal(m,object,result);
        context.marshallerPool.recycle(m);
    }
    public final void marshal(@NotNull BridgeContext context,T object, Result result) throws JAXBException {
        marshal( ((BridgeContextImpl)context).marshaller, object, result );
    }
    public abstract void marshal(@NotNull Marshaller m,T object, Result result) throws JAXBException;



    private T exit(T r, Unmarshaller u) {
        context.unmarshallerPool.recycle(u);
        return r;
    }

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
    public final @NotNull T unmarshal(@NotNull XMLStreamReader in) throws JAXBException {
        Unmarshaller u = context.unmarshallerPool.take();
        return exit(unmarshal(u,in),u);
    }
    public final @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull XMLStreamReader in) throws JAXBException {
        return unmarshal( ((BridgeContextImpl)context).unmarshaller, in );
    }
    public abstract @NotNull T unmarshal(@NotNull Unmarshaller u, @NotNull XMLStreamReader in) throws JAXBException;

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
    public final @NotNull T unmarshal(@NotNull Source in) throws JAXBException {
        Unmarshaller u = context.unmarshallerPool.take();
        return exit(unmarshal(u,in),u);
    }
    public final @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull Source in) throws JAXBException {
        return unmarshal( ((BridgeContextImpl)context).unmarshaller, in );
    }
    public abstract @NotNull T unmarshal(@NotNull Unmarshaller u, @NotNull Source in) throws JAXBException;

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
    public final @NotNull T unmarshal(@NotNull InputStream in) throws JAXBException {
        Unmarshaller u = context.unmarshallerPool.take();
        return exit(unmarshal(u,in),u);
    }
    public final @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull InputStream in) throws JAXBException {
        return unmarshal( ((BridgeContextImpl)context).unmarshaller, in );
    }
    public abstract @NotNull T unmarshal(@NotNull Unmarshaller u, @NotNull InputStream in) throws JAXBException;

    /**
     * Unmarshals the specified type object.
     *
     * @param n
     *      Node to be unmarshalled.
     *
     * @return
     *      never null.
     *
     * @throws JAXBException
     *      if there was an error while unmarshalling.
     *
     * @since 2.0 FCS
     */
    public final @NotNull T unmarshal(@NotNull Node n) throws JAXBException {
        Unmarshaller u = context.unmarshallerPool.take();
        return exit(unmarshal(u,n),u);
    }
    public final @NotNull T unmarshal(@NotNull BridgeContext context, @NotNull Node n) throws JAXBException {
        return unmarshal( ((BridgeContextImpl)context).unmarshaller, n );
    }
    public abstract @NotNull T unmarshal(@NotNull Unmarshaller context, @NotNull Node n) throws JAXBException;

    /**
     * Gets the {@link TypeReference} from which this bridge was created.
     */
    public abstract TypeReference getTypeReference();
}

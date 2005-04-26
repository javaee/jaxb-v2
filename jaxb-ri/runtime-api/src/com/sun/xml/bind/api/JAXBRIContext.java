/*
 * @(#)$Id: JAXBRIContext.java,v 1.4 2005-04-26 20:35:47 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.bind.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

/**
 * {@link JAXBContext} enhanced with JAXB RI specific functionalities.
 *
 * <p>
 * <b>Subject to change without notice</b>.
 * 
 * @since 2.0 EA1
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class JAXBRIContext extends JAXBContext {
    protected JAXBRIContext() {}

    /**
     * Creates a new {@link JAXBRIContext}.
     *
     * <p>
     * {@link JAXBContext#newInstance JAXBContext.newInstance()} methods may
     * return other JAXB providers that are not compatible with the JAX-RPC RI.
     *
     * <p>
     * This method guarantees that the JAX-RPC RI will finds the JAXB RI.
     *
     * <P>
     * TODO: verify that the parameters are OK with the JAX-RPC. 
     */
    public static final JAXBRIContext newInstance(Class[] classes, Collection<TypeReference> typeRefs, String defaultNamespaceRemap ) throws JAXBException {
        try {
            Class c = Class.forName("com.sun.xml.bind.v2.ContextFactory");
            Method method = c.getMethod("createContext",Class[].class,Collection.class,String.class);
            Object o = method.invoke(null,classes,typeRefs,defaultNamespaceRemap);
            return (JAXBRIContext)o;
        } catch (ClassNotFoundException e) {
            throw new JAXBException(e);
        } catch (NoSuchMethodException e) {
            throw new JAXBException(e);
        } catch (IllegalAccessException e) {
            throw new JAXBException(e);
        } catch (InvocationTargetException e) {
            Throwable te = e.getTargetException();
            if(te instanceof JAXBException)
                throw (JAXBException)te;
            if(te instanceof RuntimeException)
                throw (RuntimeException)te;
            if(te instanceof Error)
                throw (Error)te;
            throw new JAXBException(e);
        }
    }

    /**
     * If the given object is bound to an element in XML by JAXB,
     * returns the element name.
     *
     * @return null
     *      if the object is not bound to an element.
     * @throws JAXBException
     *      if the object is not known to this context.
     *
     * @since 2.0 EA1
     */
    public abstract QName getElementName(Object o) throws JAXBException;

    /**
     * Creates a mini-marshaller/unmarshaller that can process a {@link TypeReference}.
     *
     * @return
     *      null if the specified reference is not given to {@link JAXBRIContext#newInstance}.
     *
     * @since 2.0 EA1
     */
    public abstract Bridge createBridge(TypeReference ref);

    /**
     * Creates a new {@link BridgeContext} instance.
     *
     * @return
     *      always a valid non-null instance.
     *
     * @since 2.0 EA1
     */
    public abstract BridgeContext createBridgeContext();

    /**
     * Gets a {@link RawAccessor} for the specified element property of the specified wrapper bean class.
     *
     * <p>
     * This method is designed to assist the JAX-RPC RI fill in a wrapper bean (in the doc/lit/wrap mode.)
     * In the said mode, a wrapper bean is supposed to only have properties that match elements,
     * and for each element that appear in the content model there's one property.
     *
     * <p>
     * Therefore, this method takes a wrapper bean and a tag name that identifies a property
     * on the given wrapper bean, then returns a {@link RawAccessor} that allows the caller
     * to set/get a value from the property of the bean.
     *
     * <p>
     * This method is not designed for a performance. The caller is expected to cache the result.
     *
     * @param <B>
     *      type of the wrapper bean
     * @param <V>
     *      type of the property of the bean
     * @return
     *      always return non-null valid accessor object.
     * @throws JAXBException
     *      if the specified wrapper bean is not bound by JAXB, or if it doesn't have an element property
     *      of the given name.
     *
     * @since 2.0 EA1
     */
    public abstract <B,V> RawAccessor<B,V> getElementPropertyAccessor( Class<B> wrapperBean, String nsUri, String localName )
            throws JAXBException;

    /**
     * Gets the namespace URIs statically known to this {@link JAXBContext}.
     *
     * <p>
     * When JAXB is used to marshal into sub-trees, it declares
     * these namespace URIs at each top-level element that it marshals.
     *
     * To avoid repeated namespace declarations at sub-elements, the application
     * may declare those namespaces at a higher level.
     *
     * @return
     *      always non-null.
     *
     * @since 2.0 EA2
     */
    public abstract List<String> getKnownNamespaceURIs();


    /**
     * Generates the schema documents from the model.
     *
     * <p>
     * The caller can use the additionalElementDecls parameter to
     * add element declarations to the generate schema.
     * For example, if the JAX-RPC passes in the following entry:
     *
     * {foo}bar -> DeclaredType for java.lang.String
     *
     * then JAXB generates the following element declaration (in the schema
     * document for the namespace "foo")"
     *
     * &lt;xs:element name="bar" type="xs:string" />
     *
     * This can be used for generating schema components necessary for WSDL.
     *
     * @param outputResolver
     *      this object controls the output to which schemas
     *      will be sent.
     *
     * @throws IOException
     *      if {@link SchemaOutputResolver} throws an {@link IOException}.
     */
    public abstract void generateSchema(SchemaOutputResolver outputResolver) throws IOException;

    /**
     * The property that you can specify to {@link JAXBContext#newInstance}
     * to reassign the default namespace URI to something else at the runtime.
     *
     * <p>
     * The value of the property is {@link String}, and it is used as the namespace URI
     * that succeeds the default namespace URI.
     *
     * @since 2.0 EA1
     */
    public static final String DEFAULT_NAMESPACE_REMAP = "com.sun.xml.bind.defaultNamespaceRemap";

    /**
     * The property that you can specify to {@link JAXBContext#newInstance}
     * to put additional JAXB type references into the {@link JAXBContext}.
     *
     * <p>
     * The value of the property is {@link Collection}&lt;{@link TypeReference}>.
     * Those {@link TypeReference}s can then be used to create {@link Bridge}s.
     *
     * @since 2.0 EA1
     */
    public static final String TYPE_REFERENCES = "com.sun.xml.bind.typeReferences";

    /**
     * Marshaller/Unmarshaller property to enable XOP processing.
     *
     * @since 2.0 EA2
     */
    public static final String ENABLE_XOP = "com.sun.xml.bind.XOP";
}

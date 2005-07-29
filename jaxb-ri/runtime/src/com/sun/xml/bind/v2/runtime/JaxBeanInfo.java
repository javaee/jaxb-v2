/*
 * @(#)$Id: JaxBeanInfo.java,v 1.4 2005-07-29 22:43:35 ryan_shoemaker Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.bind.v2.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;

import org.xml.sax.SAXException;

/**
 * Encapsulates various JAXB operations on objects bound by JAXB.
 * Immutable and thread-safe.
 * 
 * <p>
 * Each JAXB-bound class has a corresponding {@link JaxBeanInfo} object,
 * which performs all the JAXB related operations on behalf of
 * the JAXB-bound object.
 * 
 * <p>
 * Given a class, the corresponding {@link JaxBeanInfo} can be located
 * via {@link JAXBContextImpl#getBeanInfo(Class,boolean)}.
 * 
 * <p>
 * Typically, {@link JaxBeanInfo} implementations should be generated
 * by XJC/JXC. Those impl classes will register themselves to their
 * master <tt>ObjectFactory</tt> class.
 *
 * <p>
 * The type parameter BeanT is the Java class of the bean that this represents.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class JaxBeanInfo<BeanT> {

    protected JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, QName typeName, boolean isElement,boolean isImmutable) {
        grammar.beanInfos.put(rti,this);

        this.jaxbType = jaxbType;
        this.typeName = typeName;
        this.flag = (byte)((isElement?FLAG_IS_ELEMENT:0)|(isImmutable?FLAG_IS_IMMUTABLE:0));
    }

    /**
     * Various boolean flags combined into one field to improve memory footprint.
     */
    protected byte flag;

    private static final byte FLAG_IS_ELEMENT = 1;
    private static final byte FLAG_IS_IMMUTABLE = 2;
    private static final byte FLAG_HAS_ELEMENT_ONLY_CONTENTMODEL = 4;
    private static final byte FLAG_HAS_BEFORE_UNMARSHAL_METHOD = 8;
    private static final byte FLAG_HAS_AFTER_UNMARSHAL_METHOD = 16;
    private static final byte FLAG_HAS_BEFORE_MARSHAL_METHOD = 32;
    private static final byte FLAG_HAS_AFTER_MARSHAL_METHOD = 64;

    /** cache of lifecycle methods */
    private LifecycleMethods lcm = null;

    /**
     * True if {@link #jaxbType} has the  lifecycle method.
     */
    public final boolean hasBeforeUnmarshalMethod() {
        return (flag&FLAG_HAS_BEFORE_UNMARSHAL_METHOD) != 0;
    }

    /**
     * True if {@link #jaxbType} has the  lifecycle method.
     */
    public final boolean hasAfterUnmarshalMethod() {
        return (flag&FLAG_HAS_AFTER_UNMARSHAL_METHOD) != 0;
    }

    /**
     * True if {@link #jaxbType} has the  lifecycle method.
     */
    public final boolean hasBeforeMarshalMethod() {
        return (flag&FLAG_HAS_BEFORE_MARSHAL_METHOD) != 0;
    }

    /**
     * True if {@link #jaxbType} has the  lifecycle method.
     */
    public final boolean hasAfterMarshalMethod() {
        return (flag&FLAG_HAS_AFTER_MARSHAL_METHOD) != 0;
    }

    /**
     * Gets the JAXB bound class type that this {@link JaxBeanInfo}
     * handles.
     * 
     * <p>
     * IOW, when a bean info object is requested for T,
     * sometimes the bean info for one of its base classes might be
     * returned.
     */
    public final Class<BeanT> jaxbType;

    /**
     * Returns true if the bean is mapped to/from an XML element.
     * 
     * <p>
     * When this method returns true, {@link #getElementNamespaceURI(Object)}
     * and {@link #getElementLocalName(Object)} returns the element name of
     * the bean.
     */
    public final boolean isElement() {
        return (flag&FLAG_IS_ELEMENT)!=0;
    }

    /**
     * Returns true if the bean is immutable.
     *
     * <p>
     * If this is true, Binder won't try to ueuse this object, and the unmarshaller
     * won't create a new instance of it before it starts.
     */
    public final boolean isImmutable() {
        return (flag&FLAG_IS_IMMUTABLE)!=0;
    }

    /**
     * True if this bean has an element-only content model.
     * <p>
     * If this flag is true, the unmarshaller can work
     * faster by ignoring whitespaces more efficiently.
     */
    public final boolean hasElementOnlyContentModel() {
        return (flag&FLAG_HAS_ELEMENT_ONLY_CONTENTMODEL)!=0;
    }

    /**
     * True if this bean has an element-only content model.
     * <p>
     * Should be considered immutable, though I can't mark it final
     * because it cannot be computed in this constructor.
     */ 
    protected final void hasElementOnlyContentModel(boolean value) {
        if(value)
            flag |= FLAG_HAS_ELEMENT_ONLY_CONTENTMODEL;
        else
            flag &= ~FLAG_HAS_ELEMENT_ONLY_CONTENTMODEL;
    }

    /**
     * Returns the namespace URI portion of the element name,
     * if the bean that this class represents is mapped from/to
     * an XML element.
     * 
     * @throws UnsupportedOperationException
     *      if {@link #isElement} is false.
     */
    public abstract String getElementNamespaceURI(BeanT o);
    
    /**
     * Returns the local name portion of the element name,
     * if the bean that this class represents is mapped from/to
     * an XML element.
     * 
     * @throws UnsupportedOperationException
     *      if {@link #isElement} is false.
     */
    public abstract String getElementLocalName(BeanT o);
    
    /**
     * Returns the XML Schema type name if the bean is mapped to/from
     * a complex type of XML Schema.
     * 
     * <p>
     * This is an ugly necessity to correctly handle
     * the type substitution semantics of XML Schema.
     *
     * <p>
     * A single Java class maybe mapped to more than one
     * XML types. This method returns the "primary type" for that
     * Java class, which we use when we marshal.
     *
     * <p>
     * null if the class is not bound to a schema type.
     */
    public final QName typeName;

    /**
     * Creates a new instance of the bean.
     *
     * <p>
     * This operation is only supported when {@link #isImmutable} is false.
     *
     * @param context
     *      Sometimes the created bean remembers the corresponding source location,
     *      and this information is used to obtain that information.
     */
    public abstract BeanT createInstance(UnmarshallingContext context) throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException;

    /**s
     * Resets the object to the initial state, as if the object
     * is created fresh.
     * 
     * <p>
     * This is used to reuse an existing object for unmarshalling.
     *
     * @param context
     *      used for reporting any errors.
     *
     * @return
     *      true if the object was successfuly resetted.
     *      False if the object is not resettable, in which case the object will be
     *      discarded and new one will be created.
     *      <p>
     *      If the object is resettable but failed by an error, it should be reported to the context,
     *      then return false. If the object is not resettable to begin with, do not report an error.
     *
     * @throws SAXException
     *      as a result of reporting an error, the context may throw a {@link SAXException}.
     */
    public abstract boolean reset( BeanT o, UnmarshallingContext context ) throws SAXException;
    
    /**
     * Gets the ID value of the given bean, if it has an ID value.
     * Otherwise return null.
     */
    public abstract String getId(BeanT o, XMLSerializer target) throws SAXException;
    
    /**
     * Serializes child elements and texts into the specified target.
     */
    public abstract void serializeBody( BeanT o, XMLSerializer target ) throws SAXException, IOException, XMLStreamException;
    
    /**
     * Serializes attributes into the specified target.
     */
    public abstract void serializeAttributes( BeanT o, XMLSerializer target ) throws SAXException, IOException, XMLStreamException;

    /**
     * Serializes the bean as the root element.
     *
     * <p>
     * In the java-to-schema binding, an object might marshal in two different
     * ways depending on whether it is used as the root of the graph or not.
     * In the former case, an object could marshal as an element, whereas
     * in the latter case, it marshals as a type.
     *
     * <p>
     * This method is used to marshal the root of the object graph to allow
     * this semantics to be implemented.
     *
     * <p>
     * It is doubtful to me if it's a good idea for an object to marshal
     * in two ways depending on the context.
     *
     * <p>
     * For schema-to-java, this is equivalent to {@link #serializeBody(Object, XMLSerializer)}.
     */
    public abstract void serializeRoot( BeanT o, XMLSerializer target ) throws SAXException, IOException, XMLStreamException;

    /**
     * Declares all the namespace URIs this object is using at
     * its top-level scope into the specified target.
     */
    public abstract void serializeURIs( BeanT o, XMLSerializer target ) throws SAXException;
    
    /**
     * Gets an unmarshaller that will unmarshall the given object.
     *
     * @return
     *      must return non-null valid object
     * @param root
     *      In the java-to-schema binding, an object might unmarshal in two different
     *      ways depending on whether it is used as the root of the graph or not.
     *      In the former case, an object could unmarshal as an element, whereas
     *      in the latter case, it marshals as a type.
     *
     *      This is the ugly flag to handle those two cases. If true, it returns
     *      the unmarshaller for unmarshalling it as an element. Otherwise as the type.
     */
    public abstract UnmarshallingEventHandler getUnmarshaller(boolean root);

    /**
     * If the bean's representation in XML is just a text,
     * this method return a {@link Transducer} that lets you convert
     * values between the text and the bean.
     */
    public abstract Transducer<BeanT> getTransducer();


    /**
     * Called after all the {@link JaxBeanInfo}s are created.
     * @param grammar
     */
    protected  void link(JAXBContextImpl grammar) {
    }

    /**
     * Called at the end of the {@link JAXBContext} initialization phase
     * to clean up any unnecessary references.
     */
    public void wrapUp() {}

    /**
     * This method is used to determine which of the sub-classes should be
     * interrogated for the existence of lifecycle methods.
     *
     * @return true if the un|marshaller should look for lifecycle methods
     *         on this beanInfo, false otherwise.
     */
    public boolean lookForLifecycleMethods() {
        // overridden by subclasses that return true
        return false;
    }

    /**
     * use reflection to determine which of the 4 object lifecycle methods exist on
     * the JAXB bound type.
     */
    protected void setLifecycleFlags() {
        Method m;

        // two possible parameter sets
        Class[] unmarshalEventParams = new Class[] { javax.xml.bind.Unmarshaller.class, Object.class };
        Class[] marshalEventParams = new Class[] { javax.xml.bind.Marshaller.class };

        // beforeUnmarshal
        try {
            m = jaxbType.getDeclaredMethod("beforeUnmarshal", unmarshalEventParams);
            cacheLifecycleMethod(m, FLAG_HAS_BEFORE_UNMARSHAL_METHOD);
        } catch (NoSuchMethodException e) {
            // no-op, look for the next method
        }

        // afterUnmarshal
        try {
            m = jaxbType.getDeclaredMethod("afterUnmarshal", unmarshalEventParams);
            cacheLifecycleMethod(m, FLAG_HAS_AFTER_UNMARSHAL_METHOD);
        } catch (NoSuchMethodException e) {
            // no-op, look for the next method
        }

        // beforeMarshal
        try {
            m = jaxbType.getDeclaredMethod("beforeMarshal", marshalEventParams);
            cacheLifecycleMethod(m, FLAG_HAS_BEFORE_MARSHAL_METHOD);
        } catch (NoSuchMethodException e) {
            // no-op, look for the next method
        }

        // afterMarshal
        try {
            m = jaxbType.getDeclaredMethod("afterMarshal", marshalEventParams);
            cacheLifecycleMethod(m, FLAG_HAS_AFTER_MARSHAL_METHOD);
        } catch (NoSuchMethodException e) {
            // no-op
        }
    }

    /**
     * Cache a reference to the specified lifecycle method for the jaxbType
     * associated with this beanInfo.
     *
     * @param m Method reference
     * @param lifecycleFlag byte representing which of the 4 lifecycle methods
     *        is being cached
     */
    private void cacheLifecycleMethod(Method m, byte lifecycleFlag) {
        //LifecycleMethods lcm = getLifecycleMethods();
        if(lcm==null) {
            lcm = new LifecycleMethods();
            //lcmCache.put(jaxbType, lcm);
        }

        m.setAccessible(true);

        flag |= lifecycleFlag;

        switch (lifecycleFlag) {
        case FLAG_HAS_BEFORE_UNMARSHAL_METHOD:
            lcm.setBeforeUnmarshal(m);
            break;
        case FLAG_HAS_AFTER_UNMARSHAL_METHOD:
            lcm.setAfterUnmarshal(m);
            break;
        case FLAG_HAS_BEFORE_MARSHAL_METHOD:
            lcm.setBeforeMarshal(m);
            break;
        case FLAG_HAS_AFTER_MARSHAL_METHOD:
            lcm.setAfterMarshal(m);
            break;
        }
    }

    /**
     * Return the LifecycleMethods cache for this ClassBeanInfo's corresponding
     * jaxbType if it exists, else return null.
     *
     */
    public LifecycleMethods getLifecycleMethods() {
        //return (LifecycleMethods)lcmCache.get(jaxbType);
        return lcm;
    }
}

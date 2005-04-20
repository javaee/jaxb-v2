/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: JAXBContextImpl.java,v 1.4 2005-04-20 23:57:33 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Validator;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import com.sun.xml.bind.annotation.Binder;
import com.sun.xml.bind.annotation.XmlList;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.RawAccessor;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.impl.RuntimeAnyTypeImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class provides the implementation of JAXBContext.  It
 * also creates the GrammarInfoFacade that unifies all of the grammar
 * info from packages on the contextPath.
 *
 * @version $Revision: 1.4 $
 */
public final class JAXBContextImpl extends JAXBRIContext {

    /**
     * All the bridge classes.
     */
    private final Map<TypeReference,Bridge> bridges = new LinkedHashMap<TypeReference,Bridge>();

    /**
     * Shared instance of {@link TransformerFactory}.
     * Lock before use, because a {@link TransformerFactory} is not thread-safe
     * whereas {@link JAXBContextImpl} is.
     */
    private static final SAXTransformerFactory tf = (SAXTransformerFactory)TransformerFactory.newInstance();

    /**
     * Shared instance of {@link DocumentBuilder}.
     * Lock before use.
     */
    private static final DocumentBuilder db;
    static {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // impossible
            throw new FactoryConfigurationError(e);
        }
    }

    private final Map<QName,JaxBeanInfo> rootMap = new LinkedHashMap<QName,JaxBeanInfo>();
    private final Map<QName,JaxBeanInfo> typeMap = new LinkedHashMap<QName,JaxBeanInfo>();

    /**
     * Map from JAXB-bound {@link Class} to its {@link JaxBeanInfo}.
     */
    private final Map<Class,JaxBeanInfo> beanInfoMap = new LinkedHashMap<Class,JaxBeanInfo>();

    /**
     * All created {@link JaxBeanInfo}s.
     * Updated from each {@link JaxBeanInfo}s constructors to avoid infinite recursion
     * for a cyclic reference.
     *
     * <p>
     * This map is only used while the {@link JAXBContextImpl} is built and set to null
     * to avoid keeping references too long.
     */
    protected Map<RuntimeTypeInfo,JaxBeanInfo> beanInfos = new LinkedHashMap<RuntimeTypeInfo, JaxBeanInfo>();

    private final Map<Class/*scope*/,Map<QName,ElementBeanInfoImpl>> elements = new LinkedHashMap<Class, Map<QName, ElementBeanInfoImpl>>();

//    /**
//     * Special {@link JaxBeanInfo} for <tt>xs:anyType</tt>.
//     * This cannot be found through {@link #beanInfos} because this is the only {@link JaxBeanInfo}
//     * that works with an interface (and thus no TypeInfo entry, no Class representation.)
//     */
//    private final JaxBeanInfo anyTypeBeanInfo;

    /**
     * Used to assign indices to known names in this grammar.
     * Reset to null once the build phase is completed.
     */
    public NameBuilder nameBuilder = new NameBuilder();

    /**
     * Keeps the list of known names.
     * This field is set once the build pahse is completed.
     */
    public final NameList nameList;

    /**
     *
     * @param typeSet
     *      controls where the annotations are read from.
     * @param typeRefs
     *      used to build {@link Bridge}s. Can be empty.
     */
    public JAXBContextImpl( RuntimeTypeInfoSet typeSet, Collection<TypeReference> typeRefs ) throws JAXBException {

        // at least prepare the empty table so that we don't have to check for null later
        elements.put(null,new LinkedHashMap<QName, ElementBeanInfoImpl>());

        // recognize leaf bean infos
        for( RuntimeBuiltinLeafInfo leaf : RuntimeBuiltinLeafInfoImpl.builtinBeanInfos ) {
            Class leafType = leaf.getClazz();
            LeafBeanInfoImpl bi = new LeafBeanInfoImpl(this,leaf);
            beanInfoMap.put(leafType,bi);
            for( QName t : leaf.getTypeNames() )
                typeMap.put(t,bi);
        }

        for (RuntimeEnumLeafInfo e : typeSet.enums().values()) {
            JaxBeanInfo bi = getOrCreate(e);
            if( bi.typeName!=null )
                typeMap.put( bi.typeName, bi );
        }

        for (RuntimeArrayInfo a : typeSet.arrays().values()) {
            JaxBeanInfo ai = getOrCreate(a);
            assert ai.typeName!=null;
            typeMap.put(ai.typeName,ai);
        }

        for( RuntimeClassInfo ci : typeSet.beans().values() ) {
            ClassBeanInfoImpl bi = getOrCreate(ci);

            if(bi.isElement())
                rootMap.put( ci.getElementName(), bi );

            QName n = bi.typeName;
            if(n!=null)
                typeMap.put(n,bi);
        }

        // fill in element mappings
        for( RuntimeElementInfo n : typeSet.getAllElements() ) {
            ElementBeanInfoImpl bi = getOrCreate(n);
            if(n.getScope()==null)
                rootMap.put(n.getElementName(),bi);

            RuntimeClassInfo scope = n.getScope();
            Class scopeClazz = scope==null?null:scope.getClazz();
            Map<QName,ElementBeanInfoImpl> m = elements.get(scopeClazz);
            if(m==null) {
                m = new LinkedHashMap<QName, ElementBeanInfoImpl>();
                elements.put(scopeClazz,m);
            }
            m.put(n.getElementName(),bi);
        }

        // this one is so that we can handle plain JAXBElements.
        beanInfoMap.put(JAXBElement.class,new ElementBeanInfoImpl(this));

        getOrCreate(RuntimeAnyTypeImpl.theInstance);

        // then link them all!
        for (JaxBeanInfo bi : beanInfos.values())
            bi.link(this);

        // register primitives for boxed types just to make GrammarInfo fool-proof
        for( Map.Entry<Class,Class> e : Util.primitiveToBox.entrySet() )
            beanInfoMap.put( e.getKey(), beanInfoMap.get(e.getValue()) );

        // build bridges
        ReflectionNavigator nav = typeSet.getNavigator();

        for (TypeReference tr : typeRefs) {
            XmlJavaTypeAdapter xjta = tr.get(XmlJavaTypeAdapter.class);
            Adapter<Type,Class> a=null;
            XmlList xl = tr.get(XmlList.class);

            // eventually compute the in-memory type
            Class erasedType = nav.erasure(tr.type);

            if(xjta!=null) {
                a = new Adapter<Type,Class>(xjta.value(),nav);
                erasedType = nav.erasure(a.defaultType);
            }

            Name name = nameBuilder.createElementName(tr.tagName);

            Bridge bridge;
            if(xl==null)
                bridge = new BridgeImpl(name,getBeanInfo(erasedType,true),tr);
            else
                bridge = new BridgeImpl(name,new ValueListBeanInfoImpl(this,erasedType),tr);

            if(a!=null)
                bridge = new BridgeAdapter(bridge,xjta.value());

            bridges.put(tr,bridge);
        }


        this.nameList = nameBuilder.conclude();

        for (JaxBeanInfo bi : beanInfos.values())
            bi.wrapUp();
        
        // no use for them now
        nameBuilder = null;
        beanInfos = null;
    }


    public ElementBeanInfoImpl getElement(Class scope, QName name) {
        Map<QName,ElementBeanInfoImpl> m = elements.get(scope);
        if(m!=null) {
            ElementBeanInfoImpl bi = m.get(name);
            if(bi!=null)
                return bi;
        }
        m = elements.get(null);
        return m.get(name);
    }





    private ElementBeanInfoImpl getOrCreate( RuntimeElementInfo rei ) {
        JaxBeanInfo bi = beanInfos.get(rei);
        if(bi!=null)    return (ElementBeanInfoImpl)bi;

        // all elements share the same type, so we can't register them to beanInfoMap
        return new ElementBeanInfoImpl(this, rei);
    }

    protected JaxBeanInfo getOrCreate( RuntimeEnumLeafInfo eli ) {
        JaxBeanInfo bi = beanInfos.get(eli);
        if(bi!=null)    return bi;
        bi = new LeafBeanInfoImpl(this,eli);
        beanInfoMap.put(bi.jaxbType,bi);
        return bi;
    }

    protected ClassBeanInfoImpl getOrCreate( RuntimeClassInfo ci ) {
        ClassBeanInfoImpl bi = (ClassBeanInfoImpl)beanInfos.get(ci);
        if(bi!=null)    return bi;
        bi = new ClassBeanInfoImpl(this,ci);
        beanInfoMap.put(bi.jaxbType,bi);
        return bi;
    }

    protected JaxBeanInfo getOrCreate( RuntimeArrayInfo ai ) {
        JaxBeanInfo abi = beanInfos.get(ai.getType());
        if(abi!=null)   return abi;

        abi = new ArrayBeanInfoImpl(this,ai);

        beanInfoMap.put(ai.getType(),abi);
        return abi;
    }

    public JaxBeanInfo getOrCreate(RuntimeTypeInfo e) {
        if(e instanceof RuntimeElementInfo)
            return getOrCreate((RuntimeElementInfo)e);
        if(e instanceof RuntimeClassInfo)
            return getOrCreate((RuntimeClassInfo)e);
        if(e instanceof RuntimeLeafInfo) {
            JaxBeanInfo bi = beanInfos.get(e); // must have been created
            assert bi!=null;
            return bi;
        }
        if(e instanceof RuntimeArrayInfo)
            return getOrCreate((RuntimeArrayInfo)e);
        if(e==RuntimeAnyTypeImpl.theInstance) {
            // anyType
            JaxBeanInfo bi = beanInfoMap.get(Object.class);
            if(bi==null) {
                bi = new AnyTypeBeanInfo(this);
                beanInfoMap.put(Object.class,bi);
            }
            return bi;
        }

        throw new IllegalArgumentException();
    }

    /**
     * Gets the {@link JaxBeanInfo} object that can handle
     * the given JAXB-bound object.
     *
     * <p>
     * This method traverses the base classes of the given object.
     *
     * @return null
     *      if <tt>c</tt> isn't a JAXB-bound class and <tt>fatal==false</tt>.
     */
    public final JaxBeanInfo getBeanInfo(Object o) {
        // don't allow xs:anyType beanInfo to handle all the unbound objects
        for( Class c=o.getClass(); c!=Object.class; c=c.getSuperclass()) {
            JaxBeanInfo bi = beanInfoMap.get(c);
            if(bi!=null)    return bi;
        }
        if(o instanceof Element)
            return beanInfoMap.get(Object.class);   // return the BeanInfo for xs:anyType
        return null;
    }

    /**
     * Gets the {@link JaxBeanInfo} object that can handle
     * the given JAXB-bound object.
     *
     * @param fatal
     *      if true, the failure to look up will throw an exception.
     *      Otherwise it will just return null.
     */
    public final JaxBeanInfo getBeanInfo(Object o,boolean fatal) throws JAXBException {
        JaxBeanInfo bi = getBeanInfo(o);
        if(bi!=null)    return bi;
        if(fatal)
            throw new JAXBException(o.getClass().getName()+" nor any of its super class is known to this context");
        return null;
    }

    /**
     * Gets the {@link JaxBeanInfo} object that can handle
     * the given JAXB-bound class.
     *
     * <p>
     * This method doesn't look for base classes.
     *
     * @return null
     *      if <tt>c</tt> isn't a JAXB-bound class and <tt>fatal==false</tt>.
     */
    public final JaxBeanInfo getBeanInfo(Class clazz) {
        return beanInfoMap.get(clazz);
    }

    /**
     * Gets the {@link JaxBeanInfo} object that can handle
     * the given JAXB-bound class.
     *
     * @param fatal
     *      if true, the failure to look up will throw an exception.
     *      Otherwise it will just return null.
     */
    public final JaxBeanInfo getBeanInfo(Class clazz,boolean fatal) throws JAXBException {
        JaxBeanInfo bi = getBeanInfo(clazz);
        if(bi!=null)    return bi;
        if(fatal)
            throw new JAXBException(clazz.getName()+" is not known to this context");
        return null;
    }

    /**
     * Creates an unmarshaller that can unmarshal a given element,
     * then create a new object to unmarshal, and push the newly created
     * {@link UnmarshallingEventHandler} to the context.
     *
     * @param namespaceUri
     *      The string needs to be interned by the caller
     *      for a performance reason.
     * @param localName
     *      The string needs to be interned by the caller
     *      for a performance reason.
     * @return
     *      null if the given name pair is not recognized.
     */
    public final UnmarshallingEventHandler pushUnmarshaller(
        String namespaceUri, String localName, UnmarshallingContext context ) throws SAXException {

        JaxBeanInfo beanInfo = rootMap.get(new QName(namespaceUri,localName));
        if(beanInfo==null)        return null;

        // try the outer peer of the current element first
        Object child = context.getOuterPeer();
        if(!beanInfo.jaxbType.isInstance(child))
            child = null;   // unexpected type

        if(child!=null) {
            if(!beanInfo.reset(child,context))
                child = null;
        }

        if(child==null)
            child = context.createInstance(beanInfo);

        UnmarshallingEventHandler u = beanInfo.getUnmarshaller(true);
        context.pushContentHandler(u, child, beanInfo.implementsLifecycle());

        return u;
    }

    /**
     * Gets the {@link JaxBeanInfo} for the given named XML Schema type.
     *
     * @return
     *      null if the type name is not recognized. For schema
     *      languages other than XML Schema, this method always
     *      returns null.
     */
    public JaxBeanInfo getGlobalType(QName name) {
        return typeMap.get(name);
    }

    /**
     * Returns the set of valid root tag names.
     */
    public Set<QName> getValidRootNames() {
        return rootMap.keySet();
    }

    /**
     * Cache of UTF-8 encoded local names to improve the performance for the marshalling.
     */
    private Encoded[] utf8nameTable;

    public synchronized Encoded[] getUTF8NameTable() {
        if(utf8nameTable==null) {
            Encoded[] x = new Encoded[nameList.localNames.length];
            for( int i=0; i<x.length; i++ ) {
                Encoded e = new Encoded(nameList.localNames[i]);
                e.compact();
                x[i] = e;
            }
            utf8nameTable = x;
        }
        return utf8nameTable;
    }


    /**
     * Creates a new identity transformer.
     */
    static Transformer createTransformer() {
        try {
            synchronized(tf) {
                return tf.newTransformer();
            }
        } catch (TransformerConfigurationException e) {
            throw new Error(e); // impossible
        }
    }

    /**
     * Creates a new identity transformer.
     */
    public static TransformerHandler createTransformerHandler() {
        try {
            synchronized(tf) {
                return tf.newTransformerHandler();
            }
        } catch (TransformerConfigurationException e) {
            throw new Error(e); // impossible
        }
    }

    /**
     * Creates a new DOM document.
     */
    static Document createDom() {
        synchronized(db) {
            return db.newDocument();
        }
    }

    public MarshallerImpl createMarshaller() {
        return new MarshallerImpl( this );
    }

    public UnmarshallerImpl createUnmarshaller() {
        return new UnmarshallerImpl( this, null );
    }    
        
    public Validator createValidator() {
        throw new UnsupportedOperationException(Messages.NOT_IMPLEMENTED_IN_2_0.format());
    }

    @Override
    public JAXBIntrospector createJAXBIntrospector() {
        return new JAXBIntrospector() {
            public boolean isElement(Object object) {
                return getElementName(object)!=null;
            }

            public QName getElementName(Object jaxbElement) {
                try {
                    return JAXBContextImpl.this.getElementName(jaxbElement);
                } catch (JAXBException e) {
                    return null;
                }
            }
        };
    }


    public Binder<Node> createBinder() {
        return new BinderImpl<Node>(this,new DOMScanner());
    }

    /**
     * There are no required properties, so simply throw an exception.  Other
     * providers may have support for properties on Validator, but the RI doesn't
     */
    public void setProperty( String name, Object value )
        throws PropertyException {
        
        throw new PropertyException(name, value);
    }
    
    /**
     * There are no required properties, so simply throw an exception.  Other
     * providers may have support for properties on Validator, but the RI doesn't
     */
    public Object getProperty( String name )
        throws PropertyException {
            
        throw new PropertyException(name);
    }
    
    public QName getElementName(Object o) throws JAXBException {
        JaxBeanInfo bi = getBeanInfo(o,true);
        if(!bi.isElement())
            return null;
        return new QName(bi.getElementNamespaceURI(o),bi.getElementLocalName(o));
    }

    public boolean isElement(Object o) {
        return getBeanInfo(o)!=null;
    }

    public Bridge createBridge(TypeReference ref) {
        return bridges.get(ref);
    }

    public BridgeContext createBridgeContext() {
        return new BridgeContextImpl(this);
    }

    public RawAccessor getElementPropertyAccessor(Class wrapperBean, String nsUri, String localName) throws JAXBException {
        JaxBeanInfo bi = getBeanInfo(wrapperBean,true);
        if(!(bi instanceof ClassBeanInfoImpl))
            throw new JAXBException(wrapperBean+" is not a bean");

        ClassBeanInfoImpl cb = (ClassBeanInfoImpl) bi;
        for (Property p : cb.properties) {
            Accessor acc = p.getElementPropertyAccessor(nsUri,localName);
            if(acc!=null)
                return acc;
        }
        throw new JAXBException(new QName(nsUri,localName)+" is not a valid property on "+wrapperBean);
    }

    /**
     * Gets the value of the xmime:contentType attribute on the given object, or null
     * if for some reason it couldn't be found, including any error.
     */
    public String getXMIMEContentType( Object o ) {
        JaxBeanInfo bi = getBeanInfo(o);
        if(!(bi instanceof ClassBeanInfoImpl))
            return null;

        ClassBeanInfoImpl cb = (ClassBeanInfoImpl) bi;
        for (Property p : cb.properties) {
            if (p instanceof AttributeProperty) {
                AttributeProperty ap = (AttributeProperty) p;
                if(ap.attName.equals(WellKnownNamespace.XML_MIME_URI,"contentType"))
                    try {
                        return (String)ap.xacc.print(o);
                    } catch (AccessorException e) {
                        return null;
                    } catch (SAXException e) {
                        return null;
                    } catch (ClassCastException e) {
                        return null;
                    }
            }
        }
        return null;
    }
}

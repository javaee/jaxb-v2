package com.sun.xml.bind.v2.model.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import com.sun.xml.bind.v2.FlattenIterator;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.BuiltinLeafInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Util;

/**
 * Set of {@link TypeInfo}s.
 *
 * <p>
 * This contains a fixed set of {@link LeafInfo}s and arbitrary set of {@link ClassInfo}s.
 *
 * <p>
 * Members are annotated with JAXB annotations so that we can dump it easily.
 *
 * @author Kohsuke Kawaguchi
 */
class TypeInfoSetImpl<TypeT,ClassDeclT,FieldT,MethodT> implements
        TypeInfoSet<TypeT,ClassDeclT,FieldT,MethodT> {

    @XmlTransient
    public final Navigator<TypeT,ClassDeclT,FieldT,MethodT> nav;

    @XmlTransient
    public final AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> reader;

    /**
     * All the leaves.
     */
    private final Map<TypeT,BuiltinLeafInfo<TypeT,ClassDeclT>> builtins =
            new LinkedHashMap<TypeT,BuiltinLeafInfo<TypeT,ClassDeclT>>();

    /** All {@link EnumLeafInfoImpl}s. */
    private final Map<ClassDeclT,EnumLeafInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> enums =
            new LinkedHashMap<ClassDeclT,EnumLeafInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>();

    /** All {@link ArrayInfoImpl}s. */
    private final Map<TypeT,ArrayInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> arrays =
            new LinkedHashMap<TypeT,ArrayInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>();

    /**
     * All the user-defined classes.
     *
     * Using {@link LinkedHashMap} allows us to process classes
     * in the order they are given to us. When the user incorrectly
     * puts an unexpected class into a reference graph, this causes
     * an error to be reported on a class closer to the user's code.
     */
    @XmlJavaTypeAdapter(Util.ToStringAdapter.class)
    private final Map<ClassDeclT,ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> beans
            = new LinkedHashMap<ClassDeclT,ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>();

    @XmlTransient
    private final Map<ClassDeclT,ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> beansView =
        Collections.unmodifiableMap(beans);

    /**
     * The element mapping.
     */
    private final Map<ClassDeclT,Map<QName,ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>> elementMappings =
        new LinkedHashMap<ClassDeclT,Map<QName,ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>>();
    
    private final Iterable<? extends ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> allElements =
        new Iterable<ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>() {
            public Iterator<ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> iterator() {
                return new FlattenIterator<ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>(elementMappings.values());
            }
        };

    /**
     * {@link TypeInfo} for <tt>xs:anyType</tt>.
     *
     * anyType is the only {@link TypeInfo} that works with an interface,
     * and accordingly it requires a lot of special casing.
     */
    private final NonElement<TypeT,ClassDeclT> anyType;

    public TypeInfoSetImpl(Navigator<TypeT,ClassDeclT,FieldT,MethodT> nav,
                           AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> reader,
                           Map<TypeT,? extends BuiltinLeafInfoImpl<TypeT,ClassDeclT>> leaves) {
        this.nav = nav;
        this.reader = reader;
        this.builtins.putAll(leaves);

        this.anyType = createAnyType();

        // register primitive types.
        for (Map.Entry<Class, Class> e : Util.primitiveToBox.entrySet()) {
            this.builtins.put( nav.getPrimitive(e.getKey()), leaves.get(nav.ref(e.getValue())) );
        }

        // make sure at lease we got a map for global ones.
        elementMappings.put(null,new LinkedHashMap<QName,ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>());
    }

    protected NonElement<TypeT,ClassDeclT> createAnyType() {
        return new AnyTypeImpl<TypeT,ClassDeclT>(nav);
    }

    public Navigator<TypeT,ClassDeclT,FieldT,MethodT> getNavigator() {
        return nav;
    }

    /**
     * Adds a new {@link ClassInfo} to the set.
     */
    public void add( ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> ci ) {
        beans.put( ci.getClazz(), ci );
    }

    /**
     * Adds a new {@link LeafInfo} to the set.
     */
    public void add( EnumLeafInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> li ) {
        enums.put( li.clazz,  li );
    }

    public void add(ArrayInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> ai) {
        arrays.put( ai.getType(), ai );
    }

    /**
     * Returns a {@link TypeInfo} for the given type.
     *
     * @return
     *      null if the specified type cannot be bound by JAXB, or
     *      not known to this set.
     */
    public NonElement<TypeT,ClassDeclT> getTypeInfo( TypeT type ) {
        LeafInfo<TypeT,ClassDeclT> l = builtins.get(type);
        if(l!=null)     return l;

        if( nav.isArray(type) ) {
            ArrayInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> a = arrays.get(type);
            return a;
        }

        if(nav.ref(Object.class).equals(type))
            return anyType;

        ClassDeclT d = nav.asDecl(type);
        if(d==null)     return null;
        return getClassInfo(d);
    }

    /**
     * This method is used to add a root reference to a model.
     */
    public NonElement<TypeT,ClassDeclT> getTypeInfo(Ref<TypeT,ClassDeclT> ref) {
        // TODO: handle XmlValueList
        assert !ref.valueList;
        ClassDeclT c = nav.asDecl(ref.type);
        if(c!=null && reader.getClassAnnotation(XmlRegistry.class,c,null/*TODO: is this right?*/)!=null) {
            return null;    // TODO: is this correct?
        } else
            return getTypeInfo(ref.type);
    }

    /**
     * Returns all the {@link ClassInfo}s known to this set.
     */
    public Map<ClassDeclT,? extends ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> beans() {
        return beansView;
    }

    public Map<TypeT, ? extends BuiltinLeafInfo<TypeT,ClassDeclT>> builtins() {
        return builtins;
    }

    public Map<ClassDeclT, ? extends EnumLeafInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> enums() {
        return enums;
    }

    public Map<? extends TypeT, ? extends ArrayInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> arrays() {
        return arrays;
    }

    /**
     * Returns a {@link ClassInfo} for the given bean.
     *
     * <p>
     * This method is almost like refinement of {@link #getTypeInfo(Object)} except
     * our ClassDeclT cannot derive from TypeT.
     *
     * @return
     *      null if the specified type is not bound by JAXB or otherwise
     *      unknown to this set.
     */
    public NonElement<TypeT,ClassDeclT> getClassInfo( ClassDeclT type ) {
        LeafInfo<TypeT,ClassDeclT> l = builtins.get(nav.use(type));
        if(l!=null)     return l;

        l = enums.get(type);
        if(l!=null)     return l;

        return beans.get(type);
    }

    public ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> getElementInfo( ClassDeclT scope, QName name ) {
        while(scope!=null) {
            Map<QName,ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> m = elementMappings.get(scope);
            if(m!=null) {
                ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> r = m.get(name);
                if(r!=null)     return r;
            }
            scope = nav.getSuperClass(scope);
        }
        return elementMappings.get(null).get(name);
    }

    /**
     * @param builder
     *      used for reporting errors.
     */
    public final void add( ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> ei, ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> builder ) {
        ClassDeclT scope = null;
        if(ei.getScope()!=null)
            scope = ei.getScope().getClazz();

        Map<QName,ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> m = elementMappings.get(scope);
        if(m==null)
            elementMappings.put(scope,m=new LinkedHashMap<QName,ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>());

        ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> existing = m.put(ei.getElementName(),ei);

        if(existing!=null) {
            QName en = ei.getElementName();
            builder.reportError(
                new IllegalAnnotationException(
                    Messages.CONFLICTING_XML_ELEMENT_MAPPING.format(en.getNamespaceURI(),en.getLocalPart()),
                    ei, existing ));
        }
    }

    public Map<QName,? extends ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> getElementMappings( ClassDeclT scope ) {
        return elementMappings.get(scope);
    }

    public Iterable<? extends ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> getAllElements() {
        return allElements;
    }

    /**
     * Dumps this model into XML.
     *
     * For debug only.
     *
     * TODO: not sure if this actually works. We don't really know what are TypeT,ClassDeclT.
     */
    public void dump( Result out ) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(this.getClass());
        Marshaller m = context.createMarshaller();
        m.marshal(this,out);
    }
}

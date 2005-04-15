package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.xml.bind.annotation.XmlAnyAttribute;
import com.sun.xml.bind.annotation.XmlAnyElement;
import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * A part of the {@link ClassInfo} that doesn't depend on a particular
 * reflection library.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends TypeInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ClassInfo<TypeT,ClassDeclT>, Element<TypeT,ClassDeclT> {

    protected final ClassDeclT clazz;

    /**
     * @see #getElementName()
     */
    private final QName elementName;

    /**
     * @see #getTypeName()
     */
    private final QName typeName;

    /**
     * Lazily created.
     *
     * @see #getProperties()
     */
    private List<PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> properties;

    /**
     * The property order.
     *
     * null if unordered. {@link #DEFAULT_ORDER} if ordered but the order is defaulted
     *
     * @see #isOrdered()
     */
    private final String[] propOrder;

    private final ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> baseClass;

    /**
     * If this class has a declared (not inherited) attribute wildcard,  keep the reference
     * to it.
     *
     * This parameter is initialized at the construction time and never change.
     */
    protected /*final*/ PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> attributeWildcard;

    ClassInfoImpl(ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> builder, Locatable upstream, ClassDeclT clazz) {
        super(builder,upstream);
        this.clazz = clazz;
        assert clazz!=null;

        // the class must have the default constructor
        if(!nav().hasDefaultConstructor(clazz))
            builder.reportError(new IllegalAnnotationException(
                Messages.NO_DEFAULT_CONSTRUCTOR.format(nav().getClassName(clazz)), this ));

        // compute the element name
        XmlRootElement e = reader().getClassAnnotation(XmlRootElement.class,clazz,this);
        if(e==null)
            elementName = null;
        else {
            elementName = parseElementName(e);
        }

        // compute the type name
        XmlType t = reader().getClassAnnotation(XmlType.class,clazz,this);
        if(t!=null) {
            typeName = parseTypeName(clazz,t);
            String[] propOrder = t.propOrder();
            if(propOrder.length==0)
                this.propOrder = null;   // unordered
            else {
                if(propOrder[0].length()==0)
                    this.propOrder = DEFAULT_ORDER;
                else
                    this.propOrder = propOrder;
            }
        } else {
            if(elementName!=null)
                typeName = elementName;
            else
                typeName = parseTypeName(clazz,null);
            propOrder = DEFAULT_ORDER;
        }

        // compute the base class
        ClassDeclT s = nav().getSuperClass(clazz);
        if(s==null || s==nav().asDecl(Object.class))
            baseClass = null;
        else
            baseClass = (ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>) builder.getClassInfo(s,this);
    }

    public ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> getBaseClass() {
        return baseClass;
    }

    /**
     * {@inheritDoc}
     *
     * The substitution hierarchy is the same as the inheritance hierarchy.
     */
    public final Element<TypeT,ClassDeclT> getSubstitutionHead() {
        ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> c = baseClass;
        while(c!=null && !c.isElement())
            c = c.getBaseClass();
        return c;
    }

    public final ClassDeclT getClazz() {
        return clazz;
    }

    public ClassInfoImpl getScope() {
        TODO.checkSpec("Do we have scope for classes?");
        return null;
    }

    public final TypeT getType() {
        return nav().use(clazz);
    }

    public final String getName() {
        return nav().getClassName(clazz);
    }

    public <A extends Annotation> A readAnnotation(Class<A> a) throws IllegalAnnotationException {
        return reader().getClassAnnotation(a,clazz,this);
    }

    public Element<TypeT,ClassDeclT> asElement() {
        if(isElement())
            return this;
        else
            return null;
    }

    public List<? extends PropertyInfo<TypeT,ClassDeclT>> getProperties() {
        TODO.prototype();       // TODO: implement this method properly later

        if(properties!=null)    return properties;

        // check the access type first
        AccessType at = getAccessType();

        properties = new ArrayList<PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>();

        // find properties from fields
        for( FieldT f : nav().getDeclaredFields(clazz) ) {
            if( nav().isStaticField(f) )
                continue;
            if(at==AccessType.FIELD || hasAnnotation(f))
                addProperty(adaptIfNecessary(createFieldSeed(f)));
        }

        findGetterSetterProperties(at);

        if(propOrder==DEFAULT_ORDER || propOrder==null) {
            // TODO: sort them in the default order
        } else {
            //sort them as specified
            PropertySorter sorter = new PropertySorter();
            for (PropertyInfoImpl p : properties)
                sorter.checkedGet(p);   // have it check for errors
            Collections.sort(properties,sorter);
            sorter.checkUnusedProperties();
        }

        return properties;
    }

    /**
     * Computes the {@link AccessType} on this class by looking at {@link XmlAccessorType}
     * annotations.
     */
    private AccessType getAccessType() {
        TODO.checkSpec();
        AccessType at = AccessType.PROPERTY;
        XmlAccessorType xat = reader().getClassAnnotation(XmlAccessorType.class,clazz,this);
        if(xat==null)
            // defaults to the package level
            xat = reader().getPackageAnnotation(XmlAccessorType.class,clazz,this);
        if(xat!=null)
            at = xat.value();
        return at;
    }

    /**
     * Returns true if the given field has a JAXB annotation
     */
    private boolean hasAnnotation(FieldT f) {
        for (Class<? extends Annotation> a : jaxbAnnotations)
            if(reader().hasFieldAnnotation(a,f))
                return true;
        return false;
    }

    /**
     * Returns true if the given methods have a JAXB annotation
     */
    private boolean hasAnnotation(MethodT getter, MethodT setter) {
        for (Class<? extends Annotation> a : jaxbAnnotations)
            if (reader().hasMethodAnnotation(a,getter) || reader().hasMethodAnnotation(a,setter))
                return true;
        return false;
    }

    /**
     * Compares orders among {@link PropertyInfoImpl} according to {@link ClassInfoImpl#propOrder}.
     *
     * <p>
     * extends {@link HashMap} to save memory.
     */
    private final class PropertySorter extends HashMap<String,Integer> implements Comparator<PropertyInfoImpl> {
        /**
         * Mark property names that are used, so that we can report unused property names in the propOrder array.
         */
        boolean[] used = new boolean[propOrder.length];

        PropertySorter() {
            super(propOrder.length);
            for( String name : propOrder )
                put(name,size());
        }

        public int compare(PropertyInfoImpl o1, PropertyInfoImpl o2) {
            int lhs = checkedGet(o1);
            int rhs = checkedGet(o2);

            return lhs-rhs;
        }

        private int checkedGet(PropertyInfoImpl p) {
            Integer i = get(p.getName());
            if(i==null) {
                // missing
                if((p.kind()==PropertyKind.ELEMENT || p.kind()==PropertyKind.REFERENCE))
                    builder.reportError(new IllegalAnnotationException(
                        Messages.PROPERTY_MISSING_FROM_ORDER.format(p.getName()),p));

                // give it an order to recover from an error
                i = size();
                put(p.getName(),i);
            }

            // mark the used field
            int ii = i;
            if(ii<used.length)
                used[ii] = true;

            return i;
        }

        /**
         * Report errors for unused propOrder entries.
         */
        public void checkUnusedProperties() {
            for( int i=0; i<used.length; i++ )
                if(!used[i]) {
                    String unusedName = propOrder[i];
                    builder.reportError(new IllegalAnnotationException(
                        Messages.PROPERTY_ORDER_CONTAINS_UNUSED_ENTRY.format(unusedName),ClassInfoImpl.this));
                }
        }
    }

    private PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> adaptIfNecessary(PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed) {
        XmlJavaTypeAdapter adapter = seed.readAnnotation(XmlJavaTypeAdapter.class);
        
        if(adapter==null)   return seed;

        return createAdaptedSeed(seed,new Adapter(adapter,reader(),nav()));
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }


    private static final <T> List<T> makeSet( T... args ) {
        List<T> l = new ArrayList<T>();
        for( T arg : args )
            if(arg!=null)   l.add(arg);
        return l;
    }

    private static final class ConflictException extends Exception {
        final Annotation one;

        public ConflictException(Annotation one) {
            this.one = one;
        }
    }

    /**
     * Called only from {@link #getProperties()}.
     */
    private void addProperty( PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed ) {
        XmlAttribute a = seed.readAnnotation(XmlAttribute.class);
        XmlValue v = seed.readAnnotation(XmlValue.class);
        XmlElement e = seed.readAnnotation(XmlElement.class);
        XmlElementRef r1 = seed.readAnnotation(XmlElementRef.class);
        XmlElementRefs r2 = seed.readAnnotation(XmlElementRefs.class);
        XmlTransient t = seed.readAnnotation(XmlTransient.class);
        XmlAnyAttribute aa = seed.readAnnotation(XmlAnyAttribute.class);

        XmlAnyElement xae = seed.readAnnotation(XmlAnyElement.class);
        //TODO: xae and other things

        // these are mutually exclusive annotations
        int count = (a!=null?1:0)+(v!=null?1:0)+(e!=null?1:0)+(r1!=null?1:0)+(r2!=null?1:0)+(t!=null?1:0)+(aa!=null?1:0);

        try {
            if(count>1) {
                List<Annotation> err = makeSet(a,v,e,r1,r2,t,aa);
                throw new ConflictException(err.get(0));
            }

            if(t!=null) {
                // a transient property
                return;
            }

            if(aa!=null) {
                // this property is attribute wildcard
                if(attributeWildcard!=null) {
                    builder.reportError(new IllegalAnnotationException(
                        Messages.TWO_ATTRIBUTE_WILDCARDS.format(
                            nav().getClassName(getClazz())),aa,attributeWildcard));
                    return; // recover by ignore
                }
                attributeWildcard = seed;

                if(inheritsAttributeWildcard()) {
                    builder.reportError(new IllegalAnnotationException(
                        Messages.SUPER_CLASS_HAS_WILDCARD.format(),
                            aa,getInheritedAttributeWildcard()));
                    return;
                }

                // check the signature and make sure it's assignable to Map
                if(!nav().isSubClassOf(seed.getRawType(),nav().ref(Map.class))) {
                    builder.reportError(new IllegalAnnotationException(
                        Messages.INVALID_ATTRIBUTE_WILDCARD_TYPE.format(nav().getTypeName(seed.getRawType())),
                            aa,getInheritedAttributeWildcard()));
                    return;
                }


                return;
            }

            //if no annotation present defaults to Element
            // if XMLElement annotation present also then Kind is Kind.ELEMENT
            if(v!=null) {
                properties.add(createValueProperty(seed));
            } else
            if(a!=null) {
                properties.add(createAttributeProperty(seed));
            } else
            if(r1!=null || r2!=null || xae!=null) {
                properties.add(createReferenceProperty(seed));
            } else
                properties.add(createElementProperty(seed));
        } catch( ConflictException x ) {
            // report a conflicting annotation
            List<Annotation> err = makeSet(a,v,e,r1,r2,t,aa);
            err.remove(x.one);
            assert !err.isEmpty();

            builder.reportError(new IllegalAnnotationException(
                Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(
                    nav().getClassName(getClazz())+'#'+seed.getName(),
                    x.one.annotationType(), err.get(1).annotationType()),
                x.one, err.get(1) ));

            // recover by ignoring this property
        }
    }

    protected ReferencePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> createReferenceProperty(PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed) {
        return new ReferencePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,seed);
    }

    protected AttributePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> createAttributeProperty(PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed) {
        return new AttributePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,seed);
    }

    protected ValuePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> createValueProperty(PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed) {
        return new ValuePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,seed);
    }

    protected ElementPropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> createElementProperty(PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed) {
        return new ElementPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,seed);
    }


    /**
     * Adds properties that consists of accessors.
     */
    private void findGetterSetterProperties(AccessType at) {
        TODO.checkSpec();   // TODO: I don't think the spec describes how properties are found

        // in the first step we accumulate getters and setters
        // into this map keyed by the property name.
        Map<String,MethodT> getters = new LinkedHashMap<String,MethodT>();
        Map<String,MethodT> setters = new LinkedHashMap<String,MethodT>();

        Collection<? extends MethodT> methods = nav().getDeclaredMethods(clazz);
        for( MethodT method : methods ) {
            String name = nav().getMethodName(method);
            int arity = nav().getMethodParameters(method).length;

            if(nav().isStaticMethod(method))
                continue;

            // don't look at XmlTransient. We'll deal with that later.

            // TODO: if methods are overriding properties of a base class, ignore.

            // is this a get method?
            String propName = getPropertyNameFromGetMethod(name);
            if(propName!=null) {
                if(arity==0) {
                    getters.put(propName,method);
                }
                // TODO: do we support indexed property?
            }

            // is this a set method?
            propName = getPropertyNameFromSetMethod(name);
            if(propName!=null) {
                if(arity==1) {
                    // TODO: we should check collisions like setFoo(int) and setFoo(String)
                    setters.put(propName,method);
                }
                // TODO: do we support indexed property?
            }
        }

        Set<String> complete = new TreeSet<String>();
        complete.addAll(getters.keySet());
        complete.retainAll(setters.keySet());

        // then look for read/write properties.
        for (String name : complete) {
            MethodT getter = getters.get(name);
            MethodT setter = setters.get(name);

            // make sure that the type is consistent
            if (!nav().getReturnType(getter).equals(nav().getMethodParameters(setter)[0]))
                continue;

            // recognize getters/setters only when they have annotations

            // this looks OK
            if (at==AccessType.PROPERTY || hasAnnotation(getter, setter))
                addProperty(adaptIfNecessary(createAccessorSeed(getter, setter)));
        }
        // done with complete pairs
        getters.keySet().removeAll(complete);
        setters.keySet().removeAll(complete);

        // TODO: allow incomplete getter/setter to participate.
        // especialyl with JAXB annotations.
        // e.g., List getFoo()
        //
        // or sometimes users just want to marshal it and may not have a setter.

        // TODO: think about
        // class Foo {
        //   int getFoo();
        // }
        // class Bar extends Foo {
        //   void setFoo(int x);
        // }
        // and how it will be XML-ized.
    }


    private static final Class<? extends Annotation>[] jaxbAnnotations = new Class[]{
        XmlElement.class, XmlAttribute.class, XmlValue.class, XmlElementRef.class,
        XmlElements.class, XmlElementRefs.class, XmlElementWrapper.class,
        XmlJavaTypeAdapter.class
    };

    /**
     * Returns "Foo" from "getFoo" or "isFoo".
     *
     * @return null
     *      if the method name doesn't look like a getter.
     */
    private static String getPropertyNameFromGetMethod(String name) {
        if(name.startsWith("get") && name.length()>3)
            return name.substring(3);
        if(name.startsWith("is") && name.length()>2)
            return name.substring(2);
        return null;
    }

    /**
     * Returns "Foo" from "setFoo".
     *
     * @return null
     *      if the method name doesn't look like a setter.
     */
    private static String getPropertyNameFromSetMethod(String name) {
        if(name.startsWith("set") && name.length()>3)
            return name.substring(3);
        return null;
    }

    /**
     * Creates a new {@link FieldPropertySeed} object.
     *
     * <p>
     * Derived class can override this method to create a sub-class.
     */
    protected PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> createFieldSeed(FieldT f) {
        return new FieldPropertySeed<TypeT,ClassDeclT,FieldT,MethodT>(this, f);
    }

    /**
     * Creates a new {@link GetterSetterPropertySeed} object.
     */
    protected PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> createAccessorSeed(MethodT getter, MethodT setter) {
        return new GetterSetterPropertySeed<TypeT,ClassDeclT,FieldT,MethodT>(this, getter,setter);
    }

    protected PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> createAdaptedSeed(PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed, Adapter a) {
        return new AdaptedPropertySeed<TypeT,ClassDeclT,FieldT,MethodT>(seed,a);
    }

    /**
     * Parses an {@link XmlRootElement} annotation on a class
     * and determine the name.
     */
    final QName parseElementName(XmlRootElement e) {
        String local = e.name();
        if(local.equals("##default")) {
            // if defaulted...
            TODO.prototype();   // TODO
            local = NameConverter.standard.toVariableName(nav().getClassShortName(clazz));
        }
        String nsUri = e.namespace();
        if(nsUri.equals("##default")) {
            // if defaulted ...
            XmlSchema xs = reader().getPackageAnnotation(XmlSchema.class,clazz,this);
            if(xs!=null)
                nsUri = xs.namespace();
            else {
                nsUri = builder.defaultNsUri;
            }
        }

        return new QName(nsUri.intern(),local.intern());
    }

    public final boolean isElement() {
        return elementName!=null;
    }

    public boolean isAbstract() {
        return nav().isAbstract(clazz);
    }

    public boolean isOrdered() {
        return propOrder!=null;
    }

    public final boolean hasAttributeWildcard() {
        return declaresAttributeWildcard() || inheritsAttributeWildcard();
    }

    public final boolean inheritsAttributeWildcard() {
        return getInheritedAttributeWildcard()!=null;
    }

    public final boolean declaresAttributeWildcard() {
        return attributeWildcard!=null;
    }

    /**
     * Gets the {@link PropertySeed} object for the inherited attribute wildcard.
     */
    private PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> getInheritedAttributeWildcard() {
        for( ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> c=baseClass; c!=null; c=c.baseClass )
            if(c.attributeWildcard!=null)
                return c.attributeWildcard;
        return null;
    }

    public final QName getElementName() {
        return elementName;
    }

    public final QName getTypeName() {
        return typeName;
    }

    /**
     * Called after all the {@link TypeInfo}s are collected into the {@link #owner}.
     */
    @Override
    /*package*/ void link() {
        getProperties();    // make sure properties!=null
        for( PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> p : properties )
            p.link();
        super.link();
    }

    public Location getLocation() {
        return nav().getClassLocation(clazz);
    }

    private static final String[] DEFAULT_ORDER = new String[0];
}

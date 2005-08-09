package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
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
import javax.xml.bind.annotation.AccessorOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.xml.bind.annotation.XmlLocation;
import com.sun.xml.bind.v2.FinalArrayList;
import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.MethodLocatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;

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
    private FinalArrayList<PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> properties;

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

    public <A extends Annotation> A readAnnotation(Class<A> a) {
        return reader().getClassAnnotation(a,clazz,this);
    }

    public Element<TypeT,ClassDeclT> asElement() {
        if(isElement())
            return this;
        else
            return null;
    }

    public List<? extends PropertyInfo<TypeT,ClassDeclT>> getProperties() {
        if(properties!=null)    return properties;

        // check the access type first
        AccessType at = getAccessType();

        properties = new FinalArrayList<PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>();

        // find properties from fields
        for( FieldT f : nav().getDeclaredFields(clazz) ) {
            if( nav().isStaticField(f) )
                continue;
            if(at==AccessType.FIELD
            ||(at==AccessType.PUBLIC_MEMBER && nav().isPublicField(f))
            || hasFieldAnnotation(f))
                addProperty(adaptIfNecessary(createFieldSeed(f)));
            else
                checkFieldXmlLocation(f);
        }

        findGetterSetterProperties(at);

        if(propOrder==DEFAULT_ORDER || propOrder==null) {
            AccessorOrder ao = getAccessorOrder();
            if(ao==AccessorOrder.ALPHABETICAL)
                Collections.sort(properties);
        } else {
            //sort them as specified
            PropertySorter sorter = new PropertySorter();
            for (PropertyInfoImpl p : properties)
                sorter.checkedGet(p);   // have it check for errors
            Collections.sort(properties,sorter);
            sorter.checkUnusedProperties();
        }

        {// additional error checks
            PropertyInfoImpl vp=null; // existing value property
            PropertyInfoImpl ep=null; // existing element property

            for (PropertyInfoImpl p : properties) {
                switch(p.kind()) {
                case ELEMENT:
                case REFERENCE:
                case MAP:
                    ep = p;
                    break;
                case VALUE:
                    if(vp!=null) {
                        // can't have multiple value properties.
                        builder.reportError(new IllegalAnnotationException(
                            Messages.MULTIPLE_VALUE_PROPERTY.format(),
                            vp, p ));
                    }
                    if(getBaseClass()!=null) {
                        builder.reportError(new IllegalAnnotationException(
                            Messages.XMLVALUE_IN_DERIVED_TYPE.format(), p ));
                    }
                    vp = p;
                    break;
                case ATTRIBUTE:
                    break;  // noop
                default:
                    assert false;
                }
            }

            if(ep!=null && vp!=null) {
                // can't have element and value property at the same time
                builder.reportError(new IllegalAnnotationException(
                    Messages.ELEMENT_AND_VALUE_PROPERTY.format(),
                    vp, ep
                ));
            }
        }

        return properties;
    }

    /**
     * This hook is used by {@link RuntimeClassInfoImpl} to look for {@link XmlLocation}.
     */
    protected void checkFieldXmlLocation(FieldT f) {
    }

    /**
     * Gets an annotation that are allowed on both class and type.
     */
    private <T extends Annotation> T getClassOrPackageAnnotation(Class<T> type) {
        T t = reader().getClassAnnotation(type,clazz,this);
        if(t!=null)
            return t;
        // defaults to the package level
        return reader().getPackageAnnotation(type,clazz,this);
    }

    /**
     * Computes the {@link AccessType} on this class by looking at {@link XmlAccessorType}
     * annotations.
     */
    private AccessType getAccessType() {
        XmlAccessorType xat = getClassOrPackageAnnotation(XmlAccessorType.class);
        if(xat!=null)
            return xat.value();
        else
            return AccessType.PUBLIC_MEMBER;
    }

    /**
     * Gets the accessor order for this class by consulting {@link XmlAccessorOrder}.
     */
    private AccessorOrder getAccessorOrder() {
        XmlAccessorOrder xao = getClassOrPackageAnnotation(XmlAccessorOrder.class);
        if(xao!=null)
            return xao.value();
        else
            return AccessorOrder.UNDEFINED;
    }

    /**
     * Returns true if the given field has a JAXB annotation
     */
    private boolean hasFieldAnnotation(FieldT f) {
        for (Class<? extends Annotation> a : jaxbAnnotations)
            if(reader().hasFieldAnnotation(a,f))
                return true;
        return false;
    }

    private boolean hasMethodAnnotation(MethodT m) {
        for (Class<? extends Annotation> a : jaxbAnnotations)
            if(reader().hasMethodAnnotation(a,m))
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
        PropertyInfoImpl[] used = new PropertyInfoImpl[propOrder.length];

        /**
         * If any name collides, it will be added to this set.
         * This is used to avoid repeating the same error message.
         */
        private Set<String> collidedNames;

        PropertySorter() {
            super(propOrder.length);
            for( String name : propOrder )
                if(put(name,size())!=null) {
                    // two properties with the same name
                    builder.reportError(new IllegalAnnotationException(
                        Messages.DUPLICATE_ENTRY_IN_PROP_ORDER.format(name),ClassInfoImpl.this));
                }
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
                if((p.kind().isOrdered))
                    builder.reportError(new IllegalAnnotationException(
                        Messages.PROPERTY_MISSING_FROM_ORDER.format(p.getName()),p));

                // give it an order to recover from an error
                i = size();
                put(p.getName(),i);
            }

            // mark the used field
            int ii = i;
            if(ii<used.length) {
                if(used[ii]!=null && used[ii]!=p) {
                    if(collidedNames==null) collidedNames = new HashSet<String>();

                    if(collidedNames.add(p.getName()))
                        // report the error only on the first time
                        builder.reportError(new IllegalAnnotationException(
                            Messages.DUPLICATE_PROPERTIES.format(p.getName()),p,used[ii]));
                }
                used[ii] = p;
            }

            return i;
        }

        /**
         * Report errors for unused propOrder entries.
         */
        public void checkUnusedProperties() {
            for( int i=0; i<used.length; i++ )
                if(used[i]==null) {
                    String unusedName = propOrder[i];
                    builder.reportError(new IllegalAnnotationException(
                        Messages.PROPERTY_ORDER_CONTAINS_UNUSED_ENTRY.format(unusedName),ClassInfoImpl.this));
                }
        }
    }

    private PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> adaptIfNecessary(PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed) {
        XmlJavaTypeAdapter adapter = seed.readAnnotation(XmlJavaTypeAdapter.class);
        if(adapter!=null)
            return createAdaptedSeed(seed,new Adapter<TypeT,ClassDeclT>(adapter,reader(),nav()));

        // this is actually incorrect because it's OK to have an adapter and the attachment
        // at the same time.

        XmlAttachmentRef xsa = seed.readAnnotation(XmlAttachmentRef.class);
        if(xsa!=null)
            return createAdaptedSeed(seed,
                new Adapter<TypeT,ClassDeclT>(owner.nav.asDecl(SwaRefAdapter.class),owner.nav));

        return seed;
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }


    private static final <T> List<T> makeSet( T... args ) {
        List<T> l = new FinalArrayList<T>();
        for( T arg : args )
            if(arg!=null)   l.add(arg);
        return l;
    }

    private static final class ConflictException extends Exception {
        final List<Annotation> annotations;

        public ConflictException(List<Annotation> one) {
            this.annotations = one;
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
                throw new ConflictException(err);
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

            if(v!=null) {
                properties.add(createValueProperty(seed));
            } else
            if(a!=null) {
                properties.add(createAttributeProperty(seed));
            } else
            if(r1!=null || r2!=null || xae!=null) {
                properties.add(createReferenceProperty(seed));
            } else {
                // either an element property or a map property.
                // sniff the signature and then decide.
                // UGLY: the presence of XmlJavaTypeAdapter makes it an element property. ARGH.
                if(nav().isSubClassOf( seed.getRawType(), nav().ref(Map.class) )
                && !seed.hasAnnotation(XmlJavaTypeAdapter.class))
                    properties.add(createMapProperty(seed));
                else
                    properties.add(createElementProperty(seed));
            }
        } catch( ConflictException x ) {
            // report a conflicting annotation
            List<Annotation> err = x.annotations;

            builder.reportError(new IllegalAnnotationException(
                Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(
                    nav().getClassName(getClazz())+'#'+seed.getName(),
                    err.get(0).annotationType().getName(), err.get(1).annotationType().getName()),
                    err.get(0), err.get(1) ));

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

    protected MapPropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> createMapProperty(PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed) {
        return new MapPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,seed);
    }


    /**
     * Adds properties that consists of accessors.
     */
    private void findGetterSetterProperties(AccessType at) {
        TODO.checkSpec();   // TODO: I don't think the spec describes how properties are found

        // in the first step we accumulate getters and setters
        // into this map keyed by the property name.
        // TODO: allocating three maps seem to be redundant
        Map<String,MethodT> getters = new LinkedHashMap<String,MethodT>();
        Map<String,MethodT> setters = new LinkedHashMap<String,MethodT>();

        Collection<? extends MethodT> methods = nav().getDeclaredMethods(clazz);
        for( MethodT method : methods ) {
            boolean used = false;   // if this method is added to getters or setters
            String name = nav().getMethodName(method);
            int arity = nav().getMethodParameters(method).length;

            if(nav().isStaticMethod(method)) {
                ensureNoAnnotation(method);
                continue;
            }

            // don't look at XmlTransient. We'll deal with that later.

            // TODO: if methods are overriding properties of a base class, ignore.

            // is this a get method?
            String propName = getPropertyNameFromGetMethod(name);
            if(propName!=null) {
                if(arity==0) {
                    getters.put(propName,method);
                    used = true;
                }
                // TODO: do we support indexed property?
            }

            // is this a set method?
            propName = getPropertyNameFromSetMethod(name);
            if(propName!=null) {
                if(arity==1) {
                    // TODO: we should check collisions like setFoo(int) and setFoo(String)
                    setters.put(propName,method);
                    used = true;
                }
                // TODO: do we support indexed property?
            }

            if(!used)
                ensureNoAnnotation(method);
        }

        // compute the intersection
        Set<String> complete = new TreeSet<String>();
        complete.addAll(getters.keySet());
        complete.retainAll(setters.keySet());

        resurrect(getters, complete);
        resurrect(setters, complete);

        // then look for read/write properties.
        for (String name : complete) {
            MethodT getter = getters.get(name);
            MethodT setter = setters.get(name);

            boolean getterHasAnnotation = getter!=null && hasMethodAnnotation(getter);
            boolean setterHasAnnotation = setter!=null && hasMethodAnnotation(setter);

            if (at==AccessType.PROPERTY
            || (at==AccessType.PUBLIC_MEMBER && (getter==null || nav().isPublicMethod(getter)) && (setter==null || nav().isPublicMethod(setter)))
            || getterHasAnnotation || setterHasAnnotation) {
                // make sure that the type is consistent
                if(getter!=null && setter!=null
                && !nav().getReturnType(getter).equals(nav().getMethodParameters(setter)[0])) {
                    // inconsistent
                    builder.reportError(new IllegalAnnotationException(
                        Messages.GETTER_SETTER_INCOMPATIBLE_TYPE.format(
                            nav().getTypeName(nav().getReturnType(getter)),
                            nav().getTypeName(nav().getMethodParameters(setter)[0])
                        ),
                        new MethodLocatable<MethodT>( this, getter, nav()),
                        new MethodLocatable<MethodT>( this, setter, nav())));
                    continue;
                }

                addProperty(adaptIfNecessary(createAccessorSeed(getter, setter)));
            }
        }
        // done with complete pairs
        getters.keySet().removeAll(complete);
        setters.keySet().removeAll(complete);

        // TODO: think about
        // class Foo {
        //   int getFoo();
        // }
        // class Bar extends Foo {
        //   void setFoo(int x);
        // }
        // and how it will be XML-ized.
    }

    /**
     * If the method has an explicit annotation, allow it to participate
     * to the processing even if it lacks the setter or the getter.
     */
    private void resurrect(Map<String, MethodT> methods, Set<String> complete) {
        for (Map.Entry<String, MethodT> e : methods.entrySet()) {
            if(complete.contains(e.getKey()))
                continue;
            if(hasMethodAnnotation(e.getValue()))
                complete.add(e.getKey());
        }
    }

    /**
     * Makes sure that the method doesn't have any annotation, if it does,
     * report it as an error
     */
    private void ensureNoAnnotation(MethodT method) {
        for (Class<? extends Annotation> a : jaxbAnnotations)
            if(reader().hasMethodAnnotation(a,method)) {
                builder.reportError(new IllegalAnnotationException(
                    Messages.ANNOTATION_ON_WRONG_METHOD.format(),
                    reader().getMethodAnnotation(a,method,this)));
            }
    }


    private static final Class<? extends Annotation>[] jaxbAnnotations = new Class[]{
        XmlElement.class, XmlAttribute.class, XmlValue.class, XmlElementRef.class,
        XmlElements.class, XmlElementRefs.class, XmlElementWrapper.class,
        XmlJavaTypeAdapter.class, XmlAnyElement.class
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

    public boolean isFinal() {
        return nav().isFinal(clazz);
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

    public final boolean isSimpleType() {
        for (PropertyInfo p : getProperties()) {
            if(p.kind()!=PropertyKind.VALUE)
                return false;
        }
        return true;
    }

    /**
     * Called after all the {@link TypeInfo}s are collected into the {@link #owner}.
     */
    @Override
    /*package*/ void link() {
        getProperties();    // make sure properties!=null

        Map<String,PropertyInfoImpl> names = new HashMap<String,PropertyInfoImpl>();
        for( PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> p : properties ) {
            p.link();
            PropertyInfoImpl old = names.put(p.getName(),p);
            if(old!=null) {
                builder.reportError(new IllegalAnnotationException(
                    Messages.PROPERTY_COLLISION.format(p.getName()),
                    p, old ));
            }
        }
        super.link();
    }

    public Location getLocation() {
        return nav().getClassLocation(clazz);
    }

    private static final String[] DEFAULT_ORDER = new String[0];
}

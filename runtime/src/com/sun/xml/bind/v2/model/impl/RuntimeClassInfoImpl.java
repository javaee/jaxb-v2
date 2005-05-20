package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.annotation.XmlLocation;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class RuntimeClassInfoImpl extends ClassInfoImpl<Type,Class,Field,Method>
        implements RuntimeClassInfo, RuntimeElement {

    protected final ReflectionNavigator nav;

    /**
     * If this class has a property annotated with {@link XmlLocation},
     * this field will get the accessor for it.
     *
     * TODO: support method based XmlLocation
     */
    private Accessor<?,Locator> xmlLocationAccessor;

    public RuntimeClassInfoImpl(RuntimeModelBuilder modelBuilder, Locatable upstream, Class clazz) {
        super(modelBuilder, upstream, clazz);
        this.nav = modelBuilder.getNavigator();
    }

    public final RuntimeClassInfoImpl getBaseClass() {
        return (RuntimeClassInfoImpl)super.getBaseClass();
    }

    @Override
    protected ReferencePropertyInfoImpl createReferenceProperty(PropertySeed<Type,Class,Field,Method> seed) {
        return new RuntimeReferencePropertyInfoImpl(this,seed);
    }

    @Override
    protected AttributePropertyInfoImpl createAttributeProperty(PropertySeed<Type,Class,Field,Method> seed) {
        return new RuntimeAttributePropertyInfoImpl(this,seed);
    }

    @Override
    protected ValuePropertyInfoImpl createValueProperty(PropertySeed<Type,Class,Field,Method> seed) {
        return new RuntimeValuePropertyInfoImpl(this,seed);
    }

    @Override
    protected ElementPropertyInfoImpl createElementProperty(PropertySeed<Type,Class,Field,Method> seed) {
        return new RuntimeElementPropertyInfoImpl(this,seed);
    }


    @Override
    public List<? extends RuntimePropertyInfo> getProperties() {
        return (List<? extends RuntimePropertyInfo>)super.getProperties();
    }

    public void link() {
        super.link();
        getTransducer();    // populate the transducer
    }

    private Accessor<?,Map<QName,Object>> attributeWildcardAccessor;

    public Accessor<?,Map<QName,Object>> getAttributeWildcard() {
        for( RuntimeClassInfoImpl c=this; c!=null; c=c.getBaseClass() ) {
            if(c.attributeWildcard!=null) {
                if(c.attributeWildcardAccessor==null)
                    c.attributeWildcardAccessor = c.createAttributeWildcardAccessor();
                return c.attributeWildcardAccessor;
            }
        }
        return null;
    }

    private boolean computedTransducer = false;
    private Transducer xducer = null;

    public Transducer getTransducer() {
        if(!computedTransducer) {
            computedTransducer = true;
            xducer = calcTransducer();
        }
        return xducer;
    }

    /**
     * Creates a transducer if this class is bound to a text in XML.
     */
    private Transducer calcTransducer() {
        RuntimeValuePropertyInfo valuep=null;
        for (RuntimeClassInfoImpl ci = this; ci != null; ci = ci.getBaseClass()) {
            for( RuntimePropertyInfo pi : ci.getProperties() )
                if(pi.kind()==PropertyKind.VALUE) {
                    valuep = (RuntimeValuePropertyInfo)pi;
                } else {
                    // this bean has something other than a value
                    return null;
                }
        }
        if(valuep==null)
            return null;

        return new TransducerImpl(getClazz(),TransducedAccessor.get(valuep));
    }

    /**
     * Creates
     */
    private Accessor<?,Map<QName,Object>> createAttributeWildcardAccessor() {
        assert attributeWildcard!=null;
        return ((RuntimePropertySeed)attributeWildcard).getAccessor();
    }

    @Override
    protected RuntimePropertySeed createFieldSeed(Field field) {
        return new RuntimePropertySeed(
            super.createFieldSeed(field),
            new Accessor.FieldReflection(field) );
    }

    @Override
    public RuntimePropertySeed createAccessorSeed(Method getter, Method setter) {
        return new RuntimePropertySeed(
            super.createAccessorSeed(getter,setter),
            new Accessor.GetterSetterReflection(getter,setter) );
    }

    @Override
    public RuntimePropertySeed createAdaptedSeed(PropertySeed<Type,Class,Field,Method> _seed,Adapter a) {
        RuntimePropertySeed seed = (RuntimePropertySeed) _seed;
        AdaptedPropertySeed<Type,Class,Field,Method> cp =
            (AdaptedPropertySeed<Type,Class,Field,Method>)super.createAdaptedSeed(seed.core,a);

        return new RuntimePropertySeed(cp,seed.getAccessor().adapt(
            Navigator.REFLECTION.erasure(cp.adapter.defaultType),
            cp.adapter.adapterType));
    }

    @Override
    protected void checkFieldXmlLocation(Field f) {
        if(reader().hasFieldAnnotation(XmlLocation.class,f))
            // TODO: check for XmlLocation signature
            // TODO: check a collision with the super class
            xmlLocationAccessor = new Accessor.FieldReflection<Object,Locator>(f);
    }

    public Accessor<?,Locator> getLocatorField() {
        return xmlLocationAccessor;
    }

    static final class RuntimePropertySeed implements PropertySeed<Type,Class,Field,Method> {
        /**
         * @see #getAccessor()
         */
        private final Accessor acc;

        private final PropertySeed<Type,Class,Field,Method> core;

        public RuntimePropertySeed(PropertySeed<Type,Class,Field,Method> core, Accessor acc) {
            this.core = core;
            this.acc = acc;
        }

        public String getName() {
            return core.getName();
        }

        public <A extends Annotation> A readAnnotation(Class<A> annotationType) {
            return core.readAnnotation(annotationType);
        }

        public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
            return core.hasAnnotation(annotationType);
        }

        public Type getRawType() {
            return core.getRawType();
        }

        public Location getLocation() {
            return core.getLocation();
        }

        public Locatable getUpstream() {
            return core.getUpstream();
        }

        public String generateSetValue(String $bean, String $var) {
            return core.generateSetValue($bean, $var);
        }

        public String generateGetValue(String $bean) {
            return core.generateGetValue($bean);
        }

        public Accessor getAccessor() {
            return acc;
        }
    }


    
    /**
     * {@link Transducer} implementation used when this class maps to PCDATA in XML.
     *
     * TODO: revisit the exception handling
     */
    private static final class TransducerImpl<BeanT> implements Transducer<BeanT> {
        private final TransducedAccessor<BeanT> xacc;
        private final Class<BeanT> ownerClass;

        public TransducerImpl(Class<BeanT> ownerClass,TransducedAccessor<BeanT> xacc) {
            this.xacc = xacc;
            this.ownerClass = ownerClass;
        }

        public boolean useNamespace() {
            return xacc.useNamespace();
        }

        public boolean isDefault() {
            return false;
        }

        public void declareNamespace(BeanT bean, XMLSerializer w) throws AccessorException {
            try {
                xacc.declareNamespace(bean,w);
            } catch (SAXException e) {
                throw new AccessorException(e);
            }
        }

        public CharSequence print(BeanT bean) throws AccessorException {
            try {
                return xacc.print(bean);
            } catch (SAXException e) {
                throw new AccessorException(e);
            }
        }

        public BeanT parse(CharSequence lexical) throws AccessorException, SAXException {
            UnmarshallingContext ctxt = UnmarshallingContext.getInstance();
            BeanT inst;
            if(ctxt!=null)
                inst = (BeanT)ctxt.createInstance(ownerClass);
            else
                // when this runs for parsing enum constants,
                // there's no UnmarshallingContext.
                inst = ClassFactory.create(ownerClass);

            xacc.parse(inst,lexical);
            return inst;
        }
    }
}

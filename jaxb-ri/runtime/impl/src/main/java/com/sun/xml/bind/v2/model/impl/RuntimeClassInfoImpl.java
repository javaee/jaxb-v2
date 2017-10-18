/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.bind.v2.model.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.istack.NotNull;
import com.sun.xml.bind.AccessorFactory;
import com.sun.xml.bind.AccessorFactoryImpl;
import com.sun.xml.bind.InternalAccessorFactory;
import com.sun.xml.bind.XmlAccessorFactory;
import com.sun.xml.bind.annotation.XmlLocation;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class RuntimeClassInfoImpl extends ClassInfoImpl<Type,Class,Field,Method>
        implements RuntimeClassInfo, RuntimeElement {

    /**
     * If this class has a property annotated with {@link XmlLocation},
     * this field will get the accessor for it.
     *
     * TODO: support method based XmlLocation
     */
    private Accessor<?,Locator> xmlLocationAccessor;

    private AccessorFactory accessorFactory;

    private boolean supressAccessorWarnings = false;

    public RuntimeClassInfoImpl(RuntimeModelBuilder modelBuilder, Locatable upstream, Class clazz) {
        super(modelBuilder, upstream, clazz);
        accessorFactory = createAccessorFactory(clazz);
    }

    protected AccessorFactory createAccessorFactory(Class clazz) {
        XmlAccessorFactory factoryAnn;
        AccessorFactory accFactory = null;

        // user providing class to be used.
        JAXBContextImpl context = ((RuntimeModelBuilder) builder).context;
        if (context!=null) {
            this.supressAccessorWarnings = context.supressAccessorWarnings;
            if (context.xmlAccessorFactorySupport) {
                factoryAnn = findXmlAccessorFactoryAnnotation(clazz);
                if (factoryAnn != null) {
                    try {
                        accFactory = factoryAnn.value().newInstance();
                    } catch (InstantiationException e) {
                        builder.reportError(new IllegalAnnotationException(
                                Messages.ACCESSORFACTORY_INSTANTIATION_EXCEPTION.format(
                                factoryAnn.getClass().getName(), nav().getClassName(clazz)), this));
                    } catch (IllegalAccessException e) {
                        builder.reportError(new IllegalAnnotationException(
                                Messages.ACCESSORFACTORY_ACCESS_EXCEPTION.format(
                                factoryAnn.getClass().getName(), nav().getClassName(clazz)),this));
                    }
                }
            }
        }


        // Fall back to local AccessorFactory when no
        // user not providing one or as error recovery.
        if (accFactory == null){
            accFactory = AccessorFactoryImpl.getInstance();
        }
        return accFactory;
    }

    protected XmlAccessorFactory findXmlAccessorFactoryAnnotation(Class clazz) {
        XmlAccessorFactory factoryAnn = reader().getClassAnnotation(XmlAccessorFactory.class,clazz,this);
        if (factoryAnn == null) {
            factoryAnn = reader().getPackageAnnotation(XmlAccessorFactory.class,clazz,this);
        }
        return factoryAnn;
    }


    public Method getFactoryMethod(){
        return super.getFactoryMethod();
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
    protected MapPropertyInfoImpl createMapProperty(PropertySeed<Type,Class,Field,Method> seed) {
        return new RuntimeMapPropertyInfoImpl(this,seed);
    }


    @Override
    public List<? extends RuntimePropertyInfo> getProperties() {
        return (List<? extends RuntimePropertyInfo>)super.getProperties();
    }

    @Override
    public RuntimePropertyInfo getProperty(String name) {
        return (RuntimePropertyInfo)super.getProperty(name);
    }


    public void link() {
        getTransducer();    // populate the transducer
        super.link();
    }

    private Accessor<?,Map<QName,String>> attributeWildcardAccessor;

    public <B> Accessor<B,Map<QName,String>> getAttributeWildcard() {
        for( RuntimeClassInfoImpl c=this; c!=null; c=c.getBaseClass() ) {
            if(c.attributeWildcard!=null) {
                if(c.attributeWildcardAccessor==null)
                    c.attributeWildcardAccessor = c.createAttributeWildcardAccessor();
                return (Accessor<B,Map<QName,String>>)c.attributeWildcardAccessor;
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
        if(hasAttributeWildcard())
            return null;        // has attribute wildcard. Can't be handled as a leaf
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
        if( !valuep.getTarget().isSimpleType() )
            return null;    // if there's an error, recover from it by returning null.
        
        return new TransducerImpl(getClazz(),TransducedAccessor.get(
                ((RuntimeModelBuilder)builder).context,valuep));
    }

    /**
     * Creates
     */
    private Accessor<?,Map<QName,String>> createAttributeWildcardAccessor() {
        assert attributeWildcard!=null;
        return ((RuntimePropertySeed)attributeWildcard).getAccessor();
    }

    @Override
    protected RuntimePropertySeed createFieldSeed(Field field) {
       final boolean readOnly = Modifier.isStatic(field.getModifiers());
        Accessor acc;
        try {
            if (supressAccessorWarnings) { 
                acc = ((InternalAccessorFactory)accessorFactory).createFieldAccessor(clazz, field, readOnly, supressAccessorWarnings);
            } else {
                acc = accessorFactory.createFieldAccessor(clazz, field, readOnly);
            }
        } catch(JAXBException e) {
            builder.reportError(new IllegalAnnotationException(
                    Messages.CUSTOM_ACCESSORFACTORY_FIELD_ERROR.format(
                    nav().getClassName(clazz), e.toString()), this ));
            acc = Accessor.getErrorInstance(); // error recovery
        }
        return new RuntimePropertySeed(super.createFieldSeed(field), acc );
    }

    @Override
    public RuntimePropertySeed createAccessorSeed(Method getter, Method setter) {
        Accessor acc;
        try {
            acc = accessorFactory.createPropertyAccessor(clazz, getter, setter);
        } catch(JAXBException e) {
            builder.reportError(new IllegalAnnotationException(
                Messages.CUSTOM_ACCESSORFACTORY_PROPERTY_ERROR.format(
                nav().getClassName(clazz), e.toString()), this ));
            acc = Accessor.getErrorInstance(); // error recovery
        }
        return new RuntimePropertySeed( super.createAccessorSeed(getter,setter),
          acc );
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

        public void declareNamespace(BeanT bean, XMLSerializer w) throws AccessorException {
            try {
                xacc.declareNamespace(bean,w);
            } catch (SAXException e) {
                throw new AccessorException(e);
            }
        }

        public @NotNull CharSequence print(BeanT o) throws AccessorException {
            try {
                CharSequence value = xacc.print(o);
                if(value==null)
                    throw new AccessorException(Messages.THERE_MUST_BE_VALUE_IN_XMLVALUE.format(o));
                return value;
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

        public void writeText(XMLSerializer w, BeanT o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
            if(!xacc.hasValue(o))
                throw new AccessorException(Messages.THERE_MUST_BE_VALUE_IN_XMLVALUE.format(o));
            xacc.writeText(w,o,fieldName);
        }

        public void writeLeafElement(XMLSerializer w, Name tagName, BeanT o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
            if(!xacc.hasValue(o))
                throw new AccessorException(Messages.THERE_MUST_BE_VALUE_IN_XMLVALUE.format(o));
            xacc.writeLeafElement(w,tagName,o,fieldName);
        }

        public QName getTypeName(BeanT instance) {
            return null;
        }
    }
}

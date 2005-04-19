package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.activation.MimeType;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.MimeTypedTransducer;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * {@link ModelBuilder} that works at the run-time by using
 * the {@code java.lang.reflect} package.
 *
 * <p>
 * This extends {@link ModelBuilder} by providing more functionalities such
 * as accessing the fields and classes.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class RuntimeModelBuilder extends ModelBuilder<Type,Class,Field,Method> {

    public RuntimeModelBuilder(RuntimeAnnotationReader annotationReader, String defaultNamespaceRemap) {
        super(annotationReader, Navigator.REFLECTION, defaultNamespaceRemap);
    }

    @Override
    public RuntimeNonElement getClassInfo( Class clazz, Locatable upstream ) {
        return (RuntimeNonElement)super.getClassInfo(clazz,upstream);
    }

    @Override
    protected RuntimeEnumLeafInfoImpl createEnumLeafInfo(Class clazz, Locatable upstream) {
        return new RuntimeEnumLeafInfoImpl(this,upstream,clazz);
    }

    @Override
    protected RuntimeClassInfoImpl createClassInfo( Class clazz, Locatable upstream ) {
        return new RuntimeClassInfoImpl(this,upstream,clazz);
    }

    @Override
    public RuntimeElementInfoImpl createElementInfo(RegistryInfoImpl<Type,Class,Field,Method> registryInfo, Method method) throws IllegalAnnotationException {
        return new RuntimeElementInfoImpl(this,registryInfo, method);
    }

    @Override
    public RuntimeArrayInfoImpl createArrayInfo(Locatable upstream, Type arrayType) {
        return new RuntimeArrayInfoImpl(this, upstream, (Class)arrayType);
    }

    public ReflectionNavigator getNavigator() {
        return (ReflectionNavigator)nav;
    }

    @Override
    protected RuntimeTypeInfoSetImpl createTypeInfoSet() {
        return new RuntimeTypeInfoSetImpl(reader);
    }

    @Override
    public RuntimeTypeInfoSet link() {
        return (RuntimeTypeInfoSet)super.link();
    }

    /**
     * Creates a {@link Transducer} given a reference.
     *
     * TODO: this is not the proper place for this class to be in.
     */
    public static Transducer createTransducer(RuntimeNonElementRef ref) {
        Transducer t = ref.getTarget().getTransducer();
        ID id = ref.getSource().id();

        if(id==ID.IDREF)
            return RuntimeBuiltinLeafInfoImpl.STRING;

        if(id==ID.ID)
            t = new IDTransducerImpl(t);

        MimeType emt = ref.getSource().getExpectedMimeType();
        if(emt!=null)
            t = new MimeTypedTransducer(t,emt);

        return t;
    }



    /**
     * Transducer implementation for ID.
     *
     * This transducer wraps another {@link Transducer} and adds
     * handling for ID.
     */
    private static final class IDTransducerImpl<ValueT> implements Transducer<ValueT> {
        private final Transducer<ValueT> core;

        public IDTransducerImpl(Transducer<ValueT> core) {
            this.core = core;
        }

        public boolean isDefault() {
            return false;
        }

        public boolean useNamespace() {
            return core.useNamespace();
        }

        public void declareNamespace( ValueT o, XMLSerializer w ) throws AccessorException {
            core.declareNamespace(o, w);
        }

        public CharSequence print(ValueT o) throws AccessorException {
            return core.print(o);
        }

        public ValueT parse(CharSequence lexical) throws AccessorException, SAXException {
            String value = WhiteSpaceProcessor.trim(lexical).toString();
            UnmarshallingContext.getInstance().addToIdTable(value);
            return core.parse(value);
        }
    }
}

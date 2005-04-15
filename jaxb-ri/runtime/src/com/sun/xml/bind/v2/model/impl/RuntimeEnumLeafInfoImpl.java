package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.annotation.FieldLocatable;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
final class RuntimeEnumLeafInfoImpl<T extends Enum<T>,B> extends EnumLeafInfoImpl<Type,Class,Field,Method>
    implements RuntimeEnumLeafInfo, Transducer<T> {

    public Transducer<T> getTransducer() {
        return this;
    }

    /**
     * {@link Transducer} that knows how to convert a lexical value
     * into the Java value that we can handle.
     */
    private final Transducer<B> baseXducer;

    private final Map<B,T> parseMap = new HashMap<B,T>();
    private final Map<T,B> printMap;

    RuntimeEnumLeafInfoImpl(RuntimeModelBuilder builder, Locatable upstream, Class<T> enumType) {
        super(builder,upstream,enumType,enumType);
        this.printMap = new EnumMap<T,B>(enumType);

        baseXducer = ((RuntimeNonElement)baseType).getTransducer();
    }

    @Override
    public RuntimeEnumConstantImpl createEnumConstant(String name, String literal, Field constant, EnumConstantImpl<Type,Class,Field,Method> last) {
        T t = null;
        try {
            try {
                constant.setAccessible(true);
            } catch (SecurityException e) {
                ; // in case the constant is already accessible, swallow this error.
                // if the constant is indeed not accessible, we will get IllegalAccessException
                // in the following line, and that is not too late.
            }
            t = (T)constant.get(null);
        } catch (IllegalAccessException e) {
            // impossible, because this is an enum constant
            throw new IllegalAccessError(e.getMessage());
        }

        B b = null;
        try {
            b = baseXducer.parse(literal);
        } catch (Exception e) {
            builder.reportError(new IllegalAnnotationException(
                Messages.INVALID_XML_ENUM_VALUE.format(name,baseType.getType().toString()), e,
                    new FieldLocatable<Field>(this,constant,nav()) ));
        }

        parseMap.put(b,t);
        printMap.put(t,b);

        return new RuntimeEnumConstantImpl(this, name, literal, last);
    }

    public boolean isDefault() {
        return false;
    }

    public Class getClazz() {
        return clazz;
    }

    public boolean useNamespace() {
        return baseXducer.useNamespace();
    }

    public void declareNamespace(T t, XMLSerializer w) throws AccessorException {
        baseXducer.declareNamespace(printMap.get(t),w);
    }

    public CharSequence print(T t) throws AccessorException {
        return baseXducer.print(printMap.get(t));
    }

    public T parse(CharSequence lexical) throws AccessorException, SAXException {
        // TODO: error handling

        B b = baseXducer.parse(lexical);
        if(b==null) {

            return null;
        }

        return parseMap.get(b);
    }
}

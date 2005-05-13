package com.sun.xml.bind.v2.runtime.property;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

/**
 * Create {@link Property} objects.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class PropertyFactory {
    private PropertyFactory() {}


    /**
     * Constructors of the {@link Property} implementation.
     */
    private static final Constructor<? extends Property>[] propImpls;

    static {
        Class<? extends Property>[] implClasses = new Class[] {
            SingleElementLeafProperty.class,
            null, // single reference leaf --- but there's no such thing as "reference leaf"

            ArrayElementLeafProperty.class,
            null, // array reference leaf --- but there's no such thing as "reference leaf"

            SingleElementNodeProperty.class,
            SingleReferenceNodeProperty.class,

            ArrayElementNodeProperty.class,
            ArrayReferenceNodeProperty.class,
        };

        propImpls = new Constructor[implClasses.length];
        for( int i=0; i<propImpls.length; i++ ) {
            if(implClasses[i]!=null)
                propImpls[i] = implClasses[i].getConstructors()[0];
        }
    }

    /**
     * Creates/obtains a properly configured {@link Property}
     * object from the given description.
     */
    public static Property create( JAXBContextImpl grammar, RuntimePropertyInfo info ) {

        if(info instanceof RuntimeAttributePropertyInfo)
            return new AttributeProperty(grammar,(RuntimeAttributePropertyInfo)info);
        if(info instanceof RuntimeValuePropertyInfo)
            return new ValueProperty(grammar,(RuntimeValuePropertyInfo)info);

        boolean isElement = info instanceof RuntimeElementPropertyInfo;

        if(isElement && ((RuntimeElementPropertyInfo)info).isValueList())
            return new ListElementProperty(grammar,(RuntimeElementPropertyInfo) info);

        boolean isCollection = info.isCollection();
        boolean isLeaf = isLeaf(info);

        Constructor<? extends Property> c = propImpls[(isLeaf?0:4)+(isCollection?2:0)+(isElement?0:1)];
        try {
            return c.newInstance( grammar, info );
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if(t instanceof Error)
                throw (Error)t;
            if(t instanceof RuntimeException)
                throw (RuntimeException)t;

            throw new AssertionError(t);
        }
    }

    private static boolean isLeaf(RuntimePropertyInfo info) {
        Collection<? extends RuntimeTypeInfo> types = info.ref();
        if(types.size()!=1)     return false;

        RuntimeTypeInfo rti = types.iterator().next();
        if(!(rti instanceof RuntimeNonElement)) return false;

        if(((RuntimeNonElement)rti).getTransducer()==null )
            return false;

        return true;
    }
}

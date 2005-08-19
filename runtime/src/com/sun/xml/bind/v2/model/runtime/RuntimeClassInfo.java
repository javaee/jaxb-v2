package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.annotation.XmlLocation;

import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface RuntimeClassInfo extends ClassInfo<Type,Class>, RuntimeNonElement {
    RuntimeClassInfo getBaseClass();

    // refined to return RuntimePropertyInfo
    List<? extends RuntimePropertyInfo> getProperties();
    RuntimePropertyInfo getProperty(String name);


    /**
     * If {@link #hasAttributeWildcard()} is true,
     * returns the accessor to access the property.
     *
     * @return
     *      unoptimized accessor.
     *      non-null iff {@link #hasAttributeWildcard()}==true.
     *
     * @see Accessor#optimize()
     */
    <BeanT> Accessor<BeanT,Map<QName,String>> getAttributeWildcard();

    /**
     * If this JAXB bean has a property annotated with {@link XmlLocation},
     * this method returns it.
     *
     * @return may be null.
     */
    <BeanT> Accessor<BeanT,Locator> getLocatorField();
}

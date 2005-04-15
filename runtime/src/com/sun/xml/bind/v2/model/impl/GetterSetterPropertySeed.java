package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link PropertyInfo} implementation backed by a getter and a setter.
 *
 * We allow the getter or setter to be null, in which case the bean
 * can only participate in unmarshalling (or marshalling)
 */
class GetterSetterPropertySeed<TypeT,ClassDeclT,FieldT,MethodT> implements
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> {

    protected final MethodT getter;
    protected final MethodT setter;
    private ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent;

    GetterSetterPropertySeed(ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent, MethodT getter, MethodT setter) {
        this.parent = parent;
        this.getter = getter;
        this.setter = setter;

        if(getter==null && setter==null)
            throw new IllegalArgumentException();
    }

    public TypeT getRawType() {
        TODO.prototype();   // handle the case where setter is null
        return parent.nav().getReturnType(getter);
    }

    public <A extends Annotation> A readAnnotation(Class<A> annotation) {
        return parent.reader().getMethodAnnotation(annotation,getName(),getter,setter,this);
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return parent.reader().hasMethodAnnotation(annotationType,getName(),getter,setter,this);
    }

    public String getName() {
        if(getter!=null)
            return getName(getter);
        else
            return getName(setter);
    }

    private String getName(MethodT m) {
        String seed = parent.nav().getMethodName(m);
        String lseed = seed.toLowerCase();
        if(lseed.startsWith("get") || lseed.startsWith("set"))
            return camelize(seed.substring(3));
        if(lseed.startsWith("is"))
            return camelize(seed.substring(2));
        return seed;
    }


    private static String camelize(String s) {
        // is this what the spec is going to use?
        TODO.checkSpec();
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Use the enclosing class as the upsream {@link Location}.
     */
    public Locatable getUpstream() {
        return parent;
    }

    public Location getLocation() {
        if(getter!=null)
            return parent.nav().getMethodLocation(getter);
        else
            return parent.nav().getMethodLocation(setter);
    }

//
//
// AccessibleProperty implementation
//
//
    public String generateSetValue(String $bean, String $var) {
        return $bean+'.'+parent.nav().getMethodName(setter)+'('+$var+");";
    }

    public String generateGetValue(String $bean) {
        return $bean+'.'+parent.nav().getMethodName(getter)+"()";
    }
}

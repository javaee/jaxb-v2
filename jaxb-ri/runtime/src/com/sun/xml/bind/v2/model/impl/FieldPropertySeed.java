package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link PropertyInfo} implementation backed by a field.
 */
class FieldPropertySeed<TypeT,ClassDeclT,FieldT,MethodT> implements
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> {

    protected final FieldT field;
    private ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent;

    FieldPropertySeed(ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> classInfo, FieldT field) {
        this.parent = classInfo;
        this.field = field;
    }

    public <A extends Annotation> A readAnnotation(Class<A> a) {
        return parent.reader().getFieldAnnotation(a,getName(),field,this);
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return parent.reader().hasFieldAnnotation(annotationType,field);
    }

    public String getName() {
        return parent.nav().getFieldName(field);
    }

    public TypeT getRawType() {
        return parent.nav().getFieldType(field);
    }

    /**
     * Use the enclosing class as the upsream {@link Location}.
     */
    public Locatable getUpstream() {
        return parent;
    }

    public Location getLocation() {
        return parent.nav().getFieldLocation(field);
    }
//
//
// AccessibleProperty implementation
//
//
    private String lvalue(String $bean) {
        return $bean+'.'+parent.nav().getFieldName(field);
    }

    public String generateSetValue(String $bean, String $var) {
        return lvalue($bean)+'='+$var+';';
    }

    public String generateGetValue(String $bean) {
        return lvalue($bean);
    }
}

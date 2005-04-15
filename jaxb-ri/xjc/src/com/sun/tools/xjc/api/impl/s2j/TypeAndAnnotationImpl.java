package com.sun.tools.xjc.api.impl.s2j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.sun.xml.bind.annotation.XmlList;

/**
 * {@link TypeAndAnnotation} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
final class TypeAndAnnotationImpl implements TypeAndAnnotation {
    private final TypeUse typeUse;
    private final Outline outline;

    public TypeAndAnnotationImpl(Outline outline, TypeUse typeUse) {
        this.typeUse = typeUse;
        this.outline = outline;
    }

    public String getTypeClass() {
        CAdapter a = typeUse.getAdapterUse();
        NType nt;
        if(a!=null)
            nt = a.customType;
        else
            nt = typeUse.getInfo().getType();

        JType jt = nt.toType(outline,Aspect.EXPOSED);

        JPrimitiveType prim = jt.boxify().getPrimitiveType();
        if(!typeUse.isCollection() && prim!=null)
            jt = prim;
        String name = jt.fullName();

        if(typeUse.isCollection())
            name = name+"[]";

        return name;
    }

    public List<String> getAnnotations() {
        if(typeUse.getAdapterUse()==null && !typeUse.isCollection())
            return Collections.emptyList();

        List<String> a = new ArrayList<String>();
        CAdapter adapterUse = typeUse.getAdapterUse();
        if(adapterUse!=null) {
            a.add('@'+XmlJavaTypeAdapter.class.getName()+'('+adapterUse.adapterType.fullName()+".class)");
        }
        if(typeUse.isCollection()) {
            a.add('@'+XmlList.class.getName());
        }
        return a;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for( String s : getAnnotations() )
            builder.append(s).append(' ');
        builder.append(getTypeClass());
        return builder.toString();
    }
}

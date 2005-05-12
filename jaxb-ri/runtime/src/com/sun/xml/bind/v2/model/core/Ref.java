package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * Reference to a type in a model.
 *
 * TODO: isn't there a similarity between this and TypeUse in XJC?
 *
 * @author Kohsuke Kawaguchi
 */
public final class Ref<TypeT,ClassDeclT> {
    /**
     * The type being referenced.
     * <p>
     * If the type is adapted, this field is the same as the adapter's default type.
     */
    public final TypeT type;
    /**
     * If the reference has an adapter, non-null.
     */
    public final Adapter<TypeT,ClassDeclT> adapter;
    /**
     * If the {@link #type} is an array and it is a value list,
     * true.
     */
    public final boolean valueList;

    public Ref(TypeT type) {
        this(type,null,false);
    }

    public Ref(TypeT type, Adapter<TypeT, ClassDeclT> adapter, boolean valueList) {
        this.adapter = adapter;
        if(adapter!=null)
            type=adapter.defaultType;
        this.type = type;
        this.valueList = valueList;
    }

    public Ref(ModelBuilder<TypeT,ClassDeclT,?,?> builder, TypeT type, XmlJavaTypeAdapter xjta, XmlList xl ) {
        this(builder.reader,builder.nav,type,xjta,xl);
    }
    
    public Ref(AnnotationReader<TypeT,ClassDeclT,?,?> reader,
               Navigator<TypeT,ClassDeclT,?,?> nav,
               TypeT type, XmlJavaTypeAdapter xjta, XmlList xl ) {
        Adapter<TypeT,ClassDeclT> adapter=null;
        if(xjta!=null) {
            adapter = new Adapter<TypeT,ClassDeclT>(xjta,reader,nav);
            type = adapter.defaultType;
        }

        this.type = type;
        this.adapter = adapter;
        this.valueList = xl!=null;
    }
}

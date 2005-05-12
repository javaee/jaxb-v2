package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * {@link Adapter} that wraps {@link XmlJavaTypeAdapter}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Adapter<TypeT,ClassDeclT> {
    /**
     * The adapter class. Always non-null.
     *
     * A class that derives from {@link javax.xml.bind.annotation.adapters.XmlAdapter}.
     */
    public final ClassDeclT adapterType;

    /**
     * The type that the JAXB can handle natively.
     * The <tt>Default</tt> parameter of <tt>XmlAdapter&lt;Default,Custom></tt>.
     *
     * Always non-null.
     */
    public final TypeT defaultType;

    /**
     * The type that is stored in memory.
     * The <tt>Custom</tt> parameter of <tt>XmlAdapter&lt;Default,Custom></tt>.
     */
    public final TypeT customType;



    public Adapter(
        XmlJavaTypeAdapter spec,
        AnnotationReader<TypeT,ClassDeclT,?,?> reader,
        Navigator<TypeT,ClassDeclT,?,?> nav) {

        this( nav.asDecl(reader.getClassValue(spec,"value")), nav );
    }

    public Adapter(ClassDeclT adapterType,Navigator<TypeT,ClassDeclT,?,?> nav) {
        this.adapterType = adapterType;
        TypeT baseClass = nav.getBaseClass(nav.use(adapterType), nav.asDecl(XmlAdapter.class));

        // because the parameterization of XmlJavaTypeAdapter requires that the class derives from XmlAdapter.
        assert baseClass!=null;

        if(nav.isParameterizedType(baseClass))
            defaultType = nav.getTypeArgument(baseClass,0);
        else
            defaultType = nav.ref(Object.class);

        if(nav.isParameterizedType(baseClass))
            customType = nav.getTypeArgument(baseClass,1);
        else
            customType = nav.ref(Object.class);
    }
}

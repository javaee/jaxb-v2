package com.sun.xml.bind.v2.model.impl;

import javax.xml.namespace.QName;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;

import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;

/**
 * Common code between {@link PropertyInfoImpl} and {@link ElementInfoImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
final class Util {
    static <T,C,F,M> QName calcSchemaType(
            AnnotationReader<T,C,F,M> reader,
            AnnotationSource primarySource, C enclosingClass, C individualType, Locatable src ) {

        XmlSchemaType xst = primarySource.readAnnotation(XmlSchemaType.class);
        if(xst!=null) {
            return new QName(xst.namespace(),xst.name());
        }

        // check the defaulted annotation
        XmlSchemaTypes xsts = reader.getPackageAnnotation(XmlSchemaTypes.class,enclosingClass,src);
        XmlSchemaType[] values = null;
        if(xsts!=null)
            values = xsts.value();
        else {
            xst = reader.getPackageAnnotation(XmlSchemaType.class,enclosingClass,src);
            if(xst!=null) {
                values = new XmlSchemaType[1];
                values[0] = xst;
            }
        }
        if(values!=null) {
            for( XmlSchemaType item : values ) {
                if(reader.getClassValue(item,"type").equals(individualType)) {
                    return new QName(item.namespace(),item.name());
                }
            }
        }

        return null;
    }
}

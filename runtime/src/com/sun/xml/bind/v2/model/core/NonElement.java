package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

/**
 * {@link TypeInfo} that maps to an element.
 *
 * Either {@link LeafInfo} or {@link ClassInfo}.
 *
 * TODO: better name.
 *
 * @author Kohsuke Kawaguchi
 */
public interface NonElement<TypeT,ClassDeclT> extends TypeInfo<TypeT,ClassDeclT> {
    /**
     * Gets the XML type name of the class.
     *
     * @return
     *      null if the object doesn't have an explicit type name.
     */
    QName getTypeName();
}

package com.sun.xml.bind.v2.model.core;

/**
 * Type referenced as a result of having the wildcard.
 *
 * TODO: think about how to gracefully handle the difference between LAX,SKIP, and STRICT.
 *
 * @author Kohsuke Kawaguchi
 */
public interface WildcardTypeInfo<TypeT,ClassDeclT> extends TypeInfo<TypeT,ClassDeclT> {
}

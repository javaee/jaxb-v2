package com.sun.xml.bind.v2.model.core;

/**
 * @author Kohsuke Kawaguchi
 */
public interface EnumLeafInfo<TypeT,ClassDeclT> extends LeafInfo<TypeT,ClassDeclT> {
    /**
     * The same as {@link #getType()} but an {@link EnumLeafInfo}
     * is guaranteed to represent an enum declaration, which is a
     * kind of a class declaration.
     *
     * @return
     *      always non-null.
     */
    ClassDeclT getClazz();

    /**
     * Returns the base type of the enumeration.
     *
     * <p>
     * For example, with the following enum class, this method
     * returns {@link BuiltinLeafInfo} for {@link Integer}.
     *
     * <pre>
     * &amp;XmlEnum(Integer.class)
     * enum Foo {
     *   &amp;XmlEnumValue("1")
     *   ONE,
     *   &amp;XmlEnumValue("2")
     *   TWO
     * }
     * </pre>
     *
     * @return
     *      never null.
     */
    NonElement<TypeT,ClassDeclT> getBaseType();

    /**
     * Returns the read-only list of enumeration constants.
     *
     * @return
     *      never null. Can be empty (really?).
     */
    Iterable<? extends EnumConstant> getConstants();
}

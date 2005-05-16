package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Individual constant of an enumeration.
 *
 * <p>
 * Javadoc in this class uses the following sample to explain the semantics:
 * <pre>
 * &#64;XmlEnum(Integer.class)
 * enum Foo {
 *   &#64;XmlEnumValue("1")
 *   ONE,
 *   &#64;XmlEnumValue("2")
 *   TWO
 * }
 * </pre>
 *
 * @see EnumLeafInfo
 * @author Kohsuke Kawaguchi
 */
public interface EnumConstant<TypeT,ClassDeclT> {

    /**
     * Gets the {@link EnumLeafInfo} to which this constant belongs to.
     *
     * @return never null.
     */
    EnumLeafInfo<TypeT,ClassDeclT> getEnclosingClass();

    /**
     * Lexical value of this constant.
     *
     * <p>
     * This value should be evaluated against
     * {@link EnumLeafInfo#getBaseType()} to obtain the typed value.
     *
     * <p>
     * This is the same value as written in the {@link XmlEnumValue} annotation.
     * In the above example, this method returns "1" and "2".
     *
     * @return
     *      never null.
     */
    String getLexicalValue();

    /**
     * Gets the constant name.
     *
     * <p>
     * In the above example this method return "ONE" and "TWO".
     *
     * @return
     *      never null. A valid Java identifier.
     */
    String getName();
}

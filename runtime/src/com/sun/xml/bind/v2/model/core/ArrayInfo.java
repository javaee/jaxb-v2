package com.sun.xml.bind.v2.model.core;

/**
 * Stand-alone array that can be marshalled/unmarshalled on its own
 * (without being part of any encloding {@link ClassInfo}.)
 *
 * <p>
 * Most of the times arrays are treated as properties of their enclosing classes,
 * but sometimes we do need to map an array class to its own XML type.
 * This object is used for that purpose.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ArrayInfo<T,C> extends NonElement<T,C> {
    /**
     * T of T[]. The type of the items of the array.
     *
     * @return  never null
     */
    NonElement<T,C> getItemType();
}

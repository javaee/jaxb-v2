package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.api.SchemaOutputResolver;

/**
 * Implemented by the schema generators.
 *
 * <p>
 * This interface allows the runtime code to interact with the implementation
 * without statically linking to it.
 *
 * @author Kohsuke Kawaguchi
 */
public interface SchemaGenerator<T,C,F,M> {
    /**
     * Fills the schema generator by the specified type information.
     */
    void fill(TypeInfoSet<T,C,F,M> types);

    /**
     * Adds an additional element declaration.
     *
     * @param tagName
     *      The name of the element declaration to be added.
     * @param type
     *      The type this element refers to.
     *      Can be null, in which case the element refers to an empty anonymous complex type.
     */
    void add( QName tagName, NonElement<T,C> type );

    /**
     * Writes the schema to the given output.
     */
    void write(SchemaOutputResolver resolver) throws IOException;
}

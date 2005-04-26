package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import com.sun.xml.bind.v2.model.core.TypeInfoSet;
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
     * Writes the schema to the given output.
     */
    void write(SchemaOutputResolver resolver) throws IOException;
}

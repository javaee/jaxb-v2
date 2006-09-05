package com.sun.tools.xjc.api;

import java.io.IOException;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

/**
 * {@link JAXBModel} that exposes additional information available
 * only for the java->schema direction.
 *
 * @author Kohsuke Kawaguchi
 */
public interface J2SJAXBModel extends JAXBModel {
    /**
     * Returns the name of the XML Type bound to the
     * specified Java type.
     *
     * @param javaType
     *      must not be null. This must be one of the {@link Reference}s specified
     *      in the {@link JavaCompiler#bind} method.
     *
     * @return
     *      null if it is not a part of the input to {@link JavaCompiler#bind}.
     *
     * @throws IllegalArgumentException
     *      if the parameter is null
     */
    QName getXmlTypeName(Reference javaType);

    /**
     * Generates the schema documents from the model.
     *
     * @param outputResolver
     *      this object controls the output to which schemas
     *      will be sent.
     *
     * @throws IOException
     *      if {@link SchemaOutputResolver} throws an {@link IOException}.
     */
    void generateSchema(SchemaOutputResolver outputResolver, ErrorListener errorListener) throws IOException;

    /**
     * Generates the episode file from the model.
     *
     * <p>
     * The "episode file" is really just a JAXB customization file (but with vendor extensions,
     * at this point), that can be used later with a schema compilation to support separate
     * compilation.
     *
     * @param output
     *      This receives the generated episode file.
     * @since 2.1
     */
    void generateEpisodeFile(Result output);
}

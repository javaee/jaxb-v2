package com.sun.tools.xjc.api;

import java.util.Collection;

import javax.xml.namespace.QName;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;

/**
 * {@link JAXBModel} that exposes additional information available
 * only for the schema->java direction.
 *
 * @author Kohsuke Kawaguchi
 */
public interface S2JJAXBModel extends JAXBModel {

    /**
     * Gets a {@link Mapping} object for the given global element.
     *
     * @return
     *      null if the element name is not a defined global element in the schema.
     */
    Mapping get( QName elementName );

    /**
     * Gets a read-only view of all the {@link Mapping}s.
     */
    Collection<? extends Mapping> getMappings();

    /**
     * Returns the fully-qualified name of the Java type that is bound to the
     * specified XML type.
     *
     * @param xmlTypeName
     *      must not be null.
     * @return
     *      null if the XML type is not bound to any Java type.
     */
    TypeAndAnnotation getJavaType(QName xmlTypeName);

    /**
     * Generates artifacts.
     *
     * <p>
     * TODO: if JAXB supports various modes of code generations
     * (such as public interface only or implementation only or
     * etc), we should define bit flags to control those.
     *
     * <p>
     * This operation is only supported for a model built from a schema.
     *
     * @param extensions
     *      The JAXB RI extensions to run. This can be null or empty
     *      array if the caller wishes not to run any extension.
     *      <br>
     *
     *      Those specified extensions
     *      will participate in the code generation. Specifying an extension
     *      in this list has the same effect of turning that extension on
     *      via command line.
     *      <br>
     *
     *      It is the caller's responsibility to configure each augmenter
     *      properly by using {@link Plugin#parseArgument(Options, String[], int)}.
     *
     * @return
     *      object filled with the generated code. Use
     *      {@link JCodeModel#build(CodeWriter)} to write them
     *      to a disk.
     */
    JCodeModel generateCode( Plugin[] extensions, ErrorListener errorListener );
}

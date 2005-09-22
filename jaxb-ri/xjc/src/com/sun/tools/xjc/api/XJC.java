/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.api;

import com.sun.codemodel.JJavaName;
import com.sun.tools.xjc.api.impl.j2s.JavaCompilerImpl;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;
import com.sun.tools.xjc.reader.Util;
import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.codemodel.JJavaName;

/**
 * Entry point to the JAXB RI interface.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class XJC {
    /**
     * Gets a fresh {@link JavaCompiler}.
     * 
     * @return
     *      always return non-null object.
     */
    public static final JavaCompiler createJavaCompiler() {
        return new JavaCompilerImpl();
    }
    
    /**
     * Gets a fresh {@link SchemaCompiler}.
     * 
     * @return
     *      always return non-null object.
     */
    public static final SchemaCompiler createSchemaCompiler() {
        return new SchemaCompilerImpl();
    }

    /**
     * Computes the namespace URI -> package name conversion
     * as specified by the JAXB spec.
     *
     * @param namespaceUri
     *      Namespace URI. Can be empty, but must not be null.
     * @return
     *      A Java package name (e.g., "foo.bar"). "" to represent the root package.
     *      This method returns null if the method fails to derive the package name
     *      (there are certain namespace URIs with which this algorithm does not
     *      work --- such as ":::" as the URI.)
     */
    public static final String getDefaultPackageName( String namespaceUri ) {
        if(namespaceUri==null)   throw new IllegalArgumentException();
        return Util.getPackageNameFromNamespaceURI( namespaceUri );
    }

    /**
     * Obtains the expected MIME type for the given Java BLOB class.
     *
     * <p>
     * The details of this table is going to be defined in the JAXB spec,
     * but for example, this method encapsulates a table like
     * "java.awt.Image" -> "image/*".
     *
     * @param className
     *      a string that looks like a fully qualified Java class name.
     * @return
     *      null if the given class name is not recognized as a Java BLOB class.
     */
    public static final String getExpectedMimeTypeForBlobClass( String className ) {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    /**
     * Obtains the Java BLOB class for the given expected MIME type.
     *
     *
     * @param mimeType
     *      a string that looks like a MIME type, such as "image/jpeg"
     *      or "text/*".
     * @return
     *      always non-null valid string that looks like a fully qualifieid Java class name.
     *      Such as "javax.activation.DataHandler".
     * @throws IllegalArgumentException
     *      if the given string is not a MIME type. 
     */
    public static final String getBlobClassForMimeType( String mimeType ) {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    /**
     * Computes a Java identifier from a local name.
     *
     * <p>
     * This method faithfully implements the name mangling rule as specified in the JAXB spec.
     *
     * <p>
     * In JAXB, a collision with a Java reserved word (such as "return") never happens.
     * Accordingly, this method may return an identifier that collides with reserved words.
     *
     * <p>
     * Use {@link JJavaName#isJavaIdentifier(String)} to check for such collision.
     *
     * @return
     *      Typically, this method returns "nameLikeThis".
     *
     * @see JJavaName#isJavaIdentifier(String)
     *
     * @deprecated use {@link JAXBRIContext#mangleNameToVariableName(String)}
     */
    public static final String mangleNameToVariableName(String localName) {
        return NameConverter.standard.toVariableName(localName);
    }

    /**
     * Computes a Java class name from a local name.
     *
     * <p>
     * This method faithfully implements the name mangling rule as specified in the JAXB spec.
     *
     * @return
     *      Typically, this method returns "NameLikeThis".
     *
     * @deprecated use {@link JAXBRIContext#mangleNameToClassName(String)} 
     */
    public static final String mangleNameToClassName(String localName) {
        return NameConverter.standard.toClassName(localName);
    }
}

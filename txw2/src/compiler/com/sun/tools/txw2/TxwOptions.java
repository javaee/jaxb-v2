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

package com.sun.tools.txw2;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.xml.txw2.annotation.XmlNamespace;

/**
 * Controls the various aspects of the TXW generation.
 *
 * But this doesn't contain options for the command-line interface
 * nor any of the driver-level configuration (such as where to place
 * the generated source code, etc.)
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TxwOptions {
    public final JCodeModel codeModel = new JCodeModel();

    /**
     * The package to put the generated code into.
     */
    public JPackage _package;

    /**
     * Always non-null.
     */
    public ErrorListener errorListener;

    /**
     * The generated code will be sent to this.
     */
    CodeWriter codeWriter;

    /**
     * Schema file.
     */
    SchemaBuilder source;

    /**
     * If true, generate attribute/value methods that
     * returns the <tt>this</tt> object for chaining.
     */
    public boolean chainMethod;

    /**
     * If true, the generated code will not use the package-level
     * {@link XmlNamespace} annotation.
     */
    public boolean noPackageNamespace;
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.tools.xjc.api;

import com.sun.tools.xjc.api.impl.j2s.JavaCompilerImpl;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;
import com.sun.xml.bind.api.impl.NameConverter;

/**
 * Entry point to the programatic API to access
 * schema compiler (XJC) and schema generator (schemagen).
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
    public static JavaCompiler createJavaCompiler() {
        return new JavaCompilerImpl();
    }

    /**
     * Gets a fresh {@link SchemaCompiler}.
     * 
     * @return
     *      always return non-null object.
     */
    public static SchemaCompiler createSchemaCompiler() {
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
    public static String getDefaultPackageName( String namespaceUri ) {
        if(namespaceUri==null)   throw new IllegalArgumentException();
        return NameConverter.standard.toPackageName( namespaceUri );
    }
}

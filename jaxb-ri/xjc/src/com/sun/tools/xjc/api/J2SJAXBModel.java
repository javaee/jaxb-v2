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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * {@link Adapter} that wraps {@link XmlJavaTypeAdapter}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Adapter<TypeT,ClassDeclT> {
    /**
     * The adapter class. Always non-null.
     *
     * A class that derives from {@link javax.xml.bind.annotation.adapters.XmlAdapter}.
     */
    public final ClassDeclT adapterType;

    /**
     * The type that the JAXB can handle natively.
     * The {@code Default} parameter of {@code XmlAdapter<Default,Custom>}.
     *
     * Always non-null.
     */
    public final TypeT defaultType;

    /**
     * The type that is stored in memory.
     * The {@code Custom} parameter of {@code XmlAdapter<Default,Custom>}.
     */
    public final TypeT customType;



    public Adapter(
        XmlJavaTypeAdapter spec,
        AnnotationReader<TypeT,ClassDeclT,?,?> reader,
        Navigator<TypeT,ClassDeclT,?,?> nav) {

        this( nav.asDecl(reader.getClassValue(spec,"value")), nav );
    }

    public Adapter(ClassDeclT adapterType,Navigator<TypeT,ClassDeclT,?,?> nav) {
        this.adapterType = adapterType;
        TypeT baseClass = nav.getBaseClass(nav.use(adapterType), nav.asDecl(XmlAdapter.class));

        // because the parameterization of XmlJavaTypeAdapter requires that the class derives from XmlAdapter.
        assert baseClass!=null;

        if(nav.isParameterizedType(baseClass))
            defaultType = nav.getTypeArgument(baseClass,0);
        else
            defaultType = nav.ref(Object.class);

        if(nav.isParameterizedType(baseClass))
            customType = nav.getTypeArgument(baseClass,1);
        else
            customType = nav.ref(Object.class);
    }
}

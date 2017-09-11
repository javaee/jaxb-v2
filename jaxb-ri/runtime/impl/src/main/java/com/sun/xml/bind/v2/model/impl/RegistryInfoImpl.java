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

package com.sun.xml.bind.v2.model.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElementDecl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.MethodLocatable;
import com.sun.xml.bind.v2.model.core.RegistryInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.ContextFactory;

/**
 * Implementation of {@link RegistryInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
// experimenting with shorter type parameters for <T,C,F,M> quadruple.
// the idea is that they show so often that you'd understand the meaning
// without relying on the whole name.
final class RegistryInfoImpl<T,C,F,M> implements Locatable, RegistryInfo<T,C> {

    final C registryClass;
    private final Locatable upstream;
    private final Navigator<T,C,F,M> nav;

    /**
     * Types that are referenced from this registry.
     */
    private final Set<TypeInfo<T,C>> references = new LinkedHashSet<TypeInfo<T,C>>();

    /**
     * Picks up references in this registry to other types.
     */
    RegistryInfoImpl(ModelBuilder<T,C,F,M> builder, Locatable upstream, C registryClass) {
        this.nav = builder.nav;
        this.registryClass = registryClass;
        this.upstream = upstream;
        builder.registries.put(getPackageName(),this);

        if(nav.getDeclaredField(registryClass,ContextFactory.USE_JAXB_PROPERTIES)!=null) {
            // the user is trying to use ObjectFactory that we generate for interfaces,
            // that means he's missing jaxb.properties
            builder.reportError(new IllegalAnnotationException(
                Messages.MISSING_JAXB_PROPERTIES.format(getPackageName()),
                this
            ));
            // looking at members will only add more errors, so just abort now
            return;
        }

        for( M m : nav.getDeclaredMethods(registryClass) ) {
            XmlElementDecl em = builder.reader.getMethodAnnotation(
                XmlElementDecl.class, m, this );

            if(em==null) {
                if(nav.getMethodName(m).startsWith("create")) {
                    // this is a factory method. visit this class
                    references.add(
                        builder.getTypeInfo(nav.getReturnType(m),
                            new MethodLocatable<M>(this,m,nav)));
                }

                continue;
            }

            ElementInfoImpl<T,C,F,M> ei;
            try {
                ei = builder.createElementInfo(this,m);
            } catch (IllegalAnnotationException e) {
                builder.reportError(e);
                continue;   // recover by ignoring this element
            }

            // register this mapping
            // TODO: any chance this could cause a stack overflow (by recursively visiting classes)?
            builder.typeInfoSet.add(ei,builder);
            references.add(ei);
        }
    }

    public Locatable getUpstream() {
        return upstream;
    }

    public Location getLocation() {
        return nav.getClassLocation(registryClass);
    }

    public Set<TypeInfo<T,C>> getReferences() {
        return references;
    }

    /**
     * Gets the name of the package that this registry governs.
     */
    public String getPackageName() {
        return nav.getPackageName(registryClass);
    }

    public C getClazz() {
        return registryClass;
    }
}

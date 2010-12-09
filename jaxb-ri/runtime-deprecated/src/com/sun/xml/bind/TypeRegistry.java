/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.bind;

import com.sun.xml.bind.unmarshaller.UnmarshallableObject;

/**
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class TypeRegistry
{
    public TypeRegistry( GrammarInfo _info ) {
        this.info = _info;
    }
    
    private final GrammarInfo info;
    public final GrammarInfo getGrammarInfo() { return info; }
    
    /**
     * Creates a new instance of XMLObject from a root element name.
     * 
     * @return
     *      If there is no associated root element, this method returns
     *      null.
     */
    public UnmarshallableObject createRootElement( String namespaceUri, String localName ) {
        Class clazz = getRootElement(namespaceUri,localName);
        if(clazz==null) return null;
        return createInstanceOf(clazz);
    }
    
    public Class getRootElement( String namespaceUri, String localName ) {
        return info.getRootElement(namespaceUri,localName);
    }
    
    public UnmarshallableObject createInstanceOf( Class clazz ) {
        // TODO: type registry can instanciate a derived class.
        try {
            return (UnmarshallableObject)clazz.newInstance();
        } catch( InstantiationException e ) {
            // TODO: error handling
            throw new InstantiationError(e.getMessage());
        } catch( IllegalAccessException e ) {
            // TODO: error handling
            throw new IllegalAccessError(e.getMessage());
        }
    }
}

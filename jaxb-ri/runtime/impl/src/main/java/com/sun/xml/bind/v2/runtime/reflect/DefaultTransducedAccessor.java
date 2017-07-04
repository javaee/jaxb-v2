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

package com.sun.xml.bind.v2.runtime.reflect;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.Name;

import org.xml.sax.SAXException;

/**
 * {@link TransducedAccessor} that prints to {@link String}.
 *
 * <p>
 * The print method that works for {@link String} determines the dispatching
 * of the {@link #writeText(XMLSerializer,Object,String)} and
 * {@link #writeLeafElement(XMLSerializer, Name, Object, String)} methods,
 * so those are implemented here. 
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class DefaultTransducedAccessor<T> extends TransducedAccessor<T> {

    public abstract String print(T o) throws AccessorException, SAXException;

    public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName) throws SAXException, AccessorException, IOException, XMLStreamException {
        w.leafElement(tagName,print(o),fieldName);
    }

    public void writeText(XMLSerializer w, T o, String fieldName) throws AccessorException, SAXException, IOException, XMLStreamException {
        w.text(print(o),fieldName);
    }
}

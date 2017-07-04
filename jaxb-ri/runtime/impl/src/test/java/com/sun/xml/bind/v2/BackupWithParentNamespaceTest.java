/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.bind.v2;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader;

import junit.framework.TestCase;

public class BackupWithParentNamespaceTest extends TestCase {

    @XmlRootElement(name = "root", namespace = "http://example.org")
    static class Root {
        @XmlElement(namespace = "http://nested.example.org")
        Nested foo;
    }

    @XmlType(namespace = "http://example.org")
    static class Nested {
        @XmlElement(namespace = "http://example.org")
        String bar;    
    }

    // bug#25092248/21667799/JAXB-867: lookup loader by parent namespace also
    // root = example.org namespace
    // foo = nested.example.org namespace
    // bar = example.org namepace with no namespace specified, example.org namespace should be used, instead of nested.example.org
    // by SPEC unmarshaller should fail, but due to JAXB-867 there were few releases (from 2.2.5 to 2.3 (not including)
    // that handled it gracefully, so some clients rely on this behavior and need support for this further on
    // this is fullfilled with com.sun.xml.bind.v2.runtime.unmarshaller.BackupWithParentNamespace system property
    public void test1() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JAXBRIContext.BACKUP_WITH_PARENT_NAMESPACE, Boolean.TRUE);
        JAXBContext c = JAXBContext.newInstance(new Class[] {Root.class}, properties);

        Root root = (Root) c.createUnmarshaller().unmarshal(new StringReader("<root xmlns='http://example.org'><foo xmlns='http://nested.example.org'><bar>bar</bar></foo></root>"));
        assertNotNull("root", root);
        Nested foo = root.foo;
        assertNotNull("foo", foo);
        assertEquals("bar", foo.bar);
    }
}

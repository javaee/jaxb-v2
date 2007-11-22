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

package com.sun.xml.bind.v2;

import java.util.Arrays;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;

import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class CompositeStructureTest extends TestCase {

    // this annotation is just so that we can pass it to tr4.
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    public void test1() throws Exception {
        TypeReference tr1 = new TypeReference(new QName("","foo"),String.class);
        TypeReference tr2 = new TypeReference(new QName("","bar"),int.class);
        TypeReference tr3 = new TypeReference(new QName("","zot"),byte[].class);
        TypeReference tr4 = new TypeReference(new QName("","zoo"),byte[].class,
                this.getClass().getMethod("test1").getAnnotation(XmlJavaTypeAdapter.class));
        JAXBRIContext c = JAXBRIContext.newInstance(new Class[0],
                Arrays.asList(tr1,tr2,tr3,tr4),"",false);

        CompositeStructure cs = new CompositeStructure();
        cs.bridges = new Bridge[] {
            c.createBridge(tr1),
            c.createBridge(tr2),
            c.createBridge(tr3),
            c.createBridge(tr4),
        };
        cs.values = new Object[] { "foo", 5, new byte[4], new byte[4] };

        JAXBElement<CompositeStructure> root = new JAXBElement<CompositeStructure>(
                new QName("", "root"), CompositeStructure.class, cs);

        StringWriter sw = new StringWriter();
        c.createMarshaller().marshal(root,System.out);
        c.createMarshaller().marshal(root,sw);
        assertTrue(sw.toString().contains(
            "<root><foo>foo</foo><bar>5</bar><zot>AAAAAA==</zot><zoo>00000000</zoo></root>"));
    }
}

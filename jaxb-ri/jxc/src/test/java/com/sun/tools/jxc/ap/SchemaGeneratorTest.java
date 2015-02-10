/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.jxc.ap;

import com.sun.tools.xjc.api.Reference;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static mockit.Deencapsulation.invoke;
import static org.junit.Assert.assertTrue;

/**
 * @author yaroska
 */

@RunWith(JMockit.class)
public final class SchemaGeneratorTest {

    @Test
    public void filterClassTest(
            @Mocked Reference ref, // needed for filing of the result's list
            @Mocked final TypeElement interfaceElement, @Mocked final TypeElement enumElement,
            @Mocked final TypeElement classElement, @Mocked final TypeElement nestedClassElement,
            @Mocked final TypeElement nestedEnumElement) throws Exception {

        new Expectations() {{
            interfaceElement.getKind(); result = ElementKind.INTERFACE; // this one will be ignored
            enumElement.getKind(); result = ElementKind.ENUM;
            nestedEnumElement.getKind(); result = ElementKind.ENUM;
            classElement.getKind(); result = ElementKind.CLASS;
            nestedClassElement.getKind(); result = ElementKind.CLASS;

            // these two let's have enclosed elements
            enumElement.getEnclosedElements(); result = Arrays.asList(interfaceElement, nestedClassElement);
            classElement.getEnclosedElements(); result = Arrays.asList(nestedEnumElement, interfaceElement);
        }};

        List<Reference> result = new ArrayList<Reference>();
        SchemaGenerator sg = new SchemaGenerator();

        Collection<TypeElement> elements = Collections.singletonList(interfaceElement);
        invoke(sg, "filterClass", result, elements);
        assertTrue("Expected no root types to be found. But found: " + result.size(), result.isEmpty());

        elements = Arrays.asList(interfaceElement, enumElement, classElement);
        invoke(sg, "filterClass", result, elements);
        assertTrue("Expected 4 root types to be found. But found: " + result.size(), result.size() == 4);

        new Verifications() {{
            interfaceElement.getEnclosedElements(); maxTimes = 0;
            nestedClassElement.getEnclosedElements(); maxTimes = 1;
            nestedEnumElement.getEnclosedElements(); maxTimes = 1;
        }};
    }
}

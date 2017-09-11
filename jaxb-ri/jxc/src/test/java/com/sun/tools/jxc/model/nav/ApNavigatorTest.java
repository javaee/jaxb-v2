/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jxc.model.nav;

import java.util.Arrays;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author aefimov
 */
@RunWith(JMockit.class)
public final class ApNavigatorTest {

    @Test
    public void testgetEnumConstantsOrder(
            @Mocked final ProcessingEnvironment env,
            @Mocked final TypeElement clazz,
            @Mocked final VariableElement enumElement1,
            @Mocked final VariableElement enumElement2,
            @Mocked final VariableElement enumElement3,
            @Mocked final VariableElement enumElement4 ) throws Exception {

        new Expectations() {
            {
                //The primitiveType is irrelevant for getEnumConstants() operations
                env.getTypeUtils().getPrimitiveType(TypeKind.BYTE); result = (PrimitiveType) null;
                //enumElements needs to return ENUM_CONSTANT for getEnumConstants() to work properly
                enumElement1.getKind(); result = ElementKind.ENUM_CONSTANT;
                enumElement2.getKind(); result = ElementKind.ENUM_CONSTANT;
                enumElement3.getKind(); result = ElementKind.ENUM_CONSTANT;
                enumElement4.getKind(); result = ElementKind.ENUM_CONSTANT;
                //Redefine the hashCode() for test enum's - it will gives us an assurance that
                // all elements will have predifined order when they will be added to incorrect
                // HashSet container in getEnumConstants()
                enumElement1.hashCode(); result = 4;
                enumElement2.hashCode(); result = 3;
                enumElement3.hashCode(); result = 2;
                enumElement4.hashCode(); result = 1;
                //We want to return here a predifined clazz members
                env.getElementUtils().getAllMembers(clazz); result = Arrays.asList(enumElement1,
                                                                         enumElement2, enumElement3, enumElement4);
            }
        };
        //Create ApNavigator instance
        ApNavigator apn = new ApNavigator(env);
        //Filter the enum constants
        VariableElement[] resArr = apn.getEnumConstants(clazz);
        //Check that order is preserved after the getEnumConstants() operation
        assertTrue("Position of first element is changed", resArr[0] == enumElement1);
        assertTrue("Position of second element is changed", resArr[1] == enumElement2);
        assertTrue("Position of third element is changed", resArr[2] == enumElement3);
        assertTrue("Position of fourth element is changed", resArr[3] == enumElement4);

    }
}

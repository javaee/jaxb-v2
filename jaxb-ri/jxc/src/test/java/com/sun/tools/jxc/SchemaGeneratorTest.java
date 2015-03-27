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
package com.sun.tools.jxc;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import static mockit.Deencapsulation.invoke;
import mockit.Mock;
import mockit.MockUp;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author aefimov
 */

@RunWith(JMockit.class)
public final class SchemaGeneratorTest {

    @Test
    public void setClassPathTest() throws Exception {

        // Mocked URL instance that returns incorrect path
        // similar to behaviour on Windows platform
        final URL cUrl = new MockUp<URL>() {
            String path = "C:";

            @Mock
            public String getPath() {
                return "/" + path;
            }

            @Mock
            public URI toURI() {
                return new File(path).toURI();
            }
        }.getMockInstance();

        // Mocked URLClassLoder will return mocked URL
        new MockUp<URLClassLoader>() {
            @Mock
            URL[] getURLs() {
                URL[] urls = {
                    cUrl
                };
                return urls;
            }
        };

        //Mock the 'findJaxbApiJar' in SchemaGenerator class to avoid
        //additional calls to URL class
        new NonStrictExpectations(SchemaGenerator.class) {{
                invoke(SchemaGenerator.class, "findJaxbApiJar"); result = "";
        }};

        //Invoke the method under test
        String result = invoke(SchemaGenerator.class, "setClasspath", "");
        String sepChar = File.pathSeparator;

        // When the URL path problem is fixed the following behaviour is expected:
        // On *nix plarforms the C: path will converted to "test dir path + path separator + C:"
        // On Windows "path separator + C:" will be returned
        // "path separator + /C:" should never be returned on any platform
        assertFalse("Result classpath contains incorrect drive path", result.contains(sepChar+"/C:"));
    }
}

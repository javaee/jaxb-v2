/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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

package jaxb.osgi_test.osgi;

import jaxb.osgi_test.JaxbOsgiTest;
import org.osgi.framework.Bundle;

/**
 * Testing jaxb-osgi bundle jar
 *
 * @author yaroska
 */
public class JaxbOsgiBundleTest extends JaxbOsgiTest {
    private static final String BUNDLE = "com.sun.xml.bind.jaxb-osgi";

    private Bundle bundle;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bundle = getBundle(BUNDLE);
    }

    public void testBundle() {
        checkBundle(bundle);
    }

    public void testLogger() {
        checkClassInBundle("com.sun.istack.logging.Logger", bundle);
    }

    public void testContextFactoryJaxb1() {
        checkClassInBundle("com.sun.xml.bind.ContextFactory_1_0_1", bundle);
    }
}

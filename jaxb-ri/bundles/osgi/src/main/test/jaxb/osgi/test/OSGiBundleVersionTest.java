/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package jaxb.osgi.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import junit.framework.TestCase;

/**
 *
 * @author lukas
 */
public class OSGiBundleVersionTest extends TestCase {

    public OSGiBundleVersionTest(String name) {
        super(name);
    }

    public void testBundleVersion() throws IOException {
        String osgiFolder = System.getProperty("osgi.dist");
        assertNotNull("osgi.dist not set", osgiFolder);
        for (File f : new File(osgiFolder).listFiles(new FF())) {
            System.out.println("Checking: " + f.getAbsolutePath());
            Manifest mf = new JarFile(f).getManifest();
            if (mf == null) {
                System.out.println("No manifest file");
                continue;
            }
            String version = mf.getMainAttributes().getValue("Bundle-Version");
            if (version == null) {
                System.out.println("no 'Bundle-Version' version for: " + f.getName());
                continue;
            }
            assertNotNull(version);
            String[] v = version.split("\\.");
            assertTrue("only <X.Y.Z> is allowed but was: <" + version + ">", v.length <= 3);
            for (String s : v) {
                try {
                    Integer.parseInt(s);
                } catch (Throwable t) {
                    fail("'" + s + "' is not a number");
                }
            }
        }
    }

    private class FF implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".jar");
        }
    }
}

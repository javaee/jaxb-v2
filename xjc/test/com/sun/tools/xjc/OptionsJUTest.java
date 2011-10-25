/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.xjc;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import junit.framework.TestCase;

/**
 *
 * @author Lukas Jungmann
 */
public class OptionsJUTest extends TestCase {

    private Options o;

    public OptionsJUTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        o = new Options();
        o.targetDir = new File(System.getProperty("java.io.tmpdir"), "jxc_optionsTest");
        o.targetDir.mkdirs();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        delDirs(o.targetDir);
    }

    public void testCreateCodeWriter() throws JClassAlreadyExistsException, IOException {
        JCodeModel jcm = new JCodeModel();
        JDefinedClass c = jcm._class("test.TestClass");
        c.constructor(JMod.PUBLIC);
        o.readOnly = false;

        //test UTF-8
        o.encoding = "UTF-8";
        jcm.build(o.createCodeWriter());
        File cls = new File(o.targetDir, "test/TestClass.java");
        FileInputStream fis = new FileInputStream(cls);
        byte[] in = new byte[12];
        fis.read(in);
        fis.close();
        cls.delete();
        String inStr = new String(in, "UTF-8");
        assertTrue("Got: '" + inStr + "'", inStr.endsWith("// This f"));

        //test UTF-16
        o.noFileHeader = true;
        o.encoding = "UTF-16";
        jcm.build(o.createCodeWriter());
        cls = new File(o.targetDir, "test/TestClass.java");
        fis = new FileInputStream(cls);
        in = new byte[22];
        fis.read(in);
        fis.close();
        cls.delete();
        inStr = new String(in, "UTF-16");
        assertTrue("Got: '" + inStr + "'", inStr.endsWith("package t"));

        //test default encoding
        o.noFileHeader = false;
        o.encoding = null;
        jcm.build(o.createCodeWriter());
        cls = new File(o.targetDir, "test/TestClass.java");
        fis = new FileInputStream(cls);
        //this should handle also UTF-32...
        in = new byte[84];
        fis.read(in);
        fis.close();
        cls.delete();
        inStr = new String(in, Charset.defaultCharset());
        assertTrue("Got: '" + inStr + "'", inStr.contains("// This f"));
    }

    private static void delDirs(File... dirs) {
        for (File dir : dirs) {
            if (!dir.exists()) {
                continue;
            }
            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    delDirs(f);
                }
                dir.delete();
            } else {
                dir.delete();
            }
        }
    }
}

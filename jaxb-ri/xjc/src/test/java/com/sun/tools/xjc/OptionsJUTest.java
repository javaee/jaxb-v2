/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.istack.tools.DefaultAuthenticator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        //same string in UTF-8 is 1byte shorter on JDK6 than on JDK5
        //therefore final check is for 'contains' and not for 'endsWith'
        byte[] in = new byte[13];
        fis.read(in);
        fis.close();
        cls.delete();
        String inStr = new String(in, "UTF-8");
        assertTrue("Got: '" + inStr + "'", inStr.contains("// This f"));

        //test UTF-16
        o.noFileHeader = true;
        o.encoding = "UTF-16";
        jcm.build(o.createCodeWriter());
        cls = new File(o.targetDir, "test/TestClass.java");
        fis = new FileInputStream(cls);
        in = new byte[26];
        fis.read(in);
        fis.close();
        cls.delete();
        inStr = new String(in, "UTF-16");
        assertTrue("Got: '" + inStr + "'", inStr.contains("package t"));

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
        inStr = new String(in, Charset.defaultCharset().name());
        assertTrue("Got: '" + inStr + "'", inStr.contains("// This f"));
    }

    public void testProxySettings() throws Exception {
        Options opts = new Options();
        File grammar = File.createTempFile("jaxbproxytest", "xsd");
        grammar.deleteOnExit();

        try {
            opts.parseArguments(new String[]{"-httpproxy", "www.proxy", grammar.getAbsolutePath()});
            assertEquals("www.proxy", getField("proxyHost", opts));
            assertEquals("80", getField("proxyPort", opts));
            assertNull(opts.proxyAuth);
        } catch (BadCommandLineException ex) {
            Logger.getLogger(OptionsJUTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } finally {
            if (opts.proxyAuth != null) {
                DefaultAuthenticator.reset();
            }
        }
        opts = new Options();
        try {
            opts.parseArguments(new String[]{"-httpproxy", "www.proxy1:4321", grammar.getAbsolutePath()});
            assertEquals("www.proxy1", getField("proxyHost", opts));
            assertEquals("4321", getField("proxyPort", opts));
            assertNull(opts.proxyAuth);
        } catch (BadCommandLineException ex) {
            Logger.getLogger(OptionsJUTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } finally {
            if (opts.proxyAuth != null) {
                DefaultAuthenticator.reset();
            }
        }
        opts = new Options();
        try {
            opts.parseArguments(new String[]{"-httpproxy", "user:pwd@www.proxy3:7890", grammar.getAbsolutePath()});
            assertEquals("www.proxy3", getField("proxyHost", opts));
            assertEquals("7890", getField("proxyPort", opts));
            assertEquals("user:pwd", opts.proxyAuth);
        } catch (BadCommandLineException ex) {
            Logger.getLogger(OptionsJUTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } finally {
            if (opts.proxyAuth != null) {
                DefaultAuthenticator.reset();
            }
        }
        opts = new Options();
        try {
            opts.parseArguments(new String[]{"-httpproxy", "duke:s@cr@t@proxy98", grammar.getAbsolutePath()});
            assertEquals("proxy98", getField("proxyHost", opts));
            assertEquals("80", getField("proxyPort", opts));
            assertEquals("duke:s@cr@t", opts.proxyAuth);
        } catch (BadCommandLineException ex) {
            Logger.getLogger(OptionsJUTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } finally {
            if (opts.proxyAuth != null) {
                DefaultAuthenticator.reset();
            }
        }
    }

    public static void delDirs(File... dirs) {
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

    private String getField(String fieldName, Object instance) {
        Field f = null;
        boolean reset = false;
        try {
            f = Options.class.getDeclaredField(fieldName);
            if (!f.isAccessible()) {
                f.setAccessible(reset = true);
            }
            return (String) f.get(instance);
        } catch (Exception ex) {
            Logger.getLogger(OptionsJUTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (reset && f != null) {
                f.setAccessible(false);
            }
        }
        return null;
    }
}

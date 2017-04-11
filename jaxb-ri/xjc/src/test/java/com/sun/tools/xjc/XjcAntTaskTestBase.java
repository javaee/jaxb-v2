/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

import junit.framework.TestCase;
import java.io.*;

/**
 *
 * @author Yan GAO.
 *         Copyright (c) 2017 Oracle and/or its affiliates.
 *         All rights reserved.
 */
public abstract class XjcAntTaskTestBase extends TestCase {
  protected File projectDir;
  protected File srcDir;
  protected File buildDir;
  protected File script;
  protected boolean tryDelete = false;

  public abstract String getBuildScript();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    projectDir = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName() + "-" + getName());
    if (projectDir.exists() && projectDir.isDirectory()) {
      OptionsJUTest.delDirs(projectDir);
    }
    srcDir = new File(projectDir, "src");
    buildDir = new File(projectDir, "build");
    assertTrue("project dir created", projectDir.mkdirs());
    script = copy(projectDir, getBuildScript(), XjcAntTaskTestBase.class.getResourceAsStream("resources/" + getBuildScript()));
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (tryDelete) {
      OptionsJUTest.delDirs(srcDir, buildDir);
      script.delete();
      assertTrue("project dir exists", projectDir.delete());
    }
  }

  protected static File copy(File dest, String name, InputStream is) throws FileNotFoundException, IOException {
    return copy(dest, name, is, null);
  }

  protected static File copy(File dest, String name, InputStream is, String targetEncoding)
      throws FileNotFoundException, IOException {
    File destFile = new File(dest, name);
    OutputStream os = new BufferedOutputStream(new FileOutputStream(destFile));
    Writer w = targetEncoding != null ?
        new OutputStreamWriter(os, targetEncoding) : new OutputStreamWriter(os);
    byte[] b = new byte[4096];
    int len = -1;
    while ((len = is.read(b)) > 0) {
      w.write(new String(b), 0, len);
    }
    w.flush();
    w.close();
    is.close();
    return destFile;
  }

  static boolean is9() {
    return System.getProperty("java.version").startsWith("9");
  }
}

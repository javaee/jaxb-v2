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

package com.sun.tools.xjc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yan GAO (gaoyan.gao@oracle.com)
 */
public class AntExecutor {
    private static boolean DEBUG = Boolean.getBoolean("anttasks.debug");
    private static String DEBUG_PORT = "5432";
    private static boolean PROFILE = Boolean.getBoolean("anttasks.profile");

    public static int exec(File script, String... targets) throws IOException {
        File heapDump = null;
        List<String> cmd = new ArrayList<String>();
        cmd.add("java");
        if (XjcAntTaskTestBase.is9()) {
            cmd.add("--add-modules");
            cmd.add("java.xml.ws");
        }
        if (DEBUG) {
            cmd.add("-Xdebug");
            cmd.add("-Xnoagent");
            cmd.add("-Xrunjdwp:transport=dt_socket,address=" + DEBUG_PORT + ",server=y,suspend=y");
        } else if (PROFILE) {
            heapDump = File.createTempFile(script.getName(), ".hprof", new File(System.getProperty("user.home")));
            cmd.add("-agentlib:hprof=heap=dump,file=" + heapDump.getAbsolutePath() + ",format=b");
        }
        cmd.add("-Dbin.folder=" + System.getProperty("bin.folder"));
//        cmd.add("-Djaxp.debug=true");
        cmd.add("-cp");
        cmd.add(getAntCP(new File(System.getProperty("bin.folder"), "lib/ant")));
        cmd.add("org.apache.tools.ant.Main");
//        cmd.add("-v");
        cmd.add("-f");
        cmd.add(script.getName());
        cmd.addAll(Arrays.asList(targets));
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(script.getParentFile());
        Process p = pb.start();
        new InOut(p.getInputStream(), System.out).start();
        new InOut(p.getErrorStream(), System.out).start();
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            // ignore
        }
        if (PROFILE) {
            System.out.println("Heap dump (in binary format): " + heapDump.getAbsolutePath());
        }
        return p.exitValue();
    }

    private static String getAntCP(File dir) {
        StringBuilder path = new StringBuilder();
        for (File jar : dir.listFiles()) {
            path.append(jar.getAbsolutePath());
            path.append(File.pathSeparator);
        }
        return path.substring(0, path.length() - 1);
    }

    private static class InOut extends Thread {

        private InputStream is;
        private OutputStream out;

        public InOut(InputStream is, OutputStream out) {
            this.is = is;
            this.out = out;
        }
    }
}

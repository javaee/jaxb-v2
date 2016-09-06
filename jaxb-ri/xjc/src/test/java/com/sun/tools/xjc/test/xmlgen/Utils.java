/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.xjc.test.xmlgen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * XML utilities.
 */
public class Utils {
    
    /**
     * Builds arguments {@code Map} from {@code [<key>, <value>]} pairs passed as {@code String[2]} arrays.
     * @param arguments {@code [<key>, <value>]} pairs passed as {@code String[2]} arrays.
     * @return {@code Map} with arguments or {@code null} if no arguments pairs were passed.
     */
    static Map<String, String> buildArgumentsMap(final String[]... arguments) {
        if (arguments == null) {
            return null;
        }
        Map<String, String> argsMap = new HashMap<>(arguments.length);
        for (String[] argument : arguments) {
            if (argument.length == 2) {
                argsMap.put(argument[0], argument[1]);
            } else {
                throw new IllegalArgumentException("XML element arguments array must be of size 2: {<name>, <url>}");
            }
        }
        return argsMap;
    }

    /**
     * Gets Java home directory.
     */
    static String getJavaHome() {
        return System.getProperty("java.home");
    }

    /**
     * Gets Java compiler path.
     * @return Java compiler path.
     */
    public static String getJavac() {
        final String javaHome =  getJavaHome();
        final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        final StringBuilder sb = new StringBuilder(
                (isWindows ? 4 : 0) +
                javaHome.length() + 3 + 5 + 2*File.separator.length());
        sb.append(javaHome);
        sb.append(File.separator);
        sb.append("bin");
        sb.append(File.separator);
        sb.append("javac");
        if (isWindows) {
            sb.append(".exe");
        }
        final String javacPath = sb.toString();
        final File javac = new File(javacPath);
        if (javac.canExecute()) {
            return javacPath;
        }
        return isWindows ? "javac.exe" : "javac";
    }

}

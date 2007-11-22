/*
 * $Id: GoldenFileProcessor.java,v 1.9 2007-11-22 00:53:14 kohsuke Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineParser;

/**
 * Checks the build execution result against a golden file.
 *
 * @author <ul>
 *         <li>Ryan Shoemaker, Sun Microsystems, Inc.</li>
 *         </ul>
 * @version $Revision: 1.9 $
 */
public class GoldenFileProcessor implements Processor {
    public boolean process(File dir, boolean verbose) {
        File buildDotOut = new File(dir, "build.out");
        File buildDotGoldenRegexp = new File(dir, "build.golden.regexp");

        if(!buildDotGoldenRegexp.exists()) {
            trace("FAILED: Golden file missing : "+buildDotGoldenRegexp, verbose);
            return false;
        }

        boolean match = match(buildDotOut, buildDotGoldenRegexp);
        if(match) {
            trace("PASSED: output matches golden file", verbose);
            return true;
        } else {
            trace("FAILED: output differs from golden file", verbose);
            return false;
        }
    }

    /**
     * return true if the contents of file matches the multi-line
     * regular expression in template.
     *
     * @param file source file
     * @param template mnulti-line regular expression template
     * @return true if the file matches the regular expression, false otherwise
     */
    boolean match(File file, File template) {
//        return Pattern
//            .compile(getFileAsString(template), Pattern.MULTILINE)
//            .matcher(getFileAsString(file))
//            .matches();

        // braindead line-by-line pattern match to narrow down the
        // cause of unit test failures
        try {
            BufferedReader f = new BufferedReader(new FileReader(file));
            BufferedReader t = new BufferedReader(new FileReader(template));

            String fLine;
            String tLine;
            int line = 0;
            boolean matches = true;
            do {
                fLine = f.readLine();
                tLine = t.readLine();
                if (fLine == null || tLine == null)
                    break;
                line++;
                try {
                    matches = Pattern.compile(tLine).matcher(fLine).matches();
                } catch (Exception e) {
                    return false;
                }
            } while (matches);
            if (!matches) {
                System.out.println("error: line " + line + " doesn't match");
                System.out.println("regexp: " + stripBackslashes(tLine));
                System.out.println("xmlout: " + fLine);
                System.out.println("Complete trace of build output:\n>>>>>>>>>>");
                System.out.println(getFileAsString(file, true));
                System.out.println("<<<<<<<<<<");
            }

            return matches;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return true;
    }

    private String stripBackslashes(String line) {
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if(c != '\\')
                s.append(c);
        }
        return s.toString();
    }

    /**
     * return the contents of the file as a String
     *
     * @param f source file
     * @param lineNumbers true if you want to prepend line numbers to the output
     * @return contents of f as a String
     */
    String getFileAsString(File f, boolean lineNumbers) {
        int lineNo = 1;
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while (line != null) {
                if(lineNumbers) sb.append(String.format("%4d: ", lineNo++));
                sb.append(line).append('\n');
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void addCmdLineOptions(CmdLineParser parser) {
        // no-op
    }

    private void trace(String msg, boolean verbose) {
        if(verbose) {
            System.out.println("GoldenFileProcessor: " + msg);
        }
    }
}

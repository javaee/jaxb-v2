/*
 * $Id: RegExpHelper.java,v 1.3 2007-11-22 00:53:14 kohsuke Exp $
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
package processor.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * This helper class is used to process the output from a sample app and help
 * generate a multi-line regular expression that will be used during automated
 * runs of the sample apps.
 * 
 * @author <ul>
 *         <li>Ryan Shoemaker, Sun Microsystems, Inc.</li>
 *         </ul>
 * @version $Revision: 1.3 $
 */
public class RegExpHelper {

    private File infile;

    /**
     * @param infile
     *      the name of the input file
     */
    public RegExpHelper(String infile) {
        this.infile = new File(infile);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println(
                "usage: java processor.util.RegExpHelper </path/to/build.out>");
            System.exit(-1);
        }

        RegExpHelper regEx = new RegExpHelper(args[0]);
        regEx.escapeFile();

        exitReminder();
    }

    /**
     *  
     */
    private void escapeFile() throws IOException {
        File outFile = new File(infile.getParentFile(), "build.golden.regexp");

        BufferedReader br = new BufferedReader(new FileReader(infile));
        BufferedWriter bw =
            new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile)));

        CharacterEscapeHandler escHandler = RegExpEscapeHandler.theInstance;

        System.out.println("Generating: " + outFile.getPath());

        String line = br.readLine();
        while (line != null) {
            char[] chars = line.toCharArray();
            escHandler.escape(chars, 0, chars.length, bw);
            bw.newLine();
            line = br.readLine();
        }

        System.out.println("done.\n\n");
        br.close();
        bw.close();
    }

    /**
     *  
     */
    private static void exitReminder() {
        System.out.println(
            "Please edit the generated output and replace any dynamic\n"
                + "content with the appropriate regular expression.  For\n"
                + "example:\n"
                + "replace \"D\\:\\files\\jaxb\\ws\\jaxb-ri\\samples\\work\\unmarshal-read\\gen-src\\\" with \".*\"\n"
                + "replace \"Total time\\: 11 seconds\" with \"Total time\\: .* seconds\"\n"
                + "Don't forget to check build.regexp into the workspace.");
    }

}

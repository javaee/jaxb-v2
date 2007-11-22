/*
 * $Id: ValidatingProcessor.java,v 1.3 2007-11-22 00:53:14 kohsuke Exp $
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

import java.io.File;
import java.net.URL;

import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.BooleanOption;

import com.thaiopensource.relaxng.jarv.RelaxNgCompactSyntaxVerifierFactory;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * @author <ul>
 *         <li>Ryan Shoemaker, Sun Microsystems, Inc.</li>
 *         </ul>
 * @version $Revision: 1.3 $
 */
public class ValidatingProcessor implements Processor {

    // command-line options for validation
    public BooleanOption isValidating = new BooleanOption("-validating", false);

    // pointer to the sample app meta data schema
    public static final org.iso_relax.verifier.Schema descriptorSchema;

    // the name of the meta file we're searching for
    private static final String META_FILE_NAME = "sample.meta";

    // validator
    private final Verifier verifier;

    /**
     *  
     */
    public ValidatingProcessor() {
        try {
            verifier = descriptorSchema.newVerifier();
        } catch (VerifierConfigurationException e) {
            // impossible
            e.printStackTrace();
            throw new InternalError();
        }
        // use the error handler that reports errors to console
        verifier.setErrorHandler(new ErrorHandlerImpl());
    }

    /*
     * (non-Javadoc)
     * 
     * @see processor.Processor#process(java.io.File)
     */
    public boolean process(File dir, boolean verbose) {
        boolean continueProcessing = true;

        if (isValidating.value) {
            // find the meta data file in dir
            // this can't be null - the driver only passes in directories
            // containing sample.meta files
            File metaFile =
                SampleProcessorDriver.getMetaFile(dir, META_FILE_NAME);

            try {
                // check the meta data file for validation errors
                if (verifier.verify(metaFile)) {
                    trace("sample.meta is valid", verbose);
                } else {
                    trace("sample.meta is invalid - terminating chain", true);
                    continueProcessing = false;
                }
            } catch (Exception e) {
                continueProcessing = false;
                trace(
                    "ERROR validating sample.meta - terminating chain\ncause: "
                        + e,
                    true);
                e.printStackTrace();
            }
        }

        return continueProcessing;
    }

    private void trace(String msg, boolean verbose) {
        if (verbose)
            System.out.println("ValidatingProcessor: " + msg);
    }

    public void addCmdLineOptions(CmdLineParser parser) {
        parser.addOption(isValidating);
    }

    static {
        URL url = ValidatingProcessor.class.getResource("sample.rnc");
        if (url == null)
            throw new InternalError("unable to find sample.rnc");
        try {
            descriptorSchema =
                new RelaxNgCompactSyntaxVerifierFactory().compileSchema(
                    url.toExternalForm());
        } catch (Exception e) {
            // e.printStackTrace();
            throw new InternalError("unable to parse sample.rnc\ncause: " + e);
        }
    }

}

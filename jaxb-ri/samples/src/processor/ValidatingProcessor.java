/*
 * $Id: ValidatingProcessor.java,v 1.1 2005-04-15 20:07:44 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * @version $Revision: 1.1 $
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

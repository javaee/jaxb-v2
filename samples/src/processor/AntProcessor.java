/*
 * $Id: AntProcessor.java,v 1.2 2005-09-10 19:08:22 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package processor;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import com.sun.tools.xmlpp.PrettyPrintResult;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.StringOption;

/**
 * Generates a build script for a sample app.
 * 
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Revision: 1.2 $
 */
public class AntProcessor implements Processor {

    // command-line options for ant processing
    public StringOption antTarget = new StringOption("-ant");

    // xsl transform file name
    private static final String XSLT_FILE_NAME = "build-script.xsl";

    // the name of the meta file we're searching for
    private static final String META_FILE_NAME = "sample.meta";

    private final Transformer transformer;

    AntProcessor() {
        // avoid using Apache xalan due to bugid 5007398 filed by Kohsuke
        System.setProperty(
                "javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(StreamSource.FEATURE)) {
            throw new TransformerFactoryConfigurationError(
                "Error: "
                    + tf.getClass().getName()
                    + " doesn't support StreamSource");
        }
        InputStream xsltFile =
            AntProcessor.class.getResourceAsStream(XSLT_FILE_NAME);
        if(xsltFile==null)
            throw new TransformerFactoryConfigurationError("Unable to find "+XSLT_FILE_NAME);
        try {
            transformer = tf.newTransformer(new StreamSource(xsltFile));
            System.out.println("Transformer is: "+transformer.getClass().getName());
        } catch (TransformerConfigurationException e) {
            // must be a bug of the stylesheet
            e.printStackTrace();
            throw new TransformerFactoryConfigurationError(e);
        }
    }

    /*
     * Look for sample.meta in the specified directory and then apply a
     * transform that generates an Ant build.xml for the sample.
     * 
     * @see processor.Processor#process(java.util.List)
     */
    public boolean process(File dir, boolean verbose) {
        boolean continueProcessing = true;

        // find the meta data file in dir
        // this can't be null - the driver only passes in directories
        // containing sample.meta files
        File metaFile = SampleProcessorDriver.getMetaFile(dir, META_FILE_NAME);

        // process it
        try {
            // trace info
            trace("generating build.xml", verbose);

            // setup source and result
            StreamSource source = new StreamSource(metaFile);
            PrettyPrintResult result =
                new PrettyPrintResult(
                    new FileWriter(
                        metaFile.getParent()
                            + File.separatorChar
                            + "build.xml"));
            
            transformer.setParameter("target",antTarget.value);
            
            // run the transform
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
            continueProcessing = false;
        }

        return continueProcessing;
    }

    private void trace(String msg, boolean verbose) {
        if(verbose)
            System.out.println("AntProcessor: " + msg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see processor.Processor#addCmdLineOptions(org.kohsuke.args4j.CmdLineParser)
     */
    public void addCmdLineOptions(CmdLineParser parser) {
        parser.addOption(antTarget);
    }

}

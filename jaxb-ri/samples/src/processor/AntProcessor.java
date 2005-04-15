/*
 * $Id: AntProcessor.java,v 1.1 2005-04-15 20:07:43 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * @version $Revision: 1.1 $
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

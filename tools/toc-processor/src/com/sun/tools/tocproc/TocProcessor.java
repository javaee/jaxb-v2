/*
 * $Id: TocProcessor.java,v 1.2 2005-09-10 19:08:40 kohsuke Exp $
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
package com.sun.tools.tocproc;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;

/**
 * This is a specialized processor that generates a toc navbar in each of the
 * .html files in the jaxb release notes.
 * 
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Revision: 1.2 $
 */
public class TocProcessor {

    /** the directory containing html files to process */
    private File sourceDir;

    /** the destination directory */
    private String destDirName;

    /** xml file describing the toc navigation bar */
    private File tocDotXml;

    /** the name of the toc style sheet */
    private final String tocDotXsl = "toc.xsl";

    public TocProcessor(String[] args) {
        if (args.length != 1)
            usage();

        sourceDir = new File(args[0]);
        tocDotXml = new File(sourceDir, "toc.xml");

        destDirName = args[0];
    }

    private void transform(String fileName, Node node) {
        TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(StreamSource.FEATURE)) {
            throw new TransformerFactoryConfigurationError(
                "Error: "
                    + tf.getClass().getName()
                    + " doesn't support StreamSource");
        }

        InputStream xsltFile =
            TocProcessor.class.getResourceAsStream(tocDotXsl);

        try {
            StreamSource xsltSource = new StreamSource(xsltFile);
            // workaround for xalan bugid: 5008888
            xsltSource.setSystemId("blarg");
            
            Transformer transformer =
                tf.newTransformer(xsltSource);

            // setup source and result
            DOMSource source = new DOMSource(node);
            
            
            StreamResult result =
                new StreamResult(
                    new FileWriter(destDirName + File.separator + fileName));
            
            transformer.setParameter("tocDotXml", tocDotXml.toURL().toExternalForm());

            // run the transform
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            // must be a bug of the stylesheet
            e.printStackTrace();
            throw new TransformerFactoryConfigurationError(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int run() {
        try {
            DOMParser parser = new DOMParser();
            parser.setFeature(
                "http://cyberneko.org/html/features/balance-tags",
                true);

            File[] htmlFiles = sourceDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".html");
                }
            });

            for (int i = 0; i < htmlFiles.length; i++) {
                File file = htmlFiles[i];
                parser.parse(file.getCanonicalPath());
                transform(file.getName(), parser.getDocument());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void usage() {
        System.out.println("java TocProcessor <dir>");
        System.out.println("\t<dir> must contain toc.xml");
        System.exit(-1);
    }

    public static void main(String[] args) {
        (new TocProcessor(args)).run();
    }
}

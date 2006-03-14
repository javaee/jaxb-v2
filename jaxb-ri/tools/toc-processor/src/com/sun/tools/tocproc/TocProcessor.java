/*
 * $Id: TocProcessor.java,v 1.5 2006-03-14 23:21:51 kohsuke Exp $
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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.cyberneko.html.parsers.DOMParser;

/**
 * This is a specialized processor that generates a toc navbar in each of the
 * .html files in the jaxb release notes.
 *
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Revision: 1.5 $
 */
public class TocProcessor {

    /** the directory containing html files to process */
    private File sourceDir;

    /** the destination directory */
    private String destDirName;

    /** xml file describing the toc navigation bar */
    private File tocDotXml;

    /** build.properties file. */
    private File buildProperties;

    /** the name of the toc style sheet */
    private final String tocDotXsl = "toc.xsl";

    public TocProcessor(String[] args) {
        if (args.length != 2)
            usage();

        buildProperties = new File(args[0]);
        destDirName = args[1];
        sourceDir = new File(destDirName);
        tocDotXml = new File(sourceDir, "toc.xml");

    }

    private void transform(String fileName, Source source) {
        TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(StreamSource.FEATURE)) {
            throw new TransformerFactoryConfigurationError(
                "Error: "
                    + tf.getClass().getName()
                    + " doesn't support StreamSource");
        }

        URL xsltFile =
            TocProcessor.class.getResource(tocDotXsl);

        try {
            StreamSource xsltSource = new StreamSource(xsltFile.toExternalForm());

            Transformer transformer =
                tf.newTransformer(xsltSource);

            StreamResult result =
                new StreamResult(
                    new FileWriter(destDirName + File.separator + fileName));
            
            transformer.setParameter("tocDotXml", tocDotXml.toURL().toExternalForm());
            transformer.setParameter("fileName", fileName);

            // read build.properties and make everything available
            Properties prop = new Properties();
            prop.load(new FileInputStream(buildProperties));
            for (Map.Entry<Object,Object> entry : prop.entrySet()) {
                transformer.setParameter(entry.getKey().toString(),entry.getValue().toString());
            }

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
                transform(file.getName(), new DOMSource(parser.getDocument(),file.getPath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void usage() {
        System.out.println("java TocProcessor <path to $JAXB_HOME/build.properties> <dir>");
        System.out.println("\t<dir> must contain toc.xml");
        System.exit(-1);
    }

    public static void main(String[] args) {
        (new TocProcessor(args)).run();
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.txw2;

import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.SingleStreamCodeWriter;
import com.sun.tools.txw2.model.NodeSet;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.BooleanOption;
import org.kohsuke.args4j.opts.StringOption;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * Programatic entry point to the TXW compiler.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Main {
    private final TxwOptions opts;

    public Main(TxwOptions opts) {
        this.opts = opts;
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static class Options {
        public StringOption output = new StringOption("-o");
        public StringOption pkg = new StringOption("-p");
        public BooleanOption compact = new BooleanOption("-c");
        public BooleanOption xml = new BooleanOption("-x");
        public BooleanOption xsd = new BooleanOption("-xsd");
        public BooleanOption chain = new BooleanOption("-h");
    }

    public static int run(String[] args) {
        Options opts = new Options();
        CmdLineParser parser = new CmdLineParser();
        parser.addOptionClass(opts);

        try {
            parser.parse(args);
        } catch (CmdLineException e) {
            System.out.println(e.getMessage());
            printUsage();
            return 1;
        }

        TxwOptions topts = new TxwOptions();
        topts.errorListener = new ConsoleErrorReporter(System.out);

        if(opts.output.value!=null) {
            try {
                topts.codeWriter = new FileCodeWriter(new File(opts.output.value));
            } catch( IOException e ) {
                System.out.println(e.getMessage());
                printUsage();
                return 1;
            }
        } else {
            topts.codeWriter = new SingleStreamCodeWriter(System.out);
        }

        if(opts.chain.isOn()) {
            topts.chainMethod = true;
        }

        if(opts.pkg.value!=null) {
            topts._package = topts.codeModel._package(opts.pkg.value);
        } else {
            topts._package = topts.codeModel.rootPackage();
        }

        // make sure that there's only one argument (namely the schema)
        if(parser.getArguments().size()!=1) {
            printUsage();
            return 1;
        }

        try {
            topts.source = makeSourceSchema(parser,opts,topts.errorListener);
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
            printUsage();
            return 1;
        }

        return run(topts);
    }

    /**
     * Parses the command line and makes a {@link Parseable} object
     * out of the specified schema file.
     */
    private static SchemaBuilder makeSourceSchema(CmdLineParser parser, Options opts, ErrorHandler eh) throws MalformedURLException {
        File f = new File((String)parser.getArguments().get(0));
        final InputSource in = new InputSource(f.toURL().toExternalForm());

        if(opts.xsd.isOff() && opts.xml.isOff() && opts.compact.isOff()) {
            // auto detect
            if(in.getSystemId().endsWith(".rnc"))
                opts.compact.value=true;
            else
            if(in.getSystemId().endsWith(".rng"))
                opts.xml.value=true;
            else
                opts.xsd.value=true;
        }

        if(opts.xsd.isOn())
            return new XmlSchemaLoader(in);

        final Parseable parseable = makeRELAXNGSource(opts, in, eh, f);

        return new RELAXNGLoader(parseable);
    }

    private static Parseable makeRELAXNGSource(Options opts, final InputSource in, ErrorHandler eh, File f) {
        if(opts.compact.isOn())
            return new CompactParseable(in,eh);

        if(opts.xml.isOn())
            return new SAXParseable(in,eh);

        // otherwise sniff from the file extension
        if(f.getPath().toLowerCase().endsWith("rnc"))
            return new CompactParseable(in,eh);
        else
            return new SAXParseable(in,eh);
    }

    private static void printUsage() {
        System.out.println("Typed Xml Writer ver."+getVersion());
        System.out.println(
            "txw <schema file>\n"+
            " -o <dir>   : Specify the directory to place generated source files\n"+
            " -p <pkg>   : Specify the Java package to put the generated classes into\n"+
            " -c         : The input schema is written in the RELAX NG compact syntax\n"+
            " -x         : The input schema is written in the RELAX NG XML syntax\n"+
            " -xsd       : The input schema is written in the XML SChema\n"+
            " -h         : Generate code that allows method invocation chaining\n"
        );
    }

    public static int run(TxwOptions opts) {
        return new Main(opts).run();
    }

    private int run() {
        try {
            NodeSet ns = opts.source.build(opts);
            ns.write(opts);
            opts.codeModel.build(opts.codeWriter);
            return 0;
        } catch (IOException e) {
            opts.errorListener.error(new SAXParseException(e.getMessage(),null,e));
            return 1;
        } catch (IllegalSchemaException e) {
            opts.errorListener.error(new SAXParseException(e.getMessage(),null,e));
            return 1;
        } catch (SAXParseException e) {
            opts.errorListener.error(e);
            return 1;
        } catch (SAXException e) {
            opts.errorListener.error(new SAXParseException(e.getMessage(),null,e));
            return 1;
        }
    }


    /**
     * Gets the version number of TXW.
     */
    public static String getVersion() {
        try {
            Properties p = new Properties();
            p.load(Main.class.getResourceAsStream("version.properties"));
            return p.get("version").toString();
        } catch( Throwable _ ) {
            return "unknown";
        }
    }

}

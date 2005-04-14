package com.sun.tools.txw2;

import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.SingleStreamCodeWriter;
import com.sun.tools.txw2.builder.SchemaBuilderImpl;
import com.sun.tools.txw2.model.Leaf;
import com.sun.tools.txw2.model.NodeSet;
import org.kohsuke.rngom.ast.util.CheckingSchemaBuilder;
import org.kohsuke.rngom.dt.CascadingDatatypeLibraryFactory;
import org.kohsuke.rngom.dt.builtin.BuiltinDatatypeLibraryFactory;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.kohsuke.args4j.opts.StringOption;
import org.kohsuke.args4j.opts.BooleanOption;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
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

    private static class Options {
        public StringOption output = new StringOption("-o");
        public StringOption pkg = new StringOption("-p");
        public BooleanOption compact = new BooleanOption("-c");
        public BooleanOption xml = new BooleanOption("-x");
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
    private static Parseable makeSourceSchema(CmdLineParser parser, Options opts, ErrorHandler eh) throws MalformedURLException {
        File f = new File((String)parser.getArguments().get(0));
        InputSource in = new InputSource(f.toURL().toExternalForm());

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
            " -h         : Generate code that allows method invocation chaining\n"
        );
    }
//        // TODO: i18n
//        opts.addOption("o","output",true,"Specify the directory to place generated source files");
//        opts.addOption("p","package",true,"Specify the Java package to put the generated classes into");
//        opts.addOption("c","compact",false,"The input schema is written in the RELAX NG compact syntax");
//        opts.addOption("x","xml",false,"The input schema is written in the RELAX NG XML syntax");
//        opts.addOption("h","chain",false,"Generate code that allows method invocation chaining.");
//        opts.addOption("t","target",true,"Specify the target JDK version that the generated code will run (1.2/1.4/5.0).");
//        CommandLine line;

    public static int run(TxwOptions opts) {
        return new Main(opts).run();
    }

    private int run() {
        try {
            NodeSet ns = parseSchema(opts.source);
            ns.write(opts);
            opts.codeModel.build(opts.codeWriter);
            return 0;
        } catch (IOException e) {
            opts.errorListener.error(new SAXParseException(e.getMessage(),null,e));
            return 1;
        } catch (IllegalSchemaException e) {
            opts.errorListener.error(new SAXParseException(e.getMessage(),null,e));
            return 1;
        }
    }

    private NodeSet parseSchema( Parseable source ) throws IllegalSchemaException {
        // parse
        SchemaBuilderImpl stage1 = new SchemaBuilderImpl(opts.codeModel);
        Leaf pattern = (Leaf)source.parse(new CheckingSchemaBuilder(stage1,opts.errorListener,
            new CascadingDatatypeLibraryFactory(
                new BuiltinDatatypeLibraryFactory(new DatatypeLibraryLoader()),
                new DatatypeLibraryLoader())));

        return new NodeSet(opts,pattern);
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

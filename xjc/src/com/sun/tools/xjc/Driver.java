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
package com.sun.tools.xjc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import com.sun.codemodel.writer.ZipCodeWriter;
import com.sun.tools.xjc.generator.bean.BeanGenerator;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.gbind.Expression;
import com.sun.tools.xjc.reader.gbind.Graph;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.xmlschema.ExpressionBuilder;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.tools.xjc.util.NullStream;
import com.sun.tools.xjc.util.Util;
import com.sun.tools.xjc.writer.SignatureWriter;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * CUI of XJC.
 */
public class Driver {

    public static void main(final String[] args) throws Exception {
        // use the platform default proxy if available.
        // see sun.net.spi.DefaultProxySelector for details.
        try {
            System.setProperty("java.net.useSystemProxies","true");
        } catch (SecurityException e) {
            // failing to set this property isn't fatal
        }

        if( Util.getSystemProperty(Driver.class,"noThreadSwap")!=null )
            _main(args);    // for the ease of debugging

        // run all the work in another thread so that the -Xss option
        // will take effect when compiling a large schema. See
        // http://developer.java.sun.com/developer/bugParade/bugs/4362291.html
        final Throwable[] ex = new Throwable[1];

        Thread th = new Thread() {
            public void run() {
                try {
                    _main(args);
                } catch( Throwable e ) {
                    ex[0]=e;
                }
            }
        };
        th.start();
        th.join();

        if(ex[0]!=null) {
            // re-throw
            if( ex[0] instanceof Exception )
                throw (Exception)ex[0];
            else
                throw (Error)ex[0];
        }
    }

    private static void _main( String[] args ) throws Exception {
        try {
            System.exit(run( args, System.err, System.out ));
        } catch (BadCommandLineException e) {
            // there was an error in the command line.
            // print usage and abort.
            if(e.getMessage()!=null) {
                System.out.println(e.getMessage());
                System.out.println();
            }

            usage( false );
            System.exit(-1);
        }
    }



    /**
     * Performs schema compilation and prints the status/error into the
     * specified PrintStream.
     *
     * <p>
     * This method could be used to trigger XJC from other tools,
     * such as Ant or IDE.
     *
     * @param    args
     *      specified command line parameters. If there is an error
     *      in the parameters, {@link BadCommandLineException} will
     *      be thrown.
     * @param    status
     *      Status report of the compilation will be sent to this object.
     *      Useful to update users so that they will know something is happening.
     *      Only ignorable messages should be sent to this stream.
     *
     *      This parameter can be null to suppress messages.
     *
     * @param    out
     *      Various non-ignorable output (error messages, etc)
     *      will go to this stream.
     *
     * @return
     *      If the compiler runs successfully, this method returns 0.
     *      All non-zero values indicate an error. The error message
     *      will be sent to the specified PrintStream.
     */
    public static int run(String[] args, final PrintStream status, final PrintStream out)
        throws Exception {

        class Listener extends XJCListener {
            ConsoleErrorReporter cer = new ConsoleErrorReporter(out==null?new PrintStream(new NullStream()):out);

            public void generatedFile(String fileName) {
                message(fileName);
            }
            public void message(String msg) {
                if(status!=null)
                    status.println(msg);
            }

            public void error(SAXParseException exception) {
                cer.error(exception);
            }

            public void fatalError(SAXParseException exception) {
                cer.fatalError(exception);
            }

            public void warning(SAXParseException exception) {
                cer.warning(exception);
            }

            public void info(SAXParseException exception) {
                cer.info(exception);
            }
        }

        return run(args,new Listener());
    }

    /**
     * Performs schema compilation and prints the status/error into the
     * specified PrintStream.
     *
     * <p>
     * This method could be used to trigger XJC from other tools,
     * such as Ant or IDE.
     *
     * @param    args
     *        specified command line parameters. If there is an error
     *        in the parameters, {@link BadCommandLineException} will
     *        be thrown.
     * @param    listener
     *      Receives messages from XJC reporting progress/errors.
     *
     * @return
     *      If the compiler runs successfully, this method returns 0.
     *      All non-zero values indicate an error. The error message
     *      will be sent to the specified PrintStream.
     */
    public static int run(String[] args, XJCListener listener) throws BadCommandLineException {

        // recognize those special options before we start parsing options.
        for (String arg : args) {
            if (arg.equals("-help")) {
                usage(false);
                return -1;
            }
            if (arg.equals("-version")) {
                listener.message(Messages.format(Messages.VERSION));
                return -1;
            }
            if (arg.equals("-private")) {
                usage(true);
                return -1;
            }
        }

        final OptionsEx opt = new OptionsEx();
        opt.setSchemaLanguage(Language.XMLSCHEMA);  // disable auto-guessing
        opt.parseArguments(args);

        // display a warning if the user specified the default package
        // this should work, but is generally a bad idea
        if(opt.defaultPackage != null && opt.defaultPackage.length()==0) {
            listener.message(Messages.format(Messages.WARNING_MSG, Messages.format(Messages.DEFAULT_PACKAGE_WARNING)));
        }


        // set up the context class loader so that the user-specified classes
        // can be loaded from there
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
            opt.getUserClassLoader(contextClassLoader));

        // parse a grammar file
        //-----------------------------------------
        try {
            if( !opt.quiet ) {
                listener.message(Messages.format(Messages.PARSING_SCHEMA));
            }

            ErrorReceiver receiver = new ErrorReceiverFilter(listener) {
                public void info(SAXParseException exception) {
                    if(opt.verbose)
                        super.info(exception);
                }
                public void warning(SAXParseException exception) {
                    if(!opt.quiet)
                        super.warning(exception);
                }
            };

            if( opt.mode==Mode.FOREST ) {
                // dump DOM forest and quit
                ModelLoader loader  = new ModelLoader( opt, new JCodeModel(), receiver );
                try {
                    DOMForest forest = loader.buildDOMForest(new XMLSchemaInternalizationLogic());
                    forest.dump(System.out);
                    return 0;
                } catch (SAXException e) {
                    // the error should have already been reported
                } catch (IOException e) {
                    receiver.error(e);
                }

                return -1;
            }

            if( opt.mode==Mode.GBIND ) {
                try {
                    XSSchemaSet xss = new ModelLoader(opt, new JCodeModel(), receiver).loadXMLSchema();
                    Iterator<XSComplexType> it = xss.iterateComplexTypes();
                    while (it.hasNext()) {
                        XSComplexType ct =  it.next();
                        XSParticle p = ct.getContentType().asParticle();
                        if(p==null)     continue;

                        Expression tree = ExpressionBuilder.createTree(p);
                        System.out.println("Graph for "+ct.getName());
                        System.out.println(tree.toString());
                        Graph g = new Graph(tree);
                        System.out.println(g.toString());
                        System.out.println();
                    }
                    return 0;
                } catch (SAXException e) {
                    // the error should have already been reported
                }
                return -1;
            }
            
            Model model = ModelLoader.load( opt, new JCodeModel(), receiver );

            if (model == null) {
                listener.message(Messages.format(Messages.PARSE_FAILED));
                return -1;
            }

            if( !opt.quiet ) {
                listener.message(Messages.format(Messages.COMPILING_SCHEMA));
            }

            switch (opt.mode) {
            case SIGNATURE :
                try {
                    SignatureWriter.write(
                        BeanGenerator.generate(model,receiver),
                        new OutputStreamWriter(System.out));
                    return 0;
                } catch (IOException e) {
                    receiver.error(e);
                    return -1;
                }

            case CODE :
            case DRYRUN :
            case ZIP :
                {
                    // generate actual code
                    receiver.debug("generating code");
                    {// don't want to hold outline in memory for too long.
                        Outline outline = model.generateCode(opt,receiver);
                        if(outline==null) {
                            listener.message(
                                Messages.format(Messages.FAILED_TO_GENERATE_CODE));
                            return -1;
                        }

                        listener.compiled(outline);
                    }

                    if( opt.mode == Mode.DRYRUN )
                        break;  // enough

                    // then print them out
                    try {
                        CodeWriter cw;
                        if( opt.mode==Mode.ZIP ) {
                            OutputStream os;
                            if(opt.targetDir.getPath().equals("."))
                                os = System.out;
                            else
                                os = new FileOutputStream(opt.targetDir);

                            cw = createCodeWriter(new ZipCodeWriter(os));
                        } else
                            cw = createCodeWriter(opt.targetDir,opt.readOnly);

                        if( !opt.quiet ) {
                            cw = new ProgressCodeWriter(cw,listener);
                        }
                        model.codeModel.build(cw);
                    } catch (IOException e) {
                        receiver.error(e);
                        return -1;
                    }

                    break;
                }
            default :
                assert false;
            }

            return 0;
        } catch( StackOverflowError e ) {
            if(opt.verbose)
                // in the debug mode, propagate the error so that
                // the full stack trace will be dumped to the screen.
                throw e;
            else {
                // otherwise just print a suggested workaround and
                // quit without filling the user's screen
                listener.message(Messages.format(Messages.STACK_OVERFLOW));
                return -1;
            }
        }
    }

    public static String getBuildID() {
        return Messages.format(Messages.BUILD_ID);
    }


    /**
     * Operation mode.
     */
    private static enum Mode {
        // normal mode. compile the code
        CODE,

        // dump the signature of the generated code
        SIGNATURE,

        // dump DOMForest
        FOREST,

        // same as CODE but don't produce any Java source code
        DRYRUN,

        // same as CODE but pack all the outputs into a zip and dumps to stdout
        ZIP,

        // testing a new binding mode
        GBIND
    }

    
    /**
     * Command-line arguments processor.
     * 
     * <p>
     * This class contains options that only make sense
     * for the command line interface.
     */
    static class OptionsEx extends Options
    {
        /** Operation mode. */
        protected Mode mode = Mode.CODE;
        
        /** A switch that determines the behavior in the BGM mode. */
        public boolean noNS = false;
        
        /** Parse XJC-specific options. */
        protected int parseArgument(String[] args, int i) throws BadCommandLineException {
            if (args[i].equals("-noNS")) {
                noNS = true;
                return 1;
            }
            if (args[i].equals("-mode")) {
                i++;
                if (i == args.length)
                    throw new BadCommandLineException(
                        Messages.format(Messages.MISSING_MODE_OPERAND));

                String mstr = args[i].toLowerCase();

                for( Mode m : Mode.values() ) {
                    if(m.name().toLowerCase().startsWith(mstr) && mstr.length()>2) {
                        mode = m;
                        return 2;
                    }
                }

                throw new BadCommandLineException(
                    Messages.format(Messages.UNRECOGNIZED_MODE, args[i]));
            }
            
            return super.parseArgument(args, i);
        }
    }


    /**
     * Prints the usage screen and exits the process.
     */
    protected static void usage( boolean privateUsage ) {
        if( privateUsage ) {
            System.out.println(Messages.format(Messages.DRIVER_PRIVATE_USAGE));
        } else {
            System.out.println(Messages.format(Messages.DRIVER_PUBLIC_USAGE));
        }
        
        if( Options.allPlugins.size()!=0 ) {
            System.out.println(Messages.format(Messages.ADDON_USAGE));
            for (Plugin p : Options.allPlugins) {
                System.out.println(p.getUsage());
            }
        }
    }
    
    
    /**
     * Creates a configured CodeWriter that produces files into the specified directory.
     */
    public static CodeWriter createCodeWriter(File targetDir, boolean readonly ) throws IOException {
        return createCodeWriter(new FileCodeWriter( targetDir, readonly ));
    }

    /**
     * Creates a configured CodeWriter that produces files into the specified directory.
     */
    public static CodeWriter createCodeWriter( CodeWriter core ) {

        // generate format syntax: <date> 'at' <time>
        String format =
            Messages.format(Messages.DATE_FORMAT)
                + " '"
                + Messages.format(Messages.AT)
                + "' "
                + Messages.format(Messages.TIME_FORMAT);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    
        return new PrologCodeWriter( core,
                Messages.format(
                    Messages.FILE_PROLOG_COMMENT,
                    dateFormat.format(new Date())) );
    }
}

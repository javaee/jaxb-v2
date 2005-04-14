package com.sun.tools.txw2;

import com.sun.codemodel.writer.FileCodeWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Ant task interface for txw compiler.
 *
 * @author ryan_shoemaker@dev.java.net
 */
public class TxwTask extends org.apache.tools.ant.Task {

    // txw options - reuse command line options from the main driver
    private final TxwOptions options = new TxwOptions();

    // schema file
    private File schemaFile;

    // syntax style of RELAX NG source schema - "xml" or "compact"
    private static final String COMPACT = "compact";
    private static final String XML = "xml";
    private static final String AUTO_DETECT = "detect";
    private String style = AUTO_DETECT;

    public TxwTask() {
        // default package
        options._package = options.codeModel.rootPackage();

        // default codewriter
        try {
            options.codeWriter = new FileCodeWriter(new File("."));
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Parse @package
     *
     * @param pkg name of the package to generate the java classes into
     */
    public void setPackage( String pkg ) {
        options._package = options.codeModel._package( pkg );
    }

    /**
     * Parse @syntax
     *
     * @param style either "compact" for RELAX NG compact syntax or "XML"
     * for RELAX NG xml syntax
     */
    public void setSyntax( String style ) {
        if (!COMPACT.equals(style) && !XML.equals(style)) {
            // if style is specified incorrectly, then die
            // if style isn't specified, guess the syntax based on the file extension
            throw new BuildException("'@syntax' must be set to either 'compact' or 'xml'");
        }

        this.style = style;
    }

    /**
     * parse @schema
     *
     * @param schema the schema file to be processed by txw
     */
    public void setSchema( File schema ) {
        schemaFile = schema;
    }

    /**
     * parse @destdir
     *
     * @param dir the directory to produce generated source code in
     */
    public void setDestdir( File dir ) {
        try {
            options.codeWriter = new FileCodeWriter(dir);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * parse @methodChaining
     *
     * @param flg true if the txw should generate api's that allow
     * method chaining (when possible, false otherwise
     */
    public void setMethodChaining( boolean flg ) {
        options.chainMethod = flg;
    }

    /**
     * launch txw
     */
    public void execute() throws BuildException {
        options.errorListener = new AntErrorListener(getProject());

        try {
            InputSource in = new InputSource(schemaFile.toURL().toExternalForm());

            String msg = "Compiling: " + in.getSystemId();
            log( msg, Project.MSG_INFO );

            if(AUTO_DETECT.equals(style)) {
                if(schemaFile.getPath().toLowerCase().endsWith("rnc"))
                    style = COMPACT;
                else
                    style = XML;
            }

            if(COMPACT.equals(style))
                options.source = new CompactParseable(in,options.errorListener);
            if(XML.equals(style))
                options.source = new SAXParseable(in,options.errorListener);
        } catch (MalformedURLException e) {
            throw new BuildException(e);
        }

        // kick off the compiler
        Main.run(options);
        log( "Compilation complete.", Project.MSG_INFO );
    }
}

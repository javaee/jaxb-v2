package com.sun.tools.txw2;

import org.xml.sax.SAXParseException;

import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * Prints the error to a stream.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ConsoleErrorReporter implements ErrorListener {
    private final PrintStream out;

    public ConsoleErrorReporter(PrintStream out) {
        this.out = out;
    }

    public void error(SAXParseException exception) {
        out.print("[ERROR]   ");
        print(exception);
    }

    public void fatalError(SAXParseException exception) {
        out.print("[FATAL]   ");
        print(exception);
    }

    public void warning(SAXParseException exception) {
        out.print("[WARNING] ");
        print(exception);
    }

    private void print(SAXParseException e) {
        out.println(e.getMessage());
        out.println(MessageFormat.format("  {0}:{1} of {2}",
            new Object[]{
                String.valueOf(e.getLineNumber()),
                String.valueOf(e.getColumnNumber()),
                e.getSystemId()}));
    }


}

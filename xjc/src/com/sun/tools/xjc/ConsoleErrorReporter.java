/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

import java.io.OutputStream;
import java.io.PrintStream;

import org.xml.sax.SAXParseException;

/**
 * {@link ErrorReceiver} that prints to a {@link PrintStream}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ConsoleErrorReporter extends ErrorReceiver {

    /**
     * Errors, warnings are sent to this output.
     */
    private PrintStream output;
    
    private boolean hadError = false;

    public ConsoleErrorReporter( PrintStream out) {
        this.output = out;
    }
    public ConsoleErrorReporter( OutputStream out ) {
        this(new PrintStream(out));
    }
    public ConsoleErrorReporter() { this(System.out); }
    
    public void warning(SAXParseException e) {
        print(Messages.WARNING_MSG,e);
    }
    
    public void error(SAXParseException e) {
        hadError = true;
        print(Messages.ERROR_MSG,e);
    }
    
    public void fatalError(SAXParseException e) {
        hadError = true;
        print(Messages.ERROR_MSG,e);
    }
    
    public void info(SAXParseException e) {
        print(Messages.INFO_MSG,e);
    }

    public boolean hadError() {
        return hadError;
    }

    private void print( String resource, SAXParseException e ) {
        output.println(Messages.format(resource,e.getMessage()));
        output.println(getLocationString(e));
        output.println();
    }
}

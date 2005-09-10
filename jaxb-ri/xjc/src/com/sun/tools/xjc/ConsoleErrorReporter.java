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

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

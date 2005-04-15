package com.sun.xml.bind.v2.runtime;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author Kohsuke Kawaguchi
 */
final class Printer {
    private final PrintWriter out;
    private int indent=0;

    public Printer(PrintWriter out) {
        this.out = out;
    }

    public Printer(PrintStream out) {
        this.out = new PrintWriter(out);
    }

    public void in() {
        indent++;
    }

    public void out() {
        indent--;
    }

    public void print(String msg) {
        printIndent();
        out.println(msg);
        out.flush();
    }

    private void printIndent() {
        for( int i=0; i<indent; i++ )
            out.print("  ");
    }
}

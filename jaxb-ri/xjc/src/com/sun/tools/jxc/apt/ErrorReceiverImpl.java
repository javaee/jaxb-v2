package com.sun.tools.jxc.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.tools.xjc.ErrorReceiver;

import org.xml.sax.SAXParseException;

/**
 * @author Kohsuke Kawaguchi
 */
final class ErrorReceiverImpl extends ErrorReceiver {
    private final Messager messager;
    private final boolean debug;

    public ErrorReceiverImpl(Messager messager, boolean debug) {
        this.messager = messager;
        this.debug = debug;
    }

    public ErrorReceiverImpl(Messager messager) {
        this(messager,false);
    }

    public ErrorReceiverImpl(AnnotationProcessorEnvironment env) {
        this(env.getMessager());
    }

    public void error(SAXParseException exception) {
        messager.printError(exception.getMessage());
        messager.printError(getLocation(exception));
        printDetail(exception);
    }

    public void fatalError(SAXParseException exception) {
        messager.printError(exception.getMessage());
        messager.printError(getLocation(exception));
        printDetail(exception);
    }

    public void warning(SAXParseException exception) {
        messager.printWarning(exception.getMessage());
        messager.printWarning(getLocation(exception));
        printDetail(exception);
    }

    public void info(SAXParseException exception) {
        printDetail(exception);
    }

    private String getLocation(SAXParseException e) {
        // TODO: format the location information for printing
        return "";
    }

    private void printDetail(SAXParseException e) {
        if(debug) {
            e.printStackTrace(System.out);
        }
    }
}
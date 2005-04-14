package com.sun.tools.txw2;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Used internally to report errors.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface ErrorListener extends ErrorHandler {
    abstract void error (SAXParseException exception);
    abstract void fatalError (SAXParseException exception);
    abstract void warning (SAXParseException exception);

}

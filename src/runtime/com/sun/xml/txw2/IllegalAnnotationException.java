package com.sun.xml.txw2;

/**
 * Signals an incorrect use of TXW annotations.
 *
 * @author Kohsuke Kawaguchi
 */
public class IllegalAnnotationException extends TxwException {
    public IllegalAnnotationException(String message) {
        super(message);
    }

    public IllegalAnnotationException(Throwable cause) {
        super(cause);
    }

    public IllegalAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 1L;
}

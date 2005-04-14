package com.sun.xml.txw2;

/**
 * Signals incorrect method signatures on {@link TypedXmlWriter}-derived interfaces.
 *
 * @author Kohsuke Kawaguchi
 */
public class IllegalSignatureException extends TxwException {
    public IllegalSignatureException(String message) {
        super(message);
    }

    public IllegalSignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalSignatureException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 1L;
}

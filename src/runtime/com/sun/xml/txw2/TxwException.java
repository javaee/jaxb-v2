package com.sun.xml.txw2;

/**
 * Signals errors in the TXW processing.
 */
public class TxwException extends RuntimeException {
    public TxwException(String message) {
        super(message);
    }

    public TxwException(Throwable cause) {
        super(cause);
    }

    public TxwException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 1L;
}

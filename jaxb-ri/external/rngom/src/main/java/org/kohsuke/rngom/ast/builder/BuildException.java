package org.kohsuke.rngom.ast.builder;

/**
 * Signals an error while building schemas.
 * 
 * <p>
 * Only {@link SchemaBuilder} can throw this exception to
 * abort the parsing in the middle.
 */
public class BuildException extends RuntimeException {
    private final Throwable cause;
    public BuildException(Throwable cause) {
        if (cause == null)
            throw new NullPointerException("null cause");
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}

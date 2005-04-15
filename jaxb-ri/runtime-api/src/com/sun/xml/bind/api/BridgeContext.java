package com.sun.xml.bind.api;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.mtom.AttachmentMarshaller;
import javax.xml.bind.mtom.AttachmentUnmarshaller;

/**
 * Holds thread specific state information for {@link Bridge}s,
 * to make {@link Bridge} thread-safe.
 *
 * <p>
 * This object cannot be used concurrently; two threads cannot
 * use the same object with {@link Bridge}s at the same time, nor
 * a thread can use a {@link BridgeContext} with one {@link Bridge} while
 * the same context is in use by another {@link Bridge}.
 *
 * <p>
 * {@link BridgeContext} is relatively a heavy-weight object, and
 * therefore it is expected to be cached by the JAX-RPC RI.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.0 EA1
 * @see Bridge
 */
public abstract class BridgeContext {
    protected BridgeContext() {}
    
    /**
     * Registers the error handler that receives unmarshalling/marshalling errors.
     *
     * @param handler
     *      can be null, in which case all errors will be considered fatal.
     *
     * @since 2.0 EA1
     */
    public abstract void setErrorHandler(ValidationEventHandler handler);

    /**
     * Sets the {@link AttachmentMarshaller}.
     *
     * @since 2.0 EA1
     */
    public abstract void setAttachmentMarshaller(AttachmentMarshaller m);

    /**
     * Sets the {@link AttachmentUnmarshaller}.
     *
     * @since 2.0 EA1
     */
    public abstract void setAttachmentUnmarshaller(AttachmentUnmarshaller m);
}

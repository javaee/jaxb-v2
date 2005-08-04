package com.sun.xml.bind.v2.runtime.unmarshaller;

/**
 * @author Kohsuke Kawaguchi
 */
public final class ChildLoader {
    public final Loader loader;
    public final Receiver receiver;

    public ChildLoader(Loader loader, Receiver receiver) {
        this.loader = loader;
        this.receiver = receiver;
    }
}

package com.sun.xml.bind.v2.runtime;

import java.util.HashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

/**
 * Object that coordinates the marshalling/unmarshalling.
 *
 * <p>
 * This class takes care of the logic that allows code to obtain
 * {@link UnmarshallingContext} and {@link XMLSerializer} instances
 * during the unmarshalling/marshalling.
 *
 * <p>
 * This is done by using a {@link ThreadLocal}. Therefore one unmarshalling/marshalling
 * episode has to be done from the beginning till end by the same thread.
 * (Note that the same {@link Coordinator} can be then used by a different thread
 * for an entirely different episode.)
 *
 * This class also maintains the user-configured instances of {@link XmlAdapter}s.
 * 
 * @author Kohsuke Kawaguchi
 */
public class Coordinator {
    
    private final HashMap<Class<? extends XmlAdapter>,XmlAdapter> adapters =
            new HashMap<Class<? extends XmlAdapter>,XmlAdapter>();


    public final XmlAdapter putAdapter(Class<? extends XmlAdapter> c, XmlAdapter a) {
        if(a==null)
            return adapters.remove(c);
        else
            return adapters.put(c,a);
    }

    /**
     * Gets the instance of the adapter.
     *
     * @return
     *      always non-null.
     */
    public final <T extends XmlAdapter> T getAdapter(Class<T> key) {
        T v = (T)adapters.get(key);
        if(v==null) {
            v = ClassFactory.create(key);
            putAdapter(key,v);
        }
        return v;
    }

    public <T extends XmlAdapter> boolean containsAdapter(Class<T> type) {
        return adapters.containsKey(type);
    }

    /**
     * The {@link Coordinator} in charge before this {@link Coordinator}.
     */
    private Coordinator old;

    /**
     * A 'pointer' to a {@link Coordinator} that keeps track of the currently active {@link Coordinator}.
     * Having this improves the runtime performance.
     */
    private Coordinator[] table;

    /**
     * Associates this {@link Coordinator} with the current thread.
     * Should be called at the very beginning of the episode.
     */
    protected final void setThreadAffinity() {
        table = activeTable.get();
    }

    /**
     * Dis-associate this {@link Coordinator} with the current thread.
     * Sohuld be called at the end of the episode to avoid memory leak.
     */
    protected final void resetThreadAffinity() {
        table = null;
    }

    /**
     * Called whenever an execution flow enters the realm of this {@link Coordinator}.
     */
    protected final void pushCoordinator() {
        old = table[0];
        table[0] = this;
    }

    /**
     * Called whenever an execution flow exits the realm of this {@link Coordinator}.
     */
    protected final void popCoordinator() {
        assert table[0]==this;
        table[0] = old;
        old = null; // avoid memory leak
    }

    public static final Coordinator _getInstance() {
        return activeTable.get()[0];
    }

    // this much is necessary to avoid calling get and set twice when we push.
    private static final ThreadLocal<Coordinator[]> activeTable = new ThreadLocal<Coordinator[]>() {
        public Coordinator[] initialValue() {
            return new Coordinator[1];
        }
    };
}

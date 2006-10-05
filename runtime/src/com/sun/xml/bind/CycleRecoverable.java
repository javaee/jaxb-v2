package com.sun.xml.bind;

/**
 * Optional interface that can be implemented by JAXB-bound objects
 * to handle cycles in the object graph.
 *
 * <p>
 * As discussed in <a href="https://jaxb.dev.java.net/guide/Mapping_cyclic_references_to_XML.html">
 * the users' guide</a>, normally a cycle in the object graph causes the marshaller to report an error,
 * and when an error is found, the JAXB RI recovers by cutting the cycle arbitrarily.
 * This is not always a desired behavior.
 *
 * <p>
 * Implementing this interface allows user application to change this behavior.
 * Also see <a href="http://forums.java.net/jive/thread.jspa?threadID=13670">this related discussion</a>.
 *
 * @since JAXB 2.1 EA2
 * @author Kohsuke Kawaguchi
 */
public interface CycleRecoverable {
    /**
     * Called when a cycle is detected by the JAXB RI marshaller
     * to nominate a new object to be marshalled instead.
     *
     * @return
     *      the object to be marshalled instead of <tt>this</tt> object.
     *      Or return null to indicate that the JAXB RI should behave
     *      just like when your object does not implement {@link CycleRecoverable}
     *      (IOW, cut the cycle arbitrarily and try to go on.)
     */
    Object onCycleDetected(Context context);

    public interface Context {}
}

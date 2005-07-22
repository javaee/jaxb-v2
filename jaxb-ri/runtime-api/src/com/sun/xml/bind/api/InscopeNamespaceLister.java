package com.sun.xml.bind.api;

import java.io.OutputStream;

import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLEventWriter;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Node;

/**
 * Application callback "interface" that allows
 * available in-scope namespace bindings to be used by {@link Marshaller}.
 *
 * <h2>Use Case</h2>
 * <p>
 * Sometimes JAXB is used to marshal an XML document, which will be
 * used as a subtree of a bigger document. When this happens, it's nice
 * for a JAXB marshaller to be able to use in-scope namespace bindings
 * of the larger document and avoid declaring redundant namespace URIs.
 *
 * <p>
 * This is automatically done when you are marshalling to {@link XMLStreamWriter},
 * {@link XMLEventWriter}, {@link DOMResult}, or {@link Node}, because
 * those output format allows us to inspect what's currently available
 * as in-scope namespace binding. However, with other output format,
 * such as {@link OutputStream}, the JAXB RI cannot do this automatically.
 * That's when this "interface" comes into play.
 *
 *
 * <h2>Usage</h2>
 * The calling application
 * implements this interface by writing the {@link #listInscopeNamespaces(Receiver)} method
 * that enumerates all the in-scope namespace bindings.
 *
 * <p>
 * The JAXB RI receives an implementation through
 * {@link Marshaller#setProperty(String, Object)} like this:
 *
 * <pre>
 * marshaller.setProperty({@link JAXBRIContext#INSCOPE_NAMESPACE_LISTER},lister);
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB 2.0 beta
 */
public abstract class InscopeNamespaceLister {

    /**
     * Invoked by the JAXB RI once during an marshalling to retrieve
     * the information about the in-scope namespace bindings.
     *
     * @param receiver
     *      The JAXB RI always supply a non-null valid implementation of this object.
     *      The callee should call its {@link Receiver#addInscopeBinding(String, String)} method
     *      repeatedly for each in-scope namespace binding.
     *
     *      <p>
     *      It is <b>NOT</b> OK to call the receiver twice with the same binding, or give
     *      the receiver a conflicting binding information.
     *      It's a responsibility of the caller to make sure that this doesn't happen
     *      even if the ancestor elements look like:
     *      <pre><xmp>
     *        <foo:abc xmlns:foo="abc">
     *          <foo:abc xmlns:foo="def">
     *            <foo:abc xmlns:foo="abc">
     *              ... JAXB marshalling into here.
     *            </foo:abc>
     *          </foo:abc>
     *        </foo:abc>
     *      </xmp></pre>
     *
     *      <p>
     *      TODO: check with XWSS / JAX-WS to see if this restriction is reasonable.
     *      I'm mostly assuming that they only use a fixed set of namespace URIs on soap:Envelope
     *      and soap:Header, but in theory the {@link Receiver} is in a better position to
     *      detect a collision/duplicate. (if the lister needs to create a set/map to check for
     *      redundancy, that robs the whole point of this abstraction.)
     */
    public abstract void listInscopeNamespaces( Receiver receiver );

    /**
     * Implemented by the JAXB RI.
     *
     * New methods maybe added without a notice.
     */
    public interface Receiver {
        /**
         * Receives the in-scope namespace binding.
         */
        void addInscopeBinding(String nsUri,String prefix);
    }
}

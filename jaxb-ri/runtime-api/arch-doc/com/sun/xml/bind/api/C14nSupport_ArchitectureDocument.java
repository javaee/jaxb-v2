package com.sun.xml.bind.api;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * <h1>Canonical XML Support in the JAXB RI</h1>
 *
 * <p>
 * The JAXB RI marshaller can be configured to marshal canonical XML.
 * To do so, you specify true to the {@link JAXBRIContext#newInstance} method.
 *
 * <h3>Supported C14n Modes</h3>
 * <p>
 * If you create {@link JAXBContext} with the c14n support, {@link Marshaller}s created
 * from such {@link JAXBContext} will automatically generate canonical XML documents
 * (in the sense of <a href="http://www.w3.org/TR/2001/REC-xml-c14n-20010315">the Canonical XML spec</a>)
 * provided that you marshal it to UTF-8 and don't do indentation.
 * (The c14n code is tied to this particular configuration.)
 *
 * <p>
 * When using JAXB to marshal a tree canonically to be a subtree of a bigger document,
 * you also need to use {@link InscopeNamespaceLister} with the marshaller. To do so,
 * use {@link Marshaller#setProperty(String, Object)} }. Also
 * refer to {@link JAXBRIContext#INSCOPE_NAMESPACE_LISTER} for details.
 *
 *
 * <h2>TODOs</h2>
 * <p>
 * Exclusive canonicalization.
 *
 * <h2>Internals</h2>
 * <p>
 * The c14n support in {@link JAXBRIContext} causes the marshaller to write
 * "known" attributes in the lexicographical order (except those attributes in the attribute wildcard.)
 *
 * <p>
 * If the object tree to be marshalled doesn't contain any attribute wildcard,
 * the marshalling performs almost as fast as the ordinary marshalling mode.
 * The only additional cost is:
 * <ol>
 *  <li>buffering of attributes
 *  <li>bubble-sorting of namespace declarations
 * </ol>
 *
 * @see JAXBRIContext#CANONICALIZATION_SUPPORT
 * @see InscopeNamespaceLister
 *
 * @ArchitectureDocument
 * @author Kohsuke Kawaguchi
 */
public class C14nSupport_ArchitectureDocument {
}

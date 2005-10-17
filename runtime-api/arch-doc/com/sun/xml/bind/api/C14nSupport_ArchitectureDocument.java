package com.sun.xml.bind.api;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * <h1>Canonical XML Support in the JAXB RI</h1>
 *
 * <p>
 * The JAXB RI marshaller or {@link JAXBRIContext} can be configured to marshal
 * canonical XML. There are two ways to do this:
 *
 * <ol>
 *  <li>If you know in advance that you'll need c14n, use
 *      {@link JAXBRIContext#newInstance} to create a new {@link JAXBContext}
 *      with c14n. In this way, every marshaller created from this {@link JAXBRIContext}
 *      will do c14n. This also runs faster than the second option.
 *  <li>use {@link Marshaller#setProperty(String, Object)} with {@link JAXBRIContext#CANONICALIZATION_SUPPORT}
 *      to enable c14n on a marshaller instance.
 * </ol>
 *
 * <h2>Supported C14n Modes</h2>
 * <h3>(Inclusive) Canonicalization</h3>
 * <p>
 * Regardless of which two ways you took to configure JAXB,
 * generated canonical XML documents will follow
 * <a href="http://www.w3.org/TR/2001/REC-xml-c14n-20010315">the Canonical XML spec</a>
 * provided that you marshal it to UTF-8 (which is the spec requirement) and
 * don't turn on the formatting (which is our implementation requirement.)
 *
 * <p>
 * When using JAXB to marshal a tree canonically to be a subtree of a bigger document,
 * you also need to use {@link com.sun.xml.bind.marshaller.NamespacePrefixMapper}
 * with the marshaller.
 *
 * In particular, use
 * {@link com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreDeclaredNamespaceUris2()}
 * to make sure that the marshalling redeclares all the in-scope namespace bindings at
 * the root element.
 *
 *
 *
 * <h3>Exclusive Canonicalization</h3>
 * <p>
 * JAXB RI doesn't support exclusive canonicalization (xc14n) in its full generality,
 * but it can be used to performa xc14n if the application is in position to choose
 * the list of <a href="http://www.w3.org/TR/2002/REC-xml-exc-c14n-20020718/#def-InclusiveNamespaces-PrefixList">
 * "inclusive namespaces"</a>.
 *
 * <p>
 * IOW, this support can be used when c14n is for producing a new signature, but not
 * when c14n is for verifying a signature.
 *
 * <p>
 * The exclusive c14n support in JAXB can be done by making "inclusive namespaces"
 * an union of (1) all the namespace URIs statically known to JAXB RI and
 * (2) all in-scope namespace bindings available at ancestors.
 *
 * This effectively reduces xc14n to inclusive c14n.
 *
 * <p>
 * Specifically, the calling application should do the followings:
 *
 * <ol>
 *  <li>List up all the in-scope namespaces declared in ancestor elements,
 *      and add them to the inclusive namespace prefix list.
 *  <li>Do not  
 *
 * <h2>Unsupported Features</h2>
 * <p>
 * When canonicalizing a subtree, the canonical XML spec requires the xml attributes
 * (such as xml:lang, xml:base) on ancestor elements to be copied over to the root
 * of the canonical subtree.
 * This behavior is not implemented.
 *
 *
 *
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
 *
 * @ArchitectureDocument
 * @author Kohsuke Kawaguchi
 */
public class C14nSupport_ArchitectureDocument {
}

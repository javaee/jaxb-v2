package com.sun.xml.xsom;

import org.relaxng.datatype.ValidationContext;

/**
 * Selector or field of {@link XSIdentityConstraint}.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface XSXPath extends XSComponent  {

    /**
     * Returns the {@link XSIdentityConstraint} to which
     * this XPath belongs to.
     *
     * @return
     *      never null.
     */
    XSIdentityConstraint getParent();

    /**
     * Gets the XPath as a string.
     *
     * <p>
     * To correctly resolve prefixes in this string, use
     * {@link #getContext()}.
     *
     * @return
     *      never null.
     */
    String getXPath();


    /**
     * Gets the context in which the facet value was found.
     *
     * <p>
     * The primary use of the ValidationContext is to resolve the
     * namespace prefix of the value when it is a QName.
     *
     * @return
     *      never null.
     */
    ValidationContext getContext();
}

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

import java.util.List;

/**
 * Base interface for all the schema components.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSComponent
{
    /** Gets the annotation associated to this component, if any. */
    XSAnnotation getAnnotation();

    /**
     * Gets the foreign attributes on this schema component.
     *
     * <p>
     * In general, a schema component may match multiple elements
     * in a schema document, and those elements can individually
     * carry foreign attributes.
     *
     * <p>
     * This method returns a list of {@link ForeignAttributes}, where
     * each {@link ForeignAttributes} object represent foreign attributes
     * on one element.
     *
     * @return
     *      can be an empty list but never be null.
     */
    List<? extends ForeignAttributes> getForeignAttributes();

    /**
     * Gets the foreign attribute of the given name, or null if not found.
     *
     * <p>
     * If multiple occurences of the same attribute is found,
     * this method returns the first one.
     *
     * @see #getForeignAttributes()
     */
    String getForeignAttribute(String nsUri, String localName);

    /**
     * Gets the locator that indicates the source location where
     * this component is created from, or null if no information is
     * available.
     */
    Locator getLocator();

    /**
     * Gets a reference to the {@link XSSchema} object to which this component
     * belongs.
     * <p>
     * In case of <code>XSEmpty</code> component, this method
     * returns null since there is no owner component.
     */
    XSSchema getOwnerSchema();
    
    /**
     * Accepts a visitor.
     */
    void visit( XSVisitor visitor );
    /**
     * Accepts a functor.
     */
    <T> T apply( XSFunction<T> function );
}

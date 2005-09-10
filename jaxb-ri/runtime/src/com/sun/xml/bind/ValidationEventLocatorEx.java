/*
 * @(#)$Id: ValidationEventLocatorEx.java,v 1.2 2005-09-10 19:07:39 kohsuke Exp $
 */

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
package com.sun.xml.bind;

import javax.xml.bind.ValidationEventLocator;

/**
 * Defines additional accessor methods for the event source location.
 * <p>
 * This interface exposes the location information only available
 * in the JAXB RI specific extension.
 * <p>
 * <em>DO NOT IMPLEMENT THIS INTERFACE BY YOUR CODE</em> because
 * we might add more methods on this interface in the future release
 * of the RI.
 * 
 * <h2>Usage</h2>
 * <p>
 * If you obtain a reference to {@link javax.xml.bind.ValidationEventLocator},
 * check if you can cast it to {@link ValidationEventLocatorEx} first, like this:
 * <pre>
 * void foo( ValidationEvent e ) {
 *     ValidationEventLocator loc = e.getLocator();
 *     if( loc instanceof ValidationEventLocatorEx ) {
 *         String fieldName = ((ValidationEventLocatorEx)loc).getFieldName();
 *         if( fieldName!=null ) {
 *             // do something with location.
 *         }
 *     }
 * }
 * </pre>
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface ValidationEventLocatorEx extends ValidationEventLocator {
    /**
     * Returns the field name of the object where the error occured.
     * <p>
     * This method always returns null when you are doing
     * a validation during unmarshalling.
     * 
     * When not null, the field name indicates the field of the object
     * designated by the {@link #getObject()} method where the error
     * occured. 
     */
    String getFieldName();
}

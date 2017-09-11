/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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

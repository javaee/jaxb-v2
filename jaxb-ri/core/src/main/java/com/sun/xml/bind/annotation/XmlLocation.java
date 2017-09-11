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

package com.sun.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.xml.sax.Locator;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Marks a property that receives a location from which the object is unmarshalled.
 *
 * <h2>Usage</h2>
 * <p>
 * The @XmlLocation can be specified on:
 * <ul>
 *  <li>a field whose type is {@link Locator}, or
 *  <li>a method that takes a {@link Locator} as the sole parameter
 * </ul>
 *
 * <p>
 * When a class that contains such a field/method is unmarshalled by the JAXB RI,
 * such a field/method will receive an immutable {@link Locator} object that describes
 * the location in the XML document where the object is unmarshalled from.
 *
 * <p>
 * If the unmarshaller does not know the source location information, the locator
 * will not be set. For example, this happens when it is unmarshalling from a DOM tree.
 * This also happens if you use JAXB implementations other than the JAXB RI.
 *
 * <p>
 * This information can be used by applications, for example to provide user-friendly
 * error information.
 *
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB RI 2.0 EA
 */
@Retention(RUNTIME) @Target({FIELD,METHOD})
public @interface XmlLocation {
}

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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlValue;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Designates a boolean field/property as a flag to indicate
 * whether another property is present or not.
 *
 * <p>
 * Sometimes you'd want to map a Java primitive type to an
 * optional element/attribute. Doing this makes it impossible
 * to represent the absence of the property, thus you always
 * end up producing the value when you marshal to XML.
 *
 * For example,
 * <pre>
 * {@link XmlElement}
 * class Foo {
 *      {@link XmlElement}
 *      int x;
 * }
 *
 * marshaller.marshal(new Foo());
 * </pre>
 * and you get:
 * <pre>{@code
 * <foo><x>0</x></foo>
 * }</pre>
 *
 * <p>
 * By creating a side boolean field/property that has this annotation,
 * you can indicate the absence of the property by setting this boolean
 * to false.
 * <pre>
 * {@link XmlElement}
 * class Foo {
 *      {@link XmlElement}
 *      int x;
 *      {@link XmlIsSet}("x")
 *      boolean xIsPresent;
 * }
 *
 * Foo f = new Foo();
 * f.x = 5;
 * f.xIsPresent = false;
 *
 * marshaller.marshal(f);
 *
 * {@code
 * <foo/>
 * }
 *
 * f.xIsPresent = true;
 * {@code
 * <foo><x>5</x></foo>
 * }
 * </pre>
 *
 * <p>
 * A property/field annotated with {@link XmlIsSet} itself will not show up in XML.
 * It is an error to use this annotation on the same property/field
 * as {@link XmlElement}, {@link XmlAttribute}, {@link XmlValue}, or {@link XmlElementRef},
 * ...<b>TBD</b>.
 *
 * @deprecated
 *      this hasn't been implemented in the RI, and this hasn't been speced yet.
 *      I believe Joe asked for this feature. I'd like to drop this.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlIsSet {
    /**
     * Specifies the name of the property to attach to.
     */
    String value();
}

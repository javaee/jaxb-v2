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
package com.sun.tools.xjc.api;

import javax.xml.namespace.QName;

import com.sun.codemodel.JType;

/**
 * Represents a property of a wrapper-style element.
 * 
 * <p>
 * Carrys information about one property of a wrapper-style
 * element. This interface is solely intended for the use by
 * the JAX-RPC and otherwise the use is discouraged.
 * 
 * <p>
 * REVISIT: use CodeModel.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @see Mapping
 */
public interface Property {
    /**
     * The name of the property.
     * 
     * <p>
     * This method returns a valid identifier suitable for
     * the use as a variable name.
     * 
     * @return
     *      always non-null. Camel-style name like "foo" or "barAndZot".
     *      Note that it may contain non-ASCII characters (CJK, etc.)
     *      The caller is responsible for proper escaping if it
     *      wants to print this as a variable name.
     */
    String name();
    
    /**
     * The Java type of the property.
     * 
     * @return
     *      always non-null.
     *      {@link JType} is a representation of a Java type in a codeModel.
     *      If you just need the fully-qualified class name, call {@link JType#fullName()}.
     */
    JType type();
    
    /**
     * Name of the XML element that corresponds to the property.
     * 
     * <p>
     * Each child of a wrapper style element corresponds with an
     * element, and this method returns that name.
     * 
     * @return
     *      always non-null valid {@link QName}.
     */
    QName elementName();
}

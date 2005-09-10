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

package com.sun.xml.bind.serializer;

import javax.xml.namespace.NamespaceContext;

/**
 * Maintains namespace&lt;->prefix bindings.
 * 
 * <p>
 * This interface extends {@link NamespaceContext} and provides
 * an additional functionality, which is necessary to declare
 * namespaced attributes on elements. The added method is for
 * self-consumption by the marshaller.
 * 
 * This object is composed into a Serializer.
 * 
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface NamespaceContext2 extends NamespaceContext
{
    /**
     * Declares a new namespace binding within the current context.
     * 
     * <p>
     * The prefix is automatically assigned by MarshallingContext. If
     * a given namespace URI is already declared, nothing happens.
     * 
     * <p>
     * It is <b>NOT</b> an error to declare the same namespace URI
     * more than once.
     * 
     * <p>
     * For marshalling to work correctly, all namespace bindings
     * for an element must be declared between its startElement method and
     * its endAttributes event. Calling the same method with the same
     * parameter between the endAttributes and the endElement returns
     * the same prefix.
     * 
     * @param   requirePrefix
     *      If this parameter is true, this method must assign a prefix
     *      to this namespace, even if it's already bound to the default
     *      namespace. IOW, this method will never return null if this
     *      flag is true. This functionality is necessary to declare
     *      namespace URI used for attribute names.
     * 
     * @return
     *      returns the assigned prefix. If the namespace is bound to
     *      the default namespace, null is returned.
     */
    String declareNamespace( String namespaceUri, boolean requirePrefix );
}

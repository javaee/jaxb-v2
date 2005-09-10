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
package com.sun.xml.bind.marshaller;

/**
 * This interface will be implemented by content tree classes
 * with ID, so that the marshaller can properly serialize ID value.
 * 
 * <p>
 * deprecated in 2.0
 *      We no longer expect the generated beans to implement
 *      any of the marker interfaces.
 * 
 * @since 1.0
 */
public interface IdentifiableObject
{
    /**
     * Gets the value of ID of this object.
     * 
     * To forestall the possibility of name collision with the
     * generated class, the method name is intentionally made long.
     */
    String ____jaxb____getId();
}

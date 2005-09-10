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

import javax.xml.bind.Element;

/**
 * RI-generated class that implements the {@link Element} interface
 * always also implement this interface, so that the name of the
 * element can be obtained.
 * 
 * <p>
 * Since {@link Element} is used only when the schema language is
 * XML Schema, this interface is also XML Schema specific
 * and used only when the classes are generated from XML Schema.
 * 
 * <p>
 * This interface allows the runtime to retrieve the element name
 * from the generated code. To avoid name conflict with user-defined
 * properties, names of the methods are intentionally obfuscated.
 * 
 * <p>
 * deprecated in 2.0
 *      We no longer expect JAXB-bound beans to implement any
 *      interface.
 *
 * @since 1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * 
 */
public interface RIElement extends Element {
    String ____jaxb_ri____getNamespaceURI();
    String ____jaxb_ri____getLocalName();
}

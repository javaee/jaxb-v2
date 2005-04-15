/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

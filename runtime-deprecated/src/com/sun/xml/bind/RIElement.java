/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

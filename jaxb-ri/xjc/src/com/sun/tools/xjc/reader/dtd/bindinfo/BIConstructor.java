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
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.sun.tools.xjc.model.CClassInfo;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

/**
 * &lt;constructor> declaration in the binding file.
 * 
 * <p>
 * Since JAXB will generate both interfaces and implementations,
 * A constructor declaration will create:
 * 
 * <ul>
 *  <li> a method declaration in the factory interface
 *  <li> a method implementation in the factory implementation class
 *  <li> a constructor implementation in the actual implementation class
 * </ul>
 */
public class BIConstructor
{
    BIConstructor( Element _node ) {
        this.dom = _node;
        
        StringTokenizer tokens = new StringTokenizer(
            DOMUtil.getAttribute(_node,"properties"));
        
        List<String> vec = new ArrayList<String>();
        while(tokens.hasMoreTokens())
            vec.add(tokens.nextToken());
        properties = vec.toArray(new String[0]);
        
        if( properties.length==0 )
            throw new AssertionError("this error should be catched by the validator");
    }
    
    /** &lt;constructor> element in the source binding file. */
    private final Element dom;
    
    /** properties specified by @properties. */
    private final String[] properties;
    
    /**
     * Creates a constructor declaration into the ClassItem.
     * 
     * @param   cls
     *      ClassItem object that corresponds to the
     *      element declaration that contains this declaration.
     */
    public void createDeclaration( CClassInfo cls ) {
        cls.addConstructor(properties);
    }

    /** Gets the location where this declaration is declared. */
    public Locator getSourceLocation() {
        return DOMLocator.getLocationInfo(dom);
    }
    

}

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

package com.sun.xml.bind.v2.runtime;

import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Post-init action for {@link MarshallerImpl} that incorporate the in-scope namespace bindings
 * from a DOM node.
 *
 * TODO: do we really need this? think about a better way to put this logic back into marshaller.
 *
 * @author Kohsuke Kawaguchi
 */
final class DomPostInitAction implements Runnable {

    private final Node node;
    private final XMLSerializer serializer;

    DomPostInitAction(Node node, XMLSerializer serializer) {
        this.node = node;
        this.serializer = serializer;
    }

    // declare the currently in-scope namespace bindings
    public void run() {
        Set<String> declaredPrefixes = new HashSet<String>();
        for( Node n=node; n!=null && n.getNodeType()==Node.ELEMENT_NODE; n=n.getParentNode() ) {
            NamedNodeMap atts = n.getAttributes();
            if(atts==null)      continue; // broken DOM. but be graceful.
            for( int i=0; i<atts.getLength(); i++ ) {
                Attr a = (Attr)atts.item(i);
                String nsUri = a.getNamespaceURI();
                if(nsUri==null || !nsUri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
                    continue;   // not a namespace declaration
                String prefix = a.getLocalName();
                if(prefix==null)
                    continue;   // broken DOM. skip to be safe
                if(prefix.equals("xmlns")) {
                    prefix = "";
                }
                String value = a.getValue();
                if(value==null)
                    continue;   // broken DOM. skip to be safe
                if(declaredPrefixes.add(prefix)) {
                    serializer.addInscopeBinding(value,prefix);
                }
            }
        }
    }
}

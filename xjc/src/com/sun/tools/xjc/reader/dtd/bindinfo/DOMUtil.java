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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Kohsuke Kawaguchi
 */
public final class DOMUtil {
    final static String getAttribute(Element e,String attName) {
        if(e.getAttributeNode(attName)==null)   return null;
        return e.getAttribute(attName);
    }

    public static String getAttribute(Element e, String nsUri, String local) {
        if(e.getAttributeNodeNS(nsUri,local)==null) return null;
        return e.getAttributeNS(nsUri,local);
    }

    public static Element getElement(Element e, String nsUri, String localName) {
        NodeList l = e.getChildNodes();
        for(int i=0;i<l.getLength();i++) {
            Node n = l.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE) {
                Element r = (Element)n;
                if(equals(r.getLocalName(),localName) && equals(fixNull(r.getNamespaceURI()),nsUri))
                    return r;
            }
        }
        return null;
    }

    /**
     * Used for defensive string comparisons, as many DOM methods often return null
     * depending on how they are created.
     */
    private static boolean equals(String a,String b) {
        if(a==b)    return true;
        if(a==null || b==null)  return false;
        return a.equals(b);
    }

    /**
     * DOM API returns null for the default namespace whereas it should return "".
     */
    private static String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }

    public static Element getElement(Element e, String localName) {
        return getElement(e,"",localName);
    }

    public static List<Element> getChildElements(Element e) {
        List<Element> r = new ArrayList<Element>();
        NodeList l = e.getChildNodes();
        for(int i=0;i<l.getLength();i++) {
            Node n = l.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE)
                r.add((Element)n);
        }
        return r;
    }

    public static List<Element> getChildElements(Element e,String localName) {
        List<Element> r = new ArrayList<Element>();
        NodeList l = e.getChildNodes();
        for(int i=0;i<l.getLength();i++) {
            Node n = l.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE) {
                Element c = (Element)n;
                if(c.getLocalName().equals(localName))
                    r.add(c);
            }
        }
        return r;
    }
}

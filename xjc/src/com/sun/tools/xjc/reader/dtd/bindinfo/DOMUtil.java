package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @author Kohsuke Kawaguchi
 */
final class DOMUtil {
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
                if(r.getLocalName().equals(localName) && r.getNamespaceURI().equals(nsUri))
                    return r;
            }
        }
        return null;
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

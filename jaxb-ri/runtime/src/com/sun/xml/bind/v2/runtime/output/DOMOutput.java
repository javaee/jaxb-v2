package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.AssociationMap;
import com.sun.xml.bind.marshaller.SAX2DOMEx;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * {@link XmlOutput} implementation that writes to DOM.
 *
 * <p>
 * This is used to perform the associative marshalling.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DOMOutput extends SAXOutput {
    private final AssociationMap assoc;

    public DOMOutput(Node node, AssociationMap assoc) {
        super(new SAX2DOMEx(node));
        this.assoc = assoc;
    }

    private SAX2DOMEx getBuilder() {
        return (SAX2DOMEx)out;
    }

    public void endStartTag() throws SAXException {
        super.endStartTag();

        if(serializer.currentOuterPeer!=null) {
            assoc.addOuter( getBuilder().getCurrentElement(), serializer.currentOuterPeer );
            serializer.currentOuterPeer = null;
        }
        if(serializer.currentTarget!=null)
            assoc.addInner( getBuilder().getCurrentElement(), serializer.currentTarget );
    }
}

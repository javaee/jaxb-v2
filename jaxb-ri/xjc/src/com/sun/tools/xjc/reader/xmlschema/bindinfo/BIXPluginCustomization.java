package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Ring;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

/**
 * Customization specified via {@link Plugin#getCustomizationURIs()}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class BIXPluginCustomization extends AbstractDeclarationImpl {

    /**
     * Customization element.
     */
    public final Element element;

    private QName name;

    public BIXPluginCustomization(Element e, Locator _loc) {
        super(_loc);
        element = e;
    }

    public void onSetOwner() {
        super.onSetOwner();
        if(!Ring.get(Model.class).options.pluginURIs.contains(element.getNamespaceURI()))
            markAsAcknowledged();
    }

    public final QName getName() {
        if(name==null)
            name = new QName(element.getNamespaceURI(),element.getLocalName());
        return name;
    }
}

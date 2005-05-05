package com.sun.tools.xjc.model;

import java.util.ArrayList;
import java.util.Collection;

import com.sun.tools.xjc.Plugin;

/**
 * Represents the list of {@link CPluginCustomization}s attached to a JAXB model component.
 *
 * <p>
 * When {@link Plugin}s register the customization namespace URIs through {@link Plugin#getCustomizationURIs()},
 * XJC will treat those URIs just like XJC's own extension "http://java.sun.com/xml/ns/xjc" and make them
 * available as DOM nodes through {@link CPluginCustomization}. A {@link Plugin} can then access
 * this information to change its behavior.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CCustomizations extends ArrayList<CPluginCustomization> {

    public CCustomizations() {
    }

    public CCustomizations(Collection<? extends CPluginCustomization> cPluginCustomizations) {
        super(cPluginCustomizations);
    }

    /**
     * Finds the first {@link CPluginCustomization} that belongs to the given namespace URI.
     * @return null if not found
     */
    public CPluginCustomization find( String nsUri ) {
        for (CPluginCustomization p : this) {
            if(fixNull(p.element.getNamespaceURI()).equals(nsUri))
                return p;
        }
        return null;
    }

    /**
     * Finds the first {@link CPluginCustomization} that belongs to the given namespace URI and the local name.
     * @return null if not found
     */
    public CPluginCustomization find( String nsUri, String localName ) {
        for (CPluginCustomization p : this) {
            if(fixNull(p.element.getNamespaceURI()).equals(nsUri)
            && fixNull(p.element.getLocalName()).equals(localName))
                return p;
        }
        return null;
    }

    private String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }

    /**
     * Convenient singleton instance that represents an empty {@link CCustomizations}.
     */
    public static final CCustomizations EMPTY = new CCustomizations();

    /**
     * Merges two {@link CCustomizations} objects into one.
     */
    public static CCustomizations merge(CCustomizations lhs, CCustomizations rhs) {
        if(lhs==null || lhs.isEmpty())   return rhs;
        if(rhs==null || rhs.isEmpty())   return lhs;

        CCustomizations r = new CCustomizations(lhs);
        r.addAll(rhs);
        return r;
    }
}

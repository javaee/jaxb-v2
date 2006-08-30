package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.impl.UName;

/**
 * {@link Step} that matches to a specific name.
 *
 * TODO: create other {@link Step} implementations for
 * anonymous and all names.
 *
 * @author Kohsuke Kawaguchi
 */
public class NameStep extends Step {
    private final String nsUri;
    private final String localName;

    public NameStep(Axis axis, int predicate, UName n) {
        this(axis,predicate,n.getNamespaceURI(),n.getName());
    }

    public NameStep(Axis axis, int predicate, String nsUri, String localName) {
        super(axis);
        this.nsUri = nsUri;
        this.localName = localName;
    }

    protected boolean match(XSComponent node) {
        if (node instanceof XSDeclaration) {
            XSDeclaration d = (XSDeclaration) node;
            if(d.getName().equals(localName) && d.getTargetNamespace().equals(nsUri))
                return true;
        }
        return false;
    }
}

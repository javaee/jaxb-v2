package com.sun.xml.bind.v2.schemagen;

import javax.xml.bind.annotation.XmlNsForm;

/**
 * Represents the form default value.
 *
 * @author Kohsuke Kawaguchi
 */
enum Form {
    QUALIFIED(XmlNsForm.QUALIFIED),
    UNQUALIFIED(XmlNsForm.UNQUALIFIED),
    UNSET(XmlNsForm.UNSET);

    /**
     * The same constant defined in the spec.
     */
    private final XmlNsForm xnf;

    Form(XmlNsForm xnf) {
        this.xnf = xnf;
    }

    /**
     * Gets the constant the corresponds to the given {@link XmlNsForm}.
     */
    public static Form get(XmlNsForm xnf) {
        for (Form v : values()) {
            if(v.xnf==xnf)
                return v;
        }
        throw new IllegalArgumentException();
    }
}

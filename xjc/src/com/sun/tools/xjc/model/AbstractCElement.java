package com.sun.tools.xjc.model;

import javax.xml.bind.annotation.XmlTransient;

import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * Partial default implementation of {@link CElement}.
 * 
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractCElement extends AbstractCTypeInfoImpl implements CElement {

    /**
     * The location in the source file where this class was declared.
     */
    @XmlTransient
    private final Locator locator;

    private boolean isAbstract;

    protected AbstractCElement(Model model, XSComponent source, Locator locator, CCustomizations customizations) {
        super(model, source, customizations);
        this.locator = locator;
    }

    public Locator getLocator() {
        return locator;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract() {
        isAbstract = true;
    }
}

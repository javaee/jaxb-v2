package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.Locator;

/**
 * {@link LocatorEx} implemented by {@link Locator}.
 * 
 * @author Kohsuke Kawaguchi
 */
class LocatorExWrapper implements LocatorEx {
    private final Locator locator;

    public LocatorExWrapper(Locator locator) {
        this.locator = locator;
    }

    public ValidationEventLocator getLocation() {
        return new ValidationEventLocatorImpl(locator);
    }

    public String getPublicId() {
        return locator.getPublicId();
    }

    public String getSystemId() {
        return locator.getSystemId();
    }

    public int getLineNumber() {
        return locator.getLineNumber();
    }

    public int getColumnNumber() {
        return locator.getColumnNumber();
    }
}

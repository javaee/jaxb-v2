package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.ValidationEventLocator;

import org.xml.sax.Locator;

/**
 * Object that returns the current location that the {@link XmlVisitor} is parsing.
 *
 * @author Kohsuke Kawaguchi
 */
public interface LocatorEx extends Locator {
    /**
     * Gets the current location in a {@link ValidationEventLocator} object.
     */
    ValidationEventLocator getLocation();

}

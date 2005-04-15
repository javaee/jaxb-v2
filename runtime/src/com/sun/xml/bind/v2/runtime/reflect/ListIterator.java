package com.sun.xml.bind.v2.runtime.reflect;

import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

/**
 * Almost like {@link Iterator} but can throw JAXB specific exceptions.
 * @author Kohsuke Kawaguchi
 */
public interface ListIterator<E> {
    boolean hasNext();
    E next() throws SAXException, JAXBException;
}

package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
abstract class PropertyImpl<BeanT> implements Property<BeanT> {

    /**
     * @see #isId()
     */
    protected final boolean isId;

    /**
     * Name of this field.
     */
    protected final String fieldName;

    public PropertyImpl(JAXBContextImpl context, RuntimePropertyInfo prop) {
        // even though we don't use the context parameter in this constructor,
        // some other constructors do, and when we create a property object
        // we blindly pass it as a parameter.
        // declaring it here guarantees that all the derived classes will have
        // the same parameter.
        isId = prop.id()==ID.ID;
        fieldName = prop.getName();
    }

    public final boolean isId() {
        return isId;
    }

    public void serializeBody(BeanT o, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
    }

    public void serializeAttributes(BeanT o, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
    }

    public void serializeURIs(BeanT o, XMLSerializer w) throws SAXException, AccessorException {
    }

    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        // default implementation. should be overrided
        return null;
    }

    public void wrapUp() {
        // noop
    }
}

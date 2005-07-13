package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * {@link Property} implementation for {@link AttributePropertyInfo}.
 *
 * <p>
 * This one works for both leaves and nodes, scalars and arrays.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class AttributeProperty<BeanT> extends PropertyImpl<BeanT> {

    private final boolean required;

    /**
     * Attribute name.
     */
    public final Name attName;

    /**
     * Heart of the conversion logic.
     */
    public final TransducedAccessor<BeanT> xacc;

    private final Accessor acc;

    public AttributeProperty(JAXBContextImpl p, RuntimeAttributePropertyInfo prop) {
        super(p,prop);
        this.required = prop.isRequired();
        this.attName = p.nameBuilder.createAttributeName(prop.getXmlName());
        this.xacc = TransducedAccessor.get(prop);
        this.acc = prop.getAccessor();   // we only use this for binder, so don't waste memory by optimizing
    }

    public void serializeAttributes(BeanT o, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        if(xacc.hasValue(o))
            w.attribute(attName,xacc.print(o).toString());
    }

    public void serializeURIs(BeanT o, XMLSerializer w) throws AccessorException, SAXException {
        if(xacc.useNamespace() && xacc.hasValue(o))
            xacc.declareNamespace(o,w);
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chainElem, QNameMap<Unmarshaller.Handler> handlers) {
    }

   
    public PropertyKind getKind() {
        return PropertyKind.ATTRIBUTE;
    }

    public void reset(BeanT o) throws AccessorException {
        acc.set(o,null);
    }

    public String getIdValue(BeanT bean) throws AccessorException, SAXException {
        return xacc.print(bean).toString();
    }
}

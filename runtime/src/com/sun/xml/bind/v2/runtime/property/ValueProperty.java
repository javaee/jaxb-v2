package com.sun.xml.bind.v2.runtime.property;


import java.io.IOException;

import javax.xml.bind.annotation.XmlValue;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

import org.xml.sax.SAXException;

/**
 * {@link Property} implementation for {@link XmlValue} properties.
 *
 * <p>
 * This one works for both leaves and nodes, scalars and arrays.
 *
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
final class ValueProperty<BeanT,ListT,ItemT> extends PropertyImpl<BeanT> {

    /**
     * Heart of the conversion logic.
     */
    private final TransducedAccessor<BeanT> xacc;


    public ValueProperty(JAXBContextImpl grammar, RuntimeValuePropertyInfo prop) {
        super(grammar,prop);
        xacc = TransducedAccessor.get(prop);
    }

    public final void serializeBody(BeanT o, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        if(xacc.hasValue(o)) {
            w.text(xacc.print(o),fieldName);
        }
    }

    public Unmarshaller.Handler createUnmarshallerHandler(JAXBContextImpl grammar, Unmarshaller.Handler tail){
         return new Unmarshaller.TextHandler(xacc,Unmarshaller.ERROR, tail);
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chainElem, QNameMap<Unmarshaller.Handler> handlers) {
    }

    public PropertyKind getKind() {
        return PropertyKind.VALUE;
    }

    public void reset(BeanT o) {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    public String getIdValue(BeanT bean) throws AccessorException, SAXException {
        return xacc.print(bean).toString();
    }

}

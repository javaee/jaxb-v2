package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

import org.xml.sax.SAXException;

/**
 * {@link Property} that contains a leaf value.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class SingleElementLeafProperty<BeanT> extends PropertyImpl<BeanT> {

    private final Name tagName;
    private final boolean nillable;
    private final Accessor acc;
    private final String defaultValue;
    private final TransducedAccessor<BeanT> xacc;

    public SingleElementLeafProperty(JAXBContextImpl context, RuntimeElementPropertyInfo prop) {
        super(context,prop);
        RuntimeTypeRef ref = prop.getTypes().get(0);
        tagName = context.nameBuilder.createElementName(ref.getTagName());
        assert tagName!=null;
        nillable = ref.isNillable();
        defaultValue = ref.getDefaultValue();
        this.acc = prop.getAccessor().optimize();

        xacc = TransducedAccessor.get(ref);
        assert xacc!=null;
    }

    public void reset(BeanT o) throws AccessorException {
        acc.set(o,null);
    }

    public String getIdValue(BeanT bean) throws AccessorException, SAXException {
        return xacc.print(bean).toString();
    }

    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        boolean hasValue = xacc.hasValue(o);
        if(hasValue) {
            xacc.writeLeafElement(o,tagName,fieldName,w);
        } else
        if(nillable) {
            w.startElement(tagName,null);
            w.writeXsiNilTrue();
            w.endElement();
        }
    }

    public Unmarshaller.Handler createUnmarshallerHandler(JAXBContextImpl grammar, Unmarshaller.Handler tail) {
        Unmarshaller.Handler cont = tail;

        Unmarshaller.Handler ee = new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR,tail);
        Unmarshaller.Handler text = new Unmarshaller.TextHandler(xacc,Unmarshaller.ERROR,ee);

        if(nillable) {
            tail = new Unmarshaller.SingleXsiNilHandler(text,ee,acc);
        } else
            tail = text;

        tail = new Unmarshaller.EnterElementHandler(tagName,false,defaultValue,cont,tail);

        return tail;
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        Unmarshaller.Handler tail = createUnmarshallerHandler(chain.context,chain.tail);
        chain.tail = tail;
        handlers.put(tagName,tail);
    }


    public PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(tagName.equals(nsUri,localName))
            return acc;
        else
            return null;
    }
}

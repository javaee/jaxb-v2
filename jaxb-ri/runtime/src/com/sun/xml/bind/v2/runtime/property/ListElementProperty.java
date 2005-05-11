package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

import org.xml.sax.SAXException;

import static com.sun.xml.bind.v2.runtime.property.Unmarshaller.ERROR;

/**
 * {@link Property} implementation for {@link ElementPropertyInfo} whose
 * {@link ElementPropertyInfo#isValueList()} is true.
 *
 * @author Kohsuke Kawaguchi
 */
final class ListElementProperty<BeanT,ListT,ItemT> extends ArrayProperty<BeanT,ListT,ItemT> {

    private final Name tagName;
    /**
     * Converts all the values to a list and back.
     */
    private final TransducedAccessor xacc;

    public ListElementProperty(JAXBContextImpl grammar, RuntimeElementPropertyInfo prop) {
        super(grammar, prop);

        assert prop.isValueList();
        assert prop.getTypes().size()==1;   // required by the contract of isValueList
        RuntimeTypeRef ref = prop.getTypes().get(0);

        tagName = grammar.nameBuilder.createElementName(ref.getTagName());

        // transducer for each item
        Transducer xducer = ref.getTransducer();
        // transduced accessor for the whole thing
        xacc = new ListTransducedAccessorImpl(xducer,acc,lister);
    }


    public Unmarshaller.Handler createUnmarshallerHandler(JAXBContextImpl grammar, Unmarshaller.Handler tail) {
        Unmarshaller.Handler item = new Unmarshaller.LeaveElementHandler(ERROR,tail);
        item = new Unmarshaller.TextHandler(xacc,ERROR,item);
        item = new Unmarshaller.EnterElementHandler(tagName,false,null,ERROR,item);

        return item;
    }

    public PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        handlers.put(tagName, createUnmarshallerHandler(chain.context, chain.tail));
    }

    public void serializeBody(BeanT o, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        ListT list = acc.get(o);

        if(list!=null) {
            if(xacc.useNamespace()) {
                w.startElement(tagName,null);
                xacc.declareNamespace(o,w);
                w.endNamespaceDecls(list);
                w.endAttributes();
                w.text(xacc.print(o),fieldName);
                w.endElement();
            } else {
                w.leafElement(tagName,xacc.print(o),fieldName);
            }
        }
    }
}

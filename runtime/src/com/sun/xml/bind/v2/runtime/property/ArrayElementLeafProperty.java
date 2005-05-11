package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

import org.xml.sax.SAXException;

/**
 * {@link ArrayProperty} that contains only one leaf type.
 *
 * <p>
 * This class is optimized for the case where there's only one kind of types
 * in the array and that type is a leaf type.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class ArrayElementLeafProperty<BeanT,ListT,ItemT> extends ArrayElementProperty<BeanT,ListT,ItemT> {

    private final Transducer<ItemT> xducer;

    public ArrayElementLeafProperty(JAXBContextImpl p, RuntimeElementPropertyInfo prop) {
        super(p, prop);

        // unless those are true, use the ArrayElementNodeProperty.
        assert prop.getTypes().size()==1;
//        assert prop.getTypes().get(0).getType().isLeaf(); // this assertion is incorrect in case it's IDREF

        xducer = prop.getTypes().get(0).getTransducer();
        assert xducer!=null;
    }

    public void serializeItem(JaxBeanInfo bi, ItemT item, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        xducer.declareNamespace(item,w);
        w.endNamespaceDecls(item);
        w.endAttributes();
        // TODO: think about the type substitution impact
        TODO.prototype();
        w.text(xducer.print(item), fieldName);
    }
}

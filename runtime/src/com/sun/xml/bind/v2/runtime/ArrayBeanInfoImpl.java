package com.sun.xml.bind.v2.runtime;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;

import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} implementation that binds T[] to a complex type
 * with an element for each item.
 *
 * @author Kohsuke Kawaguchi
 */
final class ArrayBeanInfoImpl  extends JaxBeanInfo {

    private final Class itemType;
    private final JaxBeanInfo itemBeanInfo;
    private final Loader loader;

    public ArrayBeanInfoImpl(JAXBContextImpl owner, RuntimeArrayInfo rai) {
        super(owner,rai,rai.getType(), rai.getTypeName(), false, true, false);
        this.itemType = jaxbType.getComponentType();
        this.itemBeanInfo = owner.getOrCreate(rai.getItemType());

        loader = new ArrayLoader();
    }

    private final class ArrayLoader extends Loader implements Receiver {
        public ArrayLoader() {
            super(false);
        }

        private final XsiTypeLoader itemLoader = new XsiTypeLoader(itemBeanInfo);

        @Override
        public void startElement(UnmarshallingContext.State state, EventArg ea) {
            state.target = new ArrayList();
        }

        public void leaveElement(UnmarshallingContext.State state, EventArg ea) {
            state.target = toArray((List)state.target);
        }

        public void childElement(UnmarshallingContext.State state, EventArg ea) throws SAXException {
            if(ea.matches("","item")) {
                state.loader = itemLoader;
                state.receiver = this;
            } else {
                super.childElement(state,ea);
            }
        }

        public void receive(UnmarshallingContext.State state, Object o) {
            ((List)state.target).add(o);
        }
    };

    protected Object toArray( List list ) {
        int len = list.size();
        Object array = Array.newInstance(itemType,len);
        for( int i=0; i<len; i++ )
            Array.set(array,i,list.get(i));
        return array;
    }

    public void serializeBody(Object array, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        int len = Array.getLength(array);
        for( int i=0; i<len; i++ )  {
            Object item = Array.get(array,i);
            // TODO: check the namespace URI.
            target.startElement("","item",null,null);
            if(item==null) {
                target.writeXsiNilTrue();
            } else {
                target.childAsXsiType(item,"arrayItem",itemBeanInfo);
            }
            target.endElement();
        }
    }

    public final String getElementNamespaceURI(Object array) {
        throw new UnsupportedOperationException();
    }

    public final String getElementLocalName(Object array) {
        throw new UnsupportedOperationException();
    }

    public final Object createInstance(UnmarshallingContext context) {
        // we first create a List and then later convert it to an array
        return new ArrayList();
    }

    public final boolean reset(Object array, UnmarshallingContext context) {
        return false;
    }

    public final String getId(Object array, XMLSerializer target) {
        return null;
    }

    public final void serializeAttributes(Object array, XMLSerializer target) {
        // noop
    }

    public final void serializeRoot(Object array, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        serializeBody(array,target);
    }

    public final void serializeURIs(Object array, XMLSerializer target) {
        // noop
    }

    public final Transducer getTransducer() {
        return null;
    }

    public final Loader getLoader() {
        return loader;
    }
}

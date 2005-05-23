package com.sun.xml.bind.v2.runtime;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.runtime.property.Unmarshaller;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;

import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} implementation that binds T[] to a complex type
 * with an element for each item.
 *
 * @author Kohsuke Kawaguchi
 */
final class ArrayBeanInfoImpl  extends JaxBeanInfo {

    protected final Class itemType;
    protected final JaxBeanInfo itemBeanInfo;
    protected final Unmarshaller.Handler unmarshaller;

    public ArrayBeanInfoImpl(JAXBContextImpl owner, RuntimeArrayInfo rai) {
        super(owner,rai,rai.getType(), rai.getTypeName(), false, true);
        this.itemType = jaxbType.getComponentType();
        this.itemBeanInfo = owner.getOrCreate(rai.getItemType());

        unmarshaller = createUnmarshaller(owner);
    }

    private Unmarshaller.Handler createUnmarshaller(JAXBContextImpl grammar) {
        Unmarshaller.EnterElementHandler enter = new Unmarshaller.EnterElementHandler(
            grammar.nameBuilder.createElementName("", "item"), false, null,
                new Unmarshaller.EpsilonHandler() {
                    protected void handle(UnmarshallingContext context) throws SAXException {
                        context.setTarget(toArray((List)context.getTarget()));
                        context.popContentHandler();
                    }
                }, null );

        enter.next = new Unmarshaller.SpawnChildHandler(itemBeanInfo,
                new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR,enter), false ) {
            protected void onNewChild(Object bean, Object value, UnmarshallingContext context) {
                if(bean==null) {
                    context.setTarget(bean=new ArrayList());
                }
                ((List)bean).add(value);
            }
        };

        return enter;
    }

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

    public final UnmarshallingEventHandler getUnmarshaller(boolean root) {
        return unmarshaller;
    }
}

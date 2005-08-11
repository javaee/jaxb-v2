package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.WildcardLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
class ArrayReferenceNodeProperty<BeanT,ListT,ItemT> extends ArrayERProperty<BeanT,ListT,ItemT> {

    /**
     * Expected element names and what class to unmarshal.
     */
    private final QNameMap<JaxBeanInfo> expectedElements = new QNameMap<JaxBeanInfo>();

    private final boolean isMixed;

    private final DomHandler domHandler;
    private final WildcardMode wcMode;

    public ArrayReferenceNodeProperty(JAXBContextImpl p, RuntimeReferencePropertyInfo prop) {
        super(p, prop, prop.getXmlName(), prop.isCollectionNillable());

        for (RuntimeElement e : prop.getElements()) {
            JaxBeanInfo bi = p.getOrCreate(e);
            expectedElements.put( e.getElementName().getNamespaceURI(),e.getElementName().getLocalPart(), bi );
        }

        isMixed = prop.isMixed();

        if(prop.getWildcard()!=null) {
            domHandler = (DomHandler) ClassFactory.create(prop.getDOMHandler());
            wcMode = prop.getWildcard();
        } else {
            domHandler = null;
            wcMode = null;
        }
    }

    protected final void serializeListBody(BeanT o, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException {
        ListIterator<ItemT> itr = lister.iterator(list, w);

        while(itr.hasNext()) {
            try {
                ItemT item = itr.next();
                if (item != null) {
                    if(isMixed && item.getClass()==String.class) {
                        w.text((String)item,null);
                    } else {
                        JaxBeanInfo bi = w.grammar.getBeanInfo(item,domHandler==null);
                        if(bi.jaxbType==Object.class && domHandler!=null)
                            // even if 'v' is a DOM node, it always derive from Object,
                            // so the getBeanInfo returns BeanInfo for Object
                            w.writeDom(item,domHandler,o,fieldName);
                        else
                            bi.serializeRoot(item,w);
                    }
                }
            } catch (JAXBException e) {
                w.reportError(fieldName,e);
                // recover by ignoring this item
            }
        }
    }

    public void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<ChildLoader> loaders) {
        final int offset = chain.allocateOffset();

        Receiver recv = new ReceiverImpl(offset);

        for( QNameMap.Entry<JaxBeanInfo> n : expectedElements.entrySet() ) {
            final JaxBeanInfo beanInfo = n.getValue();
            loaders.put(n.nsUri,n.localName,new ChildLoader(new XsiTypeLoader(beanInfo),recv));
        }

        if(isMixed) {
            // handler for processing mixed contents.
            loaders.put(TEXT_HANDLER,
                new ChildLoader(new MixedTextoader(recv),null));
        }

        if(domHandler!=null) {
            loaders.put(CATCH_ALL,
                new ChildLoader(new WildcardLoader(domHandler,wcMode),recv));
        }
    }

    private static final class MixedTextoader extends Loader {

        private final Receiver recv;

        public MixedTextoader(Receiver recv) {
            super(true);
            this.recv = recv;
        }

        public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
            recv.receive(state,text.toString());
        }
    }


    public PropertyKind getKind() {
        return PropertyKind.REFERENCE;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(wrapperTagName!=null) {
            if(wrapperTagName.equals(nsUri,localName))
                return acc;
        } else {
            if(expectedElements.containsKey(nsUri,localName))
                return acc;
        }
        return null;
    }
}

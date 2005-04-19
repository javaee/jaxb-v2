package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.annotation.DomHandler;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.WildcardUnmarshaller;

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
        super(p, prop, prop.getXmlName());

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

    public void serializeBody(BeanT o, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        ListT list = acc.get(o);

        if(list!=null) {
            if(tagName!=null) {
                w.startElement(tagName,null);
                w.endNamespaceDecls();
                w.endAttributes();
            }

            ListIterator<ItemT> itr = lister.iterator(list, w);

            while(itr.hasNext()) {
                try {
                    ItemT item = itr.next();
                    if (item != null) {
                        if(isMixed && item.getClass()==String.class) {
                            w.text((String)item,null);
                        } else {
                            JaxBeanInfo bi = w.grammar.getBeanInfo(item,domHandler==null);
                            if(bi!=null)
                                bi.serializeRoot(item,w);
                            else
                                w.writeDom(item,domHandler,o,fieldName);
                        }
                    }
                } catch (JAXBException e) {
                    w.reportError(fieldName,e);
                    // recover by ignoring this item
                }
            }

            if(tagName!=null)
                w.endElement();
        }
    }

    public void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        final int offset = chain.allocateOffset();

        for( QNameMap.Entry<JaxBeanInfo> n : expectedElements.entrySet() ) {
            final JaxBeanInfo beanInfo = n.getValue();
            Unmarshaller.Handler spawnChildHandler = new Unmarshaller.ForkHandler(Unmarshaller.ERROR,chain.tail) {
                public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
                    spawnChild(context,beanInfo,true).enterElement(context,arg);
                }

                @Override
                public void leaveChild(UnmarshallingContext context, Object child) throws SAXException {
                    context.getScope(offset).add(acc,lister,(ItemT)child);
                    context.setCurrentHandler(next);
                }

                protected Unmarshaller.Handler forwardTo(Unmarshaller.EventType event) {
                    if(event==Unmarshaller.EventType.ENTER_ELEMENT)
                        return this;
                    else
                        return super.forwardTo(event);
                }
            };
            handlers.put(n.nsUri,n.localName,  spawnChildHandler);
        }

        if(isMixed) {
            // handler for processing mixed contents.
            handlers.put(TEXT_HANDLER,new Unmarshaller.RawTextHandler(Unmarshaller.ERROR,chain.tail) {
                public void processText(UnmarshallingContext context, CharSequence s) throws SAXException {
                    context.getScope(offset).add(acc,lister,s.toString());
                }
            });
        }

        if(domHandler!=null) {
            handlers.put(CATCH_ALL,new WildcardUnmarshallerImpl(domHandler,wcMode,chain.tail,offset));
        }
    }

    public PropertyKind getKind() {
        return PropertyKind.REFERENCE;
    }

    private final class WildcardUnmarshallerImpl extends WildcardUnmarshaller {
        private final int offset;

        WildcardUnmarshallerImpl(DomHandler domHandler, WildcardMode wcmode, Unmarshaller.Handler next, int offset) {
            super(domHandler, wcmode, next);
            this.offset = offset;
        }

        public void onDone(UnmarshallingContext context, Object element) throws SAXException {
            super.onDone(context,element);
            context.getScope(offset).add(acc,lister,element);
        }
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(tagName!=null) {
            if(tagName.equals(nsUri,localName))
                return acc;
        } else {
            if(expectedElements.containsKey(nsUri,localName))
                return acc;
        }
        return null;
    }
}

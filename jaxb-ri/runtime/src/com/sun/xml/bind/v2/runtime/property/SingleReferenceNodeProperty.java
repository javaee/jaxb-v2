package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.stream.XMLStreamException;

import javax.xml.bind.annotation.DomHandler;
import com.sun.xml.bind.api.AccessorException;
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
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.WildcardUnmarshaller;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
final class SingleReferenceNodeProperty<BeanT,ValueT> extends PropertyImpl<BeanT> {

    private final Accessor<BeanT,ValueT> acc;

    private final QNameMap<JaxBeanInfo> expectedElements = new QNameMap<JaxBeanInfo>();

    private final DomHandler domHandler;
    private final WildcardMode wcMode;

    public SingleReferenceNodeProperty(JAXBContextImpl p, RuntimeReferencePropertyInfo prop) {
        super(p,prop);
        acc = prop.getAccessor().optimize();

        for (RuntimeElement e : prop.getElements()) {
            expectedElements.put( e.getElementName(), p.getOrCreate(e) );
        }

        if(prop.getWildcard()!=null) {
            domHandler = (DomHandler) ClassFactory.create(prop.getDOMHandler());
            wcMode = prop.getWildcard();
        } else {
            domHandler = null;
            wcMode = null;
        }
    }

    public void reset(BeanT bean) throws AccessorException {
        acc.set(bean,null);
    }

    public String getIdValue(BeanT beanT) {
        return null;
    }

    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ValueT v = acc.get(o);
        if(v!=null) {
            try {
                JaxBeanInfo bi = w.grammar.getBeanInfo(v,domHandler==null);
                if(bi!=null)
                    bi.serializeRoot(v,w);
                else
                    w.writeDom(v,domHandler,o,fieldName);
            } catch (JAXBException e) {
                w.reportError(fieldName,e);
                // recover by ignoring this property
            }
        }
    }

    private Unmarshaller.Handler createUnmarshaller( Unmarshaller.Handler fallthrough,Unmarshaller.Handler next ) {
        return new Unmarshaller.ForkHandler(fallthrough,next) {
            public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
                // TODO: this is somewhat inefficient when launched from ElementDispatcher
                // additional check is redundant
                JaxBeanInfo target = expectedElements.get(arg.uri,arg.local);
                if(target==null) {
                    super.enterElement(context,arg);
                } else {
                    context.setCurrentHandler(this);
                    spawnChild(context,target,true).enterElement(context,arg);
                    return;
                }
            }

            @Override
            public void leaveChild(UnmarshallingContext context, Object child) throws SAXException {
                try {
                    acc.set((BeanT)context.getTarget(),(ValueT)child);
                } catch (AccessorException e) {
                    handleGenericException(e);
                }
                context.setCurrentHandler(next);
            }

            protected Unmarshaller.Handler forwardTo(Unmarshaller.EventType event) {
                if(event==Unmarshaller.EventType.ENTER_ELEMENT)
                    return this;
                else
                    return super.forwardTo(event);
            }
        };
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        for (QNameMap.Entry<JaxBeanInfo> n : expectedElements.entrySet())
            handlers.put(n.nsUri,n.localName,new Unmarshaller.SpawnChildSetHandler(n.getValue(),chain.tail,true,acc));
        if(domHandler!=null)
            handlers.put(CATCH_ALL,new WildcardUnmarshallerImpl(domHandler,wcMode,chain.tail));

    }

    public PropertyKind getKind() {
        return PropertyKind.REFERENCE;
    }

    // TODO: revisit leaveChild method. Maybe it's easier if that can be specified separately
    // instead of just calling the method on the parent handler
    private final class WildcardUnmarshallerImpl extends WildcardUnmarshaller {
        public WildcardUnmarshallerImpl(DomHandler domHandler, WildcardMode mode, Unmarshaller.Handler next) {
            super(domHandler, mode, next);
        }

        public void onDone(UnmarshallingContext context, Object element) throws SAXException {
            super.onDone(context,element);
            try {
                acc.set((BeanT)context.getTarget(),(ValueT)element);
            } catch (AccessorException e) {
                handleGenericException(e);
            }
        }
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(expectedElements.containsKey(nsUri,localName))
            return acc;
        else
            return null;
    }
}

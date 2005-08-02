package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeMapPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
final class SingleMapNodeProperty<BeanT,ValueT extends Map> extends PropertyImpl<BeanT> {

    private final Accessor<BeanT,ValueT> acc;
    /**
     * The tag name that surrounds the whole property.
     */
    private final Name tagName;
    /**
     * The tag name that corresponds to the 'entry' element.
     */
    private final Name entryTag;
    private final Name keyTag;
    private final Name valueTag;

    private final boolean nillable;
    private RuntimeMapPropertyInfo prop;   // reset to null in the wrapUp method to avoid memory leak

    private JaxBeanInfo keyBeanInfo;
    private JaxBeanInfo valueBeanInfo;

    /**
     * The implementation class for this property.
     * If the property is null, we create an instance of this class.
     */
    private final Class<? extends ValueT> mapImplClass;

    public SingleMapNodeProperty(JAXBContextImpl context, RuntimeMapPropertyInfo prop) {
        super(context, prop);
        acc = prop.getAccessor().optimize();
        this.prop = prop;
        this.tagName = context.nameBuilder.createElementName(prop.getXmlName());
        this.entryTag = context.nameBuilder.createElementName("","entry");
        this.keyTag = context.nameBuilder.createElementName("","key");
        this.valueTag = context.nameBuilder.createElementName("","value");
        this.nillable = prop.isCollectionNillable();
        this.keyBeanInfo = context.getOrCreate(prop.getKeyType());
        this.valueBeanInfo = context.getOrCreate(prop.getValueType());

        // infer the implementation class
        Class<ValueT> sig = ReflectionNavigator.REFLECTION.erasure(prop.getRawType());
        mapImplClass = ClassFactory.inferImplClass(sig,knownImplClasses);
        // TODO: error check for mapImplClass==null
        // what is the error reporting path for this part of the code?
    }

    private static final Class[] knownImplClasses = {
        HashMap.class, TreeMap.class, LinkedHashMap.class
    };

    public void reset(BeanT bean) throws AccessorException {
        acc.set(bean,null);
    }


    /**
     * A Map property can never be ID.
     */
    public String getIdValue(BeanT bean) {
        return null;
    }

    public PropertyKind getKind() {
        return PropertyKind.MAP;
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        Unmarshaller.Handler tail = chain.tail;

        // handle </items>
        tail = new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR,tail) {
            public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
                context.endState();
                super.leaveElement(context, arg);
            }
        };

        // handle the body (entry*)
        tail = new ElementDispatcher( chain.context,
                Collections.singletonList(new ChildElementUnmarshallerBuilder() {
                    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
                        buildUnmarshallerForEntry(chain,handlers);
                    }
                }),
                tail);

        // handle <items>
        tail = new Unmarshaller.EnterElementHandler(tagName,true,null,Unmarshaller.ERROR,tail) {
            protected void act(UnmarshallingContext context) throws SAXException {
                // create or obtain the Map object
                try {
                    BeanT target = context.<BeanT>getTarget();
                    ValueT map = acc.get(target);
                    if(map==null) {
                        map = ClassFactory.create(mapImplClass);
                        acc.set(target,map);
                    }
                    map.clear();

                    context.startState(map);   // this state is used to store Map object.
                } catch (AccessorException e) {
                    // recover from error by setting a dummy Map that receives and discards the values
                    context.handleError(e);
                    context.startState(new HashMap());
                }

                super.act(context);
            }
        };

        handlers.put(tagName,tail);
    }

    private void buildUnmarshallerForEntry(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        Unmarshaller.Handler tail = chain.tail;

        // handle </entry>
        tail = new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR, tail) {
            public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
                Object[] keyValue = context.getState();
                context.endState(); // finish the state for the entry
                ((Map)context.getState()).put(keyValue[0],keyValue[1]);
                super.leaveElement(context, arg);
            }
        };

        // handle the body (key?,value?)
        tail = new ElementDispatcher( chain.context,
                Collections.singletonList(new ChildElementUnmarshallerBuilder() {
                    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
                        buildUnmarshallerForKeyValue(chain,handlers);
                    }
                }),
                tail);

        // handle <entry>
        tail = new Unmarshaller.EnterElementHandler(entryTag,true,null,Unmarshaller.ERROR,tail) {
            protected void act(UnmarshallingContext context) throws SAXException {
                context.startState(new Object[2]);  // this is inefficient
                super.act(context);
            }
        };

        handlers.put(entryTag,tail);
    }

    private void buildUnmarshallerForKeyValue(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        final Unmarshaller.Handler leaveElement = new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR, chain.tail);

        handlers.put(keyTag,  createItemUnmarshaller(keyTag,  prop.getKeyType(),  keyBeanInfo,  leaveElement,0));
        handlers.put(valueTag,createItemUnmarshaller(valueTag,prop.getValueType(),valueBeanInfo,leaveElement,1));
    }

    private final Unmarshaller.Handler createItemUnmarshaller(Name tagName, RuntimeNonElement type, JaxBeanInfo bi, Unmarshaller.Handler tail, final int offset) {
        final Transducer xducer = type.getTransducer();
        Unmarshaller.Handler body;

        if(xducer!=null) {
            body = new Unmarshaller.RawTextHandler(Unmarshaller.ERROR,tail) {
                public void processText(UnmarshallingContext context, CharSequence s) throws SAXException {
                    try {
                        Object[] state = context.getState();
                        state[offset] = xducer.parse(s);
                    } catch (AccessorException e) {
                        handleGenericException(e,true);
                    }
                }
            };
        } else {
            body = new Unmarshaller.SpawnChildHandler(bi,tail,false) {
                protected void onNewChild(Object bean, Object value, UnmarshallingContext context) {
                    Object[] state = context.getState();
                    state[offset] = value;
                }
            };
        }
        return new Unmarshaller.EnterElementHandler(tagName,false,null,Unmarshaller.ERROR,body);
    }


    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ValueT v = acc.get(o);
        if(v!=null) {
            bareStartTag(w,tagName,v);
            for( Map.Entry e : ((Map<?,?>)v).entrySet() ) {
                bareStartTag(w,entryTag,null);

                Object key = e.getKey();
                if(key!=null) {
                    w.startElement(keyTag,key);
                    w.childAsXsiType(key,fieldName,keyBeanInfo);
                    w.endElement();
                }

                Object value = e.getValue();
                if(value!=null) {
                    w.startElement(valueTag,value);
                    w.childAsXsiType(value,fieldName,valueBeanInfo);
                    w.endElement();
                }

                w.endElement();
            }
            w.endElement();
        } else
        if(nillable) {
            w.startElement(tagName,null);
            w.writeXsiNilTrue();
            w.endElement();
        }
    }

    private void bareStartTag(XMLSerializer w, Name tagName, Object peer) throws IOException, XMLStreamException, SAXException {
        w.startElement(tagName,peer);
        w.endNamespaceDecls(peer);
        w.endAttributes();
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(tagName.equals(nsUri,localName))
            return acc;
        return null;
    }

    public void wrapUp() {
        super.wrapUp();
        prop = null;
    }
}

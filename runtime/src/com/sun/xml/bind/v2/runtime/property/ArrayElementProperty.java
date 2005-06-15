package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.Util;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * {@link Property} implementation for multi-value property that maps to an element.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ArrayElementProperty<BeanT,ListT,ItemT> extends ArrayERProperty<BeanT,ListT,ItemT> {

    private final Map<Class,TagAndType> typeMap  = new HashMap<Class,TagAndType>();
    /**
     * Set by the constructor and reset in the {@link #wrapUp()} method.
     */
    private Map<TypeRef<Type,Class>,JaxBeanInfo> refs = new HashMap<TypeRef<Type, Class>, JaxBeanInfo>();
    /**
     * Set by the constructor and reset in the {@link #wrapUp()} method.
     */
    protected RuntimeElementPropertyInfo prop;

    /**
     * Tag name used when we see null in the collection. Can be null.
     */
    private final Name nillableTagName;

    protected ArrayElementProperty(JAXBContextImpl grammar, RuntimeElementPropertyInfo prop) {
        super(grammar, prop, prop.getXmlName(), prop.isCollectionNillable());
        assert prop!=null;
        this.prop = prop;

        List<? extends RuntimeTypeRef> types = prop.getTypes();

        Name n = null;

        for (RuntimeTypeRef typeRef : types) {
            Class type = (Class)typeRef.getTarget().getType();
            if(type.isPrimitive())
                type = Util.primitiveToBox.get(type);

            JaxBeanInfo beanInfo = grammar.getOrCreate(typeRef.getTarget());
            TagAndType tt = new TagAndType(
                                grammar.nameBuilder.createElementName(typeRef.getTagName()),
                                beanInfo);
            typeMap.put(type,tt);
            refs.put(typeRef,beanInfo);
            if(typeRef.isNillable() && n==null)
                n = tt.tagName;
        }

        nillableTagName = n;
    }

    @Override
    public void wrapUp() {
        super.wrapUp();
        refs = null;
        prop = null;    // avoid keeping model objects live
    }

    protected void serializeListBody(BeanT beanT, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException, AccessorException {
        ListIterator<ItemT> itr = lister.iterator(list, w);

        boolean isIdref = itr instanceof Lister.IDREFSIterator; // UGLY

        while(itr.hasNext()) {
            try {
                ItemT item = itr.next();
                if (item != null) {
                    Class itemType = item.getClass();
                    if(isIdref)
                        // This should be the only place where we need to be aware
                        // that the iterator is iterating IDREFS.
                        itemType = ((Lister.IDREFSIterator)itr).last().getClass();

                    // normally, this returns non-null
                    TagAndType tt = typeMap.get(itemType);
                    while(tt==null && itemType!=null) {
                        // otherwise we'll just have to try the slow way
                        itemType = itemType.getSuperclass();
                        tt = typeMap.get(itemType);
                    }

                    if(tt==null) {
                        // item is not of the expected type.
                        w.reportError(new ValidationEventImpl(ValidationEvent.ERROR,
                            Messages.UNEXPECTED_JAVA_TYPE.format(
                                item.getClass().getName(),
                                getExpectedClassNameList()
                            ),
                            w.getCurrentLocation(fieldName)));
                        continue;
                    }

                    w.startElement(tt.tagName,null);
                    serializeItem(tt.beanInfo,item,w);
                    w.endElement();
                } else {
                    if(nillableTagName!=null) {
                        w.startElement(nillableTagName,null);
                        w.writeXsiNilTrue();
                        w.endElement();
                    }
                }
            } catch (JAXBException e) {
                w.reportError(fieldName,e);
                // recover by ignoring this item
            }
        }
    }

    /**
     * Compute the list of the expected class names. Used for error diagnosis.
     */
    private String getExpectedClassNameList() {
        StringBuilder buf = new StringBuilder();
        for (Class c : typeMap.keySet()) {
            if(buf.length()>0)  buf.append(',');
            buf.append(c.getName());
        }
        return buf.toString();
    }

    /**
     * Serializes one item of the property.
     */
    protected abstract void serializeItem(JaxBeanInfo expected, ItemT item, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException;


    public void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {

        final Unmarshaller.Handler tail = chain.tail;
        // all items go to the same lister,
        // so they should share the same offset.
        int offset = chain.allocateOffset();

        Unmarshaller.Handler leaveElement = new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR, tail);

        for (RuntimeTypeRef typeRef : prop.getTypes()) {

            Name tagName = chain.context.nameBuilder.createElementName(typeRef.getTagName());
            Unmarshaller.Handler item = createItemUnmarshaller(typeRef,leaveElement,offset);

            if(typeRef.isNillable())
                item = new Unmarshaller.ArrayXsiNilHandler(item,leaveElement,acc,offset,lister);

            item = new Unmarshaller.EnterElementHandler(tagName,
                false,
                typeRef.getDefaultValue(),
                Unmarshaller.ERROR,item);

            handlers.put(tagName, item);
        }
    }

    public final PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    /**
     * Creates unmarshaller handler that unmarshals the body of the item.
     *
     * <p>
     * This will be sandwiched into <item> ... </item>.
     *
     * <p>
     * When unmarshalling the body of item, the Pack of {@link Lister} is available
     * as the handler state.
     *
     * @param typeRef
     *      the type of the child for which we are creating the unmarshaller.
     */
    private final Unmarshaller.Handler createItemUnmarshaller(RuntimeTypeRef typeRef, Unmarshaller.Handler tail, final int offset) {
        if(PropertyFactory.isLeaf(typeRef.getSource())) {
            final Transducer xducer = typeRef.getTransducer();
            return new Unmarshaller.RawTextHandler(Unmarshaller.ERROR,tail) {
                public void processText(UnmarshallingContext context, CharSequence s) throws SAXException {
                    try {
                        context.getScope(offset).add(acc,lister,xducer.parse(s));
                    } catch (AccessorException e) {
                        handleGenericException(e,true);
                    }
                }
            };
        } else {
            return new Unmarshaller.SpawnChildHandler(refs.get(typeRef),tail,false) {
                protected void onNewChild(Object bean, Object value, UnmarshallingContext context) throws SAXException {
                    context.getScope(offset).add(acc,lister,value);
                }
            };
        }
    }

    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(wrapperTagName!=null) {
            if(wrapperTagName.equals(nsUri,localName))
                return acc;
        } else {
            for (TagAndType tt : typeMap.values()) {
                if(tt.tagName.equals(nsUri,localName))
                    return acc;
            }
        }
        return null;
    }
}

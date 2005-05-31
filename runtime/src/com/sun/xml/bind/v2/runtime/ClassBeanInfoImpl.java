package com.sun.xml.bind.v2.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.property.AttributeDispatcher;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.ElementDispatcher;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.property.Unmarshaller;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * {@link JaxBeanInfo} implementation for j2s bean.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ClassBeanInfoImpl<BeanT> extends JaxBeanInfo<BeanT> {

    /**
     * Properties of this bean class but not its ancestor classes.
     */
    protected final Property[] properties;

    /**
     * Non-null if this bean has an ID property.
     */
    private Property idProperty;

    /**
     * Immutable configured unmarshaller for this class.
     *
     * <p>
     * Set from the link method, but considered final.
     */
    private /*final*/ Unmarshaller.Handler typeUnmarshaller;
    /**
     * Unmarshaller to unmarshal this bean as an element.
     * If this bean is not bound to an element, null.
     *
     * <p>
     * Set from the link method, but considered final.
     */
    private /*final*/ Unmarshaller.Handler elementUnmarshaller;

    // TODO: revisit and try to eliminate this reference for better memory footproint
    /**
     * Set only until the link phase to avoid leaking memory.
     */
    private RuntimeClassInfo ci;

    private final Accessor declaredAttWildcard;
    private final Transducer xducer;
    protected final ClassBeanInfoImpl superClazz;

    private final Accessor<BeanT,Locator> xmlLocatorField;

    private final Name tagName;


    /*package*/ ClassBeanInfoImpl(JAXBContextImpl owner, RuntimeClassInfo ci) {
        super(owner,ci,ci.getClazz(),ci.getTypeName(),ci.isElement(),false);

        this.ci = ci;
        if(ci.declaresAttributeWildcard())
            this.declaredAttWildcard = ci.getAttributeWildcard();
        else
            this.declaredAttWildcard = null;
        this.xducer = ci.getTransducer();

        if(ci.getBaseClass()==null)
            this.superClazz = null;
        else
            this.superClazz = owner.getOrCreate(ci.getBaseClass());

        if(superClazz!=null && superClazz.xmlLocatorField!=null)
            xmlLocatorField = superClazz.xmlLocatorField;
        else
            xmlLocatorField = ci.getLocatorField();

        // create property objects
        Collection<? extends RuntimePropertyInfo> ps = ci.getProperties();
        this.properties = new Property[ps.size()];
        int idx=0;
        boolean elementOnly = true;
        for( RuntimePropertyInfo info : ps ) {
            Property p = PropertyFactory.create(owner,info);
            if(info.id()==ID.ID)
                idProperty = p;
            properties[idx++] = p;
            elementOnly &= info.elementOnlyContent();
        }
        // super class' idProperty might not be computed at this point,
        // so check that later

        hasElementOnlyContentModel( elementOnly );
        // again update this value later when we know that of the super class

        if(ci.isElement())
            tagName = owner.nameBuilder.createElementName(ci.getElementName());
        else
            tagName = null;
    }

    @Override
    protected void link(JAXBContextImpl grammar) {
        if(typeUnmarshaller!=null)      return; // avoid linkng twice.

        if(superClazz!=null)
            superClazz.link(grammar);

        // create unmarshaller. our unmarshaller is immutable
        typeUnmarshaller = createTypeUnmarshaller(grammar,Unmarshaller.REVERT_TO_PARENT);

        if(ci.isElement()) {
            Element e = ci.asElement();
            Unmarshaller.Handler te = Unmarshaller.REVERT_TO_PARENT;
            te = new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR,te);
            te = createTypeUnmarshaller(grammar,te);
            te = new Unmarshaller.EnterElementHandler(
                grammar.nameBuilder.createElementName(e.getElementName()),
                hasElementOnlyContentModel(),
                null /*TODO: or capture this in @XmlRootElement */,
                Unmarshaller.ERROR,te);

            elementUnmarshaller = te;
        } else {
            elementUnmarshaller = null;
        }

        // propagate values from super class
        if(superClazz!=null) {
            if(idProperty==null)
                idProperty = superClazz.idProperty;

            if(!superClazz.hasElementOnlyContentModel())
                hasElementOnlyContentModel(false);
        }
    }

    public void wrapUp() {
        for (Property p : properties)
            p.wrapUp();
        ci = null;
        super.wrapUp();
    }

    private Unmarshaller.Handler createTypeUnmarshaller(JAXBContextImpl grammar,Unmarshaller.Handler tail) {
        Unmarshaller.Handler valueHandler = null;
        List <Property> propList = new ArrayList<Property>();

        for (ClassBeanInfoImpl bi = this; bi != null; bi = bi.superClazz) {
            for (int i = bi.properties.length - 1; i >= 0; i--) {
                Property p = bi.properties[i];

                switch(p.getKind()) {
                case ELEMENT:
                case REFERENCE:
                    propList.add(p);
                    break;
                case VALUE:
                    if(valueHandler!=null) {
                        // TODO: only up to one value handler per class.
                        // think about who should be responsible for reporting this as an error
                        throw new UnsupportedOperationException();
                    }
                    valueHandler = p.createUnmarshallerHandler(grammar, tail);
                    break;
                }
            }
        }

        Unmarshaller.Handler handler;
        if (propList.size() > 0 ) {
            if(valueHandler!=null) {
                // TODO: you can't mix elements and values
                // report errors
                throw new UnsupportedOperationException();
            }
            handler = new ElementDispatcher(grammar,propList,tail);
        }

        else
        if(valueHandler!=null)
            handler = valueHandler;
        else
            handler = tail;

        handler = createAttributeHandler(handler);
        return handler;
    }

    /**
     * Creates a chain of handlers to unmarshal attributes,
     * and prepend it in front of the given 'tail'.
     */
    private Unmarshaller.Handler createAttributeHandler(Unmarshaller.Handler tail) {
        List<AttributeProperty> propList = new ArrayList<AttributeProperty>();
        for (ClassBeanInfoImpl bi = this; bi != null; bi = bi.superClazz) {
            for (int i = bi.properties.length - 1; i >= 0; i--) {
                Property p = bi.properties[i];
                if (p.getKind()==PropertyKind.ATTRIBUTE) {
                    propList.add((AttributeProperty)p);
                }
            }
        }
        Accessor<?,Map<QName,Object>> attw = ci.getAttributeWildcard();
        if(propList.isEmpty() && attw==null)
            // no attributes
            return tail;
        else {
            return new AttributeDispatcher(propList,attw,tail,tail);
        }
    }

    public String getElementNamespaceURI(BeanT bean) {
        return tagName.nsUri;
    }

    public String getElementLocalName(BeanT bean) {
        return tagName.localName;
    }

    public BeanT createInstance(UnmarshallingContext context) throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException {
        BeanT bean = ClassFactory.create0(jaxbType);
        if(xmlLocatorField!=null)
            // need to copy because Locator is mutable
            try {
                xmlLocatorField.set(bean,new LocatorImpl(context.getLocator()));
            } catch (AccessorException e) {
                context.handleError(e);
            }
        return bean;
    }

    public boolean reset(BeanT bean, UnmarshallingContext context) throws SAXException {
        try {
            if(superClazz!=null)
                superClazz.reset(bean,context);
            for( Property p : properties )
                p.reset(bean);
            return true;
        } catch (AccessorException e) {
            context.handleError(e);
            return false;
        }
    }

    public String getId(BeanT bean, XMLSerializer target) throws SAXException {
        if(idProperty!=null) {
            try {
                return idProperty.getIdValue(bean);
            } catch (AccessorException e) {
                target.reportError(null,e);
            }
        }
        return null;
    }

    public void serializeRoot(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        if(tagName==null)
            serializeBody(bean,target);
        else {
            target.startElement(tagName,bean);
            target.childAsSoleContent(bean,null);
            target.endElement();
        }
    }

    public void serializeBody(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        if(superClazz!=null)
            superClazz.serializeBody(bean,target);
        try {
            for( Property p : properties )
                p.serializeBody(bean,target, null);
        } catch (AccessorException e) {
            target.reportError(null,e);
        }
    }

    public void serializeAttributes(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        if(superClazz!=null)
            superClazz.serializeAttributes(bean,target);
        try {
            for( Property p : properties )
                p.serializeAttributes(bean,target);

            if(declaredAttWildcard!=null) {
                Map<QName,Object> map = (Map<QName,Object>)declaredAttWildcard.get(bean);
                target.attWildcardAsAttributes(map,null);
            }
        } catch (AccessorException e) {
            target.reportError(null,e);
        }
    }

    public void serializeURIs(BeanT bean, XMLSerializer target) throws SAXException {
        if(superClazz!=null)
            superClazz.serializeURIs(bean,target);
        try {
            for( Property p : properties )
                p.serializeURIs(bean,target);

            if(declaredAttWildcard!=null) {
                Map<QName,Object> map = (Map<QName,Object>)declaredAttWildcard.get(bean);
                target.attWildcardAsURIs(map,null);
            }
        } catch (AccessorException e) {
            target.reportError(null,e);
        }
    }

    public UnmarshallingEventHandler getUnmarshaller(boolean root) {
        if(root)    return elementUnmarshaller;
        else        return typeUnmarshaller;
    }

    public Transducer<BeanT> getTransducer() {
        return xducer;
    }
}

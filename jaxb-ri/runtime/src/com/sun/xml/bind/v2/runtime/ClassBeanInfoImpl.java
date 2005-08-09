package com.sun.xml.bind.v2.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.FinalArrayList;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
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
     * Immutable configured loader for this class.
     *
     * <p>
     * Set from the link method, but considered final.
     */
    private Loader loader;

    /**
     * Set only until the link phase to avoid leaking memory.
     */
    private RuntimeClassInfo ci;

    private final Accessor inheritedAttWildcard;
    private final Transducer xducer;
    protected final ClassBeanInfoImpl superClazz;

    private final Accessor<BeanT,Locator> xmlLocatorField;

    private final Name tagName;

    /**
     * The {@link AttributeProperty}s for this type and all its ancestors.
     * If {@link JAXBContextImpl#c14nSupport} is true, this is sorted alphabetically.
     */
    private /*final*/ AttributeProperty[] attributeProperties;

    /**
     * {@link Property}s that need to receive {@link Property#serializeURIs(Object, XMLSerializer)} callback.
     */
    private /*final*/ Property[] uriProperties;


    /*package*/ ClassBeanInfoImpl(JAXBContextImpl owner, RuntimeClassInfo ci) {
        super(owner,ci,ci.getClazz(),ci.getTypeName(),ci.isElement(),false,true);

        this.ci = ci;
        this.inheritedAttWildcard = ci.getAttributeWildcard();
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

        setLifecycleFlags();
    }

    @Override
    protected void link(JAXBContextImpl grammar) {
        if(loader!=null)      return; // avoid linkng twice.

        if(superClazz!=null)
            superClazz.link(grammar);

        // create unmarshaller. our unmarshaller is immutable
        loader = createLoader(grammar);

        // propagate values from super class
        if(superClazz!=null) {
            if(idProperty==null)
                idProperty = superClazz.idProperty;

            if(!superClazz.hasElementOnlyContentModel())
                hasElementOnlyContentModel(false);
        }

        // create a list of attribute/URI handlers
        List<AttributeProperty> attProps = new FinalArrayList<AttributeProperty>();
        List<Property> uriProps = new FinalArrayList<Property>();
        for (ClassBeanInfoImpl bi = this; bi != null; bi = bi.superClazz) {
            for (int i = bi.properties.length - 1; i >= 0; i--) {
                Property p = bi.properties[i];
                if(p instanceof AttributeProperty)
                    attProps.add((AttributeProperty) p);
                if(p.hasSerializeURIAction())
                    uriProps.add(p);
            }
        }
        if(grammar.c14nSupport)
            Collections.sort(attProps);

        if(attProps.isEmpty())
            attributeProperties = EMPTY_PROPERTIES;
        else
            attributeProperties = attProps.toArray(new AttributeProperty[attProps.size()]);

        if(uriProps.isEmpty())
            uriProperties = EMPTY_PROPERTIES;
        else
            uriProperties = uriProps.toArray(new Property[uriProps.size()]);
    }

    public void wrapUp() {
        for (Property p : properties)
            p.wrapUp();
        ci = null;
        super.wrapUp();
    }

    private Loader createLoader(JAXBContextImpl grammar) {
        List<Property> propList = new FinalArrayList<Property>();
        List<AttributeProperty> atts = new FinalArrayList<AttributeProperty>();

        for (ClassBeanInfoImpl bi = this; bi != null; bi = bi.superClazz) {
            for (int i = bi.properties.length - 1; i >= 0; i--) {
                Property p = bi.properties[i];

                switch(p.getKind()) {
                case ATTRIBUTE:
                    atts.add((AttributeProperty)p);
                    break;
                case ELEMENT:
                case REFERENCE:
                case MAP:
                case VALUE:
                    propList.add(p);
                    break;
                }
            }
        }

        Accessor<?,Map<QName,String>> attw = ci.getAttributeWildcard();
        return new StructureLoader(grammar,this,propList,atts,attw);
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
        try {
            for( AttributeProperty p : attributeProperties )
                p.serializeAttributes(bean,target);

            if(inheritedAttWildcard!=null) {
                Map<QName,Object> map = (Map<QName,Object>)inheritedAttWildcard.get(bean);
                target.attWildcardAsAttributes(map,null);
            }
        } catch (AccessorException e) {
            target.reportError(null,e);
        }
    }

    public void serializeURIs(BeanT bean, XMLSerializer target) throws SAXException {
        try {
            for( Property p : uriProperties )
                p.serializeURIs(bean,target);

            if(inheritedAttWildcard!=null) {
                Map<QName,Object> map = (Map<QName,Object>)inheritedAttWildcard.get(bean);
                target.attWildcardAsURIs(map,null);
            }
        } catch (AccessorException e) {
            target.reportError(null,e);
        }
    }

    public Loader getLoader() {
        return loader;
    }

    public Transducer<BeanT> getTransducer() {
        return xducer;
    }

    private static final AttributeProperty[] EMPTY_PROPERTIES = new AttributeProperty[0];
}


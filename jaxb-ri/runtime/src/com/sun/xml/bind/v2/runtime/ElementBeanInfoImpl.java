package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.property.Unmarshaller;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;

import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} implementation for {@link RuntimeElementInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
final class ElementBeanInfoImpl extends JaxBeanInfo<JAXBElement> {

    private final Unmarshaller.Handler unmarshaller;

    private final Property property;

    // used to create new instances of JAXBElement.
    private final QName tagName;
    private final Class expectedType;
    private final Class scope;

    ElementBeanInfoImpl(JAXBContextImpl grammar, RuntimeElementInfo rei) {
        super(grammar,rei,(Class<JAXBElement>)rei.getType(),null,true,false);

        this.property = PropertyFactory.create(grammar,rei.getProperty());

//        this.unmarshaller = property.createUnmarshallerHandler(grammar,Unmarshaller.REVERT_TO_PARENT);
        UnmarshallerChain c = new UnmarshallerChain(grammar);
        c.tail = Unmarshaller.REVERT_TO_PARENT;
        QNameMap<Unmarshaller.Handler> result = new QNameMap<Unmarshaller.Handler>();
        property.buildChildElementUnmarshallers(c,result);
        assert result.size()==1;    // ElementBeanInfoImpl only has one tag name
        this.unmarshaller = result.getOne().getValue();


        // TODO: pre-compute these values to improve speed
        tagName = rei.getElementName();
        expectedType = Navigator.REFLECTION.erasure(rei.getContentInMemoryType());
        scope = rei.getScope()==null ? JAXBElement.GlobalScope.class : rei.getScope().getClazz();
    }

    /**
     * The constructor for the sole instanceof {@link JaxBeanInfo} for
     * handling user-created {@link JAXBElement}.
     *
     * Such {@link JaxBeanInfo} is used only for marshalling.
     *
     * This is a hack.
     */
    protected ElementBeanInfoImpl(final JAXBContextImpl grammar) {
        super(grammar,null,JAXBElement.class,null,true,false);
        tagName = null;
        expectedType = null;
        scope = null;

        this.property = new Property<JAXBElement>() {
            public void reset(JAXBElement o) {
                throw new UnsupportedOperationException();
            }

            public void serializeBody(JAXBElement e, XMLSerializer target, Object outerPeer) throws SAXException, IOException, XMLStreamException {
                Class scope = e.getScope();
                if(e.isGlobalScope())   scope = null;
                QName n = e.getName();
                ElementBeanInfoImpl bi = grammar.getElement(scope,n);
                if(bi==null) {
                    // infer what to do from the type
                    JaxBeanInfo tbi;
                    try {
                        tbi = grammar.getBeanInfo(e.getDeclaredType(),true);
                    } catch (JAXBException x) {
                        // if e.getDeclaredType() isn't known to this JAXBContext
                        target.reportError(null,x);
                        return;
                    }
                    Object value = e.getValue();
                    target.startElement(n.getNamespaceURI(),n.getLocalPart(),n.getPrefix(),null);
                    if(value==null) {
                        target.writeXsiNilTrue();
                    } else {
                        target.childAsXsiType(value,"value",tbi);
                    }
                    target.endElement();
                } else {
                    try {
                        bi.property.serializeBody(e,target,e);
                    } catch (AccessorException x) {
                        target.reportError(null,x);
                    }
                }
            }

            public void serializeURIs(JAXBElement o, XMLSerializer target) {
            }

            public String getIdValue(JAXBElement o) {
                return null;
            }

            public PropertyKind getKind() {
                return PropertyKind.ELEMENT;
            }

            public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
            }

            public Accessor getElementPropertyAccessor(String nsUri, String localName) {
                throw new UnsupportedOperationException();
            }

            public void wrapUp() {
            }
        };
        this.unmarshaller = null;
    }

    public String getElementNamespaceURI(JAXBElement e) {
        return e.getName().getNamespaceURI();
    }

    public String getElementLocalName(JAXBElement e) {
        return e.getName().getLocalPart();
    }

    public UnmarshallingEventHandler getUnmarshaller(boolean root) {
        return unmarshaller;
    }

    public final JAXBElement createInstance(UnmarshallingContext context) {
        return new JAXBElement(tagName,expectedType,scope,null );
    }

    public boolean reset(JAXBElement e, UnmarshallingContext context) {
        e.setValue(null);
        return true;
    }

    public String getId(JAXBElement e, XMLSerializer target) {
        // TODO: is this OK? Should we be returning the ID value of the type property?
        /*
            There's one case where we JAXBElement needs to be designated as ID,
            and that is when there's a global element whose type is ID.
        */
        Object o = e.getValue();
        if(o instanceof String)
            return (String)o;
        else
            return null;
    }

    public void serializeBody(JAXBElement element, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        try {
            property.serializeBody(element,target,null);
        } catch (AccessorException x) {
            target.reportError(null,x);
        }
    }

    public void serializeRoot(JAXBElement e, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        serializeBody(e,target);
    }

    public void serializeAttributes(JAXBElement e, XMLSerializer target) {
        // noop
    }

    public void serializeURIs(JAXBElement e, XMLSerializer target) {
        // noop
    }

    public final Transducer<JAXBElement> getTransducer() {
        return null;
    }

    public void wrapUp() {
        super.wrapUp();
        property.wrapUp();
    }

    /** generate lifecycle events for JAXBElements */
    @Override public boolean lookForLifecycleMethods() {
        return true;
    }
}

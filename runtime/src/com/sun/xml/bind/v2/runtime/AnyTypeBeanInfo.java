package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.impl.RuntimeAnyTypeImpl;
import com.sun.xml.bind.v2.runtime.property.Unmarshaller;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;
import com.sun.xml.bind.v2.runtime.unmarshaller.WildcardUnmarshaller;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} for handling <tt>xs:anyType</tt>.
 *
 * @author Kohsuke Kawaguchi
 */
final class AnyTypeBeanInfo extends JaxBeanInfo<Object> {

    public AnyTypeBeanInfo(JAXBContextImpl grammar) {
        super(grammar, RuntimeAnyTypeImpl.theInstance, Object.class, new QName(WellKnownNamespace.XML_SCHEMA,"anyType"), false, true);
    }

    public String getElementNamespaceURI(Object element) {
        throw new UnsupportedOperationException();
    }

    public String getElementLocalName(Object element) {
        throw new UnsupportedOperationException();
    }

    public Object createInstance(UnmarshallingContext context) {
        throw new UnsupportedOperationException();
        // return JAXBContextImpl.createDom().createElementNS("","noname");
    }

    public boolean reset(Object element, UnmarshallingContext context) {
        return false;
//        NodeList nl = element.getChildNodes();
//        while(nl.getLength()>0)
//            element.removeChild(nl.item(0));
//        NamedNodeMap al = element.getAttributes();
//        while(al.getLength()>0)
//            element.removeAttributeNode((Attr)al.item(0));
//        return true;
    }

    public String getId(Object element, XMLSerializer target) {
        return null;
    }

    public void serializeBody(Object element, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        NodeList childNodes = ((Element)element).getChildNodes();
        int len = childNodes.getLength();
        for( int i=0; i<len; i++ ) {
            Node child = childNodes.item(i);
            switch(child.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                target.text(child.getNodeValue(),null);
                break;
            case Node.ELEMENT_NODE:
                target.writeDom((Element)child,domHandler,null,null);
                break;
            }
        }
    }

    public void serializeAttributes(Object element, XMLSerializer target) throws SAXException {
        NamedNodeMap al = ((Element)element).getAttributes();
        int len = al.getLength();
        for( int i=0; i<len; i++ ) {
            Attr a = (Attr)al.item(i);
            // work defensively
            String uri = a.getNamespaceURI();
            if(uri==null)   uri="";
            String local = a.getLocalName();
            String name = a.getName();
            if(local==null) local = name;

            if(name.startsWith("xmlns")) continue;// DOM reports ns decls as attributes

            target.attribute(uri,local,a.getValue());
        }
    }

    public void serializeRoot(Object element, XMLSerializer target) throws SAXException {
        target.writeDom((Element)element,domHandler,null,null);
    }

    public void serializeURIs(Object element, XMLSerializer target) {
        NamedNodeMap al = ((Element)element).getAttributes();
        int len = al.getLength();
        NamespaceContext2 context = target.getNamespaceContext();
        for( int i=0; i<len; i++ ) {
            Attr a = (Attr)al.item(i);
            if( "xmlns".equals(a.getPrefix()) ) {
                context.declareNamespace( a.getValue(), a.getLocalName(), true );
                continue;
            }
            if( "xmlns".equals(a.getName()) ) {
                context.declareNamespace( a.getValue(), "", false );
                continue;
            }
            String nsUri = a.getNamespaceURI();
            if(nsUri!=null && nsUri.length()>0)
                context.declareNamespace( nsUri, a.getPrefix(), true );
        }
    }

    public Transducer<Object> getTransducer() {
        return null;
    }

    public UnmarshallingEventHandler getUnmarshaller(boolean root) {
        final Unmarshaller.Handler base = new WildcardUnmarshaller(domHandler,WildcardMode.SKIP,Unmarshaller.REVERT_TO_PARENT) {
            public void onDone(UnmarshallingContext context, Object element) throws SAXException {
                context.setTarget(element);
                super.onDone(context,element);
                EventArg ea = new EventArg("","noname","noname",null);
                context.getCurrentHandler().leaveElement(context,ea);
            }
        };

        return new Unmarshaller.EpsilonHandler() {
            protected void handle(UnmarshallingContext context) throws SAXException {
                context.setCurrentHandler(base);

                EventArg ea = new EventArg("","noname","noname",context.getUnconsumedAttributes());
                base.enterElement(context,ea);
            }
        };
    }

    private static final W3CDomHandler domHandler = new W3CDomHandler();
}

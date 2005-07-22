package com.sun.xml.bind.v2.runtime;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.FinalArrayList;
import com.sun.xml.bind.v2.runtime.property.Unmarshaller;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;

import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} implementation that binds T[] to a list of simple types.
 * 
 * @author Kohsuke Kawaguchi
 */
final class ValueListBeanInfoImpl extends JaxBeanInfo {

    private final Class itemType;
    private final Unmarshaller.Handler unmarshaller;

    private final Transducer xducer;    // for items

    public ValueListBeanInfoImpl(JAXBContextImpl owner, Class arrayType) throws JAXBException {
        super(owner, null, arrayType, null, false, true);
        this.itemType = jaxbType.getComponentType();
        this.xducer = owner.getBeanInfo(arrayType.getComponentType(),true).getTransducer();
        assert xducer!=null;
        unmarshaller = createUnmarshaller();
    }

    private Unmarshaller.Handler createUnmarshaller() {
        return new Unmarshaller.RawTextHandler(Unmarshaller.REVERT_TO_PARENT,Unmarshaller.REVERT_TO_PARENT) {
            public void processText(UnmarshallingContext context, CharSequence s) throws SAXException {
                List<Object> r = new FinalArrayList<Object>();

                int idx = 0;
                int len = s.length();

                while(true) {
                    int p = idx;
                    while( p<len && !WhiteSpaceProcessor.isWhiteSpace(s.charAt(p)) )
                        p++;

                    CharSequence token = s.subSequence(idx,p);
                    if (!token.equals(""))
                        try {
                            r.add(xducer.parse(token));
                        } catch (AccessorException e) {
                            handleGenericException(e,true);
                            continue;   // move on to next
                        }

                    if(p==len)      break;  // done

                    while( p<len && WhiteSpaceProcessor.isWhiteSpace(s.charAt(p)) )
                        p++;
                    if(p==len)      break;  // done

                    idx = p;
                }

                context.setTarget(toArray(r));
            }
        };
    }

    private Object toArray( List list ) {
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
            try {
                target.text(xducer.print(item),"arrayItem");
            } catch (AccessorException e) {
                target.reportError("arrayItem",e);
            }
        }
    }

    public final void serializeURIs(Object array, XMLSerializer target) throws SAXException {
        if(xducer.useNamespace()) {
            int len = Array.getLength(array);
            for( int i=0; i<len; i++ )  {
                Object item = Array.get(array,i);
                try {
                    xducer.declareNamespace(item,target);
                } catch (AccessorException e) {
                    target.reportError("arrayItem",e);
                }
            }
        }
    }

    public final String getElementNamespaceURI(Object array) {
        throw new UnsupportedOperationException();
    }

    public final String getElementLocalName(Object array) {
        throw new UnsupportedOperationException();
    }

    public final Object createInstance(UnmarshallingContext context) {
        throw new UnsupportedOperationException();
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

    public final Transducer getTransducer() {
        return null;
    }

    public final UnmarshallingEventHandler getUnmarshaller(boolean root) {
        return unmarshaller;
    }
}

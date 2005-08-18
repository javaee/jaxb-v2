package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import javax.xml.namespace.QName;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.StructureLoaderBuilder;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Loads children of an element.
 *
 * <p>
 * This loader works with a single {@link JaxBeanInfo} and handles
 * attributes, child elements, or child text.
 *
 * <p>
 * TODO: create an object
 *
 * @author Kohsuke Kawaguchi
 */
public final class StructureLoader extends Loader {
    /**
     * This map statically stores information of the
     * unmarshaller loader and can be used while unmarshalling
     * Since creating new QNames is expensive use this optimized
     * version of the map
     */
    private final QNameMap<ChildLoader> childUnmarshallers;

    /**
     * Loader that processes elements that didn't match anf of the {@link #childUnmarshallers}.
     * Can be null.
     */
    private final ChildLoader catchAll;

    /**
     * If we have a loader for processing text. Otherwise null.
     */
    private final ChildLoader textHandler;

    /**
     * Unmarshallers for attribute values.
     * May be null if no attribute is expected and {@link #attCatchAll}==null.
     */
    private final QNameMap<TransducedAccessor> attUnmarshallers;

    /**
     * This will receive all the attributes
     * that were not processed. Never be null.
     */
    private final Accessor<Object,Map<QName,String>> attCatchAll;

    private final JaxBeanInfo beanInfo;

    /**
     * The number of scopes this dispatcher needs to keep active.
     */
    private final int frameSize;

    // TODO: revisit the parameters we take
    public StructureLoader( JAXBContextImpl context, JaxBeanInfo beanInfo,
                    List<? extends StructureLoaderBuilder> properties,
                    List<AttributeProperty> attributes,
                    Accessor<?,Map<QName,String>> attWildcard) {
        super(true);

        this.beanInfo = beanInfo;
        this.childUnmarshallers = new QNameMap<ChildLoader>();

        UnmarshallerChain chain = new UnmarshallerChain(context);
        for( StructureLoaderBuilder p : properties ) {
            p.buildChildElementUnmarshallers(chain,childUnmarshallers);
        }

        this.frameSize = chain.getScopeSize();

        textHandler = childUnmarshallers.get(StructureLoaderBuilder.TEXT_HANDLER);

        catchAll = childUnmarshallers.get(StructureLoaderBuilder.CATCH_ALL);

        if(!attributes.isEmpty() || attWildcard!=null) {
            attCatchAll = (Accessor<Object,Map<QName,String>>) attWildcard;
            this.attUnmarshallers = new QNameMap<TransducedAccessor>();
            for( AttributeProperty p : attributes )
                attUnmarshallers.put(p.attName.toQName(),p.xacc);
        } else {
            attUnmarshallers = null;
            attCatchAll = null;
        }
    }

    @Override
    public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        UnmarshallingContext context = state.getContext();

        // create the object to unmarshal
        Object child;
        assert !beanInfo.isImmutable();

        // let's see if we can reuse the existing peer object
        child = context.getInnerPeer();

        if(child!=null && beanInfo.jaxbType!=child.getClass())
            child = null;   // unexpected type.

        if(child!=null)
            beanInfo.reset(child,context);

        if(child==null)
            child = context.createInstance(beanInfo);

        context.recordInnerPeer(child);

        state.target = child;

        fireBeforeUnmarshal(beanInfo, child, state);


        context.startScope(frameSize);

        if(attUnmarshallers!=null) {
            Attributes atts = ea.atts;
            for (int i = 0; i < atts.getLength(); i ++){
                String auri = atts.getURI(i);
                String alocal = atts.getLocalName(i);
                String avalue = atts.getValue(i);
                TransducedAccessor xacc = attUnmarshallers.get(auri,alocal);

                try {
                    if(xacc!=null) {
                        xacc.parse(child,avalue);
                    } else
                    if(attCatchAll!=null) {
                        String qname = atts.getQName(i);
                        if(atts.getURI(i).equals(WellKnownNamespace.XML_SCHEMA_INSTANCE))
                            continue;   // xsi:* attributes are meant to be processed by us, not by user apps.
                        Object o = state.target;
                        Map<QName,String> map = attCatchAll.get(o);
                        if(map==null) {
                            // TODO: use  ClassFactory.inferImplClass(sig,knownImplClasses)

                            // if null, create a new map.
                            if(attCatchAll.valueType.isAssignableFrom(HashMap.class))
                                map = new HashMap<QName,String>();
                            else {
                                // we don't know how to create a map for this.
                                // report an error and back out
                                context.handleError(Messages.UNABLE_TO_CREATE_MAP.format(attCatchAll.valueType));
                                return;
                            }
                            attCatchAll.set(o,map);
                        }

                        String prefix;
                        int idx = qname.indexOf(':');
                        if(idx<0)   prefix="";
                        else        prefix=qname.substring(0,idx);

                        map.put(new QName(auri,alocal,prefix),avalue);
                    }
                } catch (AccessorException e) {
                   handleGenericException(e,true);
                }
            }
        }
    }

    @Override
    public void childElement(UnmarshallingContext.State state, TagName arg) throws SAXException {
        ChildLoader child = childUnmarshallers.get(arg.uri,arg.local);
        if(child==null) {
            child = catchAll;
            if(child==null) {
                super.childElement(state,arg);
                return;
            }
        }

        state.loader = child.loader;
        state.receiver = child.receiver;
    }

    @Override
    public Collection<QName> getExpectedChildElements() {
        return childUnmarshallers.keySet();
    }

    @Override
    public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
        if(textHandler!=null)
            textHandler.loader.text(state,text);
    }

    public void leaveElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        state.getContext().endScope(frameSize);
        fireAfterUnmarshal(beanInfo, state.target, state.prev);
    }
}

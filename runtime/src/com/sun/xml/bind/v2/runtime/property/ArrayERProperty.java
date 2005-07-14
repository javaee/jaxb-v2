package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * Commonality between {@link ArrayElementProperty} and {@link ArrayReferenceNodeProperty}.
 *
 * Mostly handles the unmarshalling of the wrapper element.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ArrayERProperty<BeanT,ListT,ItemT> extends ArrayProperty<BeanT,ListT,ItemT> {

    /**
     * Wrapper tag name if any, or null.
     */
    protected final Name wrapperTagName;

    /**
     * True if the wrapper tag name is nillable.
     * Always false if {@link #wrapperTagName}==null.
     */
    protected final boolean isWrapperNillable;

    protected ArrayERProperty(JAXBContextImpl grammar, RuntimePropertyInfo prop, QName tagName, boolean isWrapperNillable) {
        super(grammar,prop);
        if(tagName==null)
            this.wrapperTagName = null;
        else
            this.wrapperTagName = grammar.nameBuilder.createElementName(tagName);
        this.isWrapperNillable = isWrapperNillable;
    }

    private Unmarshaller.Handler createUnmarshallerHandler(JAXBContextImpl grammar, Unmarshaller.Handler tail) {
        final Unmarshaller.Handler end = tail;

        // handle </items>
        if(wrapperTagName==null) {
            final Unmarshaller.Handler next = tail;
            tail = new Unmarshaller.EpsilonHandler() {
                protected void handle(UnmarshallingContext context) throws SAXException {
                    context.endScope(1);
                    context.setCurrentHandler(next);
                }
            };
        } else {
            tail = new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR,tail) {
                public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
                    context.endScope(1);
                    super.leaveElement(context, arg);
                }
            };
        }

        // build the body
        tail = new ElementDispatcher( grammar,
                Collections.singletonList(new ChildElementUnmarshallerBuilder() {
                    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
                        createBodyUnmarshaller(chain,handlers);
                    }
                }),
                tail);

        // handle <items>
        if(wrapperTagName==null) {
            final Unmarshaller.Handler next = tail;
            tail = new Unmarshaller.EpsilonHandler() {
                protected void handle(UnmarshallingContext context) throws SAXException {
                    context.startScope(1);
                    context.setCurrentHandler(next);
                }
            };
        } else {
            tail = new Unmarshaller.EnterElementHandler(wrapperTagName,true,null,end,tail) {
                protected void act(UnmarshallingContext context) throws SAXException {
                    context.startScope(1);
                    super.act(context);
                }
            };
        }

        return tail;
    }

    public final void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ListT list = acc.get(o);

        if(list!=null) {
            if(wrapperTagName!=null) {
                w.startElement(wrapperTagName,null);
                w.endNamespaceDecls(list);
                w.endAttributes();
            }

            serializeListBody(o,w,list);

            if(wrapperTagName!=null)
                w.endElement();
        } else {
            // list is null
            if(isWrapperNillable) {
                w.startElement(wrapperTagName,null);
                w.writeXsiNilTrue();
                w.endElement();
            } // otherwise don't print the wrapper tag name
        }
    }

    /**
     * Serializses the items of the list.
     * This method is invoked after the necessary wrapper tag is produced (if necessary.)
     *
     * @param list
     *      always non-null.
     */
    protected abstract void serializeListBody(BeanT o, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException, AccessorException;

    /**
     * Creates the unmarshaller to unmarshal the body.
     */
    protected abstract void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers);


    public final void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        if(wrapperTagName!=null) {
            chain.tail = createUnmarshallerHandler(chain.context, chain.tail);
            handlers.put(wrapperTagName,chain.tail);
        } else {
            createBodyUnmarshaller(chain,handlers);
        }
    }
}

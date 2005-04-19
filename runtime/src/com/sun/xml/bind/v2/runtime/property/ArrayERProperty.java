package com.sun.xml.bind.v2.runtime.property;

import java.util.Collections;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
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
    protected final Name tagName;

    protected ArrayERProperty(JAXBContextImpl grammar, RuntimePropertyInfo prop, QName tagName) {
        super(grammar,prop);
        if(tagName==null)
            this.tagName = null;
        else
            this.tagName = grammar.nameBuilder.createElementName(tagName);
    }

    public final Unmarshaller.Handler createUnmarshallerHandler(JAXBContextImpl grammar, Unmarshaller.Handler tail) {
        final Unmarshaller.Handler end = tail;

        // handle </items>
        if(tagName==null) {
            final Unmarshaller.Handler next = tail;
            tail = new Unmarshaller.EpsilonHandler() {
                protected void handle(UnmarshallingContext context) throws SAXException {
                    context.endScope(1);
                    context.setCurrentHandler(next);
                }
            };
        } else {
            tail = new Unmarshaller.ForkHandler(Unmarshaller.ERROR,tail) {
                public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
                    context.popAttributes();
                    context.endScope(1);
                    context.setCurrentHandler(next);
                }

                protected Unmarshaller.Handler forwardTo(Unmarshaller.EventType event) {
                    if(event==Unmarshaller.EventType.LEAVE_ELEMENT)
                        return this;
                    else
                        return super.forwardTo(event);
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
        if(tagName==null) {
            final Unmarshaller.Handler next = tail;
            tail = new Unmarshaller.EpsilonHandler() {
                protected void handle(UnmarshallingContext context) throws SAXException {
                    context.startScope(1);
                    context.setCurrentHandler(next);
                }
            };
        } else {
            tail = new Unmarshaller.ForkHandler(end,tail) {
                public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
                    if(arg.matches(tagName)) {
                        context.pushAttributes(arg.atts,false,null);
                        context.startScope(1);
                        context.setCurrentHandler(next);
                        return;
                    }
                    super.enterElement(context,arg);
                }

                protected Unmarshaller.Handler forwardTo(Unmarshaller.EventType event) {
                    if(event==Unmarshaller.EventType.ENTER_ELEMENT)
                        return this;
                    else
                        return super.forwardTo(event);
                }
            };
        }

        return tail;
    }

    /**
     * Creates the unmarshaller to unmarshal the body.
     */
    protected abstract void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers);


    public final void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers) {
        if(tagName!=null) {
            chain.tail = createUnmarshallerHandler(chain.context, chain.tail);
            handlers.put(tagName,chain.tail);
        } else {
            createBodyUnmarshaller(chain,handlers);
        }
    }
}

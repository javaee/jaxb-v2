/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: ElementDispatcher.java,v 1.1 2005-04-15 20:04:31 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.property;

import java.util.List;

import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * The ElementDispatcher   is used to implement
 * the unmarshalling by name functionality.
 * 
 * <p>
 * The ElementDispatcher unmarshals a particular element
 * and gets the control back to itself. It also may need to
 * maintain multiple {@link Unmarshaller.Handler}s active at
 * the same time in case of collection items.
 *
 *
 * @author
 *     Bhakti Mehta (bhakti.mehta@sun.com)
 * @since 2.0
 */
public class ElementDispatcher extends Unmarshaller.DelegatingHandler {

    /**
     * This map statically stores information of the
     * unmarshaller handler and can be used while unmarshalling
     * Since creating new QNames is expensive use this optimized
     * version of the map
     */
    private final QNameMap<Unmarshaller.Handler> childUnmarshallers;

    /**
     * Handler that processes elements that didn't match anf of the {@link #childUnmarshallers}.
     * Can be null.
     */
    private final Unmarshaller.Handler catchAll;

    /**
     * If we have a handler for processing text. Otherwise null.
     */
    private final Unmarshaller.RawTextHandler textHandler;

    /**
     * The number of scopes this dispatcher needs to keep active.
     */
    private final int frameSize;

    public ElementDispatcher( JAXBContextImpl context, List<? extends ChildElementUnmarshallerBuilder> properties, Unmarshaller.Handler fallthrough) {
        super(fallthrough);

        this.childUnmarshallers = new QNameMap<Unmarshaller.Handler>();
        UnmarshallerChain chain = new UnmarshallerChain(context);

        for( ChildElementUnmarshallerBuilder p : properties ) {
            chain.tail = Unmarshaller.REVERT_TO_PARENT;

            p.buildChildElementUnmarshallers(chain,childUnmarshallers);
        }

        this.frameSize = chain.getScopeSize();

        textHandler = (Unmarshaller.RawTextHandler)
            childUnmarshallers.get(ChildElementUnmarshallerBuilder.TEXT_HANDLER);

        catchAll = childUnmarshallers.get(ChildElementUnmarshallerBuilder.CATCH_ALL);
    }

    /**
     * When we are processing element content model, all the texts are ignored.
     */
    public void text(UnmarshallingContext context, CharSequence s) throws SAXException {
        if(textHandler==null)
            return; // just ignore

        if(context.getCurrentHandler()!=this) {
            // activate ourselves
            context.setCurrentHandler(this);
        }
        
        // otherwise forward
        textHandler.processText(context,s);
    }


    protected Unmarshaller.Handler forwardTo(Unmarshaller.EventType event) {
        if(event==Unmarshaller.EventType.ENTER_ELEMENT)
            return this;
        if(event==Unmarshaller.EventType.TEXT)
            return this;
        return super.forwardTo(event);
    }

    public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        //This uses the optimized version of QNameMap
        Unmarshaller.Handler handler = childUnmarshallers.get(arg.uri,arg.local);
        if(handler==null) {
            handler = catchAll;
            if(handler==null) {
                unexpectedEnterElement(context,arg);
                return;
            }
        }

        if(context.getCurrentHandler()!=this) {
            // activate ourselves
            context.setCurrentHandler(this);
        }
        context.pushContentHandler(handler, context.getTarget(), false);
        context.getCurrentHandler().enterElement(context,arg);
    }

    public void activate(UnmarshallingContext context) throws SAXException {
        context.startScope(frameSize);
    }

    public void deactivated(UnmarshallingContext context) throws SAXException {
        context.endScope(frameSize);
        super.deactivated(context);
    }

    public String toString() {
        return "ElementDispatcher "+childUnmarshallers.toString();
    }
}
/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: WildcardUnmarshaller.java,v 1.3 2005-05-23 15:15:32 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Result;
import javax.xml.transform.sax.TransformerHandler;

import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.property.Unmarshaller;

import org.xml.sax.SAXException;

/**
 * Feed incoming events to {@link DomHandler} and builds a DOM tree.
 * 
 * <p>
 * Note that the SAXException returned by the ContentHandler is
 * unreported. So we have to catch them and report it, then rethrow
 * it if necessary.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class WildcardUnmarshaller<ResultT extends Result> extends Unmarshaller.Handler {

    /**
     * Used to capture the state.
     *
     * This instance is created for each unmarshalling episode.
     */
    private final class State {
        /** This handler will receive SAX events. */
        // TODO: cache identity transformer in UnmarshallingContest for improved performance.
        private final TransformerHandler handler = JAXBContextImpl.createTransformerHandler();

        /** {@link #handler} will produce this result. */
        private final ResultT result;

        // nest level of elements.
        int depth = 0;

        public State( UnmarshallingContext context ) throws SAXException {
            result = dom.createUnmarshaller(context);

            handler.setResult(result);

            // emulate the start of documents
            try {
                handler.setDocumentLocator(context.getLocator());
                handler.startDocument();
                declarePrefixes( context, context.getAllDeclaredPrefixes() );
            } catch( SAXException e ) {
                error(context,e);
            }
        }

        public Object getElement() {
            return dom.getElement(result);
        }

        private void declarePrefixes( UnmarshallingContext context, String[] prefixes ) throws SAXException {
            for( int i=prefixes.length-1; i>=0; i-- )
                handler.startPrefixMapping(
                    prefixes[i],
                    context.getNamespaceURI(prefixes[i]) );
        }

        private void undeclarePrefixes( String[] prefixes ) throws SAXException {
            for( int i=prefixes.length-1; i>=0; i-- )
                handler.endPrefixMapping( prefixes[i] );
        }
    };

    private final Unmarshaller.Handler next;

    private final DomHandler<?,ResultT> dom;

    private final WildcardMode mode;

    public WildcardUnmarshaller(DomHandler<?, ResultT> dom, WildcardMode mode, Unmarshaller.Handler next) {
        this.dom = dom;
        this.mode = mode;
        this.next = next;
    }

    @Override
    public void activate(UnmarshallingContext context) throws SAXException {
        context.startState();
        context.setState(new State(context));
    }

    public void deactivated(UnmarshallingContext context) throws SAXException {
        context.endState();
    }

    private static State getState(UnmarshallingContext context) {
        return context.getState();
    }

    public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        if(mode.allowTypedObject) {
            UnmarshallingEventHandler unm = context.getJAXBContext().pushUnmarshaller(arg.uri,arg.local,context);
            if(unm!=null) {
                // forward
                context.getCurrentHandler().enterElement(context,arg);
                return;
            }
        }
        if(mode.allowDom) {
            State state = getState(context);
            state.depth++;
            context.pushAttributes(arg.atts,true,null);
            try {
                state.declarePrefixes(context,context.getNewlyDeclaredPrefixes());
                state.handler.startElement(arg.uri, arg.local, arg.qname, arg.atts);
            } catch( SAXException e ) {
                error(context,e);
            }
        } else {
            // simply discard
            context.pushContentHandler(new Discarder(),null,false);
            context.getCurrentHandler().enterElement(context,arg);
        }
    }

    public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        State state = getState(context);

        try {
            state.handler.endElement(arg.uri, arg.local, arg.qname);
            state.undeclarePrefixes(context.getNewlyDeclaredPrefixes());
        } catch( SAXException e ) {
            error(context,e);
        }
        context.popAttributes();
        
        if((--state.depth)==0) {
            // emulate the end of the document
            try {
                state.undeclarePrefixes(context.getAllDeclaredPrefixes());
                state.handler.endDocument();
            } catch( SAXException e ) {
                error(context,e);
            }

            onDone(context,state.getElement());
        }
    }

    protected void onDone(UnmarshallingContext context, Object element) throws SAXException {
        context.setCurrentHandler(next);
    }


    public void text(UnmarshallingContext context,CharSequence s) throws SAXException {
        try {
            State state = getState(context);
            state.handler.characters(s.toString().toCharArray(),0,s.length());
        } catch( SAXException e ) {
            error(context,e);
        }
    }
    
    private static void error( UnmarshallingContext context, SAXException e ) throws SAXException {
        context.handleError(e);
        throw e;
    }

    protected Unmarshaller.Handler forwardTo(Unmarshaller.EventType event) {
        return this;
    }

    public void leaveChild(UnmarshallingContext context, Object child) throws SAXException {
        // called when a child element is unmarshalled as a typed object
        // or discarded
        if(child!=null)
            onDone(context,child);
        else
            context.setCurrentHandler(next);
    }
}

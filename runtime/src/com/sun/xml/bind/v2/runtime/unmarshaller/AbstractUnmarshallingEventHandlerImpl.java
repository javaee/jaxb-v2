/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: AbstractUnmarshallingEventHandlerImpl.java,v 1.2 2005-04-20 19:03:13 kohsuke Exp $
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.Element;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;

import com.sun.xml.bind.unmarshaller.Messages;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;

import org.xml.sax.SAXException;

/**
 * Convenient default implementation of
 * {@link UnmarshallingEventHandler}
 * to minimize code generation.
 * 
 * <p>
 * For historical reasons, sometimes this type is used where
 * {@link UnmarshallingEventHandler} should be used.
 * 
 * Once an exception is in the form of UnmarshalException, we consider
 * it to be already reported to the client app.
 */
public abstract class AbstractUnmarshallingEventHandlerImpl implements UnmarshallingEventHandler
{
    protected AbstractUnmarshallingEventHandlerImpl() {
    }

//
//
// methods that will be provided by the generated code.
//
//    
    // internal events
    public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        unexpectedEnterElement(context,arg);
    }
    public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        unexpectedLeaveElement(arg);
    }
    public void text(UnmarshallingContext context, CharSequence text) throws SAXException {
        unexpectedText(text);
    }
    public void leaveChild(UnmarshallingContext context, Object child) throws SAXException {
    }
    public void activate(UnmarshallingContext context) throws SAXException {
    }
    public void deactivated(UnmarshallingContext context) throws SAXException {
    }

//
//
// utility methods
//
//
    /** Called when a RuntimeException is thrown during unmarshalling a text. */
    protected static final void handleUnexpectedTextException(CharSequence text, RuntimeException e) throws SAXException {
        // report this as an error
        reportError(Messages.format(Messages.UNEXPECTED_TEXT,text), e, true );
    }
    
    /**
     * Last resort when something goes terribly wrong within the unmarshaller.
     */
    protected static final void handleGenericException(Exception e) throws SAXException {
        handleGenericException(e,false);
    }

    public static final void handleGenericException(Exception e, boolean canRecover) throws SAXException {
        reportError(e.getMessage(), e, canRecover );
    }


    protected static final void reportError(String msg, boolean canRecover) throws SAXException {
        reportError(msg, null, canRecover );
    }
    public static final void reportError(String msg, Exception nested, boolean canRecover) throws SAXException {
        UnmarshallingContext context = UnmarshallingContext.getInstance();
        context.handleEvent( new ValidationEventImpl(
            canRecover? ValidationEvent.ERROR : ValidationEvent.FATAL_ERROR,
            msg,
            context.getLocator().getLocation(),
            nested ), canRecover );
    }
    protected final void unexpectedEnterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
        // notify the error
        reportError(Messages.format(Messages.UNEXPECTED_ENTER_ELEMENT, arg.uri, arg.local ), true );
        // then recover by ignoring the whole element.
        context.pushContentHandler(new Discarder(),null,false);
        context.getCurrentHandler().enterElement(context,arg);
    }
    protected static final void unexpectedLeaveElement(EventArg arg) throws SAXException {
        reportError(Messages.format(Messages.UNEXPECTED_LEAVE_ELEMENT, arg.uri, arg.local ), false );
    }
    protected static final void unexpectedText(CharSequence str) throws SAXException {
        // make str printable
        str = str.toString().replace('\r',' ').replace('\n',' ').replace('\t',' ').trim();
        
        reportError(Messages.format(Messages.UNEXPECTED_TEXT, str ), true );
    }
    protected final void unexpectedLeaveChild() {
        // I believe this is really a bug of the compiler,
        // since when an object spawns a child object, it must be "prepared"
        // to receive this event.
        throw new AssertionError(
            Messages.format( Messages.UNEXPECTED_LEAVE_CHILD ) );
    }
    /**
     * This method is called by the generated derived class
     * when a datatype parse method throws an exception.
     */
    protected static final void handleParseConversionException(UnmarshallingContext context, Exception e) throws SAXException {
        if( e instanceof RuntimeException )
            throw (RuntimeException)e;  // don't catch the runtime exception. just let it go.
        
        // wrap it into a ParseConversionEvent and report it
        context.handleError(e);
    }
    
//
//    
// spawn a new child object
//
//    
    /**
     * Spawns a new child object of the given {@link JaxBeanInfo} and
     * sets its unmarshaller into the context.
     */
    protected final UnmarshallingEventHandler spawnChild( UnmarshallingContext context, JaxBeanInfo beanInfo, boolean asElement ) throws SAXException {
        
        Object child=null;

        if(!beanInfo.isImmutable()) {
            // let's see if we can reuse the existing peer object
            if(asElement)
                child = context.getOuterPeer();
            else
                child = context.getInnerPeer();

            if(child!=null && beanInfo.jaxbType!=child.getClass())
                child = null;   // unexpected type.

            if(child!=null)
                beanInfo.reset(child,context);

            if(child==null)
                child = context.createInstance(beanInfo);

            if(!asElement)
                // outer peer is recorded inside the getUnmarshaller invocation
                // so that we can correctly mark the asElement element.
                context.recordInnerPeer(child);
        }

        UnmarshallingEventHandler handler = beanInfo.getUnmarshaller(asElement);
        context.pushContentHandler(handler, child, beanInfo.implementsLifecycle());
        if(beanInfo.hasElementOnlyContentModel())
            context.disableTextCollection();
        return handler;
    }
    
    protected final Element spawnWildcard( UnmarshallingContext context, EventArg arg )
            throws SAXException {
        UnmarshallingEventHandler ueh = context.getJAXBContext().pushUnmarshaller(arg.uri,arg.local,context);
        
        if(ueh!=null) {
            ueh.enterElement(context,arg);
            return context.getTarget();
        } else {
            // if no class is available to unmarshal this element, discard
            // the sub-tree by feeding events to discarder.
            context.pushContentHandler( new Discarder(), null, false );
            context.getCurrentHandler().enterElement(context,arg);
            return null;    // return null so that the discarder will be ignored
        }
    }

    protected static final UnmarshallingEventHandler revertToParent(UnmarshallingContext context) throws SAXException {
        context.popContentHandler();
        return context.getCurrentHandler();
    }
}

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.bind.unmarshaller;

import javax.xml.bind.Element;
import javax.xml.bind.ParseConversionEvent;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ParseConversionEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.Attributes;

/**
 * Convenient default implementation of
 * {@link com.sun.xml.bind.unmarshaller.UnmarshallingEventHandler}
 * to minimize code generation.
 * 
 * <p>
 * For historical reasons, sometimes this type is used where
 * {@link com.sun.xml.bind.unmarshaller.UnmarshallingEventHandler}
 * should be used.   
 * 
 * Once an exception is in the form of UnmarshalException, we consider
 * it to be already reported to the client app.
 * 
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public abstract class ContentHandlerEx implements UnmarshallingEventHandler
{
    public ContentHandlerEx(UnmarshallingContext _ctxt,
        String _stateTextTypes ) {
        
        this.context = _ctxt;
        this.stateTextTypes = _stateTextTypes;
    }
    public final UnmarshallingContext context;
    
    /**
     * Returns the content-tree object for which this ContentHandlerEx
     * is working for.
     */
    protected abstract UnmarshallableObject owner();
    
    /**
     * Text type of states encoded into a string.
     * 'L' means a list state.
     */
    private final String stateTextTypes;
    
//
//
// methods that will be provided by the generated code.
//
//    
    // internal events
    public void enterElement(String uri, String local, Attributes atts) throws UnreportedException {
        unexpectedEnterElement(uri,local);
    }
    public void leaveElement(String uri, String local) throws UnreportedException {
        unexpectedLeaveElement(uri,local);
    }
    public void text(String s) throws UnreportedException {
        unexpectedText(s);
    }
    public void enterAttribute(String uri, String local) throws UnreportedException {
        unexpectedEnterAttribute(uri,local);
    }
    public void leaveAttribute(String uri, String local) throws UnreportedException {
        unexpectedLeaveAttribute(uri,local);
    }
    public void leaveChild(int nextState) throws UnreportedException {
        this.state = nextState;
    }
    
    
    /**
     * Checks if the current state is marked as a list state.
     */
    protected final boolean isListState() {
        return stateTextTypes.charAt(state)=='L';
    }
    
    
    /** Current state of this automaton. */
    public int state;
    
    
    
    
//
//
// utility methods
//
//
    /** Called when a RuntimeException is thrown during unmarshalling a text. */
    protected void handleUnexpectedTextException( String text, RuntimeException e ) throws UnreportedException {
        // report this as an error
        throw new UnreportedException(
            Messages.format(Messages.UNEXPECTED_TEXT,text),
            context.getLocator(), e );
    }
    
    
    protected final void dump() {
        System.err.println("state is :"+state);
    }
    protected final void unexpectedEnterElement( String uri, String local ) throws UnreportedException {
        throw new UnreportedException(
            Messages.format(Messages.UNEXPECTED_ENTER_ELEMENT, uri, local ),
            context.getLocator());
    }
    protected final void unexpectedLeaveElement( String uri, String local ) throws UnreportedException {
        throw new UnreportedException(
            Messages.format(Messages.UNEXPECTED_LEAVE_ELEMENT, uri, local ),
            context.getLocator());
    }
    protected final void unexpectedEnterAttribute( String uri, String local ) throws UnreportedException {
        throw new UnreportedException(
            Messages.format(Messages.UNEXPECTED_ENTER_ATTRIBUTE, uri, local ),
            context.getLocator());
    }
    protected final void unexpectedLeaveAttribute( String uri, String local ) throws UnreportedException {
        throw new UnreportedException(
            Messages.format(Messages.UNEXPECTED_LEAVE_ATTRIBUTE, uri, local ),
            context.getLocator());
    }
    protected final void unexpectedText( String str ) throws UnreportedException {
        // make str printable
        str = str.replace('\r',' ').replace('\n',' ').replace('\t',' ').trim();
        
        throw new UnreportedException(
            Messages.format(Messages.UNEXPECTED_TEXT, str ),
            context.getLocator() );
    }
    protected final void unexpectedLeaveChild() throws UnreportedException {
        // I believe this is really a bug of the compiler,
        // since when an object spawns a child object, it must be "prepared"
        // to receive this event.
        dump();
        throw new InternalError( 
            Messages.format( Messages.UNEXPECTED_LEAVE_CHILD ) );
    }
    /**
     * This method is called by the generated derived class
     * when a datatype parse method throws an exception.
     */
    protected void handleParseConversionException(Exception e) {
        if( e instanceof RuntimeException )
            throw (RuntimeException)e;  // don't catch the runtime exception. just let it go.
        
        // wrap it into a ParseConversionEvent and report it
        ParseConversionEvent pce = new ParseConversionEventImpl(
            ValidationEvent.ERROR, e.getMessage(), 
            new ValidationEventLocatorImpl(context.getLocator()), e );
        context.handleEvent(pce);
    }
    
//
//    
// spawn a new child object
//
//    
    // BEWARE: this field is used as the second return value from the
    // spawnChild method. UGLY CODE WARNING!!!
    private UnmarshallableObject child;
    private UnmarshallingEventHandler spawnChild( Class clazz, int memento ) {
        child = context.getTypeRegistry()
            .createInstanceOf(clazz);
        
        UnmarshallingEventHandler handler = child.getUnmarshaller(context);
        context.pushContentHandler(handler,memento);
        return handler;
    }
    
    protected final Object spawnChildFromEnterElement(Class clazz, int memento, String uri, String local, Attributes atts)
            throws UnreportedException {
        spawnChild(clazz,memento).enterElement(uri,local,atts);
        return child;
    }
    
    protected final Object spawnChildFromEnterAttribute(Class clazz, int memento, String uri, String local)
            throws UnreportedException {
        spawnChild(clazz,memento).enterAttribute(uri,local);
        return child;
    }
    
    protected final Object spawnChildFromText(Class clazz, int memento, String value)
            throws UnreportedException {
        spawnChild(clazz,memento).text(value);
        return child;
    }

    // these methods can be used if a child object can be nullable
    protected final Object spawnChildFromLeaveElement(Class clazz, int memento, String uri, String local)
            throws UnreportedException {
        spawnChild(clazz,memento).leaveElement(uri,local);
        return child;
    }

    protected final Object spawnChildFromLeaveAttribute(Class clazz, int memento, String uri, String local)
            throws UnreportedException {
        spawnChild(clazz,memento).leaveAttribute(uri,local);
        return child;
    }
    
    protected final Element spawnWildcard( int memento, String uri, String local, Attributes atts )
            throws UnreportedException {
        Class clazz = context.getTypeRegistry().getRootElement(uri,local);
        if(clazz!=null) {
            return (Element)spawnChildFromEnterElement(clazz,memento,uri,local,atts);
        } else {
            // if no class is available to unmarshal this element, discard
            // the sub-tree by feeding events to discarder.
            context.pushContentHandler( new Discarder(context), memento );
            context.getCurrentEventHandler().enterElement(uri,local,atts);
            return null;    // return null so that the discarder will be ignored
        }
    }
//
//    
// spawn a super class unmarshaller
//
//    

    
    protected final void spawnSuperClassFromEnterElement(
        ContentHandlerEx unm, int memento, String uri, String local,Attributes atts)
            throws UnreportedException {
        
        context.pushContentHandler(unm,memento);
        unm.enterElement(uri,local,atts);
    }
    
    protected final void spawnSuperClassFromEnterAttribute(
        ContentHandlerEx unm, int memento, String uri, String local)
            throws UnreportedException {
        
        context.pushContentHandler(unm,memento);
        unm.enterAttribute(uri,local);
    }
    
    protected final void spawnSuperClassFromFromText(
        ContentHandlerEx unm, int memento, String value)
            throws UnreportedException {
        
        context.pushContentHandler(unm,memento);
        unm.text(value);
    }
    
    protected final void spawnSuperClassFromLeaveElement(
        ContentHandlerEx unm, int memento, String uri, String local)
            throws UnreportedException {
        
        context.pushContentHandler(unm,memento);
        unm.leaveElement(uri,local);
    }
    
    protected final void spawnSuperClassFromLeaveAttribute(
        ContentHandlerEx unm, int memento, String uri, String local)
            throws UnreportedException {
        
        context.pushContentHandler(unm,memento);
        unm.leaveAttribute(uri,local);
    }
    
    protected final void spawnSuperClassFromText(
        ContentHandlerEx unm, int memento, String text )
            throws UnreportedException {
        
        context.pushContentHandler(unm,memento);
        unm.text(text);
    }
    
    
//
//    
// revert to parent
//
//    
    protected final void revertToParentFromEnterElement(String uri,String local,Attributes atts)
            throws UnreportedException {
        context.popContentHandler();
        context.getCurrentEventHandler().enterElement(uri,local,atts);
    }
    protected final void revertToParentFromLeaveElement(String uri,String local)
            throws UnreportedException {
        context.popContentHandler();
        context.getCurrentEventHandler().leaveElement(uri,local);
    }
    protected final void revertToParentFromEnterAttribute(String uri,String local)
            throws UnreportedException {
        context.popContentHandler();
        context.getCurrentEventHandler().enterAttribute(uri,local);
    }
    protected final void revertToParentFromLeaveAttribute(String uri,String local)
            throws UnreportedException {
        context.popContentHandler();
        context.getCurrentEventHandler().leaveAttribute(uri,local);
    }
    protected final void revertToParentFromText(String value)
            throws UnreportedException {
        context.popContentHandler();
        context.getCurrentEventHandler().text(value);
    }
}

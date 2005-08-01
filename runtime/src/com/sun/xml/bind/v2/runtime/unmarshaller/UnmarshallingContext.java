/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.v2.AssociationMap;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.property.Unmarshaller;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Center of the unmarshalling.
 *
 * <p>
 * This object is responsible for coordinating {@link UnmarshallingEventHandler}s to
 * perform the whole unmarshalling.
 *
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class UnmarshallingContext extends Coordinator
    implements NamespaceContext, ValidationEventHandler, ErrorHandler, XmlVisitor
{
    /**
     * This flag is set to true at the startDocument event
     * and false at the endDocument event.
     * 
     * Until the first document is unmarshalled, we don't
     * want to return an object. So this variable is initialized
     * to true.
     */
    private boolean isUnmarshalInProgress = true;
    
    /**
     * If the unmarshaller is doing associative unmarshalling,
     * this field is initialized to non-null.
     */
    private final AssociationMap assoc;
    
    /**
     * Indicates whether we are doing in-place unmarshalling
     * or not.
     * 
     * <p>
     * This flag is unused when {@link #assoc}==null.
     * If it's non-null, then <tt>true</tt> indicates
     * that we are doing in-place associative unmarshalling.
     * If <tt>false</tt>, then we are doing associative unmarshalling
     * without object reuse.
     */
    private boolean isInplaceMode;
    
    private Object currentElement;
    
    /**
     * This object is consulted to get the element object for
     * the current element event.
     * 
     * This is used when we are building an association map.
     */
    private InfosetScanner scanner;

    /**
     * If non-null, this unmarshaller will unmarshal {@code JAXBElement<EXPECTEDTYPE>}
     * regardless of the tag name, as opposed to deciding the root object by using
     * the tag name.
     *
     * The property has a package-level access, because we cannot copy this value
     * to {@link UnmarshallingContext} when it is created. The property
     * on {@link javax.xml.bind.Unmarshaller} could be changed after the handler is created.
     */
    /*package*/ JaxBeanInfo expectedType;

    /**
     * Stub to the user-specified factory method.
     */
    private static class Factory {
        private final Object factorInstance;
        private final Method method;

        public Factory(Object factorInstance, Method method) {
            this.factorInstance = factorInstance;
            this.method = method;
        }

        public Object createInstance() throws SAXException {
            try {
                return method.invoke(factorInstance);
            } catch (IllegalAccessException e) {
                getInstance().handleError(e,false);
            } catch (InvocationTargetException e) {
                getInstance().handleError(e,false);
            }
            return null; // can never be executed
        }
    }

    /**
     * User-specified factory methods.
     */
    private final Map<Class,Factory> factories = new HashMap<Class, Factory>();

    public void setFactories(Object factoryInstances) {
        factories.clear();
        if(factoryInstances==null) {
            return;
        }
        if(factoryInstances instanceof Object[]) {
            for( Object factory : (Object[])factoryInstances ) {
                // look for all the public methods inlcuding derived ones
                addFactory(factory);
            }
        } else {
            addFactory(factoryInstances);
        }
    }

    private void addFactory(Object factory) {
        for( Method m : factory.getClass().getMethods() ) {
            // look for methods whose signature is T createXXX()
            if(!m.getName().startsWith("create"))
                continue;
            if(m.getParameterTypes().length>0)
                continue;

            Class type = m.getReturnType();

            factories.put(type,new Factory(factory,m));
        }
    }



//    /**
//     * Debug flag. True to enable tracing.
//     */
//    private static boolean TRACE;
//
//    static {
//        try {
//            TRACE = System.getProperty("com.sun.xml.bind.trace")!=null;
//        } catch(Throwable t) {
//            ; // ignore
//        }
//    }

    /**
     * {@link EventArg} implementation that can be chained.
     */
    private static class EventArgImpl extends EventArg {
        EventArgImpl next;
    }

    /**
     * {@link EventArg}s that we can reuse.
     *
     * <p>
     * All free {@link EventArg}s are kept in a list so that
     * we can reuse instances.
     */
    private EventArgImpl eventArg = null;

    /**
     * Allocates a new {@link EventArg} filled with the given value.
     */
    private EventArgImpl alloc( String uri, String local, String qname, Attributes atts ) {
        EventArgImpl r = eventArg;
        if(r==null) {
            r = new EventArgImpl();
        } else {
            eventArg = r.next;
        }

        r.uri = uri;
        r.local = local;
        r.qname = qname;
        r.atts = atts;

        return r;
    }

    /**
     * Returns the finished EventArgImpl object.
     */
    public final void free( EventArgImpl a ) {
        a.next = eventArg;
        eventArg = a;

        a.atts = null;  // avoid keeping references too long
    }


    /**
     * Creates a new unmarshaller for associative unmarshalling.
     * 
     * @param assoc
     *      Must be both non-null when the unmarshaller does the
     *      in-place unmarshalling. Otherwise must be both null.
     */
    public UnmarshallingContext( UnmarshallerImpl _parent, AssociationMap assoc) {
        this.parent = _parent;
        this.assoc = assoc;
    }


    public void reset(InfosetScanner scanner,boolean isInplaceMode, JaxBeanInfo expectedType) {
        this.scanner = scanner;
        this.isInplaceMode = isInplaceMode;
        this.expectedType = expectedType;
    }

    /**
     * Creates a new instance of the specified class.
     * In the unmarshaller, we need to check the user-specified factory class.
     */
    public Object createInstance( Class clazz ) throws SAXException {
        if(!factories.isEmpty()) {
            Factory factory = factories.get(clazz);
            if(factory!=null)
                return factory.createInstance();
        }
        return ClassFactory.create(clazz);
    }

    /**
     * Creates a new instance of the specified class.
     * In the unmarshaller, we need to check the user-specified factory class.
     */
    public Object createInstance( JaxBeanInfo beanInfo ) throws SAXException {
        if(!factories.isEmpty()) {
            Factory factory = factories.get(beanInfo.jaxbType);
            if(factory!=null)
                return factory.createInstance();
        }
        try {
            return beanInfo.createInstance(this);
        } catch (IllegalAccessException e) {
            AbstractUnmarshallingEventHandlerImpl.reportError("Unable to create an instance of "+beanInfo.jaxbType.getName(),e,false);
        } catch (InvocationTargetException e) {
            AbstractUnmarshallingEventHandlerImpl.reportError("Unable to create an instance of "+beanInfo.jaxbType.getName(),e,false);
        } catch (InstantiationException e) {
            AbstractUnmarshallingEventHandlerImpl.reportError("Unable to create an instance of "+beanInfo.jaxbType.getName(),e,false);
        }
        return null;    // can never be here
    }

    /**
     * Obtains a reference to the current grammar info.
     */
    public JAXBContextImpl getJAXBContext() {
        return parent.context;
    }

    /**
     * Returns true if we should be collecting characters in the current element.
     */
    public boolean expectText() {
        return collectText[stackTop];
    }

    public UnmarshallingContext getContext() {
        return this;
    }


    public void startDocument(LocatorEx locator) {
        this.locator = locator;
        // reset the object
        result = null;
        handlerLen=0;
        patchers=null;
        patchersLen=0;
        aborted = false;
        isUnmarshalInProgress = true;
        
        stackTop=0;
        scopeTop=0;
        stateTop=0;
        elementDepth=1;
        nsLen=0;

        startPrefixMapping("",""); // by default, the default ns is bound to "".

        setThreadAffinity();
    }
    
    public void endDocument() throws SAXException {
        runPatchers();
        isUnmarshalInProgress = false;
        currentElement = null;
        locator = null;

        // at the successful completion, scope must be all closed
        assert scopeTop==0;
        assert handlerLen==0;
        assert stateTop==0;

        resetThreadAffinity();
    }

    public void startElement( String uri, String local, String qname, Attributes atts )
            throws SAXException {
        pushCoordinator();
        try {
            _startElement(uri,local,qname,atts);
        } finally {
            popCoordinator();
        }
    }

    private void _startElement( String uri, String local, String qname, Attributes atts )
            throws SAXException {

        // remember the current element if we are interested in it.
        // because the inner peer might not be found while we consume
        // the enter element token, we need to keep this information
        // longer than this callback. That's why we assign it to a field.
        if( assoc!=null )
            currentElement = scanner.getCurrentElement();
        
        if(result==null) {
            // this is the root element.
            // create a root object and start unmarshalling

            if(expectedType==null) {
                // normal unmarshalling
                UnmarshallingEventHandler unmarshaller =
                    parent.context.pushUnmarshaller(uri,local,this);
                if(unmarshaller==null) {
                    // the registry doesn't know about this element.
                    //
                    // the no.1 cause of this problem is that your application is configuring
                    // an XML parser by your self and you forgot to call
                    // the SAXParserFactory.setNamespaceAware(true). When this happens, you see
                    // the namespace URI is reported as empty whereas you expect something else.
                    throw new SAXParseException(
                        Messages.UNEXPECTED_ROOT_ELEMENT.format(
                            uri, local, computeExpectedRootElements() ),
                        getLocator() );
                }
                result = getTarget();
            } else {
                // unmarshals the specified type
                QName qn = new QName(uri,local);
                result = new JAXBElement(qn,expectedType.jaxbType,null,null);

                UnmarshallingEventHandler handler =
                    new Unmarshaller.SpawnChildSetHandler(expectedType,
                        new Unmarshaller.LeaveElementHandler(Unmarshaller.ERROR,Unmarshaller.REVERT_TO_PARENT),
                        false, Accessor.JAXB_ELEMENT_VALUE );

                pushAttributes(atts,true,null);

                this.pushContentHandler(handler, result, null);
                return; // don't forward the enterElement event, as it has already been consumed.
            }
        }

        EventArgImpl ea = alloc(uri,local,qname,atts);
        getCurrentHandler().enterElement(this,ea);
        free(ea);
    }

    public final void endElement( String uri, String local, String qname ) throws SAXException {
        pushCoordinator();
        try {
            EventArgImpl ea = alloc(uri,local,qname,null);
            getCurrentHandler().leaveElement(this,ea);
            free(ea);
        } finally {
            popCoordinator();
        }
    }





    /** Root object that is being unmarshalled. */
    private Object result;

    /**
     * Gets the result of the unmarshalling
     */
    public Object getResult() throws UnmarshalException {
        if(isUnmarshalInProgress)
            throw new IllegalStateException();
        
        if(!aborted)       return result;
        
        // there was an error.
        throw new UnmarshalException((String)null);
    }

    
    
//
//
// handler stack maintainance
//
//
    private UnmarshallingEventHandler[] handlers = new UnmarshallingEventHandler[16];
    private Object[] targets = new Object[16];
    private JaxBeanInfo[] beanInfos = new JaxBeanInfo[16];
    private int handlerLen=0;
    
    /**
     * Pushes the current content handler into the stack
     * and registers the newly specified content handler so
     * that it can receive SAX events.
     *
     * This method also fires beforeUnmarshal lifecycle events if necessary.
     *
     * @param target
     *      The target object that the given {@link UnmarshallingEventHandler}
     *      is going to unmarshal.
     * @param beanInfo
     *      Reference to the associated JaxBeanInfo used to determine if the Unmarshaller
     *      lifecycle methods should be triggered.  If so, the target object's
     *      beforeUnmarshalling(javax.xml.bind.Unmarshaller, Object) method
     *      is called (Spec Sec 4.4.1).  If this parameter is null, then the lifecycle
     *      processing will be skipped altogether.
     *
     * @see #getTarget()
     */
    public void pushContentHandler(UnmarshallingEventHandler handler, Object target, JaxBeanInfo beanInfo ) throws SAXException {

        // unmarshaller lifecycle handling
        if((beanInfo != null) && (beanInfo.lookForLifecycleMethods())) {
            // invoke external listener before bean embedded listener
            javax.xml.bind.Unmarshaller.Listener externalListener =
                    parent.getListener();
            if(externalListener != null) {
                externalListener.beforeUnmarshal(
                        target,
                        (handlerLen==0) ? null : targets[handlerLen-1] /* parent object of 'target' */
                );
            }

            // then invoke bean embedded listener
            if(beanInfo.hasBeforeUnmarshalMethod()) {
                Method m = beanInfo.getLifecycleMethods().getBeforeUnmarshal();
                assert m != null;

                Object p;
                if(handlerLen==0) {
                    p = null;
                } else {
                    p = targets[handlerLen-1];
                }

                Object[] params = new Object[] { parent, p };
                try {
                    m.invoke(target, params);
                } catch (IllegalAccessException e) {
                    throw new SAXException(e);
                } catch (InvocationTargetException e) {
                    throw new SAXException(e);
                }
            }
        }

        if(handlerLen==handlers.length) {
            // expand buffer
            UnmarshallingEventHandler[] h = new UnmarshallingEventHandler[handlerLen*2];
            Object[] t = new Object[handlerLen*2];
            JaxBeanInfo[] jbi = new JaxBeanInfo[handlerLen*2];
            System.arraycopy(handlers,0,h,0,handlerLen);
            System.arraycopy(targets, 0,t,0,handlerLen);
            System.arraycopy(beanInfos,0,jbi,0,handlerLen);
            handlers = h;
            targets  = t;
            beanInfos = jbi;
        }
        handlers[handlerLen] = handler;
        targets[handlerLen]  = target;
        beanInfos[handlerLen] = beanInfo;
        handlerLen++;

        handler.activate(this);
    }
    
    /**
     * Pops a content handler from the stack and registers
     * it as the current content handler.
     *
     * <p>
     * This method will also fire the leaveChild event with the
     * associated memento.
     *
     * This method also fires the afterUnmarshal lifecycle events if necessary.
     */
    public void popContentHandler() throws SAXException {
        handlerLen--;
        Object child = targets[handlerLen];
        UnmarshallingEventHandler old = handlers[handlerLen];

        handlers[handlerLen]=null;  // this handler is removed
        targets[handlerLen]=null;

        old.deactivated(this);

        Object p;
        if(handlerLen==0) {
            p = null;
        } else {
            p = targets[handlerLen-1];
        }

        // unmarshaller lifecycle handling
        final JaxBeanInfo beanInfo = beanInfos[handlerLen];
        if((beanInfo!=null) && beanInfo.lookForLifecycleMethods()){
            // invoke the bean embedded listener first
            if(beanInfo.hasAfterUnmarshalMethod()) {
                Method m = beanInfo.getLifecycleMethods().getAfterUnmarshal();
                assert m != null;

                Object[] params = new Object[] {
                    parent /* unmarshaller */,
                    p      /* target */ };
                try {
                    m.invoke(child, params);
                } catch (IllegalAccessException e) {
                    throw new SAXException(e);
                } catch (InvocationTargetException e) {
                    throw new SAXException(e);
                }
            }

            // then invoke the external listener
            javax.xml.bind.Unmarshaller.Listener externalListener =
                    parent.getListener();
            if(externalListener != null) {
                externalListener.afterUnmarshal(
                        child, /* target */
                        p      /* parent */
                );
            }
        }

        if(handlerLen!=0)
            getCurrentHandler().leaveChild(this, child);
    }


    /**
     * Gets the current handler.
     */
    public UnmarshallingEventHandler getCurrentHandler() {
        return handlers[handlerLen-1];
    }

    /**
     * Replaces the current handler with a new one.
     */
    public void setCurrentHandler(UnmarshallingEventHandler handler) throws SAXException {
        UnmarshallingEventHandler old = handlers[handlerLen-1];
        handlers[handlerLen-1] = handler;
        if(old!=handler) {
            old.deactivated(this);
            handler.activate(this);
        }
    }

    /**
     * Gets the target object that the current unmarshaller
     * is unmarshalling.
     * <p>
     * Each {@link UnmarshallingEventHandler} is associated with
     * a fixed target object it deals with.
     *
     * @see #getCurrentHandler()
     */
    public <T> T getTarget() {
        return (T)targets[handlerLen-1];
    }

    /**
     * Sets the target object of the current unmarshaller.
     *
     * <p>
     * In some circumstances, the object to be unmarshalled can be only determined
     * during the unmarshalling process. In such cases, initially the target
     * is set to null, then it will be overriden by this method.
     *
     * <p>
     * When this method is called, the target for the current unmarshaller
     * must be null.
     *
     * @param t
     *      must be non-null
     */
    public void setTarget(Object t) {
        assert targets[handlerLen-1]==null;
        assert t!=null;
        targets[handlerLen-1]=t;
    }



//
//
// scope management
//
//
    private Scope[] scopes = new Scope[16];
    /**
     * Points to the top of the scope stack (=size-1).
     */
    private int scopeTop=0;

    {
        for( int i=0; i<scopes.length; i++ )
            scopes[i] = new Scope(this);
    }

    /**
     * Starts a new packing scope.
     *
     * <p>
     * This method allocates a specified number of fresh {@link Scope} objects.
     * They can be accessed by the {@link #getScope} method until the corresponding
     * {@link #endScope} method is invoked.
     *
     * <p>
     * A new scope will mask the currently active scope. Only one frame of {@link Scope}s
     * can be accessed at any given time.
     *
     * @param frameSize
     *      The # of slots to be allocated.
     */
    public void startScope(int frameSize) {
        scopeTop += frameSize;

        // reallocation
        if(scopeTop>=scopes.length) {
            Scope[] s = new Scope[Math.max(scopeTop+1,scopes.length*2)];
            System.arraycopy(scopes,0,s,0,scopes.length);
            for( int i=scopes.length; i<s.length; i++ )
                s[i] = new Scope(this);
            scopes = s;
        }
    }

    /**
     * Ends the current packing scope.
     *
     * <p>
     * If any packing in progress will be finalized by this method.
     *
     * @param frameSize
     *      The same size that gets passed to the {@link #startScope(int)}
     *      method.
     */
    public void endScope(int frameSize) throws SAXException {
        try {
            for( ; frameSize>0; frameSize-- )
                scopes[scopeTop--].finish();
        } catch (AccessorException e) {
            handleError(e);
        }
    }

    /**
     * Gets the currently active {@link Scope}.
     *
     * @param offset
     *      a number between [0,frameSize)
     *
     * @return
     *      always a valid {@link Scope} object.
     */
    public Scope getScope(int offset) {
        return scopes[scopeTop-offset];
    }




/*
    State Management
    ================

    during the unmarshalling, state can be used as a stack of objects.
    the stack top can be modified fairly quickly.
*/
    private Object[] states = new Object[16];
    /**
     * Points to the top of the scope stack (=size-1).
     */
    private int stateTop=0;

    public void startState(Object o) {
        states[++stateTop] = o;
    }

    public void setState(Object o) {
        states[stateTop] = o;
    }

    public <T> T getState() {
        return (T)states[stateTop];
    }

    public void endState() {
        states[stateTop] = null;
        stateTop--;
    }

    public void text(CharSequence pcdata) throws SAXException {
        pushCoordinator();
        if(currentElementDefaultValue!=null) {
            if(pcdata.length()==0) {
                // send the default value into the unmarshaller instead
                pcdata = currentElementDefaultValue;
            }
            // clobber the value so that it won't affect the successive elements incorrectly
            currentElementDefaultValue = null;
        }
        getCurrentHandler().text(this,pcdata);
        popCoordinator();
    }



    

//
//
// namespace binding maintainance
//
//
    private String[] nsBind = new String[16];
    private int nsLen=0;

    // in the current scope, nsBind[0] - nsBind[idxStack[idxStackTop]-1]
    // are active.
    // use {@link #elementDepth} and {@link stackTop} to access.
    private int[] idxStack = new int[16];

    public void startPrefixMapping( String prefix, String uri ) {
        if(nsBind.length==nsLen) {
            // expand the buffer
            String[] n = new String[nsLen*2];
            System.arraycopy(nsBind,0,n,0,nsLen);
            nsBind=n;
        }
        nsBind[nsLen++] = prefix;
        nsBind[nsLen++] = uri;
    }
    public void endPrefixMapping( String prefix ) {
        nsLen-=2;
    }
    private String resolveNamespacePrefix( String prefix ) {
        if(prefix.equals("xml"))
            return "http://www.w3.org/XML/1998/namespace";

        for( int i=idxStack[stackTop]-2; i>=0; i-=2 ) {
            if(prefix.equals(nsBind[i]))
                return nsBind[i+1];
        }
        return null;
    }

    /**
     * Returns a list of prefixes newly declared on this element.
     *
     * This method has to be called after the {@link #pushAttributes}
     * method is called.
     *
     * @return
     *      A possible zero-length array of prefixes. The default prefix
     *      is represented by the empty string.
     */
    public String[] getNewlyDeclaredPrefixes() {
        return getPrefixList( idxStack[stackTop-1] );
    }

    /**
     * Returns a list of all in-scope prefixes.
     *
     * @return
     *      A possible zero-length array of prefixes. The default prefix
     *      is represented by the empty string.
     */
    public String[] getAllDeclaredPrefixes() {
        return getPrefixList( 2 );  // skip the default ""->"" mapping
    }

    private String[] getPrefixList( int startIndex ) {
        int size = (idxStack[stackTop]-startIndex)/2;
        String[] r = new String[size];
        for( int i=0; i<r.length; i++ )
            r[i] = nsBind[startIndex+i*2];
        return r;
    }


    //
    //  NamespaceContext2 implementation
    //
    public Iterator<String> getPrefixes(String uri) {
        // TODO: could be implemented much faster
        // wrap it into unmodifiable list so that the remove method
        // will throw UnsupportedOperationException.
        return Collections.unmodifiableList(
            getAllPrefixesInList(uri)).iterator();
    }

    private List<String> getAllPrefixesInList(String uri) {
        List<String> a = new ArrayList<String>();

        if( uri==null )
            throw new IllegalArgumentException();
        if( uri.equals(XMLConstants.XML_NS_URI) ) {
            a.add(XMLConstants.XML_NS_PREFIX);
            return a;
        }
        if( uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) ) {
            a.add(XMLConstants.XMLNS_ATTRIBUTE);
            return a;
        }

        for( int i=nsLen-2; i>=0; i-=2 )
            if(uri.equals(nsBind[i+1]))
                if( getNamespaceURI(nsBind[i]).equals(nsBind[i+1]) )
                    // make sure that this prefix is still effective.
                    a.add(nsBind[i]);

        return a;
    }

    public String getPrefix(String uri) {
        if( uri==null )
            throw new IllegalArgumentException();
        if( uri.equals(XMLConstants.XML_NS_URI) )
            return XMLConstants.XML_NS_PREFIX;
        if( uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) )
            return XMLConstants.XMLNS_ATTRIBUTE;

        for( int i=idxStack[stackTop]-2; i>=0; i-=2 )
            if(uri.equals(nsBind[i+1]))
                if( getNamespaceURI(nsBind[i]).equals(nsBind[i+1]) )
                    // make sure that this prefix is still effective.
                    return nsBind[i];

        return null;
    }

     public String getNamespaceURI(String prefix) {
         if( prefix==null )
             throw new IllegalArgumentException();
         if( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) )
             return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

         return resolveNamespacePrefix(prefix);
     }

//
//
// Attribute handling
//
//
    /**
     * Attributes stack.
     */
    private AttributesExImpl[] attStack = new AttributesExImpl[16];
    /**
     * Element nesting level.
     */
    private int elementDepth;
    /**
     * Always {@link #elementDepth}-1.
     */
    private int stackTop;
    
    /**
     * Stack of collectText flag.
     * False means text can be ignored for this element.
     * 
     * Use {@link #elementDepth} and {@link #stackTop} to access the array.
     */ 
    private boolean[] collectText = new boolean[16];

    /**
     * Default value of the current element, if any.
     */
    private String currentElementDefaultValue;

    /**
     * Stores a new attribute set.
     * This method should be called by the generated code
     * when it "eats" an enterElement event.
     *
     * @param collectTextFlag
     *      false if the context doesn't need to fire text events
     *      for texts inside this element. True otherwise.
     * @param defaultValue
     *      if this element has a default value, non-null.
     */
    public void pushAttributes( Attributes atts, boolean collectTextFlag, String defaultValue ) {
        
        if( attStack.length==elementDepth ) {
            // reallocate the buffer
            AttributesExImpl[] buf1 = new AttributesExImpl[attStack.length*2];
            System.arraycopy(attStack,0,buf1,0,attStack.length);
            attStack = buf1;
            
            int[] buf2 = new int[idxStack.length*2];
            System.arraycopy(idxStack,0,buf2,0,idxStack.length);
            idxStack = buf2;

            boolean[] buf3 = new boolean[collectText.length*2];
            System.arraycopy(collectText,0,buf3,0,collectText.length);
            collectText = buf3;
        }
        
        elementDepth++;
        stackTop++;
        // push the stack
        AttributesExImpl a = attStack[stackTop];
        if( a==null )
            attStack[stackTop] = a = new AttributesExImpl();
        else
            a.clear();
        
        // since Attributes object is mutable, it is criticall important
        // to make a copy.
        // also symbolize attribute names
        for( int i=0; i<atts.getLength(); i++ ) {
            // TODO: implement AttributesEx support
            String auri = atts.getURI(i);
            String alocal = atts.getLocalName(i);
            String avalue = atts.getValue(i);
            
            // <foo xsi:nil="false">some value</foo> is a valid fragment, however
            // we need a look ahead to correctly handle this case.
            // (because when we process @xsi:nil, we don't know what the value is,
            // and by the time we read "false", we can't cancel this attribute anymore.)
            //
            // as a quick workaround, we remove @xsi:nil if the value is false.
            if( auri=="http://www.w3.org/2001/XMLSchema-instance" && alocal=="nil" ) {
                String v = avalue.trim();
                if(v.equals("false") || v.equals("0"))
                    continue;   // skip this attribute
            }
            
            // otherwise just add it.
            a.addAttribute(
                    auri,
                    alocal,
                    atts.getQName(i),
                    atts.getType(i),
                    avalue );
        }
        
        
        // start a new namespace scope
        idxStack[stackTop] = nsLen;

        collectText[stackTop] = collectTextFlag;

        this.currentElementDefaultValue = defaultValue;
    }

    /**
     * Disables the text collection within the current siblings.
     * <p>
     * Sometimes you don't know if you need to collect text when you
     * call {@link #pushAttributes}. If you later find out that text
     * is not necessary for that element, this method can be used
     * to disable it.
     * <p>
     * Collecting text and rejecting whitespace is very expensive,
     * so use this method to disable the collection as much as possible.
     */
    public void disableTextCollection() {
        collectText[stackTop] = false;
    }

    /**
     * Cancel the element default value for the current element.
     *
     * {@link UnmarshallingContext} triggers the default value processing
     * whenever there's no PCDATA for the current element, but this interferes
     * with @xsi:nil. So when we see @xsi:nil, we use this method to cancel
     * the element default value processing. 
     */
    public void resetCurrentElementDefaultValue() {
        currentElementDefaultValue = null;
    }


    /**
     * Discards the previously stored attribute set.
     * This method should be called by the generated code
     * when it "eats" a leaveElement event.
     */
    public void popAttributes() {
        stackTop--;
        elementDepth--;
    }
    /**
     * Gets all the unconsumed attributes.
     * If you need to find attributes based on more complex filter,
     * you need to use this method.
     */
    public AttributesExImpl getUnconsumedAttributes() {
        return attStack[stackTop];
    }
    /**
     * Gets the index of the attribute with the specified name.
     * This is usually faster when you only need to test with
     * a simple name.
     *
     * @param uri
     *      has to be interned.
     * @param local
     *      has to be interned.
     * @return
     *      -1 if not found.
     */
    public int getAttribute( String uri, String local ) {
        if(attStack[stackTop] !=null)
            return attStack[stackTop].getIndexFast(uri,local);
        return -1;
    }

    /**
     * @see #getAttribute(String, String)
     */
    public int getAttribute(Name name) {
        return getAttribute(name.nsUri,name.localName);
    }

    /**
     * Reads the attribute value without consuming it. Use with care.
     */
    public CharSequence getAttributeValue( int idx ) {
        return attStack[stackTop].getData(idx);
    }

    /**
     * Marks the attribute as "used" and return the value of the attribute.
     */
    public CharSequence eatAttribute( int idx ) {
        AttributesExImpl a = attStack[stackTop];
        
        CharSequence value = a.getData(idx);

        // mark the attribute as consumed
        a.removeAttribute(idx);
        
        return value;
    }

    protected ValidationEventLocator getLocation() {
        return locator.getLocation();
    }

//
//
// ID/IDREF related code
//
//
    /**
     * Submitted patchers in the order they've submitted.
     * Many XML vocabulary doesn't use ID/IDREF at all, so we
     * initialize it with null.
     */
    private Patcher[] patchers = null;
    private int patchersLen = 0;
    
    /**
     * Adds a job that will be executed at the last of the unmarshalling.
     * This method is used to support ID/IDREF feature, but it can be used
     * for other purposes as well.
     *
     * @param   job
     *      The run method of this object is called.
     */
    public void addPatcher( Patcher job ) {
        // re-allocate buffer if necessary
        if( patchers==null )
            patchers = new Patcher[32];
        if( patchers.length == patchersLen ) {
            Patcher[] buf = new Patcher[patchersLen*2];
            System.arraycopy(patchers,0,buf,0,patchersLen);
            patchers = buf;
        }
        patchers[patchersLen++] = job;
    }
    
    /** Executes all the patchers. */
    private void runPatchers() throws SAXException {
        if( patchers!=null ) {
            for( int i=0; i<patchersLen; i++ )
                patchers[i].run();
        }
    }

    /** Records ID->Object map. */
    private Hashtable<String,Object> idmap = null;

    /**
     * Adds the object which is currently being unmarshalled
     * to the ID table.
     *
     * @return
     *      Returns the value passed as the parameter.
     *      This is a hack, but this makes it easier for ID
     *      transducer to do its job.
     */
    // TODO: what shall we do if the ID is already declared?
    //
    // throwing an exception is one way. Overwriting the previous one
    // is another way. The latter allows us to process invalid documents,
    // while the former makes it impossible to handle them.
    //
    // I prefer to be flexible in terms of invalid document handling,
    // so chose not to throw an exception.
    //
    // I believe this is an implementation choice, not the spec issue.
    // -kk
    public String addToIdTable( String id ) {
        if(idmap==null)     idmap = new Hashtable<String,Object>();
        idmap.put( id, getTarget() );
        return id;
    }
    
    /**
     * Looks up the ID table and gets associated object.
     *
     * @return
     *      If there is no object associated with the given id,
     *      this method returns null.
     */
    // TODO: maybe we should throw UnmarshallingException
    // if we don't find ID.
    public Object getObjectFromId( String id ) {
        if(idmap==null)     return null;
        return idmap.get(id);
    }
    


    /**
     * Gets the current source location information in SAX {@link Locator}.
     * <p>
     * Sometimes the unmarshaller works against a different kind of XML source,
     * making this information meaningless.
     */
    public LocatorEx getLocator() { return locator; }

    private LocatorEx locator;


//
//
// error handling
//
//
    public final UnmarshallerImpl parent;
    private boolean aborted = false;
    
    /**
     * Reports an error to the user, and asks if s/he wants
     * to recover. If the canRecover flag is false, regardless
     * of the client instruction, an exception will be thrown.
     *
     * Only if the flag is true and the user wants to recover from an error,
     * the method returns normally.
     *
     * The thrown exception will be catched by the unmarshaller.
     */
    public void handleEvent(ValidationEvent event, boolean canRecover ) throws SAXException {
        ValidationEventHandler eventHandler = parent.getEventHandler();

        boolean recover = eventHandler.handleEvent(event);

        // if the handler says "abort", we will not return the object
        // from the unmarshaller.getResult()
        if(!recover)    aborted = true;

        if( !canRecover || !recover )
            throw new SAXParseException( event.getMessage(), locator,
                new UnmarshalException(
                    event.getMessage(),
                    event.getLinkedException() ) );
    }

    public boolean handleEvent(ValidationEvent event) {
        try {
            // if the handler says "abort", we will not return the object.
            boolean recover = parent.getEventHandler().handleEvent(event);
            if(!recover)    aborted = true;
            return recover;
        } catch( RuntimeException re ) {
            // if client event handler causes a runtime exception, then we
            // have to return false.
            return false;
        }
    }

    /**
     * Reports an exception found during the unmarshalling to the user.
     * This method is a convenience method that calls into
     * {@link #handleEvent(ValidationEvent, boolean)}
     */
    public void handleError(Exception e) throws SAXException {
        handleError(e,true);
    }

    public void handleError(Exception e,boolean canRecover) throws SAXException {
        handleEvent(new ValidationEventImpl(ValidationEvent.ERROR,e.getMessage(),locator.getLocation(),e),canRecover);
    }

    public void handleError(String msg) {
        handleEvent(new ValidationEventImpl(ValidationEvent.ERROR,msg,locator.getLocation()));
    }

    /**
     * Called when there's no corresponding ID value.
     */
    public void errorUnresolvedIDREF(Object bean, String idref) throws SAXException {
        handleEvent( new ValidationEventImpl(
            ValidationEvent.ERROR,
            Messages.UNRESOLVED_IDREF.format(idref),
            new ValidationEventLocatorImpl(bean)), true );
    }


    /**
     * Computes the names of possible root elements for a better error diagnosis.
     */
    private String computeExpectedRootElements() {
        String r = "";

        for( QName n : parent.context.getValidRootNames() ) {
            if(r.length()!=0)   r+=',';
            r += "<{"+n.getNamespaceURI()+'}'+n.getLocalPart()+'>';
        }
        
        return r;
    }




//
// in-place unmarshalling related capabilities
//
    /**
     * Notifies the context about the inner peer of the current element.
     *
     * <p>
     * If the unmarshalling is building the association, the context
     * will use this information. Otherwise it will be just ignored.
     */
    public void recordInnerPeer(Object innerPeer) {
        if(assoc!=null)
            assoc.addInner(currentElement,innerPeer);
    }
    
    /**
     * Gets the inner peer JAXB object associated with the current element.
     *
     * @return
     *      null if the current element doesn't have an inner peer,
     *      or if we are not doing the in-place unmarshalling.
     */
    public Object getInnerPeer() {
        if(assoc!=null && isInplaceMode)
            return assoc.getInnerPeer(currentElement);
        else
            return null;
    }

    /**
     * Notifies the context about the outer peer of the current element.
     *
     * <p>
     * If the unmarshalling is building the association, the context
     * will use this information. Otherwise it will be just ignored.
     */
    public void recordOuterPeer(Object outerPeer) {
        if(assoc!=null)
            assoc.addOuter(currentElement,outerPeer);
    }
    
    /**
     * Gets the outer peer JAXB object associated with the current element.
     *
     * @return
     *      null if the current element doesn't have an inner peer,
     *      or if we are not doing the in-place unmarshalling.
     */
    public Object getOuterPeer() {
        if(assoc!=null && isInplaceMode)
            return assoc.getOuterPeer(currentElement);
        else
            return null;
    }




    /**
     * Gets the xmime:contentType value for the current object.
     *
     * @see JAXBContextImpl#getXMIMEContentType(Object)
     */
    public String getXMIMEContentType() {
        /*
            this won't work when the class is like

            class Foo {
                @XmlValue Image img;
            }

            because the target will return Foo, not the class enclosing Foo
            which will have xmime:contentType
        */
        Object t = getTarget();
        if(t==null)     return null;
        return getJAXBContext().getXMIMEContentType(t);
    }

    /**
     * When called from within the realm of the unmarshaller, this method
     * returns the current {@link UnmarshallingContext} in charge.
     */
    public static final UnmarshallingContext getInstance() {
        return (UnmarshallingContext)Coordinator._getInstance();
    }
}

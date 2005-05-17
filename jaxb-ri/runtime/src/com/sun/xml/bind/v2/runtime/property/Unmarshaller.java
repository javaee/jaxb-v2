package com.sun.xml.bind.v2.runtime.property;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.AbstractUnmarshallingEventHandlerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.EventArg;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingEventHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Fragments of unmarshaller.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class Unmarshaller {
    private Unmarshaller() {};

    public static enum EventType {
        ENTER_ELEMENT,
        LEAVE_ELEMENT,
        TEXT
    }

    public static abstract class Handler extends AbstractUnmarshallingEventHandlerImpl {
        protected Handler() {}

        /**
         * Returns the {@link Handler} object to which the event should be sent.
         *
         * <p>
         * This method is only used during the construction of the graph,
         * so it can be slow.
         *
         * @return
         *      {@code this} if this handler itself is interested in that event.
         *      Otherwise, if this handler is a {@link DelegatingHandler} and
         *      some delegated handlers are expected to process it, return that
         *      delegated handler.
         *
         *      Never return null.
         */
        protected abstract Handler forwardTo(EventType event);

    }

    public static abstract class EpsilonHandler extends Handler {
        protected EpsilonHandler() {
        }

        protected Handler forwardTo(EventType event) {
            return this;
        }

        /**
         * Derived class can override this method to do some action.
         */
        public void activate(UnmarshallingContext context) throws SAXException {
            // pass on to the next handler
            handle(context);
        }

        public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
            _handle(context).enterElement(context, arg);
        }

        public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
            _handle(context).leaveElement(context, arg);
        }

        public void text(UnmarshallingContext context, CharSequence s) throws SAXException {
            _handle(context).text(context, s);
        }

        private UnmarshallingEventHandler _handle(UnmarshallingContext context) throws SAXException {
            handle(context);
            return context.getCurrentHandler();
        }

        /**
         * The handle method is expected to set another handler
         * or pop the current handler.
         */
        protected abstract void handle(UnmarshallingContext context) throws SAXException;
    }

    /**
     * {@link UnmarshallingEventHandler} that delegates events to another handler.
     *
     * Used as a base class for more interesting {@link UnmarshallingEventHandler}
     * implementations.
     */
    public static class DelegatingHandler extends Handler {
        // handler will be delegated to this
        private Handler delegateEnterElement;
        private Handler delegateLeaveElement;
        private Handler delegateText;

        private Handler fallthrough; // this is for debug only.

        /**
         * @param fallthrough
         *      the control fall to this handler.
         */
        protected DelegatingHandler(Handler fallthrough) {
            if(fallthrough!=null)
                setFallthrough(fallthrough);
        }

        protected final void setFallthrough(Handler fallthrough) {
            this.fallthrough = fallthrough;

            this.delegateEnterElement   = fallthrough.forwardTo(EventType.ENTER_ELEMENT);
            this.delegateLeaveElement   = fallthrough.forwardTo(EventType.LEAVE_ELEMENT);
            this.delegateText           = fallthrough.forwardTo(EventType.TEXT);
        }

        public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
            delegateEnterElement.enterElement(context, arg);
        }

        public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
            delegateLeaveElement.leaveElement(context, arg);
        }

        public void text(UnmarshallingContext context, CharSequence s) throws SAXException {
            delegateText.text(context, s);
        }

        protected Handler forwardTo(EventType event) {
            switch(event) {
            case ENTER_ELEMENT:
                return delegateEnterElement;
            case LEAVE_ELEMENT:
                return delegateLeaveElement;
            case TEXT:
                return delegateText;
            }
            assert false;
            return null;
        }
    }

    /**
     * Reports error for any event.
     */
    public static final Handler ERROR = new Handler() {
        protected Handler forwardTo(EventType event) {
            return this;
        }

        public void text(UnmarshallingContext context, CharSequence s) throws SAXException {
            if(WhiteSpaceProcessor.isWhiteSpace(s))
                return; // just ignore
            else
                super.text(context,s);
        }
    };

    public static class ForkHandler extends DelegatingHandler {
        public Handler next;

        public ForkHandler(Handler fallthrough, Handler next) {
            super(fallthrough);
            this.next = next;
        }
    }

    public abstract static class AttributeHandler extends EpsilonHandler {

        private final Handler next;
        private final Handler fallthrough;

        protected AttributeHandler(Handler fallthrough, Handler next) {
            this.fallthrough = fallthrough;
            this.next = next;
        }

        public void handle(UnmarshallingContext context) throws SAXException {
            if(checkAttribute(context))
                context.setCurrentHandler(next);
            else
                context.setCurrentHandler(fallthrough);
        }

        /**
         * Derived classes should check attributes here.
         */
        protected abstract boolean checkAttribute(UnmarshallingContext context) throws SAXException;

        /**
         * Unmarshals the value of the attribute.
         *
         * Invoked when the attribute value was found.
         *
         * @throws AccessorException
         *      for the convenience of the derived classes, this method can
         *      throw an AccessorException.
         */
        public abstract void processValue(UnmarshallingContext context,
            String nsUri, String local, String qname, CharSequence value) throws AccessorException, SAXException;
    }

    /**
     * Unmarshals an attribute.
     *
     * <p>
     * This can be either used as a {@link Handler} and combined into a bigger
     * unmarshaller graph, or the caller can just invoke {@link #processValue(UnmarshallingContext,String,String,String,CharSequence)}
     * to unmarshal the value of the attribute.
     */
    public abstract static class SingleAttributeHandler extends AttributeHandler {

        /**
         * The name of the attribute that this handler looks for.
         */
        public final Name name;

        public SingleAttributeHandler(Name name, Handler fallthrough,Handler next) {
            super(fallthrough, next);
            this.name = name;
        }


        protected boolean checkAttribute(UnmarshallingContext context) throws SAXException {
            int idx = context.getAttribute(name);
            if(idx!=-1) {
                String qname = context.getUnconsumedAttributes().getQName(idx);
                CharSequence value = context.eatAttribute(idx);
                try {
                    processValue(context,name.nsUri,name.localName,qname,value);
                } catch (AccessorException e) {
                    handleGenericException(e,true);
                } catch (RuntimeException e) {
                    handleParseConversionException(context,e);
                }
                return true;
            } else
                return false;
        }
    }

    public final static class AttributeWildcardHandler extends AttributeHandler {
        /**
         * Accessor to access the attribute wildcard property.
         */
        private final Accessor acc;

        public AttributeWildcardHandler(Accessor<?,Map<QName,Object>> acc,Handler fallthrough) {
            super(fallthrough,fallthrough);
            this.acc = acc;
            assert acc!=null;
        }

        /**
         * Simply process all the values.
         */
        protected boolean checkAttribute(UnmarshallingContext context) throws SAXException {
            Attributes atts = context.getUnconsumedAttributes();
            for (int i = 0; i < atts.getLength(); i ++){
                String auri = atts.getURI(i);
                String alocal = atts.getLocalName(i);
                String aqname = atts.getQName(i);
                String avalue = atts.getValue(i);

                try {
                    processValue(context,auri,alocal,aqname,avalue);
                } catch (AccessorException e) {
                   handleGenericException(e,true);
                }
            }
            for (int i = atts.getLength()-1; i>=0; i--)
                context.eatAttribute(i);
            return false;
        }

        public void processValue(UnmarshallingContext context, String nsUri, String local, String qname, CharSequence value) throws AccessorException, SAXException {
            Object o = context.getTarget();
            Map<QName,String> map = (Map<QName,String>)acc.get(o);
            if(map==null) {
                // if null, create a new map.
                if(HashMap.class.isAssignableFrom(acc.valueType))
                    map = new HashMap<QName,String>();
                else {
                    // we don't know how to create a map for this.
                    // report an error and back out
                    context.handleError(Messages.UNABLE_TO_CREATE_MAP.format(acc.valueType));
                    return;
                }
                acc.set(o,map);
            }

            String prefix;
            int idx = qname.indexOf(':');
            if(idx<0)   prefix="";
            else        prefix=qname.substring(0,idx);

            map.put(new QName(nsUri,local,prefix),value.toString());
        }
    }


    /**
     * Looks for an enterElement event and consumes it.
     */
    public static class EnterElementHandler extends ForkHandler {

        private final Name name;

        private final boolean collectText;

        /**
         * Default value of this element if any. Otherwise null.
         */
        private final String defaultValue;

        public EnterElementHandler(Name name, boolean elementOnly, String defaultValue, Handler fallthrough, Handler next) {
            super(fallthrough,next);
            this.name = name;
            this.collectText = !elementOnly;
            this.defaultValue = defaultValue;
        }

        public void text(UnmarshallingContext context,CharSequence s) throws SAXException {
             next.text(context,s);
        }
        public void enterElement(UnmarshallingContext context, EventArg arg) throws SAXException {
            if(arg.matches(name)) {
                context.pushAttributes(arg.atts,collectText,defaultValue);
                act(context);
                return;
            }
            next.enterElement(context,arg);
        }

        /**
         * Called when the element matched.
         */
        protected void act(UnmarshallingContext context) throws SAXException {
            context.setCurrentHandler(next);
        }

        protected Handler forwardTo(EventType event) {
            if(event==EventType.ENTER_ELEMENT)
                return this;
            else
                return super.forwardTo(event);
        }

        public String toString() {
            return "EnterElementHandler "+name;
        }
    }

    /**
     * Looks for a leaveElement event and consumes it.
     */
    public static class LeaveElementHandler extends ForkHandler {

        public LeaveElementHandler(Handler fallthrough,Handler next) {
            super(fallthrough,next);
        }

        public void leaveElement(UnmarshallingContext context, EventArg arg) throws SAXException {
            context.popAttributes();
            context.setCurrentHandler(next);
// consuming the leave element and poping the handler are two different actions.
//            context.popContentHandler();
        }

        protected Handler forwardTo(EventType event) {
            if(event==EventType.LEAVE_ELEMENT)
                return this;
            else
                return super.forwardTo(event);
        }

        public String toString() {
            return "LeaveElement@"+System.identityHashCode(this);
        }
    }

    /**
     * Looks for a text event and consumes it.
     */
    public abstract static class RawTextHandler extends ForkHandler {

        public RawTextHandler(Handler fallthrough,Handler next) {
            super(fallthrough,next);
        }

        public final void text(UnmarshallingContext context, CharSequence s) throws SAXException {
            processText(context,s);
            context.setCurrentHandler(next);
        }

        public abstract void processText(UnmarshallingContext context, CharSequence s) throws SAXException;

        protected Handler forwardTo(EventType event) {
            if(event==EventType.TEXT)
                return this;
            else
                return super.forwardTo(event);
        }
    }

    /**
     * Looks for a text event, parses it, and sets it to a field.
     */
    public static final class TextHandler extends RawTextHandler {

        private final TransducedAccessor acc;

        public TextHandler(TransducedAccessor acc,Handler fallthrough,Handler next) {
            super(fallthrough,next);
            this.acc = acc;
            assert acc!=null;
        }

        public void processText(UnmarshallingContext context, CharSequence s) throws SAXException {
            try {
                acc.parse(context.getTarget(),s);
            } catch (AccessorException e) {
                handleGenericException(e,true);
            } catch (RuntimeException e) {
                handleParseConversionException(context,e);
            }
        }

        public String toString() {
            return "TextHandler "+acc.toString();
        }
    }

    /**
     * Reverts to the parent by any event.
     */
    public static final Handler REVERT_TO_PARENT = new RevertToParentHandler();

    private static final class RevertToParentHandler extends EpsilonHandler {
        public void handle(UnmarshallingContext context) throws SAXException {
            // eagerly return the control to the parent
            context.popContentHandler();
        }
    };

    /**
     * Spawns the unmarshaller of the child (also looks at xsi:type)
     */
    public static abstract class SpawnChildHandler extends EpsilonHandler {
        /**
         * {@link JaxBeanInfo} for the target class.
         * Obtained lazily, but once set, it will never change.
         */
        protected final JaxBeanInfo targetBeanInfo;

        private final Handler next;

        /**
         * Set to true if the element unmarshaller of the target should be invoked.
         * Otherwise false to invoke the type unmarshaller
         */
        private boolean childAsElement;

        public SpawnChildHandler(JaxBeanInfo target, Handler next, boolean childAsElement) {
            this.next = next;
            this.targetBeanInfo = target;
            this.childAsElement = childAsElement;
        }

        @Override
        public void leaveChild(UnmarshallingContext context, Object child) throws SAXException {
            try {
                onNewChild(context.getTarget(),child,context);
            } catch (AccessorException e) {
                handleGenericException(e);
            }
            context.setCurrentHandler(next);
        }

        protected void handle(UnmarshallingContext context) throws SAXException {
            // look for @xsi:type
            int idx = context.getAttribute(WellKnownNamespace.XML_SCHEMA_INSTANCE,"type");

            JaxBeanInfo beanInfo = targetBeanInfo;

            if(idx>=0) {
                // we'll consume the value only when it's a recognized value,
                // so don't consume it just yet.
                CharSequence value = context.getAttributeValue(idx);

                QName type = DatatypeConverterImpl._parseQName(value,context);
                if(type==null) {
                    reportError(Messages.NOT_A_QNAME.format(value),true);
                } else {
                    beanInfo =  context.getJAXBContext().getGlobalType(type);
                    if(beanInfo!=null) {
                        context.eatAttribute(idx);
                    } else {
                        reportError(Messages.UNRECOGNIZED_TYPE_NAME.format(value),true);
                        beanInfo = targetBeanInfo;  // try to recover by using the default target type.
                    }
                    // TODO: resurrect the following check
//                    else
//                    if(!target.isAssignableFrom(actual)) {
//                        reportError(context,
//                            Messages.UNSUBSTITUTABLE_TYPE.format(value,actual.getName(),target.getName()),
//                            true);
//                        actual = targetBeanInfo;  // ditto
//                    }
                }
            }

            spawnChild(context,beanInfo,childAsElement);
        }

        /**
         * Called when the child object is finished unmarshalling.
         *
         * @throws AccessorException
         *      for the convenience of the derived classes, this method can
         *      throw a {@link AccessorException}.
         */
        protected abstract void onNewChild(Object bean, Object value, UnmarshallingContext context) throws AccessorException, SAXException;
    }

    public static final class SpawnChildSetHandler extends SpawnChildHandler {
        private final Accessor acc;

        public SpawnChildSetHandler(JaxBeanInfo target, Handler next, boolean childAsElement, Accessor acc) {
            super(target, next, childAsElement);
            this.acc = acc;
        }

        protected void onNewChild(Object bean, Object value, UnmarshallingContext context) throws AccessorException {
            acc.set(bean,value);
        }
    }
    

    /**
     * Looks for xsi:nil='true' and sets the property to null.
     */
    public static abstract class XsiNilHandler extends EpsilonHandler {
        /**
         * {@link Handler} that processes the end element.
         */
        private final Handler endElement;

        private final Handler next;

        public XsiNilHandler(Unmarshaller.Handler next, Unmarshaller.Handler ee) {
            this.next = next;
            this.endElement = ee;
        }

        // called right after the enterElement is consumed
        protected void handle(UnmarshallingContext context) throws SAXException {
            int idx = context.getAttribute(WellKnownNamespace.XML_SCHEMA_INSTANCE,"nil");
            if(idx!=-1) {
                CharSequence value = context.eatAttribute(idx);
                if(DatatypeConverterImpl._parseBoolean(value)) {
                    setToNull(context);
                    context.resetCurrentElementDefaultValue();
                    context.setCurrentHandler(endElement);
                    return;
                }
            }

            context.setCurrentHandler(next);
        }

        /**
         * Set the value to null.
         */
        protected abstract void setToNull(UnmarshallingContext context) throws SAXException;
    }

    /**
     * Looks for xsi:nil='true' and sets the property to null.
     */
    public static final class SingleXsiNilHandler extends XsiNilHandler {
        private final Accessor acc;

        public SingleXsiNilHandler(Unmarshaller.Handler next, Unmarshaller.Handler ee, Accessor acc) {
            super(next,ee);
            this.acc = acc;
        }

        protected void setToNull(UnmarshallingContext context) throws SAXException {
            try {
                acc.set(context.getTarget(),null);
            } catch (AccessorException e) {
                handleGenericException(e,true);
            }
        }
    }

    public static final class ArrayXsiNilHandler extends XsiNilHandler {
        private final Accessor acc;
        private final int offset;
        private final Lister lister;

        public ArrayXsiNilHandler(Handler next, Handler ee, Accessor acc, int offset, Lister lister) {
            super(next, ee);
            this.acc = acc;
            this.offset = offset;
            this.lister = lister;
        }

        protected void setToNull(UnmarshallingContext context) throws SAXException {
            context.getScope(offset).add(acc,lister,null);
        }
    }


    public static final class SpawnNewChildHandler extends SpawnChildHandler{
        public SpawnNewChildHandler(JaxBeanInfo target, Handler next, boolean childAsElement ) {
            super(target,next,childAsElement);
        }

        public  void onNewChild(Object bean, Object value, UnmarshallingContext context) {
            targetBeanInfo.getUnmarshaller(false);
        }
    } ;
}
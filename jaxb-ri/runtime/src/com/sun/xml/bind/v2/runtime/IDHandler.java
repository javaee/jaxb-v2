package com.sun.xml.bind.v2.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Patcher;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * Code that handles ID/IDREF/IDREFS.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class IDHandler {
    private IDHandler() {} // noinstanciation

    /**
     * Called when there's no corresponding ID value.
     */
    private static void errorUnresolvedIDREF(Object bean, String idref, UnmarshallingContext context) throws SAXException {
        context.handleEvent( new ValidationEventImpl(
            ValidationEvent.ERROR,
            Messages.UNRESOLVED_IDREF.format(idref),
            new ValidationEventLocatorImpl(bean)), true );
    }

    /**
     * Called when a referenced object doesn't have an ID.
     */
    private static void errorMissingId(XMLSerializer context, Object obj) throws SAXException {
        context.reportError( new ValidationEventImpl(
            ValidationEvent.ERROR,
            Messages.MISSING_ID.format(obj),
            new ValidationEventLocatorImpl(obj)) );
    }



    public static final class IDREFS<BeanT,PropT> extends Lister<BeanT,PropT,String,IDREFS<BeanT,PropT>.Pack> {
        private final Lister<BeanT,PropT,Object,Object> core;

        public IDREFS(Lister<BeanT,PropT,Object,Object> core) {
            this.core = core;
        }

        public ListIterator<String> iterator(PropT prop, XMLSerializer context) {
            final ListIterator i = core.iterator(prop,context);

            return new IDREFSIterator(i, context);
        }

        public Pack startPacking(BeanT bean, Accessor<BeanT, PropT> acc) {
            return new Pack(bean,acc);
        }

        public void addToPack(Pack pack, String item) {
            pack.add(item);
        }

        public void endPacking(Pack pack, BeanT bean, Accessor<BeanT, PropT> acc) {
        }

        public void reset(BeanT bean, Accessor<BeanT, PropT> acc) throws AccessorException {
            core.reset(bean,acc);
        }

        /**
         * PackT for this lister.
         */
        private class Pack implements Patcher {
            private final BeanT bean;
            private final List<String> idrefs = new ArrayList<String>();
            private final UnmarshallingContext context;
            private final Accessor<BeanT,PropT> acc;

            public Pack(BeanT bean, Accessor<BeanT,PropT> acc) {
                this.bean = bean;
                this.acc = acc;
                this.context = UnmarshallingContext.getInstance();
                context.addPatcher(this);
            }

            public void add(String item) {
                idrefs.add(item);
            }

            /**
             * Resolves IDREFS and fill in the actual array.
             */
            public void run() throws SAXException {
                try {
                    Object pack = core.startPacking(bean,acc);

                    for( String id : idrefs ) {
                        Object t = context.getObjectFromId(id);
                        if(t==null) {
                            errorUnresolvedIDREF(bean,id,context);
                        } else {
                            TODO.prototype(); // TODO: check if the type of t is proper.
                            core.addToPack(pack,t);
                        }
                    }

                    core.endPacking(pack,bean,acc);
                } catch (AccessorException e) {
                    context.handleError(e);
                }
            }
        }
    }

    /**
     * {@link Iterator} for IDREFS lister.
     *
     * <p>
     * Only in ArrayElementProperty we need to get the actual
     * referenced object. This is a kind of ugly way to make that work.
     */
    public static final class IDREFSIterator implements ListIterator<String> {
        private final ListIterator i;
        private final XMLSerializer context;
        private Object last;

        private IDREFSIterator(ListIterator i, XMLSerializer context) {
            this.i = i;
            this.context = context;
        }

        public boolean hasNext() {
            return i.hasNext();
        }

        /**
         * Returns the last referenced object (not just its ID)
         */
        public Object last() {
            return last;
        }

        public String next() throws SAXException, JAXBException {
            last = i.next();
            String id = context.grammar.getBeanInfo(last,true).getId(last,context);
            if(id==null) {
                errorMissingId(context,last);
            }
            return id;
        }
    }

    /**
     * {@link TransducedAccessor} for IDREF.
     *
     * BeanT: the type of the bean that contains this the IDREF field.
     * TargetT: the type of the bean pointed by IDREF.
     */
    public static final class IDREF<BeanT,TargetT> extends TransducedAccessor<BeanT> {
        private final Accessor<BeanT,TargetT> acc;
        /**
         * The object that an IDREF resolves to should be
         * assignable to this type.
         */
        private final Class<TargetT> targetType;

        public IDREF(Accessor<BeanT, TargetT> acc) {
            this.acc = acc;
            this.targetType = acc.getValueType();
        }

        public String print(BeanT bean) throws AccessorException, SAXException {
            TargetT target = acc.get(bean);
            XMLSerializer w = XMLSerializer.getInstance();
            try {
                String id = w.grammar.getBeanInfo(target,true).getId(target,w);
                if(id==null)
                    errorMissingId(w,target);
                return id;
            } catch (JAXBException e) {
                w.reportError(null,e);
                return null;
            }
        }

        /**
         * Resolves the ID and sets the resolved object to the field.
         *
         * @return true
         *      if the resolution is successful. Otherwise false, in which case the
         *      field is untouched.
         */
        private boolean resolveId(BeanT bean, String id, UnmarshallingContext context) throws AccessorException {
            TargetT t = (TargetT)context.getObjectFromId(id);
            if(t==null)     return false;

            if(!targetType.isInstance(t)) {
                // TODO: report an error to the context
                TODO.prototype();
            }
            acc.set(bean,t);
            return true;
        }

        public void parse(final BeanT bean, CharSequence lexical) throws AccessorException {
            final String idref = WhiteSpaceProcessor.trim(lexical).toString();
            final UnmarshallingContext context = UnmarshallingContext.getInstance();
            if(!resolveId(bean,idref,context)) {
                // if we can't resolve it now, resolve it later
                context.addPatcher(new Patcher() {
                    public void run() throws SAXException {
                        try {
                            if(!resolveId(bean,idref,context)) {
                                errorUnresolvedIDREF(bean,idref,context);
                            }
                        } catch (AccessorException e) {
                            context.handleError(e);
                        }
                    }
                });
            }
        }

        public boolean hasValue(BeanT bean) throws AccessorException {
            return acc.get(bean)!=null;
        }
    }

    /**
     * Transducer implementation for ID.
     *
     * This transducer wraps another {@link Transducer} and adds
     * handling for ID.
     */
    public static final class ID<ValueT> implements Transducer<ValueT> {
        private final Transducer<ValueT> core;

        public ID(Transducer<ValueT> core) {
            this.core = core;
        }

        public boolean isDefault() {
            return false;
        }

        public boolean useNamespace() {
            return core.useNamespace();
        }

        public void declareNamespace( ValueT o, XMLSerializer w ) throws AccessorException {
            core.declareNamespace(o, w);
        }

        public CharSequence print(ValueT o) throws AccessorException {
            return core.print(o);
        }

        public ValueT parse(CharSequence lexical) throws AccessorException, SAXException {
            String value = WhiteSpaceProcessor.trim(lexical).toString();
            UnmarshallingContext.getInstance().addToIdTable(value);
            return core.parse(value);
        }
    }
}

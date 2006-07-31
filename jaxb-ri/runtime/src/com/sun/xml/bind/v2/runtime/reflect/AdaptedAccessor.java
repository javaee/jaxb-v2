package com.sun.xml.bind.v2.runtime.reflect;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.runtime.Coordinator;

/**
 * {@link Accessor} that adapts the value by using {@link Adapter}.
 *
 * @see Accessor#adapt
 * @author Kohsuke Kawaguchi
 */
final class AdaptedAccessor<BeanT,InMemValueT,OnWireValueT> extends Accessor<BeanT,OnWireValueT> {
    private final Accessor<BeanT,InMemValueT> core;
    private final Class<? extends XmlAdapter<OnWireValueT,InMemValueT>> adapter;

    /*pacakge*/ AdaptedAccessor(Class<OnWireValueT> targetType, Accessor<BeanT, InMemValueT> extThis, Class<? extends XmlAdapter<OnWireValueT, InMemValueT>> adapter) {
        super(targetType);
        this.core = extThis;
        this.adapter = adapter;
    }

    public OnWireValueT get(BeanT bean) throws AccessorException {
        InMemValueT v = core.get(bean);

        XmlAdapter<OnWireValueT,InMemValueT> a = getAdapter();
        try {
            return a.marshal(v);
        } catch (Exception e) {
            throw new AccessorException(e);
        }
    }

    public void set(BeanT bean, OnWireValueT o) throws AccessorException {
        XmlAdapter<OnWireValueT, InMemValueT> a = getAdapter();
        try {
            core.set(bean,a.unmarshal(o));
        } catch (Exception e) {
            throw new AccessorException(e);
        }
    }

    public Object getUnadapted(BeanT bean) throws AccessorException {
        return core.getUnadapted(bean);
    }

    public void setUnadapted(BeanT bean, Object value) throws AccessorException {
        core.setUnadapted(bean,value);
    }

    /**
     * Sometimes Adapters are used directly by JAX-WS outside any
     * {@link Coordinator}. Use this lazily-created cached
     * {@link XmlAdapter} in such cases.
     */
    private XmlAdapter<OnWireValueT, InMemValueT> staticAdapter;

    private XmlAdapter<OnWireValueT, InMemValueT> getAdapter() {
        Coordinator coordinator = Coordinator._getInstance();
        if(coordinator!=null)
            return coordinator.getAdapter(adapter);
        else {
            synchronized(this) {
                if(staticAdapter==null)
                    staticAdapter = ClassFactory.create(adapter);
            }
            return staticAdapter;
        }
    }
}

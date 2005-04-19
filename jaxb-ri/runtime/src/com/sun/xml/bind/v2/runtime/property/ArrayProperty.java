package com.sun.xml.bind.v2.runtime.property;


import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Lister;


/**
 * {@link Property} implementation for multi-value properties
 * (including arrays and collections.)
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
abstract class ArrayProperty<BeanT,ListT,ItemT> extends PropertyImpl<BeanT> {
    protected final Accessor<BeanT,ListT> acc;
    protected final Lister<BeanT,ListT,ItemT,Object> lister;

    protected ArrayProperty(JAXBContextImpl context, RuntimePropertyInfo prop) {
        super(context,prop);

        assert prop.isCollection();
        lister = Lister.create(Navigator.REFLECTION.erasure(prop.getRawType()),prop.id());
        assert lister!=null;
        acc = prop.getAccessor().optimize();
        assert acc!=null;
    }

    public void reset(BeanT o) throws AccessorException {
        lister.reset(o,acc);
    }

    public final String getIdValue(BeanT bean) {
        // mutli-value property can't be ID
        return null;
    }
}

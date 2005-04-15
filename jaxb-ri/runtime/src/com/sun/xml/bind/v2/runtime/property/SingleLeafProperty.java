package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.IDHandler;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

import org.xml.sax.SAXException;

/**
 * A {@link Property} that has a single value field.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class SingleLeafProperty<BeanT> extends PropertyImpl<BeanT> {

    protected final TransducedAccessor<BeanT> xacc;

    protected SingleLeafProperty(JAXBContextImpl context,RuntimePropertyInfo prop) {
        super(context,prop);

        if(prop.id()==ID.IDREF) {
            // IDREF uses a special transduced accessor
            xacc = new IDHandler.IDREF(prop.getAccessor().optimize());
        } else {
            TODO.prototype();
            xacc = TransducedAccessor.get(prop,prop.ref().iterator().next().getTransducer());
        }
        assert xacc!=null;
    }

    public void reset(BeanT o) {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    public String getIdValue(BeanT bean) throws AccessorException, SAXException {
        return xacc.print(bean).toString();
    }
}

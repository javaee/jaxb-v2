package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.IDHandler;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

/**
 * @author Kohsuke Kawaguchi
 */
class RuntimeAttributePropertyInfoImpl extends AttributePropertyInfoImpl<Type,Class,Field,Method>
    implements RuntimeAttributePropertyInfo {

    private final Accessor acc;

    /**
     * Lazily created.
     */
    private TransducedAccessor xacc;

    RuntimeAttributePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type,Class,Field,Method> seed) {
        super(classInfo, seed);
        this.acc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
    }

    public Type getRawType() {
        return seed.getRawType();
    }

    public Accessor getAccessor() {
        return acc;
    }

    public boolean elementOnlyContent() {
        return true;
    }

    public RuntimeNonElement getType() {
        return (RuntimeNonElement) super.getType();
    }

    public List<? extends RuntimeNonElement> ref() {
        return (List<? extends RuntimeNonElement>)super.ref();
    }

    public TransducedAccessor getTransducedAccessor() {
        if(xacc==null)
            calcTransducedAccessor();
        return xacc;
    }

    // TODO: share this code with RuntimeValuePropertyInfoImpl
    private <BeanT,ItemT> void calcTransducedAccessor() {
        Transducer<ItemT> xducer = getType().getTransducer();
        if(xducer==null) {
            parent().builder.reportError(new IllegalAnnotationException(
                Messages.ILLEGAL_TYPE_FOR_ATTRIBUTE.format(getType().getType()),
                this
            ));
            xacc = TransducedAccessor.ERROR;
            return;
        }
        if(!isCollection()) {
            if(id()==ID.IDREF) {
                // IDREF uses a special transduced accessor
                xacc = new IDHandler.IDREF(getAccessor().optimize());
            } else {
                xacc = TransducedAccessor.get(this,xducer);
            }
        } else {
            if(id()==ID.IDREF) {
                xducer = (Transducer)RuntimeBuiltinLeafInfoImpl.STRING;
            }
            xacc = new ListTransducedAccessorImpl(xducer,acc,
                    Lister.create(Navigator.REFLECTION.erasure(getRawType()),id()));
        }
    }

    public void link() {
        getTransducedAccessor();
        super.link();
    }
}

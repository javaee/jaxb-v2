package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeValuePropertyInfo extends ValuePropertyInfo<Type,Class>,RuntimePropertyInfo {
    RuntimeNonElement getType();

    /**
     * Gets the {@link TransducedAccessor} for this property.
     *
     * <p>
     * Because a value property can be always parsed/printed from/to
     * a chunk of text. the gut of its logic can be summarized
     * to {@link TransducedAccessor}.
     *
     * @return
     *      always non-null.
     */
    /*
        TODO: should this method really be on the model?
        Model returns Accessors and Transducers in other places,
        and TransducedAccessor = Transducer + Accessor, so in a sense, why not.

        But OTOH, this seems to blur the border between Properties and PropertyInfos.
    */
    TransducedAccessor getTransducedAccessor();
}

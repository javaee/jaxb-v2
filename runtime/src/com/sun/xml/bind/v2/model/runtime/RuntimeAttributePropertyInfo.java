package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeAttributePropertyInfo extends AttributePropertyInfo<Type,Class>, RuntimePropertyInfo {
    // refinement
    RuntimeNonElement getType();

    /**
     * Gets the {@link TransducedAccessor} for this property.
     *
     * <p>
     * Because an attribute property can be always parsed/printed from/to
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

package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;
import java.util.Collection;

import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * {@link PropertyInfo} that exposes more information.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface RuntimePropertyInfo extends PropertyInfo<Type,Class> {

    /**
     * Gets the raw type.
     *
     * TODO: we need to clarify those semantics.
     */
    Type getRawType();

    /** {@inheritDoc} */
    Collection<? extends RuntimeTypeInfo> ref();


    /**
     * Gets the {@link Accessor} for this property.
     *
     * <p>
     * Even for a multi-value property, this method returns an accessor
     * to that property. IOW, the accessor works against the raw type.
     *
     * <p>
     * This methods returns unoptimized accessor (because optimization
     * accessors are often combined into bigger pieces, and optimization
     * generally works better if you can look at a bigger piece, as opposed
     * to individually optimize a smaller components)
     *
     * @return
     *      never null.
     *
     * @see Accessor#optimize()
     */
    Accessor getAccessor();

    /**
     * Returns true if this property has an element-only content. False otherwise.
     */
    public boolean elementOnlyContent();
}

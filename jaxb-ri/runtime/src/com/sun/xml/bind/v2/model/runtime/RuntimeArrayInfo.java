package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeArrayInfo extends ArrayInfo<Type,Class>, RuntimeNonElement {
    /**
     * Represents <tt>T[]</tt>.
     *
     * The same as {@link TypeInfo#getType()} but at the runtime, an array
     * is guaranteed to have a {@link Class} representation, not just any {@link Type}.
     */
    Class getType();

    /**
     * {@inheritDoc}
     */
    RuntimeNonElement getItemType();
}

package com.sun.tools.txw2.model.prop;

import com.sun.codemodel.JType;

/**
 * @author Kohsuke Kawaguchi
 */
public class ValueProp extends Prop {
    private final JType type;

    public ValueProp(JType type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ValueProp)) return false;

        final ValueProp that = (ValueProp) o;

        return type.equals(that.type);
    }

    public int hashCode() {
        return type.hashCode();
    }
}

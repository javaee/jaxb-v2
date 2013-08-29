package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

final class NullNameClass extends NameClass {
    protected NullNameClass() {
    }

    public boolean contains(QName name) {
        return false;
    }

    public int containsSpecificity(QName name) {
        return SPECIFICITY_NONE;
    }

    public int hashCode() {
        return NullNameClass.class.hashCode();
    }

    public boolean equals(Object obj) {
        return this==obj;
    }

    public <V> V accept(NameClassVisitor<V> visitor) {
        return visitor.visitNull();
    }

    public boolean isOpen() {
        return false;
    }

    private Object readResolve() {
        return NameClass.NULL;
    }
}

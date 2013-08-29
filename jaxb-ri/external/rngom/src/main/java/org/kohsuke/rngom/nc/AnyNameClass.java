package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

final class AnyNameClass extends NameClass {
    
    protected AnyNameClass() {} // no instanciation
    
    public boolean contains(QName name) {
        return true;
    }

    public int containsSpecificity(QName name) {
        return SPECIFICITY_ANY_NAME;
    }

    public boolean equals(Object obj) {
        return obj==this;
    }

    public int hashCode() {
        return AnyNameClass.class.hashCode();
    }

    public <V> V accept(NameClassVisitor<V> visitor) {
        return visitor.visitAnyName();
    }

    public boolean isOpen() {
        return true;
    }
    
    private static Object readReplace() {
        return NameClass.ANY;
    }
}

package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class AnyNameExceptNameClass extends NameClass {

    private final NameClass nameClass;

    public AnyNameExceptNameClass(NameClass nameClass) {
        this.nameClass = nameClass;
    }

    public boolean contains(QName name) {
        return !nameClass.contains(name);
    }

    public int containsSpecificity(QName name) {
        return contains(name) ? SPECIFICITY_ANY_NAME : SPECIFICITY_NONE;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AnyNameExceptNameClass))
            return false;
        return nameClass.equals(((AnyNameExceptNameClass) obj).nameClass);
    }

    public int hashCode() {
        return ~nameClass.hashCode();
    }

    public <V> V accept(NameClassVisitor<V> visitor) {
        return visitor.visitAnyNameExcept(nameClass);
    }

    public boolean isOpen() {
        return true;
    }
}

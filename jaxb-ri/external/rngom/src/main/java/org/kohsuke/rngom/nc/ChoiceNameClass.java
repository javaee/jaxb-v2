package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class ChoiceNameClass extends NameClass {

    private final NameClass nameClass1;
    private final NameClass nameClass2;

    public ChoiceNameClass(NameClass nameClass1, NameClass nameClass2) {
        this.nameClass1 = nameClass1;
        this.nameClass2 = nameClass2;
    }

    public boolean contains(QName name) {
        return (nameClass1.contains(name) || nameClass2.contains(name));
    }

    public int containsSpecificity(QName name) {
        return Math.max(
            nameClass1.containsSpecificity(name),
            nameClass2.containsSpecificity(name));
    }

    public int hashCode() {
        return nameClass1.hashCode() ^ nameClass2.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChoiceNameClass))
            return false;
        ChoiceNameClass other = (ChoiceNameClass) obj;
        return (
            nameClass1.equals(other.nameClass1)
                && nameClass2.equals(other.nameClass2));
    }

    public <V> V accept(NameClassVisitor<V> visitor) {
        return visitor.visitChoice(nameClass1, nameClass2);
    }

    public boolean isOpen() {
        return nameClass1.isOpen() || nameClass2.isOpen();
    }
}

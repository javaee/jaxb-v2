package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class SimpleNameClass extends NameClass {

    public final QName name;

    public SimpleNameClass(QName name) {
        this.name = name;
    }
    
    public SimpleNameClass( String nsUri, String localPart ) {
        this( new QName(nsUri,localPart) );
    }

    public boolean contains(QName name) {
        return this.name.equals(name);
    }

    public int containsSpecificity(QName name) {
        return contains(name) ? SPECIFICITY_NAME : SPECIFICITY_NONE;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SimpleNameClass))
            return false;
        SimpleNameClass other = (SimpleNameClass) obj;
        return name.equals(other.name);
    }

    public <V> V accept(NameClassVisitor<V> visitor) {
        return visitor.visitName(name);
    }

    public boolean isOpen() {
        return false;
    }
}

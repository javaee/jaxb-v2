package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class NsNameExceptNameClass extends NameClass {

  private final NameClass nameClass;
  private final String namespaceURI;

  public NsNameExceptNameClass(String namespaceURI, NameClass nameClass) {
    this.namespaceURI = namespaceURI;
    this.nameClass = nameClass;
  }

  public boolean contains(QName name) {
    return (this.namespaceURI.equals(name.getNamespaceURI())
	    && !nameClass.contains(name));
  }

  public int containsSpecificity(QName name) {
    return contains(name) ? SPECIFICITY_NS_NAME : SPECIFICITY_NONE;
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NsNameExceptNameClass))
      return false;
    NsNameExceptNameClass other = (NsNameExceptNameClass)obj;
    return (namespaceURI.equals(other.namespaceURI)
	    && nameClass.equals(other.nameClass));
  }

  public int hashCode() {
    return namespaceURI.hashCode() ^ nameClass.hashCode();
  }

  public <V> V accept(NameClassVisitor<V> visitor) {
    return visitor.visitNsNameExcept(namespaceURI, nameClass);
  }

  public boolean isOpen() {
    return true;
  }
}


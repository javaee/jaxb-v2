package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public final class NsNameClass extends NameClass {

  private final String namespaceUri;

  public NsNameClass(String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }

  public boolean contains(QName name) {
    return this.namespaceUri.equals(name.getNamespaceURI());
  }

  public int containsSpecificity(QName name) {
    return contains(name) ? SPECIFICITY_NS_NAME : SPECIFICITY_NONE;
  }

  public int hashCode() {
    return namespaceUri.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NsNameClass))
      return false;
    return namespaceUri.equals(((NsNameClass)obj).namespaceUri);
  }

  public <V> V accept(NameClassVisitor<V> visitor) {
    return visitor.visitNsName(namespaceUri);
  }

  public boolean isOpen() {
    return true;
  }
}

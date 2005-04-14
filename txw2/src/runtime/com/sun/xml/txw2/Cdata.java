package com.sun.xml.txw2;

/**
 * CDATA section.
 *
 * @author Kohsuke Kawaguchi
 */
final class Cdata extends Text {
    Cdata(Document document, NamespaceResolver nsResolver, Object obj) {
        super(document, nsResolver, obj);
    }

    void accept(ContentVisitor visitor) {
        visitor.onCdata(buffer);
    }
}

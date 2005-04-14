package com.sun.xml.txw2;

/**
 * @author Kohsuke Kawaguchi
 */
final class EndTag extends Content {
    boolean concludesPendingStartTag() {
        return true;
    }

    void accept(ContentVisitor visitor) {
        visitor.onEndTag();
    }
}

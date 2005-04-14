package com.sun.xml.txw2;

/**
 * Visits {@link Content}.
 *
 * @author Kohsuke Kawaguchi
 */
interface ContentVisitor {
    void onStartDocument();

    void onEndDocument();

    void onEndTag();

    void onPcdata(StringBuilder buffer);

    void onCdata(StringBuilder buffer);

    void onStartTag(String nsUri, String localName, Attribute attributes, NamespaceDecl namespaces);

    void onComment(StringBuilder buffer);
}

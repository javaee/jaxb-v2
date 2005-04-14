package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.visitor.XSVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi
 */
public class XPathImpl extends ComponentImpl implements XSXPath {
    private XSIdentityConstraint parent;
    private final String xpath;
    private final ValidationContext context;

    public XPathImpl(SchemaImpl _owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl fa, String xpath, ValidationContext context) {
        super(_owner, _annon, _loc, fa);
        this.xpath = xpath;
        this.context = context;
    }

    public void setParent(XSIdentityConstraint parent) {
        this.parent = parent;
    }

    public XSIdentityConstraint getParent() {
        return parent;
    }

    public String getXPath() {
        return xpath;
    }

    public ValidationContext getContext() {
        return context;
    }

    public void visit(XSVisitor visitor) {
        visitor.xpath(this);
    }

    public <T> T apply(XSFunction<T> function) {
        return function.xpath(this);
    }
}

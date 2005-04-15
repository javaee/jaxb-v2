package com.sun.tools.xjc.util;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.relaxng.datatype.ValidationContext;

/**
 * @author Kohsuke Kawaguchi
 */
public class NamespaceContextAdapter implements NamespaceContext {
    private ValidationContext context;

    public NamespaceContextAdapter(ValidationContext context) {
        this.context = context;
    }

    public String getNamespaceURI(String prefix) {
        return context.resolveNamespacePrefix(prefix);
    }

    public String getPrefix(String namespaceURI) {
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        return Collections.EMPTY_LIST.iterator();
    }
}

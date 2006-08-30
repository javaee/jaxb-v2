package com.sun.xml.xsom;

import com.sun.xml.xsom.impl.scd.ParseException;
import com.sun.xml.xsom.impl.scd.SCDImpl;
import com.sun.xml.xsom.impl.scd.SCDParser;

import javax.xml.namespace.NamespaceContext;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Schema Component Designator (SCD).
 *
 * <p>
 * SCD for schema is what XPath is for XML. SCD allows you to select a schema component(s)
 * from a schema component(s).
 *
 * <p>
 * See <a href="http://www.w3.org/TR/xmlschema-ref/">XML Schema: Component Designators</a>.
 * This implementation is based on 03/29/2005 working draft.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class SCD {
    /**
     * Parses the string representation of SCD.
     *
     * @param path
     *      the string representation of SCD, such as "/foo/bar".
     * @param nsContext
     *      Its {@link NamespaceContext#getNamespaceURI(String)} is used
     *      to resolve prefixes in the SCD to the namespace URI.
     */
    public static SCD create(String path, NamespaceContext nsContext) throws java.text.ParseException {
        try {
            SCDParser p = new SCDParser(new StringReader(path));
            return new SCDImpl(p.RelativeSchemaComponentPath());
        } catch (ParseException e) {
            // TODO: copy more info
            // throw new java.text.ParseException(e.getMessage(), e.currentToken.beginColumn );
            throw new RuntimeException(e);
        }
    }

    /**
     * Evaluates the SCD against the given context node and
     * returns the matched nodes.
     *
     * @return
     *      could be empty but never be null.
     */
    public final List<XSComponent> select(XSComponent contextNode) {
        return select(Collections.singletonList(contextNode));
    }

    public final List<XSComponent> select(XSSchemaSet contextNode) {
        return select(new ArrayList<XSComponent>(contextNode.getSchemas()));
    }

    /**
     * Evaluates the SCD against the given context node and
     * returns the matched node.
     *
     * @return
     *      null if the SCD didn't match anything. If the SCD matched more than one node,
     *      the first one will be returned.
     */
    public final XSComponent selectSingleNode(XSComponent contextNode) {
        List<XSComponent> r = select(contextNode);
        if(r.isEmpty())     return null;
        return r.get(0);
    }

    /**
     * Evaluates the SCD against the given set of context nodes and
     * returns the matched nodes.
     *
     * @return
     *      could be empty but never be null.
     */
    public abstract List<XSComponent> select(List<XSComponent> contextNode);
}

package com.sun.tools.txw2;

import com.sun.tools.txw2.model.NodeSet;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.xml.sax.SAXException;

/**
 * Encapsulation of the schema file and the builder.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface SchemaBuilder {
    NodeSet build(TxwOptions options) throws IllegalSchemaException, SAXException;
}

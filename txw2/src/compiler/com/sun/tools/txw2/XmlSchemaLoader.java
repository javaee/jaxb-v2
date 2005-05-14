package com.sun.tools.txw2;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.sun.tools.txw2.model.NodeSet;
import com.sun.tools.txw2.builder.xsd.XmlSchemaBuilder;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * @author Kohsuke Kawaguchi
 */
class XmlSchemaLoader implements SchemaBuilder {
    private final InputSource in;

    public XmlSchemaLoader(InputSource in) {
        this.in = in;
    }

    public NodeSet build(TxwOptions options) throws SAXException {
        XSOMParser xsom = new XSOMParser();
        xsom.parse(in);
        return XmlSchemaBuilder.build(xsom.getResult(),options);
    }
}

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

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

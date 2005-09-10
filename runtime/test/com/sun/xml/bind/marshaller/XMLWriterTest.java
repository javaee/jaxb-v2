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

package com.sun.xml.bind.marshaller;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Manual test of XMLWriter.
 */
public class XMLWriterTest extends TestCase {
    
    public XMLWriterTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(XMLWriterTest.class);
    }

    public void testBasicOps() throws Exception {
        StringWriter sw = new StringWriter();
        XMLWriter w = new XMLWriter(sw, "US-ASCII", DumbEscapeHandler.theInstance );
        w.startDocument();
        w.startElement("","root","root",new AttributesImpl());
        
        w.startPrefixMapping("ns1","aaa");
        w.startPrefixMapping("ns2","bbb");
        w.startPrefixMapping("ns3","ccc");
        
        w.startElement("ccc","child","ns3:child",new AttributesImpl());
        w.endElement("ccc","child","ns3:child");

        w.endPrefixMapping("ns2");
        w.endPrefixMapping("ns1");
        
        w.endElement("","root","root");
        w.endDocument();
        
        checkWellformedness(sw.toString());
    }
    
    // test if the empty tag optimization is happening.
    public void testEmptyTag() throws Exception { 
        StringWriter sw = new StringWriter();
        XMLWriter w = new XMLWriter(sw, "US-ASCII", DumbEscapeHandler.theInstance );
        w.startDocument();
        w.startElement("","root","root",new AttributesImpl());
        w.startElement("","child","child",new AttributesImpl());
        w.endElement("","child","child");
        w.startElement("","kid","kid",new AttributesImpl());
        w.endElement("","kid","kid");
        w.endElement("","root","root");
        w.endDocument();

        String body = sw.toString();
        
        checkWellformedness(body);
        
        body = body.substring(body.indexOf('\n')+1);
        assertEquals( "<root><child/><kid/></root>\n\n", body);
        
    }

    /** Checks the well-formedness of XML. */
    private void checkWellformedness(String xml) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.newSAXParser().getXMLReader().parse(
            new InputSource(new StringReader(xml))); 
    }
}

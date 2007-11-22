/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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

        // cut XML header
        body = body.substring(body.indexOf("?>")+2);
        assertEquals( "<root><child/><kid/></root>", body);
        
    }

    /** Checks the well-formedness of XML. */
    private void checkWellformedness(String xml) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.newSAXParser().getXMLReader().parse(
            new InputSource(new StringReader(xml))); 
    }
}

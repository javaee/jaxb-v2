/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.io.OutputStreamWriter;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFactory;
import org.jdom.input.SAXHandler;
import org.vmguys.vmtools.utils.CostOps;
import org.vmguys.vmtools.utils.DifferenceFinder;
import org.xml.sax.InputSource;



/**
 * Tests SAXMarshaller.
 * 
 * This is a typical example of using JUnit for testing single class.
 */
public class SAXMarshallerTest
{
    /**
     * This main method allows this test to be runned by itself
     */
    public static void main( String[] args ) throws Exception {
        junit.textui.TestRunner.run(suite());
    }
    
    
    private static java.io.InputStream getStream( String name ) {
        return SAXMarshallerTest.class.getResourceAsStream(name);
    }
    
    /**
     * This static suite method allows this test to be composed into
     * a larger test.
     */
	public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        
        // validate test file
        VerifierFactory vfac =
            VerifierFactory.newInstance("http://relaxng.org/ns/structure/1.0");
        Verifier verifier =
            vfac.newVerifier(getStream("SAXMarshallerTest.rng"));
        if(!verifier.verify(new InputSource(getStream("SAXMarshallerTest.xml"))))
            Assert.fail("syntax error in SAXMarshallerTest.xml");
        
        SAXReader sr = new SAXReader();
        Document doc = sr.read(getStream("SAXMarshallerTest.xml"));
        
        Iterator itr = doc.getRootElement().elementIterator("test");
        while(itr.hasNext())
            suite.addTest( buildTest((Element)itr.next()) );
        
        return suite;
	}
    
    public static Test buildTest( final Element test ) {
        return new TestCase( test.getUniquePath() ) {
            public void runTest() throws Exception {
                
                // set up the pipe line to
                // context -> XMLWriter -> JDOM SAXHandler
                XMLWriter dw = new XMLWriter(new OutputStreamWriter(System.out),null,
                    DumbEscapeHandler.theInstance);
                SAXHandler sh = new SAXHandler();
                dw.setContentHandler(sh);
                SAXMarshaller ctxt = new SAXMarshaller(dw,null);
                
                
                dw.startDocument();
                // input sequence
                Iterator inputs = test.element("input").elementIterator();
                while(inputs.hasNext()) {
                    Element command = (Element)inputs.next();
                    String name = command.getName();
//                    System.out.println(name);
                    
                    if(name.equals("startElement")) {
                        ctxt.startElement(
                            command.attributeValue("uri",""/*default*/),
                            command.attributeValue("local"));
                        continue;
                    }
                    if(name.equals("endElement")) {
                        ctxt.endElement();
                        continue;
                    }
                    if(name.equals("endAttributes")) {
                        ctxt.endAttributes();
                        continue;
                    }
                    if(name.equals("startAttribute")) {
                        ctxt.startAttribute(
                            command.attributeValue("uri",""/*default*/),
                            command.attributeValue("local"));
                        continue;
                    }
                    if(name.equals("endAttribute")) {
                        ctxt.endAttribute();
                        continue;
                    }
                    if(name.equals("text")) {
                        ctxt.text(command.getText());
                        continue;
                    }
                    fail("unknown command:"+command.getUniquePath());
                }
                dw.endDocument();
                
                // get the expected answer in terms of JDOM tree
                SAXHandler answer = new SAXHandler();
                new org.dom4j.io.SAXWriter(answer).write(
                    (Element)test.element("output").elements().get(0));
                
                // compare the difference
                CostOps diff = new DifferenceFinder().findDifferences(
                    // generated XML by MarshallingContext
                    sh.getDocument().getRootElement(),
                    // expected answer
                    answer.getDocument().getRootElement() );
                
                // dump the difference
                Iterator itr = diff.getOps().iterator();
                while(itr.hasNext())
                    System.out.println(itr.next());
                
                assertTrue(diff.getOps().size()==0);
            }
        };
    }
}

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

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import primer.PurchaseOrderType;

/*
 * @(#)$Id: Main.java,v 1.2 2005-09-10 19:08:22 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

public class Main {
    public static void main( String[] args ) throws Exception {
        
        // create JAXBContext for the primer.xsd
        JAXBContext context = JAXBContext.newInstance("primer");
        
        // unmarshal a document, just to marshal it back again.
        JAXBElement poe = (JAXBElement)context.createUnmarshaller().unmarshal(
            new File(args[0]));
        // we don't need to check the return value, because the unmarshal
        // method should haven thrown an exception if anything went wrong.
	PurchaseOrderType po = (PurchaseOrderType)poe.getValue();
        
        
        // Here's the real meat.
        // we configure marshaller not to print out xml decl,
        // we then print out XML decl plus stylesheet header on our own,
        // then have the marshaller print the real meat.
        
        System.out.println("<?xml version='1.0'?>");
        System.out.println("<?xml-stylesheet type='text/xsl' href='foobar.xsl' ?>");
        // if you need to put DOCTYPE decl, it can be easily done here.
        
        // create JAXB marshaller.
        Marshaller m = context.createMarshaller();
        // configure it
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        // marshal
        m.marshal(poe,System.out);
    }

}

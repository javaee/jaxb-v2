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
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

import simple.*;

/*
 * @(#)$Id: Main.java,v 1.2 2005-09-10 19:08:00 kohsuke Exp $
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
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        ObjectFactory of = new ObjectFactory();
        
        // \u00F6 is o with diaeresis
        JAXBElement<String> e = of.createE("G\u00F6del & his friends");
        
        
        // set up a normal marshaller
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty( "jaxb.encoding", "US-ASCII" );
        
        // check out the console output
        marshaller.marshal( e, System.out );
        
        
        // set up a marshaller with a custom character encoding handler
        marshaller = context.createMarshaller();
        marshaller.setProperty( "jaxb.encoding", "US-ASCII" );
        marshaller.setProperty(
          "com.sun.xml.bind.characterEscapeHandler",
          new CustomCharacterEscapeHandler() );
        
        // check out the console output
        marshaller.marshal( e, System.out );
    }

}

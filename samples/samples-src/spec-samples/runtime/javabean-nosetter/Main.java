/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 *  This example is to show the unmarshalling of a Javabean with setters
 *  but no getters. 
 * 
 *  Author: Sekhar Vajjhala
 *
 *  $Id: Main.java,v 1.1 2005-04-15 20:07:06 kohsuke Exp $
 */  

/**
 * OBSERVATIONS:
 *
 * 1. The Javabean USAddress contains getters but not setters. I
 *    first thought that an unmarshal call would fail. However, it
 *    succeeded. Then I remembered that JAXBRI is unmarshalling using
 *    fields. However this may not be clear to a newbie JAXB 2.0
 *    user and is therefore worth clarifying.
 *
 * 2. If AccessType.PROPERTY is the default, then the above
 *     unmarshalling would be expected to fail (probably an
 *    exception).
 *
 * 3. An attempt to unmarshal into a JAXBElement instance resulted in
 *    a ClassCastException. 
 *        Exception in thread "main" java.lang.ClassCastException: USAddress
 * 	 at Main.main(Main.java:36)
 * 
 *    Note the way I hit this problem was that I copied the
 *    unmarshal-read sample where the the unmarshal call was cast
 *    to JAXBElement instance. However, I chose to use the
 *    @XmlRootElement on USAddress but forgot to cast the return type
 *    of the Unmarshaller.unmarshal to USAddress.
 *    
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Main {
    
    public static void main( String[] args ) {
        try {
            /**
             * Create a JAXBContext passing it a list of classes to be
             * marshalled.
             */
            JAXBContext jc = JAXBContext.newInstance(USAddress.class);

            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();

            // unmarshal a USAddress instance annotated with XmlRootElement.
            // JAXBElement<?> addrElement = (JAXBElement<?>)u.unmarshal( new FileInputStream( "po.xml" ) );
            // USAddress usaddr = (USAddress) addrElement.getValue();
            USAddress usaddr = (USAddress) u.unmarshal( new FileInputStream( "po.xml" ) );

            displayAddress(usaddr);
            System.out.println("");
         } catch( JAXBException je ) {
            je.printStackTrace();
         } catch( IOException ioe ) {
            ioe.printStackTrace();
        }
    }

    public static void displayAddress( USAddress address ) {
        // display the address
        System.out.println( "\t" + address.getName() );
        System.out.println( "\t" + address.getStreet() ); 
    }
}    

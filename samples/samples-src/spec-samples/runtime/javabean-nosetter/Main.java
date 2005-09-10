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

/**
 *  This example is to show the unmarshalling of a Javabean with setters
 *  but no getters. 
 * 
 *  Author: Sekhar Vajjhala
 *
 *  $Id: Main.java,v 1.2 2005-09-10 19:08:14 kohsuke Exp $
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

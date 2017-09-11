/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

/**
 *  This example is to show the unmarshalling of a Javabean with setters
 *  but no getters. 
 * 
 *  Author: Sekhar Vajjhala
 *
 *  $Id: Main.java,v 1.2 2009-11-11 14:17:30 pavel_bucek Exp $
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

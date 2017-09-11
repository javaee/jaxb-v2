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
 *  Author: Sekhar Vajjhala
 *
 *  $Id: Main.java,v 1.1 2007-12-05 00:49:36 kohsuke Exp $
 */  

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class Main {
    
    public static void main( String[] args ) {
        try {
            /**
             * Create a JAXBContext passing it a list of classes to be
             * marshalled.
             */
            JAXBContext jc = JAXBContext.newInstance(PurchaseOrder.class, USAddress.class, Address.class);
            
            // create a USAddress 
            USAddress addr = new USAddress();
            addr.name   = "Alice Smith";
            addr.street = "123 Maple Street";
            addr.city   = "Mill Valley";
            addr.state  = "CA";
            addr.zip    = 90952;

            // create a po type;
            PurchaseOrder po = new PurchaseOrder();
            po.shipTo = addr;

            // create a Marshaller
            Marshaller m = jc.createMarshaller();
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );

            // marshall the address
            System.out.println(".... Marshal USAddress ....");
            m.marshal(addr, System.out );
            /**
             * OBSERVATIONS:
             * a. the following print statement needed since Marshaller
             *    output does not contain a terminating new line
             *    character.
             */
            System.out.println("");

            /**
             * Marshall the purchase order. 
             *
             * OBSERVATIONS:
             * a. Initially, I forgot to add the PurchaseOrder to
             *    the list of classes in JAXBContext. So, when passed
             *    to the marshal method, this resulted in an
             *    exception.
             *         Exception in thread "main"
             *             java.lang.IllegalArgumentException: PurchaseOrder nor
             *             any of its super class is known to this context
             *
             *    I knew "not being known to this context" means that
             *    the class has to be specified in the
             *    JAXBContext. Would a newbie JAXB 2.0 be able to
             *    figure that out ?
             *
             * b. If a type is not known at runtime, then an
             *    alternative to throwing an exception is to
             *    use reflection to marshal the type. Castor uses this
             *    approach. 
             */
	    
            System.out.println(".... Marshal PurchaseOrder (USAddress type with xsi type)....");
            m.marshal(po, System.out );
            // following print statement needed to insert a new line character.
            System.out.println("");
         } catch( JAXBException je ) {
            je.printStackTrace();
         }
    }
}    

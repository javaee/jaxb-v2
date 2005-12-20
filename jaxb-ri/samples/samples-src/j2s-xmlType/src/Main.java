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

/*
 * $Id: Main.java,v 1.1 2005-12-20 15:15:34 rebeccas Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


import java.io.File;
import java.io.FileOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import address.USAddress;
import address.PurchaseOrderType;

public class Main {
    public static void main(String[] args) throws Exception {

        // Demonstates shipping and billing data printed in the property
        // order defined by the propOrder annotation element in class 
        // USAddress.        
        JAXBContext jc = JAXBContext.newInstance(PurchaseOrderType.class);
        Unmarshaller u = jc.createUnmarshaller();
        try {
            PurchaseOrderType poType = (PurchaseOrderType)u.unmarshal(new
            File("src/testData.xml"));
            System.out.println(poType.toString());
        } catch(javax.xml.bind.UnmarshalException e){
            System.out.println("Main: " + e);
        }    
    }
}


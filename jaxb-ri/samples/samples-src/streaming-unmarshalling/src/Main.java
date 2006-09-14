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

import org.xml.sax.XMLReader;
import primer.PurchaseOrderType;
import primer.PurchaseOrders;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

/*
 * @(#)$Id: Main.java,v 1.1.2.1 2006-09-14 19:16:12 kohsuke Exp $
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

        Unmarshaller unmarshaller = context.createUnmarshaller();

        // purchase order notification callback
        final PurchaseOrders.Listener orderListener = new PurchaseOrders.Listener() {
            public void handlePurchaseOrder(PurchaseOrders purchaseOrders, PurchaseOrderType purchaseOrder) {
                System.out.println("this order will be shipped to "
                        + purchaseOrder.getShipTo().getName());
            }
        };

        // install the callback on all PurchaseOrders instances
        unmarshaller.setListener(new Unmarshaller.Listener() {
            public void beforeUnmarshal(Object target, Object parent) {
                if(target instanceof PurchaseOrders) {
                    ((PurchaseOrders)target).setPurchaseOrderListener(orderListener);
                }
            }

            public void afterUnmarshal(Object target, Object parent) {
                if(target instanceof PurchaseOrders) {
                    ((PurchaseOrders)target).setPurchaseOrderListener(null);
                }
            }
        });

        // create a new XML parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XMLReader reader = factory.newSAXParser().getXMLReader();
        reader.setContentHandler(unmarshaller.getUnmarshallerHandler());

        for (String arg : args) {
            // parse all the documents specified via the command line.
            // note that XMLReader expects an URL, not a file name.
            // so we need conversion.
            reader.parse(new File(arg).toURI().toString());
        }
    }
}

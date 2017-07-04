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

package samples.ubl.report;

import samples.ubl.report.facade.OrderFacade;
import samples.ubl.report.facade.OrderLineTypeFacade;
import samples.ubl.report.facade.AddressFacade;

import java.io.FileInputStream;
import java.io.IOException;

import java.text.NumberFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.oasis.ubl.order.OrderType;

/*
 * $Id: PrintOrder.java,v 1.1 2007-12-05 00:49:45 kohsuke Exp $
 */
 
/**
 * Unmarshals a UBL order instance and prints some of its data as
 * text to the standard output.
 *
 * @author <a href="mailto:Ed.Mooney@Sun.COM">Ed Mooney</a>
 * @version 1.0
 */
public class PrintOrder {

    /**
     * Unmarshals <code>xml/OfficeSupplyOrderInstance.xml</code>,
     * computes subtotals for each line item, and prints results to the
     * standard output.
     *
     * @param args Ignored.
     */
    public static void main(String[] args) {
        try {
            JAXBContext jc =
                JAXBContext.newInstance("org.oasis.ubl.order:"
                                        + "org.oasis.ubl.commonaggregatecomponents");
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement orderElement =
                (JAXBElement)u.unmarshal(new
                                    FileInputStream("cd-UBL-1.0/xml/office/"
                                                    + "UBL-Order-1.0-Office-Example.xml"));

	    OrderType order = (OrderType)orderElement.getValue(); 
            OrderFacade of = new OrderFacade(order);

            printLetterHead(of);
            printDate(of);
            printBuyer(of);
            printLineItems(of);
        } catch (JAXBException e) {
            e.printStackTrace(System.out);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } // end of try-catch
    }

    /**
     * Prints information about the Seller.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printLetterHead(OrderFacade order) {
        AddressFacade addr = order.getSellerAddress();
        System.out.println("         "
                           + order.getSellerName()
                           + "\n         "
                           + addr.getStreet()
                           + "\n         "
                           + addr.getCity()
                           + ", "
                           + addr.getState()
                           + "  "
                           + addr.getZip());
    }

    /**
     * Prints the issue date in <code>java.text.DateFormat.LONG</code> format.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printDate(OrderFacade order) {
        System.out.println("\nDate: "
                           + order.getLongDate());
    }

    /**
     * Prints information about the Buyer.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printBuyer(OrderFacade order) {
        AddressFacade addr = order.getBuyerAddress();
        System.out.println("\nSold To: "
                           + order.getBuyerContact()
                           + "\n         c/o "
                           + order.getBuyerName()
                           + "\n         "
                           + addr.getStreet()
                           + "\n         "
                           + addr.getCity()
                           + ", "
                           + addr.getState()
                           + "  "
                           + addr.getZip());
    }

    /**
     * Prints information about line items in this order, including extension
     * based on quantity and base price and a total of all extensions.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printLineItems(OrderFacade order) {
        double total = 0;
        NumberFormat form = NumberFormat.getCurrencyInstance();

        java.util.Iterator iter = order.getLineItemIter();
        for (int i = 0; iter.hasNext(); i++) {
            OrderLineTypeFacade lineItem = (OrderLineTypeFacade) iter.next();

            // Compute subtotal and total
            double price = lineItem.getItemPrice();
            int qty = lineItem.getItemQuantity();
            double subtotal = qty * price;
            total += subtotal;

	    form.setCurrency(lineItem.getItemPriceCurrency());
            System.out.println("\n"
                               + (i + 1)
                               + ". Part No.: "
                               + lineItem.getItemPartNumber()
                               + "\n   Description: "
                               + lineItem.getItemDescription()
                               + "\n   Price: "
                               + form.format(price)
                               + "\n   Qty.: "
                               + qty
                               + "\n   Subtotal: "
                               + form.format(subtotal));
        }
        System.out.println("\nTotal: " + form.format(total));
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
 
package samples.ubl.report.facade;

import org.oasis.ubl.commonaggregatecomponents.OrderLineType;
import org.oasis.ubl.commonaggregatecomponents.BasePriceType;

/**
 * The <code>OrderLineTypeFacade</code> class provides a set of read-only
 * methods for accessing data in a UBL order line.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class OrderLineTypeFacade {
    private OrderLineType lineItem;
  
    /**
     * Creates a new <code>OrderLineTypeFacade</code> instance.
     *
     * @param olt an <code>OrderLineType</code> value
     */
    public OrderLineTypeFacade(OrderLineType olt) {
        lineItem = olt;
    }

    /**
     * Returns the part number associated with a line item.
     *
     * @return a <code>String</code> representing the part number for this line
     * item
     */
    public String getItemPartNumber() {
        String num = "";
        try {
            num = lineItem.getLineItem().getItem().getSellersItemIdentification().getID().getValue();
        } catch (NullPointerException npe) {
        }
        return num;
    }

    /**
     * Returns the description associated with a line item.
     *
     * @return a <code>String</code> representing the description of this line item
     */
    public String getItemDescription() {
        String descr = "";
        try {
            descr = lineItem.getLineItem().getItem().getDescription().getValue();
        } catch (NullPointerException npe){
        }
        return descr;
    }

    /**
     * Returns the price associated with a line item.
     *
     * @return a <code>double</code> representing the price of this line item
     */
    public double getItemPrice() {
        double price = 0.0;
        try {
            price = getTheItemPrice().getPriceAmount().getValue().doubleValue();
        } catch (NullPointerException npe){
        }
        return price;
    }

    /**
     * Returns the currency of the price associated with a line item.
     *
     * <p>
     * Both java.util.Currency and UBL currency IDs follow
     * ISO 4217 currency codes.
     *
     * @return Currency of price
     */
    public java.util.Currency getItemPriceCurrency() {
	return java.util.Currency.getInstance(getTheItemPrice().getPriceAmount().getAmountCurrencyID());
    }

    /**
     * Returns the quantity associated with a line item.
     *
     * @return an <code>int</code> representing the quantity of this line item
     */
    public int getItemQuantity() {
        int quantity = 0;
        try {
            quantity = lineItem.getLineItem().getQuantity().getValue().intValue();
        } catch (NullPointerException npe){
        }
        return quantity;
    }

    /**
     * Returns the <code>BasePriceType</code> associated with a line item
     *
     * @return a <code>BasePriceType</code> representing the price of this item
     */
    private BasePriceType getTheItemPrice() {
        return (BasePriceType) lineItem.getLineItem().getItem().getBasePrice().get(0);
    }

    static public class Iterator implements java.util.Iterator {
        java.util.Iterator iter;
        
        /** List of OrderLineType */
        public Iterator(java.util.List lst) {
            iter = lst.iterator();
        }
       
        public Object next() {
            return new OrderLineTypeFacade((OrderLineType)iter.next());
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

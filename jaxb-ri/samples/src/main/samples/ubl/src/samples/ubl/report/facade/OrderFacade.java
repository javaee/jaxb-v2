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

package samples.ubl.report.facade;

import java.text.DateFormat;

import java.util.GregorianCalendar;
import java.util.Iterator;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasis.ubl.commonaggregatecomponents.BuyerPartyType;
import org.oasis.ubl.commonaggregatecomponents.PartyNameType;
import org.oasis.ubl.commonaggregatecomponents.SellerPartyType;
import org.oasis.ubl.commonbasiccomponents.NameType;

import org.oasis.ubl.order.OrderType;

/**
 * The <code>OrderFacade</code> class provides a set of read-only methhods for
 * accessing data in a UBL order.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class OrderFacade {

    OrderType order = null;

    /**
     * Creates a new <code>OrderFacade</code> instance.
     *
     * @param order an <code>Order</code> value
     */
    public OrderFacade(OrderType order) {
        this.order = order;
    }

    /**
     * Returns a <code>String</code> representing the name of a person familiar
     * with this order.
     *
     * @return a <code>String</code> value representing the name of a person
     * familiar with <code>OrderType</code>
     */
    public String getBuyerContact() {
        BuyerPartyType party = order.getBuyerParty();
        return ((NameType) party.getParty().getPartyName().getName().get(0)).getValue();
    }

    /**
     * Returns a <code>String</code> representing the name of the entity placing
     * this order.
     *
     * @return a <code>String</code> value representing the name of the entity
     * placing this order
     */
    public String getBuyerName() {
        BuyerPartyType party = order.getBuyerParty();
        return ((NameType) party.getParty().getPartyName().getName().get(0)).getValue();
     }

    /**
     * Returns the first <code>AddressFacade</code> in list order contained by
     * the <code>BuyerPartyType</code> representing the entity placing this
     * order.
     *
     * @return an <code>AddressFacade</code> value representing the address of
     * the entity placing this order
     */
    public AddressFacade getBuyerAddress() {
        return new AddressFacade(order.getBuyerParty().getParty().getAddress());
    }


    /**
     * Returns a <code>String</code> representing the name of the entity
     * fulfilling this order.
     *
     * @return a <code>String</code> value representing the name of the entity
     * fulfilling this order
     */
    public String getSellerName() {
        SellerPartyType party = order.getSellerParty();
        return ((NameType) party.getParty().getPartyName().getName().get(0)).getValue();
    }


    /**
     * Returns the first <code>PartyNameType</code> in list order contained by
     * the <code>SellerPartyType</code> representing the entity fulfilling this
     * order.
     *
     * @param seller a <code>SellerPartyType</code> representing the entity
     * fulfilling this order
     * @return a <code>PartyNameType</code> value representing the name of the
     * entity fulfilling this order
     */
    private PartyNameType getSellerParty(SellerPartyType seller) {
        return seller.getParty().getPartyName();
    }

    /**
     * Returns the first <code>AddressFacade</code> in list order contained by
     * the <code>SellerPartyType</code> representing the entity fulfilling this
     * order.
     *
     * @return an <code>AddressFacade</code> value representing the address of the
     * entity fulfilling this order
     */
    public AddressFacade getSellerAddress() {
        return new AddressFacade(order.getSellerParty().getParty().getAddress());
    }

    /**
     * Returns an UBL <code>OrderType</code> issue date in the <code>LONG</code>
     * format as defined by <code>java.text.DateFormat</code>.
     *
     * @return a <code>String</code> value representing the issue date of this
     * UBL <code>OrderType</code>
     */
    public String getLongDate() {
        DateFormat form = DateFormat.getDateInstance(DateFormat.LONG);
        GregorianCalendar cal = getCalendar().toGregorianCalendar();
        form.setTimeZone(cal.getTimeZone());
        return form.format(cal.getTime());
    }

    /**
     * Returns a <code>Calendar</code> representing the issue date of this UBL
     * <code>OrderType</code>.
     *
     * @return a <code>Calendar</code> representing the issue date of this UBL order
     */
    private XMLGregorianCalendar getCalendar() {
        XMLGregorianCalendar date = null;
        return order.getIssueDate() != null
            ? order.getIssueDate().getValue()
            : date;
    }

    /**
     * Returns an iterator over orders line items.
     *
     * @return an Iterator over OrderLineTypeFacade.
     */
    public Iterator getLineItemIter() {
        return new OrderLineTypeFacade.Iterator(order.getOrderLine());
    }
}

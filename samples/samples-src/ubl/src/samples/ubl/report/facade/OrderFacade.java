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

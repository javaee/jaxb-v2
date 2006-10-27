package address;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="purchaseOrder",namespace="http://www.example.com/MYPO1")
@XmlType(name="PurchaseOrderType")
public class PurchaseOrderType {

    public USAddress shipTo;
    public USAddress billTo;
    public CreditCardVendor creditCardVendor;
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Ship To: ");
        s.append(shipTo.toString()).append('\n');
        s.append("Bill To: ");
        return s.toString();
    }
}


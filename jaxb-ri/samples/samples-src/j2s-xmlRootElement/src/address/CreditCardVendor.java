package address;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Valid credit card vendor
 */
@XmlRootElement(name="ccv")
public enum CreditCardVendor{
    VISA,
    AMERICANEXPRESS,
    DISCOVER
}


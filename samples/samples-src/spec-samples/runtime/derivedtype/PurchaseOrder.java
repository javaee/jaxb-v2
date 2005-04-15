/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 *  Author: Sekhar Vajjhala
 *
 *  $Id: PurchaseOrder.java,v 1.1 2005-04-15 20:07:05 kohsuke Exp $
 */

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PurchaseOrder {

    /**
     * NOTE: Address is used instead of USAddress or UKAddress since
     * the intent of this sample is to demonstrate the use of xsi type
     */
    public Address shipTo;
    public Address billTo;
}


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

/**
 *  Author: Sekhar Vajjhala
 *
 *  $Id: PurchaseOrder.java,v 1.2 2005-09-10 19:08:14 kohsuke Exp $
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


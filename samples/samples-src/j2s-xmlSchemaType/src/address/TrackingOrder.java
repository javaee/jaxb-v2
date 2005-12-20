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
 * $Id: TrackingOrder.java,v 1.1 2005-12-20 15:13:04 rebeccas Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */


package address;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement
@XmlType(name="TrackingOrderType")
public class TrackingOrder {
  String trackingDuration;
  
  @XmlSchemaType(name="date")
  XMLGregorianCalendar shipDate;
  @XmlElement XMLGregorianCalendar orderDate;
  @XmlElement XMLGregorianCalendar deliveryDate;
    
  @XmlSchemaType(name="duration")
  public String getTrackingDuration(){
    return trackingDuration;
  }
  public void setTrackingDuration( String d){
    trackingDuration = d;
  }
}


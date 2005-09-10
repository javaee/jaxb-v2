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
 *  Illustrates the use of
 *  @javax.xml.bind.annotation.XmlType.propOrder() to customize the
 *  ordering of properties.
 *
 *  $Id: USAddress.java,v 1.2 2005-09-10 19:08:13 kohsuke Exp $
 * 
 *  Author: Sekhar Vajjhala
 */  

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"name", "street", "city", "state",  "zip"})
public class USAddress {
    private java.math.BigDecimal zip;
    private String name;
    private String street;
    private String city;
    private String state;


    String getName() {
	return name;
    };

    void setName(String name) {
	this.name = name;
    }
 
    String getStreet() {
	return street;
    }

    void setStreet(String street) {
	this.street = street;
    };

    String getCity() {
	return city;
    }; 

    void setCity(String city) {
	this.city = city;
    }

    String getState() {
	return state;
    }

    void setState(String state) {
	this.state = state;
    }

     java.math.BigDecimal getZip() {
	 return zip;
     }

     void setZip(java.math.BigDecimal zip) {
	 this.zip = zip;
     }
 }

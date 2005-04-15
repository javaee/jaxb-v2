/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 *  Illustrates the use of
 *  @javax.xml.bind.annotation.XmlType.propOrder() to customize the
 *  ordering of properties.
 *
 *  $Id: USAddress.java,v 1.1 2005-04-15 20:07:03 kohsuke Exp $
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

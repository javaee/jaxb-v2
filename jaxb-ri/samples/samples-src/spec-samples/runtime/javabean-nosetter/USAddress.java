/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 *  Author: Sekhar Vajjhala
 *
 *  $Id: USAddress.java,v 1.1 2005-04-15 20:07:07 kohsuke Exp $
 */  

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="usaddr")
public class USAddress {
    private String _name;
    private String _street;

    public String getName() {
        return _name;
    }

    public String getStreet() {
        return _street;
    }
}

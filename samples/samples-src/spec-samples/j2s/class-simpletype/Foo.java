/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 *  $Id: Foo.java,v 1.1 2005-04-15 20:07:04 kohsuke Exp $
 *  Author: Sekhar Vajjhala  
 */  

/**
 * Map a class with a single property that has been marked with
 * @XmlValue to simple schema type.
 */
import javax.xml.bind.annotation.XmlValue;
 
public class Foo {
    /**
     * @XmlValue can only be used on a property whose type (int in
     * this case) is mapped to a simple schema type.
     */
    @XmlValue 
    public int bar;
}

package org.acme;

import javax.xml.bind.annotation.XmlIDREF;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    // in this bean we bind fields, just to show that we can.

    public int a;
    public String b;

    /**
     * JAXB will inject the proper bean here as configured in XML.
     */
    @XmlIDREF
    public Object c;

    public String toString() {
        return "Foo[a="+a+",b="+b+",c="+c+"]";
    }
}

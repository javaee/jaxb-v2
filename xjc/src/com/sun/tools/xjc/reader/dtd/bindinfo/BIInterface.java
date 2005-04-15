/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

/**
 * &lt;interface> declaration in the binding file.
 */
public final class BIInterface
{
    BIInterface( Element e ) {
        this.dom = e;
        name = DOMUtil.getAttribute(e,"name");
        members = parseTokens(DOMUtil.getAttribute(e,"members"));
        
        if(DOMUtil.getAttribute(e,"properties")!=null) {
            fields = parseTokens(DOMUtil.getAttribute(e,"properties"));
            throw new AssertionError("//interface/@properties is not supported");
        } else    // no property was specified
            fields = new String[0];
    }
    
    /** &lt;interface> element in the binding file. */
    private final Element dom;
    
    /** Name of the generated Java interface. */
    private final String name;
    
    /**
     * Gets the name of this interface.
     * This name should also used as the class name.
     */
    public String name() { return name; }
    
    
    private final String[] members;
    
    /**
     * Gets the names of interfaces/classes that implement
     * this interface.
     */
    public String[] members() { return members; }
    
    
    private final String[] fields;
    
    /** Gets the names of fields in this interface. */
    public String[] fields() { return fields; }
    
    
    /** Gets the location where this declaration is declared. */
    public Locator getSourceLocation() {
        return DOM4JLocator.getLocationInfo(dom);
    }
    
    
    
    /** splits a list into an array of strings. */
    private static String[] parseTokens( String value ) {
        StringTokenizer tokens = new StringTokenizer(value);
        
        String[] r = new String[tokens.countTokens()];
        int i=0;
        while(tokens.hasMoreTokens())
            r[i++] = tokens.nextToken();
        
        return r;
    }
}

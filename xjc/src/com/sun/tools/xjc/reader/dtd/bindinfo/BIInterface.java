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

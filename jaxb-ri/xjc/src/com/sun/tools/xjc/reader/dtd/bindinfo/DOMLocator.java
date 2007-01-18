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

import org.w3c.dom.Element;
import org.xml.sax.Locator;

class DOMLocator {
    private static final String locationNamespace =
        "http://www.sun.com/xmlns/jaxb/dom-location";
    private static final String systemId    = "systemid";
    private static final String column      = "column";
    private static final String line        = "line";
    
    /** Sets the location information to a specified element. */
    public static void setLocationInfo( Element e, Locator loc ) {
        e.setAttributeNS(locationNamespace,"loc:"+systemId,loc.getSystemId());
        e.setAttributeNS(locationNamespace,"loc:"+column,Integer.toString(loc.getLineNumber()));
        e.setAttributeNS(locationNamespace,"loc:"+line,Integer.toString(loc.getColumnNumber()));
    }
    
    /**
     * Gets the location information from an element.
     * 
     * <p>
     * For this method to work, the setLocationInfo method has to be
     * called before.
     */
    public static Locator getLocationInfo( final Element e ) {
        if(DOMUtil.getAttribute(e,locationNamespace,systemId)==null)
            return null;    // no location information
        
        return new Locator(){
            public int getLineNumber() {
                return Integer.parseInt(DOMUtil.getAttribute(e,locationNamespace,line));
            }
            public int getColumnNumber() {
                return Integer.parseInt(DOMUtil.getAttribute(e,locationNamespace,column));
            }
            public String getSystemId() {
                return DOMUtil.getAttribute(e,locationNamespace,systemId);
            }
            // we are not interested in PUBLIC ID.
            public String getPublicId() { return null; }
        };
    }
}

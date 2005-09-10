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
package com.sun.tools.xjc.reader.internalizer;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Stores {@link Locator} objects for every {@link Element}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class LocatorTable {
    /** Locations of the start element. */
    private final Map startLocations = new HashMap();
    
    /** Locations of the end element. */
    private final Map endLocations = new HashMap();
    
    public void storeStartLocation( Element e, Locator loc ) {
        startLocations.put(e,new LocatorImpl(loc));
    }
    
    public void storeEndLocation( Element e, Locator loc ) {
        endLocations.put(e,new LocatorImpl(loc));
    }
    
    public Locator getStartLocation( Element e ) {
        return (Locator)startLocations.get(e);
    }
    
    public Locator getEndLocation( Element e ) {
        return (Locator)endLocations.get(e);
    }
}

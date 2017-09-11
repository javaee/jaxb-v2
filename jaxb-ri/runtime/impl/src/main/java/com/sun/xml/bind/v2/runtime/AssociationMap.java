/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.bind.v2.runtime;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bi-directional map between elements, inner peers,
 * and outer peers.
 * 
 * <p>
 * TODO: this should be rewritten for efficiency.
 * 
 * @since 2.0
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class AssociationMap<XmlNode> {
    final static class Entry<XmlNode> {
        /** XML element. */
    	private XmlNode element;
        /** inner peer, or null. */
        private Object inner;
        /** outer peer, or null. */
        private Object outer;
        
        public XmlNode element() {
        	return element;
        }
        public Object inner() {
        	return inner;
        }
        public Object outer() {
        	return outer;
        }
    }
    
    private final Map<XmlNode,Entry<XmlNode>> byElement = new IdentityHashMap<XmlNode,Entry<XmlNode>>();
    private final Map<Object,Entry<XmlNode>> byPeer = new IdentityHashMap<Object,Entry<XmlNode>>();
    private final Set<XmlNode> usedNodes = new HashSet<XmlNode>();

    /** Records the new {@code element <->inner} peer association. */
    public void addInner( XmlNode element, Object inner ) {
        Entry<XmlNode> e = byElement.get(element);
        if(e!=null) {
        	if(e.inner!=null)
                byPeer.remove(e.inner);
            e.inner = inner;
        } else {
        	e = new Entry<XmlNode>();
            e.element = element;
            e.inner = inner;
        }
        
        byElement.put(element,e);
        
        Entry<XmlNode> old = byPeer.put(inner,e);
        if(old!=null) {
            if(old.outer!=null)
                byPeer.remove(old.outer);
            if(old.element!=null)
                byElement.remove(old.element);
        }
    }
    
    /** Records the new {@code element <-> outer} peer association. */
    public void addOuter( XmlNode element, Object outer ) {
        Entry<XmlNode> e = byElement.get(element);
        if(e!=null) {
            if(e.outer!=null)
                byPeer.remove(e.outer);
            e.outer = outer;
        } else {
            e = new Entry<XmlNode>();
            e.element = element;
            e.outer = outer;
        }
        
        byElement.put(element,e);
        
        Entry<XmlNode> old = byPeer.put(outer,e);
        if(old!=null) {
            old.outer=null;
            
            if(old.inner==null)
                // remove this entry
                byElement.remove(old.element);
        }
    }

    public void addUsed( XmlNode n ) {
        usedNodes.add(n);
    }

    public Entry<XmlNode> byElement( Object e ) {
        return byElement.get(e);
    }
    
    public Entry<XmlNode> byPeer( Object o ) {
        return byPeer.get(o);
    }
    
    public Object getInnerPeer( XmlNode element ) {
        Entry e = byElement(element);
        if(e==null)     return null;
        else            return e.inner;
    }
    
    public Object getOuterPeer( XmlNode element ) {
        Entry e = byElement(element);
        if(e==null)     return null;
        else            return e.outer;
    }
}

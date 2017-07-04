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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.util.QNameMap;

/**
 * Creates {@link Name}s and assign index numbers to them.
 *
 * <p>
 * During this process, this class also finds out which namespace URIs
 * are statically known to be un-bindable as the default namespace.
 * Those are the namespace URIs that are used by attribute names.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"StringEquality"})
public final class NameBuilder {
    private Map<String,Integer> uriIndexMap = new HashMap<String, Integer>();
    private Set<String> nonDefaultableNsUris = new HashSet<String>();
    private Map<String,Integer> localNameIndexMap = new HashMap<String, Integer>();
    private QNameMap<Integer> elementQNameIndexMap = new QNameMap<Integer>();
    private QNameMap<Integer> attributeQNameIndexMap = new QNameMap<Integer>();

    public Name createElementName(QName name) {
        return createElementName(name.getNamespaceURI(),name.getLocalPart());
    }

    public Name createElementName(String nsUri, String localName) {
        return createName(nsUri, localName, false, elementQNameIndexMap);
    }

    public Name createAttributeName(QName name) {
        return createAttributeName(name.getNamespaceURI(),name.getLocalPart());
    }

    public Name createAttributeName(String nsUri, String localName) {
        assert nsUri.intern()==nsUri;
        assert localName.intern()==localName;

        if(nsUri.length()==0)
            return new Name(
                    allocIndex(attributeQNameIndexMap,"",localName),
                    -1,
                    nsUri,
                    allocIndex(localNameIndexMap,localName),
                    localName,
                    true);
        else {
            nonDefaultableNsUris.add(nsUri);
            return createName(nsUri,localName, true, attributeQNameIndexMap);
        }
    }

    private Name createName(String nsUri, String localName, boolean isAttribute, QNameMap<Integer> map) {        
        assert nsUri.intern()==nsUri;
        assert localName.intern()==localName;
                
        return new Name(
                allocIndex(map,nsUri,localName),
                allocIndex(uriIndexMap,nsUri),
                nsUri,
                allocIndex(localNameIndexMap,localName),
                localName, 
                isAttribute );
    }
    
    private int allocIndex(Map<String,Integer> map, String str) {
        Integer i = map.get(str);
        if(i==null) {
            i = map.size();
            map.put(str,i);
        }
        return i;
    }

    private int allocIndex(QNameMap<Integer> map, String nsUri, String localName) {
        Integer i = map.get(nsUri,localName);
        if(i==null) {
            i = map.size();
            map.put(nsUri,localName,i);
        }
        return i;
    }
    
    /**
     * Wraps up everything and creates {@link NameList}.
     */
    public NameList conclude() {
        boolean[] nsUriCannotBeDefaulted = new boolean[uriIndexMap.size()];
        for (Map.Entry<String,Integer> e : uriIndexMap.entrySet()) {
            nsUriCannotBeDefaulted[e.getValue()] = nonDefaultableNsUris.contains(e.getKey());
        }

        NameList r = new NameList(
                list(uriIndexMap),
                nsUriCannotBeDefaulted,
                list(localNameIndexMap), 
                elementQNameIndexMap.size(),
                attributeQNameIndexMap.size() );
        // delete them so that the create method can never be called again
        uriIndexMap = null;
        localNameIndexMap = null;
        return r;
    }

    private String[] list(Map<String, Integer> map) {
        String[] r = new String[map.size()];
        for (Map.Entry<String, Integer> e : map.entrySet())
            r[e.getValue()] = e.getKey();
        return r;
    }    
}

package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.QNameMap;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Creates {@link Name}s and assign index numbers to them.
 *
 * @author Kohsuke Kawaguchi
 */
public final class NameBuilder {
    private Map<String,Integer> uriIndexMap = new HashMap<String, Integer>();
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
        else
            return createName(nsUri,localName, true, attributeQNameIndexMap);
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
        NameList r = new NameList(
                list(uriIndexMap),
                list(localNameIndexMap), 
                elementQNameIndexMap.size(),
                attributeQNameIndexMap.size() );
        // delete them so that the create method can never be called again
        uriIndexMap = null;
        localNameIndexMap = null;
        elementQNameIndexMap = null;
        attributeQNameIndexMap = null;
        return r;
    }

    private String[] list(Map<String, Integer> map) {
        String[] r = new String[map.size()];
        for (Map.Entry<String, Integer> e : map.entrySet())
            r[e.getValue()] = e.getKey();
        return r;
    }    
}

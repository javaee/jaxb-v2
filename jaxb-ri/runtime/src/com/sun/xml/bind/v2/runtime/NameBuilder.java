package com.sun.xml.bind.v2.runtime;

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

    public Name createElementName(QName name) {
        return createElementName(name.getNamespaceURI(),name.getLocalPart());
    }

    public Name createElementName(String nsUri, String localName) {
        assert nsUri.intern()==nsUri;
        assert localName.intern()==localName;

        return new Name(
                allocIndex(uriIndexMap,nsUri),
                nsUri,
                allocIndex(localNameIndexMap,localName),
                localName );
    }

    public Name createAttributeName(QName name) {
        return createAttributeName(name.getNamespaceURI(),name.getLocalPart());
    }

    public Name createAttributeName(String nsUri, String localName) {
        assert nsUri.intern()==nsUri;
        assert localName.intern()==localName;

        if(nsUri.length()==0)
            return new Name(-1,nsUri,allocIndex(localNameIndexMap,localName),localName);
        else
            return createElementName(nsUri,localName);
    }


    private int allocIndex(Map<String,Integer> map, String str) {
        Integer i = map.get(str);
        if(i==null) {
            i = map.size();
            map.put(str,i);
        }
        return i;
    }

    /**
     * Wraps up everything and creates {@link NameList}.
     */
    public NameList conclude() {
        NameList r = new NameList(list(uriIndexMap),list(localNameIndexMap));
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

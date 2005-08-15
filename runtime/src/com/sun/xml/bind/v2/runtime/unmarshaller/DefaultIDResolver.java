package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.util.HashMap;
import java.util.concurrent.Callable;

import com.sun.xml.bind.IDResolver;

/**
 * Default implementation of {@link IDResolver}.
 *
 * @author Kohsuke Kawaguchi
 */
final class DefaultIDResolver extends IDResolver {
    /** Records ID->Object map. */
    private HashMap<String,Object> idmap = null;

    public void startDocument() {
        if(idmap!=null)
            idmap.clear();
    }

    public void bind(String id, Object obj) {
        if(idmap==null)     idmap = new HashMap<String,Object>();
        idmap.put(id,obj);
    }

    public Callable resolve(final String id, Class targetType) {
        return new Callable() {
            public Object call() throws Exception {
                if(idmap==null)     return null;
                return idmap.get(id);
            }
        };
    }
}

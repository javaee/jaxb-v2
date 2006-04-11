package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

import org.xml.sax.SAXException;

/**
 * Looks for xsi:nil='true' and sets the target to null.
 * Otherwise delegate to another handler.
 *
 * @author Kohsuke Kawaguchi
 */
public class XsiNilLoader extends ProxyLoader {

    private final Loader defaultLoader;

    public XsiNilLoader(Loader defaultLoader) {
        this.defaultLoader = defaultLoader;
        assert defaultLoader!=null;
    }

    protected Loader selectLoader(UnmarshallingContext.State state, TagName ea) throws SAXException {
        int idx = ea.atts.getIndex(WellKnownNamespace.XML_SCHEMA_INSTANCE,"nil");

        if(idx!=-1) {
            String value = ea.atts.getValue(idx);
            if(DatatypeConverterImpl._parseBoolean(value)) {
                onNil(state);
                return Discarder.INSTANCE;
            }
        }

        return defaultLoader;
    }

    /**
     * Called when xsi:nil='true' was found.
     */
    protected void onNil(UnmarshallingContext.State state) throws SAXException {
    }



    public static final class Single extends XsiNilLoader {
        private final Accessor acc;
        public Single(Loader l, Accessor acc) {
            super(l);
            this.acc = acc;
        }

        protected void onNil(UnmarshallingContext.State state) throws SAXException {
            try {
                acc.set(state.prev.target,null);
            } catch (AccessorException e) {
                handleGenericException(e,true);
            }
        }
    }

    public static final class Array extends XsiNilLoader {
        public Array(Loader core) {
            super(core);
        }

        protected void onNil(UnmarshallingContext.State state) {
            // let the receiver add this to the lister
            state.target = null;
        }
    }
}

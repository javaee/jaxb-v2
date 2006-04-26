package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.namespace.QName;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Looks at @xsi:type and forwards to the right {@link Loader}.
 *
 * @author Kohsuke Kawaguchi
 */
public class XsiTypeLoader extends Loader {

    /**
     * Use this when no @xsi:type was found.
     */
    private final JaxBeanInfo defaultBeanInfo;

    public XsiTypeLoader(JaxBeanInfo defaultBeanInfo) {
        super(true);
        this.defaultBeanInfo = defaultBeanInfo;
    }

    public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        JaxBeanInfo beanInfo = parseXsiType(state, ea);
        if(beanInfo==null)
            beanInfo = defaultBeanInfo;

        Loader loader = beanInfo.getLoader(null,false);
        state.loader = loader;
        loader.startElement(state,ea);
    }

    /*pacakge*/ static JaxBeanInfo parseXsiType(UnmarshallingContext.State state, TagName ea) throws SAXException {
        UnmarshallingContext context = state.getContext();
        JaxBeanInfo beanInfo = null;

        // look for @xsi:type
        Attributes atts = ea.atts;
        int idx = atts.getIndex(WellKnownNamespace.XML_SCHEMA_INSTANCE,"type");

        if(idx>=0) {
            // we'll consume the value only when it's a recognized value,
            // so don't consume it just yet.
            String value = atts.getValue(idx);

            QName type = DatatypeConverterImpl._parseQName(value,context);
            if(type==null) {
                reportError(Messages.NOT_A_QNAME.format(value),true);
            } else {
                beanInfo = context.getJAXBContext().getGlobalType(type);
                if(beanInfo==null) {
                    reportError(Messages.UNRECOGNIZED_TYPE_NAME.format(type),true);
                }
                // TODO: resurrect the following check
        //                    else
        //                    if(!target.isAssignableFrom(actual)) {
        //                        reportError(context,
        //                            Messages.UNSUBSTITUTABLE_TYPE.format(value,actual.getName(),target.getName()),
        //                            true);
        //                        actual = targetBeanInfo;  // ditto
        //                    }
            }
        }
        return beanInfo;
    }
}

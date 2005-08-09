package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.namespace.QName;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

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
        UnmarshallingContext context = state.getContext();

        // look for @xsi:type
        Attributes atts = ea.atts;
        int idx = atts.getIndex(WellKnownNamespace.XML_SCHEMA_INSTANCE,"type");

        JaxBeanInfo beanInfo = defaultBeanInfo;

        if(idx>=0) {
            // we'll consume the value only when it's a recognized value,
            // so don't consume it just yet.
            String value = atts.getValue(idx);

            QName type = DatatypeConverterImpl._parseQName(value,context);
            if(type==null) {
                reportError(Messages.NOT_A_QNAME.format(value),true);
            } else {
                beanInfo =  context.getJAXBContext().getGlobalType(type);
                if(beanInfo!=null) {
                    ea.eatAttribute(idx);
                } else {
                    reportError(Messages.UNRECOGNIZED_TYPE_NAME.format(value),true);
                    beanInfo = defaultBeanInfo;  // try to recover by using the default target type.
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

        Loader loader = beanInfo.getLoader();
        state.loader = loader;
        loader.startElement(state,ea);
    }
}

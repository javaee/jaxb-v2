/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id:
 */
package com.sun.xml.bind.v2.runtime.property;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.QNameMap;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The AttributeDispatcher   is used to implement
 * the unmarshalling by name functionality for attributes.
 * 
 * <p>
 * The AttributeDispatcher unmarshals a particular attribute
 * and gets the control back to itself.
 *
 *
 * @author
 *     Bhakti Mehta (bhakti.mehta@sun.com)
 * @since 2.0
 */
public class AttributeDispatcher extends Unmarshaller.ForkHandler {

    /**
     * This map statically stores information of the
     * unmarshaller handler and can be used while unmarshalling
     * Since creating new QNames is expensive use this optimized
     * version of the map
     *
     */
    private final QNameMap<TransducedAccessor> attUnmarshallers;

    /**
     * This handler will receive all the attributes
     * that were not processed. Never be null.
     */
    private final Unmarshaller.AttributeHandler catchAll;


    public AttributeDispatcher( List<AttributeProperty> properties, Accessor<?,Map<QName,Object>> wildcard, Unmarshaller.Handler next ,Unmarshaller.Handler fallthrough) {
        super(fallthrough,next);

        this.attUnmarshallers = new QNameMap<TransducedAccessor>();
        if(wildcard!=null)
            this.catchAll = new Unmarshaller.AttributeWildcardHandler(wildcard,next);
        else
            this.catchAll = DUMMY_CATCH_ALL;

        for( AttributeProperty p : properties )
            attUnmarshallers.put(p.attName.toQName(),p.xacc);
    }


    public void activate(UnmarshallingContext context) throws SAXException {
        Attributes atts = context.getUnconsumedAttributes();
        for (int i = 0; i < atts.getLength(); i ++){
            String auri = atts.getURI(i);
            String alocal = atts.getLocalName(i);
            String avalue = atts.getValue(auri,alocal);
            TransducedAccessor xacc = attUnmarshallers.get(auri,alocal);

            try {
                if(xacc!=null) {
                    xacc.parse(context.getTarget(),avalue);
                } else {
                    String aqname = atts.getQName(i);
                    catchAll.processValue(context,auri,alocal,aqname,avalue);
                }
            } catch (AccessorException e) {
               handleGenericException(e,true);
            }
        }
    }

    /**
     * Dummy {@link Unmarshaller.AttributeHandler} that just discards the value.
     */
    private static final Unmarshaller.AttributeHandler DUMMY_CATCH_ALL = new Unmarshaller.AttributeHandler(null,null) {
        public void processValue(UnmarshallingContext context, String nsUri, String local, String qname, String value) {
            ; // noop
        }

        protected boolean checkAttribute(UnmarshallingContext context) {
            return false;
        }
    };
}
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

package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.namespace.QName;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.istack.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
        JaxBeanInfo beanInfo = parseXsiType(state,ea,defaultBeanInfo);
        if(beanInfo==null)
            beanInfo = defaultBeanInfo;

        Loader loader = beanInfo.getLoader(null,false);
        state.setLoader(loader);
        loader.startElement(state,ea);
    }

    /*pacakge*/ static JaxBeanInfo parseXsiType(UnmarshallingContext.State state, TagName ea, @Nullable JaxBeanInfo defaultBeanInfo) throws SAXException {
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
                if(defaultBeanInfo!=null && defaultBeanInfo.getTypeNames().contains(type))
                    // if this xsi:type is something that the default type can already handle,
                    // let it do so. This is added as a work around to bug https://jax-ws.dev.java.net/issues/show_bug.cgi?id=195
                    // where a redundant xsi:type="xs:dateTime" causes JAXB to unmarshal XMLGregorianCalendar,
                    // where Date is expected.
                    // this is not a complete fix, as we still won't be able to handle simple type substitution in general,
                    // but none-the-less
                    return defaultBeanInfo;

                beanInfo = context.getJAXBContext().getGlobalType(type);
                if(beanInfo==null) { // let's report an error
                    if (context.parent.hasEventHandler() // is somebody listening?
                            && context.shouldErrorBeReported()) { // should we report error?
                        String nearest = context.getJAXBContext().getNearestTypeName(type);
                        if(nearest!=null)
                            reportError(Messages.UNRECOGNIZED_TYPE_NAME_MAYBE.format(type,nearest),true);
                        else
                            reportError(Messages.UNRECOGNIZED_TYPE_NAME.format(type),true);
                    }
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

    static final QName XsiTypeQNAME = new QName(WellKnownNamespace.XML_SCHEMA_INSTANCE,"type");

    @Override
    public Collection<QName> getExpectedAttributes() {
        final Collection<QName> expAttrs =  new HashSet<QName>();
        expAttrs.addAll(super.getExpectedAttributes());
        expAttrs.add(XsiTypeQNAME);
        return Collections.unmodifiableCollection(expAttrs);
    }
}

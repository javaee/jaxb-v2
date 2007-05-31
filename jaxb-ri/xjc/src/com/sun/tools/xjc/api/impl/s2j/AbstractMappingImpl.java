/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.xjc.api.impl.s2j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;

/**
 * Partial common implementation between {@link ElementMappingImpl} and {@link BeanMappingImpl}
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractMappingImpl<InfoT extends CElement> implements Mapping {

    protected final JAXBModelImpl parent;
    protected final InfoT clazz;

    /**
     * Lazily computed.
     *
     * @see #getWrapperStyleDrilldown()
     */
    private List<Property> drilldown = null;
    private boolean drilldownComputed = false;

    protected AbstractMappingImpl(JAXBModelImpl parent, InfoT clazz) {
        this.parent = parent;
        this.clazz = clazz;
    }

    public final QName getElement() {
        return clazz.getElementName();
    }

    public final String getClazz() {
        return clazz.getType().fullName();
    }

    public final List<? extends Property> getWrapperStyleDrilldown() {
        if(!drilldownComputed) {
            drilldownComputed = true;
            drilldown = calcDrilldown();
        }
        return drilldown;
    }

    protected abstract List<Property> calcDrilldown();


    /**
     * Derived classes can use this method to implement {@link #calcDrilldown}.
     */
    protected List<Property> buildDrilldown(CClassInfo typeBean) {
        List<Property> result;

        CClassInfo bc = typeBean.getBaseClass();
        if(bc!=null) {
            result = buildDrilldown(bc);
            if(result==null)
                return null;        // aborted
        } else
            result = new ArrayList<Property>();

        for( CPropertyInfo p : typeBean.getProperties() ) {
            if (p instanceof CElementPropertyInfo) {
                CElementPropertyInfo ep = (CElementPropertyInfo) p;
// wrong. A+,B,C is eligible for drill-down.
//                if(ep.isCollection())
//                    // content model like A+,B,C is not eligible
//                    return null;

                List<? extends CTypeRef> ref = ep.getTypes();
                if(ref.size()!=1)
                    // content model like (A|B),C is not eligible
                    return null;

                result.add(createPropertyImpl(ep,ref.get(0).getTagName()));
            } else
            if (p instanceof ReferencePropertyInfo) {
                CReferencePropertyInfo rp = (CReferencePropertyInfo) p;

                Collection<CElement> elements = rp.getElements();
                if(elements.size()!=1)
                    return null;

                CElement ref = elements.iterator().next();
                if(ref instanceof ClassInfo) {
                    result.add(createPropertyImpl(rp,ref.getElementName()));
                } else {
                    CElementInfo eref = (CElementInfo)ref;
                    if(!eref.getSubstitutionMembers().isEmpty())
                        return null;    // elements with a substitution group isn't qualified for the wrapper style

                    // JAX-WS doesn't want to see JAXBElement, so we have to hide it for them.
                    ElementAdapter fr;
                    if(rp.isCollection())
                        fr = new ElementCollectionAdapter(parent.outline.getField(rp), eref);
                    else
                        fr = new ElementSingleAdapter(parent.outline.getField(rp), eref);

                    result.add(new PropertyImpl(this,
                        fr, eref.getElementName()));
                }
            } else
                // to be eligible for the wrapper style, only elements are allowed.
                // according to the JAX-RPC spec 2.3.1.2, element refs are disallowed
                return null;

        }

        return result;
    }

    private Property createPropertyImpl(CPropertyInfo p, QName tagName) {
        return new PropertyImpl(this,
            parent.outline.getField(p),tagName);
    }
}

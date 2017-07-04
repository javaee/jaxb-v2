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

package com.sun.xml.bind.v2.runtime.property;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

/**
 * Create {@link Property} objects.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class PropertyFactory {
    private PropertyFactory() {}


    /**
     * Constructors of the {@link Property} implementation.
     */
    private static final Constructor<? extends Property>[] propImpls;

    static {
        Class<? extends Property>[] implClasses = new Class[] {
            SingleElementLeafProperty.class,
            null, // single reference leaf --- but there's no such thing as "reference leaf"
            null, // no such thing as "map leaf"

            ArrayElementLeafProperty.class,
            null, // array reference leaf --- but there's no such thing as "reference leaf"
            null, // no such thing as "map leaf"

            SingleElementNodeProperty.class,
            SingleReferenceNodeProperty.class,
            SingleMapNodeProperty.class,

            ArrayElementNodeProperty.class,
            ArrayReferenceNodeProperty.class,
            null, // map is always a single property (Map doesn't implement Collection)
        };

        propImpls = new Constructor[implClasses.length];
        for( int i=0; i<propImpls.length; i++ ) {
            if(implClasses[i]!=null)
                // this pointless casting necessary for Mustang
                propImpls[i] = (Constructor)implClasses[i].getConstructors()[0];
        }
    }

    /**
     * Creates/obtains a properly configured {@link Property}
     * object from the given description.
     */
    public static Property create( JAXBContextImpl grammar, RuntimePropertyInfo info ) {

        PropertyKind kind = info.kind();

        switch(kind) {
        case ATTRIBUTE:
            return new AttributeProperty(grammar,(RuntimeAttributePropertyInfo)info);
        case VALUE:
            return new ValueProperty(grammar,(RuntimeValuePropertyInfo)info);
        case ELEMENT:
            if(((RuntimeElementPropertyInfo)info).isValueList())
                return new ListElementProperty(grammar,(RuntimeElementPropertyInfo) info);
            break;
        case REFERENCE:
        case MAP:
            break;
        default:
            assert false;
        }


        boolean isCollection = info.isCollection();
        boolean isLeaf = isLeaf(info);

        Constructor<? extends Property> c = propImpls[(isLeaf?0:6)+(isCollection?3:0)+kind.propertyIndex];
        try {
            return c.newInstance( grammar, info );
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if(t instanceof Error)
                throw (Error)t;
            if(t instanceof RuntimeException)
                throw (RuntimeException)t;

            throw new AssertionError(t);
        }
    }

    /**
     * Look for the case that can be optimized as a leaf,
     * which is a kind of type whose XML representation is just PCDATA.
     */
    static boolean isLeaf(RuntimePropertyInfo info) {
        Collection<? extends RuntimeTypeInfo> types = info.ref();
        if(types.size()!=1)     return false;

        RuntimeTypeInfo rti = types.iterator().next();
        if(!(rti instanceof RuntimeNonElement)) return false;

        if(info.id()==ID.IDREF)
            // IDREF is always handled as leaf -- Transducer maps IDREF String back to an object
            return true;

        if(((RuntimeNonElement)rti).getTransducer()==null)
            // Transducer!=null means definitely binds to PCDATA.
            // even if transducer==null, a referene might be IDREF,
            // in which case it will still produce PCDATA in this reference.
            return false;

        if(!info.getIndividualType().equals(rti.getType()))
            return false;

        return true;
    }
}

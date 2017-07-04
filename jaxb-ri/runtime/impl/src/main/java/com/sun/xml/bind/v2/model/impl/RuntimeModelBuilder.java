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

package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import javax.activation.MimeType;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.bind.v2.runtime.FilterTransducer;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.InlineBinaryTransducer;
import com.sun.xml.bind.v2.runtime.MimeTypedTransducer;
import com.sun.xml.bind.v2.runtime.SchemaTypeTransducer;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.istack.Nullable;

import com.sun.xml.bind.v2.WellKnownNamespace;
import javax.xml.namespace.QName;
import org.xml.sax.SAXException;

/**
 * {@link ModelBuilder} that works at the run-time by using
 * the {@code java.lang.reflect} package.
 *
 * <p>
 * This extends {@link ModelBuilder} by providing more functionalities such
 * as accessing the fields and classes.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class RuntimeModelBuilder extends ModelBuilder<Type,Class,Field,Method> {
    /**
     * The {@link JAXBContextImpl} for which the model is built.
     * Null when created for reflection.
     */
    public final @Nullable JAXBContextImpl context;

    public RuntimeModelBuilder(JAXBContextImpl context, RuntimeAnnotationReader annotationReader, Map<Class, Class> subclassReplacements, String defaultNamespaceRemap) {
        super(annotationReader, Utils.REFLECTION_NAVIGATOR, subclassReplacements, defaultNamespaceRemap);
        this.context = context;
    }

    @Override
    public RuntimeNonElement getClassInfo( Class clazz, Locatable upstream ) {
        return (RuntimeNonElement)super.getClassInfo(clazz,upstream);
    }

    @Override
    public RuntimeNonElement getClassInfo( Class clazz, boolean searchForSuperClass, Locatable upstream ) {
        return (RuntimeNonElement)super.getClassInfo(clazz,searchForSuperClass,upstream);
    }

    @Override
    protected RuntimeEnumLeafInfoImpl createEnumLeafInfo(Class clazz, Locatable upstream) {
        return new RuntimeEnumLeafInfoImpl(this,upstream,clazz);
    }

    @Override
    protected RuntimeClassInfoImpl createClassInfo( Class clazz, Locatable upstream ) {
        return new RuntimeClassInfoImpl(this,upstream,clazz);
    }

    @Override
    public RuntimeElementInfoImpl createElementInfo(RegistryInfoImpl<Type,Class,Field,Method> registryInfo, Method method) throws IllegalAnnotationException {
        return new RuntimeElementInfoImpl(this,registryInfo, method);
    }

    @Override
    public RuntimeArrayInfoImpl createArrayInfo(Locatable upstream, Type arrayType) {
        return new RuntimeArrayInfoImpl(this, upstream, (Class)arrayType);
    }

    @Override
    protected RuntimeTypeInfoSetImpl createTypeInfoSet() {
        return new RuntimeTypeInfoSetImpl(reader);
    }

    @Override
    public RuntimeTypeInfoSet link() {
        return (RuntimeTypeInfoSet)super.link();
    }

    /**
     * Creates a {@link Transducer} given a reference.
     *
     * Used to implement {@link RuntimeNonElementRef#getTransducer()}.
     * Shouldn't be called from anywhere else.
     *
     * TODO: this is not the proper place for this class to be in.
     */
    public static Transducer createTransducer(RuntimeNonElementRef ref) {
        Transducer t = ref.getTarget().getTransducer();
        RuntimePropertyInfo src = ref.getSource();
        ID id = src.id();

        if(id==ID.IDREF)
            return RuntimeBuiltinLeafInfoImpl.STRING;

        if(id==ID.ID)
            t = new IDTransducerImpl(t);

        MimeType emt = src.getExpectedMimeType();
        if(emt!=null)
            t = new MimeTypedTransducer(t,emt);

        if(src.inlineBinaryData())
            t = new InlineBinaryTransducer(t);

        if(src.getSchemaType()!=null) {
            if (src.getSchemaType().equals(createXSSimpleType())) {
                return RuntimeBuiltinLeafInfoImpl.STRING;
            }
            t = new SchemaTypeTransducer(t,src.getSchemaType());
        }
        
        return t;
    }

    private static QName createXSSimpleType() {
        return new QName(WellKnownNamespace.XML_SCHEMA,"anySimpleType");
    }

    /**
     * Transducer implementation for ID.
     *
     * This transducer wraps another {@link Transducer} and adds
     * handling for ID.
     */
    private static final class IDTransducerImpl<ValueT> extends FilterTransducer<ValueT> {
        public IDTransducerImpl(Transducer<ValueT> core) {
            super(core);
        }

        @Override
        public ValueT parse(CharSequence lexical) throws AccessorException, SAXException {
            String value = WhiteSpaceProcessor.trim(lexical).toString();
            UnmarshallingContext.getInstance().addToIdTable(value);
            return core.parse(value);
        }
    }
}

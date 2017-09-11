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

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlList;
import javax.xml.namespace.QName;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;

/**
 * Common {@link ElementPropertyInfo} implementation used for both
 * Annotation Processing and runtime.
 * 
 * @author Kohsuke Kawaguchi
 */
class ElementPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends ERPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ElementPropertyInfo<TypeT,ClassDeclT>
{
    /**
     * Lazily computed.
     * @see #getTypes()
     */
    private List<TypeRefImpl<TypeT,ClassDeclT>> types;

    private final List<TypeInfo<TypeT,ClassDeclT>> ref = new AbstractList<TypeInfo<TypeT,ClassDeclT>>() {
        public TypeInfo<TypeT,ClassDeclT> get(int index) {
            return getTypes().get(index).getTarget();
        }

        public int size() {
            return getTypes().size();
        }
    };

    /**
     * Lazily computed.
     * @see #isRequired()
     */
    private Boolean isRequired;

    /**
     * @see #isValueList()
     */
    private final boolean isValueList;

    ElementPropertyInfoImpl(
        ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent,
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> propertySeed) {
        super(parent, propertySeed);

        isValueList = seed.hasAnnotation(XmlList.class);

    }

    public List<? extends TypeRefImpl<TypeT,ClassDeclT>> getTypes() {
        if(types==null) {
            types = new FinalArrayList<TypeRefImpl<TypeT,ClassDeclT>>();
            XmlElement[] ann=null;

            XmlElement xe = seed.readAnnotation(XmlElement.class);
            XmlElements xes = seed.readAnnotation(XmlElements.class);

            if(xe!=null && xes!=null) {
                parent.builder.reportError(new IllegalAnnotationException(
                        Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(
                                nav().getClassName(parent.getClazz())+'#'+seed.getName(),
                                xe.annotationType().getName(), xes.annotationType().getName()),
                        xe, xes ));
            }

            isRequired = true;

            if(xe!=null)
                ann = new XmlElement[]{xe};
            else
            if(xes!=null)
                ann = xes.value();

            if(ann==null) {
                // default
                TypeT t = getIndividualType();
                if(!nav().isPrimitive(t) || isCollection())
                    isRequired = false;
                // nillableness defaults to true if it's collection
                types.add(createTypeRef(calcXmlName((XmlElement)null),t,isCollection(),null));
            } else {
                for( XmlElement item : ann ) {
                    // TODO: handle defaulting in names.
                    QName name = calcXmlName(item);
                    TypeT type = reader().getClassValue(item, "type");
                    if (nav().isSameType(type, nav().ref(XmlElement.DEFAULT.class)))
                        type = getIndividualType();
                    if((!nav().isPrimitive(type) || isCollection()) && !item.required())
                        isRequired = false;
                    types.add(createTypeRef(name, type, item.nillable(), getDefaultValue(item.defaultValue()) ));
                }
            }
            types = Collections.unmodifiableList(types);
            assert !types.contains(null);
        }
        return types;
    }

    private String getDefaultValue(String value) {
        if(value.equals("\u0000"))
            return null;
        else
            return value;
    }

    /**
     * Used by {@link PropertyInfoImpl} to create new instances of {@link TypeRef}
     */
    protected TypeRefImpl<TypeT,ClassDeclT> createTypeRef(QName name,TypeT type,boolean isNillable,String defaultValue) {
        return new TypeRefImpl<TypeT,ClassDeclT>(this,name,type,isNillable,defaultValue);
    }

    public boolean isValueList() {
        return isValueList;
    }

    public boolean isRequired() {
        if(isRequired==null)
            getTypes(); // compute the value
        return isRequired;
    }

    public List<? extends TypeInfo<TypeT,ClassDeclT>> ref() {
        return ref;
    }

    public final PropertyKind kind() {
        return PropertyKind.ELEMENT;
    }

    protected void link() {
        super.link();
        for (TypeRefImpl<TypeT, ClassDeclT> ref : getTypes() ) {
            ref.link();
        }

        if(isValueList()) {
            // ugly test, because IDREF's are represented as text on the wire,
            // it's OK to be a value list in that case.
            if(id()!= ID.IDREF) {
                // check if all the item types are simple types
                // this can't be done when we compute types because
                // not all TypeInfos are available yet
                for (TypeRefImpl<TypeT,ClassDeclT> ref : types) {
                    if(!ref.getTarget().isSimpleType()) {
                        parent.builder.reportError(new IllegalAnnotationException(
                        Messages.XMLLIST_NEEDS_SIMPLETYPE.format(
                            nav().getTypeName(ref.getTarget().getType())), this ));
                        break;
                    }
                }
            }

            if(!isCollection())
                parent.builder.reportError(new IllegalAnnotationException(
                    Messages.XMLLIST_ON_SINGLE_PROPERTY.format(), this
                ));
        }
    }
}

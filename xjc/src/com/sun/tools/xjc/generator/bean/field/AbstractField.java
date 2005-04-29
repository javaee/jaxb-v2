/*
 * @(#)$Id: AbstractField.java,v 1.4 2005-04-29 21:20:22 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.annotation.ri.XmlAnyElementWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlAttributeWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementRefsWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementWrapperWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementsWriter;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.reader.TypeUtil;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMixed;
import com.sun.xml.bind.v2.TODO;

import static com.sun.tools.xjc.outline.Aspect.IMPLEMENTATION;

/**
 * Useful base class for implementing {@link FieldOutline}.
 * 
 * <p>
 * This class just provides a few utility methods and keep some
 * important variables so that they can be readily accessed any time.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractField implements FieldOutline {
    
    protected final ClassOutlineImpl outline;
    
    protected final CPropertyInfo prop;
    
    protected final JCodeModel codeModel;

    /**
     * The type of this field, which can hold all the possible types.
     */
    protected final JType implType;

    /**
     * The publicly visible type of this field.
     * If we are generating value classes implType==exposedType.
     */
    protected final JType exposedType;

    protected AbstractField( ClassOutlineImpl outline, CPropertyInfo prop ) {
        this.outline = outline;
        this.prop = prop;
        this.codeModel = outline.parent().getCodeModel();
        this.implType = getType(IMPLEMENTATION);
        this.exposedType = getType(Aspect.EXPOSED);
    }

    public final ClassOutline parent() {
        return outline;
    }

    public final CPropertyInfo getPropertyInfo() {
        return prop;
    }


    /**
     * Annotate the field according to the recipes given as {@link CPropertyInfo}.
     */
    protected void annotate( JAnnotatable field ) {

        assert(field!=null);

        /*
        TODO: consider moving this logic to somewhere else
        so that it can be better shared, for how a field gets
        annotated doesn't really depend on how we generate accessors.

        so perhaps we should separate those two.
        */

        // TODO: consider a visitor
        if (prop instanceof CAttributePropertyInfo) {
            annotateAttribute(field);
        } else if (prop instanceof CElementPropertyInfo) {
            annotateElement(field);
        } else if (prop instanceof CValuePropertyInfo) {
            field.annotate(XmlValue.class);
        } else if (prop instanceof CReferencePropertyInfo) {
            annotateReference(field);
        }

        outline.parent().generateAdapterIfNecessary(prop,field);
    }

    private void annotateReference(JAnnotatable field) {
        CReferencePropertyInfo rp = (CReferencePropertyInfo) prop;

        TODO.prototype();
        // this is just a quick hack to get the basic test working

        Collection<CElement> elements = rp.getElements();

        XmlElementRefWriter refw;
        if(elements.size()==1) {
            refw = field.annotate2(XmlElementRefWriter.class);
            CElement e = elements.iterator().next();
            refw.name(e.getElementName().getLocalPart())
                .namespace(e.getElementName().getNamespaceURI())
                .type(e.getType().toType(outline.parent(),IMPLEMENTATION));
        } else
        if(elements.size()>1) {
            XmlElementRefsWriter refsw = field.annotate2(XmlElementRefsWriter.class);
            for( CElement e : elements ) {
                refw = refsw.value();
                refw.name(e.getElementName().getLocalPart())
                    .namespace(e.getElementName().getNamespaceURI())
                    .type(e.getType().toType(outline.parent(),IMPLEMENTATION));
            }
        }

        if(rp.isMixed())
            field.annotate(XmlMixed.class);

        NClass dh = rp.getDOMHandler();
        if(dh!=null) {
            XmlAnyElementWriter xaew = field.annotate2(XmlAnyElementWriter.class);
            xaew.lax(rp.getWildcard().allowTypedObject);
            xaew.value(dh.toType(outline.parent(),IMPLEMENTATION));
        }

    }

    /**
     * Annotate the element property 'field'
     */
    private void annotateElement(JAnnotatable field) {
        CElementPropertyInfo ep = (CElementPropertyInfo) prop;
        List<CTypeRef> types = ep.getTypes();
        QName eName = ep.getXmlName();

        if(ep.isValueList()) {
            field.annotate(XmlList.class);
        }

        if( types.size() == 1 ) { // single prop
            assert(eName==null); // illegal to have a single value prop with a wrapper

            // [RESULT]
            // @XmlElement(name=Foo.class, isNillable=false, targetNamespace="bar://baz"

            CTypeRef type = types.get(0);
            XmlElementWriter xew = field.annotate2(XmlElementWriter.class);
            xew.name(type.getTagName().getLocalPart())
               .type(type.getTarget().toType(outline.parent(), IMPLEMENTATION))
               .namespace(type.getTagName().getNamespaceURI())
               .nillable(type.isNillable());
            if(type.getDefaultValue()!=null)
                xew.defaultValue(type.getDefaultValue());
        } else { // multi prop
            if( eName!=null ) { // wrapper
                XmlElementWrapperWriter xcw = field.annotate2(XmlElementWrapperWriter.class);
                xcw.name(eName.getLocalPart())
                   .namespace(eName.getNamespaceURI());
            }

            if (types.size() == 1) {
                CTypeRef t = types.get(0);
                XmlElementWriter xew = field.annotate2(XmlElementWriter.class);
                xew.name(t.getTagName().getLocalPart())
                   .namespace(t.getTagName().getNamespaceURI())
                   .type(resolve(t,IMPLEMENTATION));
            } else {
                XmlElementsWriter xesw = field.annotate2(XmlElementsWriter.class);
                for (CTypeRef t : types) {
                    XmlElementWriter xew = xesw.value();
                    xew.name(t.getTagName().getLocalPart())
                       .namespace(t.getTagName().getNamespaceURI())
                       .type(resolve(t,IMPLEMENTATION));
                    if(t.getDefaultValue()!=null)
                        xew.defaultValue(t.getDefaultValue());
                }
            }
        }
    }

    /**
     * Annotate the attribute property 'field'
     */
    private void annotateAttribute(JAnnotatable field) {
        CAttributePropertyInfo ap = (CAttributePropertyInfo) prop;
        QName attName = ap.getXmlName();

        // [RESULT]
        // @XmlAttribute(name="foo", required=true, targetNamespace="bar://baz")
        XmlAttributeWriter xaw = field.annotate2(XmlAttributeWriter.class);
        xaw.name(attName.getLocalPart())
           .namespace(attName.getNamespaceURI())
           .required(ap.isRequired());
    }

    /**
     * Useful base class for implementing {@link FieldAccessor}.
     */
    protected abstract class Accessor implements FieldAccessor {

        /**
         * Evaluates to the target object this accessor should access.
         */
        protected final JExpression $target;
        
        protected Accessor( JExpression $target ) {
            this.$target = $target;
        }

        public final FieldOutline owner() {
            return AbstractField.this;
        }

        public final CPropertyInfo getPropertyInfo() {
            return prop;
        }
    }
    
    
//
//
//     utility methods
//
//

    /**
     * Generates the field declaration.
     */
    protected final JFieldVar generateField( JType type ) {
        return outline.implClass.field( JMod.PROTECTED, type, prop.getName(false) );
    }

    /**
     * Case from {@link #exposedType} to {@link #implType} if necessary.
     */
    protected final JExpression castToImplType( JExpression exp ) {
        if(implType==exposedType)
            return exp;
        else
            return JExpr.cast(implType,exp);
    }

    /**
     * Compute the type of a {@link CPropertyInfo}
     * @param aspect
     */
    protected JType getType(final Aspect aspect) {
        if(prop.getAdapter()!=null)
            return prop.getAdapter().customType.toType(outline.parent(),aspect);

        final class TypeList extends ArrayList<JType> {
            void add( CTypeInfo t ) {
                add( t.getType().toType(outline.parent(),aspect) );
                if(t instanceof CElementInfo) {
                    // UGLY. element substitution is implemented in a way that
                    // the derived elements are not assignable to base elements.
                    // so when we compute the signature, we have to take derived types
                    // into account
                    add( ((CElementInfo)t).getSubstitutionMembers());
                }
            }

            void add( Collection<? extends CTypeInfo> col ) {
                for (CTypeInfo typeInfo : col)
                    add(typeInfo);
            }
        }
        TypeList r = new TypeList();
        r.add(prop.ref());

        JType t = TypeUtil.getCommonBaseType(codeModel,r);

        // if item type is unboxable, convert t=Integer -> t=int
        // the in-memory data structure can't have primitives directly,
        // but this guarantees that items cannot legal hold null,
        // which helps us improve the boundary signature between our
        // data structure and user code
        if(prop.isUnboxable())
            t = t.boxify().getPrimitiveType();
        return t;
    }

    /**
     * Returns a string that represents a list of possible
     * objects that can be in the given field.
     */
    protected final String listPossibleTypes( CPropertyInfo prop ) {
        StringBuilder buf = new StringBuilder();

        for( CTypeInfo tt : prop.ref() ) {
            JType t = tt.getType().toType(outline.parent(),Aspect.EXPOSED);
            if( t.isPrimitive() || t.isArray() )
                buf.append(t.fullName());
            else
                buf.append("{@link "+t.fullName()+"}\n");
        }

        return buf.toString();
    }

    /**
     * return the Java type for the given type reference in the model.
     */
    private JType resolve(CTypeRef typeRef,Aspect a) {
        return outline.parent().resolve(typeRef,a);
    }

}

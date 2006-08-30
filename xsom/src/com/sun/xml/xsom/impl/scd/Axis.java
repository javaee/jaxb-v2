package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * TODO: One non-obvious axis we need is an axis that iterates all the descendants for '//'
 *
 * @param <T>
 *      The kind of components that this axis may return.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Axis<T extends XSComponent> {
    Iterator<T> iterator(XSComponent contextNode);

    public static final Axis<XSElementDecl> SUBSTITUTION_GROUP = new AbstractAxisImpl<XSElementDecl>() {
        public Iterator<XSElementDecl> elementDecl(XSElementDecl decl) {
            return singleton(decl.getSubstAffiliation());
        }
    };

    public static final Axis<XSAttributeDecl> ATTRIBUTE = new AbstractAxisImpl<XSAttributeDecl>() {
        public Iterator<XSAttributeDecl> complexType(XSComplexType type) {
            return attributeHolder(type);
        }

        public Iterator<XSAttributeDecl> attGroupDecl(XSAttGroupDecl decl) {
            return attributeHolder(decl);
        }

        private Iterator<XSAttributeDecl> attributeHolder(final XSAttContainer atts) {
            // TODO: check spec. is this correct?
            return new Iterators.Adapter<XSAttributeDecl,XSAttributeUse>(atts.iterateAttributeUses()) {
                protected XSAttributeDecl filter(XSAttributeUse u) {
                    return u.getDecl();
                }
            };
        }

        public Iterator<XSAttributeDecl> schema(XSSchema schema) {
            return schema.iterateAttributeDecls();
        }
    };

    public static final Axis<XSElementDecl> ELEMENT = new AbstractAxisImpl<XSElementDecl>() {
        public Iterator<XSElementDecl> particle(XSParticle particle) {
            return particle.getTerm().apply(this);
        }

        public Iterator<XSElementDecl> complexType(XSComplexType type) {
            XSParticle p = type.getContentType().asParticle();
            if(p!=null)     return particle(p);
            else            return empty();
        }

        public Iterator<XSElementDecl> schema(XSSchema schema) {
            return schema.iterateElementDecls();
        }

        public Iterator<XSElementDecl> modelGroupDecl(XSModelGroupDecl decl) {
            return modelGroup(decl.getModelGroup());
        }

        public Iterator<XSElementDecl> modelGroup(XSModelGroup group) {
            return new Iterators.Map<XSElementDecl,XSParticle>(group.iterator()) {
                protected Iterator<XSElementDecl> apply(XSParticle p) {
                    return particle(p);
                }
            };
        }

        public Iterator<XSElementDecl> elementDecl(XSElementDecl decl) {
            return singleton(decl);
        }
    };


    public static final Axis<XSType> TYPE_DEFINITION = new AbstractAxisImpl<XSType>() {
        public Iterator<XSType> simpleType(XSSimpleType type) {
            return singleton(type);
        }

        public Iterator<XSType> complexType(XSComplexType type) {
            return singleton(type);
        }
    };

    public static final Axis<XSType> BASETYPE = new AbstractAxisImpl<XSType>() {
        public Iterator<XSType> simpleType(XSSimpleType type) {
            return singleton(type.getBaseType());
        }

        public Iterator<XSType> complexType(XSComplexType type) {
            return singleton(type.getBaseType());
        }
    };

    public static final Axis<XSSimpleType> PRIMITIVE_TYPE = new AbstractAxisImpl<XSSimpleType>() {
        public Iterator<XSSimpleType> simpleType(XSSimpleType type) {
            return singleton(type.getPrimitiveType());
        }
    };

    public static final Axis<XSSimpleType> ITEM_TYPE = new AbstractAxisImpl<XSSimpleType>() {
        public Iterator<XSSimpleType> simpleType(XSSimpleType type) {
            XSListSimpleType baseList = type.getBaseListType();
            if(baseList==null)      return empty();
            return singleton(baseList.getItemType());
        }
    };

    public static final Axis<XSSimpleType> MEMBER_TYPE = new AbstractAxisImpl<XSSimpleType>() {
        public Iterator<XSSimpleType> simpleType(XSSimpleType type) {
            XSUnionSimpleType baseUnion = type.getBaseUnionType();
            if(baseUnion ==null)      return empty();
            return baseUnion.iterator();
        }
    };

    public static final Axis<XSElementDecl> SCOPE = new AbstractAxisImpl<XSElementDecl>() {
        public Iterator<XSElementDecl> complexType(XSComplexType type) {
            return singleton(type.getScope());
        }
    };

    // TODO
    public static final Axis ATTRIBUTE_GROUP = null;
    public static final Axis MODEL_GROUP_DECL = null;
    public static final Axis IDENTITY_CONSTRAINT = null;
    public static final Axis REFERENCED_KEY = null;
    public static final Axis NOTATION = null;
    public static final Axis WILDCARD = null;
    public static final Axis FACET = null;
}

package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;

import java.util.List;
import java.util.ArrayList;

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
    List<T> iterator(XSComponent contextNode);

    public static final Axis<XSElementDecl> SUBSTITUTION_GROUP = new AbstractAxisImpl<XSElementDecl>() {
        public List<XSElementDecl> elementDecl(XSElementDecl decl) {
            return singleton(decl.getSubstAffiliation());
        }
    };

    public static final Axis<XSAttributeDecl> ATTRIBUTE = new AbstractAxisImpl<XSAttributeDecl>() {
        public List<XSAttributeDecl> complexType(XSComplexType type) {
            return attributeHolder(type);
        }

        public List<XSAttributeDecl> attGroupDecl(XSAttGroupDecl decl) {
            return attributeHolder(decl);
        }

        private List<XSAttributeDecl> attributeHolder(XSAttContainer atts) {
            // TODO: check spec. is this correct?
            List<XSAttributeDecl> r = new ArrayList<XSAttributeDecl>();
            for (XSAttributeUse use : atts.getAttributeUses()) {
                r.add(use.getDecl());
            }
            return r;
        }

        public List<XSAttributeDecl> schema(XSSchema schema) {
            return new ArrayList<XSAttributeDecl>(schema.getAttributeDecls().values());
        }
    };

    public static final Axis<XSElementDecl> ELEMENT = new AbstractAxisImpl<XSElementDecl>() {
        public List<XSElementDecl> particle(XSParticle particle) {
            return particle.getTerm().apply(this);
        }

        public List<XSElementDecl> complexType(XSComplexType type) {
            XSParticle p = type.getContentType().asParticle();
            if(p!=null)     return particle(p);
            else            return empty();
        }

        public List<XSElementDecl> schema(XSSchema schema) {
            return new ArrayList<XSElementDecl>(schema.getElementDecls().values());
        }

        public List<XSElementDecl> modelGroupDecl(XSModelGroupDecl decl) {
            return modelGroup(decl.getModelGroup());
        }

        public List<XSElementDecl> modelGroup(XSModelGroup group) {
            List<XSElementDecl> l = new ArrayList<XSElementDecl>();
            for( XSParticle p : group.getChildren() )
                l.addAll(particle(p));
            return l;
        }

        public List<XSElementDecl> elementDecl(XSElementDecl decl) {
            return singleton(decl);
        }
    };


    public static final Axis<XSType> TYPE_DEFINITION = new AbstractAxisImpl<XSType>() {
        public List<XSType> simpleType(XSSimpleType type) {
            return singleton(type);
        }

        public List<XSType> complexType(XSComplexType type) {
            return singleton(type);
        }
    };

    public static final Axis<XSType> BASETYPE = new AbstractAxisImpl<XSType>() {
        public List<XSType> simpleType(XSSimpleType type) {
            return singleton(type.getBaseType());
        }

        public List<XSType> complexType(XSComplexType type) {
            return singleton(type.getBaseType());
        }
    };

    public static final Axis<XSSimpleType> PRIMITIVE_TYPE = new AbstractAxisImpl<XSSimpleType>() {
        public List<XSSimpleType> simpleType(XSSimpleType type) {
            return singleton(type.getPrimitiveType());
        }
    };

    public static final Axis<XSSimpleType> ITEM_TYPE = new AbstractAxisImpl<XSSimpleType>() {
        public List<XSSimpleType> simpleType(XSSimpleType type) {
            XSListSimpleType baseList = type.getBaseListType();
            if(baseList==null)      return empty();
            return singleton(baseList.getItemType());
        }
    };

    public static final Axis<XSSimpleType> MEMBER_TYPE = new AbstractAxisImpl<XSSimpleType>() {
        public List<XSSimpleType> simpleType(XSSimpleType type) {
            XSUnionSimpleType baseUnion = type.getBaseUnionType();
            if(baseUnion ==null)      return empty();
            List<XSSimpleType> r = new ArrayList<XSSimpleType>();
            for (XSSimpleType m : baseUnion)
                r.add(m);
            return r;
        }
    };

    public static final Axis<XSElementDecl> SCOPE = new AbstractAxisImpl<XSElementDecl>() {
        public List<XSElementDecl> complexType(XSComplexType type) {
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

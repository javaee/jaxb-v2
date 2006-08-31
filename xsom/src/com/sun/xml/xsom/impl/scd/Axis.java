package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSModelGroup.Compositor;

import java.util.Iterator;

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
            return singleton(particle.getTerm().asElementDecl());
        }

        public Iterator<XSElementDecl> schema(XSSchema schema) {
            return schema.iterateElementDecls();
        }

        public Iterator<XSElementDecl> modelGroupDecl(XSModelGroupDecl decl) {
            return modelGroup(decl.getModelGroup());
        }

        //public Iterator<XSElementDecl> modelGroup(XSModelGroup group) {
        //    return new Iterators.Map<XSElementDecl,XSParticle>(group.iterator()) {
        //        protected Iterator<XSElementDecl> apply(XSParticle p) {
        //            return particle(p);
        //        }
        //    };
        //}
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

    public static final Axis<XSComponent> SCOPE = new AbstractAxisImpl<XSComponent>() {
        public Iterator<XSComponent> complexType(XSComplexType type) {
            return singleton(type.getScope());
        }
        // TODO: attribute declaration has a scope, too.
        // TODO: element declaration has a scope
    };

    public static final Axis<XSAttGroupDecl> ATTRIBUTE_GROUP = new AbstractAxisImpl<XSAttGroupDecl>() {
        public Iterator<XSAttGroupDecl> schema(XSSchema schema) {
            return schema.iterateAttGroupDecls();
        }
    };

    public static final Axis<XSModelGroupDecl> MODEL_GROUP_DECL = new AbstractAxisImpl<XSModelGroupDecl>() {
        public Iterator<XSModelGroupDecl> schema(XSSchema schema) {
            return schema.iterateModelGroupDecls();
        }

        public Iterator<XSModelGroupDecl> particle(XSParticle particle) {
            return singleton(particle.getTerm().asModelGroupDecl());
        }
    };

    public static final Axis<XSIdentityConstraint> IDENTITY_CONSTRAINT = new AbstractAxisImpl<XSIdentityConstraint>() {
        public Iterator<XSIdentityConstraint> elementDecl(XSElementDecl decl) {
            return decl.getIdentityConstraints().iterator();
        }

        public Iterator<XSIdentityConstraint> schema(XSSchema schema) {
            // TODO: iterate all elements in this schema (local or global!) and its identity constraints
            return super.schema(schema);
        }
    };

    public static final Axis<XSIdentityConstraint> REFERENCED_KEY = new AbstractAxisImpl<XSIdentityConstraint>() {
        public Iterator<XSIdentityConstraint> identityConstraint(XSIdentityConstraint decl) {
            return singleton(decl.getReferencedKey());
        }
    };

    public static final Axis<XSNotation> NOTATION = new AbstractAxisImpl<XSNotation>() {
        public Iterator<XSNotation> schema(XSSchema schema) {
            return schema.iterateNotations();
        }
    };

    public static final Axis<XSWildcard> WILDCARD = new AbstractAxisImpl<XSWildcard>() {
        public Iterator<XSWildcard> particle(XSParticle particle) {
            return singleton(particle.getTerm().asWildcard());
        }
    };

    public static final Axis<XSWildcard> ATTRIBUTE_WILDCARD = new AbstractAxisImpl<XSWildcard>() {
        public Iterator<XSWildcard> complexType(XSComplexType type) {
            return singleton(type.getAttributeWildcard());
        }

        public Iterator<XSWildcard> attGroupDecl(XSAttGroupDecl decl) {
            return singleton(decl.getAttributeWildcard());
        }
    };

    public static final Axis<XSFacet> FACET = new AbstractAxisImpl<XSFacet>() {
        public Iterator<XSFacet> simpleType(XSSimpleType type) {
            // TODO: it's not clear if "facets" mean all inherited facets or just declared facets
            XSRestrictionSimpleType r = type.asRestriction();
            if(r!=null)
                return r.iterateDeclaredFacets();
            else
                return empty();
        }
    };

    public static final Axis<XSModelGroup> MODELGROUP_ALL = new ModelGroupAxis(Compositor.ALL);
    public static final Axis<XSModelGroup> MODELGROUP_CHOICE = new ModelGroupAxis(Compositor.CHOICE);
    public static final Axis<XSModelGroup> MODELGROUP_SEQUENCE = new ModelGroupAxis(Compositor.SEQUENCE);
    public static final Axis<XSModelGroup> MODELGROUP_ANY = new ModelGroupAxis(null);

    static final class ModelGroupAxis extends AbstractAxisImpl<XSModelGroup> {
        private final XSModelGroup.Compositor compositor;

        ModelGroupAxis(Compositor compositor) {
            this.compositor = compositor;
        }

        public Iterator<XSModelGroup> particle(XSParticle particle) {
            return filter(particle.getTerm().asModelGroup());
        }

        public Iterator<XSModelGroup> modelGroupDecl(XSModelGroupDecl decl) {
            return filter(decl.getModelGroup());
        }

        private Iterator<XSModelGroup> filter(XSModelGroup mg) {
            if(mg.getCompositor() == compositor || compositor == null)
                return singleton(mg);
            else
                return empty();
        }


    }
}

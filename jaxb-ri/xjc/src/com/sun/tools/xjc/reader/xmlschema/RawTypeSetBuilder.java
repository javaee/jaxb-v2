package com.sun.tools.xjc.reader.xmlschema;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.Ring;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;

/**
 * Builds {@link RawTypeSet} for XML Schema.
 *
 * @author Kohsuke Kawaguchi
 */
public class RawTypeSetBuilder implements XSTermFunction<Multiplicity> {
    /**
     * @param optional
     *      if this whole property is optional due to the
     *      occurence constraints on ancestors, set this to true.
     *      this will prevent the primitive types to be generated.
     */
    public static RawTypeSet build( XSParticle p, boolean optional ) {
        RawTypeSetBuilder rtsb = new RawTypeSetBuilder();
        Multiplicity mul = rtsb.particle(p);

        if(optional)
            mul = mul.makeOptional();

        return new RawTypeSet(rtsb.refs,mul);
    }


    /**
     * To avoid declaring the same element twice for a content model like
     * (A,A), we keep track of element names here while we are building up
     * this instance.
     */
    private final Set<QName> elementNames = new HashSet<QName>();

    private final Set<RawTypeSet.Ref> refs = new HashSet<RawTypeSet.Ref>();

    private RawTypeSetBuilder() {

    }

    /**
     * Build up {@link #refs} and compute the total multiplicity of this {@link RawTypeSet.Ref} set.
     */
    private Multiplicity particle( XSParticle p ) {
        Multiplicity m = p.getTerm().apply(this);

        Integer max;
        if(m.max==null || p.getMaxOccurs()==XSParticle.UNBOUNDED)
            max=null;
        else
            max=p.getMaxOccurs();

        return Multiplicity.multiply( m, Multiplicity.create(p.getMinOccurs(),max) );
    }

    public Multiplicity wildcard(XSWildcard wc) {
        refs.add(new WildcardRef(wc));
        return Multiplicity.one;
//
//        // TODO: implement this method later
//        TODO.prototype();
//
//        Ring.get(ErrorReceiver.class).warning( new SAXParseException(
//            "Wildcard is not supported", wc.getLocator() ));
//
//        return Multiplicity.zero;
    }

    public Multiplicity modelGroupDecl(XSModelGroupDecl decl) {
        return modelGroup(decl.getModelGroup());
    }

    public Multiplicity modelGroup(XSModelGroup group) {
        boolean isChoice = group.getCompositor() == XSModelGroup.CHOICE;

        Multiplicity r = Multiplicity.zero;

        for( XSParticle p : group.getChildren()) {
            Multiplicity m = particle(p);

            if(r==null) {
                r=m;
                continue;
            }
            if(isChoice) {
                r = Multiplicity.choice(r,m);
            } else {
                r = Multiplicity.group(r,m);
            }
        }
        return r;
    }

    public Multiplicity elementDecl(XSElementDecl decl) {

        QName n = new QName(decl.getTargetNamespace(),decl.getName());
        if(elementNames.add(n)) {
            CElement elementBean = Ring.get(ClassSelector.class).bindToType(decl);
            if(elementBean==null)
                refs.add(new RawTypeSet.XmlTypeRef(decl));
            else
                refs.add(new ElementClassRef(decl,elementBean));
        }

        return Multiplicity.one;
    }

    /**
     * Reference to a wildcard.
     */
    public static final class WildcardRef extends RawTypeSet.Ref {
        private final XSWildcard wildcard;

        WildcardRef(XSWildcard wildcard) {
            this.wildcard = wildcard;
        }

        private WildcardMode getMode() {
            switch(wildcard.getMode()) {
            case XSWildcard.LAX:
                return WildcardMode.LAX;
            case XSWildcard.STRTICT:
                return WildcardMode.STRICT;
            case XSWildcard.SKIP:
                return WildcardMode.SKIP;
            default:
                throw new IllegalStateException();
            }
        }

        protected CTypeRef toTypeRef(CElementPropertyInfo ep) {
            // we don't allow a mapping to typeRef if the wildcard is present
            throw new IllegalStateException();
        }

        protected void toElementRef(CReferencePropertyInfo prop) {
            prop.setWildcard(getMode());
        }

        protected boolean canBeType(RawTypeSet parent) {
            return false;
        }

        protected boolean isListOfValues() {
            return false;
        }
    }

    /**
     * Reference to a class that maps from an element.
     */
    public static final class ElementClassRef extends RawTypeSet.Ref {
        public final CElement target;
        public final XSElementDecl decl;

        ElementClassRef(XSElementDecl decl, CElement target) {
            this.decl = decl;
            this.target = target;
        }

        protected CTypeRef toTypeRef(CElementPropertyInfo ep) {
            if (target instanceof CClassInfo) {
                CClassInfo ci = (CClassInfo) target;
                return new CTypeRef(ci,target.getElementName(),decl.isNillable(),decl.getDefaultValue());
            } else {
                CElementInfo ei = (CElementInfo) target;
                assert !target.isCollection();
                CAdapter a = ei.getProperty().getAdapter();
                if(a!=null && ep!=null) ep.setAdapter(a);

                return new CTypeRef(ei.getContentType(),target.getElementName(),decl.isNillable(),decl.getDefaultValue());
            }
        }

        protected void toElementRef(CReferencePropertyInfo prop) {
            prop.getElements().add(target);
        }

        protected boolean canBeType(RawTypeSet parent) {
            // if element substitution can occur, no way it can be mapped to a list of types
            if(decl.getSubstitutables().size()>1)
                return false;

            if (target instanceof CElementInfo) {
                CElementInfo ei = (CElementInfo) target;
                // we have no place to put an adater if this thing maps to a type
                CElementPropertyInfo p = ei.getProperty();
                if(p.getAdapter()!=null && (parent.refs.size()>1 || !parent.mul.isAtMostOnce()))
                    return false;
            }

            return true;
        }

        protected boolean isListOfValues() {
            if (target instanceof CElementInfo) {
                CElementInfo ei = (CElementInfo) target;
                return ei.getProperty().isValueList();
            }
            return false;
        }
    }
}

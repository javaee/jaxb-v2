package com.sun.tools.xjc.reader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.MimeType;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XmlString;

import org.xml.sax.Locator;

import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT;
import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.NOT_REPEATED;
import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.REPEATED_VALUE;

/**
 * Set of {@link Ref}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class RawTypeSet {


    public final Set<Ref> refs;

    /**
     * True if this type set can form references to types.
     */
    public final boolean canBeTypeRefs;

    /**
     * The occurence of the whole references.
     */
    public final Multiplicity mul;

    // computed inside canBeTypeRefs()
    private CElementPropertyInfo.CollectionMode collectionMode;

    /**
     * Should be called from one of the raw type set builders.
     */
    public RawTypeSet( Set<Ref> refs, Multiplicity m ) {
        this.refs = refs;
        mul = m;
        canBeTypeRefs = canBeTypeRefs();
    }

    public CElementPropertyInfo.CollectionMode getCollectionMode() {
        return collectionMode;
    }

    public boolean isRequired() {
        return !mul.isOptional();
    }

    /**
     * Returns true if {@link #refs} can form refs of types.
     *
     * If there are multiple {@link Ref}s with the same type,
     * we cannot make them into type refs. Or if any of the {@link Ref}
     * says they cannot be in type refs, we cannot do that either.
     *
     * TODO: just checking if the refs are the same is not suffice.
     * If two refs derive from each other, they cannot form a list of refs
     * (because of a possible ambiguity).
     */
    private boolean canBeTypeRefs() {
        Set<NType> types = new HashSet<NType>();

        collectionMode = mul.isAtMostOnce()?NOT_REPEATED:REPEATED_ELEMENT;

        for( Ref r : refs ) {
            if(!r.canBeType(this))
                return false;   // veto
            if(!types.add(r.toTypeRef(null).getTarget().getType()))
                return false;   // collision
            if(r.isListOfValues()) {
                if(refs.size()>1 || !mul.isAtMostOnce())
                    return false;   // restriction on @XmlList
                collectionMode = REPEATED_VALUE;
            }
        }
        return true;
    }




    public void addTo(CElementPropertyInfo prop) {
        assert canBeTypeRefs;
        if(mul.isZero())
            return; // the property can't have any value

        List<CTypeRef> dst = prop.getTypes();
        for( Ref t : refs )
            dst.add(t.toTypeRef(prop));
    }

    public void addTo(CReferencePropertyInfo prop) {
        if(mul.isZero())
            return; // the property can't have any value
        for( Ref t : refs )
            t.toElementRef(prop);
    }

    public ID id() {
        for( Ref t : refs ) {
            ID id = t.id();
            if(id!=ID.NONE)    return id;
        }
        return ID.NONE;
    }

    public MimeType getExpectedMimeType() {
        for( Ref t : refs ) {
            MimeType mt = t.getExpectedMimeType();
            if(mt!=null)    return mt;
        }
        return null;
    }


    /**
     * A reference to something.
     *
     * <p>
     * A {@link Ref} can be either turned into {@link CTypeRef} to form
     * an element property, or {@link Element} to form a reference property.
     */
    public static abstract class Ref {
        /**
         * @param ep
         *      the property to which the returned {@link CTypeRef} will be
         *      added to.
         */
        protected abstract CTypeRef toTypeRef(CElementPropertyInfo ep);
        protected abstract void toElementRef(CReferencePropertyInfo prop);
        /**
         * Can this {@link Ref} be a type ref?
         * @return false to veto.
         * @param parent
         */
        protected abstract boolean canBeType(RawTypeSet parent);
        protected abstract boolean isListOfValues();
        /**
         * When this {@link RawTypeSet} binds to a {@link CElementPropertyInfo},
         * this method is used to determine if the property is ID or not.
         */
        protected abstract ID id();

        /**
         * When this {@link RawTypeSet} binds to a {@link CElementPropertyInfo},
         * this method is used to determine if the property has an associated expected MIME type or not.
         */
        protected MimeType getExpectedMimeType() { return null; }
    }

    /**
     * References to a type. Could be global or local.
     */
    public static final class XmlTypeRef extends Ref {
        public final QName elementName;
        public final TypeUse target;
        public final Locator locator;
        public final XSComponent source;
        public final CCustomizations custs;
        public final boolean nillable;
        public final XmlString defaultValue;

        public XmlTypeRef(QName elementName, TypeUse target, boolean nillable, XmlString defaultValue, XSComponent source, CCustomizations custs, Locator loc) {
            assert elementName!=null;
            assert target!=null;

            this.elementName = elementName;
            this.target = target;
            this.source = source;
            this.custs = custs;
            this.nillable = nillable;
            this.defaultValue = defaultValue;
            this.locator = loc;
        }

        public XmlTypeRef(QName elementName, XSType target, boolean nillable, XmlString defaultValue) {
            this(elementName,Ring.get(ClassSelector.class).bindToType(target), nillable, defaultValue, target,
                    Ring.get(BGMBuilder.class).getBindInfo(target).toCustomizationList(),
                    target.getLocator());
        }

        public XmlTypeRef(XSElementDecl decl) {
            this(new QName(decl.getTargetNamespace(),decl.getName()),bindToType(decl),
                    decl.isNillable(), decl.getDefaultValue(), decl,
                    Ring.get(BGMBuilder.class).getBindInfo(decl).toCustomizationList(),
                    decl.getLocator());
        }

        protected CTypeRef toTypeRef(CElementPropertyInfo ep) {
            if(ep!=null && target.getAdapterUse()!=null)
                ep.setAdapter(target.getAdapterUse());
            return new CTypeRef((CNonElement)target.getInfo(),elementName,nillable,defaultValue);
        }

        /**
         * The whole type set can be later bound to a reference property,
         * in which case we need to generate additional code to wrap this
         * type reference into an element class.
         *
         * This method generates such an element class and returns it.
         */
        protected void toElementRef(CReferencePropertyInfo prop) {
            CClassInfo scope = Ring.get(ClassSelector.class).getCurrentBean();
            Model model = Ring.get(Model.class);

            if(target instanceof CClassInfo && Ring.get(BIGlobalBinding.class).isSimpleMode()) {
                CClassInfo bean = new CClassInfo(model,scope,
                                model.getNameConverter().toClassName(elementName.getLocalPart()),
                                locator,null,elementName,source,custs);
                bean.setBaseClass((CClassInfo)target);
                prop.getElements().add(bean);
            } else {
                CElementInfo e = new CElementInfo(model,elementName,scope,target,defaultValue,source,custs,locator);
                prop.getElements().add(e);
            }
        }

        protected boolean canBeType(RawTypeSet parent) {
            // if we have an adapter or IDness, which requires special
            // annotation, and there's more than one element,
            // we have no place to put the special annotation, so we need JAXBElement.
            if(parent.refs.size()>1 || !parent.mul.isAtMostOnce()) {
                if(target.getAdapterUse()!=null || target.idUse()!=ID.NONE)
                    return false;
            }

            // nillable and optional at the same time. needs an element wrapper to distinguish those
            // two states.
            if(nillable && parent.mul.isOptional())
                return false;

            return true;
        }

        protected boolean isListOfValues() {
            return target.isCollection();
        }

        protected ID id() {
            return target.idUse();
        }

        protected MimeType getExpectedMimeType() {
            return target.getExpectedMimeType();
        }
    }

    private static TypeUse bindToType(XSElementDecl decl) {
        SimpleTypeBuilder stb = Ring.get(SimpleTypeBuilder.class);
        stb.refererStack.push(decl);
        TypeUse r = Ring.get(ClassSelector.class).bindToType(decl.getType());
        stb.refererStack.pop();
        return r;
    }
}

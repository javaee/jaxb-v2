package com.sun.xml.bind.v2.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * @author Kohsuke Kawaguchi
 */
class ReferencePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ReferencePropertyInfo<TypeT,ClassDeclT>
{
    /**
     * Lazily computed.
     * @see #getElements()
     */
    private Set<Element<TypeT,ClassDeclT>> types;

    private final boolean isMixed;

    private final WildcardMode wildcard;
    private final ClassDeclT domHandler;


    public ReferencePropertyInfoImpl(
        ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> classInfo,
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed) {

        super(classInfo, seed);

        isMixed = seed.readAnnotation(XmlMixed.class)!=null;

        XmlAnyElement xae = seed.readAnnotation(XmlAnyElement.class);
        if(xae==null) {
            wildcard = null;
            domHandler = null;
        } else {
            wildcard = xae.lax()?WildcardMode.LAX:WildcardMode.SKIP;
            domHandler = nav().asDecl(reader().getClassValue(xae,"value"));
        }
    }

    public Set<? extends Element<TypeT,ClassDeclT>> ref() {
        return getElements();
    }

    public PropertyKind kind() {
        return PropertyKind.REFERENCE;
    }

    public Set<? extends Element<TypeT,ClassDeclT>> getElements() {
        if(types==null)
            calcTypes();
        assert types!=null;
        return types;
    }

    /**
     * Compute {@link #types}.
     */
    private void calcTypes() {
        XmlElementRef[] ann;
        types = new HashSet<Element<TypeT,ClassDeclT>>();
        XmlElementRefs refs = seed.readAnnotation(XmlElementRefs.class);
        XmlElementRef ref = seed.readAnnotation(XmlElementRef.class);

        if(refs!=null && ref!=null) {
            // TODO: report an error
        }

        if(refs!=null)
            ann = refs.value();
        else {
            if(ref!=null)
                ann = new XmlElementRef[]{ref};
            else
                ann = null;
        }

        if(ann!=null) {
            Navigator<TypeT,ClassDeclT,FieldT,MethodT> nav = nav();
            AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> reader = reader();

            final TypeT defaultType = nav.ref(XmlElementRef.DEFAULT.class);
            final ClassDeclT je = nav.asDecl(JAXBElement.class);

            for( XmlElementRef r : ann ) {
                TypeT type = reader.getClassValue(r,"type");
                if( type.equals(defaultType) )
                    type = nav.erasure(_getType());
                if(nav.getBaseClass(type,je)!=null)
                    addGenericElement(r);
                else
                    addAllSubtypes(type);
            }
        }

        types = Collections.unmodifiableSet(types);
    }

    private void addGenericElement(XmlElementRef r) {
        String nsUri = r.namespace();
        if(nsUri.length()==0)
            nsUri = parent.builder.defaultNsUri;
        // TODO: check spec. defaulting of localName.
        addGenericElement(parent.owner.getElementInfo(parent.getClazz(),new QName(nsUri,r.name())));
    }

    private void addGenericElement(ElementInfo<TypeT,ClassDeclT> ei) {
        if(ei==null)
            // this can happen when we don't have the whole TypeInfos.
            return;
        types.add(ei);
        for( ElementInfo<TypeT,ClassDeclT> subst : ei.getSubstitutionMembers() )
            addGenericElement(subst);
    }

    private void addAllSubtypes(TypeT type) {
        Navigator<TypeT,ClassDeclT,FieldT,MethodT> nav = nav();

        // this allows the explicitly referenced type to be sucked in to the model
        NonElement<TypeT,ClassDeclT> t = parent.builder.getClassInfo(nav.asDecl(type),this);
        if(!(t instanceof ClassInfo))
            // this is leaf.
            return;

        ClassInfo<TypeT,ClassDeclT> c = (ClassInfo<TypeT,ClassDeclT>) t;
        if(c!=null && c.isElement())
            types.add(c.asElement());

        // look for other possible types
        for( ClassInfo<TypeT,ClassDeclT> ci : parent.owner.beans().values() ) {
            if(ci.isElement() && nav.isSubClassOf(ci.getType(),type))
                types.add(ci.asElement());
        }

        // don't allow local elements to substitute.
        for( ElementInfo<TypeT,ClassDeclT> ei : parent.owner.getElementMappings(null).values()) {
            if(nav.isSubClassOf(ei.getType(),type))
                types.add(ei);
        }
    }


    protected void link() {
        super.link();

        // until we get the whole thing into TypeInfoSet,
        // we never really know what are all the possible types that can be assigned on this field.
        // so recompute this value when we have all the information.
        calcTypes();

    }

    public QName getXmlName() {
        // TODO: define a new annotation. still under discussion with Sekhar.
        TODO.prototype();
        return null;
    }

    public final boolean isMixed() {
        return isMixed;
    }

    public final WildcardMode getWildcard() {
        return wildcard;
    }

    public final ClassDeclT getDOMHandler() {
        return domHandler;
    }
}

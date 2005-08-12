package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.FinalArrayList;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;

/**
 * {@link ElementInfo} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
class ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends TypeInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ElementInfo<TypeT,ClassDeclT> {

    private final QName tagName;

    private final NonElement<TypeT,ClassDeclT> contentType;

    private final TypeT elementType;

    private final ClassInfo<TypeT,ClassDeclT> scope;

    /**
     * Annotation that controls the binding.
     */
    private final XmlElementDecl anno;

    /**
     * If this element can substitute another element, the element name.
     * @see #link()
     */
    private ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> substitutionHead;

    /**
     * Lazily constructed list of {@link ElementInfo}s that can substitute this element.
     * This could be null.
     * @see #link()
     */
    private FinalArrayList<ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> substitutionMembers;

    /**
     * The factory method from which this mapping was created.
     */
    private final MethodT method;

    /**
     * If the content type is adapter, return that adapter.
     */
    private final Adapter<TypeT,ClassDeclT> adapter;

    private final boolean isCollection;

    private final ID id;

    private final PropertyImpl property;
    private final MimeType expectedMimeType;
    private final boolean inlineBinary;
    private final QName schemaType;

    /**
     * Singleton instance of {@link ElementPropertyInfo} for this element.
     */
    protected class PropertyImpl implements
            ElementPropertyInfo<TypeT,ClassDeclT>,
            TypeRef<TypeT,ClassDeclT>,
            AnnotationSource {
        //
        // TypeRef impl
        //
        public NonElement getTarget() {
            return contentType;
        }
        public QName getTagName() {
            return tagName;
        }

        public List<? extends TypeRef<TypeT,ClassDeclT>> getTypes() {
            return Collections.singletonList(this);
        }

        public List<? extends NonElement<TypeT,ClassDeclT>> ref() {
            return Collections.singletonList(contentType);
        }

        public QName getXmlName() {
            return tagName;
        }

        public boolean isCollectionNillable() {
            return true;
        }

        public boolean isNillable() {
            return true;
        }

        public String getDefaultValue() {
            String v = anno.defaultValue();
            if(v.equals("\u0000"))
                return null;
            else
                return v;
        }

        public ElementInfoImpl parent() {
            return ElementInfoImpl.this;
        }

        public String getName() {
            return "value";
        }

        public String displayName() {
            return "JAXBElement#value";
        }

        public boolean isCollection() {
            return isCollection;
        }

        /**
         * For {@link ElementInfo}s, a collection always means a list of values.
         */
        public boolean isValueList() {
            return isCollection;
        }

        public boolean isRequired() {
            return true;
        }

        public PropertyKind kind() {
            return PropertyKind.ELEMENT;
        }

        public Adapter<TypeT,ClassDeclT> getAdapter() {
            return adapter;
        }

        public ID id() {
            return id;
        }

        public MimeType getExpectedMimeType() {
            return expectedMimeType;
        }

        public QName getSchemaType() {
            return schemaType;
        }

        public boolean inlineBinaryData() {
            return inlineBinary;
        }

        public PropertyInfo<TypeT,ClassDeclT> getSource() {
            return this;
        }

        //
        //
        // AnnotationSource impl
        //
        //
        public <A extends Annotation> A readAnnotation(Class<A> annotationType) {
            return reader().getMethodAnnotation(annotationType,method,ElementInfoImpl.this);
        }

        public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
            return reader().hasMethodAnnotation(annotationType,method);
        }
    };

    /**
     * @param m
     *      The factory method on ObjectFactory that comes with {@link XmlElementDecl}.
     */
    public ElementInfoImpl(ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> builder,
        RegistryInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> registry, MethodT m ) throws IllegalAnnotationException {
        super(builder,registry);

        this.method = m;
        anno = reader().getMethodAnnotation( XmlElementDecl.class, m, this );
        assert anno!=null;  // the caller should check this
        assert anno instanceof Locatable;

        elementType = nav().getReturnType(m);
        TypeT baseClass = nav().getBaseClass(elementType,nav().asDecl(JAXBElement.class));
        if(baseClass==null)
            throw new IllegalAnnotationException(
                Messages.XML_ELEMENT_MAPPING_ON_NON_IXMLELEMENT_METHOD.format(nav().getMethodName(m)),
                anno );

        tagName = parseElementName(anno);

        // adapter
        Adapter<TypeT,ClassDeclT> a = null;
        if(nav().getMethodParameters(m).length>0) {
            XmlJavaTypeAdapter adapter = reader().getMethodAnnotation(XmlJavaTypeAdapter.class,m,this);
            if(adapter!=null)
                a = new Adapter<TypeT,ClassDeclT>(adapter,reader(),nav());
            else {
                XmlAttachmentRef xsa = reader().getMethodAnnotation(XmlAttachmentRef.class,m,this);
                if(xsa!=null) {
                    TODO.prototype("in APT swaRefAdapter isn't avaialble, so this returns null");
                    a = new Adapter<TypeT,ClassDeclT>(owner.nav.asDecl(SwaRefAdapter.class),owner.nav);
                }
            }
        }
        this.adapter = a;

        if(adapter==null) {
            // T of JAXBElement<T>
            TypeT typeType = nav().getTypeArgument(baseClass,0);
            TypeT list = nav().getBaseClass(typeType,nav().asDecl(List.class));
            if(list==null) {
                isCollection = false;
                contentType = builder.getTypeInfo(typeType,this);  // suck this type into the current set.
            } else {
                isCollection = true;
                contentType = builder.getTypeInfo(nav().getTypeArgument(list,0),this);
            }
        } else {
            // but if adapted, use the adapted type
            contentType = builder.getTypeInfo(this.adapter.defaultType,this);
            isCollection = false;
        }

        // scope
        TypeT s = reader().getClassValue(anno,"scope");
        if(s.equals(nav().ref(XmlElementDecl.GLOBAL.class)))
            scope = null;
        else {
            // TODO: what happens if there's an error?
            NonElement<TypeT,ClassDeclT> scp = builder.getClassInfo(nav().asDecl(s),this);
            if(!(scp instanceof ClassInfo)) {
                throw new IllegalAnnotationException(
                    Messages.SCOPE_IS_NOT_COMPLEXTYPE.format(nav().getTypeName(s)),
                    anno );
            }
            scope = (ClassInfo<TypeT,ClassDeclT>)scp;
        }

        id = calcId();

        property = createPropertyImpl();

        this.expectedMimeType = Util.calcExpectedMediaType(property,builder);
        this.inlineBinary = reader().hasMethodAnnotation(XmlInlineBinaryData.class,method);
        this.schemaType = Util.calcSchemaType(reader(),property,registry.registryClass,
                nav().asDecl(getContentInMemoryType()),this);
    }

    final QName parseElementName(XmlElementDecl e) {
        String local = e.name();
        String nsUri = e.namespace();
        if(nsUri.equals("##default")) {
            // if defaulted ...
            XmlSchema xs = reader().getPackageAnnotation(XmlSchema.class,
                nav().getDeclaringClassForMethod(method),this);
            if(xs!=null)
                nsUri = xs.namespace();
            else {
                nsUri = builder.defaultNsUri;
            }
        }

        return new QName(nsUri.intern(),local.intern());
    }

    protected PropertyImpl createPropertyImpl() {
        return new PropertyImpl();
    }

    public ElementPropertyInfo<TypeT,ClassDeclT> getProperty() {
        return property;
    }

    public NonElement<TypeT,ClassDeclT> getContentType() {
        return contentType;
    }

    public TypeT getContentInMemoryType() {
        if(adapter==null) {
            return contentType.getType();
        } else {
            return adapter.customType;
        }
    }

    public QName getElementName() {
        return tagName;
    }

    public TypeT getType() {
        return elementType;
    }

    private ID calcId() {
        // TODO: share code with PropertyInfoImpl
        if(reader().hasMethodAnnotation(XmlID.class,method)) {
            return ID.ID;
        } else
        if(reader().hasMethodAnnotation(XmlIDREF.class,method)) {
            return ID.IDREF;
        } else {
            return ID.NONE;
        }
    }

    public ClassInfo<TypeT, ClassDeclT> getScope() {
        return scope;
    }

    public ElementInfo<TypeT,ClassDeclT> getSubstitutionHead() {
        return substitutionHead;
    }

    public Collection<? extends ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>> getSubstitutionMembers() {
        if(substitutionMembers==null)
            return Collections.emptyList();
        else
            return substitutionMembers;
    }

    /**
     * Called after all the {@link TypeInfo}s are collected into the {@link #owner}.
     */
    /*package*/ void link() {
        // substitution head
        if(anno.substitutionHeadName().length()!=0) {
            QName name = new QName(
                anno.substitutionHeadNamespace(), anno.substitutionHeadName() );
            substitutionHead = owner.getElementInfo(null,name);
            if(substitutionHead==null) {
                builder.reportError(
                    new IllegalAnnotationException(Messages.NON_EXISTENT_ELEMENT_MAPPING.format(
                        name.getNamespaceURI(),name.getLocalPart()), anno));
                // recover by ignoring this substitution declaration
            } else
                substitutionHead.addSubstitutionMember(this);
        } else
            substitutionHead = null;
        super.link();
    }

    private void addSubstitutionMember(ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> child) {
        if(substitutionMembers==null)
            substitutionMembers = new FinalArrayList<ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>>();
        substitutionMembers.add(child);
    }

    public Location getLocation() {
        return nav().getMethodLocation(method);
    }
}

package com.sun.xml.bind.v2.model.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.RegistryInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;


/**
 * Builds a {@link TypeInfoSet} (a set of JAXB properties)
 * by using {@link ElementInfoImpl} and {@link ClassInfoImpl}.
 * from annotated Java classes.
 *
 * <p>
 * This class uses {@link Navigator} and {@link AnnotationReader} to
 * work with arbitrary annotation source and arbitrary Java model.
 * For this purpose this class is parameterized.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> {

    /**
     * {@link TypeInfo}s that are built will go into this set.
     */
    final TypeInfoSetImpl<TypeT,ClassDeclT,FieldT,MethodT> typeInfoSet;

    public final AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> reader;

    public final Navigator<TypeT,ClassDeclT,FieldT,MethodT> nav;

    /**
     * Used to detect collisions among global type names.
     */
    private final Map<QName,TypeInfo> typeNames = new HashMap<QName, TypeInfo>();

    /**
     * JAXB doesn't want to use namespaces unless we are told to, but WS-I BP
     * conformace requires JAX-RPC to always use a non-empty namespace URI.
     * (see http://www.ws-i.org/Profiles/BasicProfile-1.0-2004-04-16.html#WSDLTYPES R2105)
     *
     * <p>
     * To work around this issue, we allow the use of the empty namespaces to be
     * replaced by a particular designated namespace URI.
     *
     * <p>
     * This field keeps the value of that replacing namespace URI.
     * When there's no replacement, this field is set to "".
     */
    public final String defaultNsUri;

    /**
     * @see #setErrorHandler
     */
    private ErrorHandler errorHandler;
    private boolean hadError;

    private final ErrorHandler proxyErrorHandler = new ErrorHandler() {
        public void error(IllegalAnnotationException e) {
            reportError(e);
        }
    };


    public ModelBuilder(
        AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> reader,
        Navigator<TypeT,ClassDeclT,FieldT,MethodT> navigator,
        String defaultNamespaceRemap ) {

        this.reader = reader;
        this.nav = navigator;
        if(defaultNamespaceRemap==null)
            defaultNamespaceRemap = "";
        this.defaultNsUri = defaultNamespaceRemap;
        reader.setErrorHandler(proxyErrorHandler);
        typeInfoSet = createTypeInfoSet();
    }

    protected TypeInfoSetImpl<TypeT,ClassDeclT,FieldT,MethodT> createTypeInfoSet() {
        return new TypeInfoSetImpl(nav,reader,BuiltinLeafInfoImpl.createLeaves(nav));
    }

    /**
     * Builds a JAXB {@link ClassInfo} model from a given class declaration
     * and adds that to this model owner.
     *
     * <p>
     * Return type is either {@link ClassInfo} or {@link LeafInfo} (for types like
     * {@link String} or {@link Enum}-derived ones)
     */
    public NonElement<TypeT,ClassDeclT> getClassInfo( ClassDeclT clazz, Locatable upstream ) {
        assert clazz!=null;
        NonElement<TypeT,ClassDeclT> r = typeInfoSet.getClassInfo(clazz);
        if(r!=null)
            return r;

        if(nav.isEnum(clazz)) {
            EnumLeafInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> li = createEnumLeafInfo(clazz,upstream);
            typeInfoSet.add(li);
            r = li;
        } else {
            ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> ci = createClassInfo(clazz,upstream);
            typeInfoSet.add(ci);

            // compute the closure by eagerly expanding references
            for( PropertyInfo<TypeT,ClassDeclT> p : ci.getProperties() ) {
                for( TypeInfo<TypeT,ClassDeclT> t : p.ref() )
                    ; // just compute a reference should be suffice
            }
            r = ci;
        }

        addTypeName(r);

        return r;
    }

    /**
     * Checks the uniqueness of the type name.
     */
    private void addTypeName(NonElement<TypeT, ClassDeclT> r) {
        QName t = r.getTypeName();
        if(t==null)     return;

        TypeInfo old = typeNames.put(t,r);
        if(old!=null) {
            // collision
            reportError(new IllegalAnnotationException(
                    Messages.CONFLICTING_XML_TYPE_MAPPING.format(r.getTypeName()),
                    old, r ));
        }
    }

    /**
     * Have the builder recognize the type (if it hasn't done so yet),
     * and returns a {@link NonElement} that represents it.
     *
     * @return
     *      always non-null.
     */
    public NonElement<TypeT,ClassDeclT> getTypeInfo(TypeT t,Locatable upstream) {
        NonElement<TypeT,ClassDeclT> r = typeInfoSet.getTypeInfo(t);
        if(r!=null)     return r;

        if(nav.isArray(t)) { // no need for checking byte[], because above typeInfoset.getTypeInfo() would return non-null
            ArrayInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> ai =
                createArrayInfo(upstream, t);
            addTypeName(ai);
            typeInfoSet.add(ai);
            return ai;
        }

        ClassDeclT c = nav.asDecl(t);
        assert c!=null : t.toString()+" must be a leaf, but we failed to recognize it.";
        return getClassInfo(c,upstream);
    }

    /**
     * This method is used to add a root reference to a model.
     */
    public NonElement<TypeT,ClassDeclT> getTypeInfo(Ref<TypeT,ClassDeclT> ref) {
        // TODO: handle XmlValueList
        assert !ref.valueList;
        ClassDeclT c = nav.asDecl(ref.type);
        if(c!=null && reader.getClassAnnotation(XmlRegistry.class,c,null/*TODO: is this right?*/)!=null) {
            addRegistry(c,null);
            return null;    // TODO: is this correct?
        } else
            return getTypeInfo(ref.type,null);
    }


    protected EnumLeafInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> createEnumLeafInfo(ClassDeclT clazz,Locatable upstream) {
        return new EnumLeafInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,upstream,clazz,nav.use(clazz));
    }

    protected ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> createClassInfo(
            ClassDeclT clazz, Locatable upstream ) {
        return new ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,upstream,clazz);
    }

    protected ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> createElementInfo(
        RegistryInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> registryInfo, MethodT m) throws IllegalAnnotationException {
        return new ElementInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,registryInfo,m);
    }

    protected ArrayInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> createArrayInfo(Locatable upstream, TypeT arrayType) {
        return new ArrayInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>(this,upstream,arrayType);
    }


    /**
     * Visits a class with {@link XmlRegistry} and records all the element mappings
     * in it.
     */
    public RegistryInfo<TypeT,ClassDeclT> addRegistry(ClassDeclT registryClass, Locatable upstream ) {
        return new RegistryInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>(this,upstream,registryClass);
    }

    private boolean linked;

    /**
     * Called after all the classes are added to the type set
     * to "link" them together.
     *
     * <p>
     * Don't expose implementation classes in the signature.
     *
     * @return
     *      fully built {@link TypeInfoSet} that represents the model,
     *      or null if there was an error.
     */
    public TypeInfoSet<TypeT,ClassDeclT,FieldT,MethodT> link() {

        assert !linked;
        linked = true;

        for( ElementInfoImpl ei : typeInfoSet.getAllElements() )
            ei.link();

        for( ClassInfoImpl ci : typeInfoSet.beans().values() )
            ci.link();

        for( EnumLeafInfoImpl li : typeInfoSet.enums().values() )
            li.link();

        if(hadError)
            return null;
        else
            return typeInfoSet;
    }

//
//
// error handling
//
//

    /**
     * Sets the error handler that receives errors discovered during the model building.
     *
     * @param errorHandler
     *      can be null.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public final void reportError(IllegalAnnotationException e) {
        hadError = true;
        if(errorHandler!=null)
            errorHandler.error(e);
    }
}

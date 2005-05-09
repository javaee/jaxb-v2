package com.sun.xml.bind.v2.model.core;

import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * Root of models.&nbsp;Set of {@link TypeInfo}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeInfoSet<TypeT,ClassDeclT,FieldT,MethodT> {

    /**
     * {@link Navigator} for this model.
     */
    Navigator<TypeT,ClassDeclT,FieldT,MethodT> getNavigator();

//  turns out we can't have AnnotationReader in XJC, so it's impossible to have this here.
//  perhaps we should revisit this in the future.
//    /**
//     * {@link AnnotationReader} for this model.
//     */
//    AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> getReader();

    /**
     * Returns a {@link TypeInfo} for the given type.
     *
     * @return
     *      null if the specified type cannot be bound by JAXB, or
     *      not known to this set.
     */
    NonElement<TypeT,ClassDeclT> getTypeInfo( TypeT type );

    /**
     * Returns a {@link ClassInfo}, {@link ArrayInfo}, or {@link LeafInfo}
     * for the given bean.
     *
     * <p>
     * This method is almost like refinement of {@link #getTypeInfo(Object)} except
     * our ClassDeclT cannot derive from TypeT.
     *
     * @return
     *      null if the specified type is not bound by JAXB or otherwise
     *      unknown to this set.
     */
    NonElement<TypeT,ClassDeclT> getClassInfo( ClassDeclT type );

    /**
     * Returns all the {@link ArrayInfo}s known to this set.
     */
    Map<? extends TypeT,? extends ArrayInfo<TypeT,ClassDeclT>> arrays();

    /**
     * Returns all the {@link ClassInfo}s known to this set.
     */
    Map<ClassDeclT,? extends ClassInfo<TypeT,ClassDeclT>> beans();

    /**
     * Returns all the {@link BuiltinLeafInfo}s known to this set.
     */
    Map<TypeT,? extends BuiltinLeafInfo<TypeT,ClassDeclT>> builtins();

    /**
     * Returns all the {@link EnumLeafInfo}s known to this set.
     */
    Map<ClassDeclT,? extends EnumLeafInfo<TypeT,ClassDeclT>> enums();

    /**
     * Returns a {@link ElementInfo} for the given element.
     *
     * @param scope
     *      if null, return the info about a global element.
     *      Otherwise return a local element in the given scope if available,
     *      then look for a global element next.
     */
    ElementInfo<TypeT,ClassDeclT> getElementInfo( ClassDeclT scope, QName name );

    /**
     * Returns a type information for the given reference.
     */
    NonElement<TypeT,ClassDeclT> getTypeInfo(Ref<TypeT,ClassDeclT> ref);

    /**
     * Returns all  {@link ElementInfo}s in the given scope.
     *
     * @param scope
     *      if non-null, this method only returns the local element mapping.
     */
    Map<QName,? extends ElementInfo<TypeT,ClassDeclT>> getElementMappings( ClassDeclT scope );

    /**
     * Returns all the {@link ElementInfo} known to this set.
     */
    Iterable<? extends ElementInfo<TypeT,ClassDeclT>> getAllElements();


    /**
     * Gets all {@link XmlSchema#xmlns()} found in this context for the given namespace URI.
     *
     * @return
     *      A map from prefixes to namespace URIs, which should be declared when generating a schema.
     *      Could be empty but never null.
     */
    Map<String,String> getXmlNs(String namespaceUri); 

    /**
     * Dumps this model into XML.
     *
     * For debug only.
     *
     * TODO: not sure if this actually works. We don't really know what are TypeT,ClassDeclT.
     */
    public void dump( Result out ) throws JAXBException;
}

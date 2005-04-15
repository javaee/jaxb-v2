package com.sun.tools.xjc.api.impl.j2s;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.tools.jxc.XmlSchemaGenerator;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.api.SchemaOutputResolver;
import com.sun.xml.bind.annotation.XmlList;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class JAXBModelImpl implements J2SJAXBModel {

    final AnnotationProcessorEnvironment env;
    
    private final Map<QName,Reference> additionalElementDecls;

    private final List<String> classList = new ArrayList<String>();

    private final TypeInfoSet<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> types;

    private final AnnotationReader<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> reader;

    public JAXBModelImpl(AnnotationProcessorEnvironment env,
                         TypeInfoSet<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> types,
                         AnnotationReader<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> reader,
                         Map<QName,Reference> additionalElementDecls) {
        this.types = types;
        this.reader = reader;
        this.env = env;
        this.additionalElementDecls = additionalElementDecls;

        Navigator<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> navigator = types.getNavigator();

        for( ClassInfo<TypeMirror,TypeDeclaration> i : types.beans().values() ) {
            classList.add(i.getName());
        }

        for(ArrayInfo<TypeMirror,TypeDeclaration> a : types.arrays().values()) {
            String javaName = navigator.getTypeName(a.getType());
            classList.add(javaName);
        }

        for( EnumLeafInfo<TypeMirror,TypeDeclaration> l : types.enums().values() ) {
            QName tn = l.getTypeName();
            if(tn!=null) {
                String javaName = navigator.getTypeName(l.getType());
                classList.add(javaName);
            }
        }

        // check for collision between "additional" ones and the ones given to JAXB
        // and eliminate duplication
        Iterator<Map.Entry<QName, Reference>> itr = additionalElementDecls.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<QName, Reference> entry = itr.next();
            if(entry.getValue()==null)      continue;
            
            NonElement<TypeMirror,TypeDeclaration> xt = getXmlType(entry.getValue());
            assert xt!=null;
            if(xt instanceof ClassInfo) {
                ClassInfo<TypeMirror,TypeDeclaration> xct = (ClassInfo<TypeMirror,TypeDeclaration>) xt;
                Element<TypeMirror,TypeDeclaration> elem = xct.asElement();
                if(elem!=null && elem.getElementName().equals(entry.getKey())) {
                    itr.remove();
                    continue;
                }
            }
            ElementInfo<TypeMirror,TypeDeclaration> ei = types.getElementInfo(null,entry.getKey());
            if(ei!=null && ei.getContentType()==xt) {
                itr.remove();
                continue;
            }
        }
    }

    public List<String> getClassList() {
        return classList;
    }

    public QName getXmlTypeName(Reference javaType) {
        NonElement<TypeMirror,TypeDeclaration> ti = getXmlType(javaType);

        if(ti!=null)
            return ti.getTypeName();

        return null;
    }

    private NonElement<TypeMirror,TypeDeclaration> getXmlType(Reference r) {
        if(r==null)
            throw new IllegalArgumentException();

        XmlJavaTypeAdapter xjta = r.annotations.getAnnotation(XmlJavaTypeAdapter.class);
        XmlList xl = r.annotations.getAnnotation(XmlList.class);

        Ref<TypeMirror, TypeDeclaration> ref = new Ref<TypeMirror, TypeDeclaration>(
            reader,types.getNavigator(),r.type,xjta,xl);

        NonElement<TypeMirror,TypeDeclaration> ti = types.getTypeInfo(ref);
        return ti;
    }

    public void generateSchema(SchemaOutputResolver outputResolver, ErrorListener errorListener) throws IOException {
        XmlSchemaGenerator<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> xsdgen
            = new XmlSchemaGenerator<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration>();

        xsdgen.addAllClasses(types.beans().values());
        xsdgen.addAllElements(types.getElementMappings(null).values());
        xsdgen.addAllEnums(types.enums().values());
        xsdgen.addAllArrays(types.arrays().values());
        for (Map.Entry<QName,Reference> e : additionalElementDecls.entrySet()) {
            Reference value = e.getValue();
            if(value!=null) {
                NonElement<TypeMirror, TypeDeclaration> typeInfo = getXmlType(value);
                if(typeInfo==null)
                    throw new IllegalArgumentException(e.getValue()+" was not specified to JavaCompiler.bind");
                xsdgen.add(e.getKey(),typeInfo);
            } else {
                xsdgen.add(e.getKey(),null);
            }
        }

        xsdgen.write(outputResolver);
    }
}

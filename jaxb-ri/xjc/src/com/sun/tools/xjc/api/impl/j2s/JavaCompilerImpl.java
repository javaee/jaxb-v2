package com.sun.tools.xjc.api.impl.j2s;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.tools.jxc.apt.InlineAnnotationReaderImpl;
import com.sun.tools.jxc.model.nav.APTNavigator;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.JavaCompiler;
import com.sun.tools.xjc.api.Reference;
import com.sun.xml.bind.annotation.XmlList;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class JavaCompilerImpl implements JavaCompiler {
    public J2SJAXBModel bind(
        Collection<Reference> rootClasses,
        Map<QName,Reference> additionalElementDecls,
        String defaultNamespaceRemap,
        AnnotationProcessorEnvironment env) {

        ModelBuilder<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> builder =
            new ModelBuilder<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration>(
                InlineAnnotationReaderImpl.theInstance,
                new APTNavigator(env),
                defaultNamespaceRemap
            );

        builder.setErrorHandler(new ErrorHandlerImpl(env.getMessager()));

        for( Reference ref : rootClasses ) {
            TypeMirror t = ref.type;

            XmlJavaTypeAdapter xjta = ref.annotations.getAnnotation(XmlJavaTypeAdapter.class);
            XmlList xl = ref.annotations.getAnnotation(XmlList.class);

            builder.getTypeInfo(new Ref<TypeMirror,TypeDeclaration>(builder,t,xjta,xl));
        }

        TypeInfoSet r = builder.link();
        if(r==null)     return null;

        if(additionalElementDecls==null)
            additionalElementDecls = Collections.emptyMap();
        else {
            // fool proof check
            for (Map.Entry<QName, ? extends Reference> e : additionalElementDecls.entrySet()) {
                if(e.getKey()==null)
                    throw new IllegalArgumentException("nulls in additionalElementDecls");
            }
        }
        return new JAXBModelImpl(env,r,builder.reader,new HashMap(additionalElementDecls));
    }

    private static final class ErrorHandlerImpl implements ErrorHandler {
        private final Messager messager;

        public ErrorHandlerImpl(Messager messager) {
            this.messager = messager;
        }

        public void error(IllegalAnnotationException e) {
            messager.printError(e.toString());
        }
    }
}

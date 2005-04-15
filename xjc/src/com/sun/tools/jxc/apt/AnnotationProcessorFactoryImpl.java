package com.sun.tools.jxc.apt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * {@link AnnotationProcessorFactory} for JAXB.
 *
 * <p>
 * This is the entry point for the APT driver.
 */
public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {
    public Collection<String> supportedOptions() {
        return Arrays.asList(Const.CONFIG_FILE_OPTION);
    }

    public Collection<String> supportedAnnotationTypes() {
        return Arrays.asList("javax.xml.bind.annotation.*");
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env) {
        return new AnnotationParser(atds,env);
    }
}

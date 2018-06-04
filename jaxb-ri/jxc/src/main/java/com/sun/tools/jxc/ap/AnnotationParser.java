/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.jxc.ap;

import com.sun.tools.jxc.ConfigReader;
import com.sun.tools.jxc.api.JXC;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Reference;
import org.xml.sax.SAXException;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class behaves as a JAXB Annotation Processor,
 * It reads the user specified typeDeclarations
 * and the config files
 * It also reads config files
 *
 * Used in unit tests
 *
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
@SupportedAnnotationTypes("javax.xml.bind.annotation.*")
@SupportedOptions("jaxb.config")
public final class AnnotationParser extends AbstractProcessor {

    private ErrorReceiver errorListener;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        errorListener = new ErrorReceiverImpl(
                processingEnv.getMessager(),
                processingEnv.getOptions().containsKey(Const.DEBUG_OPTION.getValue())
        );
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processingEnv.getOptions().containsKey(Const.CONFIG_FILE_OPTION.getValue())) {
            String value = processingEnv.getOptions().get(Const.CONFIG_FILE_OPTION.getValue());

            // For multiple config files we are following the format
            // -Aconfig=foo.config:bar.config where : is the pathSeparatorChar
            StringTokenizer st = new StringTokenizer(value, File.pathSeparator);
            if (!st.hasMoreTokens()) {
                errorListener.error(null, Messages.OPERAND_MISSING.format(Const.CONFIG_FILE_OPTION.getValue()));
                return true;
            }

            while (st.hasMoreTokens()) {
                File configFile = new File(st.nextToken());
                if (!configFile.exists()) {
                    errorListener.error(null, Messages.NON_EXISTENT_FILE.format());
                    continue;
                }

                try {
                    Collection<TypeElement> rootElements = new ArrayList<TypeElement>();
                    filterClass(rootElements, roundEnv.getRootElements());
                    ConfigReader configReader = new ConfigReader(
                            processingEnv,
                            rootElements,
                            configFile,
                            errorListener
                    );

                    Collection<Reference> classesToBeIncluded = configReader.getClassesToBeIncluded();
                    J2SJAXBModel model = JXC.createJavaCompiler().bind(
                            classesToBeIncluded, Collections.<QName, Reference>emptyMap(), null, processingEnv);

                    SchemaOutputResolver schemaOutputResolver = configReader.getSchemaOutputResolver();

                    model.generateSchema(schemaOutputResolver, errorListener);
                } catch (IOException e) {
                    errorListener.error(e.getMessage(), e);
                } catch (SAXException e) {
                    // the error should have already been reported
                }
            }
        }
        return true;
    }

    private void filterClass(Collection<TypeElement> rootElements, Collection<? extends Element> elements) {
        for (Element element : elements) {
            if (element.getKind().equals(ElementKind.CLASS) || element.getKind().equals(ElementKind.INTERFACE) ||
                    element.getKind().equals(ElementKind.ENUM)) {
                rootElements.add((TypeElement) element);
                filterClass(rootElements, ElementFilter.typesIn(element.getEnclosedElements()));
            }
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

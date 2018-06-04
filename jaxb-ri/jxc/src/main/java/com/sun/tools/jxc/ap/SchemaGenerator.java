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

import com.sun.tools.jxc.api.JXC;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Reference;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link Processor} that implements the schema generator
 * command line tool.
 *
 * @author Kohsuke Kawaguchi
 */
@SupportedAnnotationTypes("*")
public class SchemaGenerator extends AbstractProcessor {

    /**
     * User-specified schema locations, if any.
     */
    private final Map<String,File> schemaLocations = new HashMap<String, File>();

    private File episodeFile;

    public SchemaGenerator() {
    }

    public SchemaGenerator( Map<String,File> m ) {
        schemaLocations.putAll(m);
    }

    public void setEpisodeFile(File episodeFile) {
        this.episodeFile = episodeFile;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final ErrorReceiverImpl errorListener = new ErrorReceiverImpl(processingEnv);

        List<Reference> classesToBeBound = new ArrayList<Reference>();
        // simply ignore all the interface definitions,
        // so that users won't have to manually exclude interfaces, which is silly.
        filterClass(classesToBeBound, roundEnv.getRootElements());

        J2SJAXBModel model = JXC.createJavaCompiler().bind(classesToBeBound, Collections.<QName, Reference>emptyMap(), null, processingEnv);
        if (model == null)
            return false; // error

        try {
            model.generateSchema(
                    new SchemaOutputResolver() {
                        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                            File file;
                            OutputStream out;
                            if (schemaLocations.containsKey(namespaceUri)) {
                                file = schemaLocations.get(namespaceUri);
                                if (file == null) return null;    // don't generate
                                out = new FileOutputStream(file);
                            } else {
                                // use the default
                                file = new File(suggestedFileName);
                                out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", suggestedFileName)
                                        .openOutputStream();
                                file = file.getAbsoluteFile();
                            }

                            StreamResult ss = new StreamResult(out);
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing "+file);
                            ss.setSystemId(file.toURL().toExternalForm());
                            return ss;
                        }
                    }, errorListener);

            if (episodeFile != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing "+episodeFile);
                model.generateEpisodeFile(new StreamResult(episodeFile));
            }
        } catch (IOException e) {
            errorListener.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Filter classes (note that enum is kind of class) from elements tree
     * @param result list of found classes
     * @param elements tree to be filtered
     */
    private void filterClass(List<Reference> result, Collection<? extends Element> elements) {
        for (Element element : elements) {
            final ElementKind kind = element.getKind();
            if (ElementKind.CLASS.equals(kind) || ElementKind.ENUM.equals(kind)) {
                result.add(new Reference((TypeElement) element, processingEnv));
                filterClass(result, ElementFilter.typesIn(element.getEnclosedElements()));
            }
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.jxc.apt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.api.XJC;

/**
 * {@link AnnotationProcessorFactory} that implements the schema generator
 * command line tool.
 *
 * @author Kohsuke Kawaguchi
 */
public class SchemaGenerator implements AnnotationProcessorFactory {

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

    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }

    public Collection<String> supportedAnnotationTypes() {
        return Arrays.asList("*");
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, final AnnotationProcessorEnvironment env) {
        return new AnnotationProcessor() {
            final ErrorReceiverImpl errorListener = new ErrorReceiverImpl(env);

            public void process() {
                List<Reference> decls = new ArrayList<Reference>();
                for(TypeDeclaration d : env.getTypeDeclarations()) {
                    // simply ignore all the interface definitions,
                    // so that users won't have to manually exclude interfaces, which is silly.
                    if(d instanceof ClassDeclaration)
                        decls.add(new Reference(d,env));
                }

                J2SJAXBModel model = XJC.createJavaCompiler().bind(decls,Collections.<QName,Reference>emptyMap(),null,env);
                if(model==null)
                    return; // error

                try {
                    model.generateSchema(
                        new SchemaOutputResolver() {
                            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                                File file;
                                OutputStream out;
                                if(schemaLocations.containsKey(namespaceUri)) {
                                    file = schemaLocations.get(namespaceUri);
                                    if(file==null)  return null;    // don't generate
                                    out = new FileOutputStream(file);
                                } else {
                                    // use the default
                                    file = new File(suggestedFileName);
                                    out = env.getFiler().createBinaryFile(Filer.Location.CLASS_TREE,"",file);
                                    file = file.getAbsoluteFile();
                                }

                                StreamResult ss = new StreamResult(out);
                                env.getMessager().printNotice("Writing "+file);
                                ss.setSystemId(file.toURL().toExternalForm());
                                return ss;
                            }
                        }, errorListener);

                    if(episodeFile!=null) {
                        env.getMessager().printNotice("Writing "+episodeFile);
                        model.generateEpisodeFile(new StreamResult(episodeFile));
                    }
                } catch (IOException e) {
                    errorListener.error(e.getMessage(),e);
                }
            }
        };
    }
}

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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.tools.jxc.ConfigReader;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.api.XJC;

import org.xml.sax.SAXException;



/**
 * This class behaves as a JAXB Annotation Processor,
 * It reads the user specified typeDeclarations
 * and the config files
 * It also reads config files
 *
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
final class AnnotationParser implements AnnotationProcessor  {

    /**
     * This is the environment available to the annotationProcessor
     */
    private final AnnotationProcessorEnvironment env;

    private ErrorReceiver errorListener;

    public AnnotationProcessorEnvironment getEnv() {
        return env;
    }


    AnnotationParser(Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env) {
        this.env = env;
        errorListener = new ErrorReceiverImpl(env.getMessager(),env.getOptions().containsKey(Const.DEBUG_OPTION));
    }

    public void process() {
        for( Map.Entry<String,String> me : env.getOptions().entrySet() ) {
            String key =  me.getKey();
            if (key.startsWith(Const.CONFIG_FILE_OPTION+'=')) {
                // somehow the values are passed as a part of the key in APT.
                // this is ugly
                String value = key.substring(Const.CONFIG_FILE_OPTION.length()+1);

                // For multiple config files we are following the format
                // -Aconfig=foo.config:bar.config where : is the pathSeparatorChar
                StringTokenizer st = new StringTokenizer(value,File.pathSeparator);
                if(!st.hasMoreTokens()) {
                    errorListener.error(null,Messages.OPERAND_MISSING.format(Const.CONFIG_FILE_OPTION));
                    continue;
                }

                while (st.hasMoreTokens())   {
                    File configFile = new File(st.nextToken());
                    if(!configFile.exists()) {
                        errorListener.error(null,Messages.NON_EXISTENT_FILE.format());
                        continue;
                    }

                    try {
                        ConfigReader configReader = new ConfigReader(env,env.getTypeDeclarations(),configFile,errorListener);

                        Collection<Reference> classesToBeIncluded = configReader.getClassesToBeIncluded();
                        J2SJAXBModel model = XJC.createJavaCompiler().bind(
                                classesToBeIncluded,Collections.<QName,Reference>emptyMap(),null,env);

                        SchemaOutputResolver schemaOutputResolver = configReader.getSchemaOutputResolver();

                        model.generateSchema(schemaOutputResolver,errorListener);
                    } catch (IOException e) {
                        errorListener.error(e.getMessage(),e);
                    } catch (SAXException e) {
                        // the error should have already been reported
                    }
                }
            }
        }
    }
}

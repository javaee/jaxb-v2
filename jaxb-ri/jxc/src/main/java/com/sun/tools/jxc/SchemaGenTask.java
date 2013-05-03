/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.jxc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Processor;
import com.sun.tools.jxc.ap.SchemaGenerator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;


/**
 * Ant task to invoke the schema generator.
 *
 * @author Kohsuke Kawaguchi
 */
public class SchemaGenTask extends ApBasedTask {
    private final List<Schema>/*<Schema>*/ schemas = new ArrayList<Schema>();

    private File episode;

    protected void setupCommandlineSwitches(Commandline cmd) {
        cmd.createArgument().setValue("-proc:only");
    }

    protected String getCompilationMessage() {
        return "Generating schema from ";
    }

    protected String getFailedMessage() {
        return "schema generation failed";
    }

    public Schema createSchema() {
        Schema s = new Schema();
        schemas.add(s);
        return s;
    }

    /**
     * Sets the episode file to be generated.
     * Null to not to generate one, which is the default behavior.
     */
    public void setEpisode(File f) {
        this.episode = f;
    }

    protected Processor getProcessor() {
        Map<String, File> m = new HashMap<String, File>();
        for (Schema schema : schemas) {

            if (m.containsKey(schema.namespace)) {
                throw new BuildException("the same namespace is specified twice");
            }
            m.put(schema.namespace, schema.file);

        }

        SchemaGenerator r = new SchemaGenerator(m);
        if(episode!=null)
            r.setEpisodeFile(episode);
        return r;
    }


    /**
     * Nested schema element to specify the namespace -> file name mapping.
     */
    public class Schema {
        private String namespace;
        private File file;

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public void setFile(String fileName) {
            // resolve the file name relative to the @dest, or otherwise the project base dir.
            File dest = getDestdir();
            if(dest==null)
                dest = getProject().getBaseDir();
            this.file = new File(dest,fileName);
        }
    }
}

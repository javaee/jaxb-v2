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

package com.sun.tools.xjc.api.util;

import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import javax.annotation.processing.Filer;

import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.StandardLocation.SOURCE_PATH;

/**
 * {@link CodeWriter} that generates source code to {@link Filer}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class FilerCodeWriter extends CodeWriter {

    private final Filer filer;

    public FilerCodeWriter(Filer filer) {
        this.filer = filer;
    }

    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        StandardLocation loc;
        if(fileName.endsWith(".java")) {
            // Annotation Processing doesn't do the proper Unicode escaping on Java source files,
            // so we can't rely on Filer.createSourceFile.
            loc = SOURCE_PATH;
        } else {
            // put non-Java files directly to the output folder
            loc = CLASS_PATH;
        }
        return filer.createResource(loc, pkg.name(), fileName).openOutputStream();
    }

    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        String name;
        if(pkg.isUnnamed())
            name = fileName;
        else
            name = pkg.name()+'.'+fileName;

        name = name.substring(0,name.length()-5);   // strip ".java"

        return filer.createSourceFile(name).openWriter();
    }

    public void close() {
        ; // noop
    }
}

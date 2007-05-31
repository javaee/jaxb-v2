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

package com.sun.tools.xjc.api.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.mirror.apt.Filer;

import static com.sun.mirror.apt.Filer.Location.CLASS_TREE;
import static com.sun.mirror.apt.Filer.Location.SOURCE_TREE;

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
        Filer.Location loc;
        if(fileName.endsWith(".java")) {
            // APT doesn't do the proper Unicode escaping on Java source files,
            // so we can't rely on Filer.createSourceFile.
            loc = SOURCE_TREE;
        } else {
            // put non-Java files directly to the output folder
            loc = CLASS_TREE;
        }
        return filer.createBinaryFile(loc,pkg.name(),new File(fileName));
    }

    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        String name;
        if(pkg.isUnnamed())
            name = fileName;
        else
            name = pkg.name()+'.'+fileName;

        name = name.substring(0,name.length()-5);   // strip ".java"

        return filer.createSourceFile(name);
    }

    public void close() {
        ; // noop
    }
}

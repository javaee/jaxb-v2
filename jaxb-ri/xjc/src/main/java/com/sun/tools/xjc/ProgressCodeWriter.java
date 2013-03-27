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

package com.sun.tools.xjc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FilterCodeWriter;

/**
 * {@link CodeWriter} that reports progress to {@link XJCListener}.
 */
final class ProgressCodeWriter extends FilterCodeWriter {

    private int current;
    private final int totalFileCount;

    public ProgressCodeWriter( CodeWriter output, XJCListener progress, int totalFileCount ) {
        super(output);
        this.progress = progress;
        this.totalFileCount = totalFileCount;
        if(progress==null)
            throw new IllegalArgumentException();
    }

    private final XJCListener progress;

    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        report(pkg,fileName);
        return super.openSource(pkg, fileName);
    }

    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        report(pkg,fileName);
        return super.openBinary(pkg,fileName);
    }

    private void report(JPackage pkg, String fileName) {
        String name = pkg.name().replace('.', File.separatorChar);
        if(name.length()!=0)    name +=     File.separatorChar;
        name += fileName;

        if(progress.isCanceled())
            throw new AbortException();
        progress.generatedFile(name,current++,totalFileCount);
    }
}

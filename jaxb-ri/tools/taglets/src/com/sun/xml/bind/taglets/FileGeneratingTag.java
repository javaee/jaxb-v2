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

package com.sun.xml.bind.taglets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;

/**
 * A taglet that generates additional files.
 *
 * This class does the additional bookkeeping to generate unique file name within a package.
 * @author Kohsuke Kawaguchi
 */
abstract class FileGeneratingTag implements Taglet {

    private final Map<Doc,Integer> counts = new WeakHashMap<Doc, Integer>();

    public boolean inField() {
        return true;
    }

    public boolean inConstructor() {
        return true;
    }

    public boolean inMethod() {
        return true;
    }

    public boolean inOverview() {
        return true;
    }

    public boolean inPackage() {
        return true;
    }

    public boolean inType() {
        return true;
    }

    public boolean isInlineTag() {
        return true;
    }

    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) throws IllegalArgumentException {
        throw new UnsupportedOperationException("this is an inline tag");
    }

    /**
     * Returns the full file name for the additional generated file.
     *
     * @param imageFileName
     *      just the base name of the file name.
     */
    protected final File getOutputFile(TagletWriter writer, PackageDoc pkg, String imageFileName) {
        File rootDir = new File(writer.configuration().destDirName);
        File pkgDir = new File(rootDir,pkg.name().replace('.','/'));
        File imageFile = new File(pkgDir,imageFileName);
        return imageFile;
    }

    protected final PackageDoc getPackage(Doc doc) {
        if(doc instanceof PackageDoc)
            return (PackageDoc)doc;
        if(doc instanceof ProgramElementDoc) {
            return ((ProgramElementDoc)doc).containingPackage();
        }
        // I don't think there's any other kind, but...
        throw new IllegalArgumentException(doc.getClass().getName());
    }

    protected final int getImageIndex(Doc doc) {
        Integer i = counts.get(doc);
        if(i==null) {
            i = 1;
        }
        counts.put(doc,i+1);
        return i;
    }
}


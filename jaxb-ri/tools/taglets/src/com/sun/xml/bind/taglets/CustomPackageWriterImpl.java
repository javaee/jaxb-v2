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

import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.PackageWriterImpl;

/**
 * Class to generate file for each package contents in the right-hand
 * frame. This will list all the Class Kinds in the package. A click on any
 * class-kind will update the frame with the clicked class-kind page.
 *
 * @author Atul M Dambalkar
 */
public class CustomPackageWriterImpl extends PackageWriterImpl {

    public CustomPackageWriterImpl(ConfigurationImpl configuration, PackageDoc packageDoc, PackageDoc prev, PackageDoc next) throws IOException {
        super(configuration, packageDoc, prev, next);
    }

    /**
     * {@inheritDoc}
     */
    public void writePackageDescription() {
        if (packageDoc.inlineTags().length > 0) {
            anchor("package_description");
//            h2(configuration.getText("doclet.Package_Description", packageDoc.name()));
            p();
            printInlineComment(packageDoc);
            p();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writePackageHeader(String heading) {
        String pkgName = packageDoc.name();
        String[] metakeywords = { pkgName + " " + "package" };
        printHtmlHeader(pkgName, metakeywords,true);
        navLinks(true);
        hr();
        writeAnnotationInfo(packageDoc);
//        h2(configuration.getText("doclet.Package") + " " + heading);
//        if (packageDoc.inlineTags().length > 0 && ! configuration.nocomment) {
//            printSummaryComment(packageDoc);
//            p();
//            bold(configuration.getText("doclet.See"));
//            br();
//            printNbsps();
//            printHyperLink("", "package_description",
//                configuration.getText("doclet.Description"), true);
//            p();
//        }
    }

}

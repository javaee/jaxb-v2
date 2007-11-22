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

import com.sun.javadoc.ClassDoc;
import com.sun.tools.doclets.formats.html.ClassWriterImpl;
import com.sun.tools.doclets.internal.toolkit.ClassWriter;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;

/**
 * Generate the Class Information Page.
 * @see com.sun.javadoc.ClassDoc
 * @see java.util.Collections
 * @see java.util.List
 * @see java.util.ArrayList
 * @see java.util.HashMap
 *
 * @author Atul M Dambalkar
 * @author Robert Field
 */
public class CustomClassWriterImpl extends ClassWriterImpl
        implements ClassWriter {

    public CustomClassWriterImpl(ClassDoc classDoc, ClassDoc prevClass, ClassDoc nextClass, ClassTree classTree) throws Exception {
        super(classDoc, prevClass, nextClass, classTree);
    }

    @Override
    public void writeHeader(String header) {
        String pkgname = (classDoc.containingPackage() != null)?
            classDoc.containingPackage().name(): "";
        String clname = classDoc.name();
        printHtmlHeader(clname,
            configuration.metakeywords.getMetaKeywords(classDoc), true);
        navLinks(true);
        hr();
        println("<!-- ======== START OF CLASS DATA ======== -->");
//        h2();
//        if (pkgname.length() > 0) {
//            font("-1"); print(pkgname); fontEnd(); br();
//        }
//        print(header + getTypeParameterLinks(new LinkInfoImpl(
//            LinkInfoImpl.CONTEXT_CLASS_HEADER,
//            classDoc, false)));
//        h2End();
    }


    @Override
    protected void printSummaryDetailLinks() {
        // noop
    }
}






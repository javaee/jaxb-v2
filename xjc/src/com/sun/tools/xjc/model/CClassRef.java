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

package com.sun.tools.xjc.model;

import javax.xml.namespace.QName;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum;
import com.sun.xml.xsom.XSComponent;

/**
 * Refernece to an existing class.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CClassRef extends AbstractCElement implements NClass, CClass {

    private final String fullyQualifiedClassName;

    /**
     *
     * @param decl
     *      The {@link BIClass} declaration that has {@link BIClass#getExistingClassRef()}
     */
    public CClassRef(Model model, XSComponent source, BIClass decl, CCustomizations customizations) {
        super(model, source, decl.getLocation(), customizations);
        fullyQualifiedClassName = decl.getExistingClassRef();
        assert fullyQualifiedClassName!=null;
    }

    /**
     *
     * @param decl
     *      The {@link BIClass} declaration that has {@link BIEnum#ref}
     */
    public CClassRef(Model model, XSComponent source, BIEnum decl, CCustomizations customizations) {
        super(model, source, decl.getLocation(), customizations);
        fullyQualifiedClassName = decl.ref;
        assert fullyQualifiedClassName!=null;
    }

    public void setAbstract() {
        // assume that the referenced class is marked as abstract to begin with.
    }

    public boolean isAbstract() {
        // no way to find out for sure
        return false;
    }

    public NType getType() {
        return this;
    }

    /**
     * Cached for both performance and single identity.
     */
    private JClass clazz;

    public JClass toType(Outline o, Aspect aspect) {
        if(clazz==null)
            clazz = o.getCodeModel().ref(fullyQualifiedClassName);
        return clazz;
    }

    public String fullName() {
        return fullyQualifiedClassName;
    }

    public QName getTypeName() {
        return null;
    }

    /**
     * Guaranteed to return this.
     */
    @Deprecated
    public CNonElement getInfo() {
        return this;
    }
    
// are these going to bite us?
//    we can compute some of them, but not all.

    public CElement getSubstitutionHead() {
        return null;
    }

    public CClassInfo getScope() {
        return null;
    }

    public QName getElementName() {
        return null;
    }

    public boolean isBoxedType() {
        return false;
    }

    public boolean isSimpleType() {
        return false;
    }


}

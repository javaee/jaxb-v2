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

import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.impl.ArrayInfoImpl;
import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * Because XJC doesn't generate the array binding, this class will
 * never show up in the model constructed by XJC.
 *
 * <p>
 * This class is nevertheless defined to make the type checker happy. 
 *
 * @author Kohsuke Kawaguchi
 */
public final class CArrayInfo extends AbstractCTypeInfoImpl implements ArrayInfo<NType,NClass>, CNonElement, NType {

    private final CNonElement itemType;

    private final QName typeName;

    public CArrayInfo(Model model,CNonElement itemType, XSComponent source, CCustomizations customizations) {
        super(model,source,customizations);
        this.itemType = itemType;
        assert itemType.getTypeName()!=null;
        this.typeName = ArrayInfoImpl.calcArrayTypeName(itemType.getTypeName());
    }

    public CNonElement getItemType() {
        return itemType;
    }

    public QName getTypeName() {
        return typeName;
    }

    public boolean isSimpleType() {
        return false;
    }

    @Deprecated // guaranteed to return this
    public CNonElement getInfo() {
        return this;
    }

    public JType toType(Outline o, Aspect aspect) {
        return itemType.toType(o,aspect).array();
    }

    public NType getType() {
        return this;
    }

    public boolean isBoxedType() {
        return false;
    }

    public String fullName() {
        return itemType.getType().fullName()+"[]";
    }

    public Locator getLocator() {
        return Model.EMPTY_LOCATOR;
    }
}

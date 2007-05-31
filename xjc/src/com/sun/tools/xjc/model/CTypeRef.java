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

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.runtime.RuntimeUtil;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.istack.Nullable;

/**
 * {@link TypeRef} for XJC.
 * 
 * TODO: do we need the source schema component support here?
 *
 * @author Kohsuke Kawaguchi
 */
public final class CTypeRef implements TypeRef<NType,NClass> {
    /**
     * In-memory type.
     *
     * This is the type used when 
     */
    @XmlJavaTypeAdapter(RuntimeUtil.ToStringAdapter.class)
    private final CNonElement type;

    private final QName elementName;

    /**
     * XML Schema type name of {@link #type}, if available.
     */
    /*package*/ final @Nullable QName typeName;

    private final boolean nillable;
    public final XmlString defaultValue;

    public CTypeRef(CNonElement type, XSElementDecl decl) {
        this(type, BGMBuilder.getName(decl),getSimpleTypeName(decl), decl.isNillable(), decl.getDefaultValue() );

    }

    public static QName getSimpleTypeName(XSElementDecl decl) {
        if(decl==null)  return null;
        QName typeName = null;
        if(decl.getType().isSimpleType())
            typeName = BGMBuilder.getName(decl.getType());
        return typeName;
    }

    public CTypeRef(CNonElement type, QName elementName, QName typeName, boolean nillable, XmlString defaultValue) {
        assert type!=null;
        assert elementName!=null;

        this.type = type;
        this.elementName = elementName;
        this.typeName = typeName;
        this.nillable = nillable;
        this.defaultValue = defaultValue;
    }

    public CNonElement getTarget() {
        return type;
    }

    public QName getTagName() {
        return elementName;
    }

    public boolean isNillable() {
        return nillable;
    }

    /**
     * Inside XJC, use {@link #defaultValue} that has context information.
     * This method is to override the one defined in the runtime model. 
     *
     * @see #defaultValue
     */
    public String getDefaultValue() {
        if(defaultValue!=null)
            return defaultValue.value;
        else
            return null;
    }

    public boolean isLeaf() {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    public PropertyInfo<NType, NClass> getSource() {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }
}

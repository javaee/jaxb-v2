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

import javax.activation.MimeType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStringLiteral;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.xsom.XmlString;


/**
 * General-purpose {@link TypeUse} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
final class TypeUseImpl implements TypeUse {
    private final CNonElement coreType;
    private final boolean collection;
    private final CAdapter adapter;
    private final ID id;
    private final MimeType expectedMimeType;


    public TypeUseImpl(CNonElement itemType, boolean collection, ID id, MimeType expectedMimeType, CAdapter adapter) {
        this.coreType = itemType;
        this.collection = collection;
        this.id = id;
        this.expectedMimeType = expectedMimeType;
        this.adapter = adapter;
    }

    public boolean isCollection() {
        return collection;
    }

    public CNonElement getInfo() {
        return coreType;
    }

    public CAdapter getAdapterUse() {
        return adapter;
    }

    public ID idUse() {
        return id;
    }

    public MimeType getExpectedMimeType() {
        return expectedMimeType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeUseImpl)) return false;

        final TypeUseImpl that = (TypeUseImpl) o;

        if (collection != that.collection) return false;
        if (this.id != that.id ) return false;
        if (adapter != null ? !adapter.equals(that.adapter) : that.adapter != null) return false;
        if (coreType != null ? !coreType.equals(that.coreType) : that.coreType != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (coreType != null ? coreType.hashCode() : 0);
        result = 29 * result + (collection ? 1 : 0);
        result = 29 * result + (adapter != null ? adapter.hashCode() : 0);
        return result;
    }


    public JExpression createConstant(Outline outline, XmlString lexical) {
        if(isCollection())  return null;

        if(adapter==null)     return coreType.createConstant(outline, lexical);

        // [RESULT] new Adapter().unmarshal(CONSTANT);
        JExpression cons = coreType.createConstant(outline, lexical);
        Class<? extends XmlAdapter> atype = adapter.getAdapterIfKnown();

        // try to run the adapter now rather than later.
        if(cons instanceof JStringLiteral && atype!=null) {
            JStringLiteral scons = (JStringLiteral) cons;
            XmlAdapter a = ClassFactory.create(atype);
            try {
                Object value = a.unmarshal(scons.str);
                if(value instanceof String) {
                    return JExpr.lit((String)value);
                }
            } catch (Exception e) {
                // assume that we can't eagerly bind this
            }
        }

        return JExpr._new(adapter.getAdapterClass(outline)).invoke("unmarshal").arg(cons);
    }
}

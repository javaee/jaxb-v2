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

package com.sun.tools.xjc.api.impl.s2j;

import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlList;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.nav.NType;
import static com.sun.tools.xjc.outline.Aspect.EXPOSED;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;

/**
 * {@link TypeAndAnnotation} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
final class TypeAndAnnotationImpl implements TypeAndAnnotation {
    private final TypeUse typeUse;
    private final Outline outline;

    public TypeAndAnnotationImpl(Outline outline, TypeUse typeUse) {
        this.typeUse = typeUse;
        this.outline = outline;
    }

    public JType getTypeClass() {
        CAdapter a = typeUse.getAdapterUse();
        NType nt;
        if(a!=null)
            nt = a.customType;
        else
            nt = typeUse.getInfo().getType();

        JType jt = nt.toType(outline,EXPOSED);

        JPrimitiveType prim = jt.boxify().getPrimitiveType();
        if(!typeUse.isCollection() && prim!=null)
            jt = prim;

        if(typeUse.isCollection())
            jt = jt.array();

        return jt;
    }

    public void annotate(JAnnotatable programElement) {
        if(typeUse.getAdapterUse()==null && !typeUse.isCollection())
            return; // nothing

        CAdapter adapterUse = typeUse.getAdapterUse();
        if(adapterUse!=null) {
            // ugly, ugly hack
            if(adapterUse.getAdapterIfKnown()== SwaRefAdapter.class) {
                programElement.annotate(XmlAttachmentRef.class);
            } else {
                // [RESULT]
                // @XmlJavaTypeAdapter( Foo.class )
                programElement.annotate2(XmlJavaTypeAdapterWriter.class).value(
                    adapterUse.adapterType.toType(outline,EXPOSED));
            }
        }
        if(typeUse.isCollection())
            programElement.annotate(XmlList.class);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        // TODO: support annotations
        builder.append(getTypeClass());
        return builder.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof TypeAndAnnotationImpl)) return false;
        TypeAndAnnotationImpl that = (TypeAndAnnotationImpl) o;
        return this.typeUse==that.typeUse;
    }

    public int hashCode() {
        return typeUse.hashCode();
    }
}

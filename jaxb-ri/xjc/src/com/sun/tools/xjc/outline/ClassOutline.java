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

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc.outline;

import java.util.List;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.istack.NotNull;

/**
 * Outline object that provides per-{@link CClassInfo} information
 * for filling in methods/fields for a bean.
 * 
 * This interface is accessible from {@link Outline}
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ClassOutline {

    /**
     * A {@link Outline} that encloses all the class outlines.
     */
    public abstract @NotNull Outline parent();

    /**
     * {@link PackageOutline} that contains this class.
     */
    public @NotNull PackageOutline _package() {
        return parent().getPackageContext(ref._package());
    }

    /**
     * This {@link ClassOutline} holds information about this {@link CClassInfo}.
     */
    public final @NotNull CClassInfo target;

    /**
     * The exposed aspect of the a bean.
     *
     * implClass is always assignable to this type.
     * <p>
     * Usually this is the public content interface, but
     * it could be the same as the implClass.
     */
    public final @NotNull JDefinedClass ref;

    /**
     * The implementation aspect of a bean.
     * The actual place where fields/methods should be generated into.
     */
    public final @NotNull JDefinedClass implClass;

    /**
     * The implementation class that shall be used for reference.
     * <p>
     * Usually this field holds the same value as the {@link #implClass} method,
     * but sometimes it holds the user-specified implementation class
     * when it is specified.
     * <p>
     * This is the type that needs to be used for generating fields.
     */
    public final @NotNull JClass implRef;




    protected ClassOutline( CClassInfo _target, JDefinedClass exposedClass, JClass implRef, JDefinedClass _implClass) {
        this.target = _target;
        this.ref = exposedClass;
        this.implRef = implRef;
        this.implClass = _implClass;
    }

    /**
     * Gets all the {@link FieldOutline}s newly declared
     * in this class.
     */
    public final FieldOutline[] getDeclaredFields() {
        List<CPropertyInfo> props = target.getProperties();
        FieldOutline[] fr = new FieldOutline[props.size()];
        for( int i=0; i<fr.length; i++ )
            fr[i] = parent().getField(props.get(i));
        return fr;
    }

    /**
     * Returns the super class of this class, if it has the
     * super class and it is also a JAXB-bound class.
     * Otherwise null.
     */
    public final ClassOutline getSuperClass() {
        CClassInfo s = target.getBaseClass();
        if(s==null)     return null;
        return parent().getClazz(s);
    }
}

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
package com.sun.tools.xjc.reader.xmlschema.ct;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.BindingComponent;
import com.sun.xml.xsom.XSComplexType;

/**
 * single entry point of building a field expression from a complex type.
 *
 * One object is created for one {@link BGMBuilder}.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ComplexTypeFieldBuilder extends BindingComponent {

    /**
     * All installed available complex type builders.
     *
     * <p>
     * Builders are tried in this order, to put specific ones first.
     */
    private final CTBuilder[] complexTypeBuilders = new CTBuilder[]{
//        new ChoiceContentComplexTypeBuilder(),
        new MixedComplexTypeBuilder(),
        new FreshComplexTypeBuilder(),
        new ExtendedComplexTypeBuilder(),
        new RestrictedComplexTypeBuilder(),
        new STDerivedComplexTypeBuilder()
    };

    /** Records ComplexTypeBindingMode for XSComplexType. */
    private final Map<XSComplexType,ComplexTypeBindingMode> complexTypeBindingModes =
        new HashMap<XSComplexType,ComplexTypeBindingMode>();

    /**
     * Binds a complex type to a field expression.
     */
    public void build( XSComplexType type ) {
        for( CTBuilder ctb : complexTypeBuilders )
            if( ctb.isApplicable(type) ) {
                ctb.build(type);
                return;
            }

        assert false; // shall never happen
    }

    /**
     * Records the binding mode of the given complex type.
     *
     * <p>
     * Binding of a derived complex type often depends on that of the
     * base complex type. For example, when a base type is bound to
     * the getRest() method, all the derived complex types will be bound
     * in the same way.
     *
     * <p>
     * For this reason, we have to record how each complex type is being
     * bound.
     */
    public void recordBindingMode( XSComplexType type, ComplexTypeBindingMode flag ) {
        // it is an error to override the flag.
        Object o = complexTypeBindingModes.put(type,flag);
        assert o==null;
    }

    /**
     * Obtains the binding mode recorded through
     * {@link #recordBindingMode(XSComplexType, ComplexTypeBindingMode)}.
     */
    protected ComplexTypeBindingMode getBindingMode( XSComplexType type ) {
        ComplexTypeBindingMode r = complexTypeBindingModes.get(type);
        assert r!=null;
        return r;
    }
}

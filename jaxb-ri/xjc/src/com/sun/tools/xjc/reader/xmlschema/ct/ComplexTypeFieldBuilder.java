/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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

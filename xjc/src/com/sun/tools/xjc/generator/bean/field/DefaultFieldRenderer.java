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
package com.sun.tools.xjc.generator.bean.field;

import java.util.ArrayList;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * Default implementation of the FieldRendererFactory
 * that faithfully implements the semantics demanded by the JAXB spec.
 *
 * <p>
 * This class is just a facade --- it just determines which
 * {@link FieldRenderer} to use and just delegate the work.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class DefaultFieldRenderer implements FieldRenderer {

    private final FieldRendererFactory frf;
    
    /**
     * Use {@link FieldRendererFactory#getDefault()}.
     */
    DefaultFieldRenderer(FieldRendererFactory frf) {
        this.frf = frf;
    }

    public DefaultFieldRenderer(FieldRendererFactory frf, FieldRenderer defaultCollectionFieldRenderer ) {
        this.frf = frf;
        this.defaultCollectionFieldRenderer = defaultCollectionFieldRenderer;
    }
    
    private FieldRenderer defaultCollectionFieldRenderer;


    public FieldOutline generate(ClassOutlineImpl outline, CPropertyInfo prop) {
        return decideRenderer(outline,prop).generate(outline,prop);
    }
    
    private FieldRenderer decideRenderer(ClassOutlineImpl outline,CPropertyInfo prop) {
        if(!prop.isCollection()) {
            // non-collection field

            // TODO: check for bidning info for optionalPrimitiveType=boxed or
            // noHasMethod=false and noDeletedMethod=false
            if(prop.isUnboxable())
                // this one uses a primitive type as much as possible
                return frf.getRequiredUnboxed();
            else
                // otherwise use the default non-collection field
                return frf.getSingle();
        }
        
        if( defaultCollectionFieldRenderer==null ) {
            return frf.getList(outline.parent().getCodeModel().ref(ArrayList.class));
        }
        
        // this field is a collection field.
        // use untyped list as the default. This is consistent
        // to the JAXB spec.
        return defaultCollectionFieldRenderer;
    }
}

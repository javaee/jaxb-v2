/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
public class DefaultFieldRenderer implements FieldRenderer {
    
    /**
     * Use {@link FieldRenderer#DEFAULT}.
     */
    DefaultFieldRenderer() {}

    public DefaultFieldRenderer( FieldRenderer defaultCollectionFieldRenderer ) {
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
                return FieldRenderer.REQUIRED_UNBOXED;
            else
                // otherwise use the default non-collection field
                return FieldRenderer.SINGLE;
        }
        
        if( defaultCollectionFieldRenderer==null ) {
            return new UntypedListFieldRenderer(outline.parent().getCodeModel().ref(ArrayList.class));
        }
        
        // this field is a collection field.
        // use untyped list as the default. This is consistent
        // to the JAXB spec.
        return defaultCollectionFieldRenderer;
    }
}

/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.generator.bean.field;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;


/**
 * Abstract model of one field in a generated class.
 * 
 * <p>
 * Responsible for "realizing" a Java property by actually generating
 * members(s) to store the property value and a set of methods
 * to manipulate them.
 * 
 * <p>
 * Objects that implement this interface also encapsulates the
 * <b>internal</b> access to the field.
 * 
 * <p>
 * For discussion of the model this interface is representing, see
 * the "field meta model" design document.
 * 
 * REVISIT:
 *  refactor this to two interfaces that provide
 *  (1) internal access and (2) external access.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface FieldRenderer {
    /**
     * Generates accesssors and fields for the given implementation
     * class, then return {@link FieldOutline} for accessing
     * the generated field.
     */
    public FieldOutline generate( ClassOutlineImpl context, CPropertyInfo prop );
    
    //
    // field renderers
    //
    public static final FieldRenderer CONST
        = new GenericFieldRenderer(ConstField.class);
    
    public static final FieldRenderer DEFAULT
        = new DefaultFieldRenderer();
    
    public static final FieldRenderer ARRAY
        = new GenericFieldRenderer(ArrayField.class);
    
    public static final FieldRenderer REQUIRED_UNBOXED
        = new GenericFieldRenderer(UnboxedField.class);
    
    public static final FieldRenderer SINGLE
        = new GenericFieldRenderer(SingleField.class);

    public static final FieldRenderer SINGLE_PRIMITIVE_ACCESS
        = new GenericFieldRenderer(SinglePrimitiveAccessField.class);

    public static final FieldRenderer JAXB_DEFAULT
        = new DefaultFieldRenderer();
}

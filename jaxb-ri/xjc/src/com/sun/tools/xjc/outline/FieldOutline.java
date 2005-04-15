/*
 * @(#)$Id: FieldOutline.java,v 1.1 2005-04-15 20:09:34 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.outline;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CPropertyInfo;

/**
 * Representation of a field of {@link ClassOutline}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface FieldOutline {

    /**
     * Gets the enclosing {@link ClassOutline}.
     */
    ClassOutline parent();

    /** Gets the corresponding model object. */
    CPropertyInfo getPropertyInfo();
    
    /**
     * Gets the type of the "raw value".
     * 
     * <p>
     * This type can represent the entire value of this field.
     * For fields that can carry multiple values, this is an array.
     *
     * <p>
     * This type allows the client of the outline to generate code
     * to set/get values from a property.
     */
    JType getRawType();
    
    /**
     * Creates a new {@link FieldAccessor} of this field
     * for the specified object.
     * 
     * @param targetObject
     *      Evaluates to an object, and the field on this object
     *      will be accessed.
     */
    FieldAccessor create( JExpression targetObject );

//    /**
//     * Get the type of the object returned from the getValue method.
//     *
//     * @return
//     *      A JClass object that represents the type. This method
//     *      needs to return a JClass, not JPrimitiveType since
//     *      the getContent method must be able to return null.
//     */
//    // TODO: do we still need this?
//    JClass getContentValueType();

    /**
     * Get a code block that will be executed when the state of
     * this field changes from a null state to a non-null state.
     * (the unset state to the set state.)
     *
     * <p>
     * This method can be called only after the generate method is
     * called.
     *
     * @return
     *      Always return non-null object.
     */
    // TODO: do we still need this?
    JBlock getOnSetEventHandler();
}

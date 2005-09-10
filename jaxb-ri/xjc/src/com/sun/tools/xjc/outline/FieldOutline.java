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

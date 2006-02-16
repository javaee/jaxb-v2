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
}

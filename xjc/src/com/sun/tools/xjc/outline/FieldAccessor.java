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
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CPropertyInfo;

/**
 * Encapsulates the access on a field.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface FieldAccessor {

    /**
     * Generate a code to add the "newValue" to this field
     * and set it to the 'body'.
     */
    void add( JBlock body, JExpression newValue );

    /**
     * Dumps everything in this field into the given array,
     * which is guaranteed to have the enough space to store
     * all the values (that is, the caller is responsible
     * for ensuring the size of the array.)
     *
     * The type of the array must be the same
     * as <code>getPropertyInfo().getType()</code>.
     */
    void toArray( JBlock block, JExpression $array );

    /**
     * Dumps everything in this field into the given variable.
     * 
     * <p>
     * This generates code that accesses the field from outside.
     * 
     * @param block
     *      The code will be generated into this block.
     * @param $var
     *      Variable whose type is {@link FieldOutline#getRawType()}
     */
    void toRawValue( JBlock block, JVar $var );
    
    /**
     * Sets the value of the field from the specified expression.
     * 
     * <p>
     * This generates code that accesses the field from outside.
     * 
     * @param block
     *      The code will be generated into this block.
     * @param uniqueName
     *      Identifier that the caller guarantees to be unique in
     *      the given block. When the callee needs to produce additional
     *      variables, it can do so by adding suffixes to this unique
     *      name. For example, if the uniqueName is "abc", then the 
     *      caller guarantees that any identifier "abc.*" is unused
     *      in this block.
     * @param $var
     *      The expression that evaluates to a value of the type
     *      {@link FieldOutline#getRawType()}.
     */
    void fromRawValue( JBlock block, String uniqueName, JExpression $var );
    
    /**
     * Generates a code fragment to remove any "set" value
     * and move this field to the "unset" state.
     * 
     * @param body
     *      The code will be appended at the end of this block.
     */
    void unsetValues( JBlock body );
    
    /**
     * Return an expression that evaluates to true only when
     * this field has a set value(s).
     * 
     * @return null
     *      if the isSetXXX/unsetXXX method does not make sense 
     *      for the given field.
     */
    JExpression hasSetValue();

    /**
     * Get the "content" of this field in one object.
     * The type of this expression is the type returned by the
     * getValueType method.
     * 
     * <p>
     * The notion of "content" is defined in the spec.
     * Typically, it is a single {@link Object} that represents
     * the field. For fields with a primitive value, this is a boxed
     * type. For a list field, this is a {@link java.util.List}.
     * 
     * <p>
     * This object will be returned from the getContent method
     * of the choice content interface.
     */
    JExpression getContentValue();

    /**
     * Gets the {@link FieldOutline} from which
     * this object is created.
     */
    FieldOutline owner();

    /**
     * Short for <tt>owner().getPropertyInfo()</tt>
     */
    CPropertyInfo getPropertyInfo();
}

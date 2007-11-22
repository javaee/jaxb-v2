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
     * Gets the {@link FieldOutline} from which
     * this object is created.
     */
    FieldOutline owner();

    /**
     * Short for <tt>owner().getPropertyInfo()</tt>
     */
    CPropertyInfo getPropertyInfo();
}

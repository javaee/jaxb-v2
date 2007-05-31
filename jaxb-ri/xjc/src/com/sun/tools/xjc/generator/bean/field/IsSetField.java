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
package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class IsSetField extends AbstractField {

    private final FieldOutline core;

    private final boolean generateUnSetMethod;
    private final boolean generateIsSetMethod;

    protected IsSetField( ClassOutlineImpl outline, CPropertyInfo prop,
            FieldOutline core, boolean unsetMethod, boolean issetMethod ) {
        super(outline,prop);
        this.core = core;
        this.generateIsSetMethod = issetMethod;
        this.generateUnSetMethod = unsetMethod;
        
        generate(outline,prop);
    }
    
    
    private void generate( ClassOutlineImpl outline, CPropertyInfo prop ) {
        // add isSetXXX and unsetXXX.
        MethodWriter writer = outline.createMethodWriter();
        
        JCodeModel codeModel = outline.parent().getCodeModel();
        
        FieldAccessor acc = core.create(JExpr._this());
        
        if( generateIsSetMethod ) {
            // [RESULT] boolean isSetXXX()
            JExpression hasSetValue = acc.hasSetValue();
            if( hasSetValue==null ) {
                // this field renderer doesn't support the isSet/unset methods generation.
                // issue an error
                throw new UnsupportedOperationException();
            }
            writer.declareMethod(codeModel.BOOLEAN,"isSet"+this.prop.getName(true))
                .body()._return( hasSetValue );
        }
        
        if( generateUnSetMethod ) {
            // [RESULT] void unsetXXX()
            acc.unsetValues(
                writer.declareMethod(codeModel.VOID,"unset"+this.prop.getName(true)).body() );
        }
    }

    public JType getRawType() {
        return core.getRawType();
    }
    
    public FieldAccessor create(JExpression targetObject) {
        return new Accessor(targetObject);
    }
    
    private class Accessor extends AbstractField.Accessor {
        
        private final FieldAccessor core;
        
        Accessor( JExpression $target ) {
            super($target);
            this.core = IsSetField.this.core.create($target);
        }
        

        public void unsetValues( JBlock body ) {
            core.unsetValues(body);
        }
        public JExpression hasSetValue() {
            return core.hasSetValue();
        }
        public void toRawValue(JBlock block, JVar $var) {
            core.toRawValue(block,$var);
        }

        public void fromRawValue(JBlock block, String uniqueName, JExpression $var) {
            core.fromRawValue(block,uniqueName,$var);
        }
    }
}

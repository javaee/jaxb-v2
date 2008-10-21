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

import com.sun.tools.xjc.Options;
import com.sun.codemodel.JClass;

/**
 * Factory for {@link FieldRenderer}.
 *
 * <p>
 * This class can be overridden by a plugin to change the code generation
 * behavior of XJC. Note that such changes aren't composable; for a given
 * schema compilation, only one instance of {@link FieldRendererFactory} is
 * used.
 *
 * <p>
 * See {@link Options#fieldRendererFactory}
 *
 * <p>
 * To be more precise, since {@link FieldRenderer} is just a strategy pattern
 * and by itself is stateless, the "factory methods" don't necessarily need
 * to create new instances of {@link FieldRenderer} --- it can just return
 * a set of pre-created instances.
 *
 * @author Kohsuke Kawaguchi
 */
public class FieldRendererFactory {

    public FieldRenderer getDefault() {
        return DEFAULT;
    }
    public FieldRenderer getArray() {
        return ARRAY;
    }
    public FieldRenderer getRequiredUnboxed() {
        return REQUIRED_UNBOXED;
    }
    public FieldRenderer getSingle() {
        return SINGLE;
    }
    public FieldRenderer getSinglePrimitiveAccess() {
        return SINGLE_PRIMITIVE_ACCESS;
    }
    public FieldRenderer getList(JClass coreList) {
        return new UntypedListFieldRenderer(coreList);
    }
    public FieldRenderer getContentList(JClass coreList) {
        return new UntypedListFieldRenderer(coreList, false, true);
    }
    public FieldRenderer getDummyList(JClass coreList) {
        return new UntypedListFieldRenderer(coreList, true, false);
    }
    public FieldRenderer getConst(FieldRenderer fallback) {
        return new ConstFieldRenderer(fallback);
    }

    private final FieldRenderer DEFAULT
        = new DefaultFieldRenderer(this);

    private static final FieldRenderer ARRAY
        = new GenericFieldRenderer(ArrayField.class);

    private static final FieldRenderer REQUIRED_UNBOXED
        = new GenericFieldRenderer(UnboxedField.class);

    private static final FieldRenderer SINGLE
        = new GenericFieldRenderer(SingleField.class);

    private static final FieldRenderer SINGLE_PRIMITIVE_ACCESS
        = new GenericFieldRenderer(SinglePrimitiveAccessField.class);
}

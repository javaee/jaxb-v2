/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
    public FieldOutline generate( ClassOutlineImpl context, CPropertyInfo prop);
    
//    //
//    // field renderers
//    //
//    public static final FieldRenderer DEFAULT
//        = new DefaultFieldRenderer();
//
//    public static final FieldRenderer ARRAY
//        = new GenericFieldRenderer(ArrayField.class);
//
//    public static final FieldRenderer REQUIRED_UNBOXED
//        = new GenericFieldRenderer(UnboxedField.class);
//
//    public static final FieldRenderer SINGLE
//        = new GenericFieldRenderer(SingleField.class);
//
//    public static final FieldRenderer SINGLE_PRIMITIVE_ACCESS
//        = new GenericFieldRenderer(SinglePrimitiveAccessField.class);
//
//    public static final FieldRenderer JAXB_DEFAULT
//        = new DefaultFieldRenderer();
}

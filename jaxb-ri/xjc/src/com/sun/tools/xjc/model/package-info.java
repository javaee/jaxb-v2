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

/**
 * Implementation of the {@link com.sun.xml.bind.v2.model.core} package for XJC.
 *
 * <p>
 * This model is the recipes for the code generation.
 * It captures the essence of the JAXB-bound beans,
 * so that the actual Java code can be generated from this object model
 * mechanically without knowing anything about how the model was built.
 *
 * <p>
 * Most of the classes/interfaces in this package has one-to-one relationship
 * with the parameterized core model in the {@link com.sun.xml.bind.v2.model.core} package.
 * Refer to the core model for better documentation.
 *
 * <p>
 * The model for XJC also exposes a few additional information on top of the core model.
 * Those are defined in this package. This includes such information as:
 *
 * <dl>
 *  <dt>Source location information
 *  <dd>{@link Locator} object that can be used to tell where the model components
 *      are created from in terms of the source file. Useful for error reporting.
 *
 *  <dt>Source schema component
 *  <dd>{@link XSComponent} object from which the model components are created from.
 *      See {@link CCustomizable#getSchemaComponent()} for example.
 *
 *  <dt>Plugin customizations
 *  <dd>See {@link CCustomizable}.
 * </dl>
 */
package com.sun.tools.xjc.model;

import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;
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
package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CElementInfo;

/**
 * Generates <code>ObjectFactory</code> then wraps it and provides
 * access to it.
 *
 * <p>
 * The ObjectFactory contains
 * factory methods for each schema derived content class
 *
 * @author
 *      Ryan Shoemaker
 */
public abstract class ObjectFactoryGenerator {
    /**
     * Adds code for the given {@link CElementInfo} to ObjectFactory.
     */
    abstract void populate( CElementInfo ei );

    /**
     * Adds code that is relevant to a given {@link ClassOutlineImpl} to
     * ObjectFactory.
     */
    abstract void populate( ClassOutlineImpl cc );

    /**
     * Returns a reference to the generated (public) ObjectFactory
     */
    public abstract JDefinedClass getObjectFactory();
}

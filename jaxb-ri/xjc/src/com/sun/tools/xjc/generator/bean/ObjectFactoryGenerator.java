/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

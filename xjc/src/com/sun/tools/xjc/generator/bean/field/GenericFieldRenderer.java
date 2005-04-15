/*
 * @(#)$Id: GenericFieldRenderer.java,v 1.1 2005-04-15 20:09:08 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean.field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * Creates 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class GenericFieldRenderer implements FieldRenderer {
    private Constructor constructor;
    
    GenericFieldRenderer( Class fieldClass ) {
        try {
            constructor = fieldClass.getDeclaredConstructor(new Class[]{ClassOutlineImpl.class,CPropertyInfo.class});
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    
    public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo prop) {
        try {
            return (FieldOutline)constructor.newInstance(new Object[]{context,prop});
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if(t instanceof RuntimeException)
                throw (RuntimeException)t;
            if(t instanceof Error)
                throw (Error)t;
            
            // impossible
            throw new AssertionError(t);
        }
    }
    
}

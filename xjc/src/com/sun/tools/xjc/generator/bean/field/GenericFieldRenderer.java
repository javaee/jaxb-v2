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

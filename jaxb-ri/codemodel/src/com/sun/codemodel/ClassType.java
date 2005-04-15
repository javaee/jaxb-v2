/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved. SUN
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;
/**
 * This helps enable whether the JDefinedClass is a Class or Interface or
 * AnnotationTypeDeclaration or Enum
 * @author
 *     Bhakti Mehta (bhakti.mehta@sun.com)
 *  
 */
public final class ClassType {
    // the ClassType value;
    final int classType;
    
    private ClassType(int classTypeVal) {
        this.classType = classTypeVal;
    }
    public static final int _Class = 0;
    public static final int _Interface = 1;
    public static final int _AnnotationTypeDeclaration = 2;
    public static final int _Enum = 3;
    
    public static final ClassType CLASS = new ClassType(_Class);
    public static final ClassType INTERFACE = new ClassType(_Interface);
    public static final ClassType ANNOTATION_TYPE_DECL = new ClassType(
            _AnnotationTypeDeclaration);
    public static final ClassType ENUM = new ClassType(_Enum);
    
    public int value(){
    	return classType;
    }
}

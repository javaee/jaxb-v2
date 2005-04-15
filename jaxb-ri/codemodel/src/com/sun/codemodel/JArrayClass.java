/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

import java.util.Iterator;

/**
 * Array class.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class JArrayClass extends JClass {
    
    // array component type
    private final JType componentType;
    
    
    JArrayClass( JCodeModel owner, JType component ) {
        super(owner);
        this.componentType = component;
    }
    
    
    public String name() {
        return componentType.name()+"[]";
    }
    
    public String fullName() {
        return componentType.fullName()+"[]";
    }

    public void generate(JFormatter f) {
        f.g(componentType).p("[]");
    }

    public JPackage _package() {
        return owner().rootPackage();
    }

    public JClass _extends() {
        return owner().ref(Object.class);
    }

    public Iterator _implements() {
        return emptyIterator;
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    
    /** Iterator that walks on an empty list. */
    private static Iterator emptyIterator = new Iterator() {
        public boolean hasNext() { return false; }
        public Object next() { throw new IllegalStateException(); }
        public void remove() { throw new UnsupportedOperationException(); }
    };
    
    public JType elementType() {
        return componentType;
    }

    public boolean isArray() {
        return true;
    }


    //
    // Equality is based on value
    //

    public boolean equals(Object obj) {
        if(!(obj instanceof JArrayClass))   return false;
        
        if( componentType.equals( ((JArrayClass)obj).componentType ) )
            return true;
        
        return false;
    }

    public int hashCode() {
        return componentType.hashCode();
    }


    public JTypeVar[] typeParams() {
        return JTypeVar.EMPTY_ARRAY;
    }


    protected JClass substituteParams(JTypeVar[] variables, JClass[] bindings) {
        if( componentType.isPrimitive() )
            return this;
        
        JClass c = ((JClass)componentType).substituteParams(variables,bindings);
        if(c==componentType)
            return this;
        
        return new JArrayClass(owner(),c);
    }

}

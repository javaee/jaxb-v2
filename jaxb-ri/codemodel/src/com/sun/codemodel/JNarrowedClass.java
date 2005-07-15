/*
 * @(#)$Id: JNarrowedClass.java,v 1.3 2005-07-15 21:49:26 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.codemodel;

import java.util.Iterator;

/**
 * Represents X&lt;Y>.
 *
 * TODO: consider separating the decl and the use.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class JNarrowedClass extends JClass {
    /**
     * A generic class with type parameters.
     */
    final JClass basis;
    /**
     * Arguments to those parameters.
     */
    private final JClass[] args;

    JNarrowedClass(JClass basis, JClass arg) {
        this(basis,new JClass[]{arg});
    }
    
    JNarrowedClass(JClass basis, JClass[] args) {
        super(basis.owner());
        this.basis = basis;
        assert !(basis instanceof JNarrowedClass);
        this.args = args;
    }

    public JClass narrow( JClass clazz ) {
        JClass[] newArgs = new JClass[args.length+1];
        System.arraycopy(args,0,newArgs,0,args.length);
        newArgs[args.length] = clazz;
        return new JNarrowedClass(basis,newArgs);
    }

    public JClass narrow( JClass... clazz ) {
        JClass[] newArgs = new JClass[args.length+clazz.length];
        System.arraycopy(args,0,newArgs,0,args.length);
        System.arraycopy(clazz,0,newArgs,args.length,clazz.length);
        return new JNarrowedClass(basis,newArgs);
    }


    public String name() {
        StringBuffer buf = new StringBuffer();
        buf.append(basis.name());
        buf.append('<');
        for( int i=0; i<args.length; i++ ) {
            if(i!=0)    buf.append(',');
            buf.append(args[i].name());
        }
        buf.append('>');
        return buf.toString();
    }
    
    public String fullName() {
        StringBuffer buf = new StringBuffer();
        buf.append(basis.fullName());
        buf.append('<');
        for( int i=0; i<args.length; i++ ) {
            if(i!=0)    buf.append(',');
            buf.append(args[i].fullName());
        }
        buf.append('>');
        return buf.toString();
    }

    public void generate(JFormatter f) {
        f.t(basis).p('<');
        for( int i=0; i<args.length; i++ ) {
            if(i!=0)    f.p(',');
            f.g(args[i]);
        }
        f.p(JFormatter.CLOSE_TYPE_ARGS);
    }

    @Override
    void printLink(JFormatter f) {
        f.p("{@code ");
        basis.printLink(f);
        f.p('<');
        boolean first = true;
        for( JClass p : args ) {
            if(first)
                first = false;
            else
                f.p('.');
        }
        f.p(">}");
    }

    public JPackage _package() {
        return basis._package();
    }

    public JClass _extends() {
        JClass base = basis._extends();
        if(base==null)  return base;
        return base.substituteParams(basis.typeParams(),args);
    }

    public Iterator _implements() {
        return new Iterator() {
            private final Iterator core = basis._implements();
            public void remove() {
                core.remove();
            }
            public Object next() {
                return ((JClass)core.next()).substituteParams(basis.typeParams(),args);
            }
            public boolean hasNext() {
                return core.hasNext();
            }
        };
    }

    public JClass erasure() {
        return basis;
    }

    public boolean isInterface() {
        return basis.isInterface();
    }

    public boolean isAbstract() {
        return basis.isAbstract();
    }

    public boolean isArray() {
        return false;
    }


    //
    // Equality is based on value
    //

    public boolean equals(Object obj) {
        if(!(obj instanceof JNarrowedClass))   return false;
        return fullName().equals(((JClass)obj).fullName());
    }

    public int hashCode() {
        return fullName().hashCode();
    }

    protected JClass substituteParams(JTypeVar[] variables, JClass[] bindings) {
        JClass b = basis.substituteParams(variables,bindings);
        boolean different = b!=basis;
        
        JClass[] clazz = new JClass[args.length];
        for( int i=0; i<clazz.length; i++ ) {
            clazz[i] = args[i].substituteParams(variables,bindings);
            different |= clazz[i] != args[i];
        }
        
        if(different)
            return new JNarrowedClass(b,clazz);
        else
            return this;
    }

    public JClass getTypeParameter(int index) {
        return args[index];
    }
}

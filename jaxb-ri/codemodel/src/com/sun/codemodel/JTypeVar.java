/*
 * @(#)$Id: JTypeVar.java,v 1.2 2005-05-23 14:34:37 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Type variable used to declare generics.
 * 
 * <p>
 * A type variable may have one class bound, and any number of
 * interface bounds (which are usually written as
 * <code>T extends A&amp;B&amp;C</code>.
 * 
 * @see JGenerifiable
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class JTypeVar extends JClass implements JDeclaration {
    
    private final String name;
    
    private JClass classBound;
    
    private List<JClass> interfaceBounds;
    
    JTypeVar(JCodeModel owner, String _name) {
        super(owner);
        this.name = _name;
    }
    
    public String name() {
        return name;
    }

    public String fullName() {
        return name;
    }

    public JPackage _package() {
        return null;
    }
    
    /**
     * Adds a bound to this variable.
     * 
     * @return  this
     */
    public JTypeVar bound( JClass c ) {
        if( c.isInterface() ) {
            if(interfaceBounds==null)
                interfaceBounds = new ArrayList<JClass>(1);
            interfaceBounds.add(c);
        } else {
            if(classBound!=null)
                throw new IllegalArgumentException("type variable has an existing class bound "+classBound);
            classBound = c;
        }
        return this;
    }

    /**
     * Returns the class bound of this variable.
     * 
     * <p>
     * If no bound is given, this method returns {@link Object}.
     */
    public JClass _extends() {
        if(classBound!=null)
            return classBound;
        else
            return owner().ref(Object.class);
    }

    /**
     * Returns the interface bounds of this variable.
     */
    public Iterator _implements() {
        if(interfaceBounds==null)
            return Collections.EMPTY_LIST.iterator();
        return interfaceBounds.iterator();
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    /**
     * Prints out the declaration of the variable.
     */
    public void declare(JFormatter f) {
        f.id(name);
        if(interfaceBounds!=null) {
            f.p("extends");
            for(int i=0;i<interfaceBounds.size();i++) {
                if(i!=0)    f.p('&');
                f.g(interfaceBounds.get(i));
            }
        }
    }


    public JTypeVar[] typeParams() {
        return EMPTY_ARRAY;
    }

    protected JClass substituteParams(JTypeVar[] variables, JClass[] bindings) {
        for(int i=0;i<variables.length;i++)
            if(variables[i]==this)
                return bindings[i];
        return this;
    }

    public void generate(JFormatter f) {
        f.id(name);
    }

    /**
     * Sometimes useful reusable empty array.
     */
    static final JTypeVar[] EMPTY_ARRAY = new JTypeVar[0];
}

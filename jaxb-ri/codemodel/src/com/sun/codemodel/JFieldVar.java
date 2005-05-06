/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;




/**
 * A field that can have a {@link JDocComment} associated with it
 */
public class JFieldVar extends JVar {

    /**
     * javadoc comments for this JFieldVar
     */
    private JDocComment jdoc = null;

    private final JCodeModel owner;


    /**
     * JFieldVar constructor
     *
     * @param type
     *        Datatype of this variable
     *
     * @param name
     *        Name of this variable
     *
     * @param init
     *        Value to initialize this variable to
     */
    JFieldVar(JCodeModel owner, JMods mods, JType type, String name, JExpression init) {
        super( mods, type, name, init );
        this.owner = owner;
    }
    /**
     * Creates, if necessary, and returns the class javadoc for this
     * JDefinedClass
     *
     * @return JDocComment containing javadocs for this class
     */
    public JDocComment javadoc() {
        if( jdoc == null ) 
                jdoc = new JDocComment(owner);
        return jdoc;
    }

    public void declare(JFormatter f) {
        if( jdoc != null )
            f.g( jdoc );
        super.declare( f );
    }

   
}


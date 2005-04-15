/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel.fmt;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sun.codemodel.JResourceFile;


/**
 * A simple class that takes an object and serializes it into a file
 * in the parent package with the given name.
 */
public class JSerializedObject extends JResourceFile {

    private final Object obj;
    
    /**
     * @exception   IOException
     *      If the serialization fails, this exception is thrown
     */
    public JSerializedObject( String name, Object obj ) throws IOException {
        super(name);
        this.obj = obj;
    }
    
    /**
     * called by JPackage to serialize the object 
     */
    protected void build( OutputStream os ) throws IOException {
        // serialize the obj into a ByteArrayOutputStream
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(obj);
        oos.close();
    }
}

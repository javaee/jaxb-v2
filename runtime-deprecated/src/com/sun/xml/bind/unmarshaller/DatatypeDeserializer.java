/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.unmarshaller;

import java.io.IOException;
import java.io.ObjectInputStream;

import com.sun.xml.bind.StringInputStream;

import org.relaxng.datatype.Datatype;

/**
 * Extracts a {@link Datatype} object from its serialized form.
 * 
 * @since JAXB1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DatatypeDeserializer {
    public static Datatype deserialize( String str ) {
        try {
            ObjectInputStream ois = new ObjectInputStream( new StringInputStream(str) );
            Datatype dt = (Datatype)ois.readObject();
            ois.close();
        
            return dt;
        } catch( IOException e ) {
            throw new InternalError(e.getMessage());
        } catch( ClassNotFoundException e ) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
}

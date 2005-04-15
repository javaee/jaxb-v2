/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.bind.validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.xml.bind.StringInputStream;

/**
 * Extracts a grammar from a string-encoded byte stream.
 * 
 * @since JAXB1.0
 */
public class SchemaDeserializer
{
    public static Grammar deserialize( String str ) {
        return deserialize( new StringInputStream(str) );
    }
    
    public static Grammar deserializeCompressed( String str ) {
        try {
            return deserialize( new GZIPInputStream(new StringInputStream(str)) );
        } catch( IOException e ) {
            throw new InternalError(e.getMessage());
        }
    }
    
    private static Grammar deserialize( InputStream is ) {
        try {
            ObjectInputStream ois = new ObjectInputStream( is );
            final Expression exp = (Expression)ois.readObject();
            final ExpressionPool pool = (ExpressionPool)ois.readObject();
            ois.close();
        
            return new Grammar() {
                public Expression getTopLevel() { return exp; }
                public ExpressionPool getPool() { return pool; }
            };
        } catch( IOException e ) {
            throw new InternalError(e.getMessage());
        } catch( ClassNotFoundException e ) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
}

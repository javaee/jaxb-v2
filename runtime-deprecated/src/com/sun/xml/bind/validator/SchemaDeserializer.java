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

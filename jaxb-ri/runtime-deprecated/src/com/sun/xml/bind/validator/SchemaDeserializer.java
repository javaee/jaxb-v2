/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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

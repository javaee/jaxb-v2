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

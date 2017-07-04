/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.bind.v2.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.bind.Util;

/**
 * Replaces a few constant pool tokens from a class "template" and then loads it into the VM.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ClassTailor {

    private ClassTailor() {} // no instanciation please

    private static final Logger logger = Util.getClassLogger();

    /**
     * Returns the class name in the JVM format (such as "java/lang/String")
     */
    public static String toVMClassName( Class c ) {
        assert !c.isPrimitive();
        if(c.isArray())
            // I have no idea why it is designed like this, but javap says so.
            return toVMTypeName(c);
        return c.getName().replace('.','/');
    }

    public static String toVMTypeName( Class c ) {
        if(c.isArray()) {
            // TODO: study how an array type is encoded.
            return '['+toVMTypeName(c.getComponentType());
        }
        if(c.isPrimitive()) {
            if(c==Boolean.TYPE)     return "Z";
            if(c==Character.TYPE)   return "C";
            if(c==Byte.TYPE)        return "B";
            if(c==Double.TYPE)      return "D";
            if(c==Float.TYPE)       return "F";
            if(c==Integer.TYPE)     return "I";
            if(c==Long.TYPE)        return "J";
            if(c==Short.TYPE)       return "S";

            throw new IllegalArgumentException(c.getName());
        }
        return 'L'+c.getName().replace('.','/')+';';
    }



    public static byte[] tailor( Class templateClass, String newClassName, String... replacements ) {
        String vmname = toVMClassName(templateClass);
        return tailor(
            SecureLoader.getClassClassLoader(templateClass).getResourceAsStream(vmname+".class"),
            vmname, newClassName, replacements );
    }


    /**
     * Customizes a class file by replacing constant pools.
     *
     * @param image
     *      The image of the template class.
     * @param replacements
     *      A list of pair of strings that specify the substitution
     *      {@code String[]{search_0, replace_0, search_1, replace_1, ..., search_n, replace_n }}
     *
     *      The search strings found in the constant pool will be replaced by the corresponding
     *      replacement string.
     */
    public static byte[] tailor( InputStream image, String templateClassName, String newClassName, String... replacements ) {
        DataInputStream in = new DataInputStream(image);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            DataOutputStream out = new DataOutputStream(baos);

            // skip until the constant pool count
            long l = in.readLong();
            out.writeLong(l);

            // read the constant pool size
            short count = in.readShort();
            out.writeShort(count);

            // replace constant pools
            for( int i=0; i<count; i++ ) {
                byte tag = in.readByte();
                out.writeByte(tag);
                switch(tag) {
                case 0:
                    // this isn't described in the spec,
                    // but class files often seem to have this '0' tag.
                    // we can apparently just ignore it, but not sure
                    // what this really means.
                    break;

                case 1: // CONSTANT_UTF8
                    {
                        String value = in.readUTF();
                        if(value.equals(templateClassName))
                            value = newClassName;
                        else {
                            for( int j=0; j<replacements.length; j+=2 )
                                if(value.equals(replacements[j])) {
                                    value = replacements[j+1];
                                    break;
                                }
                        }
                        out.writeUTF(value);
                    }
                break;

                case 3: // CONSTANT_Integer
                case 4: // CONSTANT_Float
                    out.writeInt(in.readInt());
                    break;

                case 5: // CONSTANT_Long
                case 6: // CONSTANT_Double
                    i++; // doubles and longs take two entries
                    out.writeLong(in.readLong());
                    break;

                case 7: // CONSTANT_Class
                case 8: // CONSTANT_String
                    out.writeShort(in.readShort());
                    break;

                case 9: // CONSTANT_Fieldref
                case 10: // CONSTANT_Methodref
                case 11: // CONSTANT_InterfaceMethodref
                case 12: // CONSTANT_NameAndType
                    out.writeInt(in.readInt());
                    break;

                default:
                    throw new IllegalArgumentException("Unknown constant type "+tag);
                }
            }

            // then copy the rest
            byte[] buf = new byte[512];
            int len;
            while((len=in.read(buf))>0)
                out.write(buf,0,len);

            in.close();
            out.close();

            // by now we got the properly tailored class file image
            return baos.toByteArray();

        } catch( IOException e ) {
            // never happen
            logger.log(Level.WARNING,"failed to tailor",e);
            return null;
        }
    }
}

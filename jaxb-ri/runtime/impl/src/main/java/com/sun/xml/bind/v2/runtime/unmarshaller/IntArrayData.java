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

package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.io.IOException;

import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;

/**
 * Typed {@link CharSequence} for int[].
 *
 * <p>
 * Fed to unmarshaller when the 'text' data is actually
 * a virtual image of int array.
 *
 * <p>
 * This class holds int[] as a triplet of (data,start,len)
 * where 'start' and 'len' represents the start position of the
 * data and the length.
 *
 * @author Kohsuke Kawaguchi
 */
public final class IntArrayData extends Pcdata {

    private int[] data;
    private int start;
    private int len;

    /**
     * String representation of the data. Lazily computed.
     */
    private StringBuilder literal;


    public IntArrayData(int[] data, int start, int len) {
        set(data, start, len);
    }

    public IntArrayData() {
    }

    /**
     * Sets the int[] data to this object.
     *
     * <p>
     * This method doesn't make a copy for a performance reason.
     * The caller is still free to modify the array it passed to this method,
     * but he should do so with a care. The unmarshalling code isn't expecting
     * the value to be changed while it's being routed.
     */
    public void set(int[] data, int start, int len) {
        this.data = data;
        this.start = start;
        this.len = len;
        this.literal = null;
    }

    public int length() {
        return getLiteral().length();
    }

    public char charAt(int index) {
        return getLiteral().charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return getLiteral().subSequence(start,end);
    }

    /**
     * Computes the literal form from the data.
     */
    private StringBuilder getLiteral() {
        if(literal!=null)   return literal;

        literal = new StringBuilder();
        int p = start;
        for( int i=len; i>0; i-- ) {
            if(literal.length()>0)  literal.append(' ');
            literal.append(data[p++]);
        }

        return literal;
    }

    public String toString() {
        return literal.toString();
    }

    public void writeTo(UTF8XmlOutput output) throws IOException {
        int p = start;
        for( int i=len; i>0; i-- ) {
            if(i!=len)
                output.write(' ');
            output.text(data[p++]);
        }
    }
}

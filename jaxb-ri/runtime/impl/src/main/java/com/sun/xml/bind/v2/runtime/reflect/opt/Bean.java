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

package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

/**
 * Used by {@link TransducedAccessor} templates.
 *
 * <p>
 * Fields needs to have a distinctive name.
 *
 * @author Kohsuke Kawaguchi
 */
final class Bean {
    public boolean f_boolean;
    public char f_char;
    public byte f_byte;
    public short f_short;
    int f_int;
    public long f_long;
    public float f_float;
    public double f_double;
    /**
     * Field of a reference type.
     * We need a distinctive type so that it can be easily replaced.
     */
    public Ref f_ref;

    public boolean get_boolean() { throw new UnsupportedOperationException(); }
    public void set_boolean(boolean b) { throw new UnsupportedOperationException(); }

    public char get_char() { throw new UnsupportedOperationException(); }
    public void set_char(char b) { throw new UnsupportedOperationException(); }

    public byte get_byte() { throw new UnsupportedOperationException(); }
    public void set_byte(byte b) { throw new UnsupportedOperationException(); }

    public short get_short() { throw new UnsupportedOperationException(); }
    public void set_short(short b) { throw new UnsupportedOperationException(); }

    public int get_int() { throw new UnsupportedOperationException(); }
    public void set_int(int b) { throw new UnsupportedOperationException(); }

    public long get_long() { throw new UnsupportedOperationException(); }
    public void set_long(long b) { throw new UnsupportedOperationException(); }

    public float get_float() { throw new UnsupportedOperationException(); }
    public void set_float(float b) { throw new UnsupportedOperationException(); }

    public double get_double() { throw new UnsupportedOperationException(); }
    public void set_double(double b) { throw new UnsupportedOperationException(); }

    public Ref get_ref() { throw new UnsupportedOperationException(); }
    public void set_ref(Ref r) { throw new UnsupportedOperationException(); }
}

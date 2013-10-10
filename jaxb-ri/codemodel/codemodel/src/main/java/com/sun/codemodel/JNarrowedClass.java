/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.codemodel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Represents X&lt;Y>.
 *
 * TODO: consider separating the decl and the use.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class JNarrowedClass extends JClass {
    /**
     * A generic class with type parameters.
     */
    final JClass basis;
    /**
     * Arguments to those parameters.
     */
    private final List<JClass> args;

    JNarrowedClass(JClass basis, JClass arg) {
        this(basis,Collections.singletonList(arg));
    }
    
    JNarrowedClass(JClass basis, List<JClass> args) {
        super(basis.owner());
        this.basis = basis;
        assert !(basis instanceof JNarrowedClass);
        this.args = args;
    }

    @Override
    public JClass narrow( JClass clazz ) {
        List<JClass> newArgs = new ArrayList<JClass>(args);
        newArgs.add(clazz);
        return new JNarrowedClass(basis,newArgs);
    }

    @Override
    public JClass narrow( JClass... clazz ) {
        List<JClass> newArgs = new ArrayList<JClass>(args);
        newArgs.addAll(Arrays.asList(clazz));
        return new JNarrowedClass(basis,newArgs);
    }

    public String name() {
        StringBuilder buf = new StringBuilder();
        buf.append(basis.name());
        buf.append('<');
        boolean first = true;
        for (JClass c : args) {
            if(first)
                first = false;
            else
                buf.append(',');
            buf.append(c.name());
        }
        buf.append('>');
        return buf.toString();
    }
    
    public String fullName() {
        StringBuilder buf = new StringBuilder();
        buf.append(basis.fullName());
        buf.append('<');
        boolean first = true;
        for (JClass c : args) {
            if(first)
                first = false;
            else
                buf.append(',');
            buf.append(c.fullName());
        }
        buf.append('>');
        return buf.toString();
    }

    @Override
    public String binaryName() {
        StringBuilder buf = new StringBuilder();
        buf.append(basis.binaryName());
        buf.append('<');
        boolean first = true;
        for (JClass c : args) {
            if(first)
                first = false;
            else
                buf.append(',');
            buf.append(c.binaryName());
        }
        buf.append('>');
        return buf.toString();
    }

    @Override
    public void generate(JFormatter f) {
        f.t(basis).p('<').g(args).p(JFormatter.CLOSE_TYPE_ARGS);
    }

    @Override
    void printLink(JFormatter f) {
        basis.printLink(f);
        f.p("{@code <}");
        boolean first = true;
        for( JClass c : args ) {
            if(first)
                first = false;
            else
                f.p(',');
            c.printLink(f);
        }
        f.p("{@code >}");
    }

    public JPackage _package() {
        return basis._package();
    }

    public JClass _extends() {
        JClass base = basis._extends();
        if(base==null)  return base;
        return base.substituteParams(basis.typeParams(),args);
    }

    public Iterator<JClass> _implements() {
        return new Iterator<JClass>() {
            private final Iterator<JClass> core = basis._implements();
            public void remove() {
                core.remove();
            }
            public JClass next() {
                return core.next().substituteParams(basis.typeParams(),args);
            }
            public boolean hasNext() {
                return core.hasNext();
            }
        };
    }

    @Override
    public JClass erasure() {
        return basis;
    }

    public boolean isInterface() {
        return basis.isInterface();
    }

    public boolean isAbstract() {
        return basis.isAbstract();
    }

    @Override
    public boolean isArray() {
        return false;
    }


    //
    // Equality is based on value
    //

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof JNarrowedClass))   return false;
        return fullName().equals(((JClass)obj).fullName());
    }

    @Override
    public int hashCode() {
        return fullName().hashCode();
    }

    protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings) {
        JClass b = basis.substituteParams(variables,bindings);
        boolean different = b!=basis;
        
        List<JClass> clazz = new ArrayList<JClass>(args.size());
        for( int i=0; i<clazz.size(); i++ ) {
            JClass c = args.get(i).substituteParams(variables,bindings);
            clazz.set(i,c);
            different |= c != args.get(i);
        }
        
        if(different)
            return new JNarrowedClass(b,clazz);
        else
            return this;
    }

    @Override
    public List<JClass> getTypeParameters() {
        return args;
    }
}

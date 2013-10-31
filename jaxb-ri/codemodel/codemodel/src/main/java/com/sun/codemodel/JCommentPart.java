/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A part is a part of a javadoc comment, and it is a list of values.
 *
 * <p>
 * A part can contain a free-form text. This text is modeled as a collection of 'values'
 * in this class. A value can be a {@link JType} (which will be prinited with a @link tag),
 * anything that can be turned into a {@link String} via the {@link Object#toString()} method,
 * or a {@link Collection}/array of those objects.
 *
 * <p>
 * Values can be added through the various append methods one by one or in a bulk.
 *
 * @author Kohsuke Kawaguchi
 */
public class JCommentPart extends ArrayList<Object> {

	private static final long serialVersionUID = 1L;

	/**
     * Appends a new value.
     *
     * If the value is {@link JType} it will be printed as a @link tag.
     * Otherwise it will be converted to String via {@link Object#toString()}.
     */
    public JCommentPart append(Object o) {
        add(o);
        return this;
    }

    public boolean add(Object o) {
        flattenAppend(o);
        return true;
    }

    private void flattenAppend(Object value) {
        if(value==null) return;
        if(value instanceof Object[]) {
            for( Object o : (Object[])value)
                flattenAppend(o);
        } else
        if(value instanceof Collection<?>) {
            for( Object o : (Collection<?>)value)
                flattenAppend(o);
        } else
            super.add(value);
    }

    /**
     * Writes this part into the formatter by using the specified indentation.
     */
    protected void format( JFormatter f, String indent ) {
        if(!f.isPrinting()) {
            // quickly pass the types to JFormatter, as that's all we care.
            // we don't need to worry about the exact formatting of text.
            for( Object o : this )
                if(o instanceof JClass)
                    f.g((JClass)o);
            return;
        }

        if(!isEmpty())
            f.p(indent);

        Iterator<Object> itr = iterator();
        while(itr.hasNext()) {
            Object o = itr.next();

            if(o instanceof String) {
                int idx;
                String s = (String)o;
                while( (idx=s.indexOf('\n'))!=-1 ) {
                    String line = s.substring(0,idx);
                    if(line.length()>0)
                        f.p(escape(line));
                    s = s.substring(idx+1);
                    f.nl().p(indent);
                }
                if(s.length()!=0)
                    f.p(escape(s));
            } else
            if(o instanceof JClass) {
                // TODO: this doesn't print the parameterized type properly
                ((JClass)o).printLink(f);
            } else
            if(o instanceof JType) {
                f.g((JType)o);
            } else
                throw new IllegalStateException();
        }

        if(!isEmpty())
            f.nl();
    }

    /**
     * Escapes the appearance of the comment terminator.
     */
    private String escape(String s) {
        while(true) {
            int idx = s.indexOf("*/");
            if(idx <0)   return s;

            s = s.substring(0,idx+1)+"<!---->"+s.substring(idx+1);
        }
    }
}

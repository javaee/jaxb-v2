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

package com.sun.codemodel.fmt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JResourceFile;
import com.sun.codemodel.JTypeVar;

/**
 * Statically generated Java soruce file.
 * 
 * <p>
 * This {@link JResourceFile} implementation will generate a Java source
 * file by copying the source code from a resource.
 * <p>
 * While copying a resource, we look for a package declaration and
 * replace it with the target package name. This allows the static Java
 * source code to have an arbitrary package declaration.
 * <p>
 * You can also use the getJClass method to obtain a {@link JClass}
 * object that represents the static file. This allows the client code
 * to refer to the class from other CodeModel generated code.
 * <p>
 * Note that because we don't parse the static Java source code,
 * the returned {@link JClass} object doesn't respond to methods like
 * "isInterface" or "_extends",  
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class JStaticJavaFile extends JResourceFile {
    
    private final JPackage pkg;
    private final String className;
    private final URL source;
    private final JStaticClass clazz;
    private final LineFilter filter;
    
    public JStaticJavaFile(JPackage _pkg, String className, String _resourceName) {
        this( _pkg, className,
            SecureLoader.getClassClassLoader(JStaticJavaFile.class).getResource(_resourceName), null );
    }
    
    public JStaticJavaFile(JPackage _pkg, String _className, URL _source, LineFilter _filter ) {
        super(_className+".java");
        if(_source==null)   throw new NullPointerException();
        this.pkg = _pkg;
        this.clazz = new JStaticClass();
        this.className = _className;
        this.source = _source;
        this.filter = _filter;
    }
    
    /**
     * Returns a class object that represents a statically generated code. 
     */
    public final JClass getJClass() {
        return clazz;
    }

    protected boolean isResource() {
        return false;
    }

    protected  void build(OutputStream os) throws IOException {
        InputStream is = source.openStream();
        
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        PrintWriter w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
        LineFilter filter = createLineFilter();
        int lineNumber=1;
        
        try {
            String line;
            while((line=r.readLine())!=null) {
                line = filter.process(line);
                if(line!=null)
                    w.println(line);
                lineNumber++;
            }
        } catch( ParseException e ) {
            throw new IOException("unable to process "+source+" line:"+lineNumber+"\n"+e.getMessage());
        }
        
        w.close();
        r.close();
    }
    
    /**
     * Creates a {@link LineFilter}.
     * <p>
     * A derived class can override this method to process
     * the contents of the source file.
     */
    private LineFilter createLineFilter() {
        // this filter replaces the package declaration.
        LineFilter f = new LineFilter() {
            public String process(String line) {
                if(!line.startsWith("package ")) return line;
                
                // replace package decl
                if( pkg.isUnnamed() )
                    return null;
                else
                    return "package "+pkg.name()+";";
            }
        };
        if( filter!=null )
            return new ChainFilter(filter,f);
        else
            return f;
    }
    
    /**
     * Filter that alters the Java source code.
     * <p>
     * By implementing this interface, derived classes
     * can modify the Java source file before it's written out.
     */
    public interface LineFilter {
        /**
         * @param line
         *      a non-null valid String that corresponds to one line.
         *      No '\n' included.
         * @return
         *      null to strip the line off. Otherwise the returned
         *      String will be written out. Do not add '\n' at the end
         *      of this string.
         * 
         * @exception ParseException
         *      when for some reason there's an error in the line.
         */
        String process(String line) throws ParseException;
    }
    
    /**
     * A {@link LineFilter} that combines two {@link LineFilter}s.
     */
    public final static class ChainFilter implements LineFilter {
        private final LineFilter first,second;
        public ChainFilter( LineFilter first, LineFilter second ) {
            this.first=first;
            this.second=second;
        }
        public String process(String line) throws ParseException {
            line = first.process(line);
            if(line==null)  return null;
            return second.process(line);
        }
    }
    
    
    private class JStaticClass extends JClass {

        private final JTypeVar[] typeParams;
        
        JStaticClass() {
            super(pkg.owner());
            // TODO: allow those to be specified
            typeParams = new JTypeVar[0];
        }

        public String name() {
            return className;
        }
        
        public String fullName() {
            if(pkg.isUnnamed())
                return className;
            else
                return pkg.name()+'.'+className;
        }

        public JPackage _package() {
            return pkg;
        }

        public JClass _extends() {
            throw new UnsupportedOperationException();
        }

        public Iterator<JClass> _implements() {
            throw new UnsupportedOperationException();
        }

        public boolean isInterface() {
            throw new UnsupportedOperationException();
        }

        public boolean isAbstract() {
            throw new UnsupportedOperationException();
        }

        public JTypeVar[] typeParams() {
            return typeParams;
        }

        protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings) {
            return this;
        }
    };
}

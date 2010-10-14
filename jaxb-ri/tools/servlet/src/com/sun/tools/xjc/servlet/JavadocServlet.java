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

package com.sun.tools.xjc.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;

import com.sun.xml.bind.webapp.LongProcessServlet;

/**
 * Generates javadoc from the generated code and serve the generated javadoc.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JavadocServlet extends LongProcessServlet  {

    protected Thread createTask() throws ServletException, IOException {
        System.out.println("launching javadoc");
        Compiler compiler = Compiler.get(request);
        if( compiler==null )   return null;
        else                   return new JavadocThread(compiler); 
    }

    protected void renderResult(Thread task) throws ServletException, IOException {
        JavadocThread javadoc = (JavadocThread)task;
        
        if( !javadoc.success ) {
            request.setAttribute("msg",new String(javadoc.statusMessage));
            forward( "/javadocError.jsp" );
        } else {
            forward( "/file/javadoc"+request.getPathInfo() );
        }
    }

    protected String getProgressTitle() {
        return "Generating Javadoc";
    }

    protected String getProgressMessage() {
        return "you'll be redirected to the javadoc shortly";
    }

    
    // the class should be static since it's pointless to access the instance
    // of Servlet given the thread model of it.
    private static class JavadocThread extends Thread {
    
        /** Used to limit the number of concurrent compilation to 1. */
        private static final Object lock = new Object();
        
        private final Compiler compiler;
        boolean success = false;
        String statusMessage = "";
        
        JavadocThread( Compiler compiler ) {
            this.compiler = compiler;
        
            // be cooperative
            setPriority(Thread.NORM_PRIORITY-1);
        }
        
        public void run() {
            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            
            synchronized(lock) {
                try {
                    
                    File outDir = compiler.getOutDir();
                    /*int r =*/ JavadocGenerator.process(
                        outDir,
                        new File(outDir,"javadoc"),
                        new ForkOutputStream(System.out,msg) );
                    
                    // javadoc doesn't seem to return the correct exit code.
                    // so always assume a success.
                    // success = (r==0);
                    success = true;
                } catch( Throwable e ) {
                    e.printStackTrace();
                    PrintStream ps = new PrintStream(msg);
                    e.printStackTrace(ps);
                    ps.flush();
                }
            }

            statusMessage = new String(msg.toByteArray());
        }
    }
}

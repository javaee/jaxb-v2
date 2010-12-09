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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.gsc.SyntaxColorizer;
import org.kohsuke.gsc.sample.java.JavaColorizer;

import antlr.ANTLRException;

import com.sun.xml.bind.webapp.HttpServletEx;

/**
 * Serves a static Java source file in the output directory.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JavaSourceServlet extends HttpServletEx {
    
    private final static SyntaxColorizer colorizer = new JavaColorizer("/xjc/java.css");
    
    protected void run() throws ServletException, IOException {
        String fileName = request.getPathInfo().substring(1);
        File doc = new File( Compiler.get(request).getOutDir(), fileName );
        if( !doc.exists() ) {
            response.sendError(404);
            return;
        } else {
            response.setContentType("text/html");
            try {
                colorizer.colorize(
                    new BufferedReader(new FileReader(doc)),
                    new BufferedWriter(response.getWriter()) );
            } catch (ANTLRException e) {
                throw new ServletException(e);
            }
        }
    }
}

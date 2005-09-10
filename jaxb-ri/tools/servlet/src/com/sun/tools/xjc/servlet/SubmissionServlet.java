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
package com.sun.tools.xjc.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sun.xml.bind.webapp.HttpServletEx;

/**
 * Parses the form submission and build {@link Compiler}.
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SubmissionServlet extends HttpServletEx {
    
    protected void run() throws ServletException, IOException {
        System.out.println("accepting a new submission");
        
        // to workaround a bug in JWSDP1.2, all the XJC-related handling is done
        // in a separate class loaded by a separate class loader.
        Compiler compiler;
        try {
            Class parserClass = XJCClassLoader.getInstance(getServletContext())
                .loadClass("com.sun.tools.xjc.servlet.boxed.SubmissionParser");
                
                
            // launch compilation
            compiler = (Compiler)
                parserClass.getMethod("parse",new Class[]{HttpServletRequest.class})
                .invoke(null,new Object[]{request});
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
        
        // start a new session. remember the compiler so that we can access it later.
        HttpSession old = request.getSession(false);
        if( old!=null ) old.invalidate();
        
        compiler.associateTo(request);
        
        response.sendRedirect("compiler");

    }

}

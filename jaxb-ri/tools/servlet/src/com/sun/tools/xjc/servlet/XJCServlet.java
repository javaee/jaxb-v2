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

import com.sun.xml.bind.webapp.LongProcessServlet;

/**
 * Launches {@link Compiler} from the form input.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XJCServlet extends LongProcessServlet {
    
    protected void run() throws ServletException, IOException {
        // check if the browser supports session
        if( request.getSession(false)==null ) {
            response.sendRedirect("cookieDisabled.jsp");
            return;
        }
        
        super.run();
    }
    
    protected Thread createTask() throws ServletException, IOException {
        return Compiler.get(request);
    }

    protected void renderResult( Thread task ) throws ServletException, IOException {
        Compiler compiler = (Compiler)task;
        
        // forward to the result
        if( compiler.getZipFile()==null )
            response.sendRedirect("compileError.jsp");
        else
            response.sendRedirect("compileSuccess.jsp");
    }
    
    
    

    protected String getProgressTitle() {
        return "Compiling your schema";
    }

    protected String getProgressMessage() {
        return "your browser will be redirected to the result page once the compilation is completed";
    }

}

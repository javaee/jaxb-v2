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
package com.sun.xml.bind.webapp;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * More OO-fashioned servlet.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class HttpServletEx extends HttpServlet implements SingleThreadModel {
    
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.request  = request;
        this.response = response;
        run();
    }

    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.request  = request;
        this.response = response;
        run();
    }
    
    protected abstract void run() throws ServletException, IOException;
    
    
    /**
     * Fowards to the specified JSP/servlet.
     */
    protected final void forward(String jspPath) throws ServletException, IOException {
        RequestDispatcher dispatch = getServletContext().getRequestDispatcher(jspPath);
        dispatch.forward(request, response);
        
    }
}

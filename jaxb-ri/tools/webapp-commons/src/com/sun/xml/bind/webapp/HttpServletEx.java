/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

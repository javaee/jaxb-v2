/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.IOException;

import javax.servlet.ServletException;

import com.sun.xml.bind.webapp.HttpServletEx;

/**
 * Used for remote configuration.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AdminServlet extends HttpServletEx {

    protected void run() throws ServletException, IOException {
        if( "zoe".equals(request.getParameter("password")) ) {
            // if the password is wrong, nothing takes place
            
            String mailServer = request.getParameter("mailServer");
            if(mailServer!=null)    Mode.mailServer = mailServer;
            
            String homeAddress = request.getParameter("homeAddress");
            if(homeAddress!=null)    Mode.homeAddress = homeAddress;
            
            Mode.canUseDisk = "true".equals(request.getParameter("canUseDisk"));
        }
        
        forward("/admin.jsp");
    }

}

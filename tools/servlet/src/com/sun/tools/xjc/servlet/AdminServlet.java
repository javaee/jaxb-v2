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

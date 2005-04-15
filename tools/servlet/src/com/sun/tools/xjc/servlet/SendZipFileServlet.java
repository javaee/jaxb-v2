/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.IOException;

import javax.servlet.ServletException;

import com.sun.xml.bind.webapp.HttpServletEx;

/**
 * Sends a zip file to the client.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SendZipFileServlet extends HttpServletEx {

    protected void run() throws ServletException, IOException {
        Compiler compiler = Compiler.get(request);

        response.setContentType("application/x-zip-compressed");
        response.getOutputStream().write(compiler.getZipFile());
    }

}

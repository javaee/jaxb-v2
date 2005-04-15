/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

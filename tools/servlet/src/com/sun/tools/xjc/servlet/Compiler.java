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

import java.io.File;

import javax.servlet.http.HttpServletRequest;

/**
 * Runs the compilation in a separate thread.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class Compiler extends Thread {
    
    /**
     * Gets the compiler instance associated with the current client.
     */
    public static Compiler get( HttpServletRequest request ) {
        return (Compiler)request.getSession().getAttribute("compiler");
    }
    
    /**
     * Associates this instance to the current client.
     */
    public void associateTo( HttpServletRequest request ) {
        request.getSession().setAttribute("compiler", this);
    }

    public abstract File getOutDir();


    /**
     * This method returns the status message produced by the compiler.
     */
    public abstract String getStatusMessages();
    
    /**
     * If the compilation was successful, this method
     * returns the byte image of the source code zip file.
     */
    public abstract byte[] getZipFile();
}

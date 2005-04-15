/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.IOException;

import com.sun.xml.bind.webapp.AbstractTagImpl;

/**
 * Custom tag that generates the heading.
 * 
 * Synopsis:
 * 
 * <pre><xmp>
 * <header title="caption>
 *   some body text
 * </head>
 * </xmp></pre>
 *      
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class HeaderTag extends AbstractTagImpl {
    
    private String title;

    public void setTitle( String title ) {
        this.title = title; 
    }
    
    
    public int startTag() throws IOException {
        
        String image = getRequest().getContextPath()+"/javaxml.gif";
        
        context.getOut().write(
            "<table width=100%><tr><td>"+
              "<img style='float:left' src="+image+">"+
            "</td><td width=100%>"+
            "<h1>"+
            title+
            "</h1>"
            );
        
        return EVAL_BODY_INCLUDE;
    }

    public int endTag() throws IOException {
        context.getOut().write("<td></tr></table>");
        return EVAL_PAGE;
    }

}

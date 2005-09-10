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

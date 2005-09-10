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
package com.sun.xml.bind.webapp.tabpage;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.IterationTag;

import com.sun.xml.bind.webapp.AbstractTagImpl;

/**
 * Tab sheet.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TabSheetTag extends AbstractTagImpl implements IterationTag {
    
    /**
     * TabSheet runs the contents twice; the first pass is to
     * generate headers and the second pass is to generate the
     * body.
     */
    private Pass pass;
    
    /**
     * We'll count the number of pages during the first pass.
     */
    private int count=0;
    
    private String shadowColor = "#d0d0d0";
    
    
    /**
     * Configures the shadow color. JSP "shadowColor" attribute.
     */
    public void setShadowColor( String color ) {
        this.shadowColor = color;
    }
    public String getShadowColor() {
        return shadowColor;
    }
    public Pass getPass() {
        return pass;
    }
    public void incrementPageCount() {
        count++; 
    }
    
    public int startTag() throws IOException {
        count=0;
        pass = Pass.header;
                
        JspWriter w = context.getOut();
        w.write("<table border=0 bgcolor="+shadowColor+" cellspacing=3><tr><td>");
        w.write("<table border=0 bordercolor="+shadowColor+" cellpadding=3 cellspacing=0 bgcolor=#ffffff><tr>");
        
        return EVAL_BODY_INCLUDE;
    }

    public int endTag() throws IOException {
        JspWriter w = context.getOut();
        w.write("</td></tr></table>");
        w.write("</td></tr></table>");
        return EVAL_PAGE;
    }

    
    public int doAfterBody() throws JspException {
        if( pass==Pass.header ) {
            pass = Pass.body;
            
            try {
                context.getOut().write("</tr><tr><td colspan="+count+" bordercolor=#ffffff>");
            } catch (IOException e) {
                throw new JspTagException(e.getMessage());
            }
            
            return EVAL_BODY_AGAIN;
        } else
            return SKIP_BODY;
    }

}

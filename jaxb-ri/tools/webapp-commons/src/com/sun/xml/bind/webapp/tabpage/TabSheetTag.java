/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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

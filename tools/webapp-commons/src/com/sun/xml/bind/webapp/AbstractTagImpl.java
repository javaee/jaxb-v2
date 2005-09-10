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
package com.sun.xml.bind.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * Helper implementation of {@link Tag}.
 * 
 * Derived class should implement the startTag method
 * and the endTag method.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class AbstractTagImpl implements Tag {
    
    protected PageContext context;
    protected Tag parent;
    
    public void setPageContext(PageContext context) {
        this.context = context;
    }

    public void setParent(Tag parent) {
        this.parent = parent;
    }

    public Tag getParent() {
        return parent;
    }

    public void release() {
    }
    
    protected final HttpServletRequest getRequest() {
        return (HttpServletRequest)context.getRequest();
    }
    

    public final int doStartTag() throws JspException {
        try {
            return startTag();
        } catch( IOException e ) {
            e.printStackTrace();
            throw new JspException(e.getMessage());
        }
    }
    
    public final int doEndTag() throws JspException {
        try {
            return endTag();
        } catch( IOException e ) {
            e.printStackTrace();
            throw new JspException(e.getMessage());
        }
    }


    protected abstract int startTag() throws JspException, IOException;
    protected abstract int endTag() throws JspException, IOException;


}

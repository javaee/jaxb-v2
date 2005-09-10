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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.IterationTag;

import com.sun.xml.bind.webapp.*;

/**
 * Repeats the body for each zip entry in the file.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ZipTag extends AbstractTagImpl implements IterationTag {
    
    private ZipInputStream zip;
    private ZipEntry currentEntry;
    

    public ZipEntry getCurrentEntry() {
        return currentEntry;
    }

    public ZipInputStream getZip() {
        return zip;
    }

    
    public int startTag() throws IOException {
        Compiler c = (Compiler)getRequest().getSession().getAttribute("compiler");
        if(c==null)     return SKIP_BODY;
    
        zip = new ZipInputStream(new ByteArrayInputStream(c.getZipFile()));
        return next()?EVAL_BODY_INCLUDE:SKIP_BODY;
    }
    
    /**
     * Moves to the next zip entry.
     */
    private boolean next() throws IOException {
        currentEntry = zip.getNextEntry();
        if( currentEntry==null ) {
            zip.close();
            zip = null;     // release
            return false;
        }
        
        String name = currentEntry.getName();
        if( name.indexOf("/impl/")!=-1 || name.indexOf(".java")==-1 )
            return next();  // skip non-java file and impl file.
        
        return true;
    }

    public int doAfterBody() throws JspException {
        try {
            zip.closeEntry();
            return next()?EVAL_BODY_AGAIN:SKIP_BODY;
        } catch (IOException e) {
            throw new JspTagException(e.getMessage());
        }
    }


    public int endTag() {
        return EVAL_PAGE;
    }
}

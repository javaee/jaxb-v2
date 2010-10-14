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

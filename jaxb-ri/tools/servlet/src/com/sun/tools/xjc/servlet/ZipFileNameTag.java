/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.IOException;

import com.sun.xml.bind.webapp.AbstractTagImpl;

/**
 * Evaluates to the name of the current file in the parent zip file.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ZipFileNameTag extends AbstractTagImpl {

    public int startTag() {
        return SKIP_BODY;
    }

    public int endTag() throws IOException {
        context.getOut().write( ((ZipTag)parent).getCurrentEntry().getName() );
        return EVAL_PAGE;
    }
}

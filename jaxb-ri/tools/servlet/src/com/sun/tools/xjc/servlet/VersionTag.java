/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.IOException;

import com.sun.xml.bind.webapp.AbstractTagImpl;

/**
 * Custom tag that produces XJC build id.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class VersionTag extends AbstractTagImpl {

    public int startTag() {
        return SKIP_BODY;
    }

    public int endTag() throws IOException {
        // context.getOut().write( Driver.getBuildID() )
        try {
            Class driverClass = XJCClassLoader.getInstance(context.getServletContext())
                .loadClass("com.sun.tools.xjc.Driver");
            
            
            context.getOut().write( (String)
                driverClass.getMethod("getBuildID",new Class[0]).invoke(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return EVAL_PAGE;
    }
}

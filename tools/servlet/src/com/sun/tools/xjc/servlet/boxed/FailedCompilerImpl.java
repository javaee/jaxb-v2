/*
 * @(#)$Id: FailedCompilerImpl.java,v 1.1 2005-04-15 20:08:30 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet.boxed;

import java.io.File;

import com.sun.tools.xjc.servlet.Compiler;


/**
 * {@link Compiler} implementation that is used when
 * there was an error before invoking a compiler.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class FailedCompilerImpl extends Compiler {
    
    private final String errorMessage;
    
    FailedCompilerImpl( String _errorMessage ) {
        this.errorMessage = _errorMessage;
    }
    
    public File getOutDir() {
        throw new UnsupportedOperationException();
    }

    public String getStatusMessages() {
        return errorMessage;
    }

    public byte[] getZipFile() {
        return null;
    }
}

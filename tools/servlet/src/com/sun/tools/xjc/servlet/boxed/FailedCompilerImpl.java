/*
 * @(#)$Id: FailedCompilerImpl.java,v 1.2 2005-09-10 19:08:36 kohsuke Exp $
 */

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

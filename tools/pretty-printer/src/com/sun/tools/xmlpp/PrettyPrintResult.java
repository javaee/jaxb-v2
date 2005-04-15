/*
 * @(#)$Id: PrettyPrintResult.java,v 1.1 2005-04-15 20:08:21 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xmlpp;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.sax.SAXResult;

/**
 * {@link javax.xml.transform.Result} that goes through pretty-printing.  
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PrettyPrintResult extends SAXResult {
    public PrettyPrintResult(OutputStream out) {
        this(new OutputStreamWriter(out));
    }
    public PrettyPrintResult(Writer out) {
        XMLPrettyPrinter pp = new XMLPrettyPrinter(out);
        setHandler(pp);
        setLexicalHandler(pp);
    }
}

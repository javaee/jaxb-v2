/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.FilterReader;
import java.io.Reader;

/**
 * {@link Reader} implementation that ignores the close method.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class CloseProofReader extends FilterReader {

    public CloseProofReader(Reader in) {
        super(in);
    }

    public void close() {}
}

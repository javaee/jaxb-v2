/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.bind.v2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * {@link Source} implementation backed by {@link DataHandler}.
 *
 * <p>
 * This implementation allows the same {@link Source} to be used
 * mutliple times.
 *
 * <p>
 * {@link Source} isn't really pluggable. As a consequence,
 * this implementation is clunky --- weak against unexpected
 * usage of the class.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DataSourceSource extends StreamSource {
    private final DataSource source;

    /**
     * If null, default to the encoding declaration
     */
    private final String charset;

    // remember the value we returned so that the 2nd invocation
    // will return the same object, which is what's expeted out of
    // StreamSource
    private Reader r;
    private InputStream is;

    public DataSourceSource(DataHandler dh) throws MimeTypeParseException {
        this(dh.getDataSource());
    }

    public DataSourceSource(DataSource source) throws MimeTypeParseException {
        this.source = source;

        String ct = source.getContentType();
        if(ct==null) {
            charset = null;
        } else {
            MimeType mimeType = new MimeType(ct);
            this.charset = mimeType.getParameter("charset");
        }
    }

    @Override
    public void setReader(Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getReader() {
        try {
            if(charset==null)   return null;
            if(r==null)
                r = new InputStreamReader(source.getInputStream(),charset);
            return r;
        } catch (IOException e) {
            // argh
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            if(charset!=null)   return null;
            if(is==null)
                is = source.getInputStream();
            return is;
        } catch (IOException e) {
            // argh
            throw new RuntimeException(e);
        }
    }

    public DataSource getDataSource() {
        return source;
    }
}

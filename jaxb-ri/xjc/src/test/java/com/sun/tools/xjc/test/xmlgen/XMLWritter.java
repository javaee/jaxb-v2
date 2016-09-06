/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.xjc.test.xmlgen;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * XML document file writer with simple formating.
 */
public class XMLWritter implements Closeable {
    /** Target XSD document file. */
    final File file;

    /** Output writer. */
    final PrintWriter out;

    /** Indentation size. */
    final int indentSize;

    /** Indents count. */
    int indentCount;

    /** Indentation {@code String}. */
    String indent;

    /**
     * Creates an instance of Simple XSD file writer.
     * @param file Target XSD document file.
     * @param indentSize Indentation size.
     */
    public XMLWritter(final String file, final int indentSize) {
        this(new File(file), indentSize);
    }

    /**
     * Creates an instance of Simple XSD file writer.
     * @param file Target XSD document file.
     * @param indentSize Indentation size.
     */
    public XMLWritter(final File file, final int indentSize) {
        this.file = file;
        this.indentSize = indentSize;
        this.indentCount = 0;
        updateIndent();
        PrintWriter newWriter;
        try {
            newWriter = new PrintWriter(file);
        } catch (FileNotFoundException ex) {
            newWriter = null;
        }
        this.out = newWriter;
    }

    public void indentUp() {
        indentCount++;
        updateIndent();
    }

    public void indentDown() {
        if (indentCount == 0) {
            throw new IllegalStateException("Could not lower indentation bellow 0.");
        }
        indentCount--;
        updateIndent();
    }

    private void updateIndent() {
        final int len = indentCount * indentSize;
        final char[] chars = new char[len];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = ' ';
        }
        indent = new String(chars);
    }

    /**
     * Print indentation to the XSD file.
     * @return current XML writer.
     */
    public XMLWritter printIndent() {
        out.print(indent);
        return this;
    }

    /**
     * Print end of line to the XSD file.
     * @return current XML writer.
     */
    public XMLWritter printEol() {
        out.println();
        return this;
    }

    /**
     * Write {@code String}s as indented single line to the XSD file.
     * @param strings {@code String}s to be written to the XSD file.
     * @return current XML writer.
     */
    public XMLWritter println(final String ...strings) {
        out.print(indent);
        if (strings != null) {
            print(strings);
        }
        out.println();
        return this;
    }

    /**
     * Write {@code String}s to the XSD file without indentation.
     * @param strings {@code String}s to be written to the XSD file.
     * @return current XML writer.
     */
    public XMLWritter print(final String ...strings) {
        if (strings != null) {
            for (int i = 0; i < strings.length; i++) {
                out.print(strings[i]);
            }
        }
        return this;
    }

    @Override
    public void close() {
        if (out != null) {
            out.close();
        }
    }

}

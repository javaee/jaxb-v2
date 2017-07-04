/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.txw2.output;

import java.io.PrintStream;

/**
 * Shows the call sequence of {@link XmlSerializer} methods.
 *
 * Useful for debugging and learning how TXW works.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class DumpSerializer implements XmlSerializer {
    private final PrintStream out;

    public DumpSerializer(PrintStream out) {
        this.out = out;
    }

    public void beginStartTag(String uri, String localName, String prefix) {
        out.println('<'+prefix+':'+localName);
    }

    public void writeAttribute(String uri, String localName, String prefix, StringBuilder value) {
        out.println('@'+prefix+':'+localName+'='+value);
    }

    public void writeXmlns(String prefix, String uri) {
        out.println("xmlns:"+prefix+'='+uri);
    }

    public void endStartTag(String uri, String localName, String prefix) {
        out.println('>');
    }

    public void endTag() {
        out.println("</  >");
    }

    public void text(StringBuilder text) {
        out.println(text);
    }

    public void cdata(StringBuilder text) {
        out.println("<![CDATA[");
        out.println(text);
        out.println("]]>");
    }

    public void comment(StringBuilder comment) {
        out.println("<!--");
        out.println(comment);
        out.println("-->");
    }

    public void startDocument() {
        out.println("<?xml?>");
    }

    public void endDocument() {
        out.println("done");
    }

    public void flush() {
        out.println("flush");
    }
}

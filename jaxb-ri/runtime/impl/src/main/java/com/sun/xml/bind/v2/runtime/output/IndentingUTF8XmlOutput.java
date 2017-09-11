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

package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.runtime.Name;

import org.xml.sax.SAXException;

/**
 * {@link UTF8XmlOutput} with indentation.
 *
 * TODO: not sure if it's a good idea to move the indenting functionality to another class.
 *
 * Doesn't have to be final, but it helps the JVM.
 *
 * @author Kohsuke Kawaguchi
 */
public final class IndentingUTF8XmlOutput extends UTF8XmlOutput {

    /**
     * Null if the writer should perform no indentation.
     *
     * Otherwise this will keep the 8 copies of the string for indentation.
     * (so that we can write 8 indentation at once.)
     */
    private final Encoded indent8;

    /**
     * Length of one indentation.
     */
    private final int unitLen;

    private int depth = 0;

    private boolean seenText = false;

    /**
     *
     * @param indentStr
     *      set to null for no indentation and optimal performance.
     *      otherwise the string is used for indentation.
     */
    public IndentingUTF8XmlOutput(OutputStream out, String indentStr, Encoded[] localNames, CharacterEscapeHandler escapeHandler) {
        super(out, localNames, escapeHandler);

        if(indentStr!=null) {
            Encoded e = new Encoded(indentStr);
            indent8 = new Encoded();
            indent8.ensureSize(e.len*8);
            unitLen = e.len;
            for( int i=0; i<8; i++ )
                System.arraycopy(e.buf, 0, indent8.buf, unitLen*i, unitLen);
        } else {
            this.indent8 = null;
            this.unitLen = 0;
        }
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException {
        indentStartTag();
        super.beginStartTag(prefix, localName);
    }

    @Override
    public void beginStartTag(Name name) throws IOException {
        indentStartTag();
        super.beginStartTag(name);
    }

    private void indentStartTag() throws IOException {
        closeStartTag();
        if(!seenText)
            printIndent();
        depth++;
        seenText = false;
    }

    @Override
    public void endTag(Name name) throws IOException {
        indentEndTag();
        super.endTag(name);
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException {
        indentEndTag();
        super.endTag(prefix, localName);
    }

    private void indentEndTag() throws IOException {
        depth--;
        if(!closeStartTagPending && !seenText)
            printIndent();
        seenText = false;
    }

    private void printIndent() throws IOException {
        write('\n');
        int i = depth%8;

        write( indent8.buf, 0, i*unitLen );

        i>>=3;  // really i /= 8;

        for( ; i>0; i-- )
            indent8.write(this);
    }

    @Override
    public void text(String value, boolean needSP) throws IOException {
        seenText = true;
        super.text(value, needSP);
    }

    @Override
    public void text(Pcdata value, boolean needSP) throws IOException {
        seenText = true;
        super.text(value, needSP);
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        write('\n');
        super.endDocument(fragment);
    }
}

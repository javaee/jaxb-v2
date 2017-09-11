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

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

import org.xml.sax.SAXException;

/**
 * Abstract implementation of {@link XmlOutput}
 *
 * Implements the optimal methods, where defer to 
 * the non-optimal methods.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class XmlOutputAbstractImpl implements XmlOutput {
//
//
// Contracts
//
//
    /**
     * Called at the very beginning.
     *
     * @param serializer
     *      the {@link XMLSerializer} that coordinates this whole marshalling episode.
     * @param fragment
     *      true if we are marshalling a fragment.
     */
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        this.nsUriIndex2prefixIndex = nsUriIndex2prefixIndex;
        this.nsContext = nsContext;
        this.serializer = serializer;
    }

    /**
     * Called at the very end.
     *
     * @param fragment
     *      false if we are writing the whole document.
     */
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        serializer = null;
    }

    /**
     * Writes a start tag.
     *
     * <p>
     * At this point {@link #nsContext} holds namespace declarations needed for this
     * new element.
     *
     * <p>
     * This method is used for writing tags that are indexed.
     */
    public void beginStartTag(Name name) throws IOException, XMLStreamException {
        beginStartTag( nsUriIndex2prefixIndex[name.nsUriIndex], name.localName );
    }

    public abstract void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException;

    public void attribute( Name name, String value ) throws IOException, XMLStreamException {
        short idx = name.nsUriIndex;
        if(idx==-1)
            attribute(-1,name.localName, value);
        else
            attribute( nsUriIndex2prefixIndex[idx], name.localName, value );
    }
    /**
     * @param prefix
     *      -1 if this attribute does not have a prefix
     *      (this handling differs from that of elements.)
     */
    public abstract void attribute( int prefix, String localName, String value ) throws IOException, XMLStreamException;

    public abstract void endStartTag() throws IOException, SAXException;

    public void endTag(Name name) throws IOException, SAXException, XMLStreamException {
        endTag( nsUriIndex2prefixIndex[name.nsUriIndex], name.localName);
    }
    public abstract void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException;




//
//
// Utilities for implementations
//
//
    /**
     * The conversion table from the namespace URI index to prefix index.
     *
     * This array is shared with {@link XMLSerializer} and
     * is updated by it automatically.
     *
     * This allows {@link Name#nsUriIndex} to be converted to prefix index
     * (for {@link NamespaceContextImpl}) quickly.
     */
    protected int[] nsUriIndex2prefixIndex;

    /**
     * Set by the marshaller before the start tag is written for the root element.
     */
    protected NamespaceContextImpl nsContext;

    protected XMLSerializer serializer;
}

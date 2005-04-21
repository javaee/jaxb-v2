/* $Id: StAXReaderToContentHandler.java,v 1.1 2005-04-21 00:01:55 kohsuke Exp $
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.xml.bind.v2.stax;

import javax.xml.stream.XMLStreamException;

/**
 * Common API's for adapting StAX events from {@link javax.xml.stream.XMLStreamReader}
 * and {@link javax.xml.stream.XMLEventReader} into SAX events on the specified
 * {@link org.xml.sax.ContentHandler}.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public interface StAXReaderToContentHandler {

    /**
     * Perform the conversion from StAX events to SAX events.
     *
     * <p>
     * The StAX parser must be pointing at the start element or the start document.
     * The method reads the parser until it hits the corresponding end element,
     * and turns the complete sub-tree into the equivalent of the SAX events.
     *
     * <p>
     * The receiver of the SAX event will see this sub-tree as if it were
     * a whole document.
     *
     * <p>
     * When this method returns successfully, the parser is at the next token
     * of the end element.
     *
     * @throws XMLStreamException
     *                 if any errors are encountered while parsing XML from the
     *                 XMLStreamReader or firing events on the ContentHandler.
     */
    public void bridge() throws XMLStreamException;
}
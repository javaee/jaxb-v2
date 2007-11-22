/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.xjc.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * ContentHandler that "forks" the incoming SAX2 events to
 * two ContentHandlers.
 *
 *
 * @version	$Id: ForkContentHandler.java,v 1.3 2007-11-22 00:54:16 kohsuke Exp $
 * @author  <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class ForkContentHandler implements ContentHandler {

	/**
	 * Creates a ForkContentHandler.
	 *
	 * @param first
	 *     This handler will receive a SAX event first.
	 * @param second
	 *     This handler will receive a SAX event after the first handler
	 *     receives it.
	 */
	public ForkContentHandler( ContentHandler first, ContentHandler second ) {
		lhs = first;
		rhs = second;
	}

	/**
	 * Creates ForkContentHandlers so that the specified handlers
	 * will receive SAX events in the order of the array.
	 */
	public static ContentHandler create( ContentHandler[] handlers ) {
		if(handlers.length==0)
			throw new IllegalArgumentException();

		ContentHandler result = handlers[0];
		for( int i=1; i<handlers.length; i++ )
			result = new ForkContentHandler( result, handlers[i] );
		return result;
	}


	private final ContentHandler lhs,rhs;

	public void setDocumentLocator (Locator locator) {
		lhs.setDocumentLocator(locator);
		rhs.setDocumentLocator(locator);
	}

	public void startDocument() throws SAXException {
		lhs.startDocument();
		rhs.startDocument();
	}

	public void endDocument () throws SAXException {
		lhs.endDocument();
		rhs.endDocument();
	}

	public void startPrefixMapping (String prefix, String uri) throws SAXException {
		lhs.startPrefixMapping(prefix,uri);
		rhs.startPrefixMapping(prefix,uri);
	}

	public void endPrefixMapping (String prefix) throws SAXException {
		lhs.endPrefixMapping(prefix);
		rhs.endPrefixMapping(prefix);
	}

	public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
		lhs.startElement(uri,localName,qName,attributes);
		rhs.startElement(uri,localName,qName,attributes);
	}

	public void endElement (String uri, String localName, String qName) throws SAXException {
		lhs.endElement(uri,localName,qName);
		rhs.endElement(uri,localName,qName);
	}

	public void characters (char ch[], int start, int length) throws SAXException {
		lhs.characters(ch,start,length);
		rhs.characters(ch,start,length);
	}

	public void ignorableWhitespace (char ch[], int start, int length) throws SAXException {
		lhs.ignorableWhitespace(ch,start,length);
		rhs.ignorableWhitespace(ch,start,length);
	}

	public void processingInstruction (String target, String data) throws SAXException {
		lhs.processingInstruction(target,data);
		rhs.processingInstruction(target,data);
	}

	public void skippedEntity (String name) throws SAXException {
		lhs.skippedEntity(name);
		rhs.skippedEntity(name);
	}

}
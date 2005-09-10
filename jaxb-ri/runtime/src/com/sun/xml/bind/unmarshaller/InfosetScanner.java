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
package com.sun.xml.bind.unmarshaller;

import javax.xml.bind.Binder;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Visits a DOM-ish API and generates SAX events.
 * 
 * <p>
 * This interface is not tied to any particular DOM API.
 * Used by the {@link Binder}.
 * 
 * <p>
 * Since we are parsing a DOM-ish tree, I don't think this
 * scanner itself will ever find an error, so this class
 * doesn't have its own error reporting scheme.
 * 
 * <p>
 * This interface <b>MAY NOT</b> be implemented by the generated
 * runtime nor the generated code. We may add new methods on
 * this interface later. This is to be implemented by the static runtime
 * only.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since 2.0
 */
public interface InfosetScanner<XmlNode> {
    /**
     * Parses the given DOM-ish element/document and generates
     * SAX events.
     * 
     * @throws ClassCastException
     *      If the type of the node is not known to this implementation.
     * 
     * @throws SAXException
     *      If the {@link ContentHandler} throws a {@link SAXException}.
     *      Do not throw an exception just because the scanner failed
     *      (if that can happen we need to change the API.)
     */
    void scan( XmlNode node ) throws SAXException;
    
    /**
     * Sets the {@link ContentHandler}.
     * 
     * This handler receives the SAX events.
     */
    void setContentHandler( ContentHandler handler );
    ContentHandler getContentHandler();
    
    /**
     * Gets the current element we are parsing.
     * 
     * <p>
     * This method could
     * be called from the {@link ContentHandler#startElement(String, String, String, Attributes)}
     * or {@link ContentHandler#endElement(String, String, String)}.
     * 
     * <p>
     * Otherwise the behavior of this method is undefined.
     * 
     * @return
     *      never return null.
     */
    XmlNode getCurrentElement();

    LocatorEx getLocator();
}

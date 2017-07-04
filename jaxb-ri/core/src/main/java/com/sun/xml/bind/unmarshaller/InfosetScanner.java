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

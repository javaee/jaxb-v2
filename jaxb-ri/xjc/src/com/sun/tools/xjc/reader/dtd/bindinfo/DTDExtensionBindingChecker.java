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

package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.AbstractExtensionBindingChecker;
import com.sun.tools.xjc.reader.Const;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/**
 * {@link XMLFilter} that checks the use of extension namespace URIs
 * (to see if they have corresponding plugins), and otherwise report an error.
 *
 * <p>
 * This code also masks the recognized extensions from the validator that
 * will be plugged as the next component to this.
 *
 * @author Kohsuke Kawaguchi
 */
final class DTDExtensionBindingChecker extends AbstractExtensionBindingChecker {
    public DTDExtensionBindingChecker(String schemaLanguage, Options options, ErrorHandler handler) {
        super(schemaLanguage, options, handler);
    }

    /**
     * Returns true if the elements with the given namespace URI
     * should be blocked by this filter.
     */
    private boolean needsToBePruned( String uri ) {
        if( uri.equals(schemaLanguage) )
            return false;
        if( uri.equals(Const.JAXB_NSURI) )
            return false;
        if( uri.equals(Const.XJC_EXTENSION_URI) )
            return false;
        // we don't want validator to see extensions that we understand ,
        // because they will complain.
        // OTOH, if  this is an extension that we didn't understand,
        // we want the validator to report an error
        return enabledExtensions.contains(uri);
    }



    public void startElement(String uri, String localName, String qName, Attributes atts)
        throws SAXException {

        if( !isCutting() ) {
            if(!uri.equals("")) {
                // "" is the standard namespace
                checkAndEnable(uri);

                verifyTagName(uri, localName, qName);

                if(needsToBePruned(uri))
                    startCutting();
            }
        }

        super.startElement(uri, localName, qName, atts);
    }
}

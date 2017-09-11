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

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

/**
 * Factory for producing XmlSerializers for various Result types.
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public abstract class ResultFactory {

    /**
     * Do not instanciate.
     */
    private ResultFactory() {}

    /**
     * Factory method for producing {@link XmlSerializer} from {@link javax.xml.transform.Result}.
     *
     * This method supports {@link javax.xml.transform.sax.SAXResult},
     * {@link javax.xml.transform.stream.StreamResult}, and {@link javax.xml.transform.dom.DOMResult}.
     *
     * @param result the Result that will receive output from the XmlSerializer 
     * @return an implementation of XmlSerializer that will produce output on the supplied Result
     */
    public static XmlSerializer createSerializer(Result result) {
        if (result instanceof SAXResult)
            return new SaxSerializer((SAXResult) result);
        if (result instanceof DOMResult)
            return new DomSerializer((DOMResult) result);
        if (result instanceof StreamResult)
            return new StreamSerializer((StreamResult) result);
        if (result instanceof TXWResult)
            return new TXWSerializer(((TXWResult)result).getWriter());

        throw new UnsupportedOperationException("Unsupported Result type: " + result.getClass().getName());
    }

}

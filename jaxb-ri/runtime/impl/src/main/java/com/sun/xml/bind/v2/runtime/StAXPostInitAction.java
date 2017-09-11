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

package com.sun.xml.bind.v2.runtime;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

/**
 * Post-init action for {@link MarshallerImpl} that incorporate the in-scope namespace bindings
 * from a StAX writer.
 *
 * <p>
 * It's always used either with {@link XMLStreamWriter}, {@link XMLEventWriter}, or bare
 * {@link NamespaceContext},
 * but to reduce the # of classes in the runtime I wrote only one class that handles both.
 *
 * @author Kohsuke Kawaguchi
 */
final class StAXPostInitAction implements Runnable {
    private final XMLStreamWriter xsw;
    private final XMLEventWriter xew;
    private final NamespaceContext nsc;
    private final XMLSerializer serializer;

    StAXPostInitAction(XMLStreamWriter xsw,XMLSerializer serializer) {
        this.xsw = xsw;
        this.xew = null;
        this.nsc = null;
        this.serializer = serializer;
    }

    StAXPostInitAction(XMLEventWriter xew,XMLSerializer serializer) {
        this.xsw = null;
        this.xew = xew;
        this.nsc = null;
        this.serializer = serializer;
    }

    StAXPostInitAction(NamespaceContext nsc,XMLSerializer serializer) {
        this.xsw = null;
        this.xew = null;
        this.nsc = nsc;
        this.serializer = serializer;
    }

    public void run() {
        NamespaceContext ns = nsc;
        if(xsw!=null)   ns = xsw.getNamespaceContext();
        if(xew!=null)   ns = xew.getNamespaceContext();

        // StAX javadoc isn't very clear on the behavior,
        // so work defensively in anticipation of broken implementations.
        if(ns==null)
            return;

        // we can't enumerate all the in-scope namespace bindings in StAX,
        // so we only look for the known static namespace URIs.
        // this is less than ideal, but better than nothing.
        for( String nsUri : serializer.grammar.nameList.namespaceURIs ) {
            String p = ns.getPrefix(nsUri);
            if(p!=null)
                serializer.addInscopeBinding(nsUri,p);
        }
    }
}

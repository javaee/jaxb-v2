/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.bind.v2.schemagen;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;

import com.sun.xml.bind.Util;

/**
 * {@link SchemaOutputResolver} that wraps the user-specified resolver
 * and makes sure that it's following the contract.
 *
 * <p>
 * This protects the rest of the {@link XmlSchemaGenerator} from client programming
 * error.
 */
final class FoolProofResolver extends SchemaOutputResolver {
    private static final Logger logger = Util.getClassLogger();
    private final SchemaOutputResolver resolver;

    public FoolProofResolver(SchemaOutputResolver resolver) {
        assert resolver!=null;
        this.resolver = resolver;
    }

    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        logger.entering(getClass().getName(),"createOutput",new Object[]{namespaceUri,suggestedFileName});
        Result r = resolver.createOutput(namespaceUri,suggestedFileName);
        if(r!=null) {
            String sysId = r.getSystemId();
            logger.finer("system ID = "+sysId);
            if(sysId!=null) {
                // TODO: make sure that the system Id is absolute

                // don't use java.net.URI, because it doesn't allow some characters (like SP)
                // which can legally used as file names.

                // but don't use java.net.URL either, because it doesn't allow a made-up URI
                // like kohsuke://foo/bar/zot
            } else
                throw new AssertionError("system ID cannot be null");
        }
        logger.exiting(getClass().getName(),"createOutput",r);
        return r;
    }
}

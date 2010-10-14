/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.servlet.boxed;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.xml.sax.InputSource;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.servlet.CloseProofReader;
import com.sun.tools.xjc.servlet.Compiler;
import com.sun.xml.bind.webapp.SecureEntityResolver;
import com.sun.xml.bind.webapp.multipart.MultiPartRequest;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SubmissionParser {
    
    private static InputSource getSourceFromBinary( MultiPartRequest multiPart, String name) {
        InputSource is = new InputSource(multiPart.getInputStream(name));
        is.setSystemId("post://"+multiPart.getFilename(name));
        return is;
    }
    
    private static InputSource getSourceFromString( MultiPartRequest multiPart, String name, String systemId) {
        InputSource is = new InputSource(
            new CloseProofReader(new StringReader(multiPart.getString(name))));
        is.setSystemId(systemId);
        return is;
    }
    
    /** Parses a submission into a Compiler object. */
    public static Compiler parse( HttpServletRequest request ) throws IOException {
        // parse parameter
        MultiPartRequest multiPart = new MultiPartRequest(request);

        Options opt = new Options();
        opt.defaultPackage="temp";
        opt.entityResolver = new SecureEntityResolver();

        if( multiPart.has("package") )
            opt.defaultPackage = multiPart.getString("package");
        if( multiPart.has("language") ) {
            String language = multiPart.getString("language");
            if( language.equals("rng") )
                opt.setSchemaLanguage(Options.SCHEMA_RELAXNG);
            else
            if( language.equals("dtd") )
                opt.setSchemaLanguage(Options.SCHEMA_DTD);
            else
                opt.setSchemaLanguage(Options.SCHEMA_XMLSCHEMA);
        }
        if( multiPart.has("extension") ) {
            opt.compatibilityMode = Options.EXTENSION;
        }
        
        
        if( multiPart.has("schema") )
            opt.addGrammar (getSourceFromBinary(multiPart, "schema"));
        if( multiPart.has("binding") )
            opt.addBindFile(getSourceFromBinary(multiPart, "binding"));
        
        if( multiPart.has("schemaURL") )
            opt.addGrammar (new InputSource(multiPart.getString("schemaURL")));
        if( multiPart.has("bindingURL") )
            opt.addBindFile(new InputSource(multiPart.getString("bindingURL")));
        
        if( multiPart.has("schemaLiteral") )
            opt.addGrammar (getSourceFromString(multiPart, "schemaLiteral", "http://dummy/test.xsd"));
        if( multiPart.has("bindingLiteral") )
            opt.addBindFile(getSourceFromString(multiPart, "bindingLiteral", "http://dummy/test.jaxb"));
        
        if( opt.getGrammars().length==0 )
            return new FailedCompilerImpl("no schema was specified");
        // launch compilation
        return new CompilerImpl(opt,request);
    }
}

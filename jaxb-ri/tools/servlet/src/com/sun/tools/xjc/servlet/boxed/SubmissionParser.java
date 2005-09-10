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

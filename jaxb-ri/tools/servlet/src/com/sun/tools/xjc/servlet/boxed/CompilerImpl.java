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
package com.sun.tools.xjc.servlet.boxed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.http.HttpServletRequest;

import org.xml.sax.SAXException;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.PrologCodeWriter;
import com.sun.codemodel.writer.ZipCodeWriter;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.GrammarLoader;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.servlet.Compiler;
import com.sun.tools.xjc.servlet.Dialer;
import com.sun.tools.xjc.servlet.Mode;
import com.sun.tools.xjc.servlet.Unzipper;
import com.sun.tools.xjc.servlet.reaper.DiskManagerServlet;

/**
 * Runs the compilation in a separate thread.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class CompilerImpl extends Compiler {
    
    private final Options opt;
    
    /** Status message produced by the compiler. */
    private String status = "";
    
    /** Image of the zip file. */
    private byte[] zipFile;
    
    public final File outDir;

    private final String remoteHost;
    
    /** Used to limit the number of concurrent compilation to 1. */
    private static final Object lock = new Object();
    
    
    public CompilerImpl(Options _opt, HttpServletRequest request ) throws IOException {
        this.opt = _opt;
        this.remoteHost = request.getRemoteHost();
        
        opt.strictCheck = false;
        
        outDir = DiskManagerServlet.createOutDir();
        
        // be cooperative
        setPriority(Thread.NORM_PRIORITY-1);
    }
    
    public File getOutDir() {
        return outDir;
    }

    /**
     * This method returns the status message produced by the compiler.
     */
    public String getStatusMessages() {
        return status;
    }
    
    /**
     * If the compilation was successful, this method
     * returns the byte image of the source code zip file.
     */
    public byte[] getZipFile() {
        return zipFile;
    }
    
    public void run() {
        synchronized(lock) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream status = new PrintStream(baos);
            
            try {
                // status output will be sent to this object.
                
                // load a schema
                AnnotatedGrammar grammar = null;
                ErrorReceiver errorReceiver = new ConsoleErrorReporter(status,true,opt.quiet);
                
                try {
                    grammar = GrammarLoader.load( opt, new JCodeModel(), errorReceiver );
                } catch( SAXException e ) {
                    status.println("Unexpected failure:\n");
                    e.printStackTrace(status);
                    return;
                }
                
                if(grammar==null)
                    return; // failed to compile
                
                
                // generate code and return to the client.
                try {
                    if( Driver.generateCode(grammar,opt,errorReceiver)!= null ) {
                        ByteArrayOutputStream zip = new ByteArrayOutputStream();
                        CodeWriter cw = new ZipCodeWriter(zip);
                        cw = new PrologCodeWriter( cw, getPrologMessage() );
                        cw = Driver.createCodeWriter(cw); // put normal JAXB RI prolog
                        grammar.codeModel.build(cw);
                        zipFile = zip.toByteArray();
                        
                        if( Mode.canUseDisk )
                            new Unzipper(outDir).unzip(new ByteArrayInputStream(zipFile));
                    }
                } catch( AbortException e ) {
                    // the error message should have been reported already
                }
                
                status.println("\ndone.");
                
            } catch( Throwable e ) {
                // catch any error and send them to the status so that we can
                // investigate them later.
                status.println("Unexpected failure:\n");
                e.printStackTrace(status);
            } finally {
                status.close();
                this.status = new String(baos.toByteArray());
            }
        }
        
        if( !Mode.inJWSDP )
            // send back the result, but not when it's in JWSDP
            new Dialer(this,remoteHost).start();
    }

    private String getPrologMessage() {
        return
            "WARNING:\n"+
            "We don't recommend you to use this for a product. \n" +            "This source code is generated from an early access release to JAXB RI. \n" +            "It doesn't go through the rigorous testing the formal releases \n" +            "go through, and therefore it may contain bugs. We may change \n" +            "the behavior of the compiler and that can break your code."; 
    }
}
